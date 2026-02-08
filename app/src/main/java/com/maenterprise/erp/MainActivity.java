package com.maenterprise.erp;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import com.maenterprise.erp.data.ErpRepository;
import com.maenterprise.erp.databinding.ActivityMainBinding;
import com.maenterprise.erp.sync.SyncWorker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private ErpRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        repository = new ErpRepository(getApplication());

        binding.cardPurchase.setOnClickListener(v -> startActivity(new Intent(this, PurchaseEntryActivity.class)));
        binding.cardSale.setOnClickListener(v -> startActivity(new Intent(this, SaleEntryActivity.class)));
        binding.cardPayment.setOnClickListener(v -> startActivity(new Intent(this, PaymentEntryActivity.class)));

        // Master Data Actions
        binding.cardCustomer.setOnClickListener(v -> startActivity(new Intent(this, CustomerActivity.class)));
        binding.cardVendor.setOnClickListener(v -> startActivity(new Intent(this, SupplierActivity.class)));
        binding.cardProduct.setOnClickListener(v -> startActivity(new Intent(this, ProductActivity.class)));

        setupStatistics();

        // Trigger initial sync to fetch Master Data
        WorkRequest syncRequest = new OneTimeWorkRequest.Builder(SyncWorker.class).build();
        WorkManager.getInstance(this).enqueue(syncRequest);
    }

    private void setupStatistics() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -7);
        String lastWeekDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());

        repository.getTotalSalesSince(lastWeekDate).observe(this, total -> {
            if (total != null) {
                binding.tvTotalSales.setText(String.format(Locale.getDefault(), "₹ %.2f", total));
            } else {
                binding.tvTotalSales.setText("$ 0.00");
            }
        });
    }
}
