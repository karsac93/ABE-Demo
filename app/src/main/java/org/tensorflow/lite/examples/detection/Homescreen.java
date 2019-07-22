package org.tensorflow.lite.examples.detection;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.tensorflow.lite.examples.detection.Messages.MessagesActivity;
import org.tensorflow.lite.examples.detection.NearbySupport.NearbyService;
import org.tensorflow.lite.examples.detection.SharedPreferences.SharedPreferenceHandler;

import java.io.File;
import java.util.List;

import org.tensorflow.lite.examples.detection.SQLHandler.AppDatabase;
import org.tensorflow.lite.examples.detection.SQLHandler.Msg;

public class Homescreen extends AppCompatActivity {

    private Button soldierbtn, gallerybtn, msgbtn, enablebtn, disablebtn;
    private TextView idview, attrs_tv;
    public static final String OWN = "Own msg", INTERMEDIATE = "Intermediate msg",
            RECEIVED = "Received msg", MSG_COUNT = "msg_count";

    private static final int REQ  = 1;
    private static final String TAG = Homescreen.class.getCanonicalName();
    String id;
    AppDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homescreen);

        database = AppDatabase.getAppDatabase(getApplicationContext());
        soldierbtn = findViewById(R.id.sdbtn);
        gallerybtn = findViewById(R.id.galbtn);
        msgbtn = findViewById(R.id.msgbtn);
        enablebtn = findViewById(R.id.enablebtn);
        disablebtn = findViewById(R.id.disablebtn);
        idview = findViewById(R.id.idtxtv);
        attrs_tv = findViewById(R.id.attrs);

        id = SharedPreferenceHandler.getStringValues(this, SetupActivity.ID);
        Log.d(TAG, id);
        idview.setText("ID: " + id);

        String node_type = SharedPreferenceHandler.getStringValues(this, SetupActivity.TYPE);
        if(node_type.equals(SetupActivity.IN)){
            gallerybtn.setVisibility(View.GONE);
            soldierbtn.setVisibility(View.GONE);
            attrs_tv.setVisibility(View.GONE);
            idview.setText("Intermediate Node");
        }

        attrs_tv.setText(" This device can decrypt messages with attributes: " +
                SharedPreferenceHandler.getStringValues(getApplicationContext(),
                        SetupActivity.ATTR));

        soldierbtn.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), DetectorActivity.class);
            startActivity(intent);
        });

        gallerybtn.setOnClickListener(v -> {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(galleryIntent, REQ);
        });

        msgbtn.setOnClickListener(v -> {
            Intent intent = new Intent(Homescreen.this, MessagesActivity.class);
            startActivity(intent);
        });

        enablebtn.setOnClickListener(v -> {
            Intent intent = new Intent(Homescreen.this, NearbyService.class);
            startService(intent);
        });

        disablebtn.setOnClickListener(v -> {
            Intent intent = new Intent(Homescreen.this, NearbyService.class);
            stopService(intent);
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK && data.getData() != null){
            Uri uri = data.getData();
            Log.d(TAG, uri.getPath());
            String path = getRealPathFromURI(this, uri);
            File file = new File(path);
            int msg_count = SharedPreferenceHandler.getIntValues(this, MSG_COUNT);
            String msg_id = id + "_" + msg_count;
            msg_count += 1;
            SharedPreferenceHandler.setIntValues(this, MSG_COUNT, msg_count);
            String source = String.valueOf(id);
            String fileName = file.getName();
            String type = OWN;
            Msg msg = new Msg(msg_id, source, path, type, fileName, null, null,
                    null, 0, null, id, true);
            database.dao().insertMsg(msg);
            Toast.makeText(this, "File Created Successfully! See in messages!", Toast.LENGTH_SHORT).show();
        }
    }

    public static String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(contentUri, null, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
