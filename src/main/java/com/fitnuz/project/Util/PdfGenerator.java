package com.fitnuz.project.Util;


import com.fitnuz.project.Payload.DTO.OrderItemDto;
import com.fitnuz.project.Payload.Response.OrderResponse;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;

@Component
public class PdfGenerator {

    public byte[] generateOrderReport(OrderResponse orderResponse) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("Order Summary").setBold().setFontSize(18).setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph("Order ID: " + orderResponse.getOrderId()));
        document.add(new Paragraph("Email: " + orderResponse.getEmail()));
        document.add(new Paragraph("Order Date: " + orderResponse.getOrderDate()));
        document.add(new Paragraph("Delivery Address: " + orderResponse.getAddress()));
        document.add(new Paragraph("Total Amount: ₹" + orderResponse.getTotalAmount()));
        document.add(new Paragraph("Payment Status: " + orderResponse.getPayment().getPgStatus()));


        document.add(new Paragraph("\nOrdered Items:").setBold());

        Table table = new Table(4);
        table.addHeaderCell("Product");
        table.addHeaderCell("Quantity");
        table.addHeaderCell("Price");
        table.addHeaderCell("Discount");

        for (OrderItemDto item : orderResponse.getOrderItems()) {
            table.addCell(item.getProduct().getProductName());
            table.addCell(String.valueOf(item.getQuantity()));
            table.addCell("₹" + item.getOrderedProductPrice());
            table.addCell(item.getDiscount() + "%");
        }

        document.add(table);
        document.close();

        return out.toByteArray();
    }
}
