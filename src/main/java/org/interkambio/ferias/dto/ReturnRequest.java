package org.interkambio.ferias.dto;

import lombok.Data;

@Data
public class ReturnRequest {
    private Long dispatchItemId;
    private int quantityReturned;
    private Integer quantitySoldManual; // opcional
}