package com.example.cyclemartberemake.service.impl;

import com.example.cyclemartberemake.dto.request.InspectionRequestDTO;
import com.example.cyclemartberemake.dto.response.InspectionResponseDTO;
import com.example.cyclemartberemake.entity.*;
import com.example.cyclemartberemake.repository.BikePostRepository;
import com.example.cyclemartberemake.repository.InspectionRepository;
import com.example.cyclemartberemake.repository.UserRepository;
import com.example.cyclemartberemake.service.InspectionService;
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

    @Override
    @Transactional
    public InspectionResponseDTO createRequest(InspectionRequestDTO request) {
        Users currentUser = getCurrentUser();

        BikePost post = bikePostRepository.findById(request.getPostId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài đăng"));

        // Chỉ chủ xe mới được yêu cầu kiểm định
        if (!post.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Bạn không có quyền yêu cầu kiểm định cho xe này");
        }

        // Kiểm tra xem xe này có đang chờ kiểm định không
        boolean isPending = inspectionRepository.existsByBikePostIdAndStatusIn(
                post.getId(),
                Arrays.asList(InspectionStatus.PENDING, InspectionStatus.ASSIGNED, InspectionStatus.INSPECTING)
        );
        if (isPending) {
            throw new RuntimeException("Xe này đang trong quá trình xử lý kiểm định rồi!");
        }

        Inspection inspection = Inspection.builder()
                .bikePost(post)
                .seller(currentUser)
                .address(request.getAddress())
                .scheduledDateTime(request.getScheduledDateTime())
                .note(request.getNote())
                .build();

        Inspection saved = inspectionRepository.save(inspection);
        return mapToResponse(saved);
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
        // 1. Tìm yêu cầu kiểm định
        Inspection inspection = inspectionRepository.findById(inspectionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu"));

        // 2. Tìm Inspector
        Users inspector = userRepository.findById(inspectorId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Inspector"));

        if (inspector.getRole() != Role.INSPECTOR) {
            throw new RuntimeException("Người dùng này không có quyền Inspector");
        }

        // 3. LOGIC CHẶN TRÙNG LỊCH 2 TIẾNG
        LocalDateTime proposedTime = inspection.getScheduledDateTime();

        // Lấy tất cả lịch sắp tới hoặc đang làm của Inspector này
        List<Inspection> activeTasks = inspectionRepository.findByInspectorIdAndStatusIn(
                inspectorId,
                Arrays.asList(InspectionStatus.ASSIGNED, InspectionStatus.INSPECTING, InspectionStatus.PASSED)
        );

        for (Inspection task : activeTasks) {
            if (task.getId().equals(inspectionId)) continue;

            // Tính khoảng cách phút giữa 2 lịch
            long minutesDiff = Math.abs(java.time.Duration.between(proposedTime, task.getScheduledDateTime()).toMinutes());

            if (minutesDiff < 120) { // 120 phút = 2 tiếng
                String timeStr = task.getScheduledDateTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm dd/MM"));
                throw new RuntimeException("Inspector này đã có lịch lúc " + timeStr + ". Vui lòng chọn người khác hoặc đổi khung giờ (cách ít nhất 2 tiếng).");
            }
        }

        // 4. Gán và cập nhật trạng thái
        inspection.setInspector(inspector);
        inspection.setStatus(InspectionStatus.ASSIGNED);
        inspectionRepository.save(inspection);
    }

    @Override
    @Transactional
    public void updateResult(Long inspectionId, String statusStr, String resultNote) {
        // 1. Tìm yêu cầu
        Inspection inspection = inspectionRepository.findById(inspectionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu"));

        // 2. Chuyển đổi trạng thái
        InspectionStatus newStatus = InspectionStatus.valueOf(statusStr.toUpperCase());
        inspection.setStatus(newStatus);
        inspection.setResultNote(resultNote);

        // 3. LOGIC ĐỒNG BỘ: Nếu trạng thái là PASSED, cập nhật ngay cho bài đăng
        if (newStatus == InspectionStatus.PASSED) {
            BikePost post = inspection.getBikePost();

            // CẬP NHẬT CỜ VERIFIED - Frontend sẽ dựa vào đây để hiện tem
            post.setIsVerified(true);

            // LƯU Ý: Nếu Entity BikePost của bạn không có phương thức setVerified,
            // hãy kiểm tra lại xem tên field có phải là setVerified(true) không nhé.
            bikePostRepository.save(post);
        }

        // 4. Nếu Inspector chấm FAILED, bạn có thể cân nhắc setVerified(false)
        // để gỡ tem của xe bị rớt kiểm định (tùy nghiệp vụ bạn muốn)
        if (newStatus == InspectionStatus.FAILED) {
            BikePost post = inspection.getBikePost();
            post.setIsVerified(false);
            bikePostRepository.save(post);
        }

        inspectionRepository.save(inspection);
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
                .postId(entity.getBikePost().getId())
                .postTitle(entity.getBikePost().getTitle())
                .sellerName(entity.getSeller().getFullName())
                .sellerPhone(entity.getSeller().getPhone())
                .inspectorName(entity.getInspector() != null ? entity.getInspector().getFullName() : null)
                .status(entity.getStatus().name())
                .address(entity.getAddress())
                .scheduledDateTime(entity.getScheduledDateTime())
                .inspectionFee(entity.getInspectionFee())
                .note(entity.getNote())
                .resultNote(entity.getResultNote())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}