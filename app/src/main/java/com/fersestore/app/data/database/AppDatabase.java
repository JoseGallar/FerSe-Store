package com.fersestore.app.data.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.fersestore.app.data.dao.DebtDao; // NUEVO
import com.fersestore.app.data.dao.ProductDao;
import com.fersestore.app.data.dao.TransactionDao;
import com.fersestore.app.data.entity.DebtEntity; // NUEVO
import com.fersestore.app.data.entity.ProductEntity;
import com.fersestore.app.data.entity.ProductVariantEntity;
import com.fersestore.app.data.entity.TransactionEntity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// VERSIÓN 5 | Agregamos DebtEntity
@Database(entities = {ProductEntity.class, TransactionEntity.class, ProductVariantEntity.class, DebtEntity.class}, version = 5, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract ProductDao productDao();
    public abstract TransactionDao transactionDao();
    public abstract DebtDao debtDao(); // NUEVO

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "fersestore_db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}