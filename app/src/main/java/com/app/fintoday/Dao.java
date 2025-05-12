package com.app.fintoday;

import androidx.lifecycle.LiveData;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;
import androidx.room.RawQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

@androidx.room.Dao

public interface Dao {

    @RawQuery(observedEntities = FinModal.class)
  LiveData<List<FinModal>> buscaDesp(SupportSQLiteQuery query);

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
// dataDesp não está formato ISO 8601 (YYYY-MM-DD), SUBSTR(dataDesp, INSTR(dataDesp, ' ') + 1): Esta parte da consulta extrai a substring depois do espaço


// Adiciona % inicio e final da string para usar no LIKE - 29.04.2025
    @Query("SELECT * FROM fin_table WHERE " +
            "REPLACE(despDescr, ' ', '%') LIKE '%' || REPLACE(:despDescr, ' ', '%') || '%' " + // Adicionado '%' no início e no final
            "AND valorDesp LIKE '%' || :valorDesp || '%' " +
            "AND tipoDesp LIKE '%' || :tipoDesp || '%' " +
            "AND fontDesp LIKE '%' || :fontDesp || '%' " +
            "AND dataDesp LIKE '%' || :dataDesp || '%' " +
            "ORDER BY SUBSTR(dataDesp, INSTR(dataDesp, ' ') + 1) DESC")
    LiveData<List<FinModal>> buscaDesp(
            String valorDesp,
            String tipoDesp,
            String fontDesp,
            String despDescr,
            String dataDesp);

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


