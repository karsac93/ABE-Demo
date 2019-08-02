package org.tensorflow.lite.examples.detection;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.mst.demo.HashObject;
import com.mst.demo.MobileServerPOJO;

import org.tensorflow.lite.examples.detection.SQLHandler.AppDatabase;
import org.tensorflow.lite.examples.detection.SQLHandler.KeyChainHash;
import org.tensorflow.lite.examples.detection.SharedPreferences.SharedPreferenceHandler;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

public class SetupActivity extends AppCompatActivity {
    private Button setupBtn;
    private RadioGroup typeGrp;
    private RadioButton btn;
    private EditText ipAddrText;
    public static final String STATUS = "setup", ID = "id";
    public static final String TYPE = "type", DS = "Disaster", AR = "Battlefield", IN = "IN";

    //public static final String IP = "10.106.41.137";
    private static final int ARPORT = 5001, DSPORT = 5002;

    private String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA, Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,};
    private static final int PERMISSIONS_REQUEST = 1;
    public static final String TAG = SetupActivity.class.getCanonicalName();
    public static final String DIR = "/ABE/", PUB = "pubkey", PRV = "prvkey", CPH = ".cpabe",
            ATTR = "attrs", OWN = "own", OTHER = "other";
    String id = "IN";
    AppDatabase database;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        database = AppDatabase.getAppDatabase(getApplicationContext());
        setupBtn = findViewById(R.id.setup);
        setupBtn.setEnabled(false);
        typeGrp = findViewById(R.id.radioGroup);
        ipAddrText = findViewById(R.id.ipAddrText);
        Intent intent = new Intent(SetupActivity.this, Homescreen.class);
        if(SharedPreferenceHandler.getBooleanValue(this, STATUS) == true){
            startActivity(intent);
            finish();
        }

        if (hasPermission()) {
            setupBtn.setEnabled(true);
        } else {
            requestPermission();
        }

        setupBtn.setOnClickListener(v -> {
            String ipAddress = ipAddrText.getText().toString();
            int selectedId = typeGrp.getCheckedRadioButtonId();
            btn = findViewById(selectedId);
            if(btn.getText().toString().contains(DS)){
                if(!validIP(ipAddress)){
                    Toast.makeText(this, "Enter a valid IP address", Toast.LENGTH_SHORT).show();
                    return;
                }
                    if(isNetworkAvailable()){
                        //connect to the internet
                        new Thread(() -> {
                            try {
                                connectToInternet(ipAddress, DSPORT, DS, this, intent);
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                        }).start();
                    }
                    else
                        Toast.makeText(SetupActivity.this,
                                "Please connect to the internet!", Toast.LENGTH_SHORT).show();
            }
            else if(btn.getText().toString().contains(AR)){
                if(!validIP(ipAddress)){
                    Toast.makeText(this, "Enter a valid IP address", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(isNetworkAvailable()){
                    //connect to the internet

                        new Thread(() -> {
                            try {
                                connectToInternet(ipAddress, ARPORT, AR, this, intent);
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                        }).start();
                }
                else
                    Toast.makeText(SetupActivity.this,
                            "Please connect to the internet!", Toast.LENGTH_SHORT).show();
            }
            else{
                SharedPreferenceHandler.setBooleanValue(getApplicationContext(), STATUS, true);
                SharedPreferenceHandler.setStringValues(getApplicationContext(), TYPE, IN);
                SharedPreferenceHandler.setStringValues(getApplicationContext(), ID, id);
                startActivity(intent);
                finish();
            }
        });
    }

    public static boolean validIP (String ip) {
        try {
            if ( ip == null || ip.isEmpty() ) {
                return false;
            }

            String[] parts = ip.split( "\\." );
            if ( parts.length != 4 ) {
                return false;
            }

            for ( String s : parts ) {
                int i = Integer.parseInt( s );
                if ( (i < 0) || (i > 255) ) {
                    return false;
                }
            }
            if ( ip.endsWith(".") ) {
                return false;
            }

            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    private void connectToInternet(String ipAddress, int port, String type, Context context, Intent intent) throws IOException, ClassNotFoundException {

        Socket socket = new Socket(ipAddress, port);
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        MobileServerPOJO obj = (MobileServerPOJO) in.readObject();

        id = obj.getDeviceID();
        byte[] pub = obj.getPub();
        byte[] prv = obj.getPrv();
        String[] attr = obj.getAttr();
//        List<HashObject> ownHash = obj.getOwnHash();
//        List<HashObject> otherHash = obj.getOtherHash();

//        setHashes(ownHash, OWN);
//        setHashes(otherHash, OTHER);

        String pubPath = SDFileHandler.createFile(PUB, "", pub).getAbsolutePath();
        String prvPath = SDFileHandler.createFile(PRV, "", prv).getAbsolutePath();

        SharedPreferenceHandler.setStringValues(getApplicationContext(), PUB, pubPath);
        SharedPreferenceHandler.setStringValues(getApplicationContext(), PRV, prvPath);

        SharedPreferenceHandler.setStringValues(getApplicationContext(), ATTR, Arrays.toString(attr));
        SharedPreferenceHandler.setStringValues(getApplicationContext(), TYPE, type);
        SharedPreferenceHandler.setStringValues(getApplicationContext(), ID, id);
        SharedPreferenceHandler.setBooleanValue(getApplicationContext(), STATUS, true);

        context.startActivity(intent);
        finish();
    }

//    private void setHashes(List<HashObject> hashes, String hashType) {
//        for(HashObject obj: hashes){
//            KeyChainHash chainHash = new KeyChainHash(obj.getK5(), obj.getK0(), hashType);
//            database.dao().insertHash(chainHash);
//        }
//
//    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onRequestPermissionsResult(
            final int requestCode, final String[] permissions, final int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                setupBtn.setEnabled(true);
            } else {
                requestPermission();
            }
        }
    }

    private boolean hasPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for(String permission : permissions){
                if(ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
                    return false;
            }
            return true;
        } else {
            return true;
        }
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_REQUEST);
        }
    }
}
