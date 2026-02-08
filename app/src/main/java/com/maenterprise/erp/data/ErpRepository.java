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

    public LiveData<List<ProductInventory>> getProductInventory() {
        return erpDao.getProductInventory();
    }

    public LiveData<List<Customer>> getActiveCustomers() {
        return erpDao.getActiveCustomers();
    }

    public LiveData<List<Supplier>> getActiveSuppliers() {
        return erpDao.getActiveSuppliers();
    }

    public void insertProduct(Product product) {
        executorService.execute(() -> erpDao.insertProduct(product));
    }

    public void updateProduct(Product product) {
        executorService.execute(() -> erpDao.updateProduct(product));
    }

    public void deleteProduct(Product product) {
        executorService.execute(() -> erpDao.deleteProduct(product));
    }

    public void insertCustomer(Customer customer) {
        executorService.execute(() -> erpDao.insertCustomer(customer));
    }

    public void updateCustomer(Customer customer) {
        executorService.execute(() -> erpDao.updateCustomer(customer));
    }

    public void insertSupplier(Supplier supplier) {
        executorService.execute(() -> erpDao.insertSupplier(supplier));
    }

    public void insertFullPurchase(PurchaseTransaction transaction, List<PurchaseBatch> batches) {
        executorService.execute(() -> erpDao.insertFullPurchase(transaction, batches));
    }

    public LiveData<Double> getTotalSalesSince(String startDate) {
        return erpDao.getTotalSalesSince(startDate);
    }

    public LiveData<List<SalesTransaction>> getAllSales() {
        return erpDao.getAllSales();
    }

    public LiveData<List<SalesTransaction>> getLatestSales() {
        return erpDao.getAllSales();
    }

    public LiveData<List<PurchaseTransaction>> getLatestPurchases() {
        return erpDao.getAllPurchases();
    }

    public void insertPayment(Payment payment) {
        executorService.execute(() -> erpDao.insertPayment(payment));
    }

    public void processFullSale(SalesTransaction transaction, List<SaleItem> items, SaleCallback callback) {
        executorService.execute(() -> {
            try {
                List<SellBatchAllocation> allAllocations = new ArrayList<>();
                
                for (SaleItem item : items) {
                    List<PurchaseBatch> batches = erpDao.getAvailableBatchesForProduct(item.productId);
                    double needed = item.quantitySku;
                    double available = 0;
                    for (PurchaseBatch b : batches) available += b.remainingQuantity;

                    if (available < needed) {
                        callback.onError("Insufficient stock for product " + item.productId + ". Available: " + available);
                        return;
                    }

                    for (PurchaseBatch batch : batches) {
                        if (needed <= 0) break;

                        double take = Math.min(batch.remainingQuantity, needed);
                        
                        SellBatchAllocation allocation = new SellBatchAllocation();
                        allocation.allocationId = "ALOC-" + System.currentTimeMillis() + "-" + batch.batchId;
                        allocation.saleId = transaction.saleId;
                        allocation.productId = item.productId;
                        allocation.sellProductId = item.saleItemId;
                        allocation.batchId = batch.batchId;
                        allocation.allocatedQuantity = take;
                        allocation.purchasePricePerSku = batch.purchasePricePerSku;
                        
                        allAllocations.add(allocation);
                        
                        batch.remainingQuantity -= take;
                        erpDao.updatePurchaseBatch(batch);
                        needed -= take;
                    }
                }

                erpDao.insertFullSale(transaction, items);
                erpDao.insertBatchAllocations(allAllocations);
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
