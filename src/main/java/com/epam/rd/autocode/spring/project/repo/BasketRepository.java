package com.epam.rd.autocode.spring.project.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BasketRepository extends CrudRepository<Basket, Long> {
    Optional<Basket> findBasketByClient_Email(String clientEmail);
}
