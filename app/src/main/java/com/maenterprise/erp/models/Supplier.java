package com.maenterprise.erp.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "suppliers")
public class Supplier {
    @PrimaryKey
    @NonNull
    public String supplierId;
    public String supplierName;
    public String contactDetails;
    public int paymentTermsDays;
    public boolean isActive;

    public Supplier() {}
}