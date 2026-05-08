package org.interkambio.ferias.repository;

import org.interkambio.ferias.entity.BookStockLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BookStockLocationRepository extends JpaRepository<BookStockLocation, Long> {

    // Busca un registro por bookId y warehouseId que tenga stock suficiente
    @Query("SELECT b FROM BookStockLocation b WHERE b.bookId = :bookId AND b.warehouseId = :warehouseId AND b.quantity >= :requiredQty ORDER BY b.quantity DESC")
    Optional<BookStockLocation> findFirstByBookIdAndWarehouseIdWithSufficientStock(
            @Param("bookId") Long bookId,
            @Param("warehouseId") Long warehouseId,
            @Param("requiredQty") int requiredQty);
}