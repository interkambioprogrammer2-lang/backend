package org.interkambio.ferias.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_transactions")
@Data
public class InventoryTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String transactionType; // FAIR_OUT, FAIR_RETURN
    private Long bookId;
    private Long locationId;
    private int quantityChange;
    private Long referenceId; // id del FairDispatchItem
    private LocalDateTime createdAt;
}