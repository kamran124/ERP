package com.maenterprise.erp.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "sales_transactions")
public class SalesTransaction {
    @PrimaryKey
    @NonNull
    public String saleId;
    public String saleDate;
    public String customerId;
    public String productId;
    public double quantitySku;
    public double sellingPricePerSku;
    public String salesChannel;
    public String invoiceNumber;
    public String paymentStatus;
    public String remarks;
    public boolean isSynced;

    public SalesTransaction() {}
}