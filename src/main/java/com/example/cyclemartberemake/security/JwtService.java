package com.example.cyclemartberemake.security;

import com.example.cyclemartberemake.entity.Users; // Bổ sung import này
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    private final String SECRET = "my-super-secret-key-which-is-very-long-123456";

    private SecretKey getSignKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    // Nhận vào entity Users thay vì chỉ chuỗi email
    public String generateToken(Users user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                // THÊM CÁC THÔNG TIN NÀY VÀO TOKEN (Claims)
                .claim("id", user.getId())
                .claim("fullName", user.getFullName())
                .claim("phone", user.getPhone())
                .claim("role", user.getRole().toString())
                .claim("status", user.getStatus().toString())
                .claim("statusDisplay", getStatusDisplay(user.getStatus().toString()))
                .claim("point", user.getPoint())
                // ----------------------------------------
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(getSignKey())
                .compact();
    }

    public String extractEmail(String token) {
        return getClaims(token).getSubject();
    }

    public boolean isValid(String token) {
        try {
            return !getClaims(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private String getStatusDisplay(String status) {
        if (status == null) return "";
        if (status.equals("ACTIVE")) return "Hoạt động";
        if (status.equals("INACTIVE")) return "Chưa xác thực";
        if (status.equals("BANNED")) return "Đã cấm";
        if (status.equals("SUSPENDED")) return "Tạm khóa";
        return "";
    }
}