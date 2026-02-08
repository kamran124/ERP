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
import com.maenterprise.erp.databinding.ActivitySupplierBinding;
import com.maenterprise.erp.models.Supplier;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SupplierActivity extends AppCompatActivity {
    private ActivitySupplierBinding binding;
    private ErpRepository repository;
    private SupplierAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySupplierBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = new ErpRepository(getApplication());
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        setupRecyclerView();
        
        repository.getActiveSuppliers().observe(this, suppliers -> {
            adapter.setSuppliers(suppliers);
        });

        binding.fabAddSupplier.setOnClickListener(v -> showAddSupplierDialog());
    }

    private void showAddSupplierDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_supplier, null);
        EditText etName = dialogView.findViewById(R.id.etSupplierName);
        EditText etContact = dialogView.findViewById(R.id.etContactDetails);

        new AlertDialog.Builder(this)
                .setTitle("Add New Supplier")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Supplier s = new Supplier();
                    s.supplierId = "SUP-" + UUID.randomUUID().toString().substring(0, 8);
                    s.supplierName = name;
                    s.contactDetails = etContact.getText().toString();
                    s.isActive = true;
                    
                    repository.insertSupplier(s);
                    Toast.makeText(this, "Supplier added locally", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setupRecyclerView() {
        adapter = new SupplierAdapter();
        binding.rvSuppliers.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSuppliers.setAdapter(adapter);
    }

    private static class SupplierAdapter extends RecyclerView.Adapter<SupplierAdapter.ViewHolder> {
        private List<Supplier> suppliers = new ArrayList<>();

        void setSuppliers(List<Supplier> suppliers) {
            this.suppliers = suppliers;
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
            Supplier supplier = suppliers.get(position);
            holder.text1.setText(supplier.supplierName);
            holder.text2.setText("ID: " + supplier.supplierId + " | Contact: " + supplier.contactDetails);
        }

        @Override
        public int getItemCount() {
            return suppliers.size();
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