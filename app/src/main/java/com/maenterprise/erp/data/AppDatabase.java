package com.maenterprise.erp.data;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.maenterprise.erp.models.*;

@Database(entities = {
        Product.class,
        Customer.class,
        Supplier.class,
        PurchaseTransaction.class,
        PurchaseBatch.class,
        SalesTransaction.class,
        SaleItem.class,
        SellBatchAllocation.class,
        Payment.class
}, version = 4)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ErpDao erpDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "erp_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
