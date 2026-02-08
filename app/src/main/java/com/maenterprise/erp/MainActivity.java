package com.maenterprise.erp;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import com.maenterprise.erp.databinding.ActivityMainBinding;
import com.maenterprise.erp.sync.SyncWorker;

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

        // Master Data Actions
        binding.cardCustomer.setOnClickListener(v -> startActivity(new Intent(this, CustomerActivity.class)));
        binding.cardVendor.setOnClickListener(v -> startActivity(new Intent(this, SupplierActivity.class)));
        binding.cardProduct.setOnClickListener(v -> startActivity(new Intent(this, ProductActivity.class)));

        // Trigger initial sync to fetch Master Data
        WorkRequest syncRequest = new OneTimeWorkRequest.Builder(SyncWorker.class).build();
        WorkManager.getInstance(this).enqueue(syncRequest);
    }
}