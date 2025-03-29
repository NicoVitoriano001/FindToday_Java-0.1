package com.gtappdevelopers.findtoday;

import androidx.lifecycle.LiveData;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@androidx.room.Dao

public interface Dao {
    @Insert
    void insert(FinModal model);

    @Update
    void update(FinModal model);

    @Delete
    void delete(FinModal model);

    @Query("DELETE FROM fin_table")
    void deleteallDesp();

    @Query("SELECT * FROM fin_table WHERE dataDesp LIKE '%2025%' ORDER BY " +
            "CASE SUBSTR(dataDesp, 1, 3) " +
            "   WHEN 'Sun' THEN 1 " +
            "   WHEN 'Mon' THEN 2 " +
            "   WHEN 'Tue' THEN 3 " +
            "   WHEN 'Wed' THEN 4 " +
            "   WHEN 'Thu' THEN 5 " +
            "   WHEN 'Fri' THEN 6 " +
            "   ELSE 7 " +
            "END ASC, " +
            "SUBSTR(dataDesp, 6) DESC, tipoDesp ASC")

    LiveData<List<FinModal>> getallDesp();

    @Query("SELECT * FROM fin_table WHERE valorDesp LIKE '%' || :valorDesp || '%' " +
            "AND tipoDesp LIKE '%' || :tipoDesp || '%' " +
            "AND fontDesp LIKE '%' || :fontDesp || '%' " +
            "AND despDescr LIKE '%' || :despDescr || '%' " +
            "AND dataDesp LIKE '%' || :dataDesp || '%' ORDER BY dataDesp DESC")

    LiveData<List<FinModal>> buscaDesp(String valorDesp, String tipoDesp, String fontDesp, String despDescr, String dataDesp);
    /*

//MODIFCADO EM 20.MAR.25
     */
    @Query("SELECT * FROM fin_table " +
            "WHERE SUBSTR(dataDesp, 6, 4) = :ano " +       // Ano (posições 6-9)
            "AND SUBSTR(dataDesp, 11, 2) = :mes " +        // Mês
            "ORDER BY " +
            "CASE SUBSTR(dataDesp, 1, 3) " +             // Ordena por dia da semana
            "   WHEN 'Sun' THEN 1 WHEN 'Mon' THEN 2 " +
            "   WHEN 'Tue' THEN 3 WHEN 'Wed' THEN 4 " +
            "   WHEN 'Thu' THEN 5 WHEN 'Fri' THEN 6 " +
            "   WHEN 'Dom.' THEN 1 WHEN 'Seg.' THEN 2 " +
            "   WHEN 'Ter.' THEN 3 WHEN 'Qua.' THEN 4 " +
            "   WHEN 'Qui.' THEN 5 WHEN 'Sex.' THEN 6 " +
            "   ELSE 7 END ASC, " +
            "SUBSTR(dataDesp, 6, 10) DESC")              // Ordena por data (YYYY-MM-DD)
    LiveData<List<FinModal>> buscaPorAnoEMes(String ano, String mes);
}
