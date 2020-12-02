package com.crapi.repository;

import com.crapi.entity.ChangeEmailRequest;
import com.crapi.entity.User;
import com.crapi.model.ChangeEmailForm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Traceabel AI
 */

@Repository
public interface ChangeEmailRepository extends JpaRepository<ChangeEmailRequest, Long> {
    ChangeEmailRequest findByUser(User user);
    ChangeEmailRequest findByEmailToken(String emailToken);


}
