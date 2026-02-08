package com.maenterprise.erp.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "purchase_batches",
        foreignKeys = {
                @ForeignKey(entity = Product.class,
                        parentColumns = "productId",
                        childColumns = "productId"),
                @ForeignKey(entity = Supplier.class,
                        parentColumns = "supplierId",
                        childColumns = "supplierId"),
                @ForeignKey(entity = PurchaseTransaction.class,
                        parentColumns = "purchaseId",
                        childColumns = "purchaseId",
                        onDelete = ForeignKey.CASCADE)
        })
public class PurchaseBatch {
    @PrimaryKey
    @NonNull
    public String batchId;

    @NonNull
    public String purchaseId;
    
    @NonNull
    public String purchaseDate;
    
    @NonNull
    public String productId;
    
    public double purchasePricePerSku;
    public double batchQuantity;
    public double remainingQuantity;
    public int schemeGlassPerSku = 0;
    
    @NonNull
    public String supplierId;
    
    public String remarks;

    public boolean isSynced;

    public PurchaseBatch() {}
}
