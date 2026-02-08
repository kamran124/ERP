package com.maenterprise.erp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.maenterprise.erp.data.ErpRepository;
import com.maenterprise.erp.databinding.ActivityProductBinding;
import com.maenterprise.erp.models.Product;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProductActivity extends AppCompatActivity {
    private ActivityProductBinding binding;
    private ErpRepository repository;
    private ProductAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = new ErpRepository(getApplication());
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        setupRecyclerView();
        
        repository.getActiveProducts().observe(this, products -> {
            adapter.setProducts(products);
        });

        binding.fabAddProduct.setOnClickListener(v -> showAddProductDialog());
    }

    private void showAddProductDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_product, null);
        EditText etName = dialogView.findViewById(R.id.etProductName);
        EditText etBrand = dialogView.findViewById(R.id.etBrand);
        EditText etPack = dialogView.findViewById(R.id.etPackSize);

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
                    p.productId = "PROD-" + UUID.randomUUID().toString().substring(0, 8);
                    p.productName = name;
                    p.brand = etBrand.getText().toString();
                    p.packSize = etPack.getText().toString();
                    p.isActive = true;
                    
                    repository.insertProduct(p);
                    Toast.makeText(this, "Product added locally", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setupRecyclerView() {
        adapter = new ProductAdapter();
        binding.rvProducts.setLayoutManager(new LinearLayoutManager(this));
        binding.rvProducts.setAdapter(adapter);
    }

    private static class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {
        private List<Product> products = new ArrayList<>();

        void setProducts(List<Product> products) {
            this.products = products;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Product product = products.get(position);
            holder.text1.setText(product.productName);
            holder.text2.setText("ID: " + product.productId + " | Brand: " + product.brand);
        }

        @Override
        public int getItemCount() {
            return products.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView text1, text2;
            ViewHolder(View itemView) {
                super(itemView);
                text1 = itemView.findViewById(android.R.id.text1);
                text2 = itemView.findViewById(android.R.id.text2);
            }
        }
    }
}