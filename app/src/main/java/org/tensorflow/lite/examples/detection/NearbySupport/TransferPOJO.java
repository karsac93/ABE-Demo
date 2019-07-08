package org.tensorflow.lite.examples.detection.NearbySupport;

import org.tensorflow.lite.examples.detection.SQLHandler.Msg;

import java.io.Serializable;
import java.util.List;

public class TransferPOJO implements Serializable {
    List<Msg> msg_list;
    List<String> cipher_list;

    public TransferPOJO(List<Msg> msg_list, List<String> cipher_list) {
        this.msg_list = msg_list;
        this.cipher_list = cipher_list;
    }

}
