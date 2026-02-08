package com.maenterprise.erp.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.util.UUID;

@Entity(tableName = "sale_items",
        foreignKeys = {
                @ForeignKey(entity = SalesTransaction.class,
                        parentColumns = "saleId",
                        childColumns = "saleId",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Product.class,
                        parentColumns = "productId",
                        childColumns = "productId")
        })
public class SaleItem {
    @PrimaryKey
    @NonNull
    public String saleItemId;

    @NonNull
    public String saleId;

    @NonNull
    public String productId;

    public double quantitySku;

    public double sellingPricePerSku;
    
    public int schemeGlassPerSku = 0;
    
    public String remarks;

    public boolean isSynced;

    public SaleItem() {
        this.saleItemId = "SITEM-" + UUID.randomUUID().toString();
    }
}
