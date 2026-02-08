package com.maenterprise.erp.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "sales_transactions")
public class SalesTransaction {
    @PrimaryKey
    @NonNull
    public String saleId;
    
    @NonNull
    public String saleDate;
    
    @NonNull
    public String customerId;
    
    @NonNull
    public String invoiceNumber;
    
    public String salesChannel = "WHOLESALE"; // WHOLESALE / RETAIL
    public String paymentStatus = "UNPAID"; // UNPAID / PARTIAL / PAID
    public String remarks;
    public boolean isSynced;

    public SalesTransaction() {}
}
