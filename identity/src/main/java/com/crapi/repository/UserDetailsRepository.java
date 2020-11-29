package com.crapi.repository;

import com.crapi.entity.UserDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
/**
 * @author Traceabel AI
 */
@Repository
public interface UserDetailsRepository extends JpaRepository<UserDetails,Long> {
    UserDetails findByUser_id(Long id);
}
