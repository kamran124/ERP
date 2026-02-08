package com.maenterprise.erp.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;
import com.maenterprise.erp.models.*;
import java.util.List;

@Dao
public interface ErpDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertProducts(List<Product> products);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertProduct(Product product);

    @Update
    void updateProduct(Product product);

    @Delete
    void deleteProduct(Product product);

    @Query("SELECT * FROM products WHERE isActive = 1")
    LiveData<List<Product>> getActiveProducts();

    @Query("SELECT p.*, COALESCE(SUM(pb.remainingQuantity), 0) as totalQuantity FROM products p " +
           "LEFT JOIN purchase_batches pb ON p.productId = pb.productId " +
           "WHERE p.isActive = 1 GROUP BY p.productId")
    LiveData<List<ProductInventory>> getProductInventory();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCustomers(List<Customer> customers);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCustomer(Customer customer);

    @Update
    void updateCustomer(Customer customer);

    @Query("SELECT * FROM customers WHERE isActive = 1")
    LiveData<List<Customer>> getActiveCustomers();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSuppliers(List<Supplier> suppliers);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSupplier(Supplier supplier);

    @Query("SELECT * FROM suppliers WHERE isActive = 1")
    LiveData<List<Supplier>> getActiveSuppliers();

    // Purchase
    @Insert
    void insertPurchaseTransaction(PurchaseTransaction transaction);

    @Insert
    void insertPurchaseBatches(List<PurchaseBatch> batches);

    @Transaction
    default void insertFullPurchase(PurchaseTransaction transaction, List<PurchaseBatch> batches) {
        insertPurchaseTransaction(transaction);
        insertPurchaseBatches(batches);
    }

    @Query("SELECT * FROM purchase_transactions ORDER BY purchaseDate DESC")
    LiveData<List<PurchaseTransaction>> getAllPurchases();

    @Query("SELECT * FROM purchase_batches WHERE productId = :productId AND remainingQuantity > 0 ORDER BY batchId ASC")
    List<PurchaseBatch> getAvailableBatchesForProduct(String productId);

    @Update
    void updatePurchaseBatch(PurchaseBatch batch);

    // Sales
    @Insert
    void insertSalesTransaction(SalesTransaction transaction);

    @Insert
    void insertSaleItems(List<SaleItem> items);

    @Transaction
    default void insertFullSale(SalesTransaction transaction, List<SaleItem> items) {
        insertSalesTransaction(transaction);
        insertSaleItems(items);
    }

    @Query("SELECT * FROM sales_transactions ORDER BY saleDate DESC")
    LiveData<List<SalesTransaction>> getAllSales();

    @Insert
    void insertBatchAllocations(List<SellBatchAllocation> allocations);

    @Insert
    void insertPayment(Payment payment);
    
    @Query("SELECT * FROM purchase_batches WHERE isSynced = 0")
    List<PurchaseBatch> getUnsyncedBatches();
    
    @Query("SELECT * FROM sales_transactions WHERE isSynced = 0")
    List<SalesTransaction> getUnsyncedSales();

    @Query("SELECT * FROM sale_items WHERE isSynced = 0")
    List<SaleItem> getUnsyncedSaleItems();

    @Query("SELECT * FROM sell_batch_allocations WHERE isSynced = 0")
    List<SellBatchAllocation> getUnsyncedAllocations();

    @Query("SELECT * FROM payments WHERE isSynced = 0")
    List<Payment> getUnsyncedPayments();

    @Update
    void updatePurchaseBatches(List<PurchaseBatch> batches);

    @Update
    void updateSalesTransactions(List<SalesTransaction> transactions);

    @Update
    void updateSaleItems(List<SaleItem> items);

    @Update
    void updateSellBatchAllocations(List<SellBatchAllocation> allocations);

    @Update
    void updatePayments(List<Payment> payments);

    @Query("SELECT SUM(si.quantitySku * si.sellingPricePerSku) FROM sales_transactions st " +
           "JOIN sale_items si ON st.saleId = si.saleId " +
           "WHERE st.saleDate >= :startDate")
    LiveData<Double> getTotalSalesSince(String startDate);
}
