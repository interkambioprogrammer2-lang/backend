package org.interkambio.ferias.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.interkambio.ferias.entity.Book;
import org.interkambio.ferias.repository.BookRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/books")
@Tag(name = "Libros", description = "Búsqueda de libros del catálogo")
public class BookController {
    private final BookRepository bookRepository;

    public BookController(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar libros por término (SKU, ISBN o título)")
    public List<Book> search(@RequestParam String term) {
        return bookRepository.findBySkuContainingOrIsbnContainingOrTitleContaining(term, term, term);
    }
}