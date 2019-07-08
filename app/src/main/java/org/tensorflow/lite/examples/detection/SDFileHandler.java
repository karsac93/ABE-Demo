package org.tensorflow.lite.examples.detection;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class SDFileHandler {

    public static final String DIR = "/ABE/";
    public static final String TAG = java.util.logging.FileHandler.class.getCanonicalName();

    public static File createFile(String msg_id, String format, byte[] data) throws IOException {
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + DIR);
        myDir.mkdirs();
        String fname = msg_id + format;
        File file = new File(myDir, fname);
        Log.i(TAG, "" + file);
        if (file.exists())
            file.delete();
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        OutputStream out = new FileOutputStream(file);
        out.write(data);
        out.flush();
        out.close();
        return file;
    }

    public static byte[] readFile(String path) {
        File file = new File(path);
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return bytes;
    }
}
