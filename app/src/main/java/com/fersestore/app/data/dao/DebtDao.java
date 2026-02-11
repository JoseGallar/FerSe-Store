package com.fersestore.app.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.fersestore.app.data.entity.DebtEntity;

import java.util.List;

@Dao
public interface DebtDao {
    @Insert
    void insert(DebtEntity debt);

    @Update
    void update(DebtEntity debt);

    @Delete
    void delete(DebtEntity debt);

    @Query("SELECT * FROM debts ORDER BY timestamp DESC")
    LiveData<List<DebtEntity>> getAllDebts();

    // Esto sumará automáticamente el total de lo que te deben para la tarjeta roja
    @Query("SELECT SUM(remainingAmount) FROM debts")
    LiveData<Double> getTotalDebt();
}