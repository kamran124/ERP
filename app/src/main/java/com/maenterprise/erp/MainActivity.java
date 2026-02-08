package com.maenterprise.erp;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.maenterprise.erp.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        binding.cardPurchase.setOnClickListener(v -> startActivity(new Intent(this, PurchaseEntryActivity.class)));
        binding.cardSale.setOnClickListener(v -> startActivity(new Intent(this, SaleEntryActivity.class)));
        binding.cardPayment.setOnClickListener(v -> startActivity(new Intent(this, PaymentEntryActivity.class)));
    }
}