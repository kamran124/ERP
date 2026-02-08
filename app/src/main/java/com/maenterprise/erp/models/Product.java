package com.maenterprise.erp.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "products")
public class Product {
    @PrimaryKey
    @NonNull
    public String productId;
    public String productName;
    public String brand;
    public String packSize;
    public boolean isActive;

    public Product() {}
}