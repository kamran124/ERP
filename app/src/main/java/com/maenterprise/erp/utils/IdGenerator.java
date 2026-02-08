package com.maenterprise.erp.utils;

import java.util.UUID;

public class IdGenerator {
    public static String generateBatchId() {
        return "BATCH-" + System.currentTimeMillis();
    }

    public static String generatePurchaseId() {
        return "PUR-" + System.currentTimeMillis();
    }

    public static String generateSaleId() {
        return "SALE-" + System.currentTimeMillis();
    }

    public static String generatePaymentId() {
        return "PAY-" + System.currentTimeMillis();
    }

    public static String generateProductId() {
        return "PROD-" + System.currentTimeMillis();
    }

    public static String generateCustomerId() {
        return "CUST-" + System.currentTimeMillis();
    }

    public static String generateSupplierId() {
        return "SUPP-" + System.currentTimeMillis();
    }
}
