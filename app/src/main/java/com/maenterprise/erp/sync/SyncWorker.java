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
    private final ErpDao erpDao;
    private GoogleSheetsHelper sheetsHelper;
    private static final String SPREADSHEET_ID = "YOUR_SPREADSHEET_ID"; // User must replace this

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        erpDao = AppDatabase.getDatabase(context).erpDao();
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            sheetsHelper = new GoogleSheetsHelper(getApplicationContext(), SPREADSHEET_ID);
            
            syncPurchaseBatches();
            syncSalesTransactions();
            syncAllocations();
            syncPayments();

            return Result.success();
        } catch (Exception e) {
            Log.e("SyncWorker", "Sync failed", e);
            return Result.retry();
        }
    }

    private void syncPurchaseBatches() throws Exception {
        List<PurchaseBatch> unsynced = erpDao.getUnsyncedBatches();
        for (PurchaseBatch batch : unsynced) {
            List<Object> row = Arrays.asList(
                batch.batchId, batch.purchaseDate, batch.productId, 
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
                sale.saleId, sale.saleDate, sale.customerId, sale.productId,
                sale.quantitySku, sale.sellingPricePerSku, sale.salesChannel,
                sale.invoiceNumber, sale.paymentStatus, sale.remarks
            );
            sheetsHelper.appendRow("Sales_Transactions", row);
            sale.isSynced = true;
        }
        erpDao.updateSalesTransactions(unsynced);
    }

    private void syncAllocations() throws Exception {
        List<SellBatchAllocation> unsynced = erpDao.getUnsyncedAllocations();
        for (SellBatchAllocation alloc : unsynced) {
            List<Object> row = Arrays.asList(
                alloc.allocationId, alloc.saleId, alloc.productId,
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