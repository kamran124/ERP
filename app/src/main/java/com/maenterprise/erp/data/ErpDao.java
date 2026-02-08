package com.maenterprise.erp.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.maenterprise.erp.models.*;
import java.util.List;

@Dao
public interface ErpDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertProducts(List<Product> products);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertProduct(Product product);

    @Query("SELECT * FROM products WHERE isActive = 1")
    LiveData<List<Product>> getActiveProducts();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCustomers(List<Customer> customers);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCustomer(Customer customer);

    @Query("SELECT * FROM customers WHERE isActive = 1")
    LiveData<List<Customer>> getActiveCustomers();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSuppliers(List<Supplier> suppliers);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSupplier(Supplier supplier);

    @Query("SELECT * FROM suppliers WHERE isActive = 1")
    LiveData<List<Supplier>> getActiveSuppliers();

    @Insert
    void insertPurchaseBatch(PurchaseBatch batch);

    @Query("SELECT * FROM purchase_batches WHERE productId = :productId AND remainingQuantity > 0 ORDER BY purchaseDate ASC")
    List<PurchaseBatch> getAvailableBatchesForProduct(String productId);

    @Update
    void updatePurchaseBatch(PurchaseBatch batch);

    @Insert
    void insertSalesTransaction(SalesTransaction transaction);

    @Insert
    void insertBatchAllocations(List<SellBatchAllocation> allocations);

    @Insert
    void insertPayment(Payment payment);
    
    @Query("SELECT * FROM purchase_batches WHERE isSynced = 0")
    List<PurchaseBatch> getUnsyncedBatches();
    
    @Query("SELECT * FROM sales_transactions WHERE isSynced = 0")
    List<SalesTransaction> getUnsyncedSales();

    @Query("SELECT * FROM sell_batch_allocations WHERE isSynced = 0")
    List<SellBatchAllocation> getUnsyncedAllocations();

    @Query("SELECT * FROM payments WHERE isSynced = 0")
    List<Payment> getUnsyncedPayments();

    @Update
    void updatePurchaseBatches(List<PurchaseBatch> batches);

    @Update
    void updateSalesTransactions(List<SalesTransaction> transactions);

    @Update
    void updateSellBatchAllocations(List<SellBatchAllocation> allocations);

    @Update
    void updatePayments(List<Payment> payments);
}