package com.epam.rd.autocode.spring.project.repo;

import com.epam.rd.autocode.spring.project.model.Book;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long>, QuerydslPredicateExecutor<Book> {
    Optional<Book> findByName(String name);

    @Transactional
    @Modifying
    void deleteByName(String name);
}
