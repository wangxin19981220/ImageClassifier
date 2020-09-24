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
import android.os.Environment;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

public class MainActivity extends AppCompatActivity {
    private ImageView imageView;
    private TextView textView;
    private Button button;
    private String object;
    private String imagePath;
    private Classifier classifier;
    static String to;

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
        //调整图片角度
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
                File image = new File(imagePath);
                try{
                    image.delete();
                }catch (Exception e){
                    e.printStackTrace();
                }
                if (results != null && results.size() > 0) {
                    Classifier.Recognition recognition= results.get(0);
                    if (recognition != null) {
                        if (recognition.getTitle() != null)
                            object=recognition.getTitle();
                            textView.setText(object);
                            translate(object);


                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


    }
    private void translate(final String word){

        String from = "auto";//源语种 en 英语 zh 中文

        if (word.length() == word.getBytes().length) {//成立则说明没有汉字，否则由汉字。
            to = "zh"; //没有汉字 英译中
        } else {
            to = "en";//含有汉字 中译英
        }
        String appid = "20200919000568880";//appid 管理控制台有
        String salt = (int) (Math.random() * 100 + 1) + "";//随机数 这里范围是[0,100]整数 无强制要求
        String key = "aaFPFnqyDKUFp_T8ulHz";//密钥 管理控制台有
        String string1 = appid + word + salt + key;// string1 = appid+q+salt+密钥
        String sign = MD5Utils.getMD5Code(string1);// 签名 = string1的MD5加密 32位字母小写

        Retrofit retrofitBaidu = new Retrofit.Builder()
                .baseUrl("http://api.fanyi.baidu.com/api/trans/vip/translate/")
                .addConverterFactory(GsonConverterFactory.create()) // 设置数据解析器
                .build();
        BaiduTranslateService baiduTranslateService = retrofitBaidu.create(BaiduTranslateService.class);


        retrofit2.Call<RespondBean> call = baiduTranslateService.translate(word, from, to, appid, salt, sign);
        call.enqueue(new Callback<RespondBean>() {
            @Override
            public void onResponse(retrofit2.Call<RespondBean> call, Response<RespondBean> response) {
                //请求成功

                RespondBean respondBean = response.body();//返回的JSON字符串对应的对象
                String result = respondBean.getTrans_result().get(0).getDst();//获取翻译的字符串String
                textView.setText(word + "\n" + result);

            }

            @Override
            public void onFailure(Call<RespondBean> call, Throwable t) {
                //请求失败 打印异常

            }
        });
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


}