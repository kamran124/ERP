package com.maenterprise.erp;

import android.app.DatePickerDialog;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.maenterprise.erp.adapters.ProductDropdownAdapter;
import com.maenterprise.erp.data.ErpRepository;
import com.maenterprise.erp.databinding.ActivitySaleEntryBinding;
import com.maenterprise.erp.databinding.ItemProductBinding;
import com.maenterprise.erp.databinding.ItemSelectedProductBinding;
import com.maenterprise.erp.models.Customer;
import com.maenterprise.erp.models.PaymentStatus;
import com.maenterprise.erp.models.Product;
import com.maenterprise.erp.models.ProductInventory;
import com.maenterprise.erp.models.SaleItem;
import com.maenterprise.erp.models.SalesChannel;
import com.maenterprise.erp.models.SalesTransaction;
import com.maenterprise.erp.utils.IdGenerator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SaleEntryActivity extends AppCompatActivity {
    private ActivitySaleEntryBinding binding;
    private ErpRepository repository;
    private List<Customer> customersList = new ArrayList<>();
    private List<ProductInventory> inventoryList = new ArrayList<>();
    private String selectedCustomerId = "";
    private ProductInventory selectedInventory = null;
    private final Calendar calendar = Calendar.getInstance();
    
    private List<SaleItem> selectedItems = new ArrayList<>();
    private ItemsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySaleEntryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = new ErpRepository(getApplication());

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.btnSave.setOnClickListener(v -> submitSale());
        binding.btnAddProduct.setOnClickListener(v -> addProductToList());
        
        // Date Picker
        binding.etDate.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
        binding.etDate.setOnClickListener(v -> showDatePicker());
        binding.etDate.setFocusable(false);

        setupDropdowns();
        setupRecyclerView();
        setupQuantityValidation();
    }

    private void setupRecyclerView() {
        adapter = new ItemsAdapter();
        binding.rvProducts.setLayoutManager(new LinearLayoutManager(this));
        binding.rvProducts.setAdapter(adapter);
    }

    private void setupQuantityValidation() {
        binding.etQuantity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (selectedInventory == null) return;
                
                String qtyStr = s.toString();
                if (qtyStr.isEmpty()) return;

                try {
                    double quantity = Double.parseDouble(qtyStr);
                    if (quantity > selectedInventory.totalQuantity) {
                        binding.etQuantity.setError("Exceeds stock (" + selectedInventory.totalQuantity + ")");
                    } else {
                        binding.etQuantity.setError(null);
                    }
                } catch (NumberFormatException e) {
                    binding.etQuantity.setError("Invalid number");
                }
            }
        });
    }

    private void showDatePicker() {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            binding.etDate.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime()));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void setupDropdowns() {
        // Customers Dropdown
        repository.getActiveCustomers().observe(this, customers -> {
            this.customersList = customers;
            List<String> names = new ArrayList<>();
            for (Customer c : customers) {
                names.add(c.customerName + " (" + c.customerId + ")");
            }
            ArrayAdapter<String> customerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, names);
            binding.etCustomerId.setAdapter(customerAdapter);
        });

        binding.etCustomerId.setOnItemClickListener((parent, view, position, id) -> {
            selectedCustomerId = customersList.get(position).customerId;
        });

        // Product Selection Dropdown with Images and Stock Filter
        repository.getProductInventory().observe(this, inventory -> {
            List<ProductInventory> filteredInventory = new ArrayList<>();
            for (ProductInventory item : inventory) {
                if (item.totalQuantity > 0) {
                    filteredInventory.add(item);
                }
            }
            
            this.inventoryList = filteredInventory;
            
            if (filteredInventory.isEmpty()) {
                binding.etProductId.setText("No product is available in stock", false);
                binding.etProductId.setEnabled(false);
            } else {
                binding.etProductId.setEnabled(true);
                binding.etProductId.setText("", false); // Clear if previously set to "No product..."
                ProductDropdownAdapter productAdapter = new ProductDropdownAdapter(this, filteredInventory);
                binding.etProductId.setAdapter(productAdapter);
            }
        });

        binding.etProductId.setOnItemClickListener((parent, view, position, id) -> {
            selectedInventory = inventoryList.get(position);
            binding.etProductId.setText(selectedInventory.product.productName, false);
            binding.etQuantity.setHint("Max: " + selectedInventory.totalQuantity);
            // Trigger validation if there's already text in quantity
            binding.etQuantity.setText(binding.etQuantity.getText());
        });

        // Sales Channel Dropdown
        ArrayAdapter<SalesChannel> channelAdapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_dropdown_item_1line, SalesChannel.values());
        binding.etChannel.setAdapter(channelAdapter);

        // Payment Status Dropdown
        ArrayAdapter<PaymentStatus> statusAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, PaymentStatus.values());
        binding.etStatus.setAdapter(statusAdapter);
        binding.etStatus.setText(PaymentStatus.UNPAID.toString(), false);
    }

    private void addProductToList() {
        String qtyStr = binding.etQuantity.getText().toString();
        String priceStr = binding.etPrice.getText().toString();

        if (selectedInventory == null) {
            Toast.makeText(this, "Please select a product", Toast.LENGTH_SHORT).show();
            return;
        }

        if (qtyStr.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Quantity and price are required", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double quantity = Double.parseDouble(qtyStr);
            double price = Double.parseDouble(priceStr);

            if (quantity > selectedInventory.totalQuantity) {
                Toast.makeText(this, "Quantity exceeds available stock", Toast.LENGTH_SHORT).show();
                return;
            }

            // Duplicate Product Prevention
            for (SaleItem existingItem : selectedItems) {
                if (existingItem.productId.equals(selectedInventory.product.productId)) {
                    Toast.makeText(this, "Product already added to list", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            SaleItem item = new SaleItem();
            item.productId = selectedInventory.product.productId;
            item.quantitySku = quantity;
            item.sellingPricePerSku = price;
            
            selectedItems.add(item);
            adapter.notifyDataSetChanged();
            
            // Clear fields
            binding.etProductId.setText("");
            binding.etQuantity.setText("");
            binding.etPrice.setText("");
            binding.etQuantity.setHint("Quantity");
            selectedInventory = null;
            
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid numbers", Toast.LENGTH_SHORT).show();
        }
    }

    private void submitSale() {
        String date = binding.etDate.getText().toString();
        String channel = binding.etChannel.getText().toString();
        String status = binding.etStatus.getText().toString();
        String remarks = binding.etRemarks.getText().toString();

        if (date.isEmpty()) {
            Toast.makeText(this, "Sale date is required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedCustomerId.isEmpty()) {
            Toast.makeText(this, "Please select a customer", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedItems.isEmpty()) {
            Toast.makeText(this, "Please add at least one product", Toast.LENGTH_SHORT).show();
            return;
        }

        SalesTransaction transaction = new SalesTransaction();
        transaction.saleId = IdGenerator.generateSaleId();
        transaction.saleDate = date;
        transaction.customerId = selectedCustomerId;
        transaction.salesChannel = channel;
        transaction.invoiceNumber = "INV-" + System.currentTimeMillis();
        transaction.paymentStatus = status;
        transaction.remarks = remarks;
        transaction.isSynced = false;

        for (SaleItem item : selectedItems) {
            item.saleId = transaction.saleId;
        }

        repository.processFullSale(transaction, selectedItems, new ErpRepository.SaleCallback() {
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

    private class ItemsAdapter extends RecyclerView.Adapter<ItemsAdapter.ViewHolder> {
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(ItemSelectedProductBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            SaleItem item = selectedItems.get(position);
            holder.binding.tvProductName.setText("Product ID: " + item.productId);
            holder.binding.tvProductDetails.setText(String.format(Locale.getDefault(), "Qty: %.2f | Price: ₹%.2f", item.quantitySku, item.sellingPricePerSku));
            holder.binding.btnRemove.setOnClickListener(v -> {
                int currentPos = holder.getAdapterPosition();
                if (currentPos != RecyclerView.NO_POSITION) {
                    selectedItems.remove(currentPos);
                    notifyItemRemoved(currentPos);
                    notifyItemRangeChanged(currentPos, selectedItems.size());
                }
            });
        }

        @Override
        public int getItemCount() {
            return selectedItems.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ItemSelectedProductBinding binding;
            ViewHolder(ItemSelectedProductBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}
