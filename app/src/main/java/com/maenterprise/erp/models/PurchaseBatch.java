package com.maenterprise.erp.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "purchase_batches")
public class PurchaseBatch {
    @PrimaryKey
    @NonNull
    public String batchId;
    public String purchaseDate;
    public String productId;
    public double purchasePricePerSku;
    public double batchQuantity;
    public double remainingQuantity;
    public String supplierId;
    public String remarks;
    public boolean isSynced;

    public PurchaseBatch() {}
}