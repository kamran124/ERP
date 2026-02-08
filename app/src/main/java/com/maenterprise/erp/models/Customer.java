package com.maenterprise.erp.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "customers")
public class Customer {
    @PrimaryKey
    @NonNull
    public String customerId;
    public String customerName;
    public String customerType;
    public double creditLimit;
    public int paymentTermsDays;
    public boolean isActive;

    public Customer() {}
}