package org.tensorflow.lite.examples.detection;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Environment;
import android.util.Log;

import org.tensorflow.lite.examples.detection.DetectorActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SaveRunnable implements Runnable {
    Bitmap bitmap;
    Rect location;
    
    public SaveRunnable(Bitmap bitmap, Rect location) {
        this.bitmap = bitmap;
        this.location = location;
    }

    @Override
    public void run() {
        Log.d("SaveThread", "Running the thread to save");
        if (location.isEmpty()) return;
        if (location.left <= 0 || location.top < 0 || location.height() < 0 || location.width() < 0) return;
        Log.d(DetectorActivity.class.getCanonicalName(), location.left + " " + location.top + " " +
                location.width() + " " + location.height() + " " + bitmap.getWidth() + " " + bitmap.getHeight());
        int width = location.width();
        if ((location.left + location.width()) > 300) {
            int diff = location.left + location.width() - 300;
            width = (location.left + location.width()) - diff;
        }
        int height = location.height();
        if ((location.top + location.height()) > 300) {
            int diff = location.left + location.height() - 300;
            height = (location.top + location.height()) - diff;
        }
        try {
            Bitmap objectBitmap = Bitmap.createBitmap(bitmap, location.left, location.top, width, height);
            File sd = Environment.getExternalStorageDirectory();
            File destFolder = new File(sd + "/DetectedObjects");
            Log.d(DetectorActivity.class.getCanonicalName(), destFolder.getAbsolutePath());
            if (!destFolder.exists()) {
                if (!destFolder.mkdir()) {
                    Log.d(DetectorActivity.class.getCanonicalName(), "Not able to create the folder!");
                    return;
                }
            }
            SimpleDateFormat s = new SimpleDateFormat("ddMMyyyyhhmmss");
            String format = s.format(new Date());
            String filename = format + ".png";
            File objectFile = new File(destFolder, filename);
            try {
                FileOutputStream out = new FileOutputStream(objectFile);
                objectBitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
                out.flush();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
