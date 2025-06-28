package com.epam.rd.autocode.spring.project.repo;

import com.epam.rd.autocode.spring.project.model.BlockedClient;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

@Repository
public interface BlockedClientRepository extends JpaRepository<BlockedClient, Long> {
    @Modifying
    @Transactional
    void deleteByClient_Email(String clientEmail);

    boolean existsByClient_Email(String clientEmail);
}
