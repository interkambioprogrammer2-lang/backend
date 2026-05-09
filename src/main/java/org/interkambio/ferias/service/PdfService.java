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

    // Datos de la empresa (puedes moverlos a un archivo de configuración si lo deseas)
    private static final String EMPRESA_NOMBRE = "Gusanito Lector E.I.R.L";
    private static final String EMPRESA_RUC = "RUC: 20603275820";
    private static final String EMPRESA_DIRECCION = "Av. Los Quechuas 1372 | Urb. Los Parques de Monterrico - 15022 Lima - Perú";
    private static final String EMPRESA_TELEFONO = "+51 (01) 707 1336";
    private static final String EMPRESA_EMAIL = "ventas@gusanitolector.pe";
    private static final String EMPRESA_WEB = "https://gusanitolector.pe";

    /**
     * Genera el PDF del listado de libros enviados a una feria.
     */
    public byte[] generateSendOutReport(Fair fair) {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            document.open();

            // --- ENCABEZADO CORPORATIVO ---
            addCorporateHeader(document, fair);

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Listado de libros enviados",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
            document.add(new Paragraph(" "));

            // --- TABLA DE LIBROS ---
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);

            // Anchuras relativas
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

    /**
     * Genera el PDF del resumen final de una feria.
     */
    public byte[] generateFinalReport(Fair fair) {
        Document document = new Document(PageSize.A4.rotate());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            document.open();

            // --- ENCABEZADO CORPORATIVO ---
            addCorporateHeader(document, fair);

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Resumen Final de Feria",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
            document.add(new Paragraph(" "));

            // --- TABLA DE RESUMEN ---
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

    // ---- MÉTODOS AUXILIARES ----

    private void addCorporateHeader(Document document, Fair fair) throws Exception {
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 8);

        // --- LOGO (parte superior izquierda) ---
        try {
            InputStream logoStream = getClass().getClassLoader().getResourceAsStream("images/logo.png");
            if (logoStream != null) {
                Image logo = Image.getInstance(logoStream.readAllBytes());
                logo.scaleToFit(150, 90);
                logo.setAlignment(Image.LEFT);
                document.add(logo);
            }
        } catch (Exception e) {
            // Si no hay logo, simplemente continúa
        }

        // --- DATOS DE LA EMPRESA (parte superior derecha) ---
        Paragraph empresa = new Paragraph();
        empresa.setAlignment(Element.ALIGN_RIGHT);
        empresa.add(new Chunk("Gusanito Lector E.I.R.L\n", boldFont));
        empresa.add(new Chunk("RUC: 20603275820\n", normalFont));
        empresa.add(new Chunk("Av. Los Quechuas 1372 | Urb. Los Parques de Monterrico - 15022\n", normalFont));
        empresa.add(new Chunk("Lima - Perú\n", normalFont));
        empresa.add(new Chunk("+51 (01) 707 1336 | ventas@gusanitolector.pe\n", normalFont));
        empresa.add(new Chunk("Website: https://gusanitolector.pe\n", normalFont));
        document.add(empresa);

        // --- LÍNEA SEPARADORA ---
        Paragraph separator = new Paragraph(" ");
        separator.add(new Chunk(new LineSeparator(1, 100, null, Element.ALIGN_CENTER, 0)));
        document.add(separator);

        // --- INFORMACIÓN DEL CLIENTE (facturar a) ---
        Paragraph facturarA = new Paragraph();
        facturarA.add(new Chunk("Facturar a: ", boldFont));
        facturarA.add(new Chunk("Lord Byron School\n", normalFont));  // Puedes hacer esto dinámico con fair.getPlace() o similar
        document.add(facturarA);

        // --- FECHA DEL PEDIDO ---
        Paragraph fechaPedido = new Paragraph();
        fechaPedido.add(new Chunk("Fecha del pedido: ", boldFont));
        fechaPedido.add(new Chunk(fair.getStartDate() != null ? fair.getStartDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "", normalFont));
        document.add(fechaPedido);

        // --- TÍTULO DE LA ORDEN ---
        Paragraph ordenTitulo = new Paragraph("Orden de venta", titleFont);
        ordenTitulo.setAlignment(Element.ALIGN_CENTER);
        document.add(ordenTitulo);

        // --- NÚMERO DE ORDEN ---
        Paragraph numeroOrden = new Paragraph("N° SO-" + String.format("%05d", fair.getId()), boldFont);
        numeroOrden.setAlignment(Element.ALIGN_CENTER);
        document.add(numeroOrden);

        document.add(new Paragraph(" ")); // espacio en blanco
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