package com.maenterprise.erp;

import android.content.Intent;
import android.content.res.ColorStateList;
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
import com.maenterprise.erp.databinding.ActivityCustomerBinding;
import com.maenterprise.erp.databinding.ItemCustomerBinding;
import com.maenterprise.erp.models.Customer;
import com.maenterprise.erp.utils.IdGenerator;
import java.util.ArrayList;
import java.util.List;

public class CustomerActivity extends AppCompatActivity {
    private ActivityCustomerBinding binding;
    private ErpRepository repository;
    private CustomerAdapter adapter;
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
        binding = ActivityCustomerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = new ErpRepository(getApplication());
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        setupRecyclerView();
        
        repository.getActiveCustomers().observe(this, customers -> {
            adapter.setCustomers(customers);
        });

        binding.fabAddCustomer.setOnClickListener(v -> showAddCustomerDialog(null));
    }

    private void showAddCustomerDialog(Customer customer) {
        selectedImageUri = null;
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_customer, null);
        EditText etName = dialogView.findViewById(R.id.etCustomerName);
        EditText etType = dialogView.findViewById(R.id.etCustomerType);
        EditText etLimit = dialogView.findViewById(R.id.etCreditLimit);
        dialogImageView = dialogView.findViewById(R.id.ivCustomerImage);
        View btnSelect = dialogView.findViewById(R.id.btnSelectImage);

        if (customer != null) {
            etName.setText(customer.customerName);
            etType.setText(customer.customerType);
            etLimit.setText(String.valueOf(customer.creditLimit));
            if (customer.imageUrl != null) {
                try {
                    selectedImageUri = Uri.parse(customer.imageUrl);
                    dialogImageView.setImageURI(selectedImageUri);
                    dialogImageView.setPadding(0, 0, 0, 0);
                } catch (Exception e) {
                    // Ignore malformed URIs
                }
            }
        }

        btnSelect.setOnClickListener(v -> {
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });

        new AlertDialog.Builder(this)
                .setTitle(customer == null ? "Add New Customer" : "Edit Customer")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Customer c = (customer == null) ? new Customer() : customer;
                    if (c.customerId == null) {
                        c.customerId = IdGenerator.generateCustomerId();
                    }
                    c.customerName = name;
                    c.customerType = etType.getText().toString();
                    c.imageUrl = selectedImageUri != null ? selectedImageUri.toString() : (c.imageUrl);
                    c.isActive = true;
                    try {
                        c.creditLimit = Double.parseDouble(etLimit.getText().toString());
                    } catch (Exception e) { c.creditLimit = 0; }
                    
                    if (customer == null) {
                        repository.insertCustomer(c);
                        Toast.makeText(this, "Customer added successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        repository.updateCustomer(c);
                        Toast.makeText(this, "Customer updated", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setupRecyclerView() {
        adapter = new CustomerAdapter(this::showAddCustomerDialog);
        binding.rvCustomers.setLayoutManager(new LinearLayoutManager(this));
        binding.rvCustomers.setAdapter(adapter);
    }

    private static class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.ViewHolder> {
        private List<Customer> customers = new ArrayList<>();
        private final OnCustomerEdit listener;

        CustomerAdapter(OnCustomerEdit listener) {
            this.listener = listener;
        }

        void setCustomers(List<Customer> customers) {
            this.customers = customers;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemCustomerBinding binding = ItemCustomerBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new ViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Customer customer = customers.get(position);
            holder.binding.tvCustomerName.setText(customer.customerName);
            holder.binding.tvCustomerDetails.setText("Type: " + customer.customerType + " | ID: " + customer.customerId);

            if (customer.imageUrl != null) {
                try {
                    holder.binding.ivCustomerImage.setImageTintList(null);
                    holder.binding.ivCustomerImage.setPadding(0, 0, 0, 0);
                    holder.binding.ivCustomerImage.setImageURI(Uri.parse(customer.imageUrl));
                } catch (Exception e) {
                    showPlaceholder(holder);
                }
            } else {
                showPlaceholder(holder);
            }
            holder.itemView.setOnClickListener(v -> listener.onEdit(customer));
        }

        private void showPlaceholder(@NonNull ViewHolder holder) {
            holder.binding.ivCustomerImage.setImageResource(android.R.drawable.ic_menu_camera);
            holder.binding.ivCustomerImage.setImageTintList(ColorStateList.valueOf(0xFFCCCCCC));
            holder.binding.ivCustomerImage.setPadding(32, 32, 32, 32);
        }

        @Override
        public int getItemCount() {
            return customers.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ItemCustomerBinding binding;
            ViewHolder(ItemCustomerBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
        
        interface OnCustomerEdit {
            void onEdit(Customer customer);
        }
    }
}
