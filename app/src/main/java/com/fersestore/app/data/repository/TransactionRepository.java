package com.fersestore.app.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;

import com.fersestore.app.data.database.AppDatabase;
import com.fersestore.app.data.dao.TransactionDao;
import com.fersestore.app.data.entity.TransactionEntity;

import java.util.List;

public class TransactionRepository {

    private TransactionDao transactionDao;
    private LiveData<List<TransactionEntity>> allTransactions;

    public TransactionRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        transactionDao = db.transactionDao();
        allTransactions = transactionDao.getAllTransactions();
    }

    public LiveData<List<TransactionEntity>> getAllTransactions() {
        return allTransactions;
    }

    public LiveData<Double> getTotalIncome() {
        return transactionDao.getTotalIncome();
    }

    public LiveData<Double> getTotalExpenses() {
        return transactionDao.getTotalExpenses();
    }

    public void insert(TransactionEntity transaction) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            transactionDao.insert(transaction);
        });
    }

    // ESTO FALTABA: La función para borrar
    public void delete(TransactionEntity transaction) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            transactionDao.delete(transaction);
        });
    }
}