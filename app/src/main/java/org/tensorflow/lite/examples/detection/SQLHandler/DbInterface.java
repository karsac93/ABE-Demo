package org.tensorflow.lite.examples.detection.SQLHandler;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.lang.annotation.Documented;
import java.util.List;

@Dao
public interface DbInterface {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMsg(Msg msg);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertHash(KeyChainHash hash);

    @Query("select * from msg where type=:type")
    List<Msg> getMsgs(String type);

    @Query("select * from msg where id<>:id and type<>:type and cph NOT NULL")
    List<Msg> getMsgsForDevice(String id, String type);

    @Query("select * from keychainhash where hashtype=:hashtype")
    List<KeyChainHash> getChainHash(String hashtype);

    @Query("select k5 from keychainhash where k0=:lasthash")
    String getk5(String lasthash);

    @Query("select * from keychainhash")
    List<KeyChainHash> getAllkeychain();

    @Delete()
    void deleteMsg(Msg msg);

}
