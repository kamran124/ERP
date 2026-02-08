package com.maenterprise.erp.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "sell_batch_allocations")
public class SellBatchAllocation {
    @PrimaryKey
    @NonNull
    public String allocationId;
    public String saleId;
    public String productId;
    public String batchId;
    public double allocatedQuantity;
    public double purchasePricePerSku;
    public boolean isSynced;

    public SellBatchAllocation() {}
}