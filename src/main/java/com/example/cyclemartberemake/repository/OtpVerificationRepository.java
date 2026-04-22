package com.example.cyclemartberemake.repository;

import com.example.cyclemartberemake.entity.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {
    Optional<OtpVerification> findByEmail(String email);
    Optional<OtpVerification> findByEmailAndOtpCode(String email, String otpCode);
}
