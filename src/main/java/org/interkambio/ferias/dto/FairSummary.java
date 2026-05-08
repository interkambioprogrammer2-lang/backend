package org.interkambio.ferias.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class FairSummary {
    private Long id;
    private String name;
    private String place;
    private LocalDate startDate;
    private LocalDate endDate;
    private String responsibleName; // solo el nombre del responsable
    private String status;
}