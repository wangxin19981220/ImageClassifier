package com.example.imageclassifier;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

public class MainActivity extends AppCompatActivity {
    private ImageView imageView;
    private TextView textView;
    private Button button;

    private String imagePath;
    private Classifier classifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        textView = findViewById(R.id.textView);
        button = findViewById(R.id.button);
        request_permissions();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                imagePath = CameraUtil.startCamera(MainActivity.this, 0);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(resultCode, resultCode, data);
        RequestOptions options = new RequestOptions().skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE);

        int orientation = CameraUtil.getExifOrientation(imagePath);
        Log.d("orientation", orientation + ":degree");


        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, opt);

        int imageWidth = opt.outWidth;
        int imageHeight = opt.outHeight;

        opt.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, opt);
        imageView.setImageBitmap(bitmap);

        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

        //int cropSize = Math.min(imageWidth, imageHeight);

        try {
            classifier = Classifier.create(this, Classifier.Model.QUANTIZED_MOBILENET, Classifier.Device.CPU, 1);
            if (classifier != null) {
                List<Classifier.Recognition> results = classifier.recognizeImage(bitmap, orientation);

                if (results != null && results.size() > 0) {
                    Classifier.Recognition recognition= results.get(0);
                    if (recognition != null) {
                        if (recognition.getTitle() != null)
                            textView.setText(recognition.getTitle());
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void  request_permissions() {

            List<String> permissionList = new ArrayList<String>();
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(Manifest.permission.CAMERA);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }

            if (!permissionList.isEmpty()) {
               String ps[]=permissionList.toArray(new String[permissionList.size()]);
               ActivityCompat.requestPermissions(MainActivity.this,ps,1);
                }

        }
//    @RequiresApi(api = Build.VERSION_CODES.M)
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantresults){
//        super.onRequestPermissionsResult(requestCode,permissions,grantresults);
//        if(grantresults.length > 0){
//            List<String> deniedlist = new ArrayList<>();
//            for(int i = 0;i < grantresults.length;i++){
//                if(grantresults[i]!= PackageManager.PERMISSION_GRANTED){
//                    deniedlist.add(permissions[i]);
//                }
//            }
//            if(!deniedlist.isEmpty()){
//                for(String deniPermission : deniedlist){
//                    boolean flag = shouldShowRequestPermissionRationale(deniPermission);
//                    if(!flag){
//                        permissionShouldShowRationale(deniedlist);
//                        return;
//                    }
//                }
//                permissionHasDenied(deniedlist);
//            }
//        }
//    }
//    private void permissionHasDenied(List<String> deniedList){
//        if(listener != null){
//            listener.onDenied(deniedList);
//        }
//    }
}