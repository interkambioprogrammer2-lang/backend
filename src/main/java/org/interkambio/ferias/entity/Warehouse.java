package org.interkambio.ferias.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "warehouses")
@Data
public class Warehouse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    // La columna 'address' no aparece en tu DDL, pero puedes conservarla como opcional
    private String address;
}