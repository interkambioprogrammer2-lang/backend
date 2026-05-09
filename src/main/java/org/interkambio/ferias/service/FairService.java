package org.interkambio.ferias.service;

import lombok.RequiredArgsConstructor;
import org.interkambio.ferias.dto.*;
import org.interkambio.ferias.entity.*;
import org.interkambio.ferias.exception.NotFoundException;
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

    @Transactional(readOnly = true)
    public List<Fair> getAllFairs() {
        return fairRepository.findAllByOrderByCreatedAtDesc();  // ← antes era findAllByOrderByStartDateDesc()
    }

    @Transactional(readOnly = true)
    public Fair getFairById(Long id) {
        return fairRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Feria no encontrada"));
    }

    @Transactional(readOnly = true)
    public FairDetail getFairDetailById(Long id) {
        Fair fair = fairRepository.findByIdWithDispatchItems(id)
                .orElseThrow(() -> new NotFoundException("Feria no encontrada"));
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
                fair.getResponsible() != null ? fair.getResponsible().getId() : null,   // ← responsableUserId
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
        fair.setStatus(FairStatus.OPEN);
        return fairRepository.save(fair);
    }

    public Fair updateFair(Long id, FairRequest request) {
        Fair fair = fairRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Feria no encontrada"));

        if (request.getName() == null || request.getName().isBlank()) {
            throw new RuntimeException("El nombre es obligatorio");
        }
        if (request.getStartDate() == null) {
            throw new RuntimeException("La fecha de inicio es obligatoria");
        }
        if (request.getEndDate() == null) {
            throw new RuntimeException("La fecha de fin es obligatoria");
        }
        if (request.getResponsibleUserId() == null) {
            throw new RuntimeException("El responsable es obligatorio");
        }

        User responsible = userRepository.findById(request.getResponsibleUserId())
                .orElseThrow(() -> new NotFoundException("Usuario responsable no encontrado"));

        fair.setName(request.getName());
        fair.setPlace(request.getPlace());
        fair.setStartDate(request.getStartDate());
        fair.setEndDate(request.getEndDate());
        fair.setResponsible(responsible);

        fair = fairRepository.save(fair);
        // Refrescar la entidad para asegurar que el responsable está completamente cargado
        return fairRepository.findById(fair.getId())
                .orElseThrow(() -> new NotFoundException("Feria recién actualizada no encontrada"));
    }

    public Fair addDispatchItems(Long fairId, List<DispatchItemRequest> items) {
        Fair fair = getFairById(fairId);
        if (fair.getStatus() != FairStatus.OPEN) {
            throw new RuntimeException("Solo se pueden agregar libros en estado OPEN");
        }
        for (DispatchItemRequest req : items) {
            Book book = bookRepository.findById(req.getBookId())
                    .orElseThrow(() -> new NotFoundException("Libro no encontrado"));
            Warehouse location = warehouseRepository.findById(req.getSourceLocationId())
                    .orElseThrow(() -> new NotFoundException("Ubicación no encontrada"));

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
        if (fair.getStatus() != FairStatus.OPEN) {
            throw new RuntimeException("La feria ya no está abierta");
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
                    .orElseThrow(() -> new NotFoundException("Ítem no encontrado"));
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

    public void deleteFair(Long id) {
        Fair fair = getFairById(id);
        // Puedes agregar validaciones, por ejemplo, no permitir eliminar si está DISPATCHED/CLOSED
        fairRepository.delete(fair);
    }

    public void removeDispatchItem(Long fairId, Long itemId) {
        Fair fair = getFairById(fairId);
        // Solo permitimos eliminar si la feria está en estado OPEN (antes de confirmar)
        if (fair.getStatus() != FairStatus.OPEN) {
            throw new RuntimeException("Solo se pueden eliminar libros en estado OPEN");
        }
        FairDispatchItem item = dispatchItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Ítem no encontrado"));
        if (!item.getFair().getId().equals(fairId)) {
            throw new RuntimeException("El ítem no pertenece a esta feria");
        }
        fair.getDispatchItems().remove(item);
        dispatchItemRepository.delete(item);
    }
}
