package org.interkambio.ferias.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "book_stock_locations")
@Data
public class BookStockLocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "book_id")
    private Long bookId;

    // Relación ManyToOne opcional si necesitas acceder al warehouse
    // De momento lo mantenemos como simple columna
    @Column(name = "warehouse_id")
    private Long warehouseId;

    @Column(name = "location_id")
    private Long locationId;

    @Column(name = "quantity")   // usamos 'quantity' que es la columna de stock real
    private int quantity;

    // Podrías agregar otros campos como location_type, book_condition si los necesitas
    // Pero para el módulo actual bastan los de arriba
}