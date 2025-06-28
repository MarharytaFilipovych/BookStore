package com.epam.rd.autocode.spring.project.repo;

import com.epam.rd.autocode.spring.project.model.Client;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    Optional<Client> getByEmail(String email);

    @Transactional
    @Modifying
    void deleteByEmail(String email);
}
