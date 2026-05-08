package org.interkambio.ferias.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DispatchItemRequest {
    private Long bookId;
    private int quantitySent;
    private BigDecimal salePrice;
    private Long sourceLocationId;
}