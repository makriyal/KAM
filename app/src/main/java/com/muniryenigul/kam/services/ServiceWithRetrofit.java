package com.muniryenigul.kam.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.muniryenigul.kam.interfaces.ApiService;
import static com.muniryenigul.kam.activities.ShowMoreActivity.pages;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class ServiceWithRetrofit extends IntentService {
    Bundle bundle = new Bundle();
    ResultReceiver resultReceiver;
    ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
    HashMap<String, String> map = null;
    int count= 0,all;
    public ServiceWithRetrofit() {
        super("ServiceWithRetrofit");
    }
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            resultReceiver = intent.getParcelableExtra("receiver");
            try { (new Retrofit.Builder().addConverterFactory(ScalarsConverterFactory.create())
                    .baseUrl("https://www.bkmkitap.com/").client(new OkHttpClient().newBuilder().build()).build())
                    .create(ApiService.class).getPrices(intent.getStringExtra("link")).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                Document doc = Jsoup.parse(response.body());
                                String s = doc.select("#pager-wrapper > div > div").text();
                                int lastIndexOf = s.lastIndexOf(" ");
                                if (!s.substring(lastIndexOf + 1).isEmpty()) pages = Integer.parseInt(s.substring(lastIndexOf + 1));
                                else pages=0;
                                for (Element table : doc.select("div.fl.col-12.catalogWrapper")) {
                                    all = table.select("div.col.col-2.col-md-4.col-sm-6.col-xs-6.p-right.mb.productItem.zoom.ease").size();
                                    for (Element row : table.select("div.col.col-2.col-md-4.col-sm-6.col-xs-6.p-right.mb.productItem.zoom.ease")) {
                                        map = new HashMap<>();
                                        if (!map.containsValue(row.select("a.fl.col-12.text-description.detailLink").text()) && !map.containsValue(row.select("#productModelText").text()) && !map.containsValue(row.select("a.col.col-12.text-title.mt").text())) {
                                            map.put("name", row.select("a.fl.col-12.text-description.detailLink").text());
                                            map.put("author", row.select("#productModelText").text());
                                            map.put("publisher", row.select("a.col.col-12.text-title.mt").text());
                                            map.put("cover", row.select("div:nth-child(1) > a > span > img").attr("src"));
                                            map.put("individual", "https://www.bkmkitap.com" + row.select("a.fl.col-12.text-description.detailLink").attr("href"));
                                            arrayList.add(map);
                                        }
                                        checkSug(" ");
                                    }
                                }
                                if (map == null) checkSug("notFound");
                            } else checkSug("error");
                        }
                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) { checkSug("error"); }
                    });
            } catch (Exception e) {
                checkSug("error");
                e.printStackTrace();
            }
        } else checkSug("error");
    }
    private void checkSug(String what) {
        switch (what) {
            case " ":
                count += 1;
                if (count == all) {
                    bundle.putSerializable("arraylist", arrayList);
                    resultReceiver.send(1, bundle);
                }
                break;
            case "notFound":
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("name", "¯\\_(ツ)_/¯"+" : Bulunamadı");
                hashMap.put("author", "¯\\_(ツ)_/¯"+" : Bulunamadı");
                hashMap.put("publisher", "¯\\_(ツ)_/¯"+" : Bulunamadı");
                arrayList.add(hashMap);
                bundle.putSerializable("arraylist", arrayList);
                resultReceiver.send(1, bundle);
                break;
            case "error":
                HashMap<String, String> hashMap2 = new HashMap<>();
                hashMap2.put("name", "ಠ_ಠ : Response Error");
                hashMap2.put("author", "ಠ_ಠ : Response Error");
                hashMap2.put("publisher", "ಠ_ಠ : Response Error");
                arrayList.add(hashMap2);
                bundle.putSerializable("arraylist", arrayList);
                resultReceiver.send(1, bundle);
                break;
        }
    }
}
