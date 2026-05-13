package org.interkambio.ferias.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.interkambio.ferias.entity.Book;
import org.interkambio.ferias.entity.BookStockLocation;
import org.interkambio.ferias.repository.BookRepository;
import org.interkambio.ferias.repository.BookStockLocationRepository; // Asegúrate de importar este repositorio
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/books")
@Tag(name = "Libros", description = "Búsqueda de libros y consulta de stock")
public class BookController {
    private final BookRepository bookRepository;
    private final BookStockLocationRepository stockRepo; // Inyectamos el repositorio

    public BookController(BookRepository bookRepository, BookStockLocationRepository stockRepo) {
        this.bookRepository = bookRepository;
        this.stockRepo = stockRepo;
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar libros por término (SKU, ISBN o título)")
    public List<Book> search(@RequestParam String term) {
        return bookRepository.findBySkuContainingOrIsbnContainingOrTitleContaining(term, term, term);
    }

    @GetMapping("/{bookId}/stock")
    @Operation(summary = "Consultar stock disponible de un libro en un almacén")
    public ResponseEntity<?> getStock(@PathVariable Long bookId, @RequestParam Long warehouseId) {
        BookStockLocation stock = stockRepo
                .findTopByBookIdAndWarehouseIdOrderByQuantityDesc(bookId, warehouseId)
                .orElseThrow(() -> new RuntimeException("No se encontró stock para este libro en el almacén seleccionado"));
        return ResponseEntity.ok(Map.of("available", stock.getQuantity()));
    }
}