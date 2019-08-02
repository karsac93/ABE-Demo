package org.tensorflow.lite.examples.detection.SQLHandler;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.io.Serializable;

@Entity
public class Msg implements Serializable {
    @PrimaryKey
    @ColumnInfo(name="id")
    @NonNull
    private String id;

    @NonNull
    @ColumnInfo(name="source")
    private String source;

    @ColumnInfo(name="path")
    private String path;

    @NonNull
    @ColumnInfo(name="type")
    private String type;

    @NonNull
    @ColumnInfo(name="filename")
    private String fileName;

    @ColumnInfo(name="cph")
    private String cipher;

    @ColumnInfo(name="policy")
    private String policy;

    @ColumnInfo(name = "hashinfo")
    private String hashInfo;

    @ColumnInfo(name = "num_nodes")
    private int num_nodes_travelled;

    @ColumnInfo(name = "first_hash")
    private String first_hash;

    @ColumnInfo(name = "enc_hash")
    private String encrypted_hash;

    public Msg(@NonNull String id, @NonNull String source, String path, @NonNull String type,
               @NonNull String fileName, String cipher, String policy, String hashInfo,
               int num_nodes_travelled, String nextHash, String connectedDevices, boolean isverified,
               String first_hash, String encrypted_hash) {
        this.id = id;
        this.source = source;
        this.path = path;
        this.type = type;
        this.fileName = fileName;
        this.cipher = cipher;
        this.policy = policy;
        this.hashInfo = hashInfo;
        this.num_nodes_travelled = num_nodes_travelled;
        this.nextHash = nextHash;
        this.connectedDevices = connectedDevices;
        this.isverified = isverified;
        this.first_hash = first_hash;
        this.encrypted_hash = encrypted_hash;
    }

    @ColumnInfo(name= "nextHash")
    private String nextHash;

    @ColumnInfo(name="connectedDevices")
    private String connectedDevices;

    @ColumnInfo(name = "verified")
    private boolean isverified;

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    @NonNull
    public String getSource() {
        return source;
    }

    public void setSource(@NonNull String source) {
        this.source = source;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @NonNull
    public String getType() {
        return type;
    }

    public void setType(@NonNull String type) {
        this.type = type;
    }

    @NonNull
    public String getFileName() {
        return fileName;
    }

    public void setFileName(@NonNull String fileName) {
        this.fileName = fileName;
    }

    public String getCipher() {
        return cipher;
    }

    public void setCipher(String cipher) {
        this.cipher = cipher;
    }

    public String getPolicy() {
        return policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }

    public String getHashInfo() {
        return hashInfo;
    }

    public void setHashInfo(String hashInfo) {
        this.hashInfo = hashInfo;
    }

    public int getNum_nodes_travelled() {
        return num_nodes_travelled;
    }

    public void setNum_nodes_travelled(int num_nodes_travelled) {
        this.num_nodes_travelled = num_nodes_travelled;
    }

    public String getNextHash() {
        return nextHash;
    }

    public void setNextHash(String nextHash) {
        this.nextHash = nextHash;
    }

    public String getConnectedDevices() {
        return connectedDevices;
    }

    public void setConnectedDevices(String connectedDevices) {
        this.connectedDevices = connectedDevices;
    }

    public boolean isIsverified() {
        return isverified;
    }

    public void setIsverified(boolean isverified) {
        this.isverified = isverified;
    }

    public String getFirst_hash() {
        return first_hash;
    }

    public void setFirst_hash(String first_hash) {
        this.first_hash = first_hash;
    }

    public String getEncrypted_hash() {
        return encrypted_hash;
    }

    public void setEncrypted_hash(String encrypted_hash) {
        this.encrypted_hash = encrypted_hash;
    }
}
