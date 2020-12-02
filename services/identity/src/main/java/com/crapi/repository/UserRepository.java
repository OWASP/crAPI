package com.crapi.repository;

import com.crapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Traceabel AI
 */
@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    boolean existsByNumber(String number);
    boolean existsByEmail(String email);
    User findByEmail(String s);
}
