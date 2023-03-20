package com.liuyue.hotfix;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.liuyue.hotfix.databinding.ActivityMainBinding;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    /**
     * 此 URL 的有效时长为 86400 秒（60 天，2023 年 3 月 20 日开始）
     */
    private static final String SO_URL = "http://qov2cux0b.hn-bkt.clouddn.com/libhotfix.so?e=1679381404&token=QxZugR8TAhI38AiJ_cptTl3RbzLyca3t-AAiH-Hh:Vbvl7UHmHiu6WpHhABd1J-5j69E=";

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String soSavePath = getDir("libs", MODE_PRIVATE).getAbsolutePath() + "/libhotfix.so";
        if (new File(soSavePath).exists()) {
            Toast.makeText(MainActivity.this, "加载新版本 so", Toast.LENGTH_SHORT).show();
            System.load(soSavePath);
        } else {
            Toast.makeText(MainActivity.this, "加载老版本 so", Toast.LENGTH_SHORT).show();
            System.loadLibrary("hotfix");
        }
        TextView tv = binding.sampleText;
        tv.setText(stringFromJNI());

        binding.button.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "开始更新 so ...", Toast.LENGTH_SHORT).show();
            loadSoFromServer();
        });
    }

    private void loadSoFromServer() {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(SO_URL).build();
                Response response = client.newCall(request).execute();

                if (response.code() == 200) {
                    String soSavePath = getDir("libs", MODE_PRIVATE).getAbsolutePath() + "/libhotfix.so";
                    File file = new File(soSavePath);
                    if (!file.exists()) {
                        file.getParentFile().mkdirs();
                    }

                    InputStream inputStream = response.body().byteStream();
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    byte[] bytes = new byte[4 * 1024];
                    int len;
                    while ((len = inputStream.read(bytes)) != -1) {
                        fileOutputStream.write(bytes, 0, len);
                    }
                    inputStream.close();
                    fileOutputStream.flush();
                    fileOutputStream.close();

                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "更新完成，请重启 APP", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "更新 so 失败 "+ e.getMessage(), Toast.LENGTH_SHORT).show());
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * A native method that is implemented by the 'hotfix' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}