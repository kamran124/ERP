package com.maenterprise.erp.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "payments")
public class Payment {
    @PrimaryKey
    @NonNull
    public String paymentId;
    public String saleId;
    public String paymentDate;
    public double amountReceived;
    public String paymentMode;
    public String remarks;
    public boolean isSynced;

    public Payment() {}
}