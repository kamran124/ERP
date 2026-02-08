package com.maenterprise.erp.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class IdGenerator {
    public static String generateBatchId() {
        String year = new SimpleDateFormat("yyyy", Locale.getDefault()).format(new Date());
        long timestamp = System.currentTimeMillis() % 100000;
        return String.format("BATCH-%s-%03d", year, timestamp);
    }

    public static String generateSaleId() {
        String year = new SimpleDateFormat("yyyy", Locale.getDefault()).format(new Date());
        long timestamp = System.currentTimeMillis() % 100000;
        return String.format("SALE-%s-%03d", year, timestamp);
    }
    
    public static String generatePaymentId() {
        return "PAY-" + System.currentTimeMillis();
    }
}