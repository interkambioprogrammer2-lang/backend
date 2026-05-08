package org.interkambio.ferias.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.interkambio.ferias.entity.Warehouse;
import org.interkambio.ferias.repository.WarehouseRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/warehouses")
@Tag(name = "Almacenes", description = "Listado de ubicaciones de stock")
public class WarehouseController {
    private final WarehouseRepository warehouseRepository;

    public WarehouseController(WarehouseRepository warehouseRepository) {
        this.warehouseRepository = warehouseRepository;
    }

    @GetMapping
    @Operation(summary = "Listar todos los almacenes")
    public List<Warehouse> getAll() {
        return warehouseRepository.findAll();
    }
}