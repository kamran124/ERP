package com.maenterprise.erp;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.maenterprise.erp.data.ErpRepository;
import com.maenterprise.erp.databinding.ActivitySaleEntryBinding;
import com.maenterprise.erp.models.Customer;
import com.maenterprise.erp.models.Product;
import com.maenterprise.erp.models.SalesTransaction;
import com.maenterprise.erp.utils.IdGenerator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SaleEntryActivity extends AppCompatActivity {
    private ActivitySaleEntryBinding binding;
    private ErpRepository repository;
    private List<Customer> customersList = new ArrayList<>();
    private List<Product> productsList = new ArrayList<>();
    private String selectedCustomerId = "";
    private String selectedProductId = "";

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

        setupDropdowns();
    }

    private void setupDropdowns() {
        // Customers Dropdown
        repository.getActiveCustomers().observe(this, customers -> {
            this.customersList = customers;
            List<String> names = new ArrayList<>();
            for (Customer c : customers) {
                names.add(c.customerName + " (" + c.customerId + ")");
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, names);
            binding.etCustomerId.setAdapter(adapter);
        });

        binding.etCustomerId.setOnClickListener(v -> ((AutoCompleteTextView) v).showDropDown());
        binding.etCustomerId.setOnItemClickListener((parent, view, position, id) -> {
            selectedCustomerId = customersList.get(position).customerId;
        });

        // Products Dropdown
        repository.getActiveProducts().observe(this, products -> {
            this.productsList = products;
            List<String> names = new ArrayList<>();
            for (Product p : products) {
                names.add(p.productName + " (" + p.productId + ")");
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, names);
            binding.etProductId.setAdapter(adapter);
        });

        binding.etProductId.setOnClickListener(v -> ((AutoCompleteTextView) v).showDropDown());
        binding.etProductId.setOnItemClickListener((parent, view, position, id) -> {
            selectedProductId = productsList.get(position).productId;
        });
    }

    private void submitSale() {
        String date = binding.etDate.getText().toString();
        String qtyStr = binding.etQuantity.getText().toString();
        String priceStr = binding.etPrice.getText().toString();
        String channel = binding.etChannel.getText().toString();
        String status = binding.etStatus.getText().toString();
        String remarks = binding.etRemarks.getText().toString();

        if (date.isEmpty() || selectedCustomerId.isEmpty() || selectedProductId.isEmpty() || qtyStr.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            SalesTransaction transaction = new SalesTransaction();
            transaction.saleId = IdGenerator.generateSaleId();
            transaction.saleDate = date;
            transaction.customerId = selectedCustomerId;
            transaction.productId = selectedProductId;
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
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid quantity or price format", Toast.LENGTH_SHORT).show();
        }
    }
}