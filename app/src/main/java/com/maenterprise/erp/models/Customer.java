package com.maenterprise.erp.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "customers")
public class Customer {
    @PrimaryKey
    @NonNull
    public String customerId;
    
    @NonNull
    public String customerName;
    
    public String customerType = "WHOLESALE"; // WHOLESALE / RETAIL
    public double creditLimit = 0;
    public int paymentTermsDays = 0;
    public boolean isActive = true;
    public String imageUrl;

    public Customer() {}
}
