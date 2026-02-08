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
import com.maenterprise.erp.databinding.ActivityCustomerBinding;
import com.maenterprise.erp.models.Customer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CustomerActivity extends AppCompatActivity {
    private ActivityCustomerBinding binding;
    private ErpRepository repository;
    private CustomerAdapter adapter;

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

        binding.fabAddCustomer.setOnClickListener(v -> showAddCustomerDialog());
    }

    private void showAddCustomerDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_customer, null);
        EditText etName = dialogView.findViewById(R.id.etCustomerName);
        EditText etType = dialogView.findViewById(R.id.etCustomerType);
        EditText etLimit = dialogView.findViewById(R.id.etCreditLimit);

        new AlertDialog.Builder(this)
                .setTitle("Add New Customer")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Customer c = new Customer();
                    c.customerId = "CUST-" + UUID.randomUUID().toString().substring(0, 8);
                    c.customerName = name;
                    c.customerType = etType.getText().toString();
                    c.isActive = true;
                    try {
                        c.creditLimit = Double.parseDouble(etLimit.getText().toString());
                    } catch (Exception e) { c.creditLimit = 0; }
                    
                    repository.insertCustomer(c);
                    Toast.makeText(this, "Customer added locally", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setupRecyclerView() {
        adapter = new CustomerAdapter();
        binding.rvCustomers.setLayoutManager(new LinearLayoutManager(this));
        binding.rvCustomers.setAdapter(adapter);
    }

    private static class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.ViewHolder> {
        private List<Customer> customers = new ArrayList<>();

        void setCustomers(List<Customer> customers) {
            this.customers = customers;
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
            Customer customer = customers.get(position);
            holder.text1.setText(customer.customerName);
            holder.text2.setText("ID: " + customer.customerId + " | Type: " + customer.customerType);
        }

        @Override
        public int getItemCount() {
            return customers.size();
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