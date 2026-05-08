package org.interkambio.ferias.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "fair_dispatch_items")
@Data
public class FairDispatchItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fair_id", nullable = false)
    private Fair fair;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    private String sku;
    private String isbn;
    private String title;
    private int quantitySent;
    private BigDecimal salePrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_location_id")
    private Warehouse sourceLocation;

    private Integer quantityReturned;
    private Integer quantitySoldManual;
    private String notes;

    // Nuevos campos de fechas
    @Column(name = "sent_date")
    private LocalDateTime sentDate;

    @Column(name = "returned_date")
    private LocalDateTime returnedDate;

    // Métodos helper para reportes
    public int getVendidosSistema() {
        return quantitySent - (quantityReturned != null ? quantityReturned : 0);
    }

    public int getFaltantes() {
        int vendidos = quantitySoldManual != null ? quantitySoldManual : getVendidosSistema();
        return quantitySent - (quantityReturned != null ? quantityReturned : 0) - vendidos;
    }
}