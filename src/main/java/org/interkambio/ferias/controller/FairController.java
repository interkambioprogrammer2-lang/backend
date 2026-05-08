package org.interkambio.ferias.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.interkambio.ferias.dto.*;
import org.interkambio.ferias.entity.Fair;
import org.interkambio.ferias.service.FairService;
import org.interkambio.ferias.service.PdfService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/fairs")
@Tag(name = "Ferias", description = "Gestión de ferias y control de envíos")
public class FairController {
    private final FairService fairService;
    private final PdfService pdfService;

    public FairController(FairService fairService, PdfService pdfService) {
        this.fairService = fairService;
        this.pdfService = pdfService;
    }

    @GetMapping
    @Operation(summary = "Listar todas las ferias (resumen)")
    public ResponseEntity<List<FairSummary>> getAll() {
        List<Fair> fairs = fairService.getAllFairs();
        List<FairSummary> summaries = fairs.stream()
                .map(f -> new FairSummary(
                        f.getId(),
                        f.getName(),
                        f.getPlace(),
                        f.getStartDate(),
                        f.getEndDate(),
                        f.getResponsible() != null ? f.getResponsible().getName() : "",
                        f.getStatus().name()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(summaries);
    }

    @PostMapping
    @Operation(summary = "Crear nueva feria")
    public ResponseEntity<Fair> create(@RequestBody FairRequest request) {
        return ResponseEntity.ok(fairService.createFair(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle de feria")
    public ResponseEntity<FairDetail> getById(@PathVariable Long id) {
        return ResponseEntity.ok(fairService.getFairDetailById(id));
    }

    @PostMapping("/{id}/dispatch-items")
    @Operation(summary = "Agregar libros al envío")
    public ResponseEntity<Fair> addItems(@PathVariable Long id,
                                         @RequestBody List<DispatchItemRequest> items) {
        return ResponseEntity.ok(fairService.addDispatchItems(id, items));
    }

    @PutMapping("/{id}/confirm-dispatch")
    @Operation(summary = "Confirmar envío y descontar inventario")
    public ResponseEntity<Fair> confirmDispatch(@PathVariable Long id) {
        return ResponseEntity.ok(fairService.confirmDispatch(id));
    }

    @PutMapping("/{id}/record-return")
    @Operation(summary = "Registrar retorno de libros")
    public ResponseEntity<Fair> recordReturn(@PathVariable Long id,
                                             @RequestBody List<ReturnRequest> returns) {
        return ResponseEntity.ok(fairService.recordReturn(id, returns));
    }

    @GetMapping("/{id}/report/sendout")
    @Operation(summary = "Generar PDF del listado de envío")
    public ResponseEntity<byte[]> getSendOutReport(@PathVariable Long id) {
        Fair fair = fairService.getFairById(id);   // <-- entidad Fair
        byte[] pdf = pdfService.generateSendOutReport(fair);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("salida_feria_" + id + ".pdf").build());
        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }

    @GetMapping("/{id}/report/final")
    @Operation(summary = "Generar PDF del resumen final")
    public ResponseEntity<byte[]> getFinalReport(@PathVariable Long id) {
        Fair fair = fairService.getFairById(id);   // <-- entidad Fair
        byte[] pdf = pdfService.generateFinalReport(fair);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("resumen_feria_" + id + ".pdf").build());
        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }
}