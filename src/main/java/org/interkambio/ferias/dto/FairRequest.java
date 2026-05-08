package org.interkambio.ferias.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class FairRequest {
    private String name;
    private String place;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long responsibleUserId;
}