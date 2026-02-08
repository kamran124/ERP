package com.maenterprise.erp.sync;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.maenterprise.erp.data.AppDatabase;
import com.maenterprise.erp.data.ErpDao;
import com.maenterprise.erp.models.*;
import com.maenterprise.erp.sheets.GoogleSheetsHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SyncWorker extends Worker {
    private static final String TAG = "SyncWorker";
    private final ErpDao erpDao;
    private GoogleSheetsHelper sheetsHelper;
    
    // IMPORTANT: Replace this with your actual Spreadsheet ID
    private static final String SPREADSHEET_ID = "YOUR_SPREADSHEET_ID"; 

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        erpDao = AppDatabase.getDatabase(context).erpDao();
    }

    @NonNull
    @Override
    public Result doWork() {
        if (SPREADSHEET_ID.equals("YOUR_SPREADSHEET_ID")) {
            Log.e(TAG, "Sync aborted: Please set SPREADSHEET_ID in SyncWorker.java");
            return Result.failure();
        }

        try {
            Log.d(TAG, "Starting sync...");
            sheetsHelper = new GoogleSheetsHelper(getApplicationContext(), SPREADSHEET_ID);
            
            syncMasterData();
            syncPurchaseBatches();
            syncSalesTransactions();
            syncSaleItems();
            syncAllocations();
            syncPayments();

            Log.d(TAG, "Sync completed successfully");
            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "Sync failed: " + e.getMessage(), e);
            return Result.retry();
        }
    }

    private void syncMasterData() throws Exception {
        // Sync Products
        Log.d(TAG, "Fetching Products...");
        List<List<Object>> productData = sheetsHelper.getSheetData("Products!A2:E");
        if (productData != null) {
            List<Product> products = new ArrayList<>();
            for (List<Object> row : productData) {
                if (row.size() < 5) continue;
                Product p = new Product();
                p.productId = row.get(0).toString();
                p.productName = row.get(1).toString();
                p.brand = row.get(2).toString();
                p.packSize = row.get(3).toString();
                p.isActive = parseBoolean(row.get(4));
                products.add(p);
            }
            erpDao.insertProducts(products);
            Log.d(TAG, "Synced " + products.size() + " products");
        }

        // Sync Customers
        Log.d(TAG, "Fetching Customers...");
        List<List<Object>> customerData = sheetsHelper.getSheetData("Customers!A2:F");
        if (customerData != null) {
            List<Customer> customers = new ArrayList<>();
            for (List<Object> row : customerData) {
                if (row.size() < 6) continue;
                Customer c = new Customer();
                c.customerId = row.get(0).toString();
                c.customerName = row.get(1).toString();
                c.customerType = row.get(2).toString();
                c.creditLimit = parseDouble(row.get(3));
                c.paymentTermsDays = parseInt(row.get(4));
                c.isActive = parseBoolean(row.get(5));
                customers.add(c);
            }
            erpDao.insertCustomers(customers);
            Log.d(TAG, "Synced " + customers.size() + " customers");
        }

        // Sync Suppliers
        Log.d(TAG, "Fetching Suppliers...");
        List<List<Object>> supplierData = sheetsHelper.getSheetData("Suppliers!A2:E");
        if (supplierData != null) {
            List<Supplier> suppliers = new ArrayList<>();
            for (List<Object> row : supplierData) {
                if (row.size() < 5) continue;
                Supplier s = new Supplier();
                s.supplierId = row.get(0).toString();
                s.supplierName = row.get(1).toString();
                s.contactDetails = row.get(2).toString();
                s.paymentTermsDays = parseInt(row.get(3));
                s.isActive = parseBoolean(row.get(4));
                suppliers.add(s);
            }
            erpDao.insertSuppliers(suppliers);
            Log.d(TAG, "Synced " + suppliers.size() + " suppliers");
        }
    }

    // Helper methods for safe parsing
    private boolean parseBoolean(Object val) {
        if (val == null) return false;
        String s = val.toString().trim().toLowerCase();
        return s.equals("true") || s.equals("1") || s.equals("yes");
    }

    private double parseDouble(Object val) {
        try { return Double.parseDouble(val.toString()); } catch (Exception e) { return 0.0; }
    }

    private int parseInt(Object val) {
        try { return Integer.parseInt(val.toString()); } catch (Exception e) { return 0; }
    }

    private void syncPurchaseBatches() throws Exception {
        List<PurchaseBatch> unsynced = erpDao.getUnsyncedBatches();
        for (PurchaseBatch batch : unsynced) {
            List<Object> row = Arrays.asList(
                batch.batchId, batch.purchaseId, batch.purchaseDate, batch.productId, 
                batch.purchasePricePerSku, batch.batchQuantity, 
                batch.remainingQuantity, batch.supplierId, batch.remarks
            );
            sheetsHelper.appendRow("Purchase_Batches", row);
            batch.isSynced = true;
        }
        erpDao.updatePurchaseBatches(unsynced);
    }

    private void syncSalesTransactions() throws Exception {
        List<SalesTransaction> unsynced = erpDao.getUnsyncedSales();
        for (SalesTransaction sale : unsynced) {
            List<Object> row = Arrays.asList(
                sale.saleId, sale.saleDate, sale.customerId,
                sale.salesChannel, sale.invoiceNumber, sale.paymentStatus, sale.remarks
            );
            sheetsHelper.appendRow("Sales_Transactions", row);
            sale.isSynced = true;
        }
        erpDao.updateSalesTransactions(unsynced);
    }

    private void syncSaleItems() throws Exception {
        List<SaleItem> unsynced = erpDao.getUnsyncedSaleItems();
        for (SaleItem item : unsynced) {
            List<Object> row = Arrays.asList(
                item.saleItemId, item.saleId, item.productId,
                item.quantitySku, item.sellingPricePerSku, item.schemeGlassPerSku, item.remarks
            );
            sheetsHelper.appendRow("Sale_Items", row);
            item.isSynced = true;
        }
        erpDao.updateSaleItems(unsynced);
    }

    private void syncAllocations() throws Exception {
        List<SellBatchAllocation> unsynced = erpDao.getUnsyncedAllocations();
        for (SellBatchAllocation alloc : unsynced) {
            List<Object> row = Arrays.asList(
                alloc.allocationId, alloc.saleId, alloc.productId, alloc.sellProductId,
                alloc.batchId, alloc.allocatedQuantity, alloc.purchasePricePerSku
            );
            sheetsHelper.appendRow("Sell_Batch_Allocation", row);
            alloc.isSynced = true;
        }
        erpDao.updateSellBatchAllocations(unsynced);
    }

    private void syncPayments() throws Exception {
        List<Payment> unsynced = erpDao.getUnsyncedPayments();
        for (Payment payment : unsynced) {
            List<Object> row = Arrays.asList(
                payment.paymentId, payment.saleId, payment.paymentDate,
                payment.amountReceived, payment.paymentMode, payment.remarks
            );
            sheetsHelper.appendRow("Payments", row);
            payment.isSynced = true;
        }
        erpDao.updatePayments(unsynced);
    }
}
