package com.maenterprise.erp;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.maenterprise.erp.data.ErpRepository;
import com.maenterprise.erp.databinding.ActivityProductBinding;
import com.maenterprise.erp.databinding.ItemProductBinding;
import com.maenterprise.erp.models.Product;
import com.maenterprise.erp.models.ProductInventory;
import com.maenterprise.erp.models.Supplier;
import com.maenterprise.erp.utils.IdGenerator;
import java.util.ArrayList;
import java.util.List;

public class ProductActivity extends AppCompatActivity {
    private ActivityProductBinding binding;
    private ErpRepository repository;
    private ProductAdapter adapter;
    private Uri selectedImageUri;
    private ImageView dialogImageView;

    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    try {
                        final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                        getContentResolver().takePersistableUriPermission(uri, takeFlags);
                        selectedImageUri = uri;
                        if (dialogImageView != null) {
                            dialogImageView.setImageURI(uri);
                            dialogImageView.setPadding(0, 0, 0, 0);
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Failed to select image", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = new ErpRepository(getApplication());
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        setupRecyclerView();
        
        repository.getProductInventory().observe(this, inventoryList -> {
            adapter.setInventory(inventoryList);
        });

        binding.fabAddProduct.setOnClickListener(v -> showAddProductDialog());
    }

    private void showAddProductDialog() {
        selectedImageUri = null;
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_product, null);
        EditText etName = dialogView.findViewById(R.id.etProductName);
        AutoCompleteTextView actvBrand = dialogView.findViewById(R.id.actvBrand);
        EditText etPack = dialogView.findViewById(R.id.etPackSize);
        EditText etBaseGlass = dialogView.findViewById(R.id.etBaseGlass);
        dialogImageView = dialogView.findViewById(R.id.ivProductImage);
        View btnSelect = dialogView.findViewById(R.id.btnSelectImage);

        repository.getActiveSuppliers().observe(this, suppliers -> {
            List<String> supplierNames = new ArrayList<>();
            for (Supplier s : suppliers) {
                supplierNames.add(s.supplierName);
            }
            ArrayAdapter<String> brandAdapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_dropdown_item_1line, supplierNames);
            actvBrand.setAdapter(brandAdapter);
        });

        btnSelect.setOnClickListener(v -> {
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });

        new AlertDialog.Builder(this)
                .setTitle("Add New Product")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Product p = new Product();
                    p.productId = IdGenerator.generateProductId();
                    p.productName = name;
                    p.brand = actvBrand.getText().toString();
                    p.packSize = etPack.getText().toString();
                    try {
                        p.baseGlassPerSku = Integer.parseInt(etBaseGlass.getText().toString());
                    } catch (Exception e) { p.baseGlassPerSku = 24; }
                    p.imageUrl = selectedImageUri != null ? selectedImageUri.toString() : null;
                    p.isActive = true;
                    
                    repository.insertProduct(p);
                    Toast.makeText(this, "Product added successfully", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditProductDialog(Product product) {
        selectedImageUri = product.imageUrl != null ? Uri.parse(product.imageUrl) : null;
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_product, null);
        EditText etName = dialogView.findViewById(R.id.etProductName);
        AutoCompleteTextView actvBrand = dialogView.findViewById(R.id.actvBrand);
        EditText etPack = dialogView.findViewById(R.id.etPackSize);
        EditText etBaseGlass = dialogView.findViewById(R.id.etBaseGlass);
        dialogImageView = dialogView.findViewById(R.id.ivProductImage);
        View btnSelect = dialogView.findViewById(R.id.btnSelectImage);

        etName.setText(product.productName);
        actvBrand.setText(product.brand);
        etPack.setText(product.packSize);
        etBaseGlass.setText(String.valueOf(product.baseGlassPerSku));

        if (selectedImageUri != null) {
            dialogImageView.setImageURI(selectedImageUri);
            dialogImageView.setPadding(0, 0, 0, 0);
        }

        repository.getActiveSuppliers().observe(this, suppliers -> {
            List<String> supplierNames = new ArrayList<>();
            for (Supplier s : suppliers) {
                supplierNames.add(s.supplierName);
            }
            ArrayAdapter<String> brandAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_dropdown_item_1line, supplierNames);
            actvBrand.setAdapter(brandAdapter);
        });

        btnSelect.setOnClickListener(v -> {
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });

        new AlertDialog.Builder(this)
                .setTitle("Edit Product")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    product.productName = name;
                    product.brand = actvBrand.getText().toString();
                    product.packSize = etPack.getText().toString();
                    try {
                        product.baseGlassPerSku = Integer.parseInt(etBaseGlass.getText().toString());
                    } catch (Exception e) { /* Do nothing */ }
                    product.imageUrl = selectedImageUri != null ? selectedImageUri.toString() : null;

                    repository.updateProduct(product);
                    Toast.makeText(this, "Product updated successfully", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setupRecyclerView() {
        adapter = new ProductAdapter(this::showEditProductDialog, repository);
        binding.rvProducts.setLayoutManager(new LinearLayoutManager(this));
        binding.rvProducts.setAdapter(adapter);
    }

    private static class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {
        private List<ProductInventory> inventoryList = new ArrayList<>();
        private final ErpRepository repository;
        private final ProductEditListener editListener;


        ProductAdapter(ProductEditListener editListener, ErpRepository repository) {
            this.repository = repository;
            this.editListener = editListener;
        }

        void setInventory(List<ProductInventory> inventoryList) {
            this.inventoryList = inventoryList;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemProductBinding binding = ItemProductBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new ViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ProductInventory item = inventoryList.get(position);
            Product product = item.product;
            holder.binding.tvProductName.setText(product.productName);
            holder.binding.tvBrand.setText(product.brand);
            holder.binding.tvPackSize.setText(product.packSize);
            holder.binding.tvAvailableQty.setText("Stock: " + item.totalQuantity);
            
            if (product.imageUrl != null) {
                try {
                    holder.binding.ivProductImage.setImageTintList(null);
                    holder.binding.ivProductImage.setPadding(0, 0, 0, 0);
                    holder.binding.ivProductImage.setImageURI(Uri.parse(product.imageUrl));
                } catch (Exception e) {
                    showPlaceholder(holder);
                }
            } else {
                showPlaceholder(holder);
            }

            if (item.totalQuantity <= 0) {
                holder.binding.tvAvailableQty.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_red_dark));
            } else {
                holder.binding.tvAvailableQty.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.purple_500));
            }

            holder.itemView.setOnLongClickListener(v -> {
                new AlertDialog.Builder(v.getContext())
                        .setTitle("Options")
                        .setItems(new String[]{"Edit", "Delete"}, (dialog, which) -> {
                            if (which == 0) {
                                editListener.onEditProduct(product);
                            } else if (which == 1) {
                                if (item.totalQuantity > 0) {
                                    Toast.makeText(v.getContext(), "Cannot delete product with stock", Toast.LENGTH_SHORT).show();
                                } else {
                                    repository.deleteProduct(product);
                                }
                            }
                        }).show();
                return true;
            });
        }

        private void showPlaceholder(@NonNull ViewHolder holder) {
            holder.binding.ivProductImage.setImageResource(android.R.drawable.ic_menu_gallery);
            holder.binding.ivProductImage.setImageTintList(ColorStateList.valueOf(0xFFCCCCCC));
            holder.binding.ivProductImage.setPadding(24, 24, 24, 24);
        }

        @Override
        public int getItemCount() {
            return inventoryList.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ItemProductBinding binding;
            ViewHolder(ItemProductBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }   }

    public interface ProductEditListener {
        void onEditProduct(Product product);
    }
}
