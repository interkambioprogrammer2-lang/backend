package org.interkambio.ferias.repository;

import org.interkambio.ferias.entity.BookStockLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BookStockLocationRepository extends JpaRepository<BookStockLocation, Long> {

    // Devuelve el registro con mayor stock disponible (si tiene stock suficiente)
    Optional<BookStockLocation> findTopByBookIdAndWarehouseIdAndQuantityGreaterThanEqualOrderByQuantityDesc(
            Long bookId, Long warehouseId, int quantity);

    // Devuelve cualquier registro (el de mayor stock) para ese libro y almacén
    Optional<BookStockLocation> findTopByBookIdAndWarehouseIdOrderByQuantityDesc(
            Long bookId, Long warehouseId);
}