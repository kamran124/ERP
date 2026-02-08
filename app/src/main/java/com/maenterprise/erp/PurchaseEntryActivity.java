package com.maenterprise.erp;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.maenterprise.erp.data.ErpRepository;
import com.maenterprise.erp.databinding.ActivityPurchaseEntryBinding;
import com.maenterprise.erp.databinding.ItemSelectedProductBinding;
import com.maenterprise.erp.models.Product;
import com.maenterprise.erp.models.PurchaseBatch;
import com.maenterprise.erp.models.PurchaseTransaction;
import com.maenterprise.erp.models.Supplier;
import com.maenterprise.erp.utils.IdGenerator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PurchaseEntryActivity extends AppCompatActivity {
    private ActivityPurchaseEntryBinding binding;
    private ErpRepository repository;
    private List<Supplier> suppliersList = new ArrayList<>();
    private List<Product> productsList = new ArrayList<>();
    private String selectedSupplierId = "";
    private Product selectedProduct = null;
    private final Calendar calendar = Calendar.getInstance();
    
    private List<PurchaseBatch> selectedBatches = new ArrayList<>();
    private ProductsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPurchaseEntryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = new ErpRepository(getApplication());

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.btnSave.setOnClickListener(v -> savePurchase());
        binding.btnAddProduct.setOnClickListener(v -> addProductToList());

        // Date Picker Setup
        binding.etDate.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
        binding.etDate.setOnClickListener(v -> showDatePicker());
        binding.etDate.setFocusable(false);

        setupDropdowns();
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        adapter = new ProductsAdapter();
        binding.rvProducts.setLayoutManager(new LinearLayoutManager(this));
        binding.rvProducts.setAdapter(adapter);
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
        repository.getActiveSuppliers().observe(this, suppliers -> {
            this.suppliersList = suppliers;
            List<String> names = new ArrayList<>();
            for (Supplier s : suppliers) {
                names.add(s.supplierName + " (" + s.supplierId + ")");
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, names);
            binding.etSupplierId.setAdapter(adapter);
        });

        binding.etSupplierId.setOnItemClickListener((parent, view, position, id) -> {
            selectedSupplierId = suppliersList.get(position).supplierId;
        });

        repository.getActiveProducts().observe(this, products -> {
            this.productsList = products;
            List<String> names = new ArrayList<>();
            for (Product p : products) {
                names.add(p.productName + " (" + p.productId + ")");
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, names);
            binding.etProductId.setAdapter(adapter);
        });

        binding.etProductId.setOnItemClickListener((parent, view, position, id) -> {
            selectedProduct = productsList.get(position);
            if (selectedProduct != null) {
                binding.etSchemeGlass.setText(String.valueOf(selectedProduct.baseGlassPerSku));
            }
        });
    }
    
    private void addProductToList() {
        String qtyStr = binding.etQuantity.getText().toString();
        String priceStr = binding.etPrice.getText().toString();
        String schemeGlassStr = binding.etSchemeGlass.getText().toString();

        if (selectedProduct == null) {
            Toast.makeText(this, "Please select a product", Toast.LENGTH_SHORT).show();
            return;
        }

        if (qtyStr.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Quantity and price are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Duplicate Check
        for (PurchaseBatch b : selectedBatches) {
            if (b.productId.equals(selectedProduct.productId)) {
                Toast.makeText(this, "Product already added to list", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        try {
            double quantity = Double.parseDouble(qtyStr);
            double price = Double.parseDouble(priceStr);
            int schemeGlass = schemeGlassStr.isEmpty() ? selectedProduct.baseGlassPerSku : Integer.parseInt(schemeGlassStr);

            PurchaseBatch batch = new PurchaseBatch();
            batch.batchId = IdGenerator.generateBatchId();
            batch.productId = selectedProduct.productId;
            batch.purchasePricePerSku = price;
            batch.batchQuantity = quantity;
            batch.remainingQuantity = quantity;
            batch.schemeGlassPerSku = schemeGlass;
            
            selectedBatches.add(batch);
            adapter.notifyDataSetChanged();
            
            // Clear product fields
            binding.etProductId.setText("");
            binding.etQuantity.setText("");
            binding.etPrice.setText("");
            binding.etSchemeGlass.setText("");
            selectedProduct = null;
            
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid numbers", Toast.LENGTH_SHORT).show();
        }
    }

    private void savePurchase() {
        String date = binding.etDate.getText().toString();
        String remarks = binding.etRemarks.getText().toString();
        String invoice = binding.etInvoiceNumber.getText().toString();

        if (date.isEmpty()) {
            Toast.makeText(this, "Purchase date is required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedSupplierId.isEmpty()) {
            Toast.makeText(this, "Please select a supplier", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedBatches.isEmpty()) {
            Toast.makeText(this, "Please add at least one product", Toast.LENGTH_SHORT).show();
            return;
        }

        String purchaseId = IdGenerator.generatePurchaseId();

        PurchaseTransaction transaction = new PurchaseTransaction();
        transaction.purchaseId = purchaseId;
        transaction.purchaseDate = date;
        transaction.supplierId = selectedSupplierId;
        transaction.invoiceNumber = invoice;
        transaction.remarks = remarks;
        transaction.isSynced = false;

        for (PurchaseBatch batch : selectedBatches) {
            batch.purchaseId = purchaseId;
            batch.purchaseDate = date;
            batch.supplierId = selectedSupplierId;
            batch.isSynced = false;
        }

        repository.insertFullPurchase(transaction, selectedBatches);
        Toast.makeText(this, "Purchase saved successfully", Toast.LENGTH_SHORT).show();
        finish();
    }

    private class ProductsAdapter extends RecyclerView.Adapter<ProductsAdapter.ViewHolder> {
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(ItemSelectedProductBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            PurchaseBatch item = selectedBatches.get(position);
            holder.binding.tvProductName.setText("Product ID: " + item.productId);
            holder.binding.tvProductDetails.setText(String.format(Locale.getDefault(), "Qty: %.2f | Price: ₹%.2f", item.batchQuantity, item.purchasePricePerSku));
            holder.binding.btnRemove.setOnClickListener(v -> {
                selectedBatches.remove(position);
                notifyDataSetChanged();
            });
        }

        @Override
        public int getItemCount() {
            return selectedBatches.size();
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
