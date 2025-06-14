package com.epam.rd.autocode.spring.project.repo;

import com.epam.rd.autocode.spring.project.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    boolean existsByClient_Email(String clientEmail);

    List<Order> findAllByEmployee_Email(String employeeEmail);

    List<Order> findAllByClient_Email(String clientEmail);

    Page<Order> findAllByClientNotNullAndEmployeeNotNull(Pageable pageable);
}
