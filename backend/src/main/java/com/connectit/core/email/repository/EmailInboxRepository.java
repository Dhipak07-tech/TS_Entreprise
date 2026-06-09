package com.connectit.core.email.repository;

import com.connectit.core.email.entity.EmailInbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmailInboxRepository extends JpaRepository<EmailInbox, Long> {
    List<EmailInbox> findByIsActive(boolean isActive);
}
