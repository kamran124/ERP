package com.maenterprise.erp.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "suppliers")
public class Supplier {
    @PrimaryKey
    @NonNull
    public String supplierId;
    
    @NonNull
    public String supplierName;
    
    public String contactDetails;
    public int paymentTermsDays = 0;
    public boolean isActive = true;
    public String imageUrl;

    public Supplier() {}
}
