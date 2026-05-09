package org.interkambio.ferias.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.draw.LineSeparator;
import org.interkambio.ferias.entity.Fair;
import org.interkambio.ferias.entity.FairDispatchItem;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Service
public class PdfService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Datos de la empresa
    private static final String EMPRESA_NOMBRE = "Gusanito Lector E.I.R.L";
    private static final String EMPRESA_RUC = "RUC: 20603275820";
    private static final String EMPRESA_DIRECCION = "Av. Los Quechuas 1372 | Urb. Los Parques de Monterrico - 15022 Lima - Perú";
    private static final String EMPRESA_TELEFONO = "+51 (01) 707 1336";
    private static final String EMPRESA_EMAIL = "ventas@gusanitolector.pe";
    private static final String EMPRESA_WEB = "Website: https://gusanitolector.pe";

    public byte[] generateSendOutReport(Fair fair) {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            addCorporateHeader(document, fair);

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Listado de libros enviados",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
            document.add(new Paragraph(" "));

            // Tabla de envío
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setWidths(new float[]{2, 6, 2, 2, 2});
            addTableHeader(table, "SKU", "Título", "Cantidad enviada", "Precio venta", "Almacén origen");

            for (FairDispatchItem item : fair.getDispatchItems()) {
                table.addCell(item.getSku());
                table.addCell(item.getTitle());
                table.addCell(String.valueOf(item.getQuantitySent()));
                table.addCell("S/ " + item.getSalePrice().toString());
                table.addCell(item.getSourceLocation().getName());
            }
            document.add(table);
            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF de envío", e);
        }
        return baos.toByteArray();
    }

    public byte[] generateFinalReport(Fair fair) {
        Document document = new Document(PageSize.A4);   // ahora vertical, igual que el de envío
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            addCorporateHeader(document, fair);

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Resumen Final de Feria",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
            document.add(new Paragraph(" "));

            // Tabla de resumen
            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{4, 2, 2, 2, 2, 2, 2, 2});
            addTableHeader(table, "Título", "Enviado", "Retornado", "Vendido (manual)",
                    "Vendido (calc)", "Faltantes", "Total Venta Est.", "Nota");

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
                table.addCell("S/ " + ventaItem.toString());
                table.addCell(falt > 0 ? "POSIBLE PÉRDIDA" : "");
            }
            document.add(table);

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Total estimado vendido: S/ " + totalVendido.toString(),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF final", e);
        }
        return baos.toByteArray();
    }

    // ==================== ENCABEZADO COMÚN ====================
    private void addCorporateHeader(Document document, Fair fair) throws Exception {
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);

        // --- TABLA DE ENCABEZADO (una sola columna) ---
        PdfPTable headerTable = new PdfPTable(1);
        headerTable.setWidthPercentage(100);

        // --- LOGO + SLOGAN (alineado a la izquierda) ---
        PdfPCell logoCell = new PdfPCell();
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        try {
            InputStream logoStream = getClass().getClassLoader().getResourceAsStream("images/logo.png");
            if (logoStream != null) {
                Image logo = Image.getInstance(logoStream.readAllBytes());
                logo.scaleToFit(150, 80);
                logoCell.addElement(logo);
            }
        } catch (Exception e) {
            // si no hay logo, simplemente se omite
        }
        headerTable.addCell(logoCell);

        // --- DATOS DE LA EMPRESA (justo debajo del logo, alineados a la izquierda) ---
        PdfPCell infoCell = new PdfPCell();
        infoCell.setBorder(Rectangle.NO_BORDER);
        infoCell.setHorizontalAlignment(Element.ALIGN_LEFT);   // ← izquierda
        Paragraph empresa = new Paragraph();
        empresa.add(new Chunk(EMPRESA_NOMBRE + "\n", boldFont));
        empresa.add(new Chunk(EMPRESA_RUC + "\n", normalFont));
        empresa.add(new Chunk(EMPRESA_DIRECCION + "\n", normalFont));
        empresa.add(new Chunk(EMPRESA_TELEFONO + " | " + EMPRESA_EMAIL + "\n", normalFont));
        empresa.add(new Chunk(EMPRESA_WEB + "\n", normalFont));
        infoCell.addElement(empresa);
        headerTable.addCell(infoCell);

        document.add(headerTable);

        // --- LÍNEA SEPARADORA ---
        Paragraph line = new Paragraph(" ");
        line.add(new Chunk(new LineSeparator(1, 100, null, Element.ALIGN_CENTER, 0)));
        document.add(line);

        // --- FECHA DEL PEDIDO ---
        Paragraph fechaPedido = new Paragraph();
        fechaPedido.add(new Chunk("Fecha del pedido: ", boldFont));
        fechaPedido.add(new Chunk(fair.getStartDate() != null ? fair.getStartDate().format(DATE_FMT) : "", normalFont));
        document.add(fechaPedido);

        document.add(new Paragraph(" "));

        // --- TÍTULO ---
        Paragraph ordenTitulo = new Paragraph("Orden de envío a feria", titleFont);
        ordenTitulo.setAlignment(Element.ALIGN_CENTER);
        document.add(ordenTitulo);

        Paragraph numeroFeria = new Paragraph("N° Feria: " + fair.getId(), boldFont);
        numeroFeria.setAlignment(Element.ALIGN_CENTER);
        document.add(numeroFeria);

        document.add(new Paragraph(" "));
    }

    private void addTableHeader(PdfPTable table, String... headers) {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(new java.awt.Color(230, 230, 230));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
    }
}