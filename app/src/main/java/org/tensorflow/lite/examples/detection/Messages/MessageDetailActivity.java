package org.tensorflow.lite.examples.detection.Messages;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.tensorflow.lite.examples.detection.Homescreen;
import org.tensorflow.lite.examples.detection.NearbySupport.NearbyService;
import org.tensorflow.lite.examples.detection.R;
import org.tensorflow.lite.examples.detection.SDFileHandler;
import org.tensorflow.lite.examples.detection.SQLHandler.AppDatabase;
import org.tensorflow.lite.examples.detection.SQLHandler.KeyChainHash;
import org.tensorflow.lite.examples.detection.SQLHandler.Msg;
import org.tensorflow.lite.examples.detection.SetupActivity;
import org.tensorflow.lite.examples.detection.SharedPreferences.SharedPreferenceHandler;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

import co.junwei.cpabe.Common;
import co.junwei.cpabe.Cpabe;

public class MessageDetailActivity extends AppCompatActivity {
    ImageView imageView;
    TextView idtv, filenametv, sourceidtv, policytv, policy_show_tv,
            nodes_travelled_tv, connected_devices_tv, isverified_tv, hashinfo_tv, firstHash_tv;
    EditText policy;
    Button encryptbtn;
    AppDatabase database;

    private static final String TAG = MessageDetailActivity.class.getCanonicalName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_detail);

        database = AppDatabase.getAppDatabase(getApplicationContext());

        Bundle bundle = getIntent().getExtras();
        Msg msg = (Msg) bundle.getSerializable(MsgAdapter.MSG);

        Log.d(TAG, "hashInfo:" + msg.getHashInfo());

        imageView = findViewById(R.id.msgImg);
        idtv = findViewById(R.id.id);
        filenametv = findViewById(R.id.file_name);
        sourceidtv = findViewById(R.id.source_id);
        policy = findViewById(R.id.policyed);
        encryptbtn = findViewById(R.id.encrypt_btn);
        policytv = findViewById(R.id.policy_tv);
        policy_show_tv = findViewById(R.id.policy_show_tv);
        nodes_travelled_tv = findViewById(R.id.nodes_travelled_tv);
        connected_devices_tv = findViewById(R.id.connected_devices_tv);
        isverified_tv = findViewById(R.id.isverified_tv);
        hashinfo_tv = findViewById(R.id.hashinfo_tv);
        firstHash_tv = findViewById(R.id.first_hash);

        if(msg.getPath() == null) Glide.with(this).load(R.drawable.sample_image).into(imageView);
        else Glide.with(this).load(new File(msg.getPath())).into(imageView);

        idtv.setText("Msg ID: " + msg.getId());
        filenametv.setText("Filename: " + msg.getFileName());
        sourceidtv.setText("Source Id: " + msg.getSource());
        nodes_travelled_tv.setText("Nodes Travelled: " + msg.getNum_nodes_travelled());
        connected_devices_tv.setText("Nodes Connected Before:" + msg.getConnectedDevices());
        if(msg.getFirst_hash() != null)
            firstHash_tv.setText("First Hash (kn): " + msg.getFirst_hash());
        else firstHash_tv.setVisibility(View.GONE);

        String policy_text = msg.getPolicy();
        if(policy_text == null || policy_text.length() == 0)
            policy_show_tv.setVisibility(View.GONE);
        else {
            policy_show_tv.setVisibility(View.VISIBLE);
            policy_show_tv.setText("Encrypted with policy: " + policy_text);
        }

        Log.d(TAG, msg.getType());
        if(!msg.getType().equals(Homescreen.OWN)){
            policy.setVisibility(View.GONE);
            encryptbtn.setVisibility(View.GONE);
            policytv.setVisibility(View.GONE);
        }
        else {
            connected_devices_tv.setVisibility(View.GONE);
        }

        if(msg.getType().equals((Homescreen.RECEIVED))){
            isverified_tv.setVisibility(View.VISIBLE);
            isverified_tv.setText("Hash Verified: " + msg.isIsverified());
            hashinfo_tv.setVisibility(View.VISIBLE);
            hashinfo_tv.setText("Hash details: " + msg.getHashInfo());
        }
        else {
            isverified_tv.setVisibility(View.GONE);
            hashinfo_tv.setVisibility(View.GONE);
        }

        encryptbtn.setOnClickListener(v -> {
            Log.d(TAG, "Inside on click listener");
            String policyTags = policy.getText().toString();
            //String imgPath = Homescreen.getRealPathFromURI(this, Uri.parse(msg.getPath()));
            String imgPath = msg.getPath();
            try {
                String pubKeyPath = SharedPreferenceHandler.getStringValues(getApplicationContext(),
                        SetupActivity.PUB);
                byte[] pub_byte = Common.suckFile(pubKeyPath);
                Cpabe abeObj = new Cpabe();
                byte[] cipher = abeObj.encModified(pub_byte, policyTags, imgPath);
                String encoded = Base64.encodeToString(cipher, Base64.DEFAULT);

                byte[] hashbytes = msg.getFirst_hash().getBytes(StandardCharsets.UTF_8);
                byte[] hashcipher = abeObj.encHash(pub_byte, policyTags, hashbytes);
                String hashStrcipher = Base64.encodeToString(hashcipher, Base64.DEFAULT);
                msg.setEncrypted_hash(hashStrcipher);

                byte[] encoded_cipher = encoded.getBytes(StandardCharsets.UTF_8);
                Log.d(TAG, "Encoded cipher:" + cipher.length);
                File encFile = SDFileHandler.createFile(msg.getId(), SetupActivity.CPH, encoded_cipher);
                msg.setCipher(encFile.getAbsolutePath());
                msg.setPolicy(policyTags);
                database.dao().insertMsg(msg);
                encryptbtn.setEnabled(false);
                Toast.makeText(this, "Image has been encrypted!", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Enter valid keys that is present in Master Key Generator!",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
