package org.interkambio.ferias.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class FairDetail {
    private Long id;
    private String name;
    private String place;
    private LocalDate startDate;
    private LocalDate endDate;
    private String responsibleName;
    private Long responsibleUserId;   // ← NUEVO CAMPO
    private String status;
    private List<DispatchItemDetail> dispatchItems;

    @Data
    @AllArgsConstructor
    public static class DispatchItemDetail {
        private Long id;
        private Long bookId;
        private String sku;
        private String isbn;
        private String title;
        private int quantitySent;
        private String salePrice;
        private Integer quantityReturned;
        private Integer quantitySoldManual;
        private LocalDateTime sentDate;
        private LocalDateTime returnedDate;
    }
}
