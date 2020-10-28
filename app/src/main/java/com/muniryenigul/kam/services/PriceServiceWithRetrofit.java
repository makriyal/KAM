package com.muniryenigul.kam.services;
import org.apache.commons.lang3.StringUtils;
import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.ResultReceiver;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import com.muniryenigul.kam.R;
import com.muniryenigul.kam.interfaces.ApiService;
import com.muniryenigul.kam.models.HowMuchAndWhere;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import okhttp3.OkHttpClient;
import okhttp3.internal.platform.Platform;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import android.util.Log;
import android.widget.Toast;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import static okhttp3.internal.platform.Platform.INFO;
import static com.muniryenigul.kam.activities.PriceActivity.stillSearch;
public class PriceServiceWithRetrofit extends IntentService {
    private StringBuilder logs;
    String code, name = null, publisher = null, author = null, coverBig = null, isbn = null, individual = null, description = null, volume = null, pages = null;
    int count = 0, favIndex = -1;
    int all;
    private Bundle bundle = new Bundle();
    private Document doc;
    private ResultReceiver resultReceiver;
    private ArrayList<HowMuchAndWhere> arrayListPrice = new ArrayList<>();
    public PriceServiceWithRetrofit() {
        super("PriceServiceWithRetrofit");
    }
    public static void initializeSSLContext(Context mContext){
        try {
            SSLContext.getInstance("TLSv1.2");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        /*try {
            ProviderInstaller.installIfNeeded(mContext.getApplicationContext());
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }*/
}
    private void freeMemory() {
        System.runFinalization();
        Runtime.getRuntime().gc();
        System.gc();
    }
    @Override
    public void onHandleIntent(@Nullable Intent intent) {
        try {
            initializeSSLContext(getApplicationContext());
            ArrayList<String> selections = null;
            if (intent != null) {
                if (intent.getStringExtra("favIndex") != null) favIndex = Integer.parseInt(intent.getStringExtra("favIndex"));
                code = intent.getStringExtra("code");
                pages = intent.getStringExtra("pages");
                volume = intent.getStringExtra("volume");
                individual = intent.getStringExtra("individual");
                description = intent.getStringExtra("description");
                coverBig = intent.getStringExtra("coverBig");
                isbn = intent.getStringExtra("isbn");
                publisher = intent.getStringExtra("publisher");
                author = intent.getStringExtra("author");
                name = intent.getStringExtra("name");
                selections = intent.getStringArrayListExtra("selections");
                Log.d("selections",selections.toString());
                resultReceiver = intent.getParcelableExtra("receiver");
            }
            String notificationTitle;
            /*if (intent != null && intent.getStringExtra("from").equals("CJS")) {
                notificationTitle = "Favori kitapların fiyatları kontrol ediliyor...";
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    String NOTIFICATION_CHANNEL_ID = "1250012";
                    String channelName = PriceServiceWithRetrofit.class.getSimpleName();
                    NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_DEFAULT);
                    chan.setLightColor(getResources().getColor(R.color.colorPrimaryDark));
                    chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
                    NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    assert manager != null;
                    manager.createNotificationChannel(chan);
                    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
                    Notification notification = notificationBuilder.setOngoing(true)
                            .setSmallIcon(R.drawable.notification)
                            .setContentTitle(notificationTitle)
                            .setPriority(NotificationManager.IMPORTANCE_MIN)
                            .setCategory(Notification.CATEGORY_SERVICE)
                            .build();
                    startForeground(2, notification);
                } else startForeground(1, new Notification());
            }*/
            Retrofit.Builder builder = new Retrofit.Builder().addConverterFactory(ScalarsConverterFactory.create());
            Retrofit retrofit;
            ApiService apiService;
            Call<String> stringCall;
            final String URL39 /*kitapstore, *//*URL25_1*//*bulut,*//*tele1*/;
            /*if (publisher.contains(" ")) {
                if (publisher.contains(".")) {
                    StringUtils.replace(publisher, ".", "");
                    //tele1 = StringUtils.join("https://www.tele1kitap.com/arama/", StringUtils.replace(name, " ", "%20"), "%20", author.replaceAll(" ", "%20"), "%20", publisher.substring(0, publisher.indexOf(" ") - 1), "%20", isbn, "/");
                    //URL25_1 = StringUtils.join("https://www.kitapaloku.com/arama/", name, " ", author, " ", publisher.substring(0, publisher.indexOf(" ") - 1), isbn, "/");
                    //kitapstore = StringUtils.join("https://www.kitapstore.com/arama/", name, " ", author, " ", publisher.substring(0, publisher.indexOf(" ") - 1), " ", isbn, "/");
                    URL39 = StringUtils.join("https://www.kitapyurdu.com/index.php?route=product/search&filter_name=", name, " ", author, " ", publisher.substring(0, publisher.indexOf(" ")));
                } else {
                    //tele1 = StringUtils.join("https://www.tele1kitap.com/arama/", name.replaceAll(" ", "%20"), "%20", author.replaceAll(" ", "%20"), "%20", publisher.substring(0, publisher.indexOf(" ")), "%20", isbn, "/");
                    //URL25_1 = StringUtils.join("https://www.kitapaloku.com/arama/", name, " ", author, " ", publisher.substring(0, publisher.indexOf(" ")), " ", isbn, "/");
                    //kitapstore = StringUtils.join("https://www.kitapstore.com/arama/", name, " ", author, " ", publisher.substring(0, publisher.indexOf(" ")), " ", isbn, "/");
                    URL39 = StringUtils.join("https://www.kitapyurdu.com/index.php?route=product/search&filter_name=", name, " ", author, " ", publisher.substring(0, publisher.indexOf(" ")));
                }
            } else {
                //tele1 = StringUtils.join("https://www.tele1kitap.com/arama/", name.replaceAll(" ", "%20"), "%20", author.replaceAll(" ", "%20"), "%20", publisher, "%20", isbn, "/");
                //URL25_1 = StringUtils.join("https://www.kitapaloku.com/arama/", name, " ", author, " ", publisher, " ", isbn, "/");
                //kitapstore = StringUtils.join("https://www.kitapstore.com/arama/", name, " ", author, " ", publisher, " ", isbn, "/");
                URL39 = StringUtils.join("https://www.kitapyurdu.com/index.php?route=product/search&filter_name=", name, " ", author, " ", publisher);
            }*/
            all = getResources().getStringArray(R.array.listOptions).length;
            arrayListPrice.clear();
            /*if (stillSearch && selections != null && selections.contains("www.kitapyurdu.com")) {
                try {
                    String finalIsbn = isbn;
                    builder.baseUrl("https://www.kitapyurdu.com/").build().create(ApiService.class).getPrices(URL39).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    boolean contains = false;
                                    doc = Jsoup.parse(response.body());
                                    for (Element table : doc.select("#product-table")) {
                                        for (Element row : table.select("div[itemtype=http://schema.org/Book]")) {
                                            if (!row.select("div.product-info").text().contains("Sponsorlu") &&
                                                    StringUtils.substring(row.select("div.product-info").text(), 0, 10).equals(StringUtils.substring(finalIsbn, 3)) &&
                                                    !arrayListPrice.contains("kitapyurdu")) {
                                                if (row.select("div.price-new > span.value").text().isEmpty())
                                                    arrayListPrice.add(new HowMuchAndWhere("Kitapyurdu", StringUtils.join(row.select("div.price > span > span.value").text(), " TL"), StringUtils.replace(StringUtils.replace(URL39, "www", "m"), "product", "products")*//*URL39.replace("www", "m").replace("product", "products")*//*));
                                                else
                                                    arrayListPrice.add(new HowMuchAndWhere("Kitapyurdu", StringUtils.join(row.select("div.price-new > span.value").text(), " TL"), StringUtils.replace(StringUtils.replace(URL39, "www", "m"), "product", "products")*//*URL39.replace("www", "m").replace("product", "products")*//*));
                                                check();
                                                contains = true;
                                            }
                                        }
                                    }
                                    if (!contains) {
                                        arrayListPrice.add(new HowMuchAndWhere("Kitapyurdu", "¯\\_(ツ)_/¯", StringUtils.replace(StringUtils.replace(URL39, "www", "m"), "product", "products")*//*URL39.replace("www", "m").replace("product", "products")*//*));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("Kitapyurdu", "ಠ_ಠ", URL39));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("Kitapyurdu", "ಠ_ಠ", StringUtils.replace(StringUtils.replace(URL39, "www", "m"), "product", "products")*//*URL39.replace("www", "m").replace("product", "products")*//*));
                                check();
                            }
                        }
                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("Kitapyurdu", "ಠ_ಠ", StringUtils.replace(StringUtils.replace(URL39, "www", "m"), "product", "products")*//*URL39.replace("www", "m").replace("product", "products")*//*));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("Kitapyurdu", "ಠ_ಠ", StringUtils.replace(StringUtils.replace(URL39, "www", "m"), "product", "products")*//*URL39.replace("www", "m").replace("product", "products")*//*));
                    check();
                    e.printStackTrace();
                }
            } else {
                arrayListPrice.add(new HowMuchAndWhere("Kitapyurdu", "□", ""));
                check();
            }*/
            if (stillSearch && selections != null && selections.contains("www.kitapyurdu.com")) {
                String kitapyurdu = StringUtils.join("https://www.google.com/search?q=kitapyurdu+", isbn);
                String kitapyurduNull = StringUtils.join("https://www.kitapyurdu.com/index.php?route=product/search&filter_name=", isbn);
                Log.d("kitapyurdu",kitapyurdu +"\n"+kitapyurduNull);
                try {
                    final HttpLoggingInterceptor logging = new HttpLoggingInterceptor(message -> {
                        Platform.get().log(message,INFO, null);
                        logs = LogsUtil.readLogs();
                    });
                    logging.setLevel(HttpLoggingInterceptor.Level.BODY);
                    final OkHttpClient okHttpClientRob = new OkHttpClient()
                            .newBuilder()
                            .addInterceptor(logging)
                            .build();
                    retrofit = new Retrofit.Builder().addConverterFactory(ScalarsConverterFactory.create()).baseUrl("https://www.google.com/").client(okHttpClientRob).build();
                    apiService = retrofit.create(ApiService.class);
                    stringCall = apiService.getPrices(kitapyurdu);
                    stringCall.enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    String kitapyurdu2 = StringUtils.substringBetween(response.body(), "<a href=\"/url?q=", /*"\"><div class=\""*/"&amp");
                                    Log.d("kitapyurdu2",kitapyurdu2);
                                    if(!kitapyurdu2.contains("https://www.kitapyurdu.com/")) {
                                        arrayListPrice.add(new HowMuchAndWhere("Kitapyurdu", "¯\\_(ツ)_/¯", kitapyurduNull));
                                        check();
                                    } else {
                                        builder.baseUrl("https://www.kitapyurdu.com/").build().create(ApiService.class).getPrices(kitapyurdu2).enqueue(new Callback<String>() {
                                            @Override
                                            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                                                if (response.isSuccessful() && response.body() != null) {
                                                    try {
                                                        doc = Jsoup.parse(response.body());
                                                        String str = doc.select("div.big-ribbon-container-yellow > div > span").first().text();
                                                        if (doc.select("div.box.no-padding > div").text().contains("bulunamadı") ||
                                                                doc.select("div.additional-info > div.preparation").text().contains("yok") || str.isEmpty()) {
                                                            arrayListPrice.add(new HowMuchAndWhere("Kitapyurdu", "¯\\_(ツ)_/¯", kitapyurdu2));
                                                            check();
                                                        } else {
                                                            arrayListPrice.add(new HowMuchAndWhere("Kitapyurdu", StringUtils.join(str," TL"), kitapyurdu2));
                                                            check();
                                                        }
                                                    } catch (Exception e) {
                                                        arrayListPrice.add(new HowMuchAndWhere("Kitapyurdu", "¯\\_(ツ)_/¯", kitapyurdu2));
                                                        check();
                                                        e.printStackTrace();
                                                    }
                                                } else {
                                                    arrayListPrice.add(new HowMuchAndWhere("Kitapyurdu", "¯\\_(ツ)_/¯", kitapyurdu2));
                                                    check();
                                                }
                                            }
                                            @Override
                                            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                                                arrayListPrice.add(new HowMuchAndWhere("Kitapyurdu", "¯\\_(ツ)_/¯", kitapyurdu2));
                                                check();
                                            }
                                        });
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("Kitapyurdu", "¯\\_(ツ)_/¯", kitapyurduNull));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("Kitapyurdu", "¯\\_(ツ)_/¯", kitapyurduNull));
                                check();
                            }
                        }
                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("Kitapyurdu", "¯\\_(ツ)_/¯", kitapyurduNull));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("Kitapyurdu", "¯\\_(ツ)_/¯", kitapyurduNull));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("Kitapyurdu", "□", ""));
                check();
            }
            if (stillSearch && selections != null && selections.contains("www.rob389.com")) {
                String URL56 = StringUtils.join("https://www.rob389.com/default.asp?PAG00_CODE=ASETR&SER00_CODE=HZL01&SER01_CODE=11H,12H,13H,16H&SEARCH=GO&SPARAM16=YAZAR_ORDER,MMM00_TITLE&SPARAM17=ASC&SPARAM20=", isbn, "&SPARAM7=%25");
                try {
                    final HttpLoggingInterceptor logging = new HttpLoggingInterceptor(message -> {
                        //Platform.get().log(INFO, message, null);
                        Platform.get().log(message,INFO, null);
                        logs = LogsUtil.readLogs();
                    });
                    logging.setLevel(HttpLoggingInterceptor.Level.BODY);
                    final OkHttpClient okHttpClientRob = new OkHttpClient()
                            .newBuilder()
                            .addInterceptor(logging)
                            .build();
                    retrofit = new Retrofit.Builder().addConverterFactory(ScalarsConverterFactory.create()).baseUrl("https://www.rob389.com/").client(okHttpClientRob).build();
                    apiService = retrofit.create(ApiService.class);
                    stringCall = apiService.getPrices(URL56);
//                (new Retrofit.Builder().addConverterFactory(ScalarsConverterFactory.create())
//                        .baseUrl("https://www.rob389.com/").client(okHttpClientRob).build())
//                        .create(ApiService.class).getPrices(URL56)
                    stringCall.enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    if (logs.toString().contains("tOBJ.DATA[0].SAL_PRICE")) {
                                        int x1 = logs.indexOf("tOBJ.DATA[0].SAL_PRICE=");
                                        int x2 = logs.indexOf("tOBJ.DATA[0].LNK_PREFIX=");
                                        int xQ = logs.indexOf("tOBJ.DATA[0].Q=");
                                        if (StringUtils.substring(String.valueOf(logs), xQ, xQ + 19).contains("\"0\"")) {
                                            arrayListPrice.add(new HowMuchAndWhere("rob389", "¯\\_(ツ)_/¯", URL56));
                                            check();
                                        } else {
                                            String s = StringUtils.replaceAll(StringUtils.replaceAll(StringUtils.replace(StringUtils.substring(String.valueOf(logs), x1, x2), "tOBJ.DATA[0].SAL_PRICE=", ""), "\\\"", ""), ";", "");
                                            if (s.isEmpty()) {
                                                arrayListPrice.add(new HowMuchAndWhere("rob389", "¯\\_(ツ)_/¯", URL56));
                                                check();
                                            } else {
                                                int h = s.length() - 1 - s.indexOf(".");
                                                if (!s.contains(".")) {
                                                    arrayListPrice.add(new HowMuchAndWhere("rob389", StringUtils.join(s, " TL"), URL56));
                                                    check();
                                                } else if (h == 2) {
                                                    arrayListPrice.add(new HowMuchAndWhere("rob389", StringUtils.join(StringUtils.replace(s, ".", ","), " TL"), URL56));
                                                    check();
                                                } else if (h == 1) {
                                                    arrayListPrice.add(new HowMuchAndWhere("rob389", StringUtils.join(StringUtils.replace(s, ".", ","), "0 TL"), URL56));
                                                    check();
                                                } else {
                                                    arrayListPrice.add(new HowMuchAndWhere("rob389", "¯\\_(ツ)_/¯", URL56));
                                                    check();
                                                }
                                            }
                                        }
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("rob389", "¯\\_(ツ)_/¯", URL56));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("rob389", "¯\\_(ツ)_/¯", URL56));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("rob389", "ಠ_ಠ", URL56));
                                check();
                            }
                        }
                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("rob389", "ಠ_ಠ", URL56));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("rob389", "ಠ_ಠ", URL56));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("rob389", "□", ""));
                check();
            }
            if (stillSearch && selections != null && selections.contains("www.dilekkitap.com")) {
                String dilek = StringUtils.join("https://www.dilekkitap.com/arama?tip=1&word=", isbn,"&kat=0&submit=Ara");
                try {
                    builder.baseUrl("https://www.dilekkitap.com/").build().create(ApiService.class).getPrices(dilek).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String str = doc.select("div.urun_kdvdahil_fiyati").first().text();
                                    if (doc.select("div.urun_stok_yok > span").text().contains("bitti") ||doc.select("div.urunler_main > div > div > div").text().contains("bulunamad") || str.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("DİLEKKİTAP", "¯\\_(ツ)_/¯", dilek));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("DİLEKKİTAP", StringUtils.replace(str,"KDV Dahil ",""), dilek));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("DİLEKKİTAP", "¯\\_(ツ)_/¯", dilek));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("DİLEKKİTAP", "¯\\_(ツ)_/¯", dilek));
                                check();
                            }
                        }
                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("DİLEKKİTAP", "ಠ_ಠ", dilek));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("DİLEKKİTAP", "ಠ_ಠ", dilek));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("DİLEKKİTAP", "□", ""));
                check();
            }
/////////////////////////07.03.2020///////////////////
            if (stillSearch && selections != null && selections.contains("www.atlaskitap.com")) {
                String atlas = StringUtils.join("https://www.google.com/search?q=atlaskitap+", isbn);
                String atlasNull = StringUtils.join("https://www.atlaskitap.com/arama?q=", isbn);
                try {
                    final HttpLoggingInterceptor logging = new HttpLoggingInterceptor(message -> {
                        Platform.get().log(message,INFO, null);
                        logs = LogsUtil.readLogs();
                    });
                    logging.setLevel(HttpLoggingInterceptor.Level.BODY);
                    final OkHttpClient okHttpClientRob = new OkHttpClient()
                            .newBuilder()
                            .addInterceptor(logging)
                            .build();
                    retrofit = new Retrofit.Builder().addConverterFactory(ScalarsConverterFactory.create()).baseUrl("https://www.google.com/").client(okHttpClientRob).build();
                    apiService = retrofit.create(ApiService.class);
                    stringCall = apiService.getPrices(atlas);
                    stringCall.enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    String atlas2 = StringUtils.substringBetween(response.body(), "<a href=\"/url?q=", "&amp");
                                    if(!atlas2.contains("https://www.atlaskitap.com/")) {
                                        arrayListPrice.add(new HowMuchAndWhere("atlaskitap", "¯\\_(ツ)_/¯", atlasNull));
                                        check();
                                    } else {
                                        builder.baseUrl("https://www.atlaskitap.com/").build().create(ApiService.class).getPrices(atlas2).enqueue(new Callback<String>() {
                                            @Override
                                            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                                                if (response.isSuccessful() && response.body() != null) {
                                                    try {
                                                        doc = Jsoup.parse(response.body());
                                                        String str = doc.select("span.nobel_i_ifiyat").first().text();
                                                        if (doc.select("a[class=nobel_item_spt nobel_btn_norm5 tukendiDugme]").text().contains("Tükendi") || str.isEmpty()) {
                                                            arrayListPrice.add(new HowMuchAndWhere("atlaskitap", "¯\\_(ツ)_/¯", atlas2));
                                                            check();
                                                        } else {
                                                            arrayListPrice.add(new HowMuchAndWhere("atlaskitap", str, atlas2));
                                                            check();
                                                        }
                                                    } catch (Exception e) {
                                                        arrayListPrice.add(new HowMuchAndWhere("atlaskitap", "¯\\_(ツ)_/¯", atlas2));
                                                        check();
                                                        e.printStackTrace();
                                                    }
                                                } else {
                                                    arrayListPrice.add(new HowMuchAndWhere("atlaskitap", "¯\\_(ツ)_/¯", atlas2));
                                                    check();
                                                }
                                            }
                                            @Override
                                            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                                                arrayListPrice.add(new HowMuchAndWhere("atlaskitap", "¯\\_(ツ)_/¯", atlas2));
                                                check();
                                            }
                                        });
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("atlaskitap", "¯\\_(ツ)_/¯", atlasNull));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("atlaskitap", "¯\\_(ツ)_/¯", atlasNull));
                                check();
                            }
                        }
                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("atlaskitap", "¯\\_(ツ)_/¯", atlasNull));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("atlaskitap", "¯\\_(ツ)_/¯", atlasNull));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("atlaskitap", "□", ""));
                check();
            }
            if (stillSearch && selections != null && selections.contains("www.isemkitap.com")) {
                String isem = StringUtils.join("https://www.google.com/search?q=isemkitap+", isbn);
                String isemNull = StringUtils.join("https://www.isemkitap.com/arama?s=", isbn);
                try {
                    final HttpLoggingInterceptor logging = new HttpLoggingInterceptor(message -> {
                        Platform.get().log(message,INFO, null);
                        logs = LogsUtil.readLogs();
                    });
                    logging.setLevel(HttpLoggingInterceptor.Level.BODY);
                    final OkHttpClient okHttpClientRob = new OkHttpClient()
                            .newBuilder()
                            .addInterceptor(logging)
                            .build();
                    retrofit = new Retrofit.Builder().addConverterFactory(ScalarsConverterFactory.create()).baseUrl("https://www.google.com/").client(okHttpClientRob).build();
                    apiService = retrofit.create(ApiService.class);
                    stringCall = apiService.getPrices(isem);
                    stringCall.enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    String isem2 = StringUtils.substringBetween(response.body(), "<a href=\"/url?q=", "&amp");
                                    if(!isem2.contains("https://www.isemkitap.com/")) {
                                        arrayListPrice.add(new HowMuchAndWhere("isemkitap", "¯\\_(ツ)_/¯", isemNull));
                                        check();
                                    } else {
                                        builder.baseUrl("https://www.isemkitap.com/").build().create(ApiService.class).getPrices(isem2).enqueue(new Callback<String>() {
                                            @Override
                                            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                                                if (response.isSuccessful() && response.body() != null) {
                                                    try {
                                                        doc = Jsoup.parse(response.body());
                                                        String str = doc.select("div.indirim.TL").first().text();
                                                        if (doc.select("#Panel2 > input").attr("value").contains("YOK") || str.isEmpty()) {
                                                            arrayListPrice.add(new HowMuchAndWhere("isemkitap", "¯\\_(ツ)_/¯", isem2));
                                                            check();
                                                        } else {
                                                            arrayListPrice.add(new HowMuchAndWhere("isemkitap", StringUtils.join(str," TL"), isem2));
                                                            check();
                                                        }
                                                    } catch (Exception e) {
                                                        arrayListPrice.add(new HowMuchAndWhere("isemkitap", "¯\\_(ツ)_/¯", isem2));
                                                        check();
                                                        e.printStackTrace();
                                                    }
                                                } else {
                                                    arrayListPrice.add(new HowMuchAndWhere("isemkitap", "¯\\_(ツ)_/¯", isem2));
                                                    check();
                                                }
                                            }
                                            @Override
                                            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                                                arrayListPrice.add(new HowMuchAndWhere("isemkitap", "¯\\_(ツ)_/¯", isem2));
                                                check();
                                            }
                                        });
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("isemkitap", "¯\\_(ツ)_/¯", isemNull));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("isemkitap", "¯\\_(ツ)_/¯", isemNull));
                                check();
                            }
                        }
                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("isemkitap", "¯\\_(ツ)_/¯", isemNull));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("isemkitap", "¯\\_(ツ)_/¯", isemNull));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("isemkitap", "□", ""));
                check();
            }
            if (stillSearch && selections != null && selections.contains("www.kitapsepeti.com")) {
                String okuoku = StringUtils.join("https://www.google.com/search?q=kitapsepeti+", isbn);
                String okuokuNull = StringUtils.join("https://www.kitapsepeti.com/arama/?filter=", isbn);
                try {
                    final HttpLoggingInterceptor logging = new HttpLoggingInterceptor(message -> {
                        Platform.get().log(message,INFO, null);
                        logs = LogsUtil.readLogs();
                    });
                    logging.setLevel(HttpLoggingInterceptor.Level.BODY);
                    final OkHttpClient okHttpClientRob = new OkHttpClient()
                            .newBuilder()
                            .addInterceptor(logging)
                            .build();
                    retrofit = new Retrofit.Builder().addConverterFactory(ScalarsConverterFactory.create()).baseUrl("https://www.google.com/").client(okHttpClientRob).build();
                    apiService = retrofit.create(ApiService.class);
                    stringCall = apiService.getPrices(okuoku);
                    stringCall.enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    String okuoku2 = StringUtils.substringBetween(response.body(), "<a href=\"/url?q=", "&amp");
                                    if(!okuoku2.contains("https://www.kitapsepeti.com/")) {
                                        arrayListPrice.add(new HowMuchAndWhere("kitapsepeti", "¯\\_(ツ)_/¯", okuokuNull));
                                        check();
                                    } else {
                                        builder.baseUrl("https://www.kitapsepeti.com/").build().create(ApiService.class).getPrices(okuoku2).enqueue(new Callback<String>() {
                                            @Override
                                            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                                                if (response.isSuccessful() && response.body() != null) {
                                                    try {
                                                        doc = Jsoup.parse(response.body());
                                                        String str = doc.select("div.price.clearfix > p.new").first().text();
                                                        if (doc.select("div.addcartout > span").text().contains("yok") || doc.select("div.additional-info > div > span").text().contains("YOK") || doc.select("#booklistempty > p:nth-child(1)").text().contains("Bulunamadı")|| doc.select("#divProductsEmpty > h1").text().contains("bulunamadı") || str.isEmpty()) {
                                                            arrayListPrice.add(new HowMuchAndWhere("kitapsepeti", "¯\\_(ツ)_/¯", okuoku2));
                                                            check();
                                                        } else {
                                                            arrayListPrice.add(new HowMuchAndWhere("kitapsepeti", str, okuoku2));
                                                            check();
                                                        }
                                                    } catch (Exception e) {
                                                        arrayListPrice.add(new HowMuchAndWhere("kitapsepeti", "¯\\_(ツ)_/¯", okuoku2));
                                                        check();
                                                        e.printStackTrace();
                                                    }
                                                } else {
                                                    arrayListPrice.add(new HowMuchAndWhere("kitapsepeti", "¯\\_(ツ)_/¯", okuoku2));
                                                    check();
                                                }
                                            }
                                            @Override
                                            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                                                arrayListPrice.add(new HowMuchAndWhere("kitapsepeti", "¯\\_(ツ)_/¯", okuoku2));
                                                check();
                                            }
                                        });
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("kitapsepeti", "¯\\_(ツ)_/¯", okuokuNull));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("kitapsepeti", "¯\\_(ツ)_/¯", okuokuNull));
                                check();
                            }
                        }
                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("kitapsepeti", "¯\\_(ツ)_/¯", okuokuNull));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("kitapsepeti", "¯\\_(ツ)_/¯", okuokuNull));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("kitapsepeti", "□", ""));
                check();
            }
            if (stillSearch && selections != null && selections.contains("www.kitapbulut.com")) {
                String bulut = StringUtils.join("https://www.google.com/search?q=kitapbulut+", StringUtils.substring(isbn, 3));
                String bulutNull = StringUtils.join("https://www.kitapbulut.com/arama/", StringUtils.substring(isbn, 3));
                try {
                    final HttpLoggingInterceptor logging = new HttpLoggingInterceptor(message -> {
                        Platform.get().log(message,INFO, null);
                        logs = LogsUtil.readLogs();
                    });
                    logging.setLevel(HttpLoggingInterceptor.Level.BODY);
                    final OkHttpClient okHttpClientRob = new OkHttpClient()
                            .newBuilder()
                            .addInterceptor(logging)
                            .build();
                    retrofit = new Retrofit.Builder().addConverterFactory(ScalarsConverterFactory.create()).baseUrl("https://www.google.com/").client(okHttpClientRob).build();
                    apiService = retrofit.create(ApiService.class);
                    stringCall = apiService.getPrices(bulut);
                    stringCall.enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    String bulut2 = StringUtils.substringBetween(response.body(), "<a href=\"/url?q=", "\"><div class=\"");
                                    if(!bulut2.contains("https://www.kitapbulut.com/")) {
                                        arrayListPrice.add(new HowMuchAndWhere("kitap bulut", "¯\\_(ツ)_/¯", bulutNull));
                                        check();
                                    } else {
                                        builder.baseUrl("https://www.kitapbulut.com/").build().create(ApiService.class).getPrices(bulut2).enqueue(new Callback<String>() {
                                            @Override
                                            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                                                if (response.isSuccessful() && response.body() != null) {
                                                    try {
                                                        doc = Jsoup.parse(response.body());
                                                        String str = doc.select("span.item-price").first().text();
                                                        if (doc.select("div.additional-info > div > span").text().contains("SATIŞ YOK") || str.isEmpty()) {
                                                            arrayListPrice.add(new HowMuchAndWhere("kitap bulut", "¯\\_(ツ)_/¯", bulut2));
                                                            check();
                                                        } else {
                                                            arrayListPrice.add(new HowMuchAndWhere("kitap bulut", str, bulut2));
                                                            check();
                                                        }
                                                    } catch (Exception e) {
                                                        arrayListPrice.add(new HowMuchAndWhere("kitap bulut", "¯\\_(ツ)_/¯", bulut2));
                                                        check();
                                                        e.printStackTrace();
                                                    }
                                                } else {
                                                    arrayListPrice.add(new HowMuchAndWhere("kitap bulut", "¯\\_(ツ)_/¯", bulut2));
                                                    check();
                                                }
                                            }
                                            @Override
                                            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                                                arrayListPrice.add(new HowMuchAndWhere("kitap bulut", "¯\\_(ツ)_/¯", bulut2));
                                                check();
                                            }
                                        });
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("kitap bulut", "¯\\_(ツ)_/¯", bulutNull));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("kitap bulut", "¯\\_(ツ)_/¯", bulutNull));
                                check();
                            }
                        }
                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("kitap bulut", "¯\\_(ツ)_/¯", bulutNull));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("kitap bulut", "¯\\_(ツ)_/¯", bulutNull));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("kitap bulut", "□", ""));
                check();
            }
            if (stillSearch && selections != null && selections.contains("www.kitap365.com")) {
                String kitap365 = StringUtils.join("https://www.google.com/search?q=kitap365+", isbn);
                String kitap365Null = StringUtils.join("https://www.kitap365.com/arama/?filter=", isbn);
                Log.d("",kitap365 +"\n"+kitap365Null);
                try {
                    final HttpLoggingInterceptor logging = new HttpLoggingInterceptor(message -> {
                        Platform.get().log(message,INFO, null);
                        logs = LogsUtil.readLogs();
                    });
                    logging.setLevel(HttpLoggingInterceptor.Level.BODY);
                    final OkHttpClient okHttpClientRob = new OkHttpClient()
                            .newBuilder()
                            .addInterceptor(logging)
                            .build();
                    retrofit = new Retrofit.Builder().addConverterFactory(ScalarsConverterFactory.create()).baseUrl("https://www.google.com/").client(okHttpClientRob).build();
                    apiService = retrofit.create(ApiService.class);
                    stringCall = apiService.getPrices(kitap365);
                    stringCall.enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    String kitap3652 = StringUtils.substringBetween(response.body(), "<a href=\"/url?q=", /*"\"><div class=\""*/"&amp");
                                    Log.d("kitap3652",kitap3652);
                                    if(!kitap3652.contains("https://www.kitap365.com/")) {
                                        arrayListPrice.add(new HowMuchAndWhere("kitap365", "¯\\_(ツ)_/¯", kitap365Null));
                                        check();
                                    } else {
                                        builder.baseUrl("https://www.kitap365.com/").build().create(ApiService.class).getPrices(kitap3652).enqueue(new Callback<String>() {
                                            @Override
                                            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                                                if (response.isSuccessful() && response.body() != null) {
                                                    try {
                                                        doc = Jsoup.parse(response.body());
                                                        String str = doc.select("p.pdprice").first().text();
                                                        if (doc.select("#booklistempty > p:nth-child(2)").text().contains("Bulunamadı") ||
                                                                doc.select("a.btn.btn-basket3").text().contains("yok") || str.isEmpty()) {
                                                            arrayListPrice.add(new HowMuchAndWhere("kitap365", "¯\\_(ツ)_/¯", kitap3652));
                                                            check();
                                                        } else {
                                                            arrayListPrice.add(new HowMuchAndWhere("kitap365", StringUtils.join(StringUtils.substring(str,0,str.indexOf("TL")),"TL"), kitap3652));
                                                            check();
                                                        }
                                                    } catch (Exception e) {
                                                        arrayListPrice.add(new HowMuchAndWhere("kitap365", "¯\\_(ツ)_/¯", kitap3652));
                                                        check();
                                                        e.printStackTrace();
                                                    }
                                                } else {
                                                    arrayListPrice.add(new HowMuchAndWhere("kitap365", "¯\\_(ツ)_/¯", kitap3652));
                                                    check();
                                                }
                                            }
                                            @Override
                                            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                                                arrayListPrice.add(new HowMuchAndWhere("kitap365", "¯\\_(ツ)_/¯", kitap3652));
                                                check();
                                            }
                                        });
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("kitap365", "¯\\_(ツ)_/¯", kitap365Null));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("kitap365", "¯\\_(ツ)_/¯", kitap365Null));
                                check();
                            }
                        }
                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("kitap365", "¯\\_(ツ)_/¯", kitap365Null));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("kitap365", "¯\\_(ツ)_/¯", kitap365Null));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("kitap365", "□", ""));
                check();
            }
            if (stillSearch && selections != null && selections.contains("www.istanbulkitapcisi.com")) {
                String istanbul = StringUtils.join("https://www.istanbulkitapcisi.com/arama?q=", isbn);
                try {
                    builder.baseUrl("https://www.istanbulkitapcisi.com/").build().create(ApiService.class).getPrices(istanbul).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String str = doc.select("div.product-price").first().text();
                                    if (doc.select("div.product-info > span").text().contains("Tükendi") ||doc.select("#main > div > div > h4").text().contains("bulunamadı") || str.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("İSTANBUL KİTAPÇISI", "¯\\_(ツ)_/¯", istanbul));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("İSTANBUL KİTAPÇISI", StringUtils.join(StringUtils.substringBetween(str, " ","TL"), "TL"), istanbul));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("İSTANBUL KİTAPÇISI", "¯\\_(ツ)_/¯", istanbul));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("İSTANBUL KİTAPÇISI", "¯\\_(ツ)_/¯", istanbul));
                                check();
                            }
                        }
                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("İSTANBUL KİTAPÇISI", "ಠ_ಠ", istanbul));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("İSTANBUL KİTAPÇISI", "ಠ_ಠ", istanbul));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("İSTANBUL KİTAPÇISI", "□", ""));
                check();
            }
            if (stillSearch && selections != null && selections.contains("www.kitapmuptelasi.com.tr")) {
                String muptela = StringUtils.join("https://www.kitapmuptelasi.com.tr/Arama?1&kelime=", isbn);
                try {
                    builder.baseUrl("https://www.kitapmuptelasi.com.tr/").build().create(ApiService.class).getPrices(muptela).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String str = doc.select("div.discountPrice").first().text();
                                    if (doc.select("a.TukendiIco > span").text().contains("Tükendi") ||
                                            doc.select("#divUrunYok > img").attr("src").contains("yok") || str.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("Kitap Müptelası", "¯\\_(ツ)_/¯", muptela));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("Kitap Müptelası", StringUtils.join(StringUtils.replace(str, "₺", ""), " TL"), muptela));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("Kitap Müptelası", "¯\\_(ツ)_/¯", muptela));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("Kitap Müptelası", "¯\\_(ツ)_/¯", muptela));
                                check();
                            }
                        }
                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("Kitap Müptelası", "ಠ_ಠ", muptela));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("Kitap Müptelası", "ಠ_ಠ", muptela));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("Kitap Müptelası", "□", ""));
                check();
            }
/////////////////////////////////////09.02.2020/////////////////////////////
            if (stillSearch && selections != null && selections.contains("www.adakitap.com.tr")) {
                String adakitap = StringUtils.join("https://www.adakitap.com.tr/arama?Keyword=", isbn);
                try {
                    builder.baseUrl("https://www.adakitap.com.tr/").build().create(ApiService.class).getPrices(adakitap).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String str = doc.select(/*"div.price > div.last-price"22.04.2020*/"div.discount-price").first().text();
                                    if (doc.select("div.row > div > ul > div > img").attr("src").contains("not-found") || str.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("ADA", "¯\\_(ツ)_/¯", adakitap));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("ADA", StringUtils.join(str, " TL"), adakitap));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("ADA", "¯\\_(ツ)_/¯", adakitap));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("ADA", "¯\\_(ツ)_/¯", adakitap));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("ADA", "ಠ_ಠ", adakitap));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("ADA", "ಠ_ಠ", adakitap));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("ADA", "□", ""));
                check();
            }
            if (stillSearch && selections != null && selections.contains("www.yesilkoala.com")) {
                String yesilkoala = StringUtils.join("https://www.yesilkoala.com/index.php?route=product/search&search=", isbn);
                try {
                    builder.baseUrl("https://www.yesilkoala.com/").build().create(ApiService.class).getPrices(yesilkoala).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String str = doc.select("div.right > div.price > span.price-new").first().text();
                                    if (doc.select("div.col-sm-6.text-right").text().isEmpty() || str.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("yeşil KOALA", "¯\\_(ツ)_/¯", yesilkoala));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("yeşil KOALA", StringUtils.join(StringUtils.replace(str, "₺", ""), "TL"), yesilkoala));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("yeşil KOALA", "¯\\_(ツ)_/¯", yesilkoala));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("yeşil KOALA", "¯\\_(ツ)_/¯", yesilkoala));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("yeşil KOALA", "ಠ_ಠ", yesilkoala));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("yeşil KOALA", "ಠ_ಠ", yesilkoala));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("yeşil KOALA", "□", ""));
                check();
            }
            if (stillSearch && selections != null && selections.contains("www.ilahiyatvakfi.com")) {
                String ilahiyat = StringUtils.join("https://www.ilahiyatvakfi.com/product/search?q=", isbn);
                try { builder.baseUrl("https://www.ilahiyatvakfi.com/").build().create(ApiService.class).getPrices(ilahiyat).enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                doc = Jsoup.parse(response.body());
                                String str = doc.select("span.urun-item-fiyat-1").first().text();
                                if (doc.select("#sf-resetcontent > h1").text().contains("Whoops") || doc.select("div.urun-item-button > span").text().contains("Tükendi") || str.isEmpty()) {
                                    arrayListPrice.add(new HowMuchAndWhere("İLAHİYAT VAKFI", "¯\\_(ツ)_/¯", ilahiyat));
                                    check();
                                } else {
                                    int ixDot = str.indexOf(".");
                                    int ix_ = str.lastIndexOf(" ");
                                    switch (ix_ - ixDot) {
                                        case 2:
                                            arrayListPrice.add(new HowMuchAndWhere("İLAHİYAT VAKFI", StringUtils.replace(StringUtils.replace(str, " TL.", "0 TL"), ".", ","), ilahiyat));
                                            check();
                                            break;
                                        case 3:
                                            arrayListPrice.add(new HowMuchAndWhere("İLAHİYAT VAKFI", StringUtils.replace(StringUtils.replace(str, "TL.", "TL"), ".", ","), ilahiyat));
                                            check();
                                            break;
                                        default:
                                            arrayListPrice.add(new HowMuchAndWhere("İLAHİYAT VAKFI", StringUtils.replace(str, " TL.", ",00 TL"), ilahiyat));
                                            check();
                                            break;
                                    }
                                }
                            } catch (Exception e) {
                                arrayListPrice.add(new HowMuchAndWhere("İLAHİYAT VAKFI", "¯\\_(ツ)_/¯", ilahiyat));
                                check();
                                e.printStackTrace();
                            }
                        } else {
                            arrayListPrice.add(new HowMuchAndWhere("İLAHİYAT VAKFI", "¯\\_(ツ)_/¯", ilahiyat));
                            check();
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        arrayListPrice.add(new HowMuchAndWhere("İLAHİYAT VAKFI", "ಠ_ಠ", ilahiyat));
                        check();
                    }
                });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("İLAHİYAT VAKFI", "ಠ_ಠ", ilahiyat));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("İLAHİYAT VAKFI", "□", ""));
                check();
            }
            if (stillSearch && selections != null && selections.contains("www.tdk.com.tr")) {
                String tdk = StringUtils.join("https://www.tdk.com.tr/index.php?p=search&search=", isbn);
                try { builder.baseUrl("https://www.tdk.com.tr/").build().create(ApiService.class).getPrices(tdk).enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                doc = Jsoup.parse(response.body());
                                String str = doc.select("div.listingPriceNormal").first().text();
                                if (doc.select("div.container > span").text().contains("bulunamadı") ||
                                        doc.select("div.listingHover > div.listingBasket").text().contains("tükendi") ||
                                        str.isEmpty()) {
                                    arrayListPrice.add(new HowMuchAndWhere("TDK Bilim", "¯\\_(ツ)_/¯", tdk));
                                    check();
                                } else {
                                    arrayListPrice.add(new HowMuchAndWhere("TDK Bilim", StringUtils.replace(StringUtils.replace(str, " +KDV", ""), ".", ","), tdk));
                                    check();
                                }
                            } catch (Exception e) {
                                arrayListPrice.add(new HowMuchAndWhere("TDK Bilim", "¯\\_(ツ)_/¯", tdk));
                                check();
                                e.printStackTrace();
                            }
                        } else {
                            arrayListPrice.add(new HowMuchAndWhere("TDK Bilim", "ಠ_ಠ", tdk));
                            check();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        arrayListPrice.add(new HowMuchAndWhere("TDK Bilim", "ಠ_ಠ", tdk));
                        check();
                    }
                });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("TDK Bilim", "ಠ_ಠ", tdk));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("TDK Bilim", "□", ""));
                check();
            }
            //////////////////////////////////////12.05.2019
            if (stillSearch && selections != null && selections.contains("www.egitimkitap.com")) {
                String egitim = StringUtils.join("https://www.egitimkitap.com/index.php?p=search&search=+", isbn);
                try { builder.baseUrl("https://www.egitimkitap.com/").build().create(ApiService.class).getPrices(egitim).enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                doc = Jsoup.parse(response.body());
                                String str = doc.select("span.divdiscountprice").first().text();
                                if (doc.select("div.searchnotfound > span").text().contains("bulunamadı") ||
                                        doc.select("div.vBottomSpt > div.vSpt").text().contains("yoktur") ||
                                        str.isEmpty()) {
                                    arrayListPrice.add(new HowMuchAndWhere("eğitim kitap", "¯\\_(ツ)_/¯", egitim));
                                    check();
                                } else {
                                    arrayListPrice.add(new HowMuchAndWhere("eğitim kitap", StringUtils.replace(str, ".", ","), egitim));
                                    check();
                                }
                            } catch (Exception e) {
                                arrayListPrice.add(new HowMuchAndWhere("eğitim kitap", "¯\\_(ツ)_/¯", egitim));
                                check();
                                e.printStackTrace();
                            }
                        } else {
                            arrayListPrice.add(new HowMuchAndWhere("eğitim kitap", "ಠ_ಠ", egitim));
                            check();
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        arrayListPrice.add(new HowMuchAndWhere("eğitim kitap", "ಠ_ಠ", egitim));
                        check();
                    }
                });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("eğitim kitap", "ಠ_ಠ", egitim));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("eğitim kitap", "□", ""));
                check();
            }
            if (stillSearch && selections != null && selections.contains("www.kpsskitap.net")) {
                String kpsskitap = StringUtils.join("https://www.kpsskitap.net/arama/", isbn);
                try { builder.baseUrl("https://www.kpsskitap.net/").build().create(ApiService.class).getPrices(kpsskitap).enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                doc = Jsoup.parse(response.body());
                                String str = doc.select("div.showcasePriceOne").first().text();
                                if (doc.select("div.alertContent > div").text().contains("bulunamamıştır") ||
                                        doc.select("img.borderNone.globalNoStockButton").attr("src").contains("nostock") ||
                                        str.isEmpty()) {
                                    arrayListPrice.add(new HowMuchAndWhere("kpsskitap", "¯\\_(ツ)_/¯", kpsskitap));
                                    check();
                                } else {
                                    arrayListPrice.add(new HowMuchAndWhere("kpsskitap", str, kpsskitap));
                                    check();
                                }
                            } catch (Exception e) {
                                arrayListPrice.add(new HowMuchAndWhere("kpsskitap", "¯\\_(ツ)_/¯", kpsskitap));
                                check();
                                e.printStackTrace();
                            }
                        } else {
                            arrayListPrice.add(new HowMuchAndWhere("kpsskitap", "ಠ_ಠ", kpsskitap));
                            check();
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        arrayListPrice.add(new HowMuchAndWhere("kpsskitap", "ಠ_ಠ", kpsskitap));
                        check();
                    }
                });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("kpsskitap", "ಠ_ಠ", kpsskitap));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("kpsskitap", "□", ""));
                check();
            }

            //////////////////////////////////////07.05.2019
            if (stillSearch && selections != null && selections.contains("www.kitapkolik.com")) {
                String kitapkolik = StringUtils.join("https://www.kitapkolik.com/Arama?1&kelime=", isbn);
                try { builder.baseUrl("https://www.kitapkolik.com/").build().create(ApiService.class).getPrices(kitapkolik).enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                doc = Jsoup.parse(response.body());
                                String str = doc.select("div.discountPrice").first().text();
                                if (doc.select("#divUrunYok > img").attr("src").contains("urunyok") ||
                                        doc.select("a.TukendiIco > span").text().contains("Tükendi") ||
                                        str.isEmpty()) {
                                    arrayListPrice.add(new HowMuchAndWhere("KİTAPKOLİK", "¯\\_(ツ)_/¯", kitapkolik));
                                    check();
                                } else {
                                    arrayListPrice.add(new HowMuchAndWhere("KİTAPKOLİK", StringUtils.join(StringUtils.replace(str, "₺", ""), " TL"), kitapkolik));
                                    check();
                                }
                            } catch (Exception e) {
                                arrayListPrice.add(new HowMuchAndWhere("KİTAPKOLİK", "¯\\_(ツ)_/¯", kitapkolik));
                                check();
                                e.printStackTrace();
                            }
                        } else {
                            arrayListPrice.add(new HowMuchAndWhere("KİTAPKOLİK", "ಠ_ಠ", kitapkolik));
                            check();
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        arrayListPrice.add(new HowMuchAndWhere("KİTAPKOLİK", "ಠ_ಠ", kitapkolik));
                        check();
                    }
                });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("KİTAPKOLİK", "ಠ_ಠ", kitapkolik));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("KİTAPKOLİK", "□", ""));
                check();
            }
            if (stillSearch && selections != null && selections.contains("www.legalkitabevi.com")) {
                String legal = StringUtils.join("https://www.legalkitabevi.com/index.php?p=Products&q=", isbn);
                try { builder.baseUrl("https://www.legalkitabevi.com/").build().create(ApiService.class).getPrices(legal).enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                doc = Jsoup.parse(response.body());
                                String str = doc.select("#prd_final_price_display").first().text();
                                if (doc.select("#page_message > div > p").text().contains("bulunamadı") ||
                                        doc.select("div.prd_no_sell").text().contains("yok") ||
                                        str.isEmpty()) {
                                    arrayListPrice.add(new HowMuchAndWhere("legalkitabevi", "¯\\_(ツ)_/¯", legal));
                                    check();
                                } else {
                                    arrayListPrice.add(new HowMuchAndWhere("legalkitabevi", StringUtils.join(str, " TL"), legal));
                                    check();
                                }
                            } catch (Exception e) {
                                arrayListPrice.add(new HowMuchAndWhere("legalkitabevi", "¯\\_(ツ)_/¯", legal));
                                check();
                                e.printStackTrace();
                            }
                        } else {
                            arrayListPrice.add(new HowMuchAndWhere("legalkitabevi", "ಠ_ಠ", legal));
                            check();
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        arrayListPrice.add(new HowMuchAndWhere("legalkitabevi", "ಠ_ಠ", legal));
                        check();
                    }
                });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("legalkitabevi", "ಠ_ಠ", legal));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("legalkitabevi", "□", ""));
                check();
            }
            //////////////////////////////////////18.04.2019
            if (stillSearch && selections != null && selections.contains("www.simetrikitap.com")) {
                String simetri = StringUtils.join("https://www.simetrikitap.com/arama/", isbn);
                try { builder.baseUrl("https://www.simetrikitap.com/").build().create(ApiService.class).getPrices(simetri).enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                doc = Jsoup.parse(response.body());
                                String str = doc.select("div.showcasePriceOne").first().text();
                                if (doc.select("div.alertContent > div").text().contains("bulunamamıştır") ||
                                        doc.select("a.soldOutBadge").text().equals("TÜKENDİ") ||
                                        str.isEmpty()) {
                                    arrayListPrice.add(new HowMuchAndWhere("simetrikitap", "¯\\_(ツ)_/¯", simetri));
                                    check();
                                } else {
                                    arrayListPrice.add(new HowMuchAndWhere("simetrikitap", str, simetri));
                                    check();
                                }
                            } catch (Exception e) {
                                arrayListPrice.add(new HowMuchAndWhere("simetrikitap", "¯\\_(ツ)_/¯", simetri));
                                check();
                                e.printStackTrace();
                            }
                        } else {
                            arrayListPrice.add(new HowMuchAndWhere("simetrikitap", "ಠ_ಠ", simetri));
                            check();
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        arrayListPrice.add(new HowMuchAndWhere("simetrikitap", "ಠ_ಠ", simetri));
                        check();
                    }
                });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("simetrikitap", "ಠ_ಠ", simetri));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("simetrikitap", "□", ""));
                check();
            }
            if (stillSearch && selections != null && selections.contains("www.hipokratkitabevi.com")) {
                String hipokrat = StringUtils.join("https://www.hipokratkitabevi.com/index.php?p=Products&q=", isbn);
                try { builder.baseUrl("https://www.hipokratkitabevi.com/").build().create(ApiService.class).getPrices(hipokrat).enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                doc = Jsoup.parse(response.body());
                                String str = doc.select("#prd_final_price_display").first().text();
                                if (doc.select("div.no_product_found > div:nth-child(1)").text().contains("bulunamadı") ||
                                        doc.select("div.prd_no_sell").text().contains("yok") ||
                                        str.isEmpty() || str.equals("0,00TL")) {
                                    arrayListPrice.add(new HowMuchAndWhere("Hipokrat Kitabevi", "¯\\_(ツ)_/¯", hipokrat));
                                    check();
                                } else {
                                    arrayListPrice.add(new HowMuchAndWhere("Hipokrat Kitabevi", StringUtils.replace(str, "TL", " TL"), hipokrat));
                                    check();
                                }
                            } catch (Exception e) {
                                arrayListPrice.add(new HowMuchAndWhere("Hipokrat Kitabevi", "¯\\_(ツ)_/¯", hipokrat));
                                check();
                                e.printStackTrace();
                            }
                        } else {
                            arrayListPrice.add(new HowMuchAndWhere("Hipokrat Kitabevi", "ಠ_ಠ", hipokrat));
                            check();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        arrayListPrice.add(new HowMuchAndWhere("Hipokrat Kitabevi", "ಠ_ಠ", hipokrat));
                        check();
                    }
                });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("Hipokrat Kitabevi", "ಠ_ಠ", hipokrat));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("Hipokrat Kitabevi", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.tercihkitabevi.com")) {
                String tercih = StringUtils.join("https://www.tercihkitabevi.com/arama?tip=1&word=", isbn);
                try { builder.baseUrl("https://www.tercihkitabevi.com/").build().create(ApiService.class).getPrices(tercih).enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                doc = Jsoup.parse(response.body());
                                String str = doc.select("div.urun_kdvdahil_fiyati").first().text();
                                if (doc.select("div.urunler_main > div > div > div").text().contains("bulunamad") ||
                                        doc.select("div.urun_stok_yok > span").text().contains("edilememektedir") ||
                                        str.isEmpty()) {
                                    arrayListPrice.add(new HowMuchAndWhere("TERCİH KİTABEVİ", "¯\\_(ツ)_/¯", tercih));
                                    check();
                                } else {
                                    arrayListPrice.add(new HowMuchAndWhere("TERCİH KİTABEVİ", StringUtils.replace(str, "KDV Dahil ", ""), tercih));
                                    check();
                                }
                            } catch (Exception e) {
                                arrayListPrice.add(new HowMuchAndWhere("TERCİH KİTABEVİ", "¯\\_(ツ)_/¯", tercih));
                                check();
                                e.printStackTrace();
                            }
                        } else {
                            arrayListPrice.add(new HowMuchAndWhere("TERCİH KİTABEVİ", "ಠ_ಠ", tercih));
                            check();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        arrayListPrice.add(new HowMuchAndWhere("TERCİH KİTABEVİ", "ಠ_ಠ", tercih));
                        check();
                    }
                });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("TERCİH KİTABEVİ", "ಠ_ಠ", tercih));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("TERCİH KİTABEVİ", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.yardimcikitaplar.com")) {
                String yardimci = StringUtils.join("https://www.yardimcikitaplar.com/urunara?srchtxt=", isbn);
                try {
                    builder.baseUrl("https://www.yardimcikitaplar.com/").build().create(ApiService.class).getPrices(yardimci).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String str = doc.select("span.price-sales").first().text();
                                    if (doc.select("div.row.hidden-xs > div > div").text().contains("0 sonuç") ||
                                            doc.select("div.price.price-box > span").text().contains("Tükendi") ||
                                            str.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("YARDIMCI KİTAPLAR", "¯\\_(ツ)_/¯", yardimci));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("YARDIMCI KİTAPLAR", str, yardimci));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("YARDIMCI KİTAPLAR", "¯\\_(ツ)_/¯", yardimci));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("YARDIMCI KİTAPLAR", "ಠ_ಠ", yardimci));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("YARDIMCI KİTAPLAR", "ಠ_ಠ", yardimci));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("YARDIMCI KİTAPLAR", "ಠ_ಠ", yardimci));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("YARDIMCI KİTAPLAR", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.kitabevimiz.com")) {
                String kitabevimiz = StringUtils.join("https://www.kitabevimiz.com/index.php?p=Products&q=", isbn);
                try { builder.baseUrl("https://www.kitabevimiz.com/").build().create(ApiService.class).getPrices(kitabevimiz).enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                doc = Jsoup.parse(response.body());
                                String str = doc.select("#prd_final_price_display").first().text();
                                if (doc.select("div.no_product_found > div:nth-child(1)").text().contains("bulunamadı") ||
                                        doc.select("div.col3 > div > div.prd_no_sell").text().contains("yok") ||
                                        str.isEmpty()) {
                                    arrayListPrice.add(new HowMuchAndWhere("kitabevimiz", "¯\\_(ツ)_/¯", kitabevimiz));
                                    check();
                                } else if(StringUtils.contains(str,"TL")){
                                    arrayListPrice.add(new HowMuchAndWhere("kitabevimiz", StringUtils.replace(str, "TL", " TL"), kitabevimiz));
                                    check();
                                } else {
                                    arrayListPrice.add(new HowMuchAndWhere("kitabevimiz", StringUtils.join(str, " TL"), kitabevimiz));
                                    check();
                                }
                            } catch (Exception e) {
                                arrayListPrice.add(new HowMuchAndWhere("kitabevimiz", "¯\\_(ツ)_/¯", kitabevimiz));
                                check();
                                e.printStackTrace();
                            }
                        } else {
                            arrayListPrice.add(new HowMuchAndWhere("kitabevimiz", "ಠ_ಠ", kitabevimiz));
                            check();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        arrayListPrice.add(new HowMuchAndWhere("kitabevimiz", "ಠ_ಠ", kitabevimiz));
                        check();
                    }
                });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("kitabevimiz", "ಠ_ಠ", kitabevimiz));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("kitabevimiz", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.milletkitabevi.com")) {
                String millet = StringUtils.join("https://www.milletkitabevi.com/arama/", isbn, "/");
                try {
                    builder.baseUrl("https://www.milletkitabevi.com/").build().create(ApiService.class).getPrices(millet).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String str = doc.select("div.SFiyat > span").first().text();
                                    if (doc.select("div.IBaslik").text().contains("bulunamadı") ||
                                            doc.select("div.Bilgi > div.Durum").text().contains("Tükenmiş") ||
                                            doc.select("div.Bilgi > div.Durum").text().equals("Ürün Satış Dışı") ||
                                            doc.select("div.Bilgi > div.Durum").text().contains("Tükendi") ||
                                            str.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("millet kitabevi", "¯\\_(ツ)_/¯", millet));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("millet kitabevi", str, millet));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("millet kitabevi", "¯\\_(ツ)_/¯", millet));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("millet kitabevi", "ಠ_ಠ", millet));
                                check();
                            }
                        }
                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("millet kitabevi", "ಠ_ಠ", millet));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("millet kitabevi", "ಠ_ಠ", millet));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("millet kitabevi", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.alfakitap.com")) {
                String alfa = StringUtils.join("https://www.alfakitap.com/kitaplar.php?a=", isbn, "&n=arama");
                try {
//        retrofit=new Retrofit.Builder().addConverterFactory(ScalarsConverterFactory.create()).baseUrl("https://www.alfakitap.com/").build();
//        apiService=retrofit.create(ApiService.class);
//        stringCall = apiService.getPrices(alfa);
                    builder.baseUrl("https://www.alfakitap.com/").build().create(ApiService.class).getPrices(alfa).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String str = doc.select("div.hbook-last > div.hbook-price").first().text();
                                    if (doc.select("div.hbook-cargo").text().contains("Yok") ||
                                            !doc.select("a").attr("").contains("Arama") ||
                                            str.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("ALFA", "¯\\_(ツ)_/¯", alfa));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("ALFA", StringUtils.replace(str, "₺", "TL"), alfa));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("ALFA", "¯\\_(ツ)_/¯", alfa));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("ALFA", "ಠ_ಠ", alfa));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("ALFA", "ಠ_ಠ", alfa));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("ALFA", "ಠ_ಠ", alfa));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("ALFA", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.kitapsan.com.tr")) {
                String kitapsan = StringUtils.join("https://www.kitapsan.com.tr/arama?Keyword=", name, " ", author, " ", publisher, " ", isbn);
                try {
//        retrofit=new Retrofit.Builder().addConverterFactory(ScalarsConverterFactory.create()).baseUrl("https://www.kitapsan.com.tr/").build();
//        apiService=retrofit.create(ApiService.class);
//        stringCall = apiService.getPrices(kitapsan);
                    builder.baseUrl("https://www.kitapsan.com.tr/").build().create(ApiService.class).getPrices(kitapsan).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String str = doc.select("div.products > ul > li > div > div.detail > div.price > div.last-price").first().text();
                                    if (doc.select("div.products > div > p:nth-child(1) > a > img").attr("alt").contains("not-found") ||
                                            str.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("KİTAPSAN", "¯\\_(ツ)_/¯", kitapsan));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("KİTAPSAN", StringUtils.join(str, " TL"), kitapsan));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("KİTAPSAN", "¯\\_(ツ)_/¯", kitapsan));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("KİTAPSAN", "ಠ_ಠ", kitapsan));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("KİTAPSAN", "ಠ_ಠ", kitapsan));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("KİTAPSAN", "ಠ_ಠ", kitapsan));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("KİTAPSAN", "□", ""));
                check();
            }
            /////////////////////////////////////18.04.2019
            try {
                freeMemory();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (stillSearch && selections != null && selections.contains("www.eflatunkitap.com")) {
                String eflatun = StringUtils.join("https://www.eflatunkitap.com/index.php?p=Products&q_field_active=0&ctg_id=&q=", isbn, "&search=&q_field=");
                try {
                    builder.baseUrl("https://www.eflatunkitap.com/").build().create(ApiService.class).getPrices(eflatun).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String str = doc.select("#prd_final_price_display").first().text();
                                    if (doc.select("div.no_product_found > div:nth-child(1)").text().contains("bulunamadı") ||
                                            doc.select("div.prd_no_sell").text().contains("Yok") ||
                                            str.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("eflatun KİTAP", "¯\\_(ツ)_/¯", eflatun));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("eflatun KİTAP", StringUtils.replace(str, "TL", " TL"), eflatun));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("eflatun KİTAP", "¯\\_(ツ)_/¯", eflatun));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("eflatun KİTAP", "ಠ_ಠ", eflatun));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("eflatun KİTAP", "ಠ_ಠ", eflatun));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("eflatun KİTAP", "ಠ_ಠ", eflatun));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("eflatun KİTAP", "□", ""));
                check();
            }

//            if (stillSearch && selections != null && selections.contains("www.eminadimlar.com")) {
//                String emin = StringUtils.join("https://www.eminadimlar.com/ara.html?q=", isbn);
//                try {
//                    builder.baseUrl("https://www.eminadimlar.com/").build().create(ApiService.class).getPrices(emin).enqueue(new Callback<String>() {
//                        @Override
//                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
//                            if (response.isSuccessful() && response.body() != null) {
//                                try {
//                                    doc = Jsoup.parse(response.body());
//                                    String str = doc.select("p.discounted-price").first().text();
//                                    if (doc.select("div.col-md-6.col-sm-6 > p").text().contains("0 kayıt") ||
//                                            doc.select("a.btn.btn-main.add-basket").text().contains("Yok") ||
//                                            str.isEmpty()) {
//                                        arrayListPrice.add(new HowMuchAndWhere("emin", "¯\\_(ツ)_/¯", emin));
//                                        check();
//                                    } else {
//                                        arrayListPrice.add(new HowMuchAndWhere("emin", str, emin));
//                                        check();
//                                    }
//                                } catch (Exception e) {
//                                    arrayListPrice.add(new HowMuchAndWhere("emin", "¯\\_(ツ)_/¯", emin));
//                                    check();
//                                    e.printStackTrace();
//                                }
//                            } else {
//                                arrayListPrice.add(new HowMuchAndWhere("emin", "ಠ_ಠ", emin));
//                                check();
//                            }
//                        }
//
//                        @Override
//                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
//                            arrayListPrice.add(new HowMuchAndWhere("emin", "ಠ_ಠ", emin));
//                            check();
//                        }
//                    });
//                } catch (Exception e) {
//                    arrayListPrice.add(new HowMuchAndWhere("emin", "ಠ_ಠ", emin));
//                    check();
//                    e.printStackTrace();
//                }
//            } else {
//                arrayListPrice.add(new HowMuchAndWhere("emin", "□", ""));
//                check();
//            }

            if (stillSearch && selections != null && selections.contains("www.kitapalalim.com")) {
                String alalim = StringUtils.join("https://www.kitapalalim.com/Search.php?a=", isbn);
                try {
                    builder.baseUrl("https://www.kitapalalim.com/").build().create(ApiService.class).getPrices(alalim).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String str = doc.select("#table1 > tbody > tr:nth-child(5) > td > center > font.BlokTutar").first().text();
                                    if (doc.select("#Wrapper > table:nth-child(1) > tbody > tr:nth-child(2) > td > table > tbody > tr > td:nth-child(2) > table:nth-child(2)").text().isEmpty() ||
                                            doc.select("img[alt=Stokta yok]").attr("src").contains("stokyok") ||
                                            str.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("kitapalalim", "¯\\_(ツ)_/¯", alalim));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("kitapalalim", StringUtils.join(StringUtils.replace(str, ".", ","), " TL"), alalim));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("kitapalalim", "¯\\_(ツ)_/¯", alalim));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("kitapalalim", "¯\\_(ツ)_/¯", alalim));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("kitapalalim", "ಠ_ಠ", alalim));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("kitapalalim", "ಠ_ಠ", alalim));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("kitapalalim", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.camlicakitap.com")) {
                String camlica = StringUtils.join("https://www.camlicakitap.com/arama?q=", isbn);
                try {
                    builder.baseUrl("https://www.camlicakitap.com/").build().create(ApiService.class).getPrices(camlica).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String str = doc.select("div.currentPrice").first().text();
                                    if (doc.select("#katalog > div:nth-child(4) > div:nth-child(2) > div > div").text().contains("bulunamadı") ||
                                            doc.select("span.out-of-stock").text().contains("Yok") ||
                                            str.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("çamlıca kitap", "¯\\_(ツ)_/¯", camlica));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("çamlıca kitap", str, camlica));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("çamlıca kitap", "¯\\_(ツ)_/¯", camlica));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("çamlıca kitap", "ಠ_ಠ", camlica));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("çamlıca kitap", "ಠ_ಠ", camlica));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("çamlıca kitap", "ಠ_ಠ", camlica));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("çamlıca kitap", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.724kitapal.com")) {
                String kitapal = StringUtils.join("https://www.724kitapal.com/arama/", isbn);
                try {
                    builder.baseUrl("https://www.724kitapal.com/").build().create(ApiService.class).getPrices(kitapal).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String str = doc.select("div.showcasePriceTwo").first().text();
                                    if (doc.select("div.alertContent > div").text().contains("bulunamamıştır") ||
                                            doc.select("img.borderNone.globalNoStockButton").attr("src").contains("nostock") ||
                                            str.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("724kitapal", "¯\\_(ツ)_/¯", kitapal));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("724kitapal", str, kitapal));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("724kitapal", "¯\\_(ツ)_/¯", kitapal));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("724kitapal", "ಠ_ಠ", kitapal));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("724kitapal", "ಠ_ಠ", kitapal));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("724kitapal", "ಠ_ಠ", kitapal));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("724kitapal", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.hepsikitap.com")) {
                String hepsikitap = StringUtils.join("https://www.hepsikitap.com/arama/", isbn);
                try {
                    builder.baseUrl("https://www.hepsikitap.com/").build().create(ApiService.class).getPrices(hepsikitap).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String str = doc.select("div.showcasePrice > div._floatRight").first().text();
                                    if (doc.select("div.alertContent > div").text().contains("bulunamamıştır") ||
                                            doc.select("a.showcaseNoStock._positionAbsolute").attr("class").contains("NoStock") ||
                                            str.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("hepsikitap", "¯\\_(ツ)_/¯", hepsikitap));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("hepsikitap", str, hepsikitap));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("hepsikitap", "¯\\_(ツ)_/¯", hepsikitap));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("hepsikitap", "ಠ_ಠ", hepsikitap));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("hepsikitap", "ಠ_ಠ", hepsikitap));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("hepsikitap", "ಠ_ಠ", hepsikitap));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("hepsikitap", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.fatihkitap.com")) {
                String fatihkitap = StringUtils.join("https://www.fatihkitap.com/index.php?p=Products&q_field_active=0&ctg_id=&q=", isbn, "&search=&q_field=");
                try {
                    builder.baseUrl("https://www.fatihkitap.com/").build().create(ApiService.class).getPrices(fatihkitap).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String str = doc.select("#prd_final_price_display").first().text();
                                    if (doc.select("div.no_product_found > div:nth-child(1)").text().contains("bulunamadı") ||
                                            doc.select("div.prd_no_sell").text().equals("Stokta yok") ||
                                            str.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("fatihkitap", "¯\\_(ツ)_/¯", fatihkitap));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("fatihkitap", StringUtils.replace(str, "TL", " TL"), fatihkitap));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("fatihkitap", "¯\\_(ツ)_/¯", fatihkitap));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("fatihkitap", "ಠ_ಠ", fatihkitap));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("fatihkitap", "ಠ_ಠ", fatihkitap));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("fatihkitap", "ಠ_ಠ", fatihkitap));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("fatihkitap", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.yildizkitabevi.com")) {
                String yildiz = StringUtils.join("https://www.yildizkitabevi.com/Urunler?q=", isbn);
                try {
                    builder.baseUrl("https://www.yildizkitabevi.com/").build().create(ApiService.class).getPrices(yildiz).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String str = doc.select("body > div.wrapper > main > div > div > div.col-md-9.col-md-push-3.col-main > div.products.products-grid > ol > li > div > div > div.product-item-detail > div > div.product-item-price > span.price").first().text();
                                    if (doc.select("ul > li.active > a").text().isEmpty() ||
                                            doc.select("span.product-item-label.label-sale-off").text().equals("Tükendi") ||
                                            str.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("Yıldız Kitabevi", "¯\\_(ツ)_/¯", yildiz));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("Yıldız Kitabevi", str, yildiz));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("Yıldız Kitabevi", "¯\\_(ツ)_/¯", yildiz));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("Yıldız Kitabevi", "ಠ_ಠ", yildiz));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("Yıldız Kitabevi", "¯\\_(ツ)_/¯", yildiz));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("Yıldız Kitabevi", "ಠ_ಠ", yildiz));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("Yıldız Kitabevi", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.egitimmarket.com.tr")) {
                String egitimmarket = StringUtils.join("https://www.egitimmarket.com.tr/search/?q=", isbn);
                try {
                    builder.baseUrl("https://www.egitimmarket.com.tr/").build().create(ApiService.class).getPrices(egitimmarket).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String s = doc.select("div.row.productDetails > div").first().text();
                                    if (doc.select("#site-canvas > div > div > div > div > div > div.col.col-9.col-sm-12 > div > div:nth-child(2)").text().contains("eklenmemiş") ||
                                            doc.select("div.row.productDetails > div").text().isEmpty() ||
                                            s.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("EĞİTİM MARKET", "¯\\_(ツ)_/¯", egitimmarket));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("EĞİTİM MARKET", StringUtils.join(StringUtils.replace(StringUtils.substring(s, s.lastIndexOf(" ") + 1), "₺", ""), " TL"), egitimmarket));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("EĞİTİM MARKET", "¯\\_(ツ)_/¯", egitimmarket));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("EĞİTİM MARKET", "ಠ_ಠ", egitimmarket));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("EĞİTİM MARKET", "ಠ_ಠ", egitimmarket));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("EĞİTİM MARKET", "ಠ_ಠ", egitimmarket));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("EĞİTİM MARKET", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.banadakitap.com")) {
                String banada = StringUtils.join("https://www.banadakitap.com/arama/", isbn);
                try {
                    builder.baseUrl("https://www.banadakitap.com/").build().create(ApiService.class).getPrices(banada).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String str = doc.select("div.showcasePriceOne").first().text();
                                    if (doc.select("div.alertContent > div").text().contains("bulunamamıştır") ||
                                            doc.select("img.borderNone.globalNoStockButton").attr("src").contains("nostock") ||
                                            str.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("Banada Kitap", "¯\\_(ツ)_/¯", banada));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("Banada Kitap", str, banada));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("Banada Kitap", "¯\\_(ツ)_/¯", banada));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("Banada Kitap", "ಠ_ಠ", banada));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("Banada Kitap", "ಠ_ಠ", banada));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("Banada Kitap", "ಠ_ಠ", banada));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("Banada Kitap", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.indekskitap.com")) {
                String indeks = StringUtils.join("https://www.indekskitap.com/arama/", isbn);
                try {
                    builder.baseUrl("https://www.indekskitap.com/").build().create(ApiService.class).getPrices(indeks).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String str = doc.select("div.showcasePrice > div:nth-child(2)").first().text();
                                    if (doc.select("div.alertContent > div").text().contains("bulunamamıştır") ||
                                            doc.select("img.borderNone.globalNoStockButton").attr("src").contains("nostock") ||
                                            str.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("İNDEKSKİTAP", "¯\\_(ツ)_/¯", indeks));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("İNDEKSKİTAP", str, indeks));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("İNDEKSKİTAP", "¯\\_(ツ)_/¯", indeks));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("İNDEKSKİTAP", "ಠ_ಠ", indeks));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("İNDEKSKİTAP", "ಠ_ಠ", indeks));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("İNDEKSKİTAP", "ಠ_ಠ", indeks));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("İNDEKSKİTAP", "□", ""));
                check();
            }

//            if (stillSearch && selections != null && selections.contains("www.kitapal.com")) {
//                String kitapal = StringUtils.join("https://www.kitapal.com/arama?q=", isbn);
//                try {
//                    builder.baseUrl("https://www.kitapal.com/").build().create(ApiService.class).getPrices(kitapal).enqueue(new Callback<String>() {
//                        @Override
//                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
//                            if (response.isSuccessful() && response.body() != null) {
//                                try {
//                                    doc = Jsoup.parse(response.body());
//                                    String str = doc.select("div.priceWrapper > div.currentPrice").first().text();
//                                    if (doc.select("#katalog > div:nth-child(4) > div:nth-child(2) > div:nth-child(1) > div")
//                                            .text().contains("bulunamadı") ||
//                                            doc.select("a > span.out-of-stock").text().equals("Stokta Yok") ||
//                                            str.isEmpty()) {
//                                        arrayListPrice.add(new HowMuchAndWhere("kitapal", "¯\\_(ツ)_/¯", kitapal));
//                                        check();
//                                    } else {
//                                        arrayListPrice.add(new HowMuchAndWhere("kitapal", str, kitapal));
//                                        check();
//                                    }
//                                } catch (Exception e) {
//                                    arrayListPrice.add(new HowMuchAndWhere("kitapal", "¯\\_(ツ)_/¯", kitapal));
//                                    check();
//                                    e.printStackTrace();
//                                }
//                            } else {
//                                arrayListPrice.add(new HowMuchAndWhere("kitapal", "ಠ_ಠ", kitapal));
//                                check();
//                            }
//                        }
//
//                        @Override
//                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
//                            arrayListPrice.add(new HowMuchAndWhere("kitapal", "ಠ_ಠ", kitapal));
//                            check();
//                        }
//                    });
//                } catch (Exception e) {
//                    arrayListPrice.add(new HowMuchAndWhere("kitapal", "ಠ_ಠ", kitapal));
//                    check();
//                    e.printStackTrace();
//                }
//            } else {
//                arrayListPrice.add(new HowMuchAndWhere("kitapal", "□", ""));
//                check();
//            }

            if (stillSearch && selections != null && selections.contains("www.selamkitap.com")) {
                String selam = StringUtils.join("https://www.selamkitap.com/Arama?1&kelime=", isbn);
                try {
                    builder.baseUrl("https://www.selamkitap.com/").build().create(ApiService.class).getPrices(selam).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String string = doc.select("div.discountPrice").first().text();
                                    if (doc.select("#divUrunYok > img").attr("src").contains("urunyok") ||
                                            doc.select("a.TukendiIco > span").text().equals("Tükendi") ||
                                            string.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("Selam Kitap", "¯\\_(ツ)_/¯", selam));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("Selam Kitap", StringUtils.join(StringUtils.replace(StringUtils.replace(string, "₺", ""), "KDV Dahil", ""), " TL"), selam));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("Selam Kitap", "¯\\_(ツ)_/¯", selam));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("Selam Kitap", "¯\\_(ツ)_/¯", selam));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("Selam Kitap", "ಠ_ಠ", selam));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("Selam Kitap", "ಠ_ಠ", selam));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("Selam Kitap", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("alokitabevi.com")) {
                String alo = StringUtils.join("https://alokitabevi.com/index.php?route=product/isearch&search=", isbn);
                try {
                    builder.baseUrl("https://www.alokitabevi.com/").build().create(ApiService.class).getPrices(alo).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String str = doc.select("div:nth-child(3) > div > p > span").first().text();
                                    if (doc.select("#content > p:nth-child(10)").text().contains("bulunamadı") ||
                                            doc.select("div:nth-child(3) > div > div").text().contains("Yok") ||
                                            str.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("ALOKİTABEVİ", "¯\\_(ツ)_/¯", alo));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("ALOKİTABEVİ", StringUtils.replace(str, "TL", " TL"), alo));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("ALOKİTABEVİ", "¯\\_(ツ)_/¯", alo));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("ALOKİTABEVİ", "ಠ_ಠ", alo));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("ALOKİTABEVİ", "ಠ_ಠ", alo));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("ALOKİTABEVİ", "ಠ_ಠ", alo));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("ALOKİTABEVİ", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.yediiklim.com.tr")) {
                String yediiklim = StringUtils.join("https://www.yediiklim.com.tr/index.php?p=search&search=+", isbn);
                try {
                    builder.baseUrl("https://www.yediiklim.com.tr/").build().create(ApiService.class).getPrices(yediiklim).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String str = doc.select("span.divdiscountprice").first().text();
                                    if (doc.select("div.searchnotfound > span").text().contains("bulunamadı") ||
                                            doc.select("div.vList-Btns > div.vList-spet").text().contains("yoktur") ||
                                            str.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("YEDİİKLİM", "¯\\_(ツ)_/¯", yediiklim));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("YEDİİKLİM", StringUtils.replace(str, ".", ","), yediiklim));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("YEDİİKLİM", "¯\\_(ツ)_/¯", yediiklim));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("YEDİİKLİM", "ಠ_ಠ", yediiklim));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("YEDİİKLİM", "ಠ_ಠ", yediiklim));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("YEDİİKLİM", "ಠ_ಠ", yediiklim));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("YEDİİKLİM", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.kpssstore.com")) {
                String kpssstore = StringUtils.join("https://www.kpssstore.com/arama/", isbn);
                try {
                    builder.baseUrl("https://www.kpssstore.com/").build().create(ApiService.class).getPrices(kpssstore).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String str = doc.select("div.showcasePriceOne").first().text();
                                    if (doc.select("div.alertContent > div").text().contains("bulunamamıştır") ||
                                            doc.select("img.borderNone.globalNoStockButton").attr("src").contains("nostock") ||
                                            str.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("KPSSstore", "¯\\_(ツ)_/¯", kpssstore));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("KPSSstore", str, kpssstore));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("KPSSstore", "¯\\_(ツ)_/¯", kpssstore));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("KPSSstore", "ಠ_ಠ", kpssstore));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("KPSSstore", "ಠ_ಠ", kpssstore));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("KPSSstore", "ಠ_ಠ", kpssstore));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("KPSSstore", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.maksimumkitap.com")) {
                String maksimum = StringUtils.join("https://www.maksimumkitap.com/AramaSonuc.asp?q=", isbn);
                try {
                    builder.baseUrl("https://www.maksimumkitap.com/").build().create(ApiService.class).getPrices(maksimum).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String str = doc.select("div.productitemlist > div").first().text();
                                    if (doc.select("div.productitemlist > div").text().contains("Bulunamadı") ||
                                            !doc.select("#HeaderPenStokOlan > div > label").attr("for")./*contains("StoktaOlanKitaplar")*/ contains("StoktaOlanKitaplar01") ||
                                            str.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("maksimumkitap", "¯\\_(ツ)_/¯", maksimum));
                                        check();
                                    } else {
                                        int ixTRY = str.indexOf("TRY");
                                        int ix_ = str.lastIndexOf(" ");
                                        if (str.charAt(ixTRY - 2) == '.') {
                                            arrayListPrice.add(new HowMuchAndWhere("maksimumkitap", StringUtils.join(StringUtils.replace(StringUtils.substring(str, ix_, ixTRY), ".", ","), "0 TL"), maksimum));
                                            check();
                                        } else if (str.charAt(ixTRY - 3) == '.') {
                                            arrayListPrice.add(new HowMuchAndWhere("maksimumkitap", StringUtils.join(StringUtils.replace(StringUtils.substring(str, ix_, ixTRY), ".", ","), " TL"), maksimum));
                                            check();
                                        } else {
                                            arrayListPrice.add(new HowMuchAndWhere("maksimumkitap", "¯\\_(ツ)_/¯", maksimum));
                                            check();
                                        }
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("maksimumkitap", "¯\\_(ツ)_/¯", maksimum));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("maksimumkitap", "ಠ_ಠ", maksimum));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("maksimumkitap", "ಠ_ಠ", maksimum));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("maksimumkitap", "ಠ_ಠ", maksimum));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("maksimumkitap", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.kitapesatis.com")) {
                String esatis = StringUtils.join("https://www.kitapesatis.com/arama/", isbn);
                try {
                    builder.baseUrl("https://www.kitapesatis.com/").build().create(ApiService.class).getPrices(esatis).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String str = doc.select("div.showcaseSmallPriceOne._colorBlack").first().text();
                                    if (doc.select("div.alertContent > div").text().contains("bulunamamıştır") ||
                                            !doc.select("img[class=borderNone globalAddtoCartButton]").attr("src").contains("addtocart") ||
                                            str.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("kitapesatis", "¯\\_(ツ)_/¯", esatis));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("kitapesatis", str, esatis));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("kitapesatis", "¯\\_(ツ)_/¯", esatis));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("kitapesatis", "ಠ_ಠ", esatis));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("kitapesatis", "ಠ_ಠ", esatis));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("kitapesatis", "ಠ_ಠ", esatis));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("kitapesatis", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.alkitap.com")) {
                String alkitap = StringUtils.join("https://www.alkitap.com/arama/", isbn);
                try {
                    builder.baseUrl("https://www.alkitap.com/").build().create(ApiService.class).getPrices(alkitap).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String str = doc.select("div.showcasePriceTwo").first().text();
                                    if (doc.select("div.alertContent > div").text().contains("bulunamamıştır") ||
                                            !doc.select("img[class=borderNone globalAddtoCartButton]").attr("src").contains("addtocart") ||
                                            str.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("alkitap", "¯\\_(ツ)_/¯", alkitap));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("alkitap", str, alkitap));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("alkitap", "¯\\_(ツ)_/¯", alkitap));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("alkitap", "ಠ_ಠ", alkitap));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("alkitap", "ಠ_ಠ", alkitap));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("alkitap", "ಠ_ಠ", alkitap));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("alkitap", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.stoktankitap.com")) {
                String stoktan = StringUtils.join("https://www.stoktankitap.com/index.php?p=Products&q_field_active=0&ctg_id=&q=", isbn, "&search=&q_field=");
                try {
                    builder.baseUrl("https://www.stoktankitap.com/").build().create(ApiService.class).getPrices(stoktan).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String str = doc.select("#prd_final_price_display").first().text();
                                    if (doc.select("div.no_product_found > div:nth-child(1)").text().contains("bulunamadı") ||
                                            doc.select("div.prd_no_sell").text().contains("Stokta Yok") ||
                                            str.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("stoktankitap", "¯\\_(ツ)_/¯", stoktan));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("stoktankitap", str, stoktan));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("stoktankitap", "¯\\_(ツ)_/¯", stoktan));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("stoktankitap", "ಠ_ಠ", stoktan));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("stoktankitap", "ಠ_ಠ", stoktan));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("stoktankitap", "ಠ_ಠ", stoktan));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("stoktankitap", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.ravzakitap.com")) {
                String ravza = StringUtils.join("https://www.ravzakitap.com/arama?q=", isbn);
                try {
                    builder.baseUrl("https://www.ravzakitap.com/").build().create(ApiService.class).getPrices(ravza).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String str = doc.select("div.currentPrice.tl").first().text();
                                    if (doc.select("div[class=box col-12 a-center]").text().contains("bulunamadı") ||
                                            doc.select("div.prd_no_sell").text().contains("TÜKENMİŞ") ||
                                            doc.select("span.out-of-stock").text().contains("Yok")||
                                            str.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("ravzakitap", "¯\\_(ツ)_/¯", ravza));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("ravzakitap", StringUtils.join(str," TL"), ravza));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("ravzakitap", "¯\\_(ツ)_/¯", ravza));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("ravzakitap", "ಠ_ಠ", ravza));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("ravzakitap", "ಠ_ಠ", ravza));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("ravzakitap", "ಠ_ಠ", ravza));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("ravzakitap", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.kitapfirsati.com.tr")) {
                String kFirsati = StringUtils.join("https://www.kitapfirsati.com.tr/arama/", isbn);
                try {
                    builder.baseUrl("https://www.kitapfirsati.com.tr/").build().create(ApiService.class).getPrices(kFirsati).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String str = doc.select("div.showcasePriceTwo").first().text();
                                    if (doc.select("div.alertContent > div").text().contains("bulunamamıştır") ||
                                            doc.select("img[class=borderNone globalNoStockButton]").attr("src").contains("nostock") ||
                                            str.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("kitap fırsatı", "¯\\_(ツ)_/¯", kFirsati));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("kitap fırsatı", str, kFirsati));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("kitap fırsatı", "¯\\_(ツ)_/¯", kFirsati));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("kitap fırsatı", "ಠ_ಠ", kFirsati));
                                check();
                            }
                        }
                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("kitap fırsatı", "ಠ_ಠ", kFirsati));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("kitap fırsatı", "ಠ_ಠ", kFirsati));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("kitap fırsatı", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.kitapbudur.com")) {
                String budur = StringUtils.join("https://www.kitapbudur.com/arama?q=", isbn);
                try {
                    builder.baseUrl("https://www.kitapbudur.com/").build().create(ApiService.class).getPrices(budur).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String str = doc.select("div > div.currentPrice").first().text();
                                    if (doc.select("div.addBasket.fr > a").text().equals("STOKTA YOK") ||
                                            str.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("kitapbudur", "¯\\_(ツ)_/¯", budur));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("kitapbudur", str, budur));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("kitapbudur", "¯\\_(ツ)_/¯", budur));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("kitapbudur", "ಠ_ಠ", budur));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("kitapbudur", "ಠ_ಠ", budur));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("kitapbudur", "ಠ_ಠ", budur));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("kitapbudur", "□", ""));
                check();
            }


            if (stillSearch && selections != null && selections.contains("www.okuyanboga.com")) {
                String okuyan = StringUtils.join("https://www.okuyanboga.com/index.php?p=Products&q_field_active=0&ctg_id=&q=", isbn, "&search=&q_field=");
                try {
                    builder.baseUrl("https://www.okuyanboga.com/").build().create(ApiService.class).getPrices(okuyan).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String str = doc.select("#prd_final_price_display").first().text();
                                    if (doc.select("div.no_product_found > div:nth-child(1)").text().contains("bulunamadı") ||
                                            doc.select("div.col3 > div > div.prd_no_sell").text().equals("Temin Edilemiyor") ||
                                            str.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("OKUYAN BOĞA", "¯\\_(ツ)_/¯", okuyan));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("OKUYAN BOĞA", StringUtils.join(str, " TL"), okuyan));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("OKUYAN BOĞA", "¯\\_(ツ)_/¯", okuyan));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("OKUYAN BOĞA", "ಠ_ಠ", okuyan));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("OKUYAN BOĞA", "ಠ_ಠ", okuyan));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("OKUYAN BOĞA", "ಠ_ಠ", okuyan));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("OKUYAN BOĞA", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.cocuklarakitap.com")) {
                String cocuklara = StringUtils.join("https://www.cocuklarakitap.com/search?q=", isbn);
                try {
                    builder.baseUrl("https://www.cocuklarakitap.com/").build().create(ApiService.class).getPrices(cocuklara).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String str = doc.select("div.add-info > div.prices > span").first().text();
                                    if (doc.select("div.search-results > strong").text().contains("bulunamadı") ||
                                            doc.select("div.add-info > div.prices > span").text().equals("Stokta Yok") ||
                                            str.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("çocuklara kitap", "¯\\_(ツ)_/¯", cocuklara));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("çocuklara kitap", str, cocuklara));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("çocuklara kitap", "¯\\_(ツ)_/¯", cocuklara));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("çocuklara kitap", "ಠ_ಠ", cocuklara));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("çocuklara kitap", "ಠ_ಠ", cocuklara));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("çocuklara kitap", "ಠ_ಠ", cocuklara));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("çocuklara kitap", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.kariyerkitaplari.com")) {
                String kariyer = StringUtils.join("https://www.kariyerkitaplari.com/arama?kat=0&tip=1&word=", isbn);
                try {
                    builder.baseUrl("https://www.kariyerkitaplari.com/").build().create(ApiService.class).getPrices(kariyer).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String str = doc.select("div.urun_fiyatlar > div.urun_kdvdahil_fiyati").first().text();
                                    if (doc.select("div.urunler_main > div > div > div").text().contains("bulunamad") ||
                                            doc.select("div.urun_stok_yok > span").text().equals("Stokta Yok") ||
                                            /*doc.select("div.urun_fiyatlar > div").text()*/str.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("Kariyer Kitapları", "¯\\_(ツ)_/¯", kariyer));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("Kariyer Kitapları",
                                                StringUtils.replace(str, "KDV Dahil ", ""), kariyer));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("Kariyer Kitapları", "¯\\_(ツ)_/¯", kariyer));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("Kariyer Kitapları", "ಠ_ಠ", kariyer));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("Kariyer Kitapları", "ಠ_ಠ", kariyer));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("Kariyer Kitapları", "ಠ_ಠ", kariyer));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("Kariyer Kitapları", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.hazirlikkitap.com")) {
                String hazirlik = StringUtils.join("https://www.hazirlikkitap.com/product/search?q=", isbn);
                try {
                    builder.baseUrl("https://www.hazirlikkitap.com/").build().create(ApiService.class).getPrices(hazirlik).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String str = doc.select("div.product-item-price > span.price").first().text();
                                    if (doc.select("#mfilter-content-container > p").text().contains("bulunamadı") ||
                                            doc.select("div.btn-group.dropup > button:nth-child(1)").text().equals("TÜKENDİ") ||
                                            str.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("Hazırlık Kitap", "¯\\_(ツ)_/¯", hazirlik));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("Hazırlık Kitap",
                                                StringUtils.replace(str, "TL", " TL"), hazirlik));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("Hazırlık Kitap", "¯\\_(ツ)_/¯", hazirlik));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("Hazırlık Kitap", "ಠ_ಠ", hazirlik));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("Hazırlık Kitap", "ಠ_ಠ", hazirlik));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("Hazırlık Kitap", "ಠ_ಠ", hazirlik));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("Hazırlık Kitap", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.savaskitap.com")) {
                String savas = StringUtils.join("https://www.savaskitap.com/index.php?p=search&search=", isbn);
                try {
                    builder.baseUrl("https://www.savaskitap.com/").build().create(ApiService.class).getPrices(savas).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String str = doc.select("div.blok_alt > div:nth-child(1)").first().text();
                                    if (doc.select("div.searchnotfound > span").text().contains("bulunamadı") ||
                                            !doc.select("div.sepet_ekle > span > img").attr("src").contains("add_basket") ||
                                            str.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("SAVAŞ Kitap", "¯\\_(ツ)_/¯", savas));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("SAVAŞ Kitap",
                                                StringUtils.replace(StringUtils.replace(str, "İndirimli Ürün Fiyatı : ", ""), ".", ","), savas));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("SAVAŞ Kitap", "¯\\_(ツ)_/¯", savas));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("SAVAŞ Kitap", "ಠ_ಠ", savas));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("SAVAŞ Kitap", "¯\\_(ツ)_/¯", savas));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("SAVAŞ Kitap", "ಠ_ಠ", savas));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("SAVAŞ Kitap", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.fidankitap.com")) {
                String fidan = StringUtils.join("https://www.fidankitap.com/product/search?q=", isbn);
                try {
                    builder.baseUrl("https://www.fidankitap.com/").build().create(ApiService.class).getPrices(fidan).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String s = doc.select("span.urun-item-fiyat-1").first().text();
                                    if (doc.select("title").text().contains("Fidan Kitap") || doc.select("title").text().contains("Whoops") ||
                                            doc.select("div.urun-item-button > span").text().equals("Tükendi") ||
                                            s.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("FİDAN KİTAP", "¯\\_(ツ)_/¯", fidan));
                                        check();
                                    } else {
                                        int ixTL = s.indexOf("TL.");
                                        int ixDot = s.indexOf(".");
                                        if (ixDot > ixTL) {
                                            arrayListPrice.add(new HowMuchAndWhere("FİDAN KİTAP",
                                                    StringUtils.replace(s, " TL.", ",00 TL"), fidan));
                                            check();
                                        } else if (ixTL - ixDot == 3) {
                                            arrayListPrice.add(new HowMuchAndWhere("FİDAN KİTAP",
                                                    StringUtils.replace(StringUtils.replace(s, " TL.", "0 TL"), ".", ","), fidan));
                                            check();
                                        } else {
                                            arrayListPrice.add(new HowMuchAndWhere("FİDAN KİTAP",
                                                    StringUtils.replace(StringUtils.replace(s, " TL.", " TL"), ".", ","), fidan));
                                            check();
                                        }
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("FİDAN KİTAP", "¯\\_(ツ)_/¯", fidan));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("FİDAN KİTAP", "¯\\_(ツ)_/¯", fidan));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("FİDAN KİTAP", "ಠ_ಠ", fidan));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("FİDAN KİTAP", "ಠ_ಠ", fidan));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("FİDAN KİTAP", "□", ""));
                check();
            }
            try {
                freeMemory();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (stillSearch && selections != null && selections.contains("www.aperatifkitap.com")) {
                String apera = StringUtils.join("https://www.aperatifkitap.com/ara/?search_performed=Y&pcode=N&q=", isbn);
                try {
                    builder.baseUrl("https://www.aperatifkitap.com/").build().create(ApiService.class).getPrices(apera).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String str = doc.select("span[class=ty-price-bloklar]").first().text();
                                    if (doc.select("p.ty-no-items").text().contains("bulunamadı") ||
                                            doc.select("span[class=ty-qty-out-of-stock ty-control-group__item]").text().equals("Stokta yok") ||
                                            str.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("AperatifKitap", "¯\\_(ツ)_/¯", apera));
                                        check();
                                    } else {
                                        if(!str.contains("TL")) arrayListPrice.add(new HowMuchAndWhere("AperatifKitap", StringUtils.join(StringUtils.replace(str, ".", ","), " TL"), apera));
                                        else arrayListPrice.add(new HowMuchAndWhere("AperatifKitap", StringUtils.replace(str, ".", ","), apera));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("AperatifKitap", "¯\\_(ツ)_/¯", apera));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("AperatifKitap", "¯\\_(ツ)_/¯", apera));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("AperatifKitap", "¯\\_(ツ)_/¯", apera));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("AperatifKitap", "ಠ_ಠ", apera));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("AperatifKitap", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.limonkitabevi.com")) {
                String limon = StringUtils.join("https://www.limonkitabevi.com/index.php?p=Products&q_field_active=0&ctg_id=&q=", isbn, "&search=&q_field=");
                try {
                    builder.baseUrl("https://www.limonkitabevi.com/").build().create(ApiService.class).getPrices(limon).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String str = doc.select("#prd_final_price_display").first().text();
                                    if (doc.select("div.no_product_found > div:nth-child(1)").text().contains("bulunamadı") ||
                                            str.isEmpty() ||
                                            doc.select("div.prd_no_sell").text().equals("Satışta değil") ||
                                            doc.select("div.prd_no_sell").text().contains("yok")) {
                                        arrayListPrice.add(new HowMuchAndWhere("LİMON kitabevi", "¯\\_(ツ)_/¯", limon));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("LİMON kitabevi",
                                                StringUtils.replace(str, "TL", " TL"), limon));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("LİMON kitabevi", "¯\\_(ツ)_/¯", limon));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("LİMON kitabevi", "ಠ_ಠ", limon));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("LİMON kitabevi", "ಠ_ಠ", limon));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("LİMON kitabevi", "ಠ_ಠ", limon));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("LİMON kitabevi", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("palmekitabevi.com")) {
                String palme = StringUtils.join("https://palmekitabevi.com/search?q=", isbn);
                try {
                    builder.baseUrl("https://www.palmekitabevi.com/").build().create(ApiService.class).getPrices(palme).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String str = doc.select("span.price.actual-price").first().text();
                                    if (doc.select("a.button.product_type_simple.add_to_cart_button.ajax_add_to_cart").attr("value").equals("Sepete Ekle")) {
                                        arrayListPrice.add(new HowMuchAndWhere("PALME KİTABEVİ",
                                                StringUtils.replace(str, "₺", "TL"), palme));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("PALME KİTABEVİ", "¯\\_(ツ)_/¯", palme));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("PALME KİTABEVİ", "¯\\_(ツ)_/¯", palme));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("PALME KİTABEVİ", "¯\\_(ツ)_/¯", palme));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("PALME KİTABEVİ", "ಠ_ಠ", palme));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("PALME KİTABEVİ", "ಠ_ಠ", palme));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("PALME KİTABEVİ", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.turkmenkitabevi.com.tr")) {
                String turkmen = StringUtils.join("https://www.turkmenkitabevi.com.tr/product/search?q=", isbn);
                try {
                    builder.baseUrl("https://www.turkmenkitabevi.com.tr/").build().create(ApiService.class).getPrices(turkmen).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String s = doc.select("span[class=urun-item-fiyat-1]").first().text();
                                    if (doc.select("#sf-resetcontent > h1").text().contains("Whoops")
                                            || doc.select("div.urun-item-button > span").text().equals("Tükendi")) {
                                        arrayListPrice.add(new HowMuchAndWhere("türkmen kitabevi", "¯\\_(ツ)_/¯", turkmen));
                                        check();
                                    } else if (s.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("türkmen kitabevi", "¯\\_(ツ)_/¯", turkmen));
                                        check();
                                    } else {
                                        int ixTL = s.indexOf("TL.");
                                        int ixDot = s.indexOf(".");
                                        if (ixDot > ixTL) {
                                            arrayListPrice.add(new HowMuchAndWhere("türkmen kitabevi", StringUtils.replace(s, " TL.", ",00 TL"), turkmen));
                                            check();
                                        } else if (ixTL - ixDot == 3) {
                                            arrayListPrice.add(new HowMuchAndWhere("türkmen kitabevi", StringUtils.replace(StringUtils.replace(s, " TL.", "0 TL"), ".", ","), turkmen));
                                            check();
                                        } else {
                                            arrayListPrice.add(new HowMuchAndWhere("türkmen kitabevi", StringUtils.replace(StringUtils.replace(s, " TL.", " TL"), ".", ","), turkmen));
                                            check();
                                        }
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("türkmen kitabevi", "¯\\_(ツ)_/¯", turkmen));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                //arrayListPrice.add(new HowMuchAndWhere("türkmen kitabevi", "ಠ_ಠ", turkmen));
                                arrayListPrice.add(new HowMuchAndWhere("türkmen kitabevi", "¯\\_(ツ)_/¯", turkmen));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("türkmen kitabevi", "ಠ_ಠ", turkmen));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("türkmen kitabevi", "ಠ_ಠ", turkmen));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("türkmen kitabevi", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.garantikitap.com")) {
                String perpa = StringUtils.join("https://www.garantikitap.com/product/search?q=", isbn);
                try {
                    builder.baseUrl("https://www.garantikitap.com/").build().create(ApiService.class).getPrices(perpa).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String s = doc.select("div.urun-item-fiyat > span").first().text();
                                    if (doc.select("#sf-resetcontent > h1").text().contains("Whoops")
                                            || doc.select("div.urun-item-button > span").text().equals("Tükendi") || s.isEmpty()/*
                                    || doc.select("form[id=prdsearch]").attr("action").equals("https://www.garantikitap.com/product/search")*/) {
                                        arrayListPrice.add(new HowMuchAndWhere("Garanti Kitap", "¯\\_(ツ)_/¯", perpa));
                                        check();
                                    } else {
                                        int ixTL = s.indexOf("TL.");
                                        int ixDot = s.indexOf(".");
                                        if (ixDot > ixTL) {
                                            arrayListPrice.add(new HowMuchAndWhere("Garanti Kitap", StringUtils.replace(s, " TL.", ",00 TL"), perpa));
                                            check();
                                        } else if (ixTL - ixDot == 3) {
                                            arrayListPrice.add(new HowMuchAndWhere("Garanti Kitap", StringUtils.replace(StringUtils.replace(s, " TL.", "0 TL"), ".", ","), perpa));
                                            check();
                                        } else {
                                            arrayListPrice.add(new HowMuchAndWhere("Garanti Kitap", StringUtils.replace(StringUtils.replace(s, " TL.", " TL"), ".", ","), perpa));
                                            check();
                                        }
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("Garanti Kitap", "¯\\_(ツ)_/¯", perpa));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("Garanti Kitap", "¯\\_(ツ)_/¯", perpa));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("Garanti Kitap", "ಠ_ಠ", perpa));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("Garanti Kitap", "ಠ_ಠ", perpa));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("Garanti Kitap", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.kitapkoala.com")) {
                String koala = StringUtils.join("https://www.kitapkoala.com/product/search?q=", isbn);
                try {
                    builder.baseUrl("https://www.kitapkoala.com/").build().create(ApiService.class).getPrices(koala).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String s = doc.select("span[class=urun-item-fiyat-1]").first().text();
                                    if (doc.select("#sf-resetcontent > h1").text().contains("Whoops")
                                            || doc.select("div.urun-item-button > span").text().equals("Tükendi")) {
                                        arrayListPrice.add(new HowMuchAndWhere("Kitap Koala", "¯\\_(ツ)_/¯", koala));
                                        check();
                                    } else if (s.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("Kitap Koala", "¯\\_(ツ)_/¯", koala));
                                        check();
                                    } else {
                                        int ixTL = s.indexOf("TL.");
                                        int ixDot = s.indexOf(".");
                                        if (ixDot > ixTL) {
                                            arrayListPrice.add(new HowMuchAndWhere("Kitap Koala", StringUtils.replace(s, " TL.", ",00 TL"), koala));
                                            check();
                                        } else if (ixTL - ixDot == 3) {
                                            arrayListPrice.add(new HowMuchAndWhere("Kitap Koala", StringUtils.replace(StringUtils.replace(s, " TL.", "0 TL"), ".", ","), koala));
                                            check();
                                        } else {
                                            arrayListPrice.add(new HowMuchAndWhere("Kitap Koala", StringUtils.replace(StringUtils.replace(s, " TL.", " TL"), ".", ","), koala));
                                            check();
                                        }
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("Kitap Koala", "¯\\_(ツ)_/¯", koala));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("Kitap Koala", "¯\\_(ツ)_/¯", koala));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("Kitap Koala", "ಠ_ಠ", koala));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("Kitap Koala", "ಠ_ಠ", koala));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("Kitap Koala", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.hermeskitap.com")) {
                String hermes = StringUtils.join("https://www.hermeskitap.com/catalog/index.php?route=product/search&search=", isbn);
                try {
                    builder.baseUrl("https://www.hermeskitap.com/").build().create(ApiService.class).getPrices(hermes).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String str2 = doc.select("div:nth-child(4) > span:nth-child(1)").first().text();
                                    String str3 = doc.select("div:nth-child(2) > div:nth-child(6)").first().text();
                                    if (doc.select("div.caption > p.price").text().contains("0,00TL")) {
                                        arrayListPrice.add(new HowMuchAndWhere("HERMES", "¯\\_(ツ)_/¯", hermes));
                                        check();
                                    } else if (str3.isEmpty()) {
                                        if (str2.isEmpty() || str2.equals("0,00TL")) {
                                            arrayListPrice.add(new HowMuchAndWhere("HERMES", "¯\\_(ツ)_/¯", hermes));
                                            check();
                                        } else {
                                            arrayListPrice.add(new HowMuchAndWhere("HERMES", StringUtils.replace(str2, "TL", " TL"), hermes));
                                            check();
                                        }
                                    } else if (str3.equals("0,00TL")) {
                                        arrayListPrice.add(new HowMuchAndWhere("HERMES", "¯\\_(ツ)_/¯", hermes));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("HERMES", StringUtils.replace(str3, "TL", " TL"), hermes));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("HERMES", "¯\\_(ツ)_/¯", hermes));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("HERMES", "¯\\_(ツ)_/¯", hermes));
                                //arrayListPrice.add(new HowMuchAndWhere("HERMES", "ಠ_ಠ", hermes));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("HERMES", "¯\\_(ツ)_/¯", hermes));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("HERMES", "ಠ_ಠ", hermes));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("HERMES", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.simurg.com.tr")) {
                String URL51 = StringUtils.join("https://www.simurg.com.tr/index.php?p=Products&q_field_active=0&ctg_id=&q=", isbn, "&search=&q_field=");
                try {
                    builder.baseUrl("https://www.simurg.com.tr/").build().create(ApiService.class).getPrices(URL51).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String no_sell = doc.select("div.prd_no_sell").text();
                                    String price = doc.select("#prd_final_price_display").first().text();
                                    if (doc.select("div.no_product_found > div:nth-child(1)").text().contains("bulunamadı") ||
                                            no_sell.contains("Satıldı") || no_sell.contains("Satışta değil") ||
                                            doc.select("div.prd_info > div.actions > span").text().equals("Satıldı") ||
                                            price.isEmpty() || price.contains("0,00")) {
                                        arrayListPrice.add(new HowMuchAndWhere("simurg", "¯\\_(ツ)_/¯", URL51));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("simurg", StringUtils.join(price, " TL"), URL51));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("simurg", "¯\\_(ツ)_/¯", URL51));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("simurg", "¯\\_(ツ)_/¯", URL51));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("simurg", "ಠ_ಠ", URL51));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("simurg", "ಠ_ಠ", URL51));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("simurg", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.ucuzkitapal.com")) {
                String ucuz = StringUtils.join("https://www.ucuzkitapal.com/", isbn, "/");
                try {
                    builder.baseUrl("https://www.ucuzkitapal.com/").build().create(ApiService.class).getPrices(ucuz).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String str = doc.select("span.ty-price-num:nth-child(1)").first().text();
                                    if (doc.select("title").text().contains("Ucuz Kitap Al")
                                            || doc.select("span[class=ty-qty-out-of-stock ty-control-group__item]").text().equals("Stokta yok")
                                            || doc.select("p.ty-no-items").text().contains("bulunamadı") || str.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("UCUZKİTAPAL", "¯\\_(ツ)_/¯", ucuz));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("UCUZKİTAPAL", StringUtils.join(StringUtils.replace(str, ".", ","), " TL"), ucuz));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("UCUZKİTAPAL", "¯\\_(ツ)_/¯", ucuz));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("UCUZKİTAPAL", "ಠ_ಠ", ucuz));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("UCUZKİTAPAL", "ಠ_ಠ", ucuz));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("UCUZKİTAPAL", "ಠ_ಠ", ucuz));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("UCUZKİTAPAL", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.inkilap.com")) {
                String inkilap = StringUtils.join("https://www.inkilap.com/Arama?1&kelime=", isbn);
                try {
                    builder.baseUrl("https://www.inkilap.com/").build().create(ApiService.class).getPrices(inkilap).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String str = doc.select("div.discountPrice").first().text();
                                    if (doc.select("#divUrunYok > img").attr("src").contains("urunyok")
                                            || doc.select("a.TukendiIco > span").text().equals("Tükendi")
                                            || str.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("İNKILÂP", "¯\\_(ツ)_/¯", inkilap));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("İNKILÂP", StringUtils.join(StringUtils.replace(StringUtils.replace(str, "KDV Dahil", ""), "₺", ""), " TL"), inkilap));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("İNKILÂP", "¯\\_(ツ)_/¯", inkilap));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("İNKILÂP", "ಠ_ಠ", inkilap));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("İNKILÂP", "¯\\_(ツ)_/¯", inkilap));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("İNKILÂP", "ಠ_ಠ", inkilap));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("İNKILÂP", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.eren.com.tr")) {
                String eren = StringUtils.join("https://www.eren.com.tr/index.php?p=Products&q_field_active=0&ctg_id=&q=", isbn, "&search=&q_field=");
                try {
                    builder.baseUrl("https://www.eren.com.tr/").build().create(ApiService.class).getPrices(eren).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String str = doc.select("span.final_price").first().text();
                                    if (doc.select("#search").attr("value").equals("Ara")
                                            || doc.select("div.prd_no_sell").text().equals("Satışta değil")
                                            || doc.select("div.prd_no_sell").text().equals("Baskısı tükendi")
                                            || str.isEmpty()
                                            || str.equals("0,00 TL")) {
                                        arrayListPrice.add(new HowMuchAndWhere("EREN Kitap", "¯\\_(ツ)_/¯", eren));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("EREN Kitap", str, eren));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("EREN Kitap", "¯\\_(ツ)_/¯", eren));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("EREN Kitap", "ಠ_ಠ", eren));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("EREN Kitap", "ಠ_ಠ", eren));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("EREN Kitap", "ಠ_ಠ", eren));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("EREN Kitap", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.amazon.com.tr")) {
                String amazon = StringUtils.join("https://www.amazon.com.tr/s?k=", isbn);
                try {
                    builder.baseUrl("https://www.amazon.com.tr/").build().create(ApiService.class).getPrices(amazon).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String str = doc.select("span.a-offscreen").first().text();
                                    if (doc.select("#noResultsTitle").text().contains("eşleşmedi")
                                            || doc.select("div.a-column.a-span7 > div > span").text().contains("mevcut değil")
                                            || doc.select("div.a-section.a-spacing-none.a-spacing-top-micro > div > span").text().contains("değil")
                                            || str.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("amazon", "¯\\_(ツ)_/¯", amazon));
                                        check();
                                    } else if (!str.contains("TL")) {
                                        arrayListPrice.add(new HowMuchAndWhere("amazon", StringUtils.join(StringUtils.replace(str, "₺", ""), " TL"), amazon));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("amazon", str, amazon));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("amazon", "¯\\_(ツ)_/¯", amazon));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("amazon", "ಠ_ಠ", amazon));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("amazon", "ಠ_ಠ", amazon));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("amazon", "ಠ_ಠ", amazon));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("amazon", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.kitapisler.com")) {
                String isler = StringUtils.join("https://www.kitapisler.com/index.php?p=search&search=+", isbn);
                try {
                    builder.baseUrl("https://www.kitapisler.com/").build().create(ApiService.class).getPrices(isler).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String str = doc.select("div.vNrmal").first().text();
                                    if (doc.select("div.searchnotfound > span").text().contains("bulunamadı")
                                            || doc.select("div.vSptCenter > div").text().contains("yoktur")
                                            || str.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("İŞLER Kitabevleri", "¯\\_(ツ)_/¯", isler));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("İŞLER Kitabevleri",
                                                StringUtils.replace(str, ".", ","), isler));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("İŞLER Kitabevleri", "¯\\_(ツ)_/¯", isler));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("İŞLER Kitabevleri", "¯\\_(ツ)_/¯", isler));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("İŞLER Kitabevleri", "ಠ_ಠ", isler));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("İŞLER Kitabevleri", "ಠ_ಠ", isler));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("İŞLER Kitabevleri", "□", ""));
                check();
            }

//            if (stillSearch && selections != null && selections.contains("www.kitabindunyasi.com")) {
//                String kd = StringUtils.join("https://www.kitabindunyasi.com/index.php?p=Products&q_field_active=0&q=", isbn);
//                try {
//                    builder.baseUrl("https://www.kitabindunyasi.com/").build().create(ApiService.class).getPrices(kd).enqueue(new Callback<String>() {
//                        @Override
//                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
//                            if (response.isSuccessful() && response.body() != null) {
//                                try {
//                                    doc = Jsoup.parse(response.body());
//                                    String str = doc.select("#prd_final_price_display").first().text();
//                                    if (doc.select("div.no_product_found > div:nth-child(1)").text().contains("bulunamadı")
//                                            || doc.select("div.prd_no_sell").text().contains("yok")
//                                            || str.isEmpty()) {
//                                        arrayListPrice.add(new HowMuchAndWhere("Kitap Dünyası", "¯\\_(ツ)_/¯", kd));
//                                        check();
//                                    } else {
//                                        arrayListPrice.add(new HowMuchAndWhere("Kitap Dünyası", StringUtils.join(str, " TL"), kd));
//                                        check();
//                                    }
//                                } catch (Exception e) {
//                                    arrayListPrice.add(new HowMuchAndWhere("Kitap Dünyası", "¯\\_(ツ)_/¯", kd));
//                                    check();
//                                    e.printStackTrace();
//                                }
//                            } else {
//                                arrayListPrice.add(new HowMuchAndWhere("Kitap Dünyası", "ಠ_ಠ", kd));
//                                check();
//                            }
//                        }
//
//                        @Override
//                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
//                            arrayListPrice.add(new HowMuchAndWhere("Kitap Dünyası", "ಠ_ಠ", kd));
//                            check();
//                        }
//                    });
//                } catch (Exception e) {
//                    arrayListPrice.add(new HowMuchAndWhere("Kitap Dünyası", "ಠ_ಠ", kd));
//                    check();
//                    e.printStackTrace();
//                }
//            } else {
//                arrayListPrice.add(new HowMuchAndWhere("Kitap Dünyası", "□", ""));
//                check();
//            }

            if (stillSearch && selections != null && selections.contains("www.kitapaktif.com")) {
                String aktif = StringUtils.join("https://www.kitapaktif.com/arama/", isbn);
                try {
                    builder.baseUrl("https://www.kitapaktif.com/").build().create(ApiService.class).getPrices(aktif).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String str = doc.select("div.showcasePriceTwo").first().text();
                                    if (doc.select("div.alertContent > div").text().contains("bulunamamıştır") ||
                                            doc.select("a[class=soldOutBadge]").text().equals("Tükendi") ||
                                            str.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("kitapaktif", "¯\\_(ツ)_/¯", aktif));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("kitapaktif", str, aktif));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("kitapaktif", "¯\\_(ツ)_/¯", aktif));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("kitapaktif", "ಠ_ಠ", aktif));
                                check();
                            }
                        }
                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("kitapaktif", "ಠ_ಠ", aktif));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("kitapaktif", "ಠ_ಠ", aktif));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("kitapaktif", "□", ""));
                check();
            }

//    if (stillSearch && selections != null && selections.contains("www.hemenkitapal.com")) {
//            String hka = StringUtils.join("https://www.hemenkitapal.com/?s=" , isbn , "&product_cat=0&post_type=product");
//            try {
//                builder.baseUrl("https://www.hemenkitapal.com/").build().create(ApiService.class).getPrices(hka).enqueue(new Callback<String>() {
//                            @Override
//                            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
//                                if (response.isSuccessful() && response.body() != null) {
//                                    try {
//                                        doc = Jsoup.parse(response.body());
//                                        String str=doc.select("p > span > ins > span").text();
//                                        if (doc.select("#main > p").text().contains("bulunamadı")) {
//                                            arrayListPrice.add(new HowMuchAndWhere("HEMENKİTAPAL", "¯\\_(ツ)_/¯", hka));
//                                            check();
//                                        } else if (doc.select("div.availability > span > p").text().equals("Stokta yok")) {
//                                            arrayListPrice.add(new HowMuchAndWhere("HEMENKİTAPAL", "¯\\_(ツ)_/¯", hka));
//                                            check();
//                                        } else if (str.isEmpty()) {
//                                            arrayListPrice.add(new HowMuchAndWhere("HEMENKİTAPAL", "¯\\_(ツ)_/¯", hka));
//                                            check();
//                                        } else {
//                                            arrayListPrice.add(new HowMuchAndWhere("HEMENKİTAPAL",
//                                                    StringUtils.replace(str,"₺ ", ""), hka));
//                                            check();
//                                        }
//                                    } catch (Exception e) {
//                                        arrayListPrice.add(new HowMuchAndWhere("HEMENKİTAPAL", "¯\\_(ツ)_/¯", hka));
//                                        check();
//                                        e.printStackTrace();
//                                    }
//                                } else {
//                                  arrayListPrice.add(new HowMuchAndWhere("HEMENKİTAPAL", "ಠ_ಠ", hka));
//                                    check();
//                                }
//                            }
//                            @Override
//                            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
//                              arrayListPrice.add(new HowMuchAndWhere("HEMENKİTAPAL", "ಠ_ಠ", hka));
//                                check();
//                            }
//                        });
//            } catch (Exception e) {
//              arrayListPrice.add(new HowMuchAndWhere("HEMENKİTAPAL", "ಠ_ಠ", hka));
//                check();
//                e.printStackTrace();
//            }
//        } else {
//            arrayListPrice.add(new HowMuchAndWhere("HEMENKİTAPAL", "□", ""));
//            check();
//        }

            /*if (stillSearch && selections != null && selections.contains("depo61.com")) {
                String depo61 = StringUtils.join("https://depo61.com/Arama?1&kelime=", isbn);
                try {
                    builder.baseUrl("https://www.depo61.com/").build().create(ApiService.class).getPrices(depo61).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String str = doc.select("#ProductPageProductList > div > div > div.productDetail > div.productPrice > div.discountPrice > span:nth-child(1)").first().text();
                                    if (doc.select("#divUrunYok > img").attr("src").contains("urunyok") ||
                                            doc.select("#ProductPageProductList > div > div > a.TukendiIco > span").text().equals("Tükendi") ||
                                            str.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("Depo61", "¯\\_(ツ)_/¯", depo61));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("Depo61", StringUtils.join(StringUtils.replace(str, "₺", ""), " TL"), depo61));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("Depo61", "¯\\_(ツ)_/¯", depo61));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("Depo61", "¯\\_(ツ)_/¯", depo61));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("Depo61", "ಠ_ಠ", depo61));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("Depo61", "ಠ_ಠ", depo61));
                    check();
                    e.printStackTrace();
                }
            } else {
                arrayListPrice.add(new HowMuchAndWhere("Depo61", "□", ""));
                check();
            }*/

            if (stillSearch && selections != null && selections.contains("www.uygunkitapci.com")) {
                String uygun_kitapci = StringUtils.join("https://www.uygunkitapci.com/arama/", isbn);
                try {
                    builder.baseUrl("https://www.uygunkitapci.com/").build().create(ApiService.class).getPrices(uygun_kitapci).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String str = doc.select("div.showcasePriceB").first().text();
                                    if (doc.select("div.alertContent > div").text().contains("bulunamamıştır")) {
                                        arrayListPrice.add(new HowMuchAndWhere("Uygun Kitapçı", "¯\\_(ツ)_/¯", uygun_kitapci));
                                        check();
                                    } else if (doc.select("img[class=borderNone globalNoStockButton]").attr("src").contains("nostock")) {
                                        arrayListPrice.add(new HowMuchAndWhere("Uygun Kitapçı", "¯\\_(ツ)_/¯", uygun_kitapci));
                                        check();
                                    } else if (str.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("Uygun Kitapçı", "¯\\_(ツ)_/¯", uygun_kitapci));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("Uygun Kitapçı", StringUtils.replace(str, " Kdv Dahil", ""), uygun_kitapci));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("Uygun Kitapçı", "¯\\_(ツ)_/¯", uygun_kitapci));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("Uygun Kitapçı", "ಠ_ಠ", uygun_kitapci));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("Uygun Kitapçı", "ಠ_ಠ", uygun_kitapci));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("Uygun Kitapçı", "ಠ_ಠ", uygun_kitapci));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("Uygun Kitapçı", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.ekinyayinevi.com")) {
                String ekin = StringUtils.join("https://www.ekinyayinevi.com/tr/Ara?keyword=", isbn);
                try {
                    builder.baseUrl("https://www.ekinyayinevi.com/").build().create(ApiService.class).getPrices(ekin).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String str = doc.select("div.information.text-left > div:nth-child(3) > div > div.price-area.text-center > div.new-price").first().text();
                                    if (doc.select("div.position.pull-right > strong:nth-child(2)").text().equals("0")) {
                                        arrayListPrice.add(new HowMuchAndWhere("EKİN", "¯\\_(ツ)_/¯", ekin));
                                        check();
                                    } else if (doc.select("div:nth-child(2) > a > span").text().equals("Ürün Tükendi")) {
                                        arrayListPrice.add(new HowMuchAndWhere("EKİN", "¯\\_(ツ)_/¯", ekin));
                                        check();
                                    } else if (str.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("EKİN", "¯\\_(ツ)_/¯", ekin));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("EKİN", StringUtils.replace(str, "TRL", " TL"), ekin));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("EKİN", "¯\\_(ツ)_/¯", ekin));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("EKİN", "ಠ_ಠ", ekin));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("EKİN", "ಠ_ಠ", ekin));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("EKİN", "ಠ_ಠ", ekin));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("EKİN", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.yargiyayinevi.com")) {
                String yargi = StringUtils.join("https://www.yargiyayinevi.com/?s=", isbn, "&post_type=product");
                try {
                    builder.baseUrl("https://www.yargiyayinevi.com/").build().create(ApiService.class).getPrices(yargi).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String str = doc.select("div.col-lg-8.col-12.col-md-6.summary.entry-summary > div > p.price > ins > span").first().text();
                                    if (doc.select("div.premmerce-filter-ajax-container > p").text().contains("bulunamadı")) {
                                        arrayListPrice.add(new HowMuchAndWhere("yargı yayınevi", "¯\\_(ツ)_/¯", yargi));
                                        check();
                                    } else if (doc.select("div.col-lg-8.col-12.col-md-6.summary.entry-summary > div > p.stock.out-of-stock")
                                            .text().equals("Stokta yok")) {
                                        arrayListPrice.add(new HowMuchAndWhere("yargı yayınevi", "¯\\_(ツ)_/¯", yargi));
                                        check();
                                    } else if (str.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("yargı yayınevi", "¯\\_(ツ)_/¯", yargi));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("yargı yayınevi", StringUtils.join(StringUtils.replace(str, "₺ ", ""), " TL"), yargi));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("yargı yayınevi", "¯\\_(ツ)_/¯", yargi));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("yargı yayınevi", "ಠ_ಠ", yargi));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("yargı yayınevi", "ಠ_ಠ", yargi));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("yargı yayınevi", "ಠ_ಠ", yargi));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("yargı yayınevi", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.avantajkitap.com")) {
                String avantaj = StringUtils.join("https://www.avantajkitap.com/arama/", isbn);
                try {
                    builder.baseUrl("https://www.avantajkitap.com/").build().create(ApiService.class).getPrices(avantaj).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String str = doc.select("div[class=showcasePriceOne]").first().text();
                                    if (doc.select("div.alertContent > div").text().contains("bulunamamıştır")) {
                                        arrayListPrice.add(new HowMuchAndWhere("avantajkitap", "¯\\_(ツ)_/¯", avantaj));
                                        check();
                                    } else if (doc.select("img[class=borderNone globalNoStockButton]").attr("src").contains("nostock")) {
                                        arrayListPrice.add(new HowMuchAndWhere("avantajkitap", "¯\\_(ツ)_/¯", avantaj));
                                        check();
                                    } else if (str.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("avantajkitap", "¯\\_(ツ)_/¯", avantaj));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("avantajkitap", str, avantaj));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("avantajkitap", "¯\\_(ツ)_/¯", avantaj));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("avantajkitap", "ಠ_ಠ", avantaj));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("avantajkitap", "ಠ_ಠ", avantaj));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("avantajkitap", "ಠ_ಠ", avantaj));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("avantajkitap", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.uygunkitapal.com")) {
                String uygun = StringUtils.join("https://www.uygunkitapal.com/?s=", isbn, "&post_type=product");
                try {
                    builder.baseUrl("https://www.uygunkitapal.com/").build().create(ApiService.class).getPrices(uygun).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String str = doc.select("div:nth-child(1) > div > div > div > div.col-sm-8.summary.entry-summary > div > div > p > ins > span").first().text();
                                    if (doc.select("div.container > div > div > p").text().contains("bulunamadı") ||
                                            doc.select("div.col-sm-8.summary.entry-summary > div > div > p.stock.out-of-stock")
                                                    .text().equals("Stokta yok") ||
                                            str.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("uygunkitapal", "¯\\_(ツ)_/¯", uygun));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("uygunkitapal", StringUtils.join(StringUtils.replace(str, "₺ ", ""), " TL"), uygun));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("uygunkitapal", "¯\\_(ツ)_/¯", uygun));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("uygunkitapal", "ಠ_ಠ", uygun));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("uygunkitapal", "ಠ_ಠ", uygun));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("uygunkitapal", "ಠ_ಠ", uygun));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("uygunkitapal", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.dukakitap.com")) {
                String duka = StringUtils.join("https://www.dukakitap.com/Arama?1&kelime=", isbn);
                try {
                    builder.baseUrl("https://www.dukakitap.com/").build().create(ApiService.class).getPrices(duka).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String str = doc.select("div.discountPrice > span:nth-child(1)").first().text();
                                    if (doc.select("#divUrunYok > img").attr("src").contains("urunyok") || str.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("EVRENSEL KİTABEVİ", "¯\\_(ツ)_/¯", duka));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("EVRENSEL KİTABEVİ", StringUtils.join(StringUtils.replace(str, "₺", ""), " TL"), duka));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("EVRENSEL KİTABEVİ", "¯\\_(ツ)_/¯", duka));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("EVRENSEL KİTABEVİ", "ಠ_ಠ", duka));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("EVRENSEL KİTABEVİ", "ಠ_ಠ", duka));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("EVRENSEL KİTABEVİ", "ಠ_ಠ", duka));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("EVRENSEL KİTABEVİ", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.tele1kitap.com")) {
                String tele1 = StringUtils.join("https://www.tele1kitap.com/arama/", isbn, "/");
                try {
                    builder.baseUrl("https://www.tele1kitap.com/").build().create(ApiService.class).getPrices(tele1).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String str = doc.select("div.SFiyat > span").first().text();
                                    if (doc.select("div.IBaslik").text().contains("bulunamadı") ||
                                            doc.select("div.Durum").text().contains("Satış Dışı") ||
                                            doc.select("div.Durum").text().contains("Tükenmiş") ||
                                            doc.select("div.Durum").text().contains("Tükendi") ||
                                            str.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("TELE1 kitap", "¯\\_(ツ)_/¯", tele1));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("TELE1 kitap", str, tele1));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("TELE1 kitap", "¯\\_(ツ)_/¯", tele1));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("TELE1 kitap", "ಠ_ಠ", tele1));
                                check();
                            }
                        }
                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("TELE1 kitap", "ಠ_ಠ", tele1));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("TELE1 kitap", "ಠ_ಠ", tele1));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("TELE1 kitap", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.kelepirkitap.com")) {
                String kelepir = StringUtils.join("https://www.kelepirkitap.com/index.php?p=search&search=", isbn);
                try {
                    builder.baseUrl("https://www.kelepirkitap.com/").build().create(ApiService.class).getPrices(kelepir).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String str = doc.select("span.divdiscountprice").first().text();
                                    if (doc.select("div.searchnotfound > span").text().contains("bulunamadı") ||
                                            doc.select("div.pro-action").text().contains("yoktur") || str.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("KELEPİR", "¯\\_(ツ)_/¯", kelepir));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("KELEPİR",
                                                StringUtils.replace(str, ".", ","), kelepir));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("KELEPİR", "¯\\_(ツ)_/¯", kelepir));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("KELEPİR", "ಠ_ಠ", kelepir));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("KELEPİR", "ಠ_ಠ", kelepir));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("KELEPİR", "ಠ_ಠ", kelepir));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("KELEPİR", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.pelikankitabevi.com.tr")) {
                String pelikan = StringUtils.join("https://www.pelikankitabevi.com.tr/index.php?p=Products&q_field_active=0&ctg_id=&q=", isbn, "&search=&q_field=");
                try {
                    builder.baseUrl("https://www.pelikankitabevi.com.tr/").build().create(ApiService.class).getPrices(pelikan).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String str = doc.select("#prd_final_price_display").first().text();
                                    if (doc.select("#search").attr("value").equals("Ara")) {
                                        arrayListPrice.add(new HowMuchAndWhere("pelikan kitabevi", "¯\\_(ツ)_/¯", pelikan));
                                        check();
                                    } else if (str.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("pelikan kitabevi", "¯\\_(ツ)_/¯", pelikan));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("pelikan kitabevi",
                                                StringUtils.replace(str, "TL", " TL"), pelikan));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("pelikan kitabevi", "¯\\_(ツ)_/¯", pelikan));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("pelikan kitabevi", "ಠ_ಠ", pelikan));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("pelikan kitabevi", "ಠ_ಠ", pelikan));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("pelikan kitabevi", "ಠ_ಠ", pelikan));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("pelikan kitabevi", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.pegem.net")) {
                String pegem = StringUtils.join("https://www.pegem.net/kitabevi/Arama_Sonuc.aspx?kelime=", isbn);
                try {
                    builder.baseUrl("https://www.pegem.net/").build().create(ApiService.class).getPrices(pegem).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String str = doc.select("#urunpanel > div > div > div > div > div.product-content > div.indirimlifiyat").first().text();
                                    if (doc.select("#status").text().contains("bulunamamıştır") ||
                                            doc.select("div.product-content > div.button-container > a")
                                                    .text().contains("Satışa Çıkınca Haber Ver") ||
                                            str.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("PEGEM", "¯\\_(ツ)_/¯", pegem));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("PEGEM", StringUtils.join(str, " TL"), pegem));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("PEGEM", "¯\\_(ツ)_/¯", pegem));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("PEGEM", "ಠ_ಠ", pegem));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("PEGEM", "ಠ_ಠ", pegem));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("PEGEM", "ಠ_ಠ", pegem));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("PEGEM", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.harmankitap.com")) {
                String harman = StringUtils.join("https://www.harmankitap.com/index.php?p=Products&q_field_active=0&search_param=all&q=", isbn, "&q_field=");
                try {
                    builder.baseUrl("https://www.harmankitap.com/").build().create(ApiService.class).getPrices(harman).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String s = doc.select("#prd_final_price_display").first().text();
                                    if (doc.select("#search").attr("value").equals("Ara")) {
                                        arrayListPrice.add(new HowMuchAndWhere("harmankitap", "¯\\_(ツ)_/¯", harman));
                                        check();
                                    } else if (doc.select("div.col-md-5.column.pl_zero.ta_center.dt > span").text().equals("Satışta değil")) {
                                        arrayListPrice.add(new HowMuchAndWhere("harmankitap", "¯\\_(ツ)_/¯", harman));
                                        check();
                                    } else if (s.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("harmankitap", "¯\\_(ツ)_/¯", harman));
                                        check();
                                    } else {
                                        if (s.contains("TL")) {
                                            arrayListPrice.add(new HowMuchAndWhere("harmankitap", s, harman));
                                            check();
                                        } else {
                                            arrayListPrice.add(new HowMuchAndWhere("harmankitap", StringUtils.join(s, " TL"), harman));
                                            check();
                                        }
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("harmankitap", "¯\\_(ツ)_/¯", harman));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("harmankitap", "ಠ_ಠ", harman));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("harmankitap", "ಠ_ಠ", harman));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("harmankitap", "ಠ_ಠ", harman));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("harmankitap", "□", ""));
                check();
            }
            try {
                freeMemory();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (stillSearch && selections != null && selections.contains("www.hemenkitap.com")) {
                String hemen = StringUtils.join("https://www.hemenkitap.com/product/search?q=", isbn);
                try {
                    builder.baseUrl("https://www.hemenkitap.com/").build().create(ApiService.class).getPrices(hemen).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String price = doc.select("span.urun-item-fiyat-1").first().text();
                                    if (doc.select("span[class=kibo-prd-sold-out-span]").text().contains("Tükendi") ||
                                            doc.select("h1").text().contains("Whoops") ||
                                            price.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("HEMENKİTAP", "¯\\_(ツ)_/¯", hemen));
                                        check();
                                    } else {
                                        int ixTL = price.indexOf("TL.");
                                        int ixDot = price.indexOf(".");
                                        if (ixDot > ixTL) {
                                            arrayListPrice.add(new HowMuchAndWhere("HEMENKİTAP", StringUtils.replace(price, " TL.", ",00 TL"), hemen));
                                            check();
                                        } else if (ixTL - ixDot == 3) {
                                            arrayListPrice.add(new HowMuchAndWhere("HEMENKİTAP", StringUtils.replace(StringUtils.replace(price, " TL.", "0 TL"), ".", ","), hemen));
                                            check();
                                        } else {
                                            arrayListPrice.add(new HowMuchAndWhere("HEMENKİTAP", StringUtils.replace(StringUtils.replace(price, " TL.", " TL"), ".", ","), hemen));
                                            check();
                                        }
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("HEMENKİTAP", "¯\\_(ツ)_/¯", hemen));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("HEMENKİTAP", "¯\\_(ツ)_/¯", hemen));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("HEMENKİTAP", "ಠ_ಠ", hemen));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("HEMENKİTAP", "ಠ_ಠ", hemen));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("HEMENKİTAP", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.1001kitap.com.tr")) {
                String _1001kitap = StringUtils.join("https://www.1001kitap.com.tr/index.php?p=Products&q_field_active=0&q=", isbn);
                try {
                    builder.baseUrl("https://www.1001kitap.com.tr/").build().create(ApiService.class).getPrices(_1001kitap).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String str = doc.select("#prd_final_price_display").first().text();
                                    if (doc.select("div.prd_no_sell").text().contains("tükendi") ||
                                            doc.select("#search").attr("value").contains("Ara") ||
                                            str.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("1001 KİTAP", "¯\\_(ツ)_/¯", _1001kitap));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("1001 KİTAP", StringUtils.join(str, " TL"), _1001kitap));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("1001 KİTAP", "¯\\_(ツ)_/¯", _1001kitap));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("1001 KİTAP", "¯\\_(ツ)_/¯", _1001kitap));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("1001 KİTAP", "ಠ_ಠ", _1001kitap));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("1001 KİTAP", "ಠ_ಠ", _1001kitap));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("1001 KİTAP", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.alternatifkitap.com")) {
                String URL3 = StringUtils.join("https://www.alternatifkitap.com/ara/?search_performed=Y&q=", isbn);
                builder.baseUrl("https://www.alternatifkitap.com/").build().create(ApiService.class).getPrices(URL3).enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                doc = Jsoup.parse(response.body());
                                String strAlt = doc.select("span.ty-price-bloklar").first().text();
                                if (doc.select("p.ty-no-items").text().contains("bulunamadı") ||
                                        doc.select("span[class=ty-qty-out-of-stock ty-control-group__item]").text().contains("yok") ||
                                        strAlt.isEmpty()) {
                                    arrayListPrice.add(new HowMuchAndWhere("alternatifkitap", "¯\\_(ツ)_/¯", URL3));
                                    check();
                                } else {
                                    arrayListPrice.add(new HowMuchAndWhere("alternatifkitap", StringUtils.replace(strAlt, ".", ","), URL3));
                                    check();
                                }
                            } catch (Exception e) {
                                arrayListPrice.add(new HowMuchAndWhere("alternatifkitap", "¯\\_(ツ)_/¯", URL3));
                                check();
                                e.printStackTrace();
                            }
                        } else {
                            arrayListPrice.add(new HowMuchAndWhere("alternatifkitap", "ಠ_ಠ", URL3));
                            check();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        arrayListPrice.add(new HowMuchAndWhere("alternatifkitap", "ಠ_ಠ", URL3));
                        check();
                    }
                });
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("alternatifkitap", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.arkadas.com.tr")) {
                String URL4 = StringUtils.join("https://www.arkadas.com.tr/PageSearchProductNew.aspx?text=", isbn, "&field=0&category=0");
                builder.baseUrl("https://www.arkadas.com.tr/").build().create(ApiService.class).getPrices(URL4).enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                doc = Jsoup.parse(response.body());
                                String strArk = doc.select("[itemprop=price]").first().text();
                                if (doc.select("td:nth-child(2) > div > div:nth-child(1)").text().contains("bulunamamıştır") ||
                                        doc.select("#ctl00_ContentPlaceHolder1_RadListView1_ctrl0_ProductTemplateChooser_ctl00_LabelNoPress").text().equals("Tükendi")) {
                                    arrayListPrice.add(new HowMuchAndWhere("Arkadaş Kitabevi", "¯\\_(ツ)_/¯", URL4));
                                    check();
                                } else {
                                    if (strArk.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("Arkadaş Kitabevi", "¯\\_(ツ)_/¯", URL4));
                                        check();
                                    } else {
                                        if (strArk.contains(",")) {
                                            int index1 = strArk.indexOf(",");
                                            int index2 = strArk.indexOf(" ");
                                            if (index1 != -1 && index2 != -1) {
                                                if (index2 - index1 == 2) {
                                                    arrayListPrice.add(new HowMuchAndWhere("Arkadaş Kitabevi", StringUtils.replace(strArk, " TL", "0 TL"), URL4));
                                                    check();
                                                } else if (index2 - index1 == 3) {
                                                    arrayListPrice.add(new HowMuchAndWhere("Arkadaş Kitabevi", strArk, URL4));
                                                    check();
                                                }
                                            }
                                        } else {
                                            arrayListPrice.add(new HowMuchAndWhere("Arkadaş Kitabevi", StringUtils.replace(strArk, " TL", ",00 TL"), URL4));
                                            check();
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                arrayListPrice.add(new HowMuchAndWhere("Arkadaş Kitabevi", "¯\\_(ツ)_/¯", URL4));
                                check();
                                e.printStackTrace();
                            }
                        } else {
                            arrayListPrice.add(new HowMuchAndWhere("Arkadaş Kitabevi", "ಠ_ಠ", URL4));
                            check();
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        arrayListPrice.add(new HowMuchAndWhere("Arkadaş Kitabevi", "ಠ_ಠ", URL4));
                        check();
                    }
                });
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("Arkadaş Kitabevi", "□", ""));
                check();
            }

            /*if (stillSearch && selections != null && selections.contains("www.babil.com")) {
                String babil = StringUtils.join("https://www.google.com/search?q=babil+", isbn);
                String babilNull = StringUtils.join("https://www.babil.com/arama?q=", isbn);
                Log.d("",babil +"\n"+babilNull);
                try {
                    final HttpLoggingInterceptor logging = new HttpLoggingInterceptor(message -> {
                        Platform.get().log(message,INFO, null);
                        logs = LogsUtil.readLogs();
                    });
                    logging.setLevel(HttpLoggingInterceptor.Level.BODY);
                    final OkHttpClient okHttpClientRob = new OkHttpClient()
                            .newBuilder()
                            .addInterceptor(logging)
                            .build();
                    retrofit = new Retrofit.Builder().addConverterFactory(ScalarsConverterFactory.create()).baseUrl("https://www.google.com/").client(okHttpClientRob).build();
                    apiService = retrofit.create(ApiService.class);
                    stringCall = apiService.getPrices(babil);
                    stringCall.enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    String babil2 = StringUtils.substringBetween(response.body(), "<a href=\"/url?q=", *//*"\"><div class=\""*//*"&amp");
                                    Log.d("babil2",babil2);
                                    if(!babil2.contains("https://www.babil.com/")) {
                                        Log.d("babil nerede","1");
                                        arrayListPrice.add(new HowMuchAndWhere("Babil", "¯\\_(ツ)_/¯", babilNull));
                                        check();
                                    } else {
                                     builder.baseUrl("https://www.babil.com/").build().create(ApiService.class).getPrices(babil2).enqueue(new Callback<String>() {
                                        @Override
                                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                                            if (response.isSuccessful() && response.body() != null) {
                                                try {
                                                    doc = Jsoup.parse(response.body());
                                                    String strBab = doc.select("span.new-price").first().text();
                                                    if (doc.select("h4").text().contains("aramanız ile ilgili bir sonuç bulunamadı.") ||
                                                            doc.select("span.stock").text().equals("Tükendi") || strBab.isEmpty()) {
                                                        Log.d("babil nerede","2");
                                                        arrayListPrice.add(new HowMuchAndWhere("Babil", "¯\\_(ツ)_/¯", babil2));
                                                        check();
                                                    } else {
                                                        Log.d("babil nerede","3");arrayListPrice.add(new HowMuchAndWhere("Babil", strBab, babil2));
                                                        check();
                                                    }
                                                } catch (Exception e) {
                                                    Log.d("babil nerede","4");arrayListPrice.add(new HowMuchAndWhere("Babil", "¯\\_(ツ)_/¯", babil2));
                                                    check();
                                                    e.printStackTrace();
                                                }
                                            } else {
                                                Log.d("babil nerede","5");arrayListPrice.add(new HowMuchAndWhere("Babil", "¯\\_(ツ)_/¯", babil2));
                                                check();
                                            }
                                        }
                                        @Override
                                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                                            Log.d("babil nerede",t.getLocalizedMessage());arrayListPrice.add(new HowMuchAndWhere("Babil", "¯\\_(ツ)_/¯", babil2));
                                            check();
                                        }
                                    });
                                    }
                                } catch (Exception e) {
                                    Log.d("babil nerede","7");arrayListPrice.add(new HowMuchAndWhere("Babil", "¯\\_(ツ)_/¯", babilNull));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                Log.d("babil nerede","8");arrayListPrice.add(new HowMuchAndWhere("Babil", "¯\\_(ツ)_/¯", babilNull));
                                check();
                            }
                        }
                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            Log.d("babil nerede","9");arrayListPrice.add(new HowMuchAndWhere("Babil", "¯\\_(ツ)_/¯", babilNull));
                            check();
                        }
                    });
                } catch (Exception e) {
                    Log.d("babil nerede","10");arrayListPrice.add(new HowMuchAndWhere("Babil", "¯\\_(ツ)_/¯", babilNull));
                    check();
                    e.printStackTrace();
                }
            } else {
                Log.d("babil nerede","11");arrayListPrice.add(new HowMuchAndWhere("Babil", "□", ""));
                check();
            }*/

            if (stillSearch && selections != null && selections.contains("www.babil.com")) {
                final String URL6 = StringUtils.join("https://www.babil.com/arama?q=", isbn);
                try {
                    builder.baseUrl("https://www.babil.com/").build().create(ApiService.class).getPrices(URL6).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String strBab = doc.select("span.new-price").first().text();
                                    if (doc.select("h4").text().contains("aramanız ile ilgili bir sonuç bulunamadı.")) {
                                        arrayListPrice.add(new HowMuchAndWhere("Babil", "¯\\_(ツ)_/¯", URL6));
                                        check();
                                    } else if (doc.select("span.stock").text().equals("Tükendi")) {
                                        arrayListPrice.add(new HowMuchAndWhere("Babil", "¯\\_(ツ)_/¯", URL6));
                                        check();
                                    } else {
                                        if (strBab.isEmpty()) {
                                            arrayListPrice.add(new HowMuchAndWhere("Babil", "¯\\_(ツ)_/¯", URL6));
                                            check();
                                        } else {
                                            arrayListPrice.add(new HowMuchAndWhere("Babil", strBab, URL6));
                                            check();
                                        }
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("Babil", "¯\\_(ツ)_/¯", URL6));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("Babil", "ಠ_ಠ", URL6));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            Log.d("babil",t.getLocalizedMessage());
                            arrayListPrice.add(new HowMuchAndWhere("Babil", "ಠ_ಠ", URL6));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("Babil", "ಠ_ಠ", URL6));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("Babil", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("basaridagitim.com")) {
                final String URL7 = StringUtils.join("https://basaridagitim.com/ara?q=", isbn);
                try {
                    builder.baseUrl("https://www.basaridagitim.com/").build().create(ApiService.class).getPrices(URL7).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String strBasari = doc.select("span.price > span").first().text();
                                    if (doc.select("h3").text()
                                            .contains("bulunamamıştır")) {
                                        arrayListPrice.add(new HowMuchAndWhere("başarı DAĞITIM", "¯\\_(ツ)_/¯", URL7));
                                        check();
                                    } else {
                                        if (strBasari.isEmpty()) {
                                            arrayListPrice.add(new HowMuchAndWhere("başarı DAĞITIM", "¯\\_(ツ)_/¯", URL7));
                                            check();
                                        } else {
                                            arrayListPrice.add(new HowMuchAndWhere("başarı DAĞITIM", StringUtils.replace(strBasari, "₺", "TL"), URL7));
                                            check();
                                        }
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("başarı DAĞITIM", "¯\\_(ツ)_/¯", URL7));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("başarı DAĞITIM", "ಠ_ಠ", URL7));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("başarı DAĞITIM", "ಠ_ಠ", URL7));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("başarı DAĞITIM", "ಠ_ಠ", URL7));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("başarı DAĞITIM", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.benlikitap.com")) {
                String URL7_1 = StringUtils.join("https://www.benlikitap.com/arama/", isbn);
                try {
                    builder.baseUrl("https://www.benlikitap.com/").build().create(ApiService.class).getPrices(URL7_1).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String strBenli = doc.select("div.showcasePriceTwo").first().text();
                                    if (doc.select("div[class=alertContentInside _textAlignCenter]").text()
                                            .contains("bulunamamıştır")) {
                                        arrayListPrice.add(new HowMuchAndWhere("Benli Kitap", "¯\\_(ツ)_/¯", URL7_1));
                                        check();
                                    } else {
                                        if (strBenli.isEmpty()) {
                                            arrayListPrice.add(new HowMuchAndWhere("Benli Kitap", "¯\\_(ツ)_/¯", URL7_1));
                                            check();
                                        } else {
                                            arrayListPrice.add(new HowMuchAndWhere("Benli Kitap", strBenli, URL7_1));
                                            check();
                                        }
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("Benli Kitap", "¯\\_(ツ)_/¯", URL7_1));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("Benli Kitap", "ಠ_ಠ", URL7_1));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("Benli Kitap", "ಠ_ಠ", URL7_1));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("Benli Kitap", "ಠ_ಠ", URL7_1));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("Benli Kitap", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.bkmkitap.com")) {
                String URL8 = StringUtils.join("https://www.bkmkitap.com/arama?q=", isbn);
                try {
                    builder.baseUrl("https://www.bkmkitap.com/").build().create(ApiService.class).getPrices(URL8).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String str = doc.select("div.currentPrice").first().text();
                                    if (doc.select("span.text-custom-dark-gray").text().contains("bulunamadı") || doc.select("span.out-of-stock").text().equals("Tükendi") || doc.select("a[class=col listStockAlert]").text().equals("Tükendi") || str.isEmpty())
                                        checkFirst("bkmkitap", "¯\\_(ツ)_/¯", URL8, null);
                                    else checkFirst("bkmkitap", str, URL8, null);
                                } catch (Exception e) {
                                    checkFirst("bkmkitap", "¯\\_(ツ)_/¯", URL8, e);
                                }
                            } else checkFirst("bkmkitap", "ಠ_ಠ", URL8, null);
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            checkFirst("bkmkitap", "ಠ_ಠ", URL8, null);
                        }
                    });
                } catch (Exception e) {
                    checkFirst("bkmkitap", "ಠ_ಠ", URL8, e);
                }
            } else check();/*checkFirst("bkmkitap", "□", "", null);*/

            if (stillSearch && selections != null && selections.contains("www.dr.com.tr")) {
                final String URL9 = StringUtils.join("https://www.dr.com.tr/search?q=", isbn);
                try {
                    builder.baseUrl("https://www.dr.com.tr/").build().create(ApiService.class).getPrices(URL9).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String strDr = doc.select("span.price").first().text();
                                    if (doc.select("article[class=catpagenoproduct searchNotProText]").text().equals("Aradığınız kritere uygun ürün bulunamadı.") ||
                                            strDr.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("D&R", "¯\\_(ツ)_/¯", URL9));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("D&R", strDr, URL9));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("D&R", "¯\\_(ツ)_/¯", URL9));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("D&R", "ಠ_ಠ", URL9));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("D&R", "ಠ_ಠ", URL9));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("D&R", "ಠ_ಠ", URL9));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("D&R", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.eganba.com")) {
                String URL10 = StringUtils.join("https://www.eganba.com/arama?q=", isbn);
                try {
                    builder.baseUrl("https://www.eganba.com/").build().create(ApiService.class).getPrices(URL10).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String strEga = doc.select("div.product-price").first().text();
                                    if (doc.select("#main > div > div > h4").text().contains("bulunamadı") ||
                                            doc.select("div.product-info > span").text().contains("Tükendi") ||
                                            strEga.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("Eganba", "¯\\_(ツ)_/¯", URL10));
                                        check();
                                    } else if(StringUtils.contains(strEga,"%")){
                                        arrayListPrice.add(new HowMuchAndWhere("Eganba", StringUtils.substringAfter(strEga," "), URL10));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("Eganba", strEga, URL10));
                                        check();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    arrayListPrice.add(new HowMuchAndWhere("Eganba", "¯\\_(ツ)_/¯", URL10));
                                    check();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("Eganba", "ಠ_ಠ", URL10));
                                check();
                            }
                        }
                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("Eganba", "ಠ_ಠ", URL10));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("Eganba", "ಠ_ಠ", URL10));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("Eganba", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.elitkitap.com")) {
                String URL10_1 = StringUtils.join("https://www.elitkitap.com/index.php?p=Products&q=", isbn);
                try {
                    builder.baseUrl("https://www.elitkitap.com/").build().create(ApiService.class).getPrices(URL10_1).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String strElit = doc.select("#prd_final_price_display").first().text();
                                    if (doc.select("#search").attr("value").equals("Ara")) {
                                        arrayListPrice.add(new HowMuchAndWhere("elitkitap", "¯\\_(ツ)_/¯", URL10_1));
                                        check();
                                    } else if (doc.select("span.text-danger").text().equals("Satışta değil")) {
                                        arrayListPrice.add(new HowMuchAndWhere("elitkitap", "¯\\_(ツ)_/¯", URL10_1));
                                        check();
                                    } else {
                                        if (strElit.isEmpty()) {
                                            arrayListPrice.add(new HowMuchAndWhere("elitkitap", "¯\\_(ツ)_/¯", URL10_1));
                                            check();
                                        } else {
                                            arrayListPrice.add(new HowMuchAndWhere("elitkitap", strElit, URL10_1));
                                            check();
                                        }
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("elitkitap", "¯\\_(ツ)_/¯", URL10_1));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("elitkitap", "ಠ_ಠ", URL10_1));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("elitkitap", "ಠ_ಠ", URL10_1));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("elitkitap", "ಠ_ಠ", URL10_1));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("elitkitap", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.gazikitabevi.com.tr")) {
                String URL14 = StringUtils.join("https://www.gazikitabevi.com.tr/arama/", isbn);
                try {
                    builder.baseUrl("https://www.gazikitabevi.com.tr/").build().create(ApiService.class).getPrices(URL14).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String strGazi = doc.select("div.showcase-price-new").first().text();
                                    if (doc.select("#results-page > div > div").text().contains("bulunamamıştır") || strGazi.isEmpty() || strGazi.contains("0,00")) {
                                        arrayListPrice.add(new HowMuchAndWhere("Gazi Kitabevi", "¯\\_(ツ)_/¯", URL14));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("Gazi Kitabevi", strGazi, URL14));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("Gazi Kitabevi", "¯\\_(ツ)_/¯", URL14));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("Gazi Kitabevi", "ಠ_ಠ", URL14));
                                check();
                            }
                        }
                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("Gazi Kitabevi", "ಠ_ಠ", URL14));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("Gazi Kitabevi", "ಠ_ಠ", URL14));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("Gazi Kitabevi", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.halkkitabevi.com")) {
                String URL15 = StringUtils.join(/*"https://www.halkkitabevi.com/index.php?p=Products&q_field_active=0&ctg_id=&q="22.04.2020*/
                        "https://www.halkkitabevi.com/index.php?p=Products&q_field_active=0&q=", isbn);
                try {
                    builder.baseUrl("https://www.halkkitabevi.com/").build().create(ApiService.class).getPrices(URL15).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String strHalk = doc.select(/*"span.prd_view_price_value.final_price"22.04.2020*/"#prd_final_price_display").first().text();
                                    if (doc.select("#search").attr("value").equals("Ara") ||
                                            doc.select("div.prd_no_sell").text().contains("yok") || strHalk.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("Halk Kitabevi", "¯\\_(ツ)_/¯", URL15));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("Halk Kitabevi", StringUtils.replace(strHalk, "TL", " TL"), URL15));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("Halk Kitabevi", "¯\\_(ツ)_/¯", URL15));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("Halk Kitabevi", "ಠ_ಠ", URL15));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("Halk Kitabevi", "ಠ_ಠ", URL15));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("Halk Kitabevi", "ಠ_ಠ", URL15));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("Halk Kitabevi", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.idefix.com")) {
                String URL17 = StringUtils.join("https://www.idefix.com/search/?Q=", isbn, "&ShowNotForSale=True");
                try {
                    builder.baseUrl("https://www.idefix.com/").build().create(ApiService.class).getPrices(URL17).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String strIde = doc.select("#prices").first().text();
                                    if (strIde.isEmpty()) {
                                        if (doc.select("div.box-line-4 > span")
                                                .text().equals("Tükendi")) {
                                            arrayListPrice.add(new HowMuchAndWhere("idefix", "¯\\_(ツ)_/¯", URL17));
                                            check();
                                        } else if (doc.select("div > center > h3")
                                                .text().contains("bulunamadı")) {
                                            arrayListPrice.add(new HowMuchAndWhere("idefix", "¯\\_(ツ)_/¯", URL17));
                                            check();
                                        }
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("idefix", strIde, URL17));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("idefix", "¯\\_(ツ)_/¯", URL17));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("idefix", "ಠ_ಠ", URL17));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("idefix", "ಠ_ಠ", URL17));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("idefix", "ಠ_ಠ", URL17));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("idefix", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.ilknokta.com")) {
                String URL18 = StringUtils.join(/*"https://www.ilknokta.com/index.php?p=Products&q_field_active=0&ctg_id=&q="22.04.2020*/
                        "https://www.ilknokta.com/index.php?p=Products&q_field_active=0&q=", isbn);
                try {
                    builder.baseUrl("https://www.ilknokta.com/").build().create(ApiService.class).getPrices(URL18).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String strIlk = doc.select(/*"span.prd_view_price_value.final_price.price_ribbon"22.04.2020*/
                                            "#prd_final_price_display").first().text();
                                    if (doc.select("#search").attr("value").equals("Ara") || doc.select("div.prd_no_sell").text().equals("Tükendi") ||
                                            doc.select("div.prd_no_sell").text().equals("TÜKENDİ")) {
                                        arrayListPrice.add(new HowMuchAndWhere("ilknokta", "¯\\_(ツ)_/¯", URL18));
                                        check();
                                    } else {
                                        if (strIlk.isEmpty()) {
                                            arrayListPrice.add(new HowMuchAndWhere("ilknokta", "¯\\_(ツ)_/¯", URL18));
                                            check();
                                        } else {
                                            arrayListPrice.add(new HowMuchAndWhere("ilknokta", StringUtils.join(strIlk," TL"), URL18));
                                            check();
                                        }
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("ilknokta", "¯\\_(ツ)_/¯", URL18));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("ilknokta", "ಠ_ಠ", URL18));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("ilknokta", "ಠ_ಠ", URL18));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("ilknokta", "ಠ_ಠ", URL18));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("ilknokta", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.imge.com.tr")) {
                String URL19 = StringUtils.join("https://www.imge.com.tr/product/search?q=", isbn);
                try {
                    builder.baseUrl("https://www.imge.com.tr/").build().create(ApiService.class).getPrices(URL19).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String strKimg = doc.select("span.urun-item-fiyat-1").first().text();
                                    if (doc.select("div:nth-child(8)").text().contains("0 Ürün") ||
                                            doc.select("span[class=kibo-prd-sold-out-span]").text().contains("Tükendi") || strKimg.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("İMGE kitabevi", "¯\\_(ツ)_/¯", URL19));
                                        check();
                                    } else {
                                        int ixTL = strKimg.indexOf("TL.");
                                        int ixDot = strKimg.indexOf(".");
                                        if (ixDot > ixTL) {
                                            arrayListPrice.add(new HowMuchAndWhere("İMGE kitabevi", StringUtils.replace(strKimg, " TL.", ",00 TL"), URL19));
                                            check();
                                        } else if (ixTL - ixDot == 3) {
                                            arrayListPrice.add(new HowMuchAndWhere("İMGE kitabevi", StringUtils.replace(StringUtils.replace(strKimg, " TL.", "0 TL"), ".", ","), URL19));
                                            check();
                                        } else {
                                            arrayListPrice.add(new HowMuchAndWhere("İMGE kitabevi", StringUtils.replace(StringUtils.replace(strKimg, " TL.", " TL"), ".", ","), URL19));
                                            check();
                                        }
//                                        if (strKimg.contains("Sitemizde")) {
//                                            arrayListPrice.add(new HowMuchAndWhere("İMGE kitabevi", StringUtils.replace(StringUtils.replace(StringUtils.substring(strKimg, strKimg.indexOf("S"), strKimg.indexOf("İ")), "Sitemizde: ", ""), "TL ", "TL"), URL19));
//                                            check();
//                                        } else {
//                                            arrayListPrice.add(new HowMuchAndWhere("İMGE kitabevi", StringUtils.replace(strKimg, "Fiyatı: ", ""), URL19));
//                                            check();
//                                        }
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("İMGE kitabevi", "¯\\_(ツ)_/¯", URL19));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("İMGE kitabevi", "ಠ_ಠ", URL19));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("İMGE kitabevi", "ಠ_ಠ", URL19));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("İMGE kitabevi", "ಠ_ಠ", URL19));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("İMGE kitabevi", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.insancilkitap.com")) {
                String URL21 = StringUtils.join("https://www.insancilkitap.com/arama/?s=", isbn);
                try {
                    builder.baseUrl("https://www.insancilkitap.com/").build().create(ApiService.class).getPrices(URL21).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String strIns = doc.select("[class=item-price]").first().text();
                                    if (doc.select("#Content1_totproduct")
                                            .text().contains("Toplam 0 adet ürün bulunmuştur.")) {
                                        arrayListPrice.add(new HowMuchAndWhere("İNSANCIL", "¯\\_(ツ)_/¯", URL21));
                                        check();
                                    } else if (strIns.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("İNSANCIL", "¯\\_(ツ)_/¯", URL21));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("İNSANCIL", StringUtils.replace(strIns, ".", ","), URL21));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("İNSANCIL", "¯\\_(ツ)_/¯", URL21));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("İNSANCIL", "ಠ_ಠ", URL21));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("İNSANCIL", "ಠ_ಠ", URL21));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("İNSANCIL", "ಠ_ಠ", URL21));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("İNSANCIL", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.kabalci.com.tr")) {
                String URL23 = StringUtils.join("https://www.kabalci.com.tr/index.php?p=Products&q_field_active=0&ctg_id=&q=", isbn);
                try {
                    builder.baseUrl("https://www.kabalci.com.tr/").build().create(ApiService.class).getPrices(URL23).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String strKab = doc.select("span[id=prd_final_price_display]").first().text();
                                    if (doc.select("div.prd_no_sell").text().equals("Stokta yok")) {
                                        arrayListPrice.add(new HowMuchAndWhere("KABALCI", "¯\\_(ツ)_/¯", URL23));
                                        check();
                                    } else if (doc.select("#search").attr("value").equals("Ara")) {
                                        arrayListPrice.add(new HowMuchAndWhere("KABALCI", "¯\\_(ツ)_/¯", URL23));
                                        check();
                                    } else if (strKab.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("KABALCI", "¯\\_(ツ)_/¯", URL23));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("KABALCI", StringUtils.replace(strKab, "TL", " TL"), URL23));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("KABALCI", "¯\\_(ツ)_/¯", URL23));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("KABALCI", "ಠ_ಠ", URL23));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("KABALCI", "ಠ_ಠ", URL23));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("KABALCI", "ಠ_ಠ", URL23));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("KABALCI", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("kidega.com")) {
                String URL24 = StringUtils.join("https://kidega.com/arama?query=", isbn);
                try {
                    builder.baseUrl("https://www.kidega.com/").build().create(ApiService.class).getPrices(URL24).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String strKid = doc.select("b[class=lastPrice]").first().text();
                                    if (doc.select("div.noStock").text().equals("Tükendi")) {
                                        arrayListPrice.add(new HowMuchAndWhere("kidega", "¯\\_(ツ)_/¯", URL24));
                                        check();
                                    } else if (doc.select("div.txt")
                                            .text().equals("Aramanız ile ilgili sonuç bulunamadı.")) {
                                        arrayListPrice.add(new HowMuchAndWhere("kidega", "¯\\_(ツ)_/¯", URL24));
                                        check();
                                    } else if (strKid.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("kidega", "¯\\_(ツ)_/¯", URL24));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("kidega", StringUtils.replace(strKid, "₺", "TL"), URL24));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("kidega", "¯\\_(ツ)_/¯", URL24));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("kidega", "ಠ_ಠ", URL24));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("kidega", "ಠ_ಠ", URL24));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("kidega", "ಠ_ಠ", URL24));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("kidega", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("kitapaloku.com")) {
                String URL25_1 = StringUtils.join("https://www.kitapaloku.com/arama/", StringUtils.substring(isbn,3), "/");
                try {
                    builder.baseUrl("https://www.kitapaloku.com/").build().create(ApiService.class).getPrices(URL25_1).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String strKao = doc.select("div.SFiyat > span").first().text();
                                    if (doc.select("div.Durum").text().equals("Ürün Tükenmiş") ||
                                            doc.select("div.Durum").text().contains("Satış Dışı") ||
                                            doc.select("div.IBaslik").text().contains("bulunamadı") ||
                                            doc.select("div.Islem > div.Alt > div.Bold").text().equals("Stok Adedi: 0")||
                                            strKao.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("kitapaloku", "¯\\_(ツ)_/¯", URL25_1));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("kitapaloku", strKao, URL25_1));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("kitapaloku", "¯\\_(ツ)_/¯", URL25_1));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("kitapaloku", "ಠ_ಠ", URL25_1));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("kitapaloku", "ಠ_ಠ", URL25_1));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("kitapaloku", "ಠ_ಠ", URL25_1));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("kitapaloku", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.kitapambari.com")) {
                String URL26 = StringUtils.join(/*"https://www.kitapambari.com/index.php?p=Products&q_field_active=0&q="22.04.2020*/
                        "https://www.kitapambari.com/index.php?p=Products&q_field_active=0&q=", isbn);
                try { builder.baseUrl("https://www.kitapambari.com/").build().create(ApiService.class).getPrices(URL26).enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                doc = Jsoup.parse(response.body());
                                String strKamb = doc.select(/*"div.final_price"*/"#prd_final_price_display").first().text();
                                if (doc.select("div.prd_no_sell").text().contains("Tükendi") ||
                                        doc.select("#search").attr("value").equals("Ara") || strKamb.isEmpty()) {
                                    arrayListPrice.add(new HowMuchAndWhere("KİTAPAMBARI", "¯\\_(ツ)_/¯", URL26));
                                    check();
                                } else {
                                    if (!strKamb.contains("TL")) {
                                        strKamb += " TL";
                                    }
                                    arrayListPrice.add(new HowMuchAndWhere("KİTAPAMBARI", strKamb, URL26));
                                    check();
                                }
                            } catch (Exception e) {
                                arrayListPrice.add(new HowMuchAndWhere("KİTAPAMBARI", "¯\\_(ツ)_/¯", URL26));
                                check();
                                e.printStackTrace();
                            }
                        } else {
                            arrayListPrice.add(new HowMuchAndWhere("KİTAPAMBARI", "ಠ_ಠ", URL26));
                            check();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        arrayListPrice.add(new HowMuchAndWhere("KİTAPAMBARI", "ಠ_ಠ", URL26));
                        check();
                    }
                });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("KİTAPAMBARI", "ಠ_ಠ", URL26));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("KİTAPAMBARI", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.kitapbende.com")) {
                final String URL26_0 = StringUtils.join("https://www.kitapbende.com/arama?q=", isbn);
                try {
                    builder.baseUrl("https://www.kitapbende.com/").build().create(ApiService.class).getPrices(URL26_0).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String strKbende = doc.select("div[class=currentPrice fontRubik fw600]").first().text();
                                    if (doc.select("div[class=btn col-12 p-left total fontRubik]").text().contains("bulunmamaktadır") ||
                                            doc.select("span.out-of-stock.fw600.fontRubik").text().contains("Tükendi") || strKbende.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("Kitapbende", "¯\\_(ツ)_/¯", URL26_0));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("Kitapbende", StringUtils.replace(strKbende, " + KDV",""), URL26_0));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("Kitapbende", "¯\\_(ツ)_/¯", URL26_0));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("Kitapbende", "ಠ_ಠ", URL26_0));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("Kitapbende", "ಠ_ಠ", URL26_0));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("Kitapbende", "ಠ_ಠ", URL26_0));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("Kitapbende", "□", ""));
                check();
            }
            try {
                freeMemory();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (stillSearch && selections != null && selections.contains("www.kitapbir.com")) {
                String URL26_1 = StringUtils.join("https://www.kitapbir.com/arama?search=", isbn);
                try {
                    builder.baseUrl("https://www.kitapbir.com/").build().create(ApiService.class).getPrices(URL26_1).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String strKitpbr = doc.select("p.price > span.price-new").first().text();
                                    if (doc.select("#mfilter-content-container > p").text().contains("bulunamadı")) {
                                        arrayListPrice.add(new HowMuchAndWhere("kitapbir", "¯\\_(ツ)_/¯", URL26_1));
                                        check();
                                    } else if (strKitpbr.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("kitapbir", "¯\\_(ツ)_/¯", URL26_1));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("kitapbir", strKitpbr, URL26_1));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("kitapbir", "¯\\_(ツ)_/¯", URL26_1));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("kitapbir", "ಠ_ಠ", URL26_1));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("kitapbir", "ಠ_ಠ", URL26_1));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("kitapbir", "ಠ_ಠ", URL26_1));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("kitapbir", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.kitapburada.com")) {
                String URL27 = StringUtils.join("https://www.kitapburada.com/index.php?p=Products&q=", isbn);
                try {
                    builder.baseUrl("https://www.kitapburada.com/").build().create(ApiService.class).getPrices(URL27).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String strKbur = doc.select(/*"span.ui-state-active.final_price"22.04.2020*/"#prd_final_price_display").first().text();
                                    if (doc.select("#search").attr("value").equals("Ara") ||
                                            doc.select("div.no_product_found > div:nth-child(1)").text().contains("bulunamadı") ||
                                            doc.select("div.prd_no_sell").text().equals("Satışta değil") ||
                                            strKbur.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("kitapburada", "¯\\_(ツ)_/¯", URL27));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("kitapburada", StringUtils.replace(strKbur,"TL"," TL"), URL27));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("kitapburada", "¯\\_(ツ)_/¯", URL27));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("kitapburada", "ಠ_ಠ", URL27));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("kitapburada", "ಠ_ಠ", URL27));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("kitapburada", "ಠ_ಠ", URL27));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("kitapburada", "□", ""));
                check();
            }

//            if (stillSearch && selections != null && selections.contains("www.kitapdenizi.com")) {
//                String URL28 = StringUtils.join("https://www.kitapdenizi.com/index.php?p=Products&q_field_active=0&ctg_id=&q=", isbn);
//                try {
//                    builder.baseUrl("https://www.kitapdenizi.com/").build().create(ApiService.class).getPrices(URL28).enqueue(new Callback<String>() {
//                        @Override
//                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
//                            if (response.isSuccessful() && response.body() != null) {
//                                try {
//                                    doc = Jsoup.parse(response.body());
//                                    String strKden = doc.select("span.ui-state-active.final_price").first().text();
//                                    if (doc.select("div.prd_no_sell").text().equals("Stokta yok")) {
//                                        arrayListPrice.add(new HowMuchAndWhere("kitapdenizi", "¯\\_(ツ)_/¯", URL28));
//                                        check();
//                                    } else if (doc.select("#search").attr("value").equals("Ara")) {
//                                        arrayListPrice.add(new HowMuchAndWhere("kitapdenizi", "¯\\_(ツ)_/¯", URL28));
//                                        check();
//                                    } else if (strKden.isEmpty()) {
//                                        arrayListPrice.add(new HowMuchAndWhere("kitapdenizi", "¯\\_(ツ)_/¯", URL28));
//                                        check();
//                                    } else {
//                                        arrayListPrice.add(new HowMuchAndWhere("kitapdenizi", strKden, URL28));
//                                        check();
//                                    }
//                                } catch (Exception e) {
//                                    arrayListPrice.add(new HowMuchAndWhere("kitapdenizi", "¯\\_(ツ)_/¯", URL28));
//                                    check();
//                                    e.printStackTrace();
//                                }
//                            } else {
//                                arrayListPrice.add(new HowMuchAndWhere("kitapdenizi", "ಠ_ಠ", URL28));
//                                check();
//                            }
//                        }
//
//                        @Override
//                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
//                            arrayListPrice.add(new HowMuchAndWhere("kitapdenizi", "ಠ_ಠ", URL28));
//                            check();
//                        }
//                    });
//                } catch (Exception e) {
//                    arrayListPrice.add(new HowMuchAndWhere("kitapdenizi", "ಠ_ಠ", URL28));
//                    check();
//                    e.printStackTrace();
//                }
//            } else {
//                arrayListPrice.add(new HowMuchAndWhere("kitapdenizi", "□", ""));
//                check();
//            }

            if (stillSearch && selections != null && selections.contains("www.kitapmatik.com.tr")) {
                String URL29 = StringUtils.join("https://www.kitapmatik.com.tr/index.php?p=Products&q_field_active=0&ctg_id=&q=", isbn);
                try {
                    builder.baseUrl("https://www.kitapmatik.com.tr/").build().create(ApiService.class).getPrices(URL29).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String str = doc.select("#prd_final_price_display").first().text();
                                    if (doc.select("#search").attr("value").equals("Ara") ||
                                            doc.select("div.prd_no_sell").text().equals("Satışta değil") ||
                                            doc.select("div.prd_no_sell").text().equals("Baskısı tükendi") ||
                                            str.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("kitapmatik", "¯\\_(ツ)_/¯", URL29));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("kitapmatik", StringUtils.replace(str, "TL", " TL"), URL29));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("kitapmatik", "¯\\_(ツ)_/¯", URL29));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("kitapmatik", "ಠ_ಠ", URL29));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("kitapmatik", "ಠ_ಠ", URL29));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("kitapmatik", "ಠ_ಠ", URL29));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("kitapmatik", "□", ""));
                check();
            }

//    if (stillSearch && selections != null && selections.contains("www.kitapperver.com")) {
//      String URL30 = StringUtils.join("https://www.kitapperver.com/index.php?p=Products&prd_barcode=",isbn);
//      try {
//          builder.baseUrl("https://www.kitapperver.com/").build().create(ApiService.class).getPrices(URL30).enqueue(new Callback<String>() {
//          @Override
//          public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
//            if (response.isSuccessful() && response.body() != null) {
//              try {
//                doc = Jsoup.parse(response.body());
//                  String strKperver = doc.select("div.final_price.price_normal").first().text();
//                if (doc.select("#search").attr("value").equals("Ara")) {
//                  arrayListPrice.add(new HowMuchAndWhere("kitapperver", "¯\\_(ツ)_/¯", URL30));
//                  check();
//                } else if (strKperver.isEmpty()) {
//                    arrayListPrice.add(new HowMuchAndWhere("kitapperver", "¯\\_(ツ)_/¯", URL30));
//                    check();
//                  } else {
//                    arrayListPrice.add(new HowMuchAndWhere("kitapperver", strKperver, URL30));
//                    check();
//                  }
//              } catch (Exception e) {
//                  arrayListPrice.add(new HowMuchAndWhere("kitapperver", "¯\\_(ツ)_/¯", URL30));
//                  check();
//                e.printStackTrace();
//              }
//            } else {
//              arrayListPrice.add(new HowMuchAndWhere("kitapperver", "ಠ_ಠ", URL30));
//              check();
//            }
//          }
//          @Override
//          public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
//            arrayListPrice.add(new HowMuchAndWhere("kitapperver", "ಠ_ಠ", URL30));
//            check();
//          }
//        });
//      } catch (Exception e) {
//        arrayListPrice.add(new HowMuchAndWhere("kitapperver", "ಠ_ಠ", URL30));
//        check();
//        e.printStackTrace();
//      }
//    } else {
//      arrayListPrice.add(new HowMuchAndWhere("kitapperver", "□", ""));
//      check();
//    }

            if (stillSearch && selections != null && selections.contains("www.kitapsahaf.net")) {
                String URL31 = StringUtils.join("https://www.kitapsahaf.net/index.php?p=Products&q_field_active=0&q=", isbn);
                try {
                    builder.baseUrl("https://www.kitapsahaf.net/").build().create(ApiService.class).getPrices(URL31).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String str = doc.select("tr.sahaf > td:nth-child(2)").first().text();
                                    if (doc.select("#search").attr("value").equals("Ara") ||
                                            doc.select("span[class=text-danger nosell]").text().equals("STOKTA YOK") ||
                                            str.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("kitapSAHAF", "¯\\_(ツ)_/¯", URL31));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("kitapSAHAF", StringUtils.join(StringUtils.replace(str, ": ", ""), " TL"), URL31));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("kitapSAHAF", "¯\\_(ツ)_/¯", URL31));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("kitapSAHAF", "ಠ_ಠ", URL31));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("kitapSAHAF", "ಠ_ಠ", URL31));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("kitapSAHAF", "ಠ_ಠ", URL31));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("kitapSAHAF", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.kitapsarayi.com")) {
                String URL33 = StringUtils.join("https://www.kitapsarayi.com/arama/", isbn);
                try {
                    builder.baseUrl("https://www.kitapsarayi.com/").build().create(ApiService.class).getPrices(URL33).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String strKsaray = doc.select("div[class=showcasePriceOne showcasePriceSpecial]").first().text();
                                    if (doc.select("div[class=alertContentInside _textAlignCenter]").text().contains("bulunamamıştır")) {
                                        arrayListPrice.add(new HowMuchAndWhere("Kitap Sarayı", "¯\\_(ツ)_/¯", URL33));
                                        check();
                                    } else if (strKsaray.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("Kitap Sarayı", "¯\\_(ツ)_/¯", URL33));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("Kitap Sarayı", strKsaray, URL33));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("Kitap Sarayı", "¯\\_(ツ)_/¯", URL33));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("Kitap Sarayı", "ಠ_ಠ", URL33));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("Kitap Sarayı", "ಠ_ಠ", URL33));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("Kitap Sarayı", "ಠ_ಠ", URL33));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("Kitap Sarayı", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.kitapsec.com")) {
                String URL34 = StringUtils.join("https://www.kitapsec.com/Search.php?a=", isbn);
                try {
                    builder.baseUrl("https://www.kitapsec.com/").build().create(ApiService.class).getPrices(URL34).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    if (doc.select("div.Ks_ContentBlokTitle")
                                            .text().contains("(0) sonuç")) {
                                        arrayListPrice.add(new HowMuchAndWhere("kitapseç", "¯\\_(ツ)_/¯", URL34));
                                        check();
                                    } else {
                                        String strKsec = doc.select("font.satis").first().text();
                                        if (strKsec.isEmpty()) {
                                            arrayListPrice.add(new HowMuchAndWhere("kitapseç", "¯\\_(ツ)_/¯", URL34));
                                            check();
                                        } else {
                                            arrayListPrice.add(new HowMuchAndWhere("kitapseç", StringUtils.replace(strKsec, ".", ","), URL34));
                                            check();
                                        }
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("kitapseç", "ಠ_ಠ", URL34));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("kitapseç", "ಠ_ಠ", URL34));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("kitapseç", "ಠ_ಠ", URL34));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("kitapseç", "ಠ_ಠ", URL34));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("kitapseç", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.kitapsihirbazi.com")) {
                final String URL35 = StringUtils.join("https://www.kitapsihirbazi.com/index.php?p=Products&q_field_active=0&q=", isbn);
                try {
                    builder.baseUrl("https://www.kitapsihirbazi.com/").build().create(ApiService.class).getPrices(URL35).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String strKsihir = doc.select("#prd_final_price_display").first().text();
                                    if (doc.select("#search").attr("value").equals("Ara") || doc.select("div.prd_no_sell").text().equals("Baskısı tükendi") || strKsihir.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("kitap sihirbazı", "¯\\_(ツ)_/¯", URL35));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("kitap sihirbazı", StringUtils.replace(strKsihir, "TL", " TL"), URL35));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("kitap sihirbazı", "¯\\_(ツ)_/¯", URL35));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("kitap sihirbazı", "ಠ_ಠ", URL35));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("kitap sihirbazı", "ಠ_ಠ", URL35));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("kitap sihirbazı", "ಠ_ಠ", URL35));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("kitap sihirbazı", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.kitapstore.com")) {
                final String kitapstore = StringUtils.join("https://www.kitapstore.com/arama/", StringUtils.substring(isbn,3), "/");
                try {
                    builder.baseUrl("https://www.kitapstore.com/").build().create(ApiService.class).getPrices(kitapstore).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String str = doc.select("div.SFiyat > span").first().text();
                                    if (doc.select("div.Durum").text().contains("Ürün Satış Dışı") ||
                                            doc.select("div.Durum").text().contains("Tükendi") ||
                                            doc.select("div.IBaslik").text().contains("bulunamadı") || str.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("KitapStore", "¯\\_(ツ)_/¯", kitapstore));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("KitapStore", str, kitapstore));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("KitapStore", "¯\\_(ツ)_/¯", kitapstore));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("KitapStore", "ಠ_ಠ", kitapstore));
                                check();
                            }
                        }
                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("KitapStore", "ಠ_ಠ", kitapstore));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("KitapStore", "ಠ_ಠ", kitapstore));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("KitapStore", "□", ""));
                check();
            }
            if (stillSearch && selections != null && selections.contains("www.kitapvekitap.com")) {
                String URL38 = StringUtils.join("https://www.kitapvekitap.com/index.php?p=Products&q_field_active=0&ctg_id=&q=", isbn, "&search=&q_field=");
                try {
                    builder.baseUrl("https://www.kitapvekitap.com/").build().create(ApiService.class).getPrices(URL38).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String str = doc.select("#prd_final_price_display").first().text();
                                    if (doc.select("div.no_product_found > div:nth-child(1)").text().contains("bulunamadı") || doc.select("div.prd_no_sell").text().equals("Tükendi") || str.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("kitap ve kitap", "¯\\_(ツ)_/¯", URL38));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("kitap ve kitap", StringUtils.replace(str, "TL", " TL"), URL38));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("kitap ve kitap", "¯\\_(ツ)_/¯", URL38));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("kitap ve kitap", "ಠ_ಠ", URL38));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("kitap ve kitap", "ಠ_ಠ", URL38));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("kitap ve kitap", "ಠ_ಠ", URL38));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("kitap ve kitap", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.kitapzen.com")) {
                String URL40 = StringUtils.join("https://www.kitapzen.com/index.php?p=Products&q_field_active=0&ctg_id=&q=", isbn, "&search.x=0&search.y=0&q_field=");
                try {
                    builder.baseUrl("https://www.kitapzen.com/").build().create(ApiService.class).getPrices(URL40).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String strKzen = doc.select("[id=prd_final_price_display]").first().text();
                                    if (doc.select("#search").attr("value").equals("Ara") || doc.select("div[class=prd_no_sell]").text().equals("Satışta değil") || strKzen.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("kitapzen", "¯\\_(ツ)_/¯", URL40));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("kitapzen", StringUtils.join(strKzen, " TL"), URL40));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("kitapzen", "¯\\_(ツ)_/¯", URL40));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("kitapzen", "ಠ_ಠ", URL40));
                                check();
                            }
                        }
                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("kitapzen", "ಠ_ಠ", URL40));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("kitapzen", "ಠ_ಠ", URL40));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("kitapzen", "□", ""));
                check();
            }
            if (stillSearch && selections != null && selections.contains("www.nezih.com.tr")) {
                String URL44 = StringUtils.join("https://www.nezih.com.tr/urunler/ara?q=", isbn, "&page=1&s=true");
                try {
                    builder.baseUrl("https://www.nezih.com.tr/").build().create(ApiService.class).getPrices(URL44).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    boolean contains = false;
                                    doc = Jsoup.parse(response.body());
                                    for (Element table : doc.select("[class=fl col-12 catalogWrapper]")) {
                                        for (Element row : table.select("[class=col col-12 productItem]")) {
                                            if(row.select("a.fl.col-12.productBrand").text().contains(publisher) || row.select("a.fl.col-12.productBrand").text().equals(publisher)) {
                                                arrayListPrice.add(new HowMuchAndWhere("nezih", row.select("div.currentPrice").text(), URL44));
                                                check();
                                                contains = true;
                                                break;
                                            }
                                        }
                                    }
                                    if (!contains) {
                                        arrayListPrice.add(new HowMuchAndWhere("nezih", "¯\\_(ツ)_/¯", URL44));
                                        check();
                                    }
//                                    doc = Jsoup.parse(response.body());
//                                    String strKnzh = doc.select("span.price").first().text();
//                                    if (doc.select("div.container.m-top20 > div > div > div > div > h3").text().contains("bulunamamıştır") || strKnzh.isEmpty()) {
//                                        arrayListPrice.add(new HowMuchAndWhere("nezih", "¯\\_(ツ)_/¯", URL44));
//                                        check();
//                                    } else {
//                                        arrayListPrice.add(new HowMuchAndWhere("nezih", strKnzh, URL44));
//                                        check();
//                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("nezih", "¯\\_(ツ)_/¯", URL44));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("nezih", "ಠ_ಠ", URL44));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("nezih", "ಠ_ಠ", URL44));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("nezih", "ಠ_ಠ", URL44));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("nezih", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.nobelkitap.com")) {
                String URL45 = StringUtils.join("https://www.nobelkitap.com/arama?q=", isbn);
                try {
                    builder.baseUrl("https://www.nobelkitap.com/").build().create(ApiService.class).getPrices(URL45).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String strKnobel = doc.select("span.nobel_newbook_y > font").first().text();
                                    if (doc.select("div[class=nobel_newbook_des nobel_boldlink nobel_defaultlink nobel_norm_13_5px]").text().contains("bulunamadı") || doc.select("div[class=nobel_item_button_tkdi]").text().equals("Tükendi") || strKnobel.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("NOBEL KİTAP", "¯\\_(ツ)_/¯", URL45));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("NOBEL KİTAP", strKnobel, URL45));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("NOBEL KİTAP", "¯\\_(ツ)_/¯", URL45));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("NOBEL KİTAP", "ಠ_ಠ", URL45));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("NOBEL KİTAP", "ಠ_ಠ", URL45));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("NOBEL KİTAP", "ಠ_ಠ", URL45));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("NOBEL KİTAP", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.odakitap.com")) {
                String oda = StringUtils.join("https://www.odakitap.com/arama?q=", isbn);
                try {
                    builder.baseUrl("https://www.odakitap.com/").build().create(ApiService.class).getPrices(oda).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String str = doc.select("div[class=sale-price]").first().text();
                                    if (doc.select("div.cntr.pad20 > div:nth-child(6)").text().contains("bulunamadı") || doc.select("div.purchase-button-wrapper > span").text().contains("Tükendi") || str.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("odakitap", "¯\\_(ツ)_/¯", oda));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("odakitap", str, oda));
                                        check();
                                    }
                                } catch (Exception e) {
                                    try {
                                        String str2 = doc.select("div.prices-wrapper > div.price").first().text();
                                        if (doc.select("div.cntr.pad20 > div:nth-child(6)").text().contains("bulunamadı") || doc.select("div.purchase-button-wrapper > span").text().contains("Tükendi") || str2.isEmpty()) {
                                            arrayListPrice.add(new HowMuchAndWhere("odakitap", "¯\\_(ツ)_/¯", oda));
                                            check();
                                        } else {
                                            arrayListPrice.add(new HowMuchAndWhere("odakitap", str2, oda));
                                            check();
                                        }
                                    } catch (Exception e2) {
                                        arrayListPrice.add(new HowMuchAndWhere("odakitap", "¯\\_(ツ)_/¯", oda));
                                        check();
                                        e2.printStackTrace();
                                    }
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("odakitap", "ಠ_ಠ", oda));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("odakitap", "ಠ_ಠ", oda));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("odakitap", "ಠ_ಠ", oda));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("odakitap", "□", ""));
                check();
            }

            if (stillSearch && selections != null && selections.contains("www.pandora.com.tr")) {
                String URL47_1 = StringUtils.join("https://www.pandora.com.tr/Arama/?type=9&isbn=", isbn);
                try {
                    builder.baseUrl("https://www.pandora.com.tr/").build().create(ApiService.class).getPrices(URL47_1).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String strKpan = doc.select("p.indirimliFiyat").first().text();
                                    if (doc.select("p:nth-child(1) > strong").text().contains("bulunamadı.") || strKpan.isEmpty()) {
                                        arrayListPrice.add(new HowMuchAndWhere("paNdora", "¯\\_(ツ)_/¯", URL47_1));
                                        check();
                                    } else {
                                        arrayListPrice.add(new HowMuchAndWhere("paNdora", StringUtils.replace(strKpan, "Site Fiyatı: ", ""), URL47_1));
                                        check();
                                    }
                                } catch (Exception e) {
                                    arrayListPrice.add(new HowMuchAndWhere("paNdora", "¯\\_(ツ)_/¯", URL47_1));
                                    check();
                                    e.printStackTrace();
                                }
                            } else {
                                arrayListPrice.add(new HowMuchAndWhere("paNdora", "ಠ_ಠ", URL47_1));
                                check();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            arrayListPrice.add(new HowMuchAndWhere("paNdora", "ಠ_ಠ", URL47_1));
                            check();
                        }
                    });
                } catch (Exception e) {
                    arrayListPrice.add(new HowMuchAndWhere("paNdora", "ಠ_ಠ", URL47_1));
                    check();
                    e.printStackTrace();
                }
            } else {
                //arrayListPrice.add(new HowMuchAndWhere("paNdora", "□", ""));
                check();
            }
            if (stillSearch && selections != null && selections.contains("www.pirtukakurdi.com")) {
                final String URL48_2 = StringUtils.join("https://www.pirtukakurdi.com/index.php?route=product/search&search=", isbn);
                try {
                    builder.baseUrl("https://www.pirtukakurdi.com/").build().create(ApiService.class).getPrices(URL48_2).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String strKprtk = doc.select("span.price-new").first().text();
                                    if (doc.select("#content > div.row.pagination > div.col-sm-6.text-right.results").text().isEmpty() || strKprtk.isEmpty())
                                        checkFirst("pirtukakurdi", "¯\\_(ツ)_/¯", URL48_2, null);
                                    else
                                        checkFirst("pirtukakurdi", StringUtils.replace(strKprtk, "TL", " TL"), URL48_2, null);
                                } catch (Exception e) {
                                    checkFirst("pirtukakurdi", "¯\\_(ツ)_/¯", URL48_2, e);
                                }
                            } else checkFirst("pirtukakurdi", "¯\\_(ツ)_/¯", URL48_2, null);
                        }
                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            checkFirst("pirtukakurdi", "¯\\_(ツ)_/¯", URL48_2, null);
                        }
                    });
                } catch (Exception e) {
                    checkFirst("pirtukakurdi", "¯\\_(ツ)_/¯", URL48_2, e);
                }
            } else check();/*checkFirst("pirtukakurdi", "□", "", null);*/

            if (stillSearch && selections != null && selections.contains("www.sahafsalih.com")) {
                String URL50_0 = StringUtils.join("https://www.sahafsalih.com/index.php?p=Products&q_field_active=0&ctg_id=&q=", isbn);
                try {
                    builder.baseUrl("https://www.sahafsalih.com/").build().create(ApiService.class).getPrices(URL50_0).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String strKsah = doc.select("[id=prd_final_price_display]").first().text();
                                    if (doc.select("#search").attr("value").equals("Ara") || doc.select("div.no_product_found > div:nth-child(1)").text().contains("bulunamadı") || doc.select("div.prd_no_sell").text().equals("Stokta yok") || strKsah.isEmpty())
                                        checkFirst("Sahaf Salih", "¯\\_(ツ)_/¯", URL50_0, null);
                                    else
                                        checkFirst("Sahaf Salih", StringUtils.replace(strKsah, "TL", " TL"), URL50_0, null);
                                } catch (Exception e) {
                                    checkFirst("Sahaf Salih", "¯\\_(ツ)_/¯", URL50_0, e);
                                }
                            } else checkFirst("Sahaf Salih", "ಠ_ಠ", URL50_0, null);
                        }
                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            checkFirst("Sahaf Salih", "ಠ_ಠ", URL50_0, null);
                        }
                    });
                } catch (Exception e) {
                    checkFirst("Sahaf Salih", "ಠ_ಠ", URL50_0, e);
                }
            } else check();/*checkFirst("Sahaf Salih", "□", "", null);*/

            if (stillSearch && selections != null && selections.contains("www.sozcukitabevi.com")) {
                String URL52 = StringUtils.join("https://www.sozcukitabevi.com/index.php?p=Products&q_field_active=0&q=", isbn);
                try {
                    builder.baseUrl("https://www.sozcukitabevi.com/").build().create(ApiService.class).getPrices(URL52).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    doc = Jsoup.parse(response.body());
                                    String strKsozcu = doc.select("span.final_price").first().text();
                                    if (doc.select("#search").attr("value").equals("Ara") || doc.select("div.prd_no_sell").text().equals("Stokta yok") || strKsozcu.isEmpty())
                                        checkFirst("SÖZCÜ KİTABEVİ", "¯\\_(ツ)_/¯", URL52, null);
                                    else if (strKsozcu.contains(" TL"))
                                        checkFirst("SÖZCÜ KİTABEVİ", strKsozcu, URL52, null);
                                    else
                                        checkFirst("SÖZCÜ KİTABEVİ", StringUtils.replace(strKsozcu, "TL", " TL"), URL52, null);
                                } catch (Exception e) {
                                    checkFirst("SÖZCÜ KİTABEVİ", "¯\\_(ツ)_/¯", URL52, e);
                                }
                            } else checkFirst("SÖZCÜ KİTABEVİ", "ಠ_ಠ", URL52, null);
                        }
                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            checkFirst("SÖZCÜ KİTABEVİ", "ಠ_ಠ", URL52, null);
                        }
                    });
                } catch (Exception e) {
                    checkFirst("SÖZCÜ KİTABEVİ", "ಠ_ಠ", URL52, e);
                }
            } else check();/*checkFirst("SÖZCÜ KİTABEVİ", "□", "", null);*/

            if (stillSearch && selections != null && selections.contains("www.toptanasya.com")) {
                String URL53 = StringUtils.join("https://www.toptanasya.com/index.php?p=Products&q_field_active=0&ctg_id=&q=", isbn);
                try {
                    builder.baseUrl("https://www.toptanasya.com/").build().create(ApiService.class).getPrices(URL53).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try { doc = Jsoup.parse(response.body());
                                    String strKtoptan = doc.select("#prd_price_display").first().text();
                                    if (doc.select("#search").attr("value").equals("Ara") || doc.select("div.prd_no_sell").text().equals("Tükendi") || strKtoptan.isEmpty()) checkFirst("toptanasya", "¯\\_(ツ)_/¯", URL53, null);
                                    else checkFirst("toptanasya", StringUtils.join(strKtoptan, " TL"), URL53, null);
                                } catch (Exception e) { checkFirst("toptanasya", "¯\\_(ツ)_/¯", URL53, e); }
                            } else checkFirst("toptanasya", "ಠ_ಠ", URL53, null);
                        }
                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) { checkFirst("toptanasya", "ಠ_ಠ", URL53, null); }
                    });
                } catch (Exception e) {
                    checkFirst("toptanasya", "ಠ_ಠ", URL53, e);
                }
            } else check();/*checkFirst("toptanasya", "□", "", null);*/
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            Toast.makeText(this, "Lütfen tekrar deneyiniz.", Toast.LENGTH_SHORT).show();
        }
    }
    private void checkFirst(String siteName, String result, String url, Exception e) {
        arrayListPrice.add(new HowMuchAndWhere(siteName, result, url));
        check();
        if (e != null) e.printStackTrace();
    }
    //    private void check() {
//            bundle.putParcelableArrayList("arrayListPrice", arrayListPrice);
//            if (favIndex != -1) bundle.putInt("favIndex", favIndex);
//            bundle.putString("notificationTitle", StringUtils.join(name, " - ", author));
//            bundle.putString("name", name);
//            bundle.putString("author", author);
//            bundle.putString("publisher", publisher);
//            bundle.putString("coverBig", coverBig);
//            bundle.putString("description", description);
//            bundle.putString("isbn", isbn);
//            bundle.putString("individual", individual);
//            if (volume != null) bundle.putString("volume", volume);
//            if (pages != null) bundle.putString("pages", pages);
//            if (code != null && code.equals("3")) {
//                bundle.putString("code", code);
//            }
//            resultReceiver.send(1, bundle);
//    }
    private void check() {
        count += 1;
        //bundle.putString("count", String.valueOf(count));
        bundle.putParcelableArrayList("arrayListPrice", arrayListPrice);
        if (favIndex != -1) bundle.putInt("favIndex", favIndex);
        bundle.putString("notificationTitle", StringUtils.join(name, " - ", author));
        bundle.putString("name", name);
        bundle.putString("author", author);
        bundle.putString("publisher", publisher);
        bundle.putString("coverBig", coverBig);
        bundle.putString("description", description);
        bundle.putString("isbn", isbn);
        bundle.putString("individual", individual);
        if (volume != null) bundle.putString("volume", volume);
        if (pages != null) bundle.putString("pages", pages);
        if (code != null && code.equals("3")) bundle.putString("code", code);
        resultReceiver.send(1, bundle);
        if (count == all) resultReceiver.send(2, bundle);
    }
    //    private void check() {
//        count += 1;
//        bundle.putString("count", String.valueOf(count));
//        resultReceiver.send(2, bundle);
//        if (count == all) {
//            bundle.putParcelableArrayList("arrayListPrice", arrayListPrice);
//            if (favIndex != -1) bundle.putInt("favIndex", favIndex);
//            bundle.putString("notificationTitle", StringUtils.join(name, " - ", author));
//            bundle.putString("name", name);
//            bundle.putString("author", author);
//            bundle.putString("publisher", publisher);
//            bundle.putString("coverBig", coverBig);
//            bundle.putString("description", description);
//            bundle.putString("isbn", isbn);
//            bundle.putString("individual", individual);
//            if (volume != null) bundle.putString("volume", volume);
//            if (pages != null) bundle.putString("pages", pages);
//            if (code != null && code.equals("3")) {
//                bundle.putString("code", code);
//            }
//            resultReceiver.send(1, bundle);
//        }
//    }
    @SuppressLint("LongLogTag")
    @Override
    public void onCreate() {
        super.onCreate();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    public static class LogsUtil {
        private static final String processId = Integer.toString(android.os.Process.myPid());

        static StringBuilder readLogs() {
            StringBuilder logBuilder = new StringBuilder();
            try {
                String[] command = new String[]{"logcat", "-d", "threadtime"};
                Process process = Runtime.getRuntime().exec(command);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    if (line.contains(processId)) logBuilder.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return logBuilder;
        }
    }
}