package com.fersestore.app.ui.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.fersestore.app.data.database.AppDatabase;
import com.fersestore.app.data.dao.DebtDao;
import com.fersestore.app.data.entity.DebtEntity;

import java.util.List;

public class DebtViewModel extends AndroidViewModel {
    private final DebtDao debtDao;
    private final LiveData<List<DebtEntity>> allDebts;
    private final LiveData<Double> totalDebt;

    public DebtViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        debtDao = db.debtDao();
        allDebts = debtDao.getAllDebts();
        totalDebt = debtDao.getTotalDebt();
    }

    public LiveData<List<DebtEntity>> getAllDebts() { return allDebts; }
    public LiveData<Double> getTotalDebt() { return totalDebt; }

    public void insert(DebtEntity debt) {
        AppDatabase.databaseWriteExecutor.execute(() -> debtDao.insert(debt));
    }

    public void update(DebtEntity debt) {
        AppDatabase.databaseWriteExecutor.execute(() -> debtDao.update(debt));
    }

    public void delete(DebtEntity debt) {
        AppDatabase.databaseWriteExecutor.execute(() -> debtDao.delete(debt));
    }
}