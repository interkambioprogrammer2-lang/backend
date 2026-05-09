package org.interkambio.ferias.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_transactions")
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class InventoryTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_type")
    private String transactionType; // FAIR_OUT, FAIR_RETURN

    @Column(name = "book_id")
    private Long bookId;

    @Column(name = "location_id")
    private Long locationId;

    // ¡Corregido! La columna real se llama 'quantity'
    @Column(name = "quantity")
    private int quantityChange; // internamente seguimos llamándolo quantityChange

    @Column(name = "reference_id")
    private Long referenceId; // id del FairDispatchItem

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}