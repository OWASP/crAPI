package com.crapi.repository;

import com.crapi.entity.Otp;
import com.crapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
/**
 * @author Traceabel AI
 */
@Repository
public interface OtpRepository extends JpaRepository<Otp,Long> {
    Otp findByOtpAndStatus(String otp, String status);

    Otp findByUser(User user);
}
