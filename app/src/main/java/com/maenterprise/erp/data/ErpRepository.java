package com.maenterprise.erp.data;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.maenterprise.erp.models.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ErpRepository {
    private final ErpDao erpDao;
    private final ExecutorService executorService;

    public ErpRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        erpDao = db.erpDao();
        executorService = Executors.newFixedThreadPool(4);
    }

    public LiveData<List<Product>> getActiveProducts() {
        return erpDao.getActiveProducts();
    }

    public LiveData<List<Customer>> getActiveCustomers() {
        return erpDao.getActiveCustomers();
    }

    public void insertPurchaseBatch(PurchaseBatch batch) {
        executorService.execute(() -> erpDao.insertPurchaseBatch(batch));
    }

    public void processSale(SalesTransaction transaction, SaleCallback callback) {
        executorService.execute(() -> {
            try {
                List<PurchaseBatch> batches = erpDao.getAvailableBatchesForProduct(transaction.productId);
                double needed = transaction.quantitySku;
                double available = 0;
                for (PurchaseBatch b : batches) available += b.remainingQuantity;

                if (available < needed) {
                    callback.onError("Insufficient stock. Available: " + available);
                    return;
                }

                List<SellBatchAllocation> allocations = new ArrayList<>();
                for (PurchaseBatch batch : batches) {
                    if (needed <= 0) break;

                    double take = Math.min(batch.remainingQuantity, needed);
                    
                    SellBatchAllocation allocation = new SellBatchAllocation();
                    allocation.allocationId = "ALOC-" + System.currentTimeMillis() + "-" + batch.batchId;
                    allocation.saleId = transaction.saleId;
                    allocation.productId = transaction.productId;
                    allocation.batchId = batch.batchId;
                    allocation.allocatedQuantity = take;
                    allocation.purchasePricePerSku = batch.purchasePricePerSku;
                    
                    allocations.add(allocation);
                    
                    batch.remainingQuantity -= take;
                    erpDao.updatePurchaseBatch(batch);
                    needed -= take;
                }

                erpDao.insertSalesTransaction(transaction);
                erpDao.insertBatchAllocations(allocations);
                callback.onSuccess();
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }

    public interface SaleCallback {
        void onSuccess();
        void onError(String message);
    }
}