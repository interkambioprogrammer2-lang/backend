package org.interkambio.ferias.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
public class FairResponse {
    private Long id;
    private String name;
    private String place;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private Responsible responsible;
    private List<FairDetail.DispatchItemDetail> dispatchItems;

    @Data
    @AllArgsConstructor
    public static class Responsible {
        private Long id;
        private String name;
    }
}
