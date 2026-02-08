package com.maenterprise.erp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.maenterprise.erp.data.ErpRepository;
import com.maenterprise.erp.databinding.ActivitySupplierBinding;
import com.maenterprise.erp.databinding.ItemSupplierBinding;
import com.maenterprise.erp.models.Supplier;
import com.maenterprise.erp.utils.IdGenerator;
import java.util.ArrayList;
import java.util.List;

public class SupplierActivity extends AppCompatActivity {
    private ActivitySupplierBinding binding;
    private ErpRepository repository;
    private SupplierAdapter adapter;
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
        selectedImageUri = null;
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_supplier, null);
        EditText etName = dialogView.findViewById(R.id.etSupplierName);
        EditText etContact = dialogView.findViewById(R.id.etContactDetails);
        dialogImageView = dialogView.findViewById(R.id.ivSupplierImage);
        View btnSelect = dialogView.findViewById(R.id.btnSelectImage);

        btnSelect.setOnClickListener(v -> {
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });

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
                    s.supplierId = IdGenerator.generateSupplierId();
                    s.supplierName = name;
                    s.contactDetails = etContact.getText().toString();
                    s.imageUrl = selectedImageUri != null ? selectedImageUri.toString() : null;
                    s.isActive = true;
                    
                    repository.insertSupplier(s);
                    Toast.makeText(this, "Supplier added successfully", Toast.LENGTH_SHORT).show();
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
            ItemSupplierBinding binding = ItemSupplierBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new ViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Supplier supplier = suppliers.get(position);
            holder.binding.tvSupplierName.setText(supplier.supplierName);
            holder.binding.tvSupplierDetails.setText("Contact: " + supplier.contactDetails + " | ID: " + supplier.supplierId);

            if (supplier.imageUrl != null) {
                try {
                    holder.binding.ivSupplierImage.setImageURI(Uri.parse(supplier.imageUrl));
                    holder.binding.ivSupplierImage.setPadding(0, 0, 0, 0);
                } catch (Exception e) {
                    holder.binding.ivSupplierImage.setImageResource(android.R.drawable.ic_menu_camera);
                }
            } else {
                holder.binding.ivSupplierImage.setImageResource(android.R.drawable.ic_menu_camera);
            }
        }

        @Override
        public int getItemCount() {
            return suppliers.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ItemSupplierBinding binding;
            ViewHolder(ItemSupplierBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}
