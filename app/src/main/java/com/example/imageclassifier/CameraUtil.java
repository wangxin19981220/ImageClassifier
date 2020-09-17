package com.example.imageclassifier;

import android.app.Activity;
import android.content.Intent;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class CameraUtil {
    public static String startCamera(Activity activity, int requestCode) {
        Uri imageUri;
        String dir = "/imageClassifier/";
        File file = new File(dir);
        if (!file.exists())
            file.mkdir();
        File outputImage = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/imageClassifier/", System.currentTimeMillis() + ".jpg");
        try{
            if(outputImage.exists()){
                outputImage.delete();
            }
          File out_path = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/imageClassifier/");
            if(!out_path.exists()){
                out_path.mkdir();
            }
            outputImage.createNewFile();
        }catch(Exception e){
            e.printStackTrace();
        }
        if (Build.VERSION.SDK_INT >= 24) {
            imageUri = FileProvider.getUriForFile(activity, "com.example.imageclassifier", outputImage);
        } else {
            imageUri = Uri.fromFile(outputImage);
        }

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
        activity.startActivityForResult(intent, 0);

        return outputImage.getAbsolutePath();
    }

    public static int getExifOrientation(String imagePath) {
        int degree = 0;
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(imagePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (exif != null) {
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
            if (orientation != -1) {
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        degree = 90;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        degree = 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        degree = 270;
                        break;
                }
            }
        }

        return degree;
    }
}
