package com.app.fintoday.data;

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
    long insert(FinModal model); // Altere o retorno de void para long
    //@Insert //void insert(FinModal model);

    @Update
    void update(FinModal model);

    @Delete
    void delete(FinModal model);

    @Query("DELETE FROM fin_table")
    void deleteallDesp();

 //@Query("SELECT * FROM fin_table WHERE dataDesp LIKE '%2025%' ORDER BY " +
     @Query("SELECT * FROM fin_table WHERE dataDesp LIKE '%' || strftime('%Y-%m', 'now') || '%' ORDER BY " +
                "CASE SUBSTR(dataDesp, 1, 3) " +
                "   WHEN 'Sun' THEN 1 " +
                "   WHEN 'Mon' THEN 2 " +
                "   WHEN 'Tue' THEN 3 " +
                "   WHEN 'Wed' THEN 4 " +
                "   WHEN 'Thu' THEN 5 " +
                "   WHEN 'Fri' THEN 6 " +
                "   ELSE 7 " +
                "END ASC, " +
                "SUBSTR(dataDesp, 6) DESC, despDescr ASC")
        LiveData<List<FinModal>> getallDesp();
        // dataDesp não está formato ISO 8601 (YYYY-MM-DD), SUBSTR(dataDesp, INSTR(dataDesp, ' ') + 1): Esta parte da consulta extrai a substring depois do espaço

        // Adicionado NOT LIKE 22.05.2025
        // Adiciona % inicio e final da string para usar no LIKE 29.04.2025
    @Query("SELECT * FROM fin_table WHERE " +
            "(:despDescr = '' OR " +
            "(REPLACE(despDescr, ' ', '%') LIKE '%' || REPLACE(SUBSTR(:despDescr, 1, CASE WHEN INSTR(:despDescr, '-') > 0 THEN INSTR(:despDescr, '-')-1 ELSE LENGTH(:despDescr) END), ' ', '%') || '%' " +
            "AND (INSTR(:despDescr, '-') = 0 OR despDescr NOT LIKE '%' || SUBSTR(:despDescr, INSTR(:despDescr, '-')+1) || '%'))) " +
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

    //Listener no grafico data "qui. YYYY-MM-DD"
    @Query("SELECT * FROM fin_table WHERE tipoDesp = :tipo AND " +
            "SUBSTR(dataDesp, 6, 4) = :ano AND " +    // Extrai o ano (posições 6-9)
            "SUBSTR(dataDesp, 11, 2) = :mes " +       // Extrai o mês (posições 11-12)
            "ORDER BY dataDesp DESC")                  // Ordena pela data de forma decrescente
    LiveData<List<FinModal>> buscarPorTipoAnoMes(String tipo, String ano, String mes);

    @Query("SELECT * FROM fin_table")
    List<FinModal> getAllItemsSync(); //16.05.25 Firebase

    @Query("SELECT * FROM fin_table WHERE id = :id")
    FinModal getDespById(int id);

    @Query("SELECT * FROM fin_table WHERE lastUpdated > :lastSyncTime")
    List<FinModal> getModifiedItems(long lastSyncTime);

    default String[] processarDespDescr(String despDescr) {
        if (despDescr == null || despDescr.isEmpty()) {
            return new String[]{"%%", ""};
        }

        String[] parts = despDescr.split("-", 2);
        String part1 = parts[0].trim();
        String part2 = parts.length > 1 ? parts[1].trim() : "";

        return new String[]{
                "%" + part1.replace(" ", "%") + "%",
                part2.isEmpty() ? "" : "%" + part2 + "%"
        };
      }

}





