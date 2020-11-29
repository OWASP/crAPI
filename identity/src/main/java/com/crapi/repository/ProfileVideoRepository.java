package com.crapi.repository;

import com.crapi.entity.ProfileVideo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Traceabel AI
 */

@Repository
public interface ProfileVideoRepository extends JpaRepository<ProfileVideo,Long> {
    ProfileVideo findByUser_id(Long id);
}
