package com.example.cyclemartberemake.service.impl;

import com.example.cyclemartberemake.dto.request.InspectionRequestDTO;
import com.example.cyclemartberemake.dto.response.CreatePaymentResponse;
import com.example.cyclemartberemake.dto.response.InspectionResponseDTO;
import com.example.cyclemartberemake.entity.*;
import com.example.cyclemartberemake.repository.BikePostRepository;
import com.example.cyclemartberemake.repository.InspectionRepository;
import com.example.cyclemartberemake.repository.PaymentRepository;
import com.example.cyclemartberemake.repository.UserRepository;
import com.example.cyclemartberemake.repository.FeeInspectionSettingRepository;
import com.example.cyclemartberemake.service.InspectionService;
import com.example.cyclemartberemake.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InspectionServiceImpl implements InspectionService {

    private final InspectionRepository inspectionRepository;
    private final BikePostRepository bikePostRepository;
    private final UserRepository userRepository;
    private final FeeInspectionSettingRepository feeInspectionSettingRepository;
    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;

    private static final String FEE_KEY = "GLOBAL_INSPECTION_FEE";

    @Override
    @Transactional
    public InspectionResponseDTO createRequest(InspectionRequestDTO request) {
        Users currentUser = getCurrentUser();

        BikePost post = bikePostRepository.findById(request.getPostId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài đăng"));
        if (post.getPostStatus() != PostStatus.APPROVED) {
            throw new RuntimeException("Chỉ những bài đăng đã được duyệt mới có thể yêu cầu kiểm định lẻ.");
        }
        if (!post.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Bạn không có quyền yêu cầu kiểm định cho xe này");
        }

        boolean isPending = inspectionRepository.existsByBikePostIdAndStatusIn(
                post.getId(),
                Arrays.asList(InspectionStatus.PENDING_PAYMENT, InspectionStatus.PENDING, InspectionStatus.ASSIGNED, InspectionStatus.INSPECTING)
        );
        if (isPending) {
            throw new RuntimeException("Xe này đang trong quá trình xử lý kiểm định rồi!");
        }

        Double currentFee = getGlobalInspectionFee();

        Inspection inspection = Inspection.builder()
                .bikePost(post)
                .seller(currentUser)
                .address(request.getAddress())
                .scheduledDateTime(request.getScheduledDateTime())
                .note(request.getNote())
                .inspectionFee(currentFee)
                .status(InspectionStatus.PENDING_PAYMENT)
                .build();

        Inspection saved = inspectionRepository.save(inspection);

        // Tạo link thanh toán phí kiểm định
        InspectionResponseDTO response = mapToResponse(saved);
        try {
            CreatePaymentResponse paymentResponse = paymentService.createInspectionPayment(
                    currentUser.getId(), saved.getId(), currentFee);
            response.setPaymentUrl(paymentResponse.getPaymentUrl());
            response.setPaymentOrderId(paymentResponse.getOrderId());
        } catch (Exception e) {
            // Không block nếu tạo link thanh toán lỗi, seller có thể tạo lại
        }
        return response;
    }

    @Override
    public Double getGlobalInspectionFee() {
        return feeInspectionSettingRepository.findById(FEE_KEY)
                .map(s -> Double.parseDouble(s.getSettingValue()))
                .orElse(250000.0); // Mặc định là 250k nếu chưa có trong DB
    }

    @Override
    @Transactional
    public void updateGlobalInspectionFee(Double fee) {
        FeeInspectionSetting setting = feeInspectionSettingRepository.findById(FEE_KEY)
                .orElse(FeeInspectionSetting.builder().settingKey(FEE_KEY).build());
        setting.setSettingValue(String.valueOf(fee));
        feeInspectionSettingRepository.save(setting);
    }

    @Override
    public Page<InspectionResponseDTO> getMyRequests(Pageable pageable) {
        Users currentUser = getCurrentUser();
        return inspectionRepository.findBySellerId(currentUser.getId(), pageable).map(this::mapToResponse);
    }

    @Override
    public Page<InspectionResponseDTO> getAllRequests(Pageable pageable) {
        return inspectionRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Override
    public Page<InspectionResponseDTO> getRequestsForInspector(Pageable pageable) {
        Users currentInspector = getCurrentUser();
        return inspectionRepository.findByInspectorId(currentInspector.getId(), pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional
    public void assignInspector(Long inspectionId, Long inspectorId) {
        Inspection inspection = inspectionRepository.findById(inspectionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu"));

        if (inspection.getStatus() == InspectionStatus.PENDING_PAYMENT) {
            throw new RuntimeException("Yêu cầu kiểm định này chưa được thanh toán phí. Không thể phân công Inspector.");
        }

        Users inspector = userRepository.findById(inspectorId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Inspector"));

        if (inspector.getRole() != Role.INSPECTOR) {
            throw new RuntimeException("Người dùng này không có quyền Inspector");
        }

        LocalDateTime proposedTime = inspection.getScheduledDateTime();
        List<Inspection> activeTasks = inspectionRepository.findByInspectorIdAndStatusIn(
                inspectorId,
                Arrays.asList(InspectionStatus.ASSIGNED, InspectionStatus.INSPECTING, InspectionStatus.PASSED)
        );

        for (Inspection task : activeTasks) {
            if (task.getId().equals(inspectionId)) continue;
            long minutesDiff = Math.abs(java.time.Duration.between(proposedTime, task.getScheduledDateTime()).toMinutes());
            if (minutesDiff < 120) {
                String timeStr = task.getScheduledDateTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm dd/MM"));
                throw new RuntimeException("Inspector này đã có lịch lúc " + timeStr + ". Vui lòng chọn người khác hoặc đổi khung giờ (cách ít nhất 2 tiếng).");
            }
        }

        inspection.setInspector(inspector);
        inspection.setStatus(InspectionStatus.ASSIGNED);
        inspectionRepository.save(inspection);
    }

    @Override
    @Transactional
    public void updateResult(Long inspectionId, String statusStr, String resultNote, String checklistData) {
        Users currentInspector = getCurrentUser();
        Inspection inspection = inspectionRepository.findById(inspectionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu"));

        if (inspection.getInspector() == null || !inspection.getInspector().getId().equals(currentInspector.getId())) {
            throw new RuntimeException("Bạn không được phân công kiểm định yêu cầu này");
        }

        if (inspection.getStatus() != InspectionStatus.ASSIGNED && inspection.getStatus() != InspectionStatus.INSPECTING) {
            throw new RuntimeException("Yêu cầu kiểm định không ở trạng thái có thể cập nhật kết quả");
        }

        InspectionStatus newStatus = InspectionStatus.valueOf(statusStr.toUpperCase());
        inspection.setStatus(newStatus);
        inspection.setResultNote(resultNote);
        inspection.setChecklistData(checklistData);

        if (newStatus == InspectionStatus.PASSED) {
            BikePost post = inspection.getBikePost();
            post.setIsVerified(true);
            bikePostRepository.save(post);
        }

        if (newStatus == InspectionStatus.FAILED) {
            BikePost post = inspection.getBikePost();
            post.setIsVerified(false);
            bikePostRepository.save(post);
        }

        inspectionRepository.save(inspection);
    }

    @Override
    public InspectionResponseDTO resumePayment(Long inspectionId) {
        Users currentUser = getCurrentUser();
        Inspection inspection = inspectionRepository.findById(inspectionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu kiểm định"));

        if (!inspection.getSeller().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Bạn không có quyền thao tác với yêu cầu này");
        }
        if (inspection.getStatus() != InspectionStatus.PENDING_PAYMENT) {
            throw new RuntimeException("Yêu cầu này không cần thanh toán hoặc đã được thanh toán");
        }

        Payment pendingPayment = paymentRepository.findFirstByReferenceIdAndTypeAndStatus(
                inspectionId, PaymentType.INSPECTION_FEE, PaymentStatus.PENDING)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giao dịch thanh toán cho yêu cầu này"));

        try {
            String freshUrl = paymentService.generateFreshPaymentUrl(
                    pendingPayment.getOrderId(), pendingPayment.getAmount());
            InspectionResponseDTO dto = mapToResponse(inspection);
            dto.setPaymentOrderId(pendingPayment.getOrderId());
            dto.setPaymentUrl(freshUrl);
            return dto;
        } catch (Exception e) {
            throw new RuntimeException("Không thể tạo link thanh toán. Vui lòng thử lại.");
        }
    }

    private Users getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Users) {
            return (Users) auth.getPrincipal();
        }
        throw new RuntimeException("Chưa đăng nhập");
    }

    private InspectionResponseDTO mapToResponse(Inspection entity) {
        return InspectionResponseDTO.builder()
                .id(entity.getId())
                .postId(entity.getBikePost() != null ? entity.getBikePost().getId() : null)
                .postTitle(entity.getBikePost() != null ? entity.getBikePost().getTitle() : null)
                .sellerName(entity.getSeller() != null ? entity.getSeller().getFullName() : null)
                .sellerPhone(entity.getSeller() != null ? entity.getSeller().getPhone() : null)
                .inspectorId(entity.getInspector() != null ? entity.getInspector().getId() : null)
                .inspectorName(entity.getInspector() != null ? entity.getInspector().getFullName() : null)
                .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                .address(entity.getAddress())
                .scheduledDateTime(entity.getScheduledDateTime())
                .inspectionFee(entity.getInspectionFee())
                .sellerNote(entity.getNote()) // Map từ 'note' của Entity sang 'sellerNote' của DTO
                .resultNote(entity.getResultNote())
                .checklistData(entity.getChecklistData())
                .createdAt(entity.getCreatedAt())
                .build();
    }
    @Override
    public InspectionResponseDTO getLatestPassedReport(Long postId) {
        // Tìm biên bản có trạng thái PASSED của postId, sắp xếp mới nhất
        Inspection inspection = inspectionRepository.findFirstByBikePostIdAndStatusOrderByCreatedAtDesc(postId, InspectionStatus.PASSED)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy biên bản kiểm định hợp lệ cho xe này"));

        // Trả về DTO (Dùng đúng Builder mà chúng ta đã sửa hôm trước)
        return InspectionResponseDTO.builder()
                .id(inspection.getId())
                .postId(inspection.getBikePost() != null ? inspection.getBikePost().getId() : null)
                .postTitle(inspection.getBikePost() != null ? inspection.getBikePost().getTitle() : null)
                .sellerName(inspection.getSeller() != null ? inspection.getSeller().getFullName() : null)
                .sellerPhone(inspection.getSeller() != null ? inspection.getSeller().getPhone() : null)
                .inspectorId(inspection.getInspector() != null ? inspection.getInspector().getId() : null)
                .inspectorName(inspection.getInspector() != null ? inspection.getInspector().getFullName() : null)
                .status(inspection.getStatus() != null ? inspection.getStatus().name() : null)
                .address(inspection.getAddress())
                .scheduledDateTime(inspection.getScheduledDateTime())
                .inspectionFee(inspection.getInspectionFee() != null ? inspection.getInspectionFee().doubleValue() : null)
                .sellerNote(inspection.getNote())
                .resultNote(inspection.getResultNote())
                .checklistData(inspection.getChecklistData())
                .createdAt(inspection.getCreatedAt())
                .build();
    }
    @Override
    public Page<InspectionResponseDTO> getTasksByInspectorId(Long inspectorId, Pageable pageable) {
        return inspectionRepository.findByInspectorId(inspectorId, pageable).map(this::mapToResponse);
    }

}
