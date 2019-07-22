package org.tensorflow.lite.examples.detection.NearbySupport;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.tasks.OnFailureListener;

import org.tensorflow.lite.examples.detection.Homescreen;
import org.tensorflow.lite.examples.detection.SDFileHandler;
import org.tensorflow.lite.examples.detection.SQLHandler.AppDatabase;
import org.tensorflow.lite.examples.detection.SQLHandler.KeyChainHash;
import org.tensorflow.lite.examples.detection.SQLHandler.Msg;
import org.tensorflow.lite.examples.detection.SetupActivity;
import org.tensorflow.lite.examples.detection.SharedPreferences.SharedPreferenceHandler;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;

import co.junwei.cpabe.Common;
import co.junwei.cpabe.Cpabe;

public class NearbyService extends Service {
    private static final String TAG = NearbyService.class.getCanonicalName();
    AppDatabase database;
    private ConnectionsClient connectionsClient;
    private static final Strategy STRATEGY = Strategy.P2P_POINT_TO_POINT;
    private String connectedFella;
    String id;
    private String lastConnected;
    boolean transmitted = false;
    private static final String ACK = "ack";
    Random rand;

    public NearbyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");
        database = AppDatabase.getAppDatabase(getApplicationContext());
        connectionsClient = Nearby.getConnectionsClient(this);
        rand = new Random();
        if (SharedPreferenceHandler.getStringValues
                (this, SetupActivity.TYPE).equals(SetupActivity.IN))
            id = SetupActivity.IN;
        else
            id = SharedPreferenceHandler.getStringValues(this, SetupActivity.ID);
        Toast.makeText(this, "Searching and discovering nearby devices!", Toast.LENGTH_SHORT).show();
        startAdvertising();
        startDiscovery();
        return START_NOT_STICKY;
    }

    private void startDiscovery() {
        connectionsClient.stopDiscovery();
        connectionsClient.startDiscovery(getPackageName(), endpointDiscoveryCallback,
                new DiscoveryOptions.Builder().setStrategy(STRATEGY).build()).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
                connectionsClient.stopDiscovery();
                connectionsClient.stopAdvertising();
                connectionsClient.stopAllEndpoints();
                startAdvertising();
                startDiscovery();
            }
        });
    }

    private final EndpointDiscoveryCallback endpointDiscoveryCallback = new EndpointDiscoveryCallback() {
        @Override
        public void onEndpointFound(@NonNull String endpointID, @NonNull DiscoveredEndpointInfo discoveredEndpointInfo) {
            Log.d(TAG, "Endpoint found:" + endpointID + " " + discoveredEndpointInfo.getServiceId());
            if (!endpointID.equals(lastConnected)) {
                Log.d(TAG, "Start connection:");
                int start = rand.nextInt(5000) + 1000;
                try {
                    Thread.sleep(start);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                connectionsClient.stopDiscovery();
                connectionsClient.stopAdvertising();
                connectionsClient.requestConnection(id, endpointID, connectionLifecycleCallback)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        connectionsClient.stopDiscovery();
                        connectionsClient.stopAdvertising();
                        startAdvertising();
                        startDiscovery();
                    }
                });
            }
        }

        @Override
        public void onEndpointLost(@NonNull String endpointID) {
            Log.d(TAG, "Just found and lost the endpoint:" + endpointID);
            connectionsClient.stopAllEndpoints();
            connectionsClient.stopDiscovery();
            connectionsClient.stopAdvertising();
            transmitted = false;
            Toast.makeText(NearbyService.this, "Searching and discovering nearby devices!",
                    Toast.LENGTH_SHORT).show();
            startAdvertising();
            startDiscovery();
        }
    };


    private void startAdvertising() {
        connectionsClient.stopAdvertising();
        connectionsClient.startAdvertising(id, getPackageName(), connectionLifecycleCallback,
                new AdvertisingOptions.Builder().setStrategy(STRATEGY).build());
    }

    private final ConnectionLifecycleCallback connectionLifecycleCallback = new
            ConnectionLifecycleCallback() {
                @Override
                public void onConnectionInitiated(@NonNull String endpointID, @NonNull ConnectionInfo connectionInfo) {
                    connectionsClient.acceptConnection(endpointID, payloadCallback);
                    connectedFella = connectionInfo.getEndpointName();
                }

                @Override
                public void onConnectionResult(@NonNull String endpointId, @NonNull ConnectionResolution result) {
                    if (result.getStatus().isSuccess()) {
                        Log.d(TAG, "Connection successful");
                        Log.d(TAG, "Connected Fella name is:" + connectedFella);
                        connectionsClient.sendPayload(endpointId,
                                Payload.fromBytes(id.getBytes(StandardCharsets.UTF_8)));
                    } else {
                        Log.d(TAG, "Connection unsuccessful");
                        Toast.makeText(NearbyService.this, "Searching and discovering nearby devices!",
                                Toast.LENGTH_SHORT).show();
                        boolean transmitted = false;
                        connectionsClient.stopAllEndpoints();
                        startAdvertising();
                        startDiscovery();
                    }
                }

                @Override
                public void onDisconnected(@NonNull String endpointId) {
                    Log.d(TAG, "Disconnected from the other device");
                    Toast.makeText(NearbyService.this,
                            "Disconnected from other device! Searching and discovering nearby " +
                                    "devices!", Toast.LENGTH_SHORT).show();
                    transmitted = false;
                    startDiscovery();
                    startAdvertising();
                }
            };

    private final PayloadCallback payloadCallback =
            new PayloadCallback() {
                TransferPOJO obj;

                @Override
                public void onPayloadReceived(String endpointId, Payload payload) {
                    lastConnected = endpointId;
                    if (payload.getType() == Payload.Type.BYTES) {
                        String connectedId = new String(payload.asBytes(), StandardCharsets.UTF_8);
                        if (!connectedId.equals(ACK)) {
                            Log.d(TAG, "Connected Id received:" + connectedId);
                            Toast.makeText(NearbyService.this, "Connected to a nearby device!", Toast.LENGTH_SHORT).show();
                            sendMsgsForConnected(endpointId, connectedId);
                        } else {
                            if (transmitted == true) {
                                connectionsClient.stopAllEndpoints();
                                transmitted = false;
                                Toast.makeText(NearbyService.this, "Disconnected from " +
                                                "nearby device and searching for nearby devices!",
                                        Toast.LENGTH_SHORT).show();
                                startAdvertising();
                                startDiscovery();
                            }
                        }
                    }
                    if (payload.getType() == Payload.Type.STREAM) {
                        try {
                            //InputStream in = payload.asStream().asInputStream();
                            InputStream in = payload.asStream().asInputStream();
                            ObjectInputStream oin = new ObjectInputStream(in);
                            obj = (TransferPOJO) oin.readObject();
                            in.close();
                            handleIncomingMsg(obj);
                            transmitted = true;
                            connectionsClient.sendPayload(endpointId, Payload.fromBytes(ACK.getBytes(StandardCharsets.UTF_8)));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {
                    Log.d(TAG, update.getTotalBytes() + " " + update.getBytesTransferred());
                    Log.d(TAG, "Transfer status: " + update.getStatus());
                    if (update.getStatus() == PayloadTransferUpdate.Status.SUCCESS) {
                        Log.d(TAG, "Success!");
                    }
                }
            };

    private void handleIncomingMsg(TransferPOJO obj) throws Exception {
        Toast.makeText(NearbyService.this, "All the msgs received " +
                "and being processed now!", Toast.LENGTH_SHORT).show();
        List<Msg> msg_list = obj.msg_list;
        List<String> cipher_list = obj.cipher_list;
        String nodeType = SharedPreferenceHandler.getStringValues(getApplicationContext(), SetupActivity.TYPE);
        if (nodeType.equals(SetupActivity.IN)) {
            HandleMsgsForIN(msg_list, cipher_list);
        } else {
            HandleMsgsForServerNodes(msg_list, cipher_list);
        }
    }

    private void HandleMsgsForServerNodes(List<Msg> msg_list, List<String> cipher_list) throws Exception {

        byte[] pub = Common.suckFile(SharedPreferenceHandler.getStringValues(getApplication(),
                SetupActivity.PUB));
        byte[] prv = Common.suckFile(SharedPreferenceHandler.getStringValues(getApplication(),
                SetupActivity.PRV));

        Log.d(TAG, pub.length + " " + prv.length);

        Cpabe cpabe = new Cpabe();
        for (int i = 0; i < msg_list.size(); i++) {
            Msg msg = msg_list.get(i);
            String encoded = cipher_list.get(i);
            byte[] cipher = Base64.decode(encoded, Base64.DEFAULT);
            Log.d(TAG, "" + cipher.length);
            try {
                byte[] plt = cpabe.decModified(pub, prv, cipher);
                String format = msg.getFileName().substring(msg.getFileName().indexOf("."));
                File imgFile = SDFileHandler.createFile(msg.getId(), format, plt);
                msg.setPath(imgFile.getAbsolutePath());
                msg.setCipher(null);
                msg.setType(Homescreen.RECEIVED);
                boolean flag = verifyHashes(msg, this);
                if(flag) msg.setIsverified(true);
                else msg.setIsverified(false);
            } catch (Exception e) {
                e.printStackTrace();
                File cipherFile = SDFileHandler.createFile(msg.getId(), SetupActivity.CPH,
                        encoded.getBytes(StandardCharsets.UTF_8));
                msg.setCipher(cipherFile.getAbsolutePath());
                msg.setPath(null);
                msg.setType(Homescreen.INTERMEDIATE);
            }
            database.dao().insertMsg(msg);

        }
    }

    public boolean verifyHashes(Msg msg, Context context) {
            database = AppDatabase.getAppDatabase(context);
            int count = msg.getNum_nodes_travelled();
            Log.d(TAG, "Number of nodes travelled:" + count);
            String currHash = msg.getNextHash();
            for (int i = count + 1; i < 5; i++) {
                currHash = shaHash(currHash);
            }
            Log.d(TAG, "LAst Hash:" + currHash);
            String lastHash = database.dao().getk5(currHash);
            String k5 = lastHash;
            Log.d(TAG, "K5:" + currHash);
            if(lastHash == null) return false;
            Log.d(TAG, "Key Hash Infot:" + msg.getHashInfo());
            String[] keychains = msg.getHashInfo().split("\\|");
            Log.d(TAG, "Number of keychains for this message:" + Arrays.toString(keychains));
            for (String keyChain : keychains) {
                String[] hashNodes = keyChain.split("-");
                String hash = hashNodes[0];
                String nodes = hashNodes[1];
                String nodeHash = shaHash(lastHash+nodes);
                Log.d(TAG, nodeHash + "|" + hash);
                if (!(nodeHash).equals(hash)) return false;
                Log.d(TAG, "True");
                lastHash = shaHash(lastHash);
            }
            String hashInfo = msg.getHashInfo();
            msg.setHashInfo(hashInfo + "|" + " First hash:" + k5 + "| Last hash:" + currHash);
            return true;
    }

    private void HandleMsgsForIN(List<Msg> msg_list, List<String> cipher_list) throws IOException {
        for (int i = 0; i < msg_list.size(); i++) {
            Msg msg = msg_list.get(i);
            String cipher = cipher_list.get(i);

            File cipherFile = SDFileHandler.createFile(msg.getId(), SetupActivity.CPH,
                    cipher.getBytes(StandardCharsets.UTF_8));
            msg.setCipher(cipherFile.getAbsolutePath());
            msg.setPath(null);
            msg.setType(Homescreen.INTERMEDIATE);
            database.dao().insertMsg(msg);
        }
    }

    private void sendMsgsForConnected(String endpointId, String connectedId) {
        try {
            List<Msg> msg_list = database.dao().getMsgsForDevice(connectedId, Homescreen.RECEIVED);
            List<String> cipher_list = getCipherForMsgs(msg_list);

            for (Msg msg : msg_list) {
                if (msg.getType().equals(Homescreen.OWN)) {
                    String devices = id+ ", " + connectedId;
                    String[] result = hashManagement(devices, null).split("/");
                    Log.d(TAG, msg.getId());
                    msg.setHashInfo(result[0] + "-" + devices);
                    msg.setNextHash(result[1]);
                    msg.setConnectedDevices(connectedId);
                } else {
                    String devices = id + ", " + connectedId;
                    String[] result = hashManagement(devices, msg.getNextHash()).split("/");
                    String hashInfo = msg.getHashInfo() + "|" + result[0] + "-" + devices;
                    String connectedDevices = msg.getConnectedDevices() + ", " + connectedId;
                    msg.setNextHash(result[1]);
                    msg.setHashInfo(hashInfo);
                    msg.setConnectedDevices(connectedDevices);
                }
                Log.d(TAG, "Next hash:" + msg.getNextHash());
                msg.setNum_nodes_travelled(msg.getNum_nodes_travelled()+1);
            }


            TransferPOJO transferPOJO = new TransferPOJO(msg_list, cipher_list);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(transferPOJO);
            objectOutputStream.flush();
            objectOutputStream.close();
            Log.d(TAG, String.valueOf(outputStream.toByteArray().length));
            InputStream in = new ByteArrayInputStream(outputStream.toByteArray());
            Log.d(TAG, "Start transfer!");
            connectionsClient.sendPayload(endpointId, Payload.fromStream(in));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private List<String> getCipherForMsgs(List<Msg> msgs) {
        List<String> cipher_list = new ArrayList<>();
        for (Msg msg : msgs) {
            byte[] cipher = SDFileHandler.readFile(msg.getCipher());
            String encoded = new String(cipher, StandardCharsets.UTF_8);
            cipher_list.add(encoded);
        }
        return cipher_list;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service is destroyed");
        Toast.makeText(this, "All Nearby operations are stopped!", Toast.LENGTH_SHORT).show();
        transmitted = false;
        connectionsClient.stopDiscovery();
        connectionsClient.stopAdvertising();
        connectionsClient.stopAllEndpoints();
    }

    private String hashManagement(String devices, String currHash) {
        if (currHash == null) {
            int num = rand.nextInt(5);
            List<KeyChainHash> keyChain = database.dao().getChainHash(SetupActivity.OWN);
            currHash = keyChain.get(num).getK5();
            Log.d(TAG, "k5:" + keyChain.get(num).getK5() + " " + "k0:" + keyChain.get(num).getK0());
            Log.d(TAG, "-----------------");
        }
        String result = shaHash(currHash + devices);
        String nextHash = shaHash(currHash);
        return result + "/" + nextHash;
    }

    private String shaHash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashInBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashInBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

}
