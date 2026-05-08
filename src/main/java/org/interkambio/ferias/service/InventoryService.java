package org.interkambio.ferias.service;

import lombok.RequiredArgsConstructor;
import org.interkambio.ferias.entity.BookStockLocation;
import org.interkambio.ferias.entity.InventoryTransaction;
import org.interkambio.ferias.repository.BookStockLocationRepository;
import org.interkambio.ferias.repository.InventoryTransactionRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final BookStockLocationRepository stockRepo;
    private final InventoryTransactionRepository txRepo;

    public void discountStock(Long bookId, Long warehouseId, int quantity, String txType, Long referenceId) {
        BookStockLocation stock = stockRepo
                .findFirstByBookIdAndWarehouseIdWithSufficientStock(bookId, warehouseId, quantity)
                .orElseThrow(() -> new RuntimeException("No hay suficiente stock del libro en ese almacén"));
        stock.setQuantity(stock.getQuantity() - quantity);
        stockRepo.save(stock);

        InventoryTransaction tx = new InventoryTransaction();
        tx.setTransactionType(txType);
        tx.setBookId(bookId);
        tx.setLocationId(warehouseId); // almacenamos el warehouseId como referencia
        tx.setQuantityChange(-quantity);
        tx.setReferenceId(referenceId);
        tx.setCreatedAt(LocalDateTime.now());
        txRepo.save(tx);
    }

    public void addStock(Long bookId, Long warehouseId, int quantity, String txType, Long referenceId) {
        // Para el retorno buscamos el mismo registro que tenía stock (podría ser el que se usó originalmente)
        // Usamos la misma consulta pero sin requerir stock mínimo
        BookStockLocation stock = stockRepo
                .findFirstByBookIdAndWarehouseIdWithSufficientStock(bookId, warehouseId, 0)
                .orElseThrow(() -> new RuntimeException("No se encontró registro de stock para reposición"));
        stock.setQuantity(stock.getQuantity() + quantity);
        stockRepo.save(stock);

        InventoryTransaction tx = new InventoryTransaction();
        tx.setTransactionType(txType);
        tx.setBookId(bookId);
        tx.setLocationId(warehouseId);
        tx.setQuantityChange(quantity);
        tx.setReferenceId(referenceId);
        tx.setCreatedAt(LocalDateTime.now());
        txRepo.save(tx);
    }
}