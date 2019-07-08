package org.tensorflow.lite.examples.detection.SQLHandler;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class KeyChainHash {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int hashId;

    @NonNull
    @ColumnInfo(name = "k5")
    private String k5;

    @NonNull
    @ColumnInfo(name = "k0")
    private String k0;

    @NonNull
    @ColumnInfo(name = "hashtype")
    private String hashType;

    public KeyChainHash(@NonNull String k5, @NonNull String k0, @NonNull String hashType) {
        this.k5 = k5;
        this.k0 = k0;
        this.hashType = hashType;
    }

    public int getHashId() {
        return hashId;
    }

    @NonNull
    public String getK5() {
        return k5;
    }

    public void setK5(@NonNull String k5) {
        this.k5 = k5;
    }

    @NonNull
    public String getK0() {
        return k0;
    }

    public void setK0(@NonNull String k0) {
        this.k0 = k0;
    }

    @NonNull
    public String getHashType() {
        return hashType;
    }

    public void setHashType(@NonNull String hashType) {
        this.hashType = hashType;
    }

    public void setHashId(int hashId) {
        this.hashId = hashId;
    }
}
