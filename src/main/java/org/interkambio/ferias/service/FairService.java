package org.interkambio.ferias.service;

import lombok.RequiredArgsConstructor;
import org.interkambio.ferias.dto.*;
import org.interkambio.ferias.entity.*;
import org.interkambio.ferias.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class FairService {
    private final FairRepository fairRepository;
    private final FairDispatchItemRepository dispatchItemRepository;
    private final BookRepository bookRepository;
    private final WarehouseRepository warehouseRepository;
    private final UserRepository userRepository;
    private final InventoryService inventoryService;

    // Método para listar todas las ferias (resumen)
    @Transactional(readOnly = true)
    public List<Fair> getAllFairs() {
        return fairRepository.findAll();
    }

    // Método que devuelve la entidad Fair (usado por reportes PDF, etc.)
    @Transactional(readOnly = true)
    public Fair getFairById(Long id) {
        return fairRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Feria no encontrada"));
    }

    // Método que devuelve un DTO con el detalle para el frontend
    @Transactional(readOnly = true)
    public FairDetail getFairDetailById(Long id) {
        Fair fair = fairRepository.findByIdWithDispatchItems(id)
                .orElseThrow(() -> new RuntimeException("Feria no encontrada"));

        List<FairDetail.DispatchItemDetail> items = fair.getDispatchItems().stream()
                .map(item -> new FairDetail.DispatchItemDetail(
                        item.getId(),
                        item.getBook().getId(),
                        item.getSku(),
                        item.getIsbn(),
                        item.getTitle(),
                        item.getQuantitySent(),
                        item.getSalePrice().toString(),
                        item.getQuantityReturned(),
                        item.getQuantitySoldManual(),
                        item.getSentDate(),
                        item.getReturnedDate()
                ))
                .collect(Collectors.toList());

        return new FairDetail(
                fair.getId(),
                fair.getName(),
                fair.getPlace(),
                fair.getStartDate(),
                fair.getEndDate(),
                fair.getResponsible() != null ? fair.getResponsible().getName() : "",
                fair.getStatus().name(),
                items
        );
    }

    public Fair createFair(FairRequest request) {
        User responsible = userRepository.findById(request.getResponsibleUserId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Fair fair = new Fair();
        fair.setName(request.getName());
        fair.setPlace(request.getPlace());
        fair.setStartDate(request.getStartDate());
        fair.setEndDate(request.getEndDate());
        fair.setResponsible(responsible);
        fair.setStatus(FairStatus.DRAFT);
        return fairRepository.save(fair);
    }

    public Fair addDispatchItems(Long fairId, List<DispatchItemRequest> items) {
        Fair fair = getFairById(fairId);
        if (fair.getStatus() != FairStatus.DRAFT) {
            throw new RuntimeException("Solo se pueden agregar libros en estado DRAFT");
        }
        for (DispatchItemRequest req : items) {
            Book book = bookRepository.findById(req.getBookId())
                    .orElseThrow(() -> new RuntimeException("Libro no encontrado"));
            Warehouse location = warehouseRepository.findById(req.getSourceLocationId())
                    .orElseThrow(() -> new RuntimeException("Ubicación no encontrada"));

            FairDispatchItem item = new FairDispatchItem();
            item.setFair(fair);
            item.setBook(book);
            item.setSku(book.getSku());
            item.setIsbn(book.getIsbn());
            item.setTitle(book.getTitle());
            item.setQuantitySent(req.getQuantitySent());
            item.setSalePrice(req.getSalePrice());
            item.setSourceLocation(location);
            fair.getDispatchItems().add(item);
        }
        return fairRepository.save(fair);
    }

    public Fair confirmDispatch(Long fairId) {
        Fair fair = getFairById(fairId);
        if (fair.getStatus() != FairStatus.DRAFT) {
            throw new RuntimeException("La feria ya no está en borrador");
        }
        LocalDateTime now = LocalDateTime.now();
        for (FairDispatchItem item : fair.getDispatchItems()) {
            inventoryService.discountStock(item.getBook().getId(), item.getSourceLocation().getId(),
                    item.getQuantitySent(), "FAIR_OUT", item.getId());
            item.setSentDate(now);
        }
        fair.setStatus(FairStatus.DISPATCHED);
        return fairRepository.save(fair);
    }

    public Fair recordReturn(Long fairId, List<ReturnRequest> returns) {
        Fair fair = getFairById(fairId);
        if (fair.getStatus() != FairStatus.DISPATCHED) {
            throw new RuntimeException("Debe estar en estado DISPATCHED para registrar retornos");
        }
        LocalDateTime now = LocalDateTime.now();
        for (ReturnRequest ret : returns) {
            FairDispatchItem item = dispatchItemRepository.findById(ret.getDispatchItemId())
                    .orElseThrow(() -> new RuntimeException("Ítem no encontrado"));
            if (!item.getFair().getId().equals(fairId)) {
                throw new RuntimeException("El ítem no pertenece a esta feria");
            }
            int maxReturn = item.getQuantitySent() - (ret.getQuantitySoldManual() != null ? ret.getQuantitySoldManual() : 0);
            if (ret.getQuantityReturned() < 0 || ret.getQuantityReturned() > maxReturn) {
                throw new RuntimeException("Cantidad retornada inválida para " + item.getTitle());
            }
            item.setQuantityReturned(ret.getQuantityReturned());
            item.setQuantitySoldManual(ret.getQuantitySoldManual());
            item.setReturnedDate(now);

            if (ret.getQuantityReturned() > 0) {
                inventoryService.addStock(item.getBook().getId(), item.getSourceLocation().getId(),
                        ret.getQuantityReturned(), "FAIR_RETURN", item.getId());
            }
            dispatchItemRepository.save(item);
        }
        fair.setStatus(FairStatus.CLOSED);
        return fairRepository.save(fair);
    }
}