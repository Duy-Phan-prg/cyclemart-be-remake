package com.example.cyclemartberemake.aspect;

import com.example.cyclemartberemake.entity.UserTracking;
import com.example.cyclemartberemake.entity.Users;
import com.example.cyclemartberemake.repository.UserTrackingRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Map;

@Aspect
@Component
@RequiredArgsConstructor
public class UserTrackingAspect {

    private final UserTrackingRepository userTrackingRepository;

    // Bản đồ ánh xạ tên hàm sang tên chức năng thân thiện
    private static final Map<String, String> FUNCTION_NAMES = Map.ofEntries(
            Map.entry("updateProfile", "Cập nhật thông tin cá nhân"),
            Map.entry("createPost", "Đăng tin bán xe mới"),
            Map.entry("updatePost", "Chỉnh sửa tin đăng"),
            Map.entry("deletePost", "Xóa tin đăng"),
            Map.entry("approvePost", "Duyệt tin đăng (Admin)"),
            Map.entry("rejectPost", "Từ chối tin đăng (Admin)"),
            Map.entry("subscribe", "Mua gói ưu tiên"),
            Map.entry("unsubscribe", "Hủy gói ưu tiên"),
            Map.entry("login", "Đăng nhập hệ thống"),
            Map.entry("banUser", "Khóa tài khoản người dùng"),
            Map.entry("unbanUser", "Mở khóa tài khoản"),
            Map.entry("createNegotiation", "Gửi yêu cầu thương lượng")
    );

    @AfterReturning("execution(* com.example.cyclemartberemake.controller..*(..))")
    public void logUserActivity(JoinPoint joinPoint) {
        try {
            String methodName = joinPoint.getSignature().getName();

            // Bỏ qua đổi mật khẩu và các hàm GET
            if (methodName.equalsIgnoreCase("changePassword")) return;

            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) return;
            HttpServletRequest request = attributes.getRequest();
            if (request.getMethod().equalsIgnoreCase("GET")) return;

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || "anonymousUser".equals(auth.getPrincipal())) return;

            int userId = ((Users) auth.getPrincipal()).getId();

            UserTracking tracking = new UserTracking();
            tracking.setUserId(userId);

            // 🔥 Lấy tên thân thiện từ Map, nếu không có thì mới dùng tên kỹ thuật
            String friendlyName = FUNCTION_NAMES.getOrDefault(methodName, "Thực hiện " + methodName);
            tracking.setEventType(friendlyName);

            tracking.setLocation(request.getRequestURI());
            tracking.setCreatedAt(LocalDateTime.now());

            userTrackingRepository.save(tracking);

        } catch (Exception e) {
            System.err.println("Lỗi ghi log: " + e.getMessage());
        }
    }
}