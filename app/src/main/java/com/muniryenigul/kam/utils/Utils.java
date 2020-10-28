package com.muniryenigul.kam.utils;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteStatement;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.muniryenigul.kam.MainActivity;
import com.muniryenigul.kam.R;
import com.muniryenigul.kam.ers.BookFinalAdapter;
import com.muniryenigul.kam.interfaces.ApiService;
//import com.muniryenigul.kam.services.ControlJobService;

import org.apache.commons.lang3.StringUtils;
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
//import static com.muniryenigul.kam.MainActivity.isLoadMoreFinished;
import static android.content.Context.MODE_PRIVATE;
public class Utils {

    private static AlertDialog dialog;
    private static int /*search=1, */all, count, pages;
    private static HashMap<String, String> mapInfoFinal = null;
    private static ArrayList<HashMap<String, String>> arrayListSpare = new ArrayList<>();

    public static void bringValues(Context context,String searchURL, String mQuery,
                                   String from,
                                   BookFinalAdapter adapter, BookFinalAdapter adapter2,
                                   BookFinalAdapter adapter3,
                                   ArrayList<HashMap<String, String>> arrayList,
                                   ArrayList<String> publisherFilterList,
                                   ArrayList<String> publisherIDsList,
                                   ArrayList<String> authorFilterList,
                                   ArrayList<String> authorIDsList,
                                   HorizontalScrollView horizontalScrollView, View filterLine,
                                   ArrayList<String> searchedList, ProgressBar progressBar, int search) {
        Log.d("searchURL",searchURL);
        if(from.equals("load")) {
            Log.d("from","load");
            HashMap<String, String> mapLoad = new HashMap<>();
            mapLoad.put("type", "load");
            arrayList.add(mapLoad);
            adapter.notifyItemInserted(arrayList.size() - 1);
        } else progressBar.setVisibility(View.VISIBLE);
        //arrayList.clear();
        publisherFilterList.clear();
        publisherIDsList.clear();
        authorFilterList.clear();
        authorIDsList.clear();
        all = 0;
        count = 0;
        mapInfoFinal = null;
        pages = 0;
        try { (new Retrofit.Builder().addConverterFactory(ScalarsConverterFactory.create())
                .baseUrl("https://www.halkkitabevi.com/").client(new OkHttpClient().newBuilder().build()).build())
                .create(ApiService.class).getPrices(searchURL).enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Document doc = Jsoup.parse(response.body());
                            //String s = doc.select("#pager-wrapper > div > div").text();
                            String s = doc.select("a.button.button_pager.button_pager_last").attr("href");
                            if(s == null || s.isEmpty()) Log.d("s is empty","true");
                            else Log.d("last",s);
                            try {
                                if(s != null || !s.isEmpty()) {
                                    int start = StringUtils.indexOf(s,"page");
                                    int end;
                                    if(s.contains("prpm")) {
                                        end = StringUtils.indexOf(s,"&prpm");
                                        pages = Integer.parseInt(StringUtils.replace(StringUtils.substring(s,start,end),"page=",""));
                                    }  else pages = Integer.parseInt(StringUtils.replace(StringUtils.substring(s,start),"page=",""));
                                }
                            } catch (NumberFormatException e) {
                                pages = search;
                                e.printStackTrace();
                            }
                            Log.d("pages","" + pages);
                            Log.d("search","" + search);
                            if(doc.select("div.no_product_found").text().contains("bulunamadı")) {
                                Toast.makeText(context, " ¯\\_(ツ)_/¯ :  Hiçbir sonuç bulunamadı. ISBN ile aramayı deneyebilirsiniz.", Toast.LENGTH_LONG).show();
                                progressBar.setVisibility(View.GONE);
                            } else {
                                if(pages == 0 || pages == search) {
                                    Log.d("pages","== 0");
                                    adapter.setMoreDataAvailable(false);
                                }
                                else {
                                    Log.d("pages","!= 0");
                                    adapter.setMoreDataAvailable(true);
                                }
                                if (mQuery != null && mQuery.matches("\\d+(?:\\.\\d+)?")) {
                                    Log.d("mQuery","digit");
                                    mapInfoFinal = new HashMap<>();
                                    mapInfoFinal.put("name", doc.select("h1.contentHeader.prdHeader").text());
                                    mapInfoFinal.put("author", doc.select("div.prd_brand_box > a.writer > span").text());
                                    mapInfoFinal.put("publisher", doc.select("div.prd_brand_box > a.publisher > span").text());
                                    mapInfoFinal.put("cover", doc.select("#main_img").attr("src"));
                                    mapInfoFinal.put("individual", searchURL);
                                    arrayList.add(mapInfoFinal);
                                    check("digit", adapter, adapter2, adapter3, horizontalScrollView,filterLine, progressBar);
                                } else {
                                    Log.d("bringValuesFromBKMFinal","5");
                                    for (Element table : doc.select("#search_filter_form > ul > li:nth-child(1) > ul")) {
                                        //Log.d("tablepublisher",table.toString());
                                        for (Element row : table.select("input[class=filters_checkbox]"
                                                /*"#search_filter_form > ul > li:nth-child(1) > ul > li"*/)) {
                                            //Log.d("rowpublisher",row.toString());
                                            publisherIDsList.add(row.select("input").attr("name"));
                                        }
                                    }
                                    for (Element table : doc.select("#search_filter_form > ul > li:nth-child(3) > ul")) {
                                        for (Element row : table.select("input[class=filters_checkbox]")) {
                                            authorIDsList.add(row.select("input").attr("name"));
                                        }
                                    }
                                    for (Element table : doc.select("#search_filter_form > ul > li:nth-child(1) > ul"))
                                        for (Element row : table.select("li")) publisherFilterList.add(row.select("label").text());

                                    for (Element table : doc.select("#search_filter_form > ul > li:nth-child(3) > ul"))
                                        for (Element row : table.select("li")) authorFilterList.add(row.select("label").text());

//                                    Log.d("publisherFilterList",publisherFilterList.toString());
//                                    Log.d("authorFilterList",authorFilterList.toString());
//                                    Log.d("publisherIDsList",publisherIDsList.toString());
//                                    Log.d("authorIDsList",authorIDsList.toString());
                                    for (Element table : doc.select("div.prd_list_container_box")) {
                                        all = table.select("div.prd_list_container_box > div > ul > li").size();
                                        for (Element row : table.select("div.prd_list_container_box > div > ul > li")) {
                                            mapInfoFinal = new HashMap<>();
                                            if (!mapInfoFinal.containsValue(row.select("div.name").text())
                                                    && !mapInfoFinal.containsValue(row.select("div.writer").text())
                                                    && !mapInfoFinal.containsValue(row.select("div.publisher").text())) {
                                                mapInfoFinal.put("name", row.select("div.name").text());
                                                mapInfoFinal.put("author", row.select("div.writer").text());
                                                mapInfoFinal.put("publisher", row.select("div.publisher").text());
                                                mapInfoFinal.put("cover", row.select("div.image_container > div > a > img")
                                                        .attr("data-src"));
                                                mapInfoFinal.put("individual", row.select("div.image_container > div > a").attr("href"));
                                                if(from.equals("load")) {
                                                    Log.d("arrayListSpare","arrayListSpare");
                                                    arrayListSpare.add(mapInfoFinal);
                                                }
                                                else {
                                                    Log.d("arrayList","arrayList");
                                                    arrayList.add(mapInfoFinal);
                                                }
                                            }
                                            check(from, adapter, adapter2, adapter3, horizontalScrollView,filterLine, progressBar);
                                        }
                                    }
                                    if(from.equals("load")) {
                                        arrayList.remove(arrayList.size() - 1);
                                        arrayList.addAll(arrayListSpare);
                                        check("load", adapter, adapter2, adapter3, horizontalScrollView,filterLine, progressBar);
                                        //check("load", bookFinalAdapter);
                                        //adapter.notifyDataChanged();
                                        arrayListSpare.clear();
                                        int start = searchURL.indexOf("page=");
                                        Log.d("start",""+start);
                                        Log.d("start",""+search/*StringUtils.substring(searchURL,start).replaceAll("\\D+","")*/);
                                        Log.d("start",String.valueOf(pages));

                                    }
                                    if (search /*StringUtils.substring(searchURL,start).replaceAll("\\D+","")*/ == pages) {
                                        Log.d("search","== pages");
                                        adapter.setMoreDataAvailable(false);
                                        //isLoadMoreFinished = true;
                                        Toast.makeText(context, "Tüm sonuçlar yüklendi.", Toast.LENGTH_LONG).show();
                                    } else {
                                        Log.d("search","!= pages");
                                        adapter.setMoreDataAvailable(true);
                                        //isLoadMoreFinished = false;
                                    }
                                }
                                if (mapInfoFinal == null) {
                                    Log.d("bringValuesFromBKMFinal","6");
                                    Toast.makeText(context, "¯\\_(ツ)_/¯ :  Hiçbir sonuç bulunamadı. ISBN ile aramayı deneyebilirsiniz.", Toast.LENGTH_LONG).show();
                                    progressBar.setVisibility(View.GONE);
                                }
                            }
                        } else {
                            Log.d("bringValuesFromBKMFinal","7");
                            Toast.makeText(context, "ಠ_ಠ : Lütfen internet bağlantınızı kontrol ediniz ve yeniden deneyiniz.", Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        Log.d("bringValuesFromBKMFinal","8");
                        Toast.makeText(context, "ಠ_ಠ : Lütfen internet bağlantınızı kontrol ediniz ve yeniden deneyiniz.", Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.GONE);
                    }
                });
        } catch (Exception e) {
            Log.d("bringValuesFromBKMFinal","9");
            Toast.makeText(context, "ಠ_ಠ : Lütfen internet bağlantınızı kontrol ediniz ve yeniden deneyiniz.", Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.GONE);
            e.printStackTrace();
        }
    }

    private static void check(String from, BookFinalAdapter adapter,
                              BookFinalAdapter adapter2, BookFinalAdapter adapter3,
                              HorizontalScrollView horizontalScrollView,
                              View filterLine, ProgressBar progressBar) {
        switch (from) {
            case "load":
            case "text":
                count += 1;
                if (count == all) {
                    filterLine.setVisibility(View.VISIBLE);
                    horizontalScrollView.setVisibility(View.VISIBLE);
                    adapter.notifyDataChanged();
                    adapter2.notifyDataChanged();
                    adapter3.notifyDataChanged();
                    progressBar.setVisibility(View.GONE);
                }
                break;
            default:
                adapter.notifyDataChanged();
                progressBar.setVisibility(View.GONE);
                break;
        }
    }

    public static void noInternet(Activity activity) {
        @SuppressLint("InflateParams")
        View alertLayout = activity.getLayoutInflater().inflate(R.layout.custom_click, null);
        final LinearLayout buttons_lay = alertLayout.findViewById(R.id.buttons_lay);
        buttons_lay.setVisibility(View.VISIBLE);
        final Button buttonConnect = alertLayout.findViewById(R.id.buttonSeeOrSearch);
        buttonConnect.setVisibility(View.VISIBLE);
        final TextView textView = alertLayout.findViewById(R.id.textView);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        textView.setText(R.string.no_internet);
        buttonConnect.setText(R.string.connect);
        builder.setCancelable(true).setView(alertLayout);
        dialog = builder.create();
        dialog.show();
        buttonConnect.setOnClickListener(v -> {
            dialog.dismiss();
            activity.startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
        });
    }

    public static class BroadcastingInnerClass extends BroadcastReceiver {
        boolean connected = false;
//        private Dialog dialog;
        private AdRequest adRequest;
        private AdView mAdView;

        public BroadcastingInnerClass(/*Dialog dialog, */AdRequest adRequest, AdView mAdView) {
//            this.dialog = dialog;
            this.adRequest = adRequest;
            this.mAdView = mAdView;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            isNetworkAvailable(context);
        }
        public boolean isNetworkAvailable(Context context) {
            ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity != null) {
                NetworkInfo[] info = connectivity.getAllNetworkInfo();
                for (NetworkInfo anInfo : info) {
                    if (anInfo.getState() == NetworkInfo.State.CONNECTED) {
                        if (!connected) {
                            connected = true;
                            if (dialog != null && dialog.isShowing()) dialog.dismiss();
                            if (!mAdView.isShown()) mAdView.loadAd(adRequest);
                        }
                        return true;
                    } /*else if(anInfo.getState() == NetworkInfo.State.DISCONNECTED) {
                        connected = false;
                        if (dialog != null && !dialog.isShowing()) dialog.show();
                        return false;
                    }
                    Log.d("info", anInfo.getExtraInfo());*/
                }
            }
            connected = false;
            return false;
        }
    }
}
