package com.maenterprise.erp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.tabs.TabLayout;
import com.maenterprise.erp.data.ErpRepository;
import com.maenterprise.erp.databinding.ActivityLatestTransactionsBinding;
import com.maenterprise.erp.databinding.ItemTransactionBinding;
import com.maenterprise.erp.models.PurchaseTransaction;
import com.maenterprise.erp.models.SalesTransaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LatestTransactionsActivity extends AppCompatActivity {
    private ActivityLatestTransactionsBinding binding;
    private ErpRepository repository;
    private TransactionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLatestTransactionsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        repository = new ErpRepository(getApplication());

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        setupRecyclerView();

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    loadSales();
                } else {
                    loadPurchases();
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) { }
            @Override public void onTabReselected(TabLayout.Tab tab) { }
        });

        loadSales();
    }

    private void setupRecyclerView() {
        adapter = new TransactionAdapter();
        binding.rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        binding.rvTransactions.setAdapter(adapter);
    }

    private void loadSales() {
        repository.getLatestSales().observe(this, sales -> {
            adapter.updateData(sales, null);
        });
    }

    private void loadPurchases() {
        repository.getLatestPurchases().observe(this, purchases -> {
            adapter.updateData(null, purchases);
        });
    }

    private static class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {
        private List<SalesTransaction> sales = new ArrayList<>();
        private List<PurchaseTransaction> purchases = new ArrayList<>();
        private boolean isSales = true;

        public void updateData(List<SalesTransaction> sales, List<PurchaseTransaction> purchases) {
            this.isSales = (sales != null);
            if(isSales) this.sales = sales;
            else this.purchases = purchases;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(ItemTransactionBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            if(isSales) {
                SalesTransaction sale = sales.get(position);
                holder.binding.tvDate.setText(sale.saleDate);
                holder.binding.tvId.setText(sale.saleId);
                holder.binding.tvName.setText("Customer: " + sale.customerId);
                holder.binding.tvProductDetails.setText("Multi-item Sale");
                holder.binding.tvAmount.setText(sale.paymentStatus);
            } else {
                PurchaseTransaction purchase = purchases.get(position);
                holder.binding.tvDate.setText(purchase.purchaseDate);
                holder.binding.tvId.setText(purchase.purchaseId);
                holder.binding.tvName.setText("Supplier: " + purchase.supplierId);
                holder.binding.tvProductDetails.setText("Invoice: " + purchase.invoiceNumber);
                holder.binding.tvAmount.setText("");
            }
        }

        @Override
        public int getItemCount() {
            return isSales ? sales.size() : purchases.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ItemTransactionBinding binding;
            ViewHolder(ItemTransactionBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}
