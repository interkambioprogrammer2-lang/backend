package org.interkambio.ferias.repository;

import org.interkambio.ferias.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findBySkuContainingOrIsbnContainingOrTitleContaining(
            String sku, String isbn, String title);
}