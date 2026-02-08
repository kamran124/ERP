package com.maenterprise.erp.models;

import androidx.room.Embedded;

public class ProductInventory {
    @Embedded
    public Product product;
    public double totalQuantity;
}
