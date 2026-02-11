package com.fersestore.app.ui.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.fersestore.app.data.entity.TransactionEntity;
import com.fersestore.app.data.repository.TransactionRepository;

import java.util.List;

public class TransactionViewModel extends AndroidViewModel {

    private TransactionRepository repository;
    private LiveData<List<TransactionEntity>> allTransactions;

    public TransactionViewModel(@NonNull Application application) {
        super(application);
        repository = new TransactionRepository(application);
        allTransactions = repository.getAllTransactions();
    }

    public void insert(TransactionEntity transaction) {
        repository.insert(transaction);
    }

    // --- ESTE ES EL MÉTODO PARA BORRAR (Solo debe estar una vez) ---
    public void delete(TransactionEntity transaction) {
        repository.delete(transaction);
    }
    // -------------------------------------------------------------

    public LiveData<List<TransactionEntity>> getHistory() {
        return allTransactions;
    }

    public LiveData<Double> getTotalIncome() {
        return repository.getTotalIncome();
    }

    public LiveData<Double> getTotalExpenses() {
        return repository.getTotalExpenses();
    }
}