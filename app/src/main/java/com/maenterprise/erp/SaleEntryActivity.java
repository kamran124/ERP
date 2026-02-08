package com.maenterprise.erp;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.maenterprise.erp.data.ErpRepository;
import com.maenterprise.erp.databinding.ActivitySaleEntryBinding;
import com.maenterprise.erp.models.SalesTransaction;
import com.maenterprise.erp.utils.IdGenerator;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SaleEntryActivity extends AppCompatActivity {
    private ActivitySaleEntryBinding binding;
    private ErpRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySaleEntryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = new ErpRepository(getApplication());

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.btnSave.setOnClickListener(v -> submitSale());
        
        // Default date
        binding.etDate.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
    }

    private void submitSale() {
        String date = binding.etDate.getText().toString();
        String customerId = binding.etCustomerId.getText().toString();
        String productId = binding.etProductId.getText().toString();
        String qtyStr = binding.etQuantity.getText().toString();
        String priceStr = binding.etPrice.getText().toString();
        String channel = binding.etChannel.getText().toString();
        String status = binding.etStatus.getText().toString();
        String remarks = binding.etRemarks.getText().toString();

        if (date.isEmpty() || customerId.isEmpty() || productId.isEmpty() || qtyStr.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        SalesTransaction transaction = new SalesTransaction();
        transaction.saleId = IdGenerator.generateSaleId();
        transaction.saleDate = date;
        transaction.customerId = customerId;
        transaction.productId = productId;
        transaction.quantitySku = Double.parseDouble(qtyStr);
        transaction.sellingPricePerSku = Double.parseDouble(priceStr);
        transaction.salesChannel = channel;
        transaction.invoiceNumber = "INV-" + System.currentTimeMillis();
        transaction.paymentStatus = status;
        transaction.remarks = remarks;
        transaction.isSynced = false;

        repository.processSale(transaction, new ErpRepository.SaleCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(SaleEntryActivity.this, "Sale Processed Successfully", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> Toast.makeText(SaleEntryActivity.this, "Error: " + message, Toast.LENGTH_LONG).show());
            }
        });
    }
}