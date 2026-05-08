package org.interkambio.ferias.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.interkambio.ferias.entity.Fair;
import org.interkambio.ferias.entity.FairDispatchItem;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;

@Service
public class PdfService {

    public byte[] generateSendOutReport(Fair fair) {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            document.add(new Paragraph("Reporte de Salida a Feria", titleFont));
            document.add(new Paragraph("Feria: " + fair.getName()));
            document.add(new Paragraph("Lugar: " + fair.getPlace()));
            document.add(new Paragraph("Fecha inicio: " + fair.getStartDate() + " - Fin: " + fair.getEndDate()));
            document.add(new Paragraph("Responsable: " + fair.getResponsible().getName()));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(5);
            table.addCell("SKU");
            table.addCell("Título");
            table.addCell("Cantidad enviada");
            table.addCell("Precio de venta");
            table.addCell("Almacén origen");

            for (FairDispatchItem item : fair.getDispatchItems()) {
                table.addCell(item.getSku());
                table.addCell(item.getTitle());
                table.addCell(String.valueOf(item.getQuantitySent()));
                table.addCell(item.getSalePrice().toString());
                table.addCell(item.getSourceLocation().getName());
            }
            document.add(table);
            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF", e);
        }
        return baos.toByteArray();
    }

    public byte[] generateFinalReport(Fair fair) {
        Document document = new Document(PageSize.A4.rotate());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            document.add(new Paragraph("Resumen Final de Feria", titleFont));
            document.add(new Paragraph("Feria: " + fair.getName()));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(8);
            table.addCell("Título");
            table.addCell("Enviado");
            table.addCell("Retornado");
            table.addCell("Vendido (manual)");
            table.addCell("Vendido (calc)");
            table.addCell("Faltantes");
            table.addCell("Total Venta Est.");
            table.addCell("Nota");

            BigDecimal totalVendido = BigDecimal.ZERO;

            for (FairDispatchItem item : fair.getDispatchItems()) {
                table.addCell(item.getTitle());
                table.addCell(String.valueOf(item.getQuantitySent()));
                table.addCell(item.getQuantityReturned() != null ? item.getQuantityReturned().toString() : "-");
                table.addCell(item.getQuantitySoldManual() != null ? item.getQuantitySoldManual().toString() : "-");
                table.addCell(String.valueOf(item.getVendidosSistema()));
                int falt = item.getFaltantes();
                table.addCell(String.valueOf(falt));
                BigDecimal ventaItem = item.getSalePrice().multiply(BigDecimal.valueOf(item.getVendidosSistema()));
                totalVendido = totalVendido.add(ventaItem);
                table.addCell(ventaItem.toString());
                table.addCell(falt > 0 ? "POSIBLE PÉRDIDA" : "");
            }
            document.add(table);
            document.add(new Paragraph("Total estimado vendido: " + totalVendido));
            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF", e);
        }
        return baos.toByteArray();
    }
}