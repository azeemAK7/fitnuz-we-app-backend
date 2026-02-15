package com.fitnuz.project.Util;

import com.fitnuz.project.Payload.DTO.OrderItemDto;
import com.fitnuz.project.Payload.Response.OrderDto;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Component
public class PdfGenerator {

    private static final DeviceRgb BRAND_DARK = new DeviceRgb(30, 41, 59);       // slate-800
    private static final DeviceRgb BRAND_MID = new DeviceRgb(71, 85, 105);        // slate-600
    private static final DeviceRgb LIGHT_BG = new DeviceRgb(248, 250, 252);       // slate-50
    private static final DeviceRgb BORDER_COLOR = new DeviceRgb(226, 232, 240);   // slate-200
    private static final DeviceRgb ACCENT = new DeviceRgb(37, 99, 235);           // blue-600
    private static final DeviceRgb SUCCESS_GREEN = new DeviceRgb(22, 163, 74);    // green-600
    private static final DeviceRgb PENDING_RED = new DeviceRgb(220, 38, 38);      // red-600

    public byte[] generateOrderReport(OrderDto orderResponse) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdf = new PdfDocument(writer);
        pdf.setDefaultPageSize(PageSize.A4);
        Document document = new Document(pdf, PageSize.A4, false);
        document.setMargins(36, 40, 36, 40);

        addHeader(document, orderResponse);
        addDivider(document);
        addOrderMeta(document, orderResponse);
        addItemsTable(document, orderResponse);
        addTotalsSection(document, orderResponse);
        addDivider(document);
        addFooter(document);

        document.close();
        return out.toByteArray();
    }

    private void addHeader(Document document, OrderDto order) {
        Table header = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .useAllAvailableWidth()
                .setBorder(Border.NO_BORDER);

        // Left: brand
        Cell brandCell = new Cell().setBorder(Border.NO_BORDER);
        brandCell.add(new Paragraph("FitNuz")
                .setFontSize(28)
                .setBold()
                .setFontColor(BRAND_DARK));
        brandCell.add(new Paragraph("Premium Dry Fruits & Nuts")
                .setFontSize(9)
                .setFontColor(BRAND_MID)
                .setMarginTop(-4));
        header.addCell(brandCell);

        // Right: invoice label
        Cell invoiceCell = new Cell().setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.RIGHT);
        invoiceCell.add(new Paragraph("INVOICE")
                .setFontSize(22)
                .setBold()
                .setFontColor(ACCENT));
        invoiceCell.add(new Paragraph("#" + order.getOrderId())
                .setFontSize(11)
                .setFontColor(BRAND_MID)
                .setMarginTop(-2));
        String dateStr = order.getOrderDate() != null
                ? order.getOrderDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                : "N/A";
        invoiceCell.add(new Paragraph(dateStr)
                .setFontSize(10)
                .setFontColor(BRAND_MID));
        header.addCell(invoiceCell);

        document.add(header);
    }

    private void addDivider(Document document) {
        document.add(new Paragraph("")
                .setBorderBottom(new SolidBorder(BORDER_COLOR, 1))
                .setMarginTop(10)
                .setMarginBottom(10));
    }

    private void addOrderMeta(Document document, OrderDto order) {
        Table metaTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .useAllAvailableWidth()
                .setBorder(Border.NO_BORDER)
                .setMarginBottom(16);

        // Left column: Bill To
        Cell billTo = new Cell().setBorder(Border.NO_BORDER).setPaddingRight(20);
        billTo.add(new Paragraph("BILL TO")
                .setFontSize(8)
                .setBold()
                .setFontColor(ACCENT)
                .setMarginBottom(4));
        billTo.add(new Paragraph(order.getEmail())
                .setFontSize(10)
                .setFontColor(BRAND_DARK));
        if (order.getAddress() != null && !order.getAddress().isEmpty()) {
            billTo.add(new Paragraph(order.getAddress())
                    .setFontSize(9)
                    .setFontColor(BRAND_MID)
                    .setMarginTop(2));
        }
        metaTable.addCell(billTo);

        // Right column: Payment info
        Cell paymentInfo = new Cell().setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.RIGHT);
        paymentInfo.add(new Paragraph("PAYMENT DETAILS")
                .setFontSize(8)
                .setBold()
                .setFontColor(ACCENT)
                .setMarginBottom(4));

        String paymentGateway = order.getPayment() != null && order.getPayment().getPgName() != null
                ? order.getPayment().getPgName() : "N/A";
        paymentInfo.add(new Paragraph("Method: " + paymentGateway)
                .setFontSize(10)
                .setFontColor(BRAND_DARK));

        String pgStatus = order.getPayment() != null && order.getPayment().getPgStatus() != null
                ? order.getPayment().getPgStatus() : "N/A";
        DeviceRgb statusColor = "Pending".equalsIgnoreCase(pgStatus) ? PENDING_RED : SUCCESS_GREEN;
        paymentInfo.add(new Paragraph("Status: " + pgStatus)
                .setFontSize(10)
                .setFontColor(statusColor)
                .setBold());

        String orderStatus = order.getOrderStatus() != null ? order.getOrderStatus() : "N/A";
        paymentInfo.add(new Paragraph("Order: " + orderStatus)
                .setFontSize(9)
                .setFontColor(BRAND_MID)
                .setMarginTop(2));
        metaTable.addCell(paymentInfo);

        document.add(metaTable);
    }

    private void addItemsTable(Document document, OrderDto order) {
        document.add(new Paragraph("ORDER ITEMS")
                .setFontSize(8)
                .setBold()
                .setFontColor(ACCENT)
                .setMarginBottom(8));

        Table table = new Table(UnitValue.createPercentArray(new float[]{4, 2, 1.5f, 1.5f, 2}))
                .useAllAvailableWidth()
                .setHorizontalAlignment(HorizontalAlignment.CENTER);

        // Header row
        String[] headers = {"Product", "Weight", "Qty", "Discount", "Price"};
        for (String h : headers) {
            Cell headerCell = new Cell()
                    .add(new Paragraph(h).setFontSize(9).setBold().setFontColor(ColorConstants.WHITE))
                    .setBackgroundColor(BRAND_DARK)
                    .setBorder(Border.NO_BORDER)
                    .setPadding(8)
                    .setTextAlignment(h.equals("Product") ? TextAlignment.LEFT : TextAlignment.CENTER);
            table.addHeaderCell(headerCell);
        }

        // Item rows
        boolean alternate = false;
        for (OrderItemDto item : order.getOrderItems()) {
            DeviceRgb rowBg = alternate ? LIGHT_BG : ColorConstants.WHITE instanceof DeviceRgb ? (DeviceRgb) ColorConstants.WHITE : new DeviceRgb(255, 255, 255);

            String productName = item.getProduct() != null ? item.getProduct().getProductName() : "Unknown";
            String weight = item.getWeightLabel() != null ? item.getWeightLabel() : "-";
            double lineTotal = item.getOrderedProductPrice() * item.getQuantity();

            table.addCell(createDataCell(productName, rowBg, TextAlignment.LEFT));
            table.addCell(createDataCell(weight, rowBg, TextAlignment.CENTER));
            table.addCell(createDataCell(String.valueOf(item.getQuantity()), rowBg, TextAlignment.CENTER));
            table.addCell(createDataCell(String.format("%.0f%%", item.getDiscount()), rowBg, TextAlignment.CENTER));
            table.addCell(createDataCell(String.format("₹%.2f", lineTotal), rowBg, TextAlignment.CENTER));

            alternate = !alternate;
        }

        document.add(table);
    }

    private Cell createDataCell(String text, DeviceRgb bgColor, TextAlignment alignment) {
        return new Cell()
                .add(new Paragraph(text).setFontSize(9).setFontColor(BRAND_DARK))
                .setBackgroundColor(bgColor)
                .setBorder(new SolidBorder(BORDER_COLOR, 0.5f))
                .setPadding(7)
                .setTextAlignment(alignment)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);
    }

    private void addTotalsSection(Document document, OrderDto order) {
        Table totalsTable = new Table(UnitValue.createPercentArray(new float[]{3, 1}))
                .useAllAvailableWidth()
                .setMarginTop(12)
                .setBorder(Border.NO_BORDER);

        // Empty left cell
        totalsTable.addCell(new Cell().setBorder(Border.NO_BORDER));

        // Right: totals box
        Cell totalsBox = new Cell()
                .setBorder(Border.NO_BORDER)
                .setBackgroundColor(LIGHT_BG)
                .setPadding(12)
                .setBorderRadius(null);

        // Subtotal
        Table innerTotals = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .useAllAvailableWidth()
                .setBorder(Border.NO_BORDER);

        innerTotals.addCell(new Cell().setBorder(Border.NO_BORDER)
                .add(new Paragraph("Subtotal").setFontSize(9).setFontColor(BRAND_MID)));
        innerTotals.addCell(new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT)
                .add(new Paragraph(String.format("₹%.2f", order.getTotalAmount())).setFontSize(9).setFontColor(BRAND_DARK)));

        // Tax
        innerTotals.addCell(new Cell().setBorder(Border.NO_BORDER)
                .add(new Paragraph("Tax (0%)").setFontSize(9).setFontColor(BRAND_MID)));
        innerTotals.addCell(new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT)
                .add(new Paragraph("₹0.00").setFontSize(9).setFontColor(BRAND_DARK)));

        // Separator line
        innerTotals.addCell(new Cell(1, 2).setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(BORDER_COLOR, 1))
                .setPaddingTop(4).setPaddingBottom(4));

        // Total
        innerTotals.addCell(new Cell().setBorder(Border.NO_BORDER)
                .add(new Paragraph("Total").setFontSize(12).setBold().setFontColor(BRAND_DARK)));
        innerTotals.addCell(new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT)
                .add(new Paragraph(String.format("₹%.2f", order.getTotalAmount()))
                        .setFontSize(12).setBold().setFontColor(BRAND_DARK)));

        totalsBox.add(innerTotals);
        totalsTable.addCell(totalsBox);

        document.add(totalsTable);
    }

    private void addFooter(Document document) {
        document.add(new Paragraph("Thank you for shopping with FitNuz!")
                .setFontSize(11)
                .setBold()
                .setFontColor(BRAND_DARK)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(20));
        document.add(new Paragraph("For any queries, reach out to us at support@fitnuz.com")
                .setFontSize(8)
                .setFontColor(BRAND_MID)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(2));
    }
}
