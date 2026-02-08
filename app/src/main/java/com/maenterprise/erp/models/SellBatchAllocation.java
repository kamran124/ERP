package com.maenterprise.erp.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "sell_batch_allocations",
        foreignKeys = {
                @ForeignKey(entity = SaleItem.class,
                        parentColumns = "saleItemId",
                        childColumns = "sellProductId",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = PurchaseBatch.class,
                        parentColumns = "batchId",
                        childColumns = "batchId"),
                @ForeignKey(entity = SalesTransaction.class,
                        parentColumns = "saleId",
                        childColumns = "saleId")
        })
public class SellBatchAllocation {
    @PrimaryKey
    @NonNull
    public String allocationId;

    @NonNull
    public String saleId;

    @NonNull
    public String productId;
    
    @NonNull
    public String sellProductId;
    
    @NonNull
    public String batchId;
    
    public double allocatedQuantity;
    public double purchasePricePerSku;
    
    public String allocationSource = "AUTO_FIFO"; // AUTO_FIFO / MANUAL
    
    public boolean isSynced;

    public SellBatchAllocation() {}
}
