package com.work.webviewandandroid;

import android.app.ProgressDialog;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RequestActivity extends AppCompatActivity {


    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);
        progressDialog = new ProgressDialog(this);

    }

    private void post(String url, String json) {

        progressDialog.show();
        MediaType JSON
                = MediaType.get("application/json; charset=utf-8");
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();


        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        Toast.makeText(RequestActivity.this, "请求失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull final Response response) throws IOException {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        try {
                            String responseString = response.body().string().toString();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Toast.makeText(RequestActivity.this, "请求成功", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }

    private String getInfo() {

        HashMap<String, Object> data = new HashMap<>();
        data.put("code", "0");

        PackageManager pckMan = this.getPackageManager();
        ArrayList<HashMap<String, Object>> items = new ArrayList<HashMap<String, Object>>();

        List<PackageInfo> packageInfo = pckMan.getInstalledPackages(0);
        Gson gson = new Gson();

        int i = 0;
        for (PackageInfo pInfo : packageInfo) {

            HashMap<String, Object> item = new HashMap<String, Object>();

            // item.put("appimage", pInfo.applicationInfo.loadIcon(pckMan));
            item.put("packageName", pInfo.packageName);
            item.put("versionCode", pInfo.versionCode);
            item.put("versionName", pInfo.versionName);
            item.put("appName", pInfo.applicationInfo.loadLabel(pckMan).toString());

            System.out.println(pInfo.packageName + "==" + pInfo.versionCode + "==" + pInfo.versionName + "==" + pInfo.applicationInfo.loadLabel(pckMan).toString());

            items.add(item);
            i++;
            if (i == 2) {
                break;
            }

        }


        data.put("datalist", items);
        String msg = gson.toJson(data);
        Log.i("need", msg);
        return msg;
    }

    public void requestInfo(View view) {
        String jsonMsg = getInfo();
        post("http://103.39.214.173:7980/rehomelist", jsonMsg);
    }
}