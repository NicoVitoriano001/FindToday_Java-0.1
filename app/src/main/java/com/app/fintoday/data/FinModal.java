package com.app.fintoday.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.Locale;

@Entity(tableName = "fin_table")

public class FinModal implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String valorDesp, tipoDesp, fontDesp, despDescr, dataDesp;
    private long lastUpdated;

    // Construtor vazio necessário para Firebase
    public FinModal() {
        this.lastUpdated = System.currentTimeMillis();
    }
    //construtor
    public FinModal(String valorDesp, String tipoDesp, String fontDesp,  String despDescr, String dataDesp) {
        this.valorDesp = valorDesp;
        this.tipoDesp = tipoDesp;
        this.fontDesp = fontDesp;
        this.despDescr = despDescr;
        this.dataDesp = dataDesp;
        this.lastUpdated = System.currentTimeMillis();  // 16.05.25 contexto Firebase. Define o timestamp atual
    }

    //on below line we are creating getter and setter methods.
    public int getId() {return id;}
    public void setId(int id) { this.id = id; }
    public String getValorDesp() {return valorDesp; }
    public void setValorDesp(String valorDesp) {
        this.valorDesp = valorDesp;
    }
    public String getTipoDesp() {
        return tipoDesp;
    }
    public void setTipoDesp(String tipoDesp) {
        this.tipoDesp = tipoDesp;
    }
    public String getFontDesp() {
        return fontDesp;
    }
    public void setFontDesp(String fontDesp) {
        this.fontDesp = fontDesp;
    }
    public String getDespDescr() { return despDescr; }
    public void setDespDescr(String despDescr) { this.despDescr = despDescr; }
    public String getDataDesp() { return dataDesp;  }
    public void setDataDesp(String dataDesp) {this.dataDesp = dataDesp; }
    public long getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(long lastUpdated) { this.lastUpdated = lastUpdated; }

    protected FinModal(Parcel in) {
        id = in.readInt();
        valorDesp = in.readString();
        tipoDesp = in.readString();
        fontDesp = in.readString();
        despDescr = in.readString();
        dataDesp = in.readString();
        lastUpdated = in.readLong();
    }

    public static final Creator<FinModal> CREATOR = new Creator<FinModal>() {
        @Override
        public FinModal createFromParcel(Parcel in) {
            return new FinModal(in);
        }

        @Override
        public FinModal[] newArray(int size) {
            return new FinModal[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(valorDesp);
        dest.writeString(tipoDesp);
        dest.writeString(fontDesp);
        dest.writeString(despDescr);
        dest.writeString(dataDesp);
        dest.writeLong(lastUpdated);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "Data: %s\nDescrição: %s\nValor: %s\nTipo: %s\nFonte: %s",
                dataDesp, despDescr, valorDesp, tipoDesp, fontDesp);
    }

}
