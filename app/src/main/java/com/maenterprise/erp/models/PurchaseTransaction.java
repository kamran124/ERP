package com.maenterprise.erp.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "purchase_transactions")
public class PurchaseTransaction {
    @PrimaryKey
    @NonNull
    public String purchaseId;
    public String purchaseDate;
    public String supplierId;
    public String invoiceNumber;
    public String remarks;
    public boolean isSynced;

    public PurchaseTransaction() {}
}
