package com.maenterprise.erp.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "payments",
        foreignKeys = @ForeignKey(entity = SalesTransaction.class,
                parentColumns = "saleId",
                childColumns = "saleId",
                onDelete = ForeignKey.CASCADE))
public class Payment {
    @PrimaryKey
    @NonNull
    public String paymentId;
    
    @NonNull
    public String saleId;
    
    @NonNull
    public String paymentDate;
    
    public double amountReceived;
    
    public String paymentMode = "CASH"; // CASH / UPI / BANK
    
    public String remarks;
    
    public boolean isSynced;

    public Payment() {}
}
