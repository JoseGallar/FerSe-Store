package com.fersestore.app.data.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.fersestore.app.data.dao.ProductDao;
import com.fersestore.app.data.dao.TransactionDao;
import com.fersestore.app.data.entity.ProductEntity;
import com.fersestore.app.data.entity.TransactionEntity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {ProductEntity.class, TransactionEntity.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract ProductDao productDao();
    public abstract TransactionDao transactionDao();

    private static volatile AppDatabase INSTANCE;

    // ESTA ES LA PARTE QUE FALTABA Y DABA ERROR
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "fersestore_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}