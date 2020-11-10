package com.muniryenigul.kam.activities;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.material.appbar.AppBarLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieSyncManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.inqbarna.tablefixheaders.TableFixHeaders;
import com.inqbarna.tablefixheaders.adapters.BaseTableAdapter;
import com.muniryenigul.kam.MainActivity;
import com.muniryenigul.kam.R;
import com.muniryenigul.kam.ers.RecyclerTouchListener;
import com.muniryenigul.kam.ers.SuggestionAdapter;
import com.muniryenigul.kam.interfaces.ApiService;
import com.muniryenigul.kam.models.HowMuchAndWhere;
import com.muniryenigul.kam.utils.Utils;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import static com.muniryenigul.kam.MainActivity.favList;
import static com.muniryenigul.kam.MainActivity.favSingleItem;
import static com.muniryenigul.kam.activities.PriceActivity.database;
import java.io.IOException;
import java.io.InputStream;
import java.math.RoundingMode;
import java.text.Collator;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import miguelbcr.ui.tableFixHeadesWrapper.TableFixHeaderAdapter;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class TableAndBestPriceActivity extends AppCompatActivity {
    private Context context;
    private List list;
    private int y;
    private ArrayList<String> arrayListTotalPrices, contextList;
    private ArrayList<Double> arrayListMins, arrayListMaxs;
    private ArrayList<Integer> arrayListPresence;
    private ArrayList<ArrayList<HowMuchAndWhere>> arrayListAll;
    private ArrayList<ArrayList<Boolean>> arrayListBooleans;
    //private AdRequest adRequest;
    //private AdView mAdView;
    private Dialog dialog;
    //private Utils.BroadcastingInnerClass receiver;
    private Intent i;
    private String from;
    private BottomSheetBehavior behavior;
    private LinearLayout linear;
    private SuggestionAdapter comparisonAdapter;
    private ArrayList<HowMuchAndWhere> arrayListComparison;
    private TextView bestSite;
    private RecyclerView recViewComparison;
//    private WebView webView;
//    private ProgressBar progressBar;
//    private AppBarLayout appBarLayout;
    private String baseSite, baseURL;
    private List sites;


    @Override
    public void onBackPressed() {
        if (behavior.getState() == BottomSheetBehavior.STATE_EXPANDED) behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        else super.onBackPressed();
    }
    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_and_best_price);
        context = this;
        bestSite = findViewById(R.id.bestSite);
        arrayListComparison = new ArrayList<>();
        comparisonAdapter = new SuggestionAdapter(TableAndBestPriceActivity.this, arrayListComparison);
        recViewComparison = findViewById(R.id.recViewComparison);
        recViewComparison.setAdapter(comparisonAdapter);
        recViewComparison.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(TableAndBestPriceActivity.this);
        llm.setAutoMeasureEnabled(false);
        recViewComparison.setLayoutManager(llm);
        recViewComparison.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(this, R.anim.layout_animation_down_to_up));
        arrayListTotalPrices = new ArrayList<>();
        arrayListPresence = new ArrayList<>();
        arrayListBooleans = new ArrayList<>();
        arrayListMins = new ArrayList<>();
        arrayListMaxs = new ArrayList<>();
        arrayListAll = new ArrayList<>();
        linear = findViewById(R.id.linear);
        list = Arrays.asList(getResources().getStringArray(R.array.listOptions));
        Collections.replaceAll(list, "maksimumkitap", "maksimum kitap");
        Collections.replaceAll(list, "KİTAPAMBARI", "KİTAP AMBARI");
        y = list.size();
        i = getIntent();
        from = i.getStringExtra("from");
        behavior = BottomSheetBehavior.from(linear);
        TableFixHeaders tablefixheaders = findViewById(R.id.tablefixheaders);
        tablefixheaders.setAdapter(getAdapter());
    }
    public void expand(View view) {
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }
    public BaseTableAdapter getAdapter() {
        BasicTableFixHeaderAdapter adapter = new BasicTableFixHeaderAdapter(this);
        List<List<String>> body = null;
        try {
            if (from.equals("findOptimumForAll")) body = getBody(favList);
            else {
                contextList = i.getStringArrayListExtra("contextList");
                body = getBody(contextList);
            }
        } catch (Exception e) { catchAndSend("getAdapter "+e.getLocalizedMessage()); }
        adapter.setFirstHeader("ÇİTLEMBİK");
        adapter.setHeader(getHeader());
        adapter.setFirstBody(body);
        adapter.setBody(body);
        adapter.setSection(body);
        return adapter;
    }
    private List<String> getHeader() {
        List<String> header = new ArrayList<>();
        for (int i = 0; i < y; i++) {
            header.add(list.get(i).toString());
        }
        return header;
    }
    private List<List<String>> getBody(ArrayList<String> list) {
        List<List<String>> rows = new ArrayList<>();
        List<String> cols = new ArrayList<>();
        DecimalFormat formatter = new DecimalFormat("0.00 TL");
        formatter.setRoundingMode(RoundingMode.DOWN);
        StringBuilder sb;
        for (int p = 0; p < list.size(); p++) {
            cols = new ArrayList<>();
            cols.add(list.get(p));
            ArrayList<HowMuchAndWhere> arrayListComparison = new ArrayList<>();
            ArrayList<String> arraylistSpare = new ArrayList<>();
            try {
                database = openOrCreateDatabase(StringUtils.replace(list.get(p), "/", ""), MODE_PRIVATE, null);
                database.execSQL("CREATE TABLE IF NOT EXISTS data (site VARCHAR, price VARCHAR, url VARCHAR)");
                Cursor cursor = database.rawQuery("SELECT * FROM data", null);
                int siteIx = cursor.getColumnIndex("site");
                int priceIx = cursor.getColumnIndex("price");
                int urlIx = cursor.getColumnIndex("url");
                if (cursor.moveToFirst()) {
                    do {
                        arraylistSpare.add(cursor.getString(siteIx));
                        arrayListComparison.add(new HowMuchAndWhere(cursor.getString(siteIx), cursor.getString(priceIx), cursor.getString(urlIx)));
                    } while (cursor.moveToNext());
                }
                cursor.close();
                database.close();
                for(String site : getResources().getStringArray(R.array.listOptions)) {
                    if(!arraylistSpare.contains(StringUtils.replace(site,"amp;",""))) {
                        arrayListComparison.add(new HowMuchAndWhere(StringUtils.replace(site,"amp;",""), "□", ""));
                        Log.d("!contains",site);
                    }
                }
            } catch (Exception e) {
                catchAndSend("getBody "+e.getLocalizedMessage());
            }

            Collections.sort(arrayListComparison, (o1, o2) -> {
                Collator collator = Collator.getInstance(new Locale("tr", "TR"));
                return collator.compare(StringUtils.replace(o1.getSite().toLowerCase(new Locale("tr", "TR")), " ", ""), StringUtils.replace(o2.getSite().toLowerCase(new Locale("tr", "TR")), " ", ""));
            });
            ArrayList<Double> spare = new ArrayList<>();
            for (int s = 0; s < arrayListComparison.size(); s++) {
                cols.add(arrayListComparison.get(s).getPrice());
                if (!arrayListComparison.get(s).getPrice().equals("ಠ_ಠ") && !arrayListComparison.get(s).getPrice().equals("¯\\_(ツ)_/¯") && !arrayListComparison.get(s).getPrice().equals("□"))
                    spare.add(Double.parseDouble(StringUtils.replace(StringUtils.replace(arrayListComparison.get(s).getPrice(), ",", "."), " TL", "")));
            }
            Log.d("cols",cols.toString());
            try {
                arrayListMaxs.add(Collections.max(spare));
            } catch (Exception e) {
                arrayListMaxs.add(0.00);
                e.printStackTrace();
            }
            try {
                arrayListMins.add(Collections.min(spare));
            } catch (Exception e) {
                arrayListMins.add(0.00);
                e.printStackTrace();
            }
            arrayListAll.add(arrayListComparison);
            rows.add(cols);
        }
        try {
            for (int o = 1; o < cols.size(); o++) {
                double totalPrice = 0;
                int presence = 0;
                sb = new StringBuilder();
                ArrayList<Boolean> arrayListBoolean = new ArrayList<>();
                for (int y = 0; y < rows.size(); y++) {
                    try {
                        if (rows.get(y).get(o).contains("TL")) {
                            presence++;
                            arrayListBoolean.add(true);
                        } else arrayListBoolean.add(false);
                        totalPrice += Double.parseDouble(StringUtils.replace(StringUtils.replace(StringUtils.replace(StringUtils.replace(StringUtils.replace(rows.get(y).get(o), "□", "0"), "ಠ_ಠ", "0"), "¯\\_(ツ)_/¯", "0"), ",", "."), " TL", ""));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
                arrayListPresence.add(presence);
                arrayListBooleans.add(arrayListBoolean);
                arrayListTotalPrices.add(StringUtils.replace(sb.append(formatter.format(totalPrice)).toString(), ".", ","));
            }
            try {
                String json = null;
                try {
                    InputStream inputStream = this.getResources().openRawResource(R.raw.info);
                    int size = inputStream.available();
                    byte[] buffer = new byte[size];
                    inputStream.read(buffer);
                    inputStream.close();
                    json = new String(buffer, "UTF-8");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                List siteLink = Arrays.asList(getResources().getStringArray(R.array.listValues));
                List siteList = Arrays.asList(getResources().getStringArray(R.array.listOptions));
                int max = Collections.max(arrayListPresence);
                if (max != favList.size()) {
                    StringBuilder stringBuilder = null;
                    if(max == 0) {
                        Log.d("max","== 0");
                        linear.setVisibility(View.GONE);
                        Toast.makeText(this, ":(", Toast.LENGTH_LONG).show();
                        Toast.makeText(this, "Ne yazık ki, favorilerdeki tüm kitapların alınabileceği bir site şu an için bulunamadı.", Toast.LENGTH_LONG).show();
                    } else {
                        Log.d("max","!= 0");
                        while (max>0) {
                            for (int i = 0; i < arrayListPresence.size(); i++) {
                                stringBuilder = new StringBuilder();
                                if(max == arrayListPresence.get(i)) {
                                    ArrayList<Boolean> arrayList = arrayListBooleans.get(i/*arrayListPresence.indexOf(max)*/);
                                    //stringBuilder.append("max :"+max+"\n");
                                    for (int m = 0 ; m < arrayList.size() ; m++) {
                                        if(arrayList.get(m)) stringBuilder.append(",\n").append(favList.get(m)); }
                                    //stringBuilder.append("\n");
                                    bestSite.setText(":(\nNe yazık ki, favorilerdeki tüm kitapların alınabileceği bir site şu an için bulunamadı.\nAşağıdaki kombinasyonlar değerlendirilebilir.");
                                    createBody(StringUtils.replaceOnce(stringBuilder.toString(),",\n",""), formatter, siteLink, siteList, json, i);
                                }
                            }
                            max--;
                        }
                    }
                } else {
                    arrayListComparison.clear();
                    Log.d("max","== favList.size()");
                    for (int i = 0; i < arrayListPresence.size(); i++) {
                        if (arrayListPresence.get(i).equals(Collections.max(arrayListPresence))) {
                             createBody(null, formatter, siteLink, siteList, json, i);
                        }
                    }
                    try {
                        if(arrayListComparison.size() == 0) {
                            linear.setVisibility(View.GONE);
                            Toast.makeText(this, ":(", Toast.LENGTH_LONG).show();
                            Toast.makeText(this, "Ne yazık ki, favorilerdeki tüm kitapların alınabileceği bir site şu an için bulunamadı.", Toast.LENGTH_LONG).show();
                        } else if(arrayListComparison.size() == 1) bestSite.setText(R.string.best_site);
                        else {
                            Collections.sort(arrayListComparison, (o1, o2) -> (int) (extractDouble(o1.getTotalPrice()) - extractDouble(o2.getTotalPrice())));
                            bestSite.setText(R.string.best_sites);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    recViewComparison.getRecycledViewPool().clear();
                    comparisonAdapter.notifyDataSetChanged();
                }
            } catch (Resources.NotFoundException | NumberFormatException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
            catchAndSend("getBody2 "+e.getLocalizedMessage());
        }
        cols = new ArrayList<>();
        cols.add("TOPLAM");
        cols.addAll(arrayListTotalPrices);
        rows.add(cols);
        return rows;
    }
    private void createBody (String howMuch, DecimalFormat formatter, List siteLink, List siteList, String json, int i) {
        try {
            String payAtTheDoorCash = null, payAtTheDoorCard = null, otherMethods = null, info, cargo_price, total_price;
            StringBuilder accTotal, accTransfer, accCargo, accInfo, accPayAtTheDoorCash, accPayAtTheDoorCard, accPaymentMethod;
            double price;
            boolean isFree = false, hasCard = false, hasTransfer = false, hasPayPal = false;
            JSONArray obj = new JSONObject(json).getJSONArray((String) siteList.get(i));
            price = Double.parseDouble(StringUtils.replace(StringUtils.replace(arrayListTotalPrices.get(i), ",", "."), " TL", ""));
            switch ((String) siteList.get(i)) {
                /*case "724kitapal": break;*/
                case "ADA": {
                    if (price >= 75) arrayListComparison.add(new HowMuchAndWhere(howMuch, (String) siteLink.get(i), arrayListTotalPrices.get(i), "PTT Kargo || Yurtiçi Kargo : Ücretsiz Kargo", StringUtils.replace(arrayListTotalPrices.get(i), ".", ","), true, false, false, false, "Sanal POS", null, null, "Sadece Sanal POS ile ödeme yapılabiliyor.", null));
                    break;
                }
                case "ALOKİTABEVİ": {
                    boolean isfree = false;
                    if (price < 100) {
                        total_price = StringUtils.join(StringUtils.replace(formatter.format(price + 9.9), ".", ","),"\n","Havale ile ödeme : ",StringUtils.replace(formatter.format(price*0.97 + 9.9), ".", ","));
                        cargo_price = "Yurtiçi Kargo : 9,90 TL";
                    } else if (price < 150) {
                        total_price = StringUtils.join(StringUtils.replace(formatter.format(price + 7.9), ".", ","),"\n","Havale ile ödeme : ",StringUtils.replace(formatter.format(price*0.97 + 7.9), ".", ","));
                        cargo_price = "Yurtiçi Kargo : 7,90 TL";
                    } else {
                        isfree = true;
                        total_price = StringUtils.join(StringUtils.replace(formatter.format(price), ".", ","),"\n","Havale ile ödeme : ",StringUtils.replace(formatter.format(price*0.97), ".", ","));
                        cargo_price = "Yurtiçi Kargo : Ücretsiz Kargo";
                    }
                    arrayListComparison.add(new HowMuchAndWhere(howMuch, (String) siteLink.get(i), arrayListTotalPrices.get(i), cargo_price, total_price, isfree, true, true, false, null, "Kapıda Nakit Ödeme : + 8 TL", "Kapıda Kredi Kartı ile Ödeme : + 10 TL", "Havale ile ödemede %3 indirim.", null));
                    break;
                }
                case "AperatifKitap": {
                    boolean isfree = false;
                    String cargoPrice;
                    if(price < 30 ) {
                        total_price = StringUtils.join("MNG Kargo || Aras Kargo : ",StringUtils.replace(formatter.format(price + 8), ".", ","));
                        cargo_price = "MNG Kargo || Aras Kargo : 8 TL";
                    } else if(price < 40 ) {
                        total_price = StringUtils.join("MNG Kargo || Aras Kargo : ",StringUtils.replace(formatter.format(price + 8.25), ".", ","));
                        cargo_price = "MNG Kargo || Aras Kargo : 8,25 TL";
                    } else if(price < 50 ) {
                        total_price = StringUtils.join("MNG Kargo || Aras Kargo : ",StringUtils.replace(formatter.format(price + 8.5), ".", ","));
                        cargo_price = "MNG Kargo || Aras Kargo : 8,50 TL";
                    } else if(price < 60 ) {
                        total_price = StringUtils.join("MNG Kargo || Aras Kargo : ",StringUtils.replace(formatter.format(price + 8.75), ".", ","));
                        cargo_price = "MNG Kargo || Aras Kargo : 8,75 TL";
                    } else if(price < 80 ) {
                        total_price = StringUtils.join("MNG Kargo || Aras Kargo : ",StringUtils.replace(formatter.format(price + 8.9), ".", ","));
                        cargo_price = "MNG Kargo || Aras Kargo : 8,90 TL";
                    } else if(price < 90 ) {
                        total_price = StringUtils.join("Aras Kargo : ",StringUtils.replace(formatter.format(price + 8.9), ".", ","),"\nMNG Kargo : ",StringUtils.replace(formatter.format(price + 9.5), ".", ","));
                        cargo_price = "Aras Kargo : 8,90 TL\nMNG Kargo : 9,50 TL";
                    } else if(price < 100 ) {
                        total_price = StringUtils.join("MNG Kargo || Aras Kargo : ",StringUtils.replace(formatter.format(price + 9.9), ".", ","));
                        cargo_price = "MNG Kargo || Aras Kargo : 9,90 TL";
                    } else if(price < 120 ) {
                        total_price = StringUtils.join("MNG Kargo : ",StringUtils.replace(formatter.format(price), ".", ","),"\nAras Kargo : ",StringUtils.replace(formatter.format(price + 9.9), ".", ","));
                        cargo_price = "MNG Kargo : Ücretsiz Kargo\nAras Kargo : 9,90 TL";
                        isfree = true;
                    } else if(price < 150 ) {
                        total_price = StringUtils.join("MNG Kargo : ",StringUtils.replace(formatter.format(price), ".", ","),"\nAras Kargo : ",StringUtils.replace(formatter.format(price + 12.9), ".", ","));
                        cargo_price = "MNG Kargo : Ücretsiz Kargo\nAras Kargo : 12,90 TL";
                        isfree = true;
                    } else {
                        total_price = StringUtils.join("MNG Kargo || Aras Kargo : ",StringUtils.replace(formatter.format(price), ".", ","));
                        cargo_price = "MNG Kargo || Aras Kargo : Ücretsiz Kargo";
                        isfree = true;
                    }
                    arrayListComparison.add(new HowMuchAndWhere(howMuch, (String) siteLink.get(i), arrayListTotalPrices.get(i), cargo_price, total_price, isfree, true, true, false, "Garanti Pay", null, null, null, null));
                    break;
                }
                case "bkmkitap": {
                    /*if(price > 29.9) {*/
                        boolean isfree = false;
                        if (price < 50) {
                            total_price = StringUtils.join("PTT Kargo : ",StringUtils.replace(formatter.format(price + 5.99), ".", ","),"\n","MNG Kargo : ",StringUtils.replace(formatter.format(price + 9.99), ".", ","));
                            cargo_price = "PTT Kargo : 5,99 TL\nMNG Kargo : 9,99 TL";
                        } else if (price < 75) {
                            total_price = StringUtils.join("PTT Kargo : ",StringUtils.replace(formatter.format(price + 3.99), ".", ","),"\n","MNG Kargo : ",StringUtils.replace(formatter.format(price + 7.99), ".", ","));
                            cargo_price = "PTT Kargo : 3,99 TL\nMNG Kargo : 7,99 TL";
                        } else {
                            isfree = true;
                            total_price = StringUtils.join("PTT Kargo : ",StringUtils.replace(formatter.format(price), ".", ","),"\n","MNG Kargo : ",StringUtils.replace(formatter.format(price + 3.99), ".", ","));
                            cargo_price = "PTT Kargo : Ücretsiz Kargo\nMNG Kargo : 3,99 TL";
                        }
                        arrayListComparison.add(new HowMuchAndWhere(howMuch, (String) siteLink.get(i), arrayListTotalPrices.get(i), cargo_price, total_price, isfree, true, true, false, "BKM Express, iyzico", "Kapıda Nakit Ödeme : + 6,99 TL", null, "Alışveriş yapabilmek için tutar 29,90 TL üzerinde olmalıdır.\nMobil uygulamasından yapılacak 30 TL ve üzeri alışverişlerde kargo bedava.", null));
                        break;
                    /*} else break;*/
                }
                case "atlaskitap": {
                    boolean isfree = true;
                    if (price < 50) {
                        isfree = false;
                        total_price = StringUtils.replace(formatter.format(price + 5), ".", ",");
                        cargo_price = "Bireysel Üye || Ticari Üye -\nAras Kargo || Yurtiçi Kargo : 5 TL";
                    } else if (price < 139) {
                        total_price = StringUtils.join("(Bireysel Üye) : ", StringUtils.replace(formatter.format(price), ".", ","), "\n(Ticari Üye) :", StringUtils.replace(formatter.format(price + 5), ".", ","));
                        cargo_price = "Bireysel Üye - Aras Kargo || Yurtiçi Kargo : Ücretsiz Kargo\n Ticari Üye - Aras Kargo || Yurtiçi Kargo : 5 TL";
                    } else {
                        total_price = StringUtils.replace(formatter.format(price), ".", ",");
                        cargo_price = "Bireysel Üye || Ticari Üye -\nAras Kargo || Yurtiçi Kargo : Ücretsiz Kargo";
                    }
                    arrayListComparison.add(new HowMuchAndWhere(howMuch, (String) siteLink.get(i), arrayListTotalPrices.get(i), cargo_price, total_price, isfree, true, true, false, "Posta Çeki", "Kapıda Nakit Ödeme : +6 TL", null, null, null));
                    break;
                }
                case "çamlıca kitap": {
                    boolean isfree = false;
                    if (price < 75) {
                        total_price = StringUtils.join("Aras Kargo : ", StringUtils.replace(formatter.format(price + 7.99), ".", ","),
                                "\nUPS Kargo : ", StringUtils.replace(formatter.format(price + 11), ".", ","));
                        cargo_price = "Aras Kargo : 7,99 TL\nUPS Kargo : 11 TL";
                    } else {
                        isfree = true;
                        total_price = StringUtils.join("Aras Kargo || UPS Kargo : ", StringUtils.replace(formatter.format(price), ".", ","));
                        cargo_price = "Aras Kargo || UPS Kargo : Ücretsiz Kargo";
                    }
                    arrayListComparison.add(new HowMuchAndWhere(howMuch, (String) siteLink.get(i), arrayListTotalPrices.get(i), cargo_price, total_price, isfree, true, true, false, null, "Kapıda Nakit Ödeme (Aras Kargo) : +8 TL", null, null, null));
                    break;
                }
                case "başarı DAĞITIM": break;
                case "DİLEKKİTAP": {
                    boolean isfree = false;
                    String payAtTheDoorCashACC = "Kapıda Nakit Ödeme (MNG Kargo) : +5 TL";
                    if (price < 30) {
                        payAtTheDoorCashACC = null;
                        total_price = StringUtils.join(StringUtils.replace(formatter.format(price + 7.0), ".", ","), "\n", "Havale ile Ödeme : ", StringUtils.replace(formatter.format(price * 0.98 + 7.0), ".", ","));
                        cargo_price = "MNG Kargo : 7,00 TL";
                    } else if (price < 99) {
                        total_price = StringUtils.join(StringUtils.replace(formatter.format(price + 7.0), ".", ","), "\n", "Havale ile Ödeme : ", StringUtils.replace(formatter.format(price * 0.98 + 7.0), ".", ","));
                        cargo_price = "MNG Kargo : 7,00 TL";
                    } else {
                        isfree = true;
                        total_price = StringUtils.join(StringUtils.replace(formatter.format(price), ".", ","), "\n", "Havale ile Ödeme : ", StringUtils.replace(formatter.format(price * 0.98), ".", ","));
                        cargo_price = "MNG Kargo : Ücretsiz Kargo";
                    }
                    arrayListComparison.add(new HowMuchAndWhere(howMuch, (String) siteLink.get(i), arrayListTotalPrices.get(i), cargo_price, total_price, isfree, true, true, false, null, payAtTheDoorCashACC, null, "Havale / EFT ile verilen siparişler %2 indirimli. \nKapıda ödeme 30,00 TL - 5.000,00 TL aralığındaki siparişlerde geçerlidir.", null));
                    break;
                }
                case "D&R": {
                    boolean isfree = false;
                    if (price < 100) {
                        total_price = StringUtils.join("Standart Teslimat : ", StringUtils.replace(formatter.format(price + 7.99), ".", ","),
                                "\nHızlı Teslimat : ", StringUtils.replace(formatter.format(price + 11.99), ".", ","));
                        cargo_price = "Standart Teslimat : 7,99 TL\nHızlı Teslimat : 11,99 TL";
                    } else {
                        isfree = true;
                        total_price = StringUtils.join("Standart Teslimat : ", StringUtils.replace(formatter.format(price), ".", ","),
                                "\nHızlı Teslimat : ", StringUtils.replace(formatter.format(price + 11.99), ".", ","));
                        cargo_price = "Standart Teslimat : Ücretsiz Kargo\nHızlı Teslimat : 11,99 TL";
                    }
                    arrayListComparison.add(new HowMuchAndWhere(howMuch, (String) siteLink.get(i), arrayListTotalPrices.get(i), cargo_price, total_price, isfree, true, true, false, "BKM Express, Garanti Pay", null, null, null, null));
                    break;
                }
                case "eflatun KİTAP": {
                    boolean isfree = false;
                    if (price < 75) {
                        total_price = StringUtils.replace(formatter.format(price + 9), ".", ",");
                        cargo_price = "9 TL";
                    } else {
                        isfree = true;
                        total_price = StringUtils.replace(formatter.format(price), ".", ",");
                        cargo_price = "Ücretsiz Kargo";
                    }
                    arrayListComparison.add(new HowMuchAndWhere(howMuch, (String) siteLink.get(i), arrayListTotalPrices.get(i), cargo_price, total_price, isfree, true, true, false, null, null, null, null, null));
                    break;
                }
                case "elitkitap": {
                    boolean isfree = false;
                    if (price < 75) {
                        total_price = StringUtils.join(StringUtils.replace(formatter.format(price + 8), ".", ","),
                                "\nHavale ile Ödeme : ", StringUtils.replace(formatter.format(price * 0.98 + 8), ".", ","));
                        cargo_price = "Aras Kargo : 8 TL";
                    } else {
                        isfree = true;
                        total_price = StringUtils.join(StringUtils.replace(formatter.format(price), ".", ","),
                                "\nHavale ile Ödeme : ", StringUtils.replace(formatter.format(price * 0.98), ".", ","));
                        cargo_price = "Aras Kargo : Ücretsiz Kargo";
                    }
                    arrayListComparison.add(new HowMuchAndWhere(howMuch, (String) siteLink.get(i), arrayListTotalPrices.get(i), cargo_price, total_price, isfree, true, true, false, null, "Kapıda Nakit Ödeme : +7 TL", "Kapıda Kredi Kartı İle Ödeme : +7 TL", "Havale ile ödeme %2 indirimli.", null));
                    break;
                }
                case "EREN Kitap": {
                    boolean isfree = false;
                    double cargo;
                    int size = favList.size();
                    if (size < 8) cargo = size + 7;
                    else cargo = 15;
                    if (price < 50) {
                        total_price = StringUtils.replace(formatter.format(price + cargo), ".", ",");
                        cargo_price = StringUtils.join("Yurtiçi Kargo : ", StringUtils.replace(formatter.format(cargo), ".", ","));
                    } else if (price < 75) {
                        total_price = StringUtils.replace(formatter.format(price + cargo * 0.9), ".", ",");
                        cargo_price = StringUtils.join("Yurtiçi Kargo : ", StringUtils.replace(formatter.format(cargo), ".", ","));
                    } else if (price < 100) {
                        total_price = StringUtils.replace(formatter.format(price + cargo * 0.8), ".", ",");
                        cargo_price = StringUtils.join("Yurtiçi Kargo : ", StringUtils.replace(formatter.format(cargo), ".", ","));
                    } else {
                        isfree = true;
                        total_price = StringUtils.replace(formatter.format(price), ".", ",");
                        cargo_price = "Yurtiçi Kargo : Ücretsiz Kargo";
                    }
                    arrayListComparison.add(new HowMuchAndWhere(howMuch, (String) siteLink.get(i), arrayListTotalPrices.get(i), cargo_price, total_price, isfree, true, false, false, null, null, null, "Kargo ücreti 50 TL üzeri spiarişlerde %10, 75 TL üzeri siparişlerde %20 indirimli.\nKredi Kartıyla 3D ödeme, 100 TL üzeri alışverişlerde geçerli.", null));
                    break;
                }
                case "EVRENSEL KİTABEVİ": {
                    boolean isfree = false;
                    if (price < 75) {
                        total_price = StringUtils.join("Aras Kargo : ", StringUtils.replace(formatter.format(price + 9.9), ".", ","),
                                "\nAnkara Kızılay Şubeden Teslimat : ", StringUtils.replace(formatter.format(price + 0.01), ".", ","));
                        cargo_price = "Aras Kargo : 9,90 TL\nAnkara Kızılay Şubeden Teslimat: 0,01 TL";
                    } else {
                        isfree = true;
                        total_price = StringUtils.join("Aras Kargo || Ankara Kızılay Şubeden Teslimat : ",StringUtils.replace(formatter.format(price), ".", ","));
                        cargo_price = "Aras Kargo : Ücretsiz Kargo || Ankara Kızılay Şubeden Teslimat";
                    }
                    arrayListComparison.add(new HowMuchAndWhere(howMuch, (String) siteLink.get(i), arrayListTotalPrices.get(i), cargo_price, total_price, isfree, true, true, false, "BKM Express, Garanti Pay", null, null, null, null));
                    break;
                }
                case "fatihkitap": {
                    boolean isfree = false;
                    if (price < 175) {
                        total_price = StringUtils.replace(formatter.format(price + 10.99), ".", ",");
                        cargo_price = "Yurtiçi Kargo : 10,99 TL";
                    } else {
                        isfree = true;
                        total_price = StringUtils.join(StringUtils.replace(formatter.format(price), ".", ","));
                        cargo_price = "Yurtiçi Kargo : Ücretsiz Kargo";
                    }
                    arrayListComparison.add(new HowMuchAndWhere(howMuch, (String) siteLink.get(i), arrayListTotalPrices.get(i), cargo_price, total_price, isfree, true, true, false, null, "Kapıda Nakit Ödeme : +10,45 TL", null, null, null));
                    break;
                }
                case "FİDAN KİTAP": {
                    boolean isfree = false;
                    boolean hasTransferACC = true, hasCardACC = true;
                    if (price < 30) {
                        hasTransferACC = false;
                        hasCardACC = false;
                        total_price = StringUtils.join("PTT Kargo : ",StringUtils.replace(formatter.format(price + 5), ".", ","),
                                "\nAras Kargo : ",StringUtils.replace(formatter.format(price + 8.90), ".", ","));
                        cargo_price = "PTT Kargo : 5 TL\nAras Kargo : 8,90 TL";
                    } else if (price < 70) {
                        total_price = StringUtils.join("PTT Kargo : ",StringUtils.replace(formatter.format(price + 5), ".", ","),
                                "\nAras Kargo : ",StringUtils.replace(formatter.format(price + 8.90), ".", ","));
                        cargo_price = "PTT Kargo : 5 TL\nAras Kargo : 8,90 TL";
                    } else if (price < 100) {
                        total_price = StringUtils.join("PTT Kargo : ",StringUtils.replace(formatter.format(price + 2.5), ".", ","),
                                "\nAras Kargo : ",StringUtils.replace(formatter.format(price + 8.90), ".", ","));
                        cargo_price = "PTT Kargo : 2,5 TL\nAras Kargo : 8,90 TL";
                    } else if (price < 150) {
                        total_price = StringUtils.join("PTT Kargo : ",StringUtils.replace(formatter.format(price), ".", ","),
                                "\nAras Kargo : ",StringUtils.replace(formatter.format(price + 8.90), ".", ","));
                        cargo_price = "PTT Kargo : Ücretsiz Kargo\nAras Kargo : 8,90 TL";
                    } else {
                        isfree = true;
                        total_price = StringUtils.replace(formatter.format(price), ".", ",");
                        cargo_price = "PTT Kargo || Aras Kargo : Ücretsiz Kargo";
                    }
                    arrayListComparison.add(new HowMuchAndWhere(howMuch, (String) siteLink.get(i), arrayListTotalPrices.get(i), cargo_price, total_price, isfree, hasCardACC, hasTransferACC, false, null, "Kapıda Nakit Ödeme\nPTT Kargo : +5 TL\nAras Kargo : +6 TL", null, "Havale/EFT ve kredi kartıyla ödeme, 30 TL ve üzeri siparişlerde geçerli.", null));
                    break;
                }
                case "Garanti Kitap": {
                    boolean isfree = false;
                    if (price < 100) {
                        total_price = StringUtils.replace(formatter.format(price + 10), ".", ",");
                        cargo_price = "Aras Kargo : 10 TL";
                    } else {
                        isfree = true;
                        total_price = StringUtils.join(StringUtils.replace(formatter.format(price), ".", ","));
                        cargo_price = "Aras Kargo : Ücretsiz Kargo";
                    }
                    arrayListComparison.add(new HowMuchAndWhere(howMuch, (String) siteLink.get(i), arrayListTotalPrices.get(i), cargo_price, total_price, isfree, true, true, false, null, null, null, null, null));
                    break;
                }
                case "Gazi Kitabevi": {
                    boolean isfree = false;
                    if (price < 100) {
                        total_price = StringUtils.replace(formatter.format(price + 8), ".", ",");
                        cargo_price = "Aras Kargo || Yurtiçi Kargo : 8 TL";
                    } else {
                        isfree = true;
                        total_price = StringUtils.join(StringUtils.replace(formatter.format(price), ".", ","));
                        cargo_price = "Aras Kargo || Yurtiçi Kargo : Ücretsiz Kargo";
                    }
                    arrayListComparison.add(new HowMuchAndWhere(howMuch, (String) siteLink.get(i), arrayListTotalPrices.get(i), cargo_price, total_price, isfree, true, true, false, null, null, null, null, null));
                    break;
                }
                case "harmankitap": break;
                case "Hipokrat Kitabevi": {
                    boolean isfree = false;
                    if (price < 75) {
                        total_price = StringUtils.join(StringUtils.replace(formatter.format(price + 6), ".", ","),
                                "\nHavale ile Ödeme : ", StringUtils.replace(formatter.format(price * 0.98 + 6), ".", ","));
                        cargo_price = "Aras Kargo : 6 TL";
                    } else {
                        isfree = true;
                        total_price = StringUtils.join(StringUtils.replace(formatter.format(price), ".", ","),
                                "\nHavale ile Ödeme : ", StringUtils.replace(formatter.format(price * 0.98), ".", ","));
                        cargo_price = "Aras Kargo : Ücretsiz Kargo";
                    }
                    arrayListComparison.add(new HowMuchAndWhere(howMuch, (String) siteLink.get(i), arrayListTotalPrices.get(i), cargo_price, total_price, isfree, true, true, false, null, "Kapıda Nakit Ödeme : +6 TL", "Kapıda Kredi Kartı İle Ödeme : +6 TL", "Havale ile ödeme %2 indirimli.", null));
                    break;
                }
                case "idefix": {
                    boolean isfree = false;
                    if (price < 50) {
                        total_price = StringUtils.replace(formatter.format(price + 7.99), ".", ",");
                        cargo_price = "Yurtiçi Kargo : 7,99 TL";
                    } else if (price < 75) {
                        total_price = StringUtils.replace(formatter.format(price + 4.99), ".", ",");
                        cargo_price = "Yurtiçi Kargo : 4,99 TL";
                    } else {
                        isfree = true;
                        total_price = StringUtils.replace(formatter.format(price), ".", ",");
                        cargo_price = "Yurtiçi Kargo : Ücretsiz Kargo";
                    }
                    arrayListComparison.add(new HowMuchAndWhere(howMuch, (String) siteLink.get(i), arrayListTotalPrices.get(i), cargo_price, total_price, isfree, true, true, false, "BKM Express", null, null, null, null));
                    break;
                }
                case "ilkNokta": {
                    boolean isfree = false;
                    boolean hasTransferACC = true, hasCardACC = true;
                    if (price < 59) {
                        hasTransferACC = false;
                        hasCardACC = false;
                        total_price = StringUtils.join("Aras Kargo : ",StringUtils.replace(formatter.format(price + 10), ".", ","),
                                "\nAtaşehir Yedpa Teslim : ",StringUtils.replace(formatter.format(price), ".", ","));
                        cargo_price = "Aras Kargo : 10 TL";
                    } else if (price < 75) {
                        hasTransferACC = false;
                        hasCardACC = false;
                        total_price = StringUtils.join("Aras Kargo : ",StringUtils.replace(formatter.format(price + 5), ".", ","),
                                "\nAtaşehir Yedpa Teslim : ",StringUtils.replace(formatter.format(price), ".", ","));
                        cargo_price = "Aras Kargo : 5 TL";
                    } else if (price < 99) {
                        total_price = StringUtils.join("Aras Kargo : ",StringUtils.replace(formatter.format(price + 5), ".", ","),
                                "\nAtaşehir Yedpa Teslim : ",StringUtils.replace(formatter.format(price), ".", ","));
                        cargo_price = "Aras Kargo : 5 TL";
                    } else {
                        isfree = true;
                        total_price = StringUtils.join("Aras Kargo : ",StringUtils.replace(formatter.format(price), ".", ","),
                                "\nAtaşehir Yedpa Teslim : ",StringUtils.replace(formatter.format(price), ".", ","));
                        cargo_price = "Aras Kargo : Ücretsiz Kargo";
                    }
                    arrayListComparison.add(new HowMuchAndWhere(howMuch, (String) siteLink.get(i), arrayListTotalPrices.get(i), cargo_price, total_price, isfree, hasCardACC, hasTransferACC, false, null, null, null, "Kredi kartıyla taksitli / 3D ödeme, 75 TL ve üzeri siparişlerde geçerli.", null));
                    break;
                }
                case "KABALCI": break;
                case "Kariyer Kitapları": {
                    boolean isfree = false;
                    if (price < 100) {
                        total_price = StringUtils.join(StringUtils.replace(formatter.format(price + 7), ".", ","),
                                "\nHavale ile Ödeme : ", StringUtils.replace(formatter.format(price * 0.98 + 7), ".", ","));
                        cargo_price = "MNG Kargo : 7 TL";
                    } else {
                        isfree = true;
                        total_price = StringUtils.join(StringUtils.replace(formatter.format(price), ".", ","),
                                "\nHavale ile Ödeme : ", StringUtils.replace(formatter.format(price * 0.98), ".", ","));
                        cargo_price = "MNG Kargo : Ücretsiz Kargo";
                    }
                    arrayListComparison.add(new HowMuchAndWhere(howMuch, (String) siteLink.get(i), arrayListTotalPrices.get(i), cargo_price, total_price, isfree, true, true, false, null, null, null, "Havale ile ödeme %2 indirimli.", null));
                    break;
                }
                case "kitabevimiz": {
                    boolean isfree = false;
                    String payAtTheDoorCashACC = "Kapıda Nakit Ödeme : +6,50 TL";
                    String payAtTheDoorCardACC = "Kapıda Kredi Kartı İle Ödeme : +6,50 TL";
                    if (price < 100) {
                        payAtTheDoorCashACC = null;
                        payAtTheDoorCardACC = null;
                        total_price = StringUtils.replace(formatter.format(price + 5), ".", ",");
                        cargo_price = "PTT Kargo : 5 TL";
                    } else {
                        isfree = true;
                        total_price = StringUtils.replace(formatter.format(price), ".", ",");
                        cargo_price = "PTT Kargo : Ücretsiz Kargo";
                    }
                    arrayListComparison.add(new HowMuchAndWhere(howMuch, (String) siteLink.get(i), arrayListTotalPrices.get(i), cargo_price, total_price, isfree, true, true, false, null, payAtTheDoorCashACC, payAtTheDoorCardACC, "Kapıda ödeme seçenekleri yalnızca İstanbul - Avrupa yakasında geçerli.", null));
                    break;
                }
                case "kitapaktif": {
                    boolean isfree = false;
                    String payAtTheDoorCashACC = null;
                    String payAtTheDoorCardACC = null;
                    if (price < 30) {
                        total_price = StringUtils.join("PTT Kargo : ",StringUtils.replace(formatter.format(price + 7.9), ".", ","),
                                "\nAras Kargo : ", StringUtils.replace(formatter.format(price + 9.9), ".", ","));
                        cargo_price = "PTT Kargo : 7,90 TL\nAras Kargo : 9,90 TL";
                    } else if (price < 100) {
                        payAtTheDoorCashACC = "Kapıda Nakit Ödeme\nPTT Kargo : +5,90 TL\nAras Kargo : +7,90 TL";
                        payAtTheDoorCardACC = "Kapıda Kredi Kartı İle Ödeme\nPTT Kargo : +5,90 TL\nAras Kargo : +7,90 TL";
                        total_price = StringUtils.join("PTT Kargo : ",StringUtils.replace(formatter.format(price + 7.9), ".", ","),
                                "\nAras Kargo : ", StringUtils.replace(formatter.format(price + 9.9), ".", ","));
                        cargo_price = "PTT Kargo : 7,90 TL\nAras Kargo : 9,90 TL";
                    } else {
                        payAtTheDoorCashACC = "Kapıda Nakit Ödeme\nPTT Kargo : +5,90 TL\nAras Kargo : +7,90 TL";
                        payAtTheDoorCardACC = "Kapıda Kredi Kartı İle Ödeme\nPTT Kargo : +5,90 TL\nAras Kargo : +7,90 TL";
                        isfree = true;
                        total_price = StringUtils.join("PTT Kargo || Aras Kargo: ",StringUtils.replace(formatter.format(price), ".", ","));
                        cargo_price = "PTT Kargo || Aras Kargo : Ücretsiz Kargo";
                    }
                    arrayListComparison.add(new HowMuchAndWhere(howMuch, (String) siteLink.get(i), arrayListTotalPrices.get(i), cargo_price, total_price, isfree, true, true, false, null, payAtTheDoorCashACC, payAtTheDoorCardACC, "150 TL'LİK ALIŞVERİŞE NUMBERONE ENGLİSH 20 SAAT ONLİNE İNGİLİZCE EĞİTİMİ ÜRÜNÜ BEDAVA.\nSEPETİNİZE PUAN YAYINLARI KİTABI EKLEYİN KARGO ÜCRETİ ÖDEMEYİN", null));
                    break;
                }
                case "KİTAPAMBARI": {
                    boolean isfree = false;
                    if (price < 40) {
                        total_price = StringUtils.replace(formatter.format(price + 7.46), ".", ",");
                        cargo_price = "Aras Kargo : 7,46 TL";
                    } else if (price < 90) {
                        total_price = StringUtils.replace(formatter.format(price + 8.57), ".", ",");
                        cargo_price = "Aras Kargo : 8,57 TL";
                    } else if (price < 120) {
                        total_price = StringUtils.replace(formatter.format(price + 9.6), ".", ",");
                        cargo_price = "Aras Kargo : 9,60 TL";
                    } else {
                        isfree = true;
                        total_price = StringUtils.replace(formatter.format(price), ".", ",");
                        cargo_price = "Aras Kargo : Ücretsiz Kargo";
                    }
                    arrayListComparison.add(new HowMuchAndWhere(howMuch, (String) siteLink.get(i), arrayListTotalPrices.get(i), cargo_price, total_price, isfree, false, true, false, null, null, null, null, null));
                    break;
                }
                case "kitapbudur": {
                    total_price = StringUtils.replace(formatter.format(price * 0.9), ".", ",");
                    cargo_price = "Sürat Kargo || Aras Kargo :  Ücretsiz Kargo";
                    arrayListComparison.add(new HowMuchAndWhere(howMuch, (String) siteLink.get(i), arrayListTotalPrices.get(i), cargo_price, total_price, true, true, false, false, null, "Kapıda Nakit Ödeme: + 7 TL", null,
                            "#EVDEKALTÜRKİYE kampanyası kapsamında tüm siparişlerde ücretsiz kargo ve sepette %10 ek indirim!", null));
                    break;
                }
                case "kitapburada": {
                    boolean isfree = false;
                    if (price < 99) {
                        total_price = StringUtils.join("PTT Kargo || Aras Kargo || Yurtiçi Kargo : ",StringUtils.replace(formatter.format(price + 9.9), ".", ","),
                                "\nKitapburada İkitelli veya Topkapı Ofisinden : ", StringUtils.replace(formatter.format(price), ".", ","));
                        cargo_price = "PTT Kargo || Aras Kargo || Yurtiçi Kargo : 9,90 TL";
                    } else {
                        isfree = true;
                        total_price = StringUtils.join("PTT Kargo || Aras Kargo || Yurtiçi Kargo : ",StringUtils.replace(formatter.format(price), ".", ","),
                                "\nKitapburada İkitelli veya Topkapı Ofisinden : ", StringUtils.replace(formatter.format(price), ".", ","));
                        cargo_price = "PTT Kargo || Aras Kargo || Yurtiçi Kargo : Ücretsiz Kargo";
                    }
                    arrayListComparison.add(new HowMuchAndWhere(howMuch, (String) siteLink.get(i), arrayListTotalPrices.get(i), cargo_price, total_price, isfree, true, true, false, "IYZICO", "Kapıda Nakit Ödeme (Yurtiçi Kargo) : %5,90 TL", null, null, null));
                    break;
                }
                case "kitapesatis": {
                    boolean isfree = false;
                    String payAtTheDoorCashACC = null;
                    String payAtTheDoorCardACC = null;
                    if (price < 30) {
                        total_price = StringUtils.join("Aras Kargo || Yurtiçi Kargo : ",StringUtils.replace(formatter.format(price + 8), ".", ","));
                        cargo_price = "Aras Kargo || Yurtiçi Kargo : 8 TL";
                    } else if (price < 75) {
                        payAtTheDoorCashACC = "Kapıda Nakit Ödeme\nAras Kargo || Yurtiçi Kargo : +7 TL";
                        payAtTheDoorCardACC = "Kapıda Kredi Kartı İle Ödeme\nAras Kargo || Yurtiçi Kargo : +7 TL";
                        total_price = StringUtils.join("Aras Kargo || Yurtiçi Kargo : ",StringUtils.replace(formatter.format(price + 8), ".", ","));
                        cargo_price = "Aras Kargo || Yurtiçi Kargo : 8 TL";
                    } else {
                        payAtTheDoorCashACC = "Kapıda Nakit Ödeme\nPTT Kargo : +5,90 TL\nAras Kargo : +7,90 TL";
                        payAtTheDoorCardACC = "Kapıda Kredi Kartı İle Ödeme\nPTT Kargo : +5,90 TL\nAras Kargo : +7,90 TL";
                        isfree = true;
                        total_price = StringUtils.join("Aras Kargo || Yurtiçi Kargo : ",StringUtils.replace(formatter.format(price), ".", ","));
                        cargo_price = "Aras Kargo || Yurtiçi Kargo : Ücretsiz Kargo";
                    }
                    arrayListComparison.add(new HowMuchAndWhere(howMuch, (String) siteLink.get(i), arrayListTotalPrices.get(i), cargo_price, total_price, isfree, true, true, false, null, payAtTheDoorCashACC, payAtTheDoorCardACC, "Kapıda nakit ve kredi kartı ile ödeme, 30 TL ve üzeri alışverişlerde geçerli.", null));
                    break;
                }
                case "kitap sihirbazı": {
                    boolean isfree = false;
                    String payAtTheDoorCashACC = "Kapıda Nakit Ödeme : +5,90 TL";
                    if (price < 15) {
                        payAtTheDoorCashACC = null;
                        total_price = StringUtils.join(StringUtils.replace(formatter.format(price + 9), ".", ","),
                                "\nHavale ile Ödeme : ", StringUtils.replace(formatter.format(price * 0.97 + 9.0), ".", ","),
                                "\nİzmit Mağazasından Teslim : ",StringUtils.replace(formatter.format(price), ".", ","));
                        cargo_price = "MNG Kargo : 9,00 TL";
                    } else if (price < 80) {
                        total_price = StringUtils.join(StringUtils.replace(formatter.format(price + 9), ".", ","),
                                "\nHavale ile Ödeme : ", StringUtils.replace(formatter.format(price * 0.97 + 9.0), ".", ","),
                                "\nİzmit Mağazasından Teslim : ",StringUtils.replace(formatter.format(price), ".", ","));
                        cargo_price = "MNG Kargo : 9,00 TL";
                    } else {
                        isfree = true;
                        total_price = StringUtils.join(StringUtils.replace(formatter.format(price), ".", ","),
                                "\nHavale ile Ödeme : ", StringUtils.replace(formatter.format(price * 0.97), ".", ","),
                                "\nİzmit Mağazasından Teslim : ",StringUtils.replace(formatter.format(price), ".", ","));
                        cargo_price = "MNG Kargo : Ücretsiz Kargo";
                    }
                    arrayListComparison.add(new HowMuchAndWhere(howMuch, (String) siteLink.get(i), arrayListTotalPrices.get(i), cargo_price, total_price, isfree, true, true, false, null, payAtTheDoorCashACC, "Kapıda Kredi Kartı İle Ödeme : +5,90 TL",
                            "Havale %3 indirimli.\nİzmit mağazasından (Körfez Mah. Şehit Rafet Karacan Bulvarı Eren Apt. A 4 Blok No:1) teslimat mevcut. \nKapıda nakit ödeme 15 TL ve üzeri alışverişlerde geçerli.", null));
                    break;
                }
                case "Kitapyurdu": {
                    boolean isfree = false;
                    if (price < 50) {
                        total_price = StringUtils.join(
                                "(PTT Kargo) ", StringUtils.replace(formatter.format(price + 9.25), ".", ","),
                                "\n(MNG Kargo) ", StringUtils.replace(formatter.format(price + 11.31), ".", ","),
                                "\n(Aras Kargo) ", StringUtils.replace(formatter.format(price + 11.75), ".", ","),
                                "\n(UPS Kargo) ", StringUtils.replace(formatter.format(price + 15.11), ".", ","));
                        cargo_price = "PTT Kargo : 9,25 TL\nMNG Kargo : 11,31 TL\nAras  Kargo : 11,75 TL\nUPS Kargo : 15,11 TL";
                    } else if (price < 100) {
                        total_price = StringUtils.join(
                                "(PTT Kargo) ", StringUtils.replace(formatter.format(price + 5), ".", ","),
                                "\n(MNG Kargo) ", StringUtils.replace(formatter.format(price + 7.06), ".", ","),
                                "\n(Aras Kargo) ", StringUtils.replace(formatter.format(price + 8.81), ".", ","),
                                "\n(UPS Kargo) ", StringUtils.replace(formatter.format(price + 12.82), ".", ","));
                        cargo_price = "PTT Kargo : 5 TL\nMNG Kargo : 7,06 TL\nAras  Kargo : 8,81 TL\nUPS Kargo : 12,82 TL";
                    } else {
                        isfree = true;
                        total_price = StringUtils.join(
                                "(PTT Kargo) ", StringUtils.replace(formatter.format(price), ".", ","),
                                "\n(MNG Kargo) ", StringUtils.replace(formatter.format(price + 2.06), ".", ","),
                                "\n(Aras Kargo) ", StringUtils.replace(formatter.format(price + 4.91), ".", ","),
                                "\n(UPS Kargo) ", StringUtils.replace(formatter.format(price + 7.82), ".", ","));
                        cargo_price = "PTT Kargo : Ücretsiz Kargo\nMNG Kargo : 2,06 TL\nAras  Kargo : 4,91 TL\nUPS Kargo : 7,82 TL";
                    }
                    arrayListComparison.add(new HowMuchAndWhere(howMuch, (String) siteLink.get(i), arrayListTotalPrices.get(i), cargo_price, total_price, isfree, true, true, false, null, "Kapıda Nakit Ödeme (MNG Kargo) : +7,08 TL", null, "Ankara, Antalya, İstanbul, İzmir, Sakarya ve Samsun'daki teslim noktaları mevcut. Detaylı bilgi için siteye gidiniz.", null));
                    break;
                }
                case "kitapzen": {
                    boolean isfree = false;
                    if (price < 95) {
                        cargo_price = "Aras Kargo : 5,50 TL";
                        total_price = StringUtils.join(StringUtils.replace(formatter.format(price + 5.5), ".", ","),
                                "\nHavale ile Ödeme : ", StringUtils.replace(formatter.format(price * 0.99 + 5.5 ), ".", ","));
                    } else {
                        isfree = true;
                        cargo_price = "Aras Kargo : Ücretsiz Kargo";
                        total_price = StringUtils.join(StringUtils.replace(formatter.format(price), ".", ","),
                                "\nHavale ile Ödeme : ", StringUtils.replace(formatter.format(price * 0.99), ".", ","));
                    }
                    arrayListComparison.add(new HowMuchAndWhere(howMuch, (String) siteLink.get(i), arrayListTotalPrices.get(i), cargo_price, total_price, isfree, true, true, false, null, "Kapıda Nakit Ödeme : +8 TL", null, "Havale %1 indirimli.", null));
                    break;
                }
                case "KPSSstore": {
                    boolean isfree = false;
                    String payAtTheDoorCashACC = "Kapıda Nakit Ödeme : +7 TL";
                    String payAtTheDoorCardACC = "Kapıda Kredi Kartı İle Ödeme : +7 TL";
                    if (price < 30) {
                        payAtTheDoorCashACC = null;
                        payAtTheDoorCardACC = null;
                        total_price = StringUtils.replace(formatter.format(price + 8), ".", ",");
                        cargo_price = "Aras Kargo || Yurtiçi Kargo: 8,00 TL";
                    } else if (price < 75) {
                        total_price = StringUtils.replace(formatter.format(price + 8), ".", ",");
                        cargo_price = "Aras Kargo || Yurtiçi Kargo: 8,00 TL";
                    } else {
                        isfree = true;
                        total_price = StringUtils.replace(formatter.format(price), ".", ",");
                        cargo_price = "Aras Kargo || Yurtiçi Kargo: Ücretsiz Kargo";
                    }
                    arrayListComparison.add(new HowMuchAndWhere(howMuch, (String) siteLink.get(i), arrayListTotalPrices.get(i), cargo_price, total_price, isfree, true, true, false, null, payAtTheDoorCashACC, payAtTheDoorCardACC, null, null));
                    break;
                }
                case "legalkitabevi": {
                    boolean isfree = false;
                    if (price < 150) {
                        cargo_price = "Aras Kargo : 9,50 TL";
                        total_price = StringUtils.join(StringUtils.replace(formatter.format(price + 9.5), ".", ","),
                                "\nHavale ile Ödeme : ", StringUtils.replace(formatter.format(price * 0.95 + 9.5 ), ".", ","));
                    } else {
                        isfree = true;
                        cargo_price = "Aras Kargo : Ücretsiz Kargo";
                        total_price = StringUtils.join(StringUtils.replace(formatter.format(price), ".", ","),
                                "\nHavale ile Ödeme : ", StringUtils.replace(formatter.format(price * 0.95), ".", ","));
                    }
                    arrayListComparison.add(new HowMuchAndWhere(howMuch, (String) siteLink.get(i), arrayListTotalPrices.get(i), cargo_price, total_price, isfree, true, true, false, null, null, null, "Havale %5 indirimli.", null));
                    break;
                }
                case "LİMON kitabevi": {
                    boolean isfree = false;
                    if (price < 99) {
                        cargo_price = "MNG Kargo : 8,99 TL\nAras Kargo : 10,99 TL";
                        total_price = StringUtils.join("MNG Kargo : ",StringUtils.replace(formatter.format(price + 8.99), ".", ","),
                                "\nAras Kargo : ", StringUtils.replace(formatter.format(price + 10.99), ".", ","),
                                "\nHavale ile Ödeme (MNG Kargo): ", StringUtils.replace(formatter.format(price * 0.98 + 8.99), ".", ","),
                                "\nHavale ile Ödeme (Aras Kargo): ", StringUtils.replace(formatter.format(price * 0.98 + 10.99), ".", ","));
                    } else {
                        isfree = true;
                        cargo_price = "MNG Kargo || Aras Kargo : Ücretsiz Kargo";
                        total_price = StringUtils.join("MNG Kargo || Aras Kargo : ",StringUtils.replace(formatter.format(price), ".", ","),
                                "\nHavale ile Ödeme (MNG Kargo || Aras Kargo): ", StringUtils.replace(formatter.format(price * 0.98), ".", ","));
                    }
                    arrayListComparison.add(new HowMuchAndWhere(howMuch, (String) siteLink.get(i), arrayListTotalPrices.get(i), cargo_price, total_price, isfree, true, true, false, null, "Kapıda Nakit Ödeme :\nMNG Kargo : +7,99 TL\nAras Kargo : +8,99 TL", "Kapıda Kredi Kartı İle Ödeme :\nMNG Kargo : +7,99 TL\nAras Kargo : +8,99 TL", "Havale %2 indirimli.", null));
                    break;
                }
                case "maksimumkitap": {
                    boolean isfree = false;
                    if (price < 95) {
                        total_price = StringUtils.join("Sürat Kargo : ", StringUtils.replace(formatter.format(price + 9.90), ".", ","),
                                "\nPTT Kargo || Aras Kargo : ",StringUtils.replace(formatter.format(price + 14.90), ".", ","),
                                "\nYurtiçi Kargo : ",StringUtils.replace(formatter.format(price + 19.90), ".", ","));
                        cargo_price = "Sürat Kargo : 9,90 TL\nPTT Kargo || Aras Kargo : 14,90 TL\nYurtiçi Kargo : 19,90 TL";
                    } else if (price < 500) {
                        isfree = true;
                        total_price = StringUtils.join("Sürat Kargo : ", StringUtils.replace(formatter.format(price), ".", ","),
                                "\nPTT Kargo || Aras Kargo : ",StringUtils.replace(formatter.format(price + 14.90), ".", ","),
                                "\nYurtiçi Kargo : ",StringUtils.replace(formatter.format(price + 19.90), ".", ","));
                        cargo_price = "Sürat Kargo : Ücretsiz Kargo\nPTT Kargo || Aras Kargo : 14,90 TL\nYurtiçi Kargo : 19,90 TL";
                    } else if (price < 750) {
                        isfree = true;
                        total_price = StringUtils.join("Sürat Kargo || PTT Kargo : ", StringUtils.replace(formatter.format(price), ".", ","),
                                "\nAras Kargo : ",StringUtils.replace(formatter.format(price + 14.90), ".", ","),
                                "\nYurtiçi Kargo : ",StringUtils.replace(formatter.format(price + 19.90), ".", ","));
                        cargo_price = "Sürat Kargo || PTT Kargo : Ücretsiz Kargo\nAras Kargo : 14,90 TL\nYurtiçi Kargo : 19,90 TL";
                    } else {
                        isfree = true;
                        total_price = StringUtils.join("Sürat Kargo || PTT Kargo || Aras Kargo || Yurtiçi Kargo : ", StringUtils.replace(formatter.format(price), ".", ","));
                        cargo_price = "Sürat Kargo || PTT Kargo || Aras Kargo || Yurtiçi Kargo : Ücretsiz Kargo";
                    }
                    arrayListComparison.add(new HowMuchAndWhere(howMuch, (String) siteLink.get(i), arrayListTotalPrices.get(i), cargo_price, total_price, isfree, true, true, false, null, "Kapıda Nakit Ödeme\nSürat Kargo || PTT Kargo : +8,50 TL\nAras Kargo : +10 TL\nYurtiçi Kargo : +15 TL", null, null, null));
                    break;
                }
                case "pelikan kitabevi": {
                    boolean isfree = false;
                    if (price < 100) {
                        total_price = StringUtils.join("Yurtiçi Kargo : ",StringUtils.replace(formatter.format(price + 3.99), ".", ","),
                                "\nAras Kargo : ", StringUtils.replace(formatter.format(price + 7.5), ".", ","),
                                "\nHavale ile Ödeme (Yurtiçi Kargo) : ", StringUtils.replace(formatter.format(price * 0.98 + 3.99), ".", ","),
                                "\nHavale ile Ödeme (Aras Kargo) : ", StringUtils.replace(formatter.format(price * 0.98 + 7.5), ".", ","));
                        cargo_price = "Yurtiçi Kargo : 3,99 TL\nAras Kargo : 7,50 TL";
                    } else {
                        isfree = true;
                        total_price = StringUtils.join("Yurtiçi Kargo || Aras Kargo : ",StringUtils.replace(formatter.format(price), ".", ","),
                                "\nHavale ile Ödeme (Yurtiçi Kargo || Aras Kargo) : ", StringUtils.replace(formatter.format(price * 0.98), ".", ","));
                        cargo_price = "Yurtiçi Kargo || Aras Kargo : Ücretsiz Kargo";
                    }
                    arrayListComparison.add(new HowMuchAndWhere(howMuch, (String) siteLink.get(i), arrayListTotalPrices.get(i), cargo_price, total_price, isfree, true, true, false, null, null, null, "Havale / EFT ile verilen siparişler %2 indirimli.", null));
                    break;
                }
                case "ravzakitap": {
                    boolean isfree = false;
                    if (price < 60) {
                        total_price = StringUtils.join("MNG Kargo : ",StringUtils.replace(formatter.format(price + 8.45), ".", ","),
                                "\nPTT Kargo : ", StringUtils.replace(formatter.format(price + 9), ".", ","));
                        cargo_price = "MNG Kargo : 8,45 TL\nPTT Kargo : 9 TL";
                    } else {
                        isfree = true;
                        total_price = StringUtils.join("MNG Kargo : ",StringUtils.replace(formatter.format(price), ".", ","),
                                "\nPTT Kargo : ", StringUtils.replace(formatter.format(price + 5), ".", ","));
                        cargo_price = "MNG Kargo : Ücretsiz Kargo\nPTT Kargo : 5 TL";
                    }
                    arrayListComparison.add(new HowMuchAndWhere(howMuch, (String) siteLink.get(i), arrayListTotalPrices.get(i), cargo_price, total_price, isfree, true, true, false, null, "Kapıda Nakit Ödeme (MNG Kargo) : +5 TL", null, null, null));
                    break;
                }
                case "TERCİH KİTABEVİ": {
                    boolean isfree = false;
                    if (price < 75) {
                        total_price = StringUtils.join("MNG Kargo : ",StringUtils.replace(formatter.format(price + 7), ".", ","),
                                "\nAras Kargo : ", StringUtils.replace(formatter.format(price + 7.5), ".", ","),
                                "\nHavale ile Ödeme (MNG Kargo) : ", StringUtils.replace(formatter.format(price * 0.985 + 7), ".", ","),
                                "\nHavale ile Ödeme (Aras Kargo) : ", StringUtils.replace(formatter.format(price * 0.985 + 7.5), ".", ","));
                        cargo_price = "MNG Kargo : 7 TL\nAras Kargo : 7,50 TL";
                    } else {
                        isfree = true;
                        total_price = StringUtils.join("MNG Kargo || Aras Kargo : ",StringUtils.replace(formatter.format(price), ".", ","),
                                "\nHavale ile Ödeme (MNG Kargo || Aras Kargo) : ", StringUtils.replace(formatter.format(price * 0.985), ".", ","));
                        cargo_price = "MNG Kargo || Aras Kargo : Ücretsiz Kargo";
                    }
                    arrayListComparison.add(new HowMuchAndWhere(howMuch, (String) siteLink.get(i), arrayListTotalPrices.get(i), cargo_price, total_price, isfree, true, true, false, null, null, null, "Havale / EFT ile verilen siparişler %1,5 indirimli.", null));
                    break;
                }
                case "toptanasya": break;
                case "simurg": {
                    boolean isfree = false;
                    if (price < 150) {
                        int size = favList.size();
                        total_price = StringUtils.replace(formatter.format(price + 10 + (size -1) * 1.5), ".", ",");
                        cargo_price = StringUtils.replace(formatter.format(10 + (size -1) * 1.5), ".", ",");
                    } else {
                        isfree = true;
                        total_price = StringUtils.replace(formatter.format(price), ".", ",");
                        cargo_price = "Ücretsiz Kargo";
                    }
                    arrayListComparison.add(new HowMuchAndWhere(howMuch, (String) siteLink.get(i), arrayListTotalPrices.get(i), cargo_price, total_price, isfree, false, true, false, null, null, null, null, null));
                    break;
                }
                case "TDK Bilim": {
                    arrayListComparison.add(new HowMuchAndWhere(howMuch, (String) siteLink.get(i), arrayListTotalPrices.get(i), "Aras Kargo || MNG Kargo : 6,90 TL", StringUtils.join("Aras Kargo || MNG Kargo : ",StringUtils.replace(formatter.format(price * 6.99), ".", ",")), false, true, false, false, null, null, null, null, null));
                    break;
                }
                case "UCUZKİTAPAL": {
                    boolean isfree = false;
                    if (price < 100) {
                        total_price = StringUtils.join("MNG Kargo : ",StringUtils.replace(formatter.format(price + 8.90), ".", ","),
                                "\nAras Kargo : ", StringUtils.replace(formatter.format(price + 9.25), ".", ","));
                        cargo_price = "MNG Kargo : 8,90 TL\nAras Kargo : 9,25 TL";
                    } else if (price < 150) {
                        isfree = true;
                        total_price = StringUtils.join("MNG Kargo : ",StringUtils.replace(formatter.format(price), ".", ","),
                                "\nAras Kargo : ", StringUtils.replace(formatter.format(price + 9.25), ".", ","));
                        cargo_price = "MNG Kargo : Ücretsiz Kargo\nAras Kargo : 9,25 TL";
                    } else {
                        isfree = true;
                        total_price = StringUtils.join("MNG Kargo || Aras Kargo : ",StringUtils.replace(formatter.format(price), ".", ","));
                        cargo_price = "MNG Kargo || Aras Kargo : Ücretsiz Kargo";
                    }
                    arrayListComparison.add(new HowMuchAndWhere(howMuch, (String) siteLink.get(i), arrayListTotalPrices.get(i), cargo_price, total_price, isfree, true, true, false, "Garanti Pay", "Kapıda Nakit Ödeme : +7 TL", null, null, null));
                    break;
                }
                /*case "uygunkitapal": {
                    arrayListComparison.add(new HowMuchAndWhere(howMuch, (String) siteLink.get(i), arrayListTotalPrices.get(i), "Aras Kargo || Sürat Kargo : Ücretsiz Kargo", StringUtils.join("Aras Kargo || Sürat Kargo : ",StringUtils.replace(formatter.format(price * 0.9), ".", ",")), true, true, false, false, null, "Kapıda Nakit Ödeme : +7 TL", null, "KIŞA VEDA EDİYORUZ kampanyası ile sepette %10 indirim.", null));
                    break;
                }*/
                /*case "yargı yayınevi": {
                    arrayListComparison.add(new HowMuchAndWhere(howMuch, (String) siteLink.get(i), arrayListTotalPrices.get(i), "Aras Kargo || Sürat Kargo : Ücretsiz Kargo", StringUtils.join("Aras Kargo || Sürat Kargo : ",StringUtils.replace(formatter.format(price * 0.9), ".", ",")), true, true, false, false, null, "Kapıda Nakit Ödeme : +7 TL", null, "KIŞA VEDA EDİYORUZ kampanyası ile sepette %10 indirim.", null));
                    break;
                }*/
                default: {
                    accTotal = new StringBuilder();
                    accTransfer = new StringBuilder();
                    accCargo = new StringBuilder();
                    accInfo = new StringBuilder();
                    accPayAtTheDoorCash = new StringBuilder();
                    accPayAtTheDoorCard = new StringBuilder();
                    accPaymentMethod = new StringBuilder();
                    double cargoPrice;
                    String threshold, keysString = null, keysStringCargo, keysStringCargoSecond, keysStringThreshold, keysStringThresholdSecond;
                    Iterator<String> keys, keysCargo;
                    JSONObject jsonObject = obj.getJSONObject(0);
                    try {
                        keys = jsonObject.getJSONObject("info").keys();
                        while (keys.hasNext()) {
                            keysString = keys.next();
                            accInfo.append("\n").append(jsonObject.getJSONObject("info").getString(keysString));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        accInfo.append(jsonObject.getString("info"));
                    }
                    info = StringUtils.replaceOnce(accInfo.toString(), "\n", "");
                    try {
                        int length = jsonObject.getJSONObject("threshold").length();
                        if (length == 1) {
                            //OKUYAN BOĞA, PANDORA, PELİKAN
                            if (price < Double.parseDouble(jsonObject.getJSONObject("threshold").getString(jsonObject.getJSONObject("threshold").keys().next()))) {
                                isFree = false;
                                try {
                                    keys = jsonObject.getJSONObject("cargo").keys();
                                    if (info.contains("Havale %"))
                                        accTransfer.append("\n").append("Havale ile Ödeme : ");
                                    while (keys.hasNext()) {
                                        keysString = keys.next();
                                        cargoPrice = Double.parseDouble(jsonObject.getJSONObject("cargo").getString(keysString));
                                        if (info.contains("Havale %"))
                                            accTransfer.append("\n").append("(").append(keysString).append(") ").append(StringUtils.replace(formatter.format((price + cargoPrice) * ((100 - Double.parseDouble(StringUtils.substring(info, StringUtils.indexOf(info, "%") + 1, StringUtils.indexOf(info, "%") + 2))) / 100)), ".", ","));
                                        accTotal.append("\n").append("(").append(keysString).append(") ").append(StringUtils.replace(formatter.format(price + cargoPrice), ".", ","));
                                        accCargo.append("\n").append(keysString).append(" : ").append(StringUtils.replace(formatter.format(cargoPrice), ".", ","));
                                    }
                                } catch (JSONException ex) {
                                    ex.printStackTrace();
                                }
                                if (StringUtils.containsAny(info, "Mağaza", "mağaza", "Şube", "şube", "teslim")) {
                                    if (siteList.get(i).equals("EVRENSEL KİTABEVİ"))
                                        accTotal.append("\n").append("Ankara Kızılay şubesinden teslimat : ").append(StringUtils.replace(formatter.format(price + 0.01), ".", ","));
                                    else
                                        accTotal.append("\n").append("Mağazadan/şubeden teslimat : ").append(StringUtils.replace(formatter.format(price), ".", ","));
                                }
                            } else {
                                isFree = true;
                                try {
                                    keys = jsonObject.getJSONObject("cargo").keys();
                                    if (info.contains("Havale %"))
                                        accTransfer.append("\n").append("Havale ile Ödeme : ");
                                    while (keys.hasNext()) {
                                        keysString = keys.next();
                                        cargoPrice = Double.parseDouble(jsonObject.getJSONObject("cargo").getString(keysString));
                                        if (keysString.equals(jsonObject.getJSONObject("threshold").keys().next())) {
                                            accTotal.append("\n").append("(").append(keysString).append(") ").append(StringUtils.replace(formatter.format(price), ".", ","));
                                            accCargo.append("\n").append(StringUtils.join(keysString, " : Ücretsiz Kargo"));
                                            if (info.contains("Havale %"))
                                                accTransfer.append("(").append(keysString).append(") ").append(StringUtils.replace(formatter.format((price) * ((100 - Double.parseDouble(StringUtils.substring(info, StringUtils.indexOf(info, "%") + 1, StringUtils.indexOf(info, "%") + 2))) / 100)), ".", ","));
                                        } else {
                                            accTotal.append("\n").append("(").append(keysString).append(") ").append(StringUtils.replace(formatter.format(price + cargoPrice), ".", ","));
                                            accCargo.append("\n").append(keysString).append(" : ").append(StringUtils.replace(formatter.format(cargoPrice), ".", ","));
                                            if (info.contains("Havale %"))
                                                accTransfer.append("\n").append("(").append(keysString).append(") ").append(StringUtils.replace(formatter.format((price + cargoPrice) * ((100 - Double.parseDouble(StringUtils.substring(info, StringUtils.indexOf(info, "%") + 1, StringUtils.indexOf(info, "%") + 2))) / 100)), ".", ","));
                                        }
                                    }
                                } catch (JSONException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        } else if (length == 2) {
                            ArrayList<Double> arrayList = new ArrayList<>();
                            //ALO, ATLAS, NOBEL, PERPA
                            try {
                                keys = jsonObject.getJSONObject("threshold").keys();
                                String firstThresholdString = jsonObject.getJSONObject("threshold").keys().next();
                                String firstCargoString = jsonObject.getJSONObject("cargo").keys().next();
                                while (keys.hasNext()) {
                                    keysStringThreshold = keys.next();
                                    arrayList.add(Double.parseDouble(jsonObject.getJSONObject("threshold").getString(keysStringThreshold)));
                                }
                                if (price < arrayList.get(0)) {
                                    isFree = false;
                                    ArrayList<Double> arrayListInner = new ArrayList<>();
                                    try {
                                        keys = jsonObject.getJSONObject("cargo").keys();
                                        while (keys.hasNext()) {
                                            keysString = keys.next();
                                            arrayListInner.add(Double.parseDouble(jsonObject.getJSONObject("cargo").getString(keysString)));
                                        }
                                        boolean isEqual = true;
                                        double first = arrayListInner.get(0);
                                        for (Double d : arrayListInner) {
                                            if (d != first) {
                                                isEqual = false;
                                                break;
                                            }
                                        }
                                        if (isEqual) {
                                            keys = jsonObject.getJSONObject("cargo").keys();
                                            if (info.contains("Havale %"))
                                                accTransfer.append("\n").append("Havale ile Ödeme : ");
                                            while (keys.hasNext()) {
                                                keysString = keys.next();
                                                accCargo.append(" || ").append(keysString);
                                            }
                                            accCargo.append("\n").append(" : ").append(StringUtils.replace(formatter.format(first), ".", ","));
                                            accTotal.append("\n").append(StringUtils.replace(formatter.format(price + first), ".", ","))/*.append(" - ")*/;
                                            if (info.contains("Havale %"))
                                                accTransfer.append(StringUtils.replace(formatter.format((price + first) * ((100 - Double.parseDouble(StringUtils.substring(info, StringUtils.indexOf(info, "%") + 1, StringUtils.indexOf(info, "%") + 2))) / 100)), ".", ","))/*.append(" - ")*/;
                                        } else {
                                            keys = jsonObject.getJSONObject("cargo").keys();
                                            if (info.contains("Havale %"))
                                                accTransfer.append("\n").append("Havale ile Ödeme : ");
                                            while (keys.hasNext()) {
                                                keysString = keys.next();
                                                cargo_price = jsonObject.getJSONObject("cargo").getString(keysString);
                                                accCargo.append("\n").append(keysString).append(" : ").append(StringUtils.replace(formatter.format(Double.parseDouble(cargo_price)), ".", ","));
                                                accTotal.append("\n").append("(").append(keysString).append(") ").append(StringUtils.replace(formatter.format(price + Double.parseDouble(cargo_price)), ".", ","))/*.append(" - ").append(keysString)*/;
                                                if (info.contains("Havale %"))
                                                    accTransfer.append("\n").append(StringUtils.replace(formatter.format((price + Double.parseDouble(cargo_price)) * ((100 - Double.parseDouble(StringUtils.substring(info, StringUtils.indexOf(info, "%") + 1, StringUtils.indexOf(info, "%") + 2))) / 100)), ".", ","))/*.append(" (").append(keysString).append(")")*/;
                                            }
                                        }
                                    } catch (JSONException ex) {
                                        ex.printStackTrace();
                                    }
                                    if (StringUtils.containsAny(info, "Mağaza", "mağaza", "Şube", "şube", "teslim")) {
                                        if (siteList.get(i).equals("EVRENSEL KİTABEVİ"))
                                            accTotal.append("\n").append("Ankara Kızılay şubesinden teslimat : ").append(StringUtils.replace(formatter.format(price + 0.01), ".", ","));
                                        else
                                            accTotal.append("\n").append("Mağazadan/şubeden teslimat : ").append(StringUtils.replace(formatter.format(price), ".", ","));
                                    }
                                } else if (arrayList.get(0) <= price && price < arrayList.get(1)) {
                                    ArrayList<Double> arrayListInner = new ArrayList<>();
                                    isFree = true;
                                    try {
                                        keys = jsonObject.getJSONObject("cargo").keys();
                                        while (keys.hasNext()) {
                                            keysString = keys.next();
                                            arrayListInner.add(Double.parseDouble(jsonObject.getJSONObject("cargo").getString(keysString)));
                                        }
                                        boolean isEqual = true;
                                        double first = arrayListInner.get(0);
                                        for (Double d : arrayListInner) {
                                            if (d != first) {
                                                isEqual = false;
                                                break;
                                            }
                                        }
                                        if (isEqual) {
                                            keys = jsonObject.getJSONObject("threshold").keys();
                                            if (info.contains("Havale %"))
                                                accTransfer.append("\n").append("Havale ile Ödeme : ");
                                            while (keys.hasNext()) {
                                                keysStringThreshold = keys.next();
                                                if (keysStringThreshold.equals(firstThresholdString)) {
                                                    accTotal.append("\n").append(keysStringThreshold).append(" : ").append(StringUtils.replace(formatter.format(price), ".", ","));
                                                    accCargo.append("\n").append(keysStringThreshold).append(" : ").append("Ücretsiz Kargo").append(" - ");
                                                    keysCargo = jsonObject.getJSONObject("cargo").keys();
                                                    while (keysCargo.hasNext()) {
                                                        keysString = keysCargo.next();
                                                        accCargo.append(" || ").append(keysString);
                                                    }
                                                    keysStringThresholdSecond = keys.next();
                                                    accTotal.append("\n").append(keysStringThresholdSecond).append(" : ").append(StringUtils.replace(formatter.format(price + first), ".", ","));
                                                    accCargo.append("\n").append(keysStringThresholdSecond).append(" : ").append(StringUtils.replace(formatter.format(first), ".", ",")).append(" - ");
                                                    keysCargo = jsonObject.getJSONObject("cargo").keys();
                                                    while (keysCargo.hasNext()) {
                                                        keysString = keysCargo.next();
                                                        accCargo.append(" || ").append(keysString);
                                                    }
                                                    if (info.contains("Havale %"))
                                                        accTransfer.append(StringUtils.replace(formatter.format((price) * ((100 - Double.parseDouble(StringUtils.substring(info, StringUtils.indexOf(info, "%") + 1, StringUtils.indexOf(info, "%") + 2))) / 100)), ".", ","))/*.append(" (").append(keysString).append(" || ").append(keysStringCargoSecond).append(")")*/;
                                                    //BURAYA ÇALIŞILMALI
                                                    break;
                                                }
                                            }
                                        } else {
                                            keys = jsonObject.getJSONObject("threshold").keys();
                                            if (info.contains("Havale %"))
                                                accTransfer.append("\n").append("Havale ile Ödeme : ");
                                            while (keys.hasNext()) {
                                                keysStringThreshold = keys.next();
                                                if (keysStringThreshold.equals(firstThresholdString)) {
                                                    accTotal.append("\n").append(keysStringThreshold).append(" : ").append(StringUtils.replace(formatter.format(price), ".", ","));
                                                    keys = jsonObject.getJSONObject("cargo").keys();
                                                    while (keys.hasNext()) {
                                                        keysStringCargo = keys.next();
                                                        if (keysStringCargo.equals(firstCargoString)) {
                                                            accCargo.append("\n").append(keysStringCargo).append(" : ").append("Ücretsiz Kargo");
                                                            keysStringCargoSecond = keys.next();
                                                            accCargo.append("\n").append(keysStringCargoSecond).append(" : ").append(StringUtils.replace(formatter.format(Double.parseDouble(jsonObject.getJSONObject("cargo").getString(keysStringCargoSecond))), ".", ","));
                                                            accTotal.append("\n").append(keysStringCargoSecond).append(" : ").append(StringUtils.replace(formatter.format(price + Double.parseDouble(jsonObject.getJSONObject("cargo").getString(keysStringCargoSecond))), ".", ","));
                                                            if (info.contains("Havale %"))
                                                                accTransfer.append("\n").append(StringUtils.replace(formatter.format((price) * ((100 - Double.parseDouble(StringUtils.substring(info, StringUtils.indexOf(info, "%") + 1, StringUtils.indexOf(info, "%") + 2))) / 100)), ".", ","))/*.append(" (").append(keysStringCargo).append(" || ").append(keysStringCargoSecond).append(")")*/;
                                                            break;
                                                        }
                                                    }
                                                    break;
                                                }
                                            }
                                        }
                                    } catch (JSONException ex) {
                                        ex.printStackTrace();
                                    }
                                } else {
                                    accTotal.append("\n").append(StringUtils.replace(formatter.format(price), ".", ","));
                                    isFree = true;
                                    keys = jsonObject.getJSONObject("threshold").keys();
                                    if (info.contains("Havale %"))
                                        accTransfer.append("\n").append("Havale ile Ödeme : ");
                                    while (keys.hasNext()) {
                                        keysStringThreshold = keys.next();
                                        accCargo.append(" || ").append(keysStringThreshold);
                                    }
                                    accCargo.append(" : ").append("Ücretsiz Kargo").append("\n");
                                    keys = jsonObject.getJSONObject("cargo").keys();
                                    while (keys.hasNext()) {
                                        keysString = keys.next();
                                        if (!accCargo.toString().contains(keysString) || !siteLink.get(i).equals("kitapseç"))
                                            accCargo.append(" || ").append(keysString);
                                    }
                                    if (info.contains("Havale %"))
                                        accTransfer.append(StringUtils.replace(formatter.format((price) * ((100 - Double.parseDouble(StringUtils.substring(info, StringUtils.indexOf(info, "%") + 1, StringUtils.indexOf(info, "%") + 2))) / 100)), ".", ","));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        //e.printStackTrace();
                        threshold = jsonObject.getString("threshold");
                        if (threshold.equals("null") || price < Double.parseDouble(threshold)) {
                            isFree = false;
                            ArrayList<Double> arrayListInner = new ArrayList<>();
                            try {
                                keys = jsonObject.getJSONObject("cargo").keys();
                                while (keys.hasNext()) {
                                    keysString = keys.next();
                                    arrayListInner.add(Double.parseDouble(jsonObject.getJSONObject("cargo").getString(keysString)));
                                }
                                if (arrayListInner.size() == 1) {
                                    if (keysString.equals("unspecified")) {
                                        accTotal.append(StringUtils.replace(formatter.format(price + arrayListInner.get(0)), ".", ","));
                                        accCargo.append(StringUtils.replace(formatter.format(arrayListInner.get(0)), ".", ","));
                                    } else {
                                        accTotal.append(StringUtils.replace(formatter.format(price + arrayListInner.get(0)), ".", ","));
                                        accCargo.append(keysString).append(" : ").append(StringUtils.replace(formatter.format(arrayListInner.get(0)), ".", ","));
                                    }
                                    if (info.contains("Havale %"))
                                        accTransfer.append("\n").append("\n").append("Havale ile Ödeme : ").append(StringUtils.replace(formatter.format((price + arrayListInner.get(0)) * ((100 - Double.parseDouble(StringUtils.substring(info, StringUtils.indexOf(info, "%") + 1, StringUtils.indexOf(info, "%") + 2))) / 100)), ".", ","));
                                } else {
                                    boolean isEqual = true;
                                    double first = arrayListInner.get(0);
                                    for (Double d : arrayListInner) {
                                        if (d != first) {
                                            isEqual = false;
                                            break;
                                        }
                                    }
                                    if (isEqual) {
                                        if (info.contains("Havale %"))
                                            accTransfer.append("\n").append("Havale ile Ödeme : ").append(StringUtils.replace(formatter.format((price + first) * ((100 - Double.parseDouble(StringUtils.substring(info, StringUtils.indexOf(info, "%") + 1, StringUtils.indexOf(info, "%") + 2))) / 100)), ".", ","));
                                        keys = jsonObject.getJSONObject("cargo").keys();
                                        while (keys.hasNext()) {
                                            keysString = keys.next();
                                            accCargo.append(" || ").append(keysString);
                                        }
                                        accCargo.append(" : ").append(StringUtils.replace(formatter.format(first), ".", ","));
                                        accTotal.append(StringUtils.replace(formatter.format(price + first), ".", ","));
                                    } else {
                                        if (info.contains("Havale %"))
                                            accTransfer.append("\n").append("Havale ile Ödeme : ");
                                        keys = jsonObject.getJSONObject("cargo").keys();
                                        while (keys.hasNext()) {
                                            keysString = keys.next();
                                            accCargo.append("\n").append(keysString).append(" : ").append(StringUtils.replace(formatter.format(Double.parseDouble(jsonObject.getJSONObject("cargo").getString(keysString))), ".", ","));
                                            accTotal.append("\n").append("(").append(keysString).append(") ").append(StringUtils.replace(formatter.format(price + Double.parseDouble(jsonObject.getJSONObject("cargo").getString(keysString))), ".", ","));
                                            if (info.contains("Havale %"))
                                                accTransfer.append("\n").append("(").append(keysString).append(") ").append(StringUtils.replace(formatter.format((price + Double.parseDouble(jsonObject.getJSONObject("cargo").getString(keysString))) * ((100 - Double.parseDouble(StringUtils.substring(info, StringUtils.indexOf(info, "%") + 1, StringUtils.indexOf(info, "%") + 2))) / 100)), ".", ","));
                                        }
                                    }
                                }
                            } catch (JSONException ex) {
                                ex.printStackTrace();
                            }
                            if (StringUtils.containsAny(info, "Mağaza", "mağaza", "Şube", "şube", "teslim")) {
                                if (siteList.get(i).equals("EVRENSEL KİTABEVİ"))
                                    accTotal.append("\n").append("Ankara Kızılay şubesinden teslimat : ").append(StringUtils.replace(formatter.format(price + 0.01), ".", ","));
                                else {
                                    if (!accTotal.toString().contains("\n"))
                                        accTotal.append("\n");
                                    accTotal.append("\n").append("Mağazadan/şubeden teslimat : ").append(StringUtils.replace(formatter.format(price), ".", ","));
                                }
                            }
                        } else {
                            isFree = true;
                            accTotal.append("\n").append(StringUtils.replace(formatter.format(price), ".", ","));
                            if (info.contains("Havale %"))
                                accTransfer.append("\n").append("Havale ile Ödeme : ").append(StringUtils.replace(formatter.format((price) * ((100 - Double.parseDouble(StringUtils.substring(info, StringUtils.indexOf(info, "%") + 1, StringUtils.indexOf(info, "%") + 2))) / 100)), ".", ","));
                            keys = jsonObject.getJSONObject("cargo").keys();
                            while (keys.hasNext()) {
                                keysString = keys.next();
                                accCargo.append(" || ").append(keysString);
                            }
                            accCargo.append(" : ").append("Ücretsiz Kargo");
                        }
                    }
                    try {
                        keys = jsonObject.getJSONObject("pay_at_the_door_cash").keys();
                        accPayAtTheDoorCash.append("Kapıda Nakit Ödeme : ");
                        while (keys.hasNext()) {
                            keysString = keys.next();
                            accPayAtTheDoorCash.append("\n").append("+").append(StringUtils.join(StringUtils.replace(formatter.format(Double.parseDouble(jsonObject.getJSONObject("pay_at_the_door_cash").getString(keysString))), ".", ","))).append(" - ").append(keysString);
                        }
                        payAtTheDoorCash = accPayAtTheDoorCash.toString()/*StringUtils.replaceOnce(accPayAtTheDoorCash.toString(), "\n", "")*/;
                    } catch (JSONException e) {
                        if (jsonObject.getString("pay_at_the_door_cash") != null && !jsonObject.getString("pay_at_the_door_cash").equals("null")) payAtTheDoorCash = StringUtils.join("Kapıda Nakit Ödeme : ", "+", StringUtils.replace(formatter.format(Double.parseDouble(jsonObject.getString("pay_at_the_door_cash"))), ".", ","));
                        e.printStackTrace();
                    }
                    try {
                        keys = jsonObject.getJSONObject("pay_at_the_door_card").keys();
                        accPayAtTheDoorCard.append("Kapıda Kredi Kartı ile Ödeme : ");
                        while (keys.hasNext()) {
                            keysString = keys.next();
                            accPayAtTheDoorCard.append("\n").append("+").append(StringUtils.join(StringUtils.replace(formatter.format(Double.parseDouble(jsonObject.getJSONObject("pay_at_the_door_card").getString(keysString))), ".", ","))).append(" - ").append(keysString);
                        }
                        payAtTheDoorCard = accPayAtTheDoorCard.toString()/*StringUtils.replaceOnce(accPayAtTheDoorCard.toString(), "\n", "")*/;
                    } catch (JSONException e) {
                        if (jsonObject.getString("pay_at_the_door_card") != null && !jsonObject.getString("pay_at_the_door_card").equals("null")) payAtTheDoorCard = StringUtils.join("Kapıda Kredi Kartı ile Ödeme : ", "+", StringUtils.replace(formatter.format(Double.parseDouble(jsonObject.getString("pay_at_the_door_card"))), ".", ","));
                        e.printStackTrace();
                    }
                    try {
                        keys = jsonObject.getJSONObject("payment_method").keys();
                        while (keys.hasNext()) {
                            keysString = keys.next();
                            if (StringUtils.containsIgnoreCase(jsonObject.getJSONObject("payment_method").getString(keysString), "Havale")) {
                                hasTransfer = true;
                                continue;
                            }
                            if (StringUtils.containsIgnoreCase(jsonObject.getJSONObject("payment_method").getString(keysString), "Kredi")) {
                                hasCard = true;
                                continue;
                            }
                            if (StringUtils.containsIgnoreCase(jsonObject.getJSONObject("payment_method").getString(keysString), "PayPal")) {
                                hasPayPal = true;
                                continue;
                            }
                            accPaymentMethod.append(", ").append(jsonObject.getJSONObject("payment_method").getString(keysString));
                        }
                        otherMethods = StringUtils.replaceOnce(accPaymentMethod.toString(), ", ", "");
                    } catch (JSONException e) {e.printStackTrace(); }
                    if (otherMethods == null || StringUtils.replace(otherMethods, " ", "").equals(""))
                        otherMethods = "";
                    arrayListComparison.add(new HowMuchAndWhere(howMuch, (String) siteLink.get(i), arrayListTotalPrices.get(i), StringUtils.replaceOnce(StringUtils.replaceOnce(StringUtils.replaceOnce(accCargo.toString(), "unspecified : ", ""), "\n", ""), " || ", ""), StringUtils.replaceOnce(StringUtils.replaceOnce(StringUtils.join(accTotal.toString(), accTransfer.toString()), "\n", ""), " || ", ""), isFree, hasCard, hasTransfer, hasPayPal, otherMethods/*StringUtils.replaceOnce(StringUtils.replaceOnce(otherMethods," ",""),",","")*/, payAtTheDoorCash, payAtTheDoorCard, info, null));
                    break;
                }
            }
        } catch (JSONException e) {
            //linear.setVisibility(View.GONE);
//            Toast.makeText(this, ":(", Toast.LENGTH_LONG).show();
//            Toast.makeText(this, "Lütfen favorileri güncelleyiniz.", Toast.LENGTH_LONG).show();
            Log.d("createBody","catch");
            e.printStackTrace();
        }
    }
    double extractDouble(String s) {
        String acc;
        try {
            acc = s.substring(0, s.indexOf("TL"));
        } catch (Exception e) {
            acc = s;
            e.printStackTrace();
        }
        acc = acc.replaceAll("\\D", "");
        return acc.isEmpty() ? 0 : Double.parseDouble(acc);
    }
    public class BasicTableFixHeaderAdapter extends TableFixHeaderAdapter<String, BasicCellViewGroup, String, BasicCellViewGroup, List<String>, BasicCellViewGroup, BasicCellViewGroup, BasicCellViewGroup> {
        private Context context;
        BasicTableFixHeaderAdapter(Context context) {
            super(context);
            this.context = context;
        }
        @Override
        protected BasicCellViewGroup inflateFirstHeader() {
            return new BasicCellViewGroup(context);
        }
        @Override
        protected BasicCellViewGroup inflateHeader() {
            return new BasicCellViewGroup(context);
        }
        @Override
        protected BasicCellViewGroup inflateFirstBody() {
            return new BasicCellViewGroup(context);
        }
        @Override
        protected BasicCellViewGroup inflateBody() {
            return new BasicCellViewGroup(context);
        }
        @Override
        protected BasicCellViewGroup inflateSection() {
            return new BasicCellViewGroup(context);
        }
        @Override
        protected List<Integer> getHeaderWidths() {
            List<Integer> headerWidths = new ArrayList<>();
            headerWidths.add((int) context.getResources().getDimension(R.dimen._115dp));
            for (int i = 0; i < y; i++)
                headerWidths.add((int) context.getResources().getDimension(R.dimen._115dp));
            return headerWidths;
        }
        @Override
        protected int getHeaderHeight() {
            return (int) context.getResources().getDimension(R.dimen._50dp);
        }
        @Override
        protected int getSectionHeight() {
            return (int) context.getResources().getDimension(R.dimen._55dp);
        }
        @Override
        protected int getBodyHeight() {
            return (int) context.getResources().getDimension(R.dimen._55dp);
        }
        @Override
        protected boolean isSection(List<List<String>> items, int row) {
            return false;
        }
    }
    private class BasicCellViewGroup extends FrameLayout implements TableFixHeaderAdapter.FirstHeaderBinder<String>, TableFixHeaderAdapter.HeaderBinder<String>, TableFixHeaderAdapter.FirstBodyBinder<List<String>>, TableFixHeaderAdapter.BodyBinder<List<String>>, TableFixHeaderAdapter.SectionBinder<List<String>> {
        private Context context;
        public TextView textView;
        public View vg_root;
        public BasicCellViewGroup(Context context) {
            super(context);
            this.context = context;
            try {
                init();
            } catch (Exception e) {
                catchAndSend("BasicCellViewGroup "+e.getLocalizedMessage());
            }
        }
        public BasicCellViewGroup(Context context, AttributeSet attrs) {
            super(context, attrs);
            this.context = context;
            try {
                init();
            } catch (Exception e) {
                catchAndSend("BasicCellViewGroup2 "+e.getLocalizedMessage());
            }
        }
        private void init() {
            LayoutInflater.from(context).inflate(R.layout.text_view_group, this, true);
            textView = findViewById(R.id.tv_text);
            vg_root = findViewById(R.id.vg_root);
        }
        @Override
        public void bindFirstHeader(String headerName) {
            try {
                textView.setText(headerName);
                textView.setTypeface(null, Typeface.BOLD);
                vg_root.setBackgroundResource(R.drawable.cell_lightgray_border_bottom_right_gray);
            } catch (Exception e) {
                catchAndSend("bindFirstHeader "+e.getLocalizedMessage());
            }
        }
        @Override
        public void bindHeader(String headerName, int column) {
            try {
                textView.setText(headerName);
                textView.setTypeface(null, Typeface.BOLD);
                vg_root.setBackgroundResource(R.drawable.cell_lightgray_border_bottom_right_gray);
                vg_root.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(StringUtils.join("https://", Arrays.asList(getResources().getStringArray(R.array.listValues)).get(column))))));
            } catch (Exception e) {
                catchAndSend("bindHeader "+e.getLocalizedMessage());
            }
        }
        @Override
        public void bindFirstBody(List<String> items, int row) {
            try {
                textView.setText(items.get(0));
                textView.setTypeface(null, Typeface.NORMAL);
                vg_root.setBackgroundResource(R.drawable.cell_lightgray_border_bottom_right_gray);
                vg_root.setOnClickListener(v -> Toast.makeText(context, favList.get(row), Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                catchAndSend("bindFirstBody "+e.getLocalizedMessage());
            }
        }
        @Override
        public void bindBody(List<String> items, int row, int column) {
            try {
                textView.setText(items.get(column + 1));
                textView.setTypeface(null, Typeface.BOLD_ITALIC);
                int size;
                if (from.equals("findOptimumForAll")) size = favList.size();
                else size = contextList.size();
                if (row < size) {
                    if (arrayListAll.get(row).get(column).getPrice().equals("□") || arrayListAll.get(row).get(column).getPrice().equals("ಠ_ಠ") || arrayListAll.get(row).get(column).getPrice().equals("¯\\_(ツ)_/¯"))
                        vg_root.setBackgroundResource(R.drawable.cell_lightgray_border_bottom_right_gray);
                    else {
                        double color, r, g, b;
                        double price = Double.parseDouble(StringUtils.replace(StringUtils.replace(StringUtils.replace(StringUtils.replace(StringUtils.replace(arrayListAll.get(row).get(column).getPrice(), "□", "0"), "ಠ_ಠ", "0"), "¯\\_(ツ)_/¯", "0"), ",", "."), " TL", ""));
                        double min = arrayListMins.get(row);
                        double max = arrayListMaxs.get(row);
                        if (min != max) color = 100 * (price - min) / (max - min);
                        else color = 0;
                        r = (255 * color) / 100;
                        g = (255 * (100 - color)) / 100;
                        b = 0;
                        vg_root.setBackgroundResource(R.drawable.cell_lightgray_border_bottom_right_gray);
                        vg_root.setBackgroundColor(Color.rgb((int) r, (int) g, (int) b));
                    }
                } else
                    vg_root.setBackgroundResource(R.drawable.cell_lightgray_border_bottom_right_gray);
                vg_root.setOnClickListener(v -> {
                    try {
                        if (arrayListAll.get(row).get(column).getURL().isEmpty())
                            Toast.makeText(context, "Seçili Olmayan Site", Toast.LENGTH_SHORT).show();
                        //else if (!receiver.isNetworkAvailable(context)) /*noInternet();*/ Utils.noInternet(TableAndBestPriceActivity.this);
                        else context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(arrayListAll.get(row).get(column).getURL())));
                    } catch (Exception e) {e.printStackTrace();
                    }
                });
                vg_root.setOnLongClickListener(v -> {
                    changeValue(row, column);
                    return true;
                });
            } catch (Exception e) {
                catchAndSend("bindBody "+e.getLocalizedMessage());
            }
        }

        @Override
        public void bindSection(List<String> item, int row, int column) {
            try {
                textView.setText(column == 0 ? "Section" : "");
                vg_root.setBackgroundResource(R.drawable.cell_lightgray_border_bottom_right_gray);
            } catch (Exception e) {
                catchAndSend("bindSection "+e.getLocalizedMessage());
            }
        }
    }
    private void changeValue(int row, int column) {
        View alertLayout = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_click4, null);
        final Button buttonAbsence = alertLayout.findViewById(R.id.buttonDelete);
        final Button buttonChange = alertLayout.findViewById(R.id.buttonChange);
        final EditText editText1 = alertLayout.findViewById(R.id.editText1);
        final EditText editText2 = alertLayout.findViewById(R.id.editText2);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true).setView(alertLayout);
        dialog = builder.create();
        dialog.show();
        editText1.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (!editText1.getText().toString().equals("")) editText2.requestFocus();
                return true;
            }
            return false;
        });
        editText2.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
                if (inputMethodManager != null)
                    inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0)
                            ;
                return true;
            }
            return false;
        });
        editText2.setOnFocusChangeListener((v, hasFocus) -> {
            editText2.post(() -> {
                InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                Objects.requireNonNull(inputMethodManager.showSoftInput(editText1, InputMethodManager.SHOW_IMPLICIT));
            });
        });
        editText1.setOnFocusChangeListener((v, hasFocus) -> editText1.post(() -> {
            InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            Objects.requireNonNull(inputMethodManager.showSoftInput(editText1, InputMethodManager.SHOW_IMPLICIT));
        }));
        editText1.requestFocus();
        buttonAbsence.setOnClickListener(v -> {
            String site = arrayListAll.get(row).get(column).getSite();
            if (dialog != null && dialog.isShowing()) dialog.dismiss();
            arrayListAll.get(row).get(column).setPrice("¯\\_(ツ)_/¯");
            updateDB(StringUtils.replace(favList.get(row), "/", ""), "¯\\_(ツ)_/¯", site);
        });
        buttonChange.setOnClickListener(v -> {
            if (editText1.getText().toString().equals(""))
                editText1.setError("Tamsayı değerini giriniz.");
            else if (editText1.getText().toString().contains(".") || editText1.getText().toString().startsWith("0"))
                editText1.setError("Değeri tamsayı olarak giriniz.");
            else {
                String site = arrayListAll.get(row).get(column).getSite(), price;
                if (dialog != null && dialog.isShowing()) dialog.dismiss();
                if (editText2.getText().toString().equals(""))
                    price = StringUtils.join(editText1.getText().toString(), ",", "00", " TL");
                else if (editText2.getText().length() == 1)
                    price = StringUtils.join(editText1.getText().toString(), ",", editText2.getText().toString(), "0", " TL");
                else
                    price = StringUtils.join(editText1.getText().toString(), ",", StringUtils.substring(editText2.getText().toString(), 0, 2), " TL");
                arrayListAll.get(row).get(column).setPrice(price);
                updateDB(StringUtils.replace(favList.get(row), "/", ""), price, site);
            }
        });
    }
    private void updateDB(String nameDB, String price, String site) {
        database = openOrCreateDatabase(nameDB, MODE_PRIVATE, null);
        SQLiteStatement statement;
        database.execSQL("CREATE TABLE IF NOT EXISTS data (site VARCHAR, price VARCHAR , url VARCHAR)");
        statement = database.compileStatement("UPDATE data SET price=(?) WHERE site=(?)");
        statement.bindString(1, price);
        statement.bindString(2, site);
        statement.execute();
        database.close();
        Intent intent = new Intent(context, TableAndBestPriceActivity.class);
        intent.putExtra("from", from);
        if (!from.equals("findOptimumForAll"))
            intent.putStringArrayListExtra("contextList", contextList);
        context.startActivity(intent);
        TableAndBestPriceActivity.this.finish();
    }
    private void catchAndSend(String e) {
        Intent intent = new Intent(TableAndBestPriceActivity.this, MainActivity.class);
        intent.putExtra("from", "tableError");
        intent.putExtra("info", e);
        Log.d("getLocalizedMessage",e);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        TableAndBestPriceActivity.this.finish();
    }
}