package com.maenterprise.erp;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.maenterprise.erp.data.ErpRepository;
import com.maenterprise.erp.databinding.ActivityPurchaseEntryBinding;
import com.maenterprise.erp.models.PurchaseBatch;
import com.maenterprise.erp.utils.IdGenerator;

public class PurchaseEntryActivity extends AppCompatActivity {
    private ActivityPurchaseEntryBinding binding;
    private ErpRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPurchaseEntryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = new ErpRepository(getApplication());

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.btnSave.setOnClickListener(v -> savePurchase());
    }

    private void savePurchase() {
        String date = binding.etDate.getText().toString();
        String supplierId = binding.etSupplierId.getText().toString();
        String productId = binding.etProductId.getText().toString();
        String qtyStr = binding.etQuantity.getText().toString();
        String priceStr = binding.etPrice.getText().toString();
        String remarks = binding.etRemarks.getText().toString();

        if (date.isEmpty() || supplierId.isEmpty() || productId.isEmpty() || qtyStr.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double quantity = Double.parseDouble(qtyStr);
        double price = Double.parseDouble(priceStr);

        PurchaseBatch batch = new PurchaseBatch();
        batch.batchId = IdGenerator.generateBatchId();
        batch.purchaseDate = date;
        batch.productId = productId;
        batch.purchasePricePerSku = price;
        batch.batchQuantity = quantity;
        batch.remainingQuantity = quantity;
        batch.supplierId = supplierId;
        batch.remarks = remarks;
        batch.isSynced = false;

        repository.insertPurchaseBatch(batch);
        Toast.makeText(this, "Purchase Batch Created: " + batch.batchId, Toast.LENGTH_SHORT).show();
        finish();
    }
}