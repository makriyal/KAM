package com.muniryenigul.kam;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import androidx.annotation.NonNull;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.material.navigation.NavigationView;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.widget.TextView;
import android.widget.Toast;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import static com.muniryenigul.kam.activities.PriceActivity.database;
import static com.muniryenigul.kam.activities.PriceActivity.fav;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.muniryenigul.kam.activities.PriceActivity;
import com.muniryenigul.kam.activities.ScanActivity;
import com.muniryenigul.kam.activities.Settings2Activity;
import com.muniryenigul.kam.activities.ShowMoreActivity;
import com.muniryenigul.kam.activities.TableAndBestPriceActivity;
import com.muniryenigul.kam.ers.BookFinalAdapter;
import com.muniryenigul.kam.ers.FavouriteAdapter;
import com.muniryenigul.kam.ers.RecyclerTouchListener;
import com.muniryenigul.kam.ers.SuggestionAdapter;
import com.muniryenigul.kam.models.SingleItemModel;
import com.muniryenigul.kam.interfaces.ApiService;
import com.muniryenigul.kam.utils.LockableNestedScrollView;
import com.muniryenigul.kam.utils.Utils;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    //public static boolean isLoadMoreFinished = false;
    private boolean isKeyboardOpen = false;
    private ProgressBar progressBar, progressBar1;
    public CheckBox selectAll;
    private LockableNestedScrollView nestedScrollView;
    private HorizontalScrollView horizontalScrollView;
    private FavouriteAdapter searchedAdapter;
    private Button update, deleteAllFavs, findOptimumForAll;
    private TextView howManySelected;
    private GridLayoutManager glm;
    private BookFinalAdapter bookFinalAdapter, publisherFilterAdapter, authorFilterAdapter;
    private RecyclerView listView, searchedRecView, horRecView, publisherFilter, authorFilter;
    private SuggestionAdapter favAdapter;
    public static ArrayList<SingleItemModel> favSingleItem;
    private ImageView closeButton, shadowImage, shadowImageForContext, coverLoading;
    public static ArrayList<String> favList;
    private ArrayList<String> searchedList, arrayForSelectedSites, contextList;
    private final int REQ_CODE_SPEECH_INPUT = 200;
    private int count, all, pages, search = 1;
    static public int sugPosition, favPosition, howMany = 0;
    private ArrayList<HashMap<String, String>> arrayListSpare, arrayListInfoFinal;
    private BroadcastingInnerClass receiver;
    private String searchURL, lastSearchURL, /*searchURL_BKM,*/ mQuery;
    private SearchView searchView;
    boolean isSearchFocused = false, hasfocus = false;
    public static boolean isContext = false, dismiss = false, hasVoiceFocus = false;
    private SQLiteDatabase searched;
    private LinearLayout favLayout, searchedLayout, outer_searchLayout, upLayout;
    private RelativeLayout outer_contextLayout;
    public static SharedPreferences detect;
    private HashMap<String, String> mapInfoFinal = null;
    //private AdView mAdView;
    //private AdRequest adRequest;
    private ProgressDialog p;
    private Dialog dialog;
    private DrawerLayout drawer;
    private boolean showList = false;
    static public ArrayList<String> indexesForUpdate;
    private ArrayList<String> publisherFilterList, publisherIDsList, authorFilterList, authorIDsList;
    private View filterLine;
    public static ArrayList<String> filteredList;
    @Override
    protected void onPause() { super.onPause();
        hideKeyboard(searchView);
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        try {
            if (intent.getStringExtra("info") != null && intent.getStringExtra("info").equalsIgnoreCase("no found")) Toast.makeText(this, intent.getStringExtra("isbn") + " ISBN / GTIN için sonuç bulunamadı", Toast.LENGTH_LONG).show();
            else if (intent.getStringExtra("info") != null && (intent.getStringExtra("info").equalsIgnoreCase("scan error")
                    || intent.getStringExtra("info").equalsIgnoreCase("delay error")
                    || intent.getStringExtra("info").equalsIgnoreCase("sort error"))) Toast.makeText(this, "Bir hata meydana geldi. Lütfen tekrar deneyiniz.", Toast.LENGTH_SHORT).show();
            else if (intent.getStringExtra("info") != null && intent.getStringExtra("info").equalsIgnoreCase("deleteError")) {
                Toast.makeText(this, "Silme işleminde hata meydana geldi", Toast.LENGTH_SHORT).show();
                Toast.makeText(this, "Lütfen silme işlemini ana ekranda deneyiniz.", Toast.LENGTH_SHORT).show();
            } else if (intent.getStringExtra("info") != null && intent.getStringExtra("info").equalsIgnoreCase("databaseError")) {
                Toast.makeText(this, "Veritabanına kayıt ekleme işleminde hata meydana geldi", Toast.LENGTH_SHORT).show();
                Toast.makeText(this, "Lütfen tekrar deneyiniz.", Toast.LENGTH_SHORT).show();
            } else if (intent.getStringExtra("from") != null && !intent.getStringExtra("from").equals("drawer")) {
                switch (intent.getStringExtra("from")) {
                    case "ItemTouch":
                        if (favSingleItem != null && favList != null && favList.contains(arrayListInfoFinal.get(favPosition).get("name"))) {
                            progressBar.setVisibility(View.VISIBLE);
                            createIntent(favSingleItem, favPosition, "old");
                        } else {
                            progressBar.setVisibility(View.VISIBLE);
                            createIntentFinalDetail(arrayListInfoFinal, favPosition, "detail");
                        }
                        break;
                    case "Scan":
                        hasfocus = false;
                        startActivity(new Intent(MainActivity.this, ScanActivity.class));
                        break;
                    case "Voice": promptSpeechInput();break;
                    case "QueryTextFocusChange":
                        searchView.requestFocus();
                        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                        shadowImage.setVisibility(View.VISIBLE);
                        break;
                    case "saveError": Toast.makeText(this, "Veritabanına kayıt esnasında hata oluştu.", Toast.LENGTH_LONG).show();break;
                    case "tableError": Toast.makeText(this, /*intent.getStringExtra("info")*/"Tablo oluşturulamadı. Favorilerin güncellenmesi önerilir.", Toast.LENGTH_LONG).show();break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.d("onResume","onResume");
        isKeyboardOpen = true;
        if (!isContext) bringFavs();
        loadArrayList(arrayForSelectedSites, MainActivity.this);
        progressBar.setVisibility(View.GONE);
    }
    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(receiver);
        hideKeyboard(searchView);
    }
    @Override
    protected void onStart() {
        super.onStart();
        register(receiver);
    }
    private void register(BroadcastingInnerClass receiver) { registerReceiver(receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)); }
    @Override
    public void onBackPressed() {
        //shadow(searchView);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            Log.d("onBackPressed","isDrawerOpen");
            drawer.closeDrawer(GravityCompat.START);
        }
        else if (isContext) {
            Log.d("onBackPressed","isContext");
            cancelContext();
        }
        else if (listView.getVisibility() == View.VISIBLE) {
            Log.d("onBackPressed","listView");
            listView.setVisibility(View.GONE);
            upLayout.setVisibility(View.VISIBLE);
            filterLine.setVisibility(View.GONE);
            horizontalScrollView.setVisibility(View.GONE);
            closeButton.setVisibility(View.GONE);
            searchView.setQuery("", true);
            searchedLayout.setVisibility(View.GONE);
        }
        else {
            Log.d("onBackPressed","moveTaskToBack");
            this.moveTaskToBack(true);
        }
    }
//    public void bringValuesFromBKMFinal(String searchURL_BKM, /*boolean showProgressBar,*/ String from, RecyclerView.Adapter adapter, ArrayList<HashMap<String, String>> arrayList) {
//        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) showProgressBar(searchView, MainActivity.this);
//        arrayList.clear();
//        search = 1;
//        all = 0;
//        count = 0;
//        mapInfoFinal = null;
//        pages = 0;
//        Log.d("searchURL_BKM",searchURL_BKM);
//        try { (new Retrofit.Builder().addConverterFactory(ScalarsConverterFactory.create())
//                .baseUrl("https://www.bkmkitap.com/").client(new OkHttpClient().newBuilder().build()).build())
//                .create(ApiService.class).getPrices(searchURL_BKM).enqueue(new Callback<String>() {
//                @Override
//                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
//                    if (response.isSuccessful() && response.body() != null) {
//                        Document doc = Jsoup.parse(response.body());
//                        String s = doc.select("#pager-wrapper > div > div").text();
//                        int lastIndexOf = s.lastIndexOf(" ");
//                        if (!s.substring(lastIndexOf + 1).isEmpty()) pages = Integer.parseInt(s.substring(lastIndexOf + 1));
//                        if (pages == 0 && !doc.select("div.box.col-6.col-sm-12 > div > span").text().contains("bulunamadı")) {
//                            bookFinalAdapter.setMoreDataAvailable(false);
//                            Log.d("bringValuesFromBKMFinal","1");
//                            if (doc.select("div.box.col-6.col-sm-12 > div > span").text().contains("bulunamadı")) {
//                                Log.d("bringValuesFromBKMFinal","2");
//                                Toast.makeText(MainActivity.this, "Tüm sonuçlar yüklendi.", Toast.LENGTH_LONG).show();
//                            }
//                        } else bookFinalAdapter.setMoreDataAvailable(true);
//                        if (mQuery != null && mQuery.matches("\\d+(?:\\.\\d+)?")) {
//                            if (!doc.select("div.box.col-6.col-sm-12 > div > span").text().contains("bulunamadı")) {
//                                Log.d("bringValuesFromBKMFinal","3");
//                                mapInfoFinal = new HashMap<>();
//                                mapInfoFinal.put("name", doc.select("a.fl.col-12.text-description.detailLink").text());
//                                mapInfoFinal.put("author", doc.select("#productModelText").text());
//                                mapInfoFinal.put("publisher", doc.select("a.col.col-12.text-title.mt").text());
//                                mapInfoFinal.put("cover", doc.select("div:nth-child(1) > a > span > img").attr("src"));
//                                mapInfoFinal.put("individual", "https://www.bkmkitap.com" + doc.select("a.fl.col-12.text-description.detailLink").attr("href"));
//                                arrayList.add(mapInfoFinal);
//                                check("digit", adapter);
//                            } else {
//                                Log.d("bringValuesFromBKMFinal","4");
//                                Toast.makeText(MainActivity.this, " ¯\\_(ツ)_/¯ :  Hiçbir sonuç bulunamadı. ISBN ile aramayı deneyebilirsiniz.", Toast.LENGTH_LONG).show();
//                                hideProgressBar(searchView);
//                            }
//                        } else {Log.d("bringValuesFromBKMFinal","5");
//                            for (Element table : doc.select("div.fl.col-12.catalogWrapper")) {
//                                all = table.select("div.col.col-2.col-md-4.col-sm-6.col-xs-6.p-right.mb.productItem.zoom.ease").size();
//                                for (Element row : table.select("div.col.col-2.col-md-4.col-sm-6.col-xs-6.p-right.mb.productItem.zoom.ease")) {
//                                    mapInfoFinal = new HashMap<>();
//                                    if (!mapInfoFinal.containsValue(row.select("a.fl.col-12.text-description.detailLink").text()) && !mapInfoFinal.containsValue(row.select("#productModelText").text()) && !mapInfoFinal.containsValue(row.select("a.col.col-12.text-title.mt").text())) {
//                                        mapInfoFinal.put("name", row.select("a.fl.col-12.text-description.detailLink").text());
//                                        mapInfoFinal.put("author", row.select("#productModelText").text());
//                                        mapInfoFinal.put("publisher", row.select("a.col.col-12.text-title.mt").text());
//                                        mapInfoFinal.put("cover", row.select("div:nth-child(1) > a > span > img").attr("src"));
//                                        mapInfoFinal.put("individual", "https://www.bkmkitap.com" + row.select("a.fl.col-12.text-description.detailLink").attr("href"));
//                                        arrayList.add(mapInfoFinal);
//                                    }
//                                    check(from, adapter);
//                                }
//                            }
//                        }
//                        if (mapInfoFinal == null) {
//                            Log.d("bringValuesFromBKMFinal","6");
//                            Toast.makeText(MainActivity.this, "¯\\_(ツ)_/¯ :  Hiçbir sonuç bulunamadı. ISBN ile aramayı deneyebilirsiniz.", Toast.LENGTH_LONG).show();
//                            hideProgressBar(searchView);
//                        }
//                    } else {
//                        Log.d("bringValuesFromBKMFinal","7");
//                        Toast.makeText(MainActivity.this, "ಠ_ಠ : Lütfen internet bağlantınızı kontrol edin ve yeniden deneyin.", Toast.LENGTH_LONG).show();
//                        hideProgressBar(searchView);
//                    }
//                }
//                @Override
//                public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
//                    Log.d("bringValuesFromBKMFinal","8");
//                    Toast.makeText(MainActivity.this, "ಠ_ಠ : Lütfen internet bağlantınızı kontrol edin ve yeniden deneyin.", Toast.LENGTH_LONG).show();
//                    hideProgressBar(searchView);
//                }
//            });
//        } catch (Exception e) {
//            Log.d("bringValuesFromBKMFinal","9");
//            Toast.makeText(MainActivity.this, "ಠ_ಠ : Lütfen internet bağlantınızı kontrol edin ve yeniden deneyin.", Toast.LENGTH_LONG).show();
//            hideProgressBar(searchView);
//            e.printStackTrace();
//        }
//        if ((mQuery != null) && (searchedList != null) || hasVoiceFocus) {
//            if(!searchedList.contains(searchView.getQuery().toString())) {
//                try {
//                    searched = MainActivity.this.openOrCreateDatabase("Searched", MODE_PRIVATE, null);
//                    String execStringSearched = "CREATE TABLE IF NOT EXISTS srchd (search VARCHAR)";
//                    searched.execSQL(execStringSearched);
//                    String sqlStringSearched = "INSERT INTO srchd (search) VALUES (?)";
//                    SQLiteStatement statement = searched.compileStatement(sqlStringSearched);
//                    statement.bindString(1, searchView.getQuery().toString());
//                    statement.execute();
//                    searched.close();
//                    hasVoiceFocus = false;
//                } catch (SQLException e) { e.printStackTrace(); }
//            }
//        }
//    }

//    public void bringValues(String searchURL,
//                                      String from,
//                                      BookFinalAdapter adapter,
//                                      ArrayList<HashMap<String, String>> arrayList) {
//        showProgressBar(searchView, MainActivity.this);
//        arrayList.clear();
//        publisherFilterList.clear();
//        authorFilterList.clear();
//        search = 1;
//        all = 0;
//        count = 0;
//        mapInfoFinal = null;
//        pages = 0;
//        try { (new Retrofit.Builder().addConverterFactory(ScalarsConverterFactory.create())
//                .baseUrl("https://www.halkkitabevi.com/").client(new OkHttpClient().newBuilder().build()).build())
//                .create(ApiService.class).getPrices(searchURL).enqueue(new Callback<String>() {
//                @Override
//                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
//                    if (response.isSuccessful() && response.body() != null) {
//                        Document doc = Jsoup.parse(response.body());
//                        //String s = doc.select("#pager-wrapper > div > div").text();
//                        String s = doc.select("a.button.button_pager.button_pager_last").attr("href");
//                        if(s == null || s.isEmpty()) Log.d("s is empty","true");
//                        else Log.d("last",s);
//                        try {
//                            if(s != null || !s.isEmpty()) {
//                                int start = StringUtils.indexOf(s,"page");
//                                pages = Integer.parseInt(StringUtils.replace(StringUtils.substring(s,start),"page=",""));
//                            }
//                            Log.d("pages","" + pages);
//                        } catch (NumberFormatException e) {
//                            e.printStackTrace();
//                        }
//
//                        if(doc.select("div.no_product_found").text().contains("bulunamadı")) {
//                            Toast.makeText(MainActivity.this, " ¯\\_(ツ)_/¯ :  Hiçbir sonuç bulunamadı. ISBN ile aramayı deneyebilirsiniz.", Toast.LENGTH_LONG).show();
//                            hideProgressBar(searchView);
//                        } else {
//                            if(pages == 0) /*bookFinalAdapter*/adapter.setMoreDataAvailable(false);
//                            else /*bookFinalAdapter*/adapter.setMoreDataAvailable(true);
//                            if (mQuery != null && mQuery.matches("\\d+(?:\\.\\d+)?")) {
//                                mapInfoFinal = new HashMap<>();
//                                mapInfoFinal.put("name", doc.select("a.fl.col-12.text-description.detailLink").text());
//                                mapInfoFinal.put("author", doc.select("#productModelText").text());
//                                mapInfoFinal.put("publisher", doc.select("a.col.col-12.text-title.mt").text());
//                                mapInfoFinal.put("cover", doc.select("div:nth-child(1) > a > span > img").attr("src"));
//                                mapInfoFinal.put("individual", "https://www.bkmkitap.com" + doc.select("a.fl.col-12.text-description.detailLink").attr("href"));
//                                arrayList.add(mapInfoFinal);
//                                check("digit", adapter);
//                            } else {Log.d("bringValuesFromBKMFinal","5");
//                            ArrayList<String> publisherIDsList = new ArrayList<>();
//                            ArrayList<String> authorIDsList = new ArrayList<>();
//
//                                for (Element table : doc.select("#search_filter_form > ul > li:nth-child(1) > ul")) {
//                                    Log.d("tablepublisher",table.toString());
//                                    for (Element row : table.select("input[class=filters_checkbox]"
//                                            /*"#search_filter_form > ul > li:nth-child(1) > ul > li"*/)) {
//                                        Log.d("rowpublisher",row.toString());
//                                        publisherIDsList.add(row.select("input").attr("name"));
//                                    }
//                                }
//
//                                for (Element table : doc.select("#search_filter_form > ul > li:nth-child(3) > ul")) {
//                                    for (Element row : table.select("input[class=filters_checkbox]")) {
//                                        authorIDsList.add(row.select("input").attr("name"));
//                                    }
//                                }
//
//                            for (Element table : doc.select("#search_filter_form > ul > li:nth-child(1) > ul"))
//                                for (Element row : table.select("li")) publisherFilterList.add(row.select("label").text());
//
//                            for (Element table : doc.select("#search_filter_form > ul > li:nth-child(3) > ul"))
//                                for (Element row : table.select("li")) authorFilterList.add(row.select("label").text());
//
//                            Log.d("publisherFilterList",publisherFilterList.toString());
//                            Log.d("authorFilterList",authorFilterList.toString());
//                            Log.d("publisherIDsList",publisherIDsList.toString());
//                            Log.d("authorIDsList",authorIDsList.toString());
//                            for (Element table : doc.select("div.prd_list_container_box")) {
//                                    all = table.select("div.prd_list_container_box > div > ul > li").size();
//                                    for (Element row : table.select("div.prd_list_container_box > div > ul > li")) {
//                                        mapInfoFinal = new HashMap<>();
//                                        if (!mapInfoFinal.containsValue(row.select("div.name").text())
//                                                && !mapInfoFinal.containsValue(row.select("div.writer").text())
//                                                && !mapInfoFinal.containsValue(row.select("div.publisher").text())) {
//                                            mapInfoFinal.put("name", row.select("div.name").text());
//                                            mapInfoFinal.put("author", row.select("div.writer").text());
//                                            mapInfoFinal.put("publisher", row.select("div.publisher").text());
//                                            mapInfoFinal.put("cover", row.select("div.image_container > div > a > img")
//                                                    .attr("data-src"));
//                                            mapInfoFinal.put("individual", row.select("div.image_container > div > a").attr("href"));
//                                            arrayList.add(mapInfoFinal);
//                                        }
//                                        check(from, adapter);
//                                    }
//                                }
//                            }
//                            if (mapInfoFinal == null) {
//                                Log.d("bringValuesFromBKMFinal","6");
//                                Toast.makeText(MainActivity.this, "¯\\_(ツ)_/¯ :  Hiçbir sonuç bulunamadı. ISBN ile aramayı deneyebilirsiniz.", Toast.LENGTH_LONG).show();
//                                hideProgressBar(searchView);
//                            }
//                        }
//                    } else {
//                        Log.d("bringValuesFromBKMFinal","7");
//                        Toast.makeText(MainActivity.this, "ಠ_ಠ : Lütfen internet bağlantınızı kontrol edin ve yeniden deneyin.", Toast.LENGTH_LONG).show();
//                        hideProgressBar(searchView);
//                    }
//                }
//                @Override
//                public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
//                    Log.d("bringValuesFromBKMFinal","8");
//                    Toast.makeText(MainActivity.this, "ಠ_ಠ : Lütfen internet bağlantınızı kontrol edin ve yeniden deneyin.", Toast.LENGTH_LONG).show();
//                    hideProgressBar(searchView);
//                }
//            });
//        } catch (Exception e) {
//            Log.d("bringValuesFromBKMFinal","9");
//            Toast.makeText(MainActivity.this, "ಠ_ಠ : Lütfen internet bağlantınızı kontrol edin ve yeniden deneyin.", Toast.LENGTH_LONG).show();
//            hideProgressBar(searchView);
//            e.printStackTrace();
//        }
//        if ((mQuery != null) && (searchedList != null) || hasVoiceFocus) {
//            if(!searchedList.contains(searchView.getQuery().toString())) {
//                try {
//                    searched = MainActivity.this.openOrCreateDatabase("Searched", MODE_PRIVATE, null);
//                    String execStringSearched = "CREATE TABLE IF NOT EXISTS srchd (search VARCHAR)";
//                    searched.execSQL(execStringSearched);
//                    String sqlStringSearched = "INSERT INTO srchd (search) VALUES (?)";
//                    SQLiteStatement statement = searched.compileStatement(sqlStringSearched);
//                    statement.bindString(1, searchView.getQuery().toString());
//                    statement.execute();
//                    searched.close();
//                    hasVoiceFocus = false;
//                } catch (SQLException e) { e.printStackTrace(); }
//            }
//        }
//    }
    private void check(String from, RecyclerView.Adapter adapter) {
        switch (from) {
            case "text":
                count += 1;
                if (count == all) {
                    publisherFilter.getRecycledViewPool().clear();
                    authorFilter.getRecycledViewPool().clear();
                    publisherFilterAdapter.notifyDataChanged();
                    authorFilterAdapter.notifyDataChanged();
                    filterLine.setVisibility(View.VISIBLE);
                    horizontalScrollView.setVisibility(View.VISIBLE);
                    adapter.notifyDataSetChanged();
                    hideProgressBar(searchView);
                    //if (showList) listView.setVisibility(View.VISIBLE);
                }
                break;
            default:
                adapter.notifyDataSetChanged();
                hideProgressBar(searchView);
                if (showList) listView.setVisibility(View.VISIBLE);
                break;
        }
    }
    private void loadMore() {
        //if(isLoadMoreFinished) Toast.makeText(MainActivity.this, "Tüm sonuçlar yüklendi.", Toast.LENGTH_LONG).show();
        if(searchURL.contains("prpm[pub]") || searchURL.contains("prpm[wrt]")) {
            search++;
            int start = searchURL.indexOf("page=");
            int end = searchURL.indexOf("&prpm");
            String newSearchURL = searchURL.substring(start,end);
            searchURL = searchURL.replace(newSearchURL, "page="+search);

            Log.d("newSearchURL",newSearchURL);
            Log.d("searchURL","contains");
            //searchURL = searchURL + "&" + publisherIDsList.get(position) + "=" + publisherIDsList.get(position).replaceAll("\\D+","");
            Log.d("search",""+search);
            Utils.bringValues(MainActivity.this, searchURL,mQuery,"load", bookFinalAdapter,
                    publisherFilterAdapter, authorFilterAdapter, arrayListInfoFinal,
                    publisherFilterList, publisherIDsList, authorFilterList, authorIDsList,
                    horizontalScrollView,
                    filterLine, searchedList, progressBar1, search);
            //if(isLoadMoreFinished) Toast.makeText(MainActivity.this, "Tüm sonuçlar yüklendi.", Toast.LENGTH_LONG).show();
        }
        else {
            //Log.d("searchURL",searchURL);
            search++;
            Log.d("search",""+search);
            Utils.bringValues(MainActivity.this, "https://www.halkkitabevi.com/index.php?p=Products&q_field_active=0&ctg_id=&q=" + mQuery + "&search=&q_field=&page="+search/*StringUtils.replace(searchURL, "page="+search,"page="+search++)*/,mQuery,"load", bookFinalAdapter,
                    publisherFilterAdapter, authorFilterAdapter, arrayListInfoFinal,
                    publisherFilterList, publisherIDsList, authorFilterList, authorIDsList,
                    horizontalScrollView,
                    filterLine, searchedList, progressBar1, search);

        }


//        HashMap<String, String> mapLoad = new HashMap<>();
//        mapLoad.put("type", "load");
//        arrayListInfoFinal.add(mapLoad);
//        bookFinalAdapter.notifyItemInserted(arrayListInfoFinal.size() - 1);
//        //search++;
//        all = 0;
//        count = 0;
//        /*"https://kidega.com/arama?query=" + mQuery + "&page=" + String.valueOf(search)*/
//        try { (new Retrofit.Builder().addConverterFactory(ScalarsConverterFactory.create())
//                .baseUrl("https://www.halkkitabevi.com/").client(new OkHttpClient().newBuilder().build()).build())
//                .create(ApiService.class).getPrices(StringUtils.replace(searchURL, "page="+search,"page="+search++))
//                .enqueue(new Callback<String>() {
//                    @Override
//                    public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {
//                        if (response.isSuccessful() && response.body() != null) {
//                            Document doc = Jsoup.parse(response.body());
//                            for (Element table : doc.select("#products")) {
//                                all = table.select("#products > div").size();
//                                for (Element row : table.select("#products > div")) {
//                                    mapInfoFinal = new HashMap<>();
//                                    if (!mapInfoFinal.containsValue(row.select("div.title").text())
//                                            && !mapInfoFinal.containsValue(row.select("div.authorArea").text())
//                                            && !mapInfoFinal.containsValue(row.select("a.publisher").text())) {
//                                        mapInfoFinal.put("name", row.select("div.title").text());
//                                        mapInfoFinal.put("author", row.select("div.authorArea").text());
//                                        mapInfoFinal.put("publisher", row.select("a.publisher").text());
//                                        mapInfoFinal.put("cover", row.select("img.lazyload").attr("data-src"));
//                                        mapInfoFinal.put("individual", row.select("div.image > a").attr("href"));
//                                        arrayListSpare.add(mapInfoFinal);
//                                    }
//                                    check("text", bookFinalAdapter);
//                                }
//                            }
//                            arrayListInfoFinal.remove(arrayListInfoFinal.size() - 1);
//                            arrayListInfoFinal.addAll(arrayListSpare);
//                            check("load", bookFinalAdapter);
//                            bookFinalAdapter.notifyDataChanged();
//                            arrayListSpare.clear();
//                            if (search == pages) {
//                                bookFinalAdapter.setMoreDataAvailable(false);
//                                Toast.makeText(MainActivity.this, "Tüm sonuçlar yüklendi.", Toast.LENGTH_LONG).show();
//                            }
//                        }
//                    }
//                    @Override
//                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) { }
//                });
//        } catch (Exception e) { e.printStackTrace(); }
    }

//    private void loadMore() {
//        HashMap<String, String> mapLoad = new HashMap<>();
//        mapLoad.put("type", "load");
//        arrayListInfoFinal.add(mapLoad);
//        bookFinalAdapter.notifyItemInserted(arrayListInfoFinal.size() - 1);
//        search++;
//        all = 0;
//        count = 0;
//        try { (new Retrofit.Builder().addConverterFactory(ScalarsConverterFactory.create())
//                    .baseUrl("https://www.bkmkitap.com/").client(new OkHttpClient().newBuilder().build()).build())
//                    .create(ApiService.class).getPrices("https://www.bkmkitap.com/arama?q=" + mQuery + "&pg=" + String.valueOf(search))
//                    .enqueue(new Callback<String>() {
//                        @Override
//                        public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {
//                            if (response.isSuccessful() && response.body() != null) {
//                                Document doc = Jsoup.parse(response.body());
//                                for (Element table : doc.select("div.fl.col-12.catalogWrapper")) {
//                                    all = table.select("div.col.col-2.col-md-4.col-sm-6.col-xs-6.p-right.mb.productItem.zoom.ease").size();
//                                    for (Element row : table.select("div.col.col-2.col-md-4.col-sm-6.col-xs-6.p-right.mb.productItem.zoom.ease")) {
//                                        mapInfoFinal = new HashMap<>();
//                                        if (!mapInfoFinal.containsValue(row.select("a.fl.col-12.text-description.detailLink").text()) && !mapInfoFinal.containsValue(row.select("#productModelText").text()) && !mapInfoFinal.containsValue(row.select("a.col.col-12.text-title.mt").text())) {
//                                            mapInfoFinal.put("name", row.select("a.fl.col-12.text-description.detailLink").text());
//                                            mapInfoFinal.put("author", row.select("#productModelText").text());
//                                            mapInfoFinal.put("publisher", row.select("a.col.col-12.text-title.mt").text());
//                                            mapInfoFinal.put("cover", row.select("div:nth-child(1) > a > span > img").attr("src"));
//                                            mapInfoFinal.put("individual", "https://www.bkmkitap.com" + row.select("a.fl.col-12.text-description.detailLink").attr("href"));
//                                            arrayListSpare.add(mapInfoFinal);
//                                        }
//                                        check("text", bookFinalAdapter);
//                                    }
//                                }
//                                arrayListInfoFinal.remove(arrayListInfoFinal.size() - 1);
//                                arrayListInfoFinal.addAll(arrayListSpare);
//                                check("load", bookFinalAdapter);
//                                bookFinalAdapter.notifyDataChanged();
//                                arrayListSpare.clear();
//                                if (search == pages) {
//                                    bookFinalAdapter.setMoreDataAvailable(false);
//                                    Toast.makeText(MainActivity.this, "Tüm sonuçlar yüklendi.", Toast.LENGTH_LONG).show();
//                                }
//                            }
//                        }
//                        @Override
//                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) { }
//                    });
//        } catch (Exception e) { e.printStackTrace(); }
//    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (glm.findFirstCompletelyVisibleItemPosition() == -1) glm.scrollToPosition(glm.findFirstVisibleItemPosition());
        else glm.scrollToPosition(glm.findFirstCompletelyVisibleItemPosition());
        if (newConfig.orientation == 2) {
            switch (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) {
                case Configuration.SCREENLAYOUT_SIZE_LARGE: glm.setSpanCount(3);break;
                default: glm.setSpanCount(2);break;
            }
        } else {
            switch (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) {
                case Configuration.SCREENLAYOUT_SIZE_LARGE: glm.setSpanCount(2);break;
                default: glm.setSpanCount(1);break;
            }
        }
        listView.setLayoutManager(glm);
        listView.setAdapter(bookFinalAdapter);
    }
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        receiver = new BroadcastingInnerClass();
        update = findViewById(R.id.update);
        indexesForUpdate = new ArrayList<>();
        searchedList = new ArrayList<>();


//          categories = findViewById(R.id.categories);
//        categoryList = new ArrayList<>();
//        categoryOrder = new ArrayList<>();
//        categoryList.add(new CategoryModel("Bilim - Mühendislik",ResourcesCompat.getDrawable(getResources(),R.drawable.ic_bio_technology,null), "0"));
//        categoryList.add(new CategoryModel("Sınavlara Hazırlık",ResourcesCompat.getDrawable(getResources(),R.drawable.ic_education,null), "1"));
//        categoryList.add(new CategoryModel("Edebiyat",ResourcesCompat.getDrawable(getResources(),R.drawable.ic_reading,null), "2"));
//        categoryList.add(new CategoryModel("Hukuk",ResourcesCompat.getDrawable(getResources(),R.drawable.ic_shaking_hands,null), "3"));
//        categoryList.add(new CategoryModel("Felsefe",ResourcesCompat.getDrawable(getResources(),R.drawable.ic_personal_growth,null), "4"));
//        categoryList.add(new CategoryModel("Ekonomi",ResourcesCompat.getDrawable(getResources(),R.drawable.ic_financial_struggle,null), "5"));
//        categoryList.add(new CategoryModel("Sanat - Mimarlık",ResourcesCompat.getDrawable(getResources(),R.drawable.ic_ar,null), "6"));
//        categoryList.add(new CategoryModel("Sosyoloji",ResourcesCompat.getDrawable(getResources(),R.drawable.ic_abstract_brainstorm,null), "7"));
//        categoryList.add(new CategoryModel("Politika",ResourcesCompat.getDrawable(getResources(),R.drawable.ic_abstract_no_comments,null), "8"));
//        categoryList.add(new CategoryModel("Tarih",ResourcesCompat.getDrawable(getResources(),R.drawable.ic_library,null), "9"));
//        categoryList.add(new CategoryModel("Bilgisayar - Mobil",ResourcesCompat.getDrawable(getResources(),R.drawable.ic_abstract_working_process,null), "10"));
//        categoryList.add(new CategoryModel("Çocuk",ResourcesCompat.getDrawable(getResources(),R.drawable.ic_present,null), "11"));
//        categoryList.add(new CategoryModel("Sağlık",ResourcesCompat.getDrawable(getResources(),R.drawable.ic_hug,null), "12"));
//        categoryList.add(new CategoryModel("Hobi",ResourcesCompat.getDrawable(getResources(),R.drawable.ic_hobby,null), "13"));
//        categoryList.add(new CategoryModel("Medya - İletişim",ResourcesCompat.getDrawable(getResources(),R.drawable.ic_media,null), "14"));
//        categoryList.add(new CategoryModel("Spor",ResourcesCompat.getDrawable(getResources(),R.drawable.ic_sport,null), "15"));
//        categoryList.add(new CategoryModel("Turizm - Gezi - Rehber",ResourcesCompat.getDrawable(getResources(),R.drawable.ic_travel,null), "16"));
//        categories.setAdapter(new SuggestionAdapter(MainActivity.this, categoryList, "main"));
//        categories.setHasFixedSize(true);
//        LinearLayoutManager llm = new LinearLayoutManager(MainActivity.this);
//        llm.setAutoMeasureEnabled(false);
//        categories.setLayoutManager(llm);
//        categories.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(this, R.anim.layout_animation_down_to_up));
//        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(
//                new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.START | ItemTouchHelper.END, 0) {
//            @Override
//            public int interpolateOutOfBoundsScroll(@NonNull RecyclerView recyclerView, int viewSize, int viewSizeOutOfBounds, int totalSize, long msSinceStartScroll) {
//                final int direction = (int) Math.signum(viewSizeOutOfBounds);
//                Log.d("direction",""+direction);
//                Log.d("viewSize",""+viewSize);
//                Log.d("viewSizeOutOfBounds",""+viewSizeOutOfBounds);
//                Log.d("totalSize",""+totalSize);
//                Log.d("msSinceStartScroll",""+msSinceStartScroll);
//                return 8 * direction;
//            }
//            @Override
//            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
//                int fromPosition = viewHolder.getAdapterPosition();
//                int toPosition = target.getAdapterPosition();
//                Collections.swap(categoryList, fromPosition, toPosition);
//                recyclerView.getAdapter().notifyItemMoved(fromPosition, toPosition);
//                /*Log.d("fromPosition",""+fromPosition);
//                Log.d("toPosition",""+toPosition);*/
//                return false;
//            }
//
//            @Override
//            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
//
//            }
//        });
//        itemTouchHelper.attachToRecyclerView(categories);
//        categories.addOnItemTouchListener(new RecyclerTouchListener(this, categories, new RecyclerTouchListener.ClickListener() {
//            @Override
//            public void onClick(View view, final int position) {
//                try { more(categoryList.get(position).getCategoryName());
//                } catch (Exception e) {
//                    Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
//                    e.printStackTrace();
//                }
//            }
//            @Override
//            public void onLongClick(View view, int position) { }
//        }));
        howManySelected = findViewById(R.id.howManySelected);
        selectAll = findViewById(R.id.selectAll);
        selectAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked) {
                howMany = favList.size();
                howManySelected.setText(String.valueOf(favList.size()));
                howManySelected.setVisibility(View.VISIBLE);
                contextList.clear();
                contextList.addAll(favList);
                update.setVisibility(View.GONE);
            } else if(howMany == favList.size()) cancelContext();
            refreshAdapter();
        });
        findOptimumForAll = findViewById(R.id.findOptimumForAll);
        deleteAllFavs = findViewById(R.id.deleteAllFavs);
        nestedScrollView = findViewById(R.id.nestedScrollView);
        horizontalScrollView = findViewById(R.id.horizontalScrollView);
        contextList = new ArrayList<>();
        detect = getSharedPreferences("com.muniryenigul.kam", MODE_PRIVATE);
        /*Set categoryOrder = new HashSet<String>();
        detect.getStringSet("categoryOrder", categoryOrder);*/
        progressBar = findViewById(R.id.progressBar);
        progressBar1 = findViewById(R.id.progressBar1);
        arrayForSelectedSites = new ArrayList<>();
        //loadArrayList(arrayForSelectedSites, this);
        coverLoading = findViewById(R.id.coverLoading);
        MobileAds.initialize(this, "ca-app-pub-3925997615763525~4155017339");
        new RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("693C73C45AE0D1F6AB06B23524732691"));
        //adRequest = new AdRequest.Builder()/*.addTestDevice("D30B2A6CEF1A8291743DA9EB38246A4A")*/.build();
        //mAdView = findViewById(R.id.adView);
        horRecView = findViewById(R.id.horRecView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false);
        horRecView.setLayoutManager(linearLayoutManager);
        horRecView.setHasFixedSize(true);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(horRecView.getContext(), DividerItemDecoration.HORIZONTAL);
        dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.line));
        horRecView.addItemDecoration(dividerItemDecoration);
        horRecView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(this, R.anim.layout_animation_right_to_left));
        outer_contextLayout = findViewById(R.id.outer_contextLayout);
        outer_searchLayout = findViewById(R.id.outer_searchLayout);
        Button barcodeB = findViewById(R.id.barcodeButton);
        longClick(MainActivity.this, findViewById(R.id.voiceButton), "Sesli Arama: Mikrofonu Aç");
        longClick(MainActivity.this, barcodeB, "Barkodla Arama: Kamerayı Aç");
//        String[] publisherNames = getResources().getStringArray(R.array.filterPublisherNames);
//        String[] publisherIDs = getResources().getStringArray(R.array.filterPublisherIDs);
//        filterPublisherNames = new ArrayList<>(Arrays.asList(publisherNames));
//        filterPublisherIDs = new ArrayList<>(Arrays.asList(publisherIDs));
        searchView = findViewById(R.id.searchView);
        searchView.setQueryHint("Kitap / Yazar / Yayınevi / ISBN");
        favLayout = findViewById(R.id.favLayout);
        upLayout = findViewById(R.id.upLayout);
        searchedLayout = findViewById(R.id.searchedLayout);
        listView = findViewById(R.id.listView);
        filterLine = findViewById(R.id.filterLine);
        publisherFilter = findViewById(R.id.publisherFilter);
        authorFilter = findViewById(R.id.authorFilter);
        publisherFilterList = new ArrayList<>();
        publisherIDsList = new ArrayList<>();
        filteredList = new ArrayList<>();
        authorFilterList = new ArrayList<>();
        authorIDsList = new ArrayList<>();
        arrayListInfoFinal = new ArrayList<>();
        arrayListSpare = new ArrayList<>();
        searchedRecView = findViewById(R.id.searchedRecView);
        searchedRecView.setHasFixedSize(true);
        searchedRecView.addItemDecoration(new DividerItemDecoration(searchedRecView.getContext(), DividerItemDecoration.VERTICAL));
        searchedRecView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, false));
        searchedRecView.scheduleLayoutAnimation();
        bookFinalAdapter = new BookFinalAdapter(this, arrayListInfoFinal);
        publisherFilterAdapter = new BookFinalAdapter(this, publisherFilterList, publisherIDsList, "filter");
        authorFilterAdapter = new BookFinalAdapter(this, authorFilterList, authorIDsList,"filter");
        bookFinalAdapter.setLoadMoreListener(() -> listView.post(this::loadMore));
        listView.setHasFixedSize(true);

        if (getResources().getConfiguration().orientation == 2) {
            if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE) glm = new GridLayoutManager(this, 3);
            else glm = new GridLayoutManager(this, 2);
        } else {
            if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE) glm = new GridLayoutManager(this, 2);
            else glm = new GridLayoutManager(this, 1);
        }
        listView.setLayoutManager(glm);
        //listView.addItemDecoration(new BottomOffsetDecoration((int) getResources().getDimension(R.dimen._50dp)));
        //listView.addItemDecoration(new DividerItemDecoration(listView.getContext(), DividerItemDecoration.VERTICAL));
        listView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(this, R.anim.layout_animation_down_to_up));
        listView.setAdapter(bookFinalAdapter);
        listView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                hideKeyboard(listView);
            }
        });

        publisherFilter.setAdapter(publisherFilterAdapter);
        authorFilter.setAdapter(authorFilterAdapter);
        publisherFilter.setHasFixedSize(true);
        authorFilter.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(MainActivity.this);
        llm.setAutoMeasureEnabled(false);
        LinearLayoutManager llm2 = new LinearLayoutManager(MainActivity.this);
        llm2.setAutoMeasureEnabled(false);
        publisherFilter.setLayoutManager(llm);
        authorFilter.setLayoutManager(llm2);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        Log.d("height width",""+height+" " +width);
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = height / 2;
        listView.setLayoutParams(params);

        ViewGroup.LayoutParams paramForPublisherFilter = publisherFilter.getLayoutParams();
        paramForPublisherFilter.width =  ( width / 2 ) - 4;
        publisherFilter.setLayoutParams(paramForPublisherFilter);

        ViewGroup.LayoutParams paramForAuthorFilter = authorFilter.getLayoutParams();
        paramForAuthorFilter.width =  ( width / 2 ) - 4;
        authorFilter.setLayoutParams(paramForAuthorFilter);

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        final NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        shadowImage = findViewById(R.id.shadowImage);
        shadowImageForContext = findViewById(R.id.shadowImageForContext);
        TextView searchViewText = searchView.findViewById(searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null));
        searchViewText.setTextSize(18);
        searchViewText.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        searchViewText.setTypeface(ResourcesCompat.getFont(getApplicationContext(), R.font.josefinsans_medium));
        //searchView.findViewById(searchView.getContext().getResources().getIdentifier("android:id/search_plate", null, null)).setBackgroundColor(Color.rgb(255, 255, 255));
        closeButton = searchView.findViewById(searchView.getContext().getResources().getIdentifier("android:id/search_close_btn", null, null));
        closeButton.setImageDrawable(ContextCompat.getDrawable(MainActivity.this,R.drawable.cancel));
        /*closeButton = findViewById(R.id.closeButton);*/
        closeButton.setOnClickListener(v1 -> {
            showList = false;
            listView.setVisibility(View.GONE);
            horizontalScrollView.setVisibility(View.GONE);
            filterLine.setVisibility(View.GONE);
            hideProgressBar(searchView);
            closeButton.setVisibility(View.GONE);
            searchView.setQuery("", true);
            bringSearched();
            searchView.requestFocus();
        });
        searchView.setOnQueryTextFocusChangeListener((v12, hasFocus) -> {
            if (hasFocus) {
                Log.d("hasFocus","true");
                hasfocus = hasFocus;
                bringSearched();
                shadowImage.setVisibility(View.VISIBLE);
                upLayout.setVisibility(View.GONE);
            } else {
                Log.d("hasFocus","false");
                if (drawer.isDrawerOpen(GravityCompat.START)) drawer.closeDrawer(GravityCompat.START);
                shadowImage.setVisibility(View.GONE);
                if(isKeyboardOpen) {
                    Log.d("isKeyboardOpen","true");
                    hideKeyboard(v12);
                    if (searchedLayout.getVisibility() == View.VISIBLE) {
                        searchedLayout.setVisibility(View.GONE);
                        listView.setVisibility(View.GONE);
                        upLayout.setVisibility(View.VISIBLE);
                    }
                }
                else {Log.d("isKeyboardOpen","false");

                    if (searchedLayout.getVisibility() == View.VISIBLE) {
                        searchedLayout.setVisibility(View.GONE);
                        listView.setVisibility(View.GONE);
                        upLayout.setVisibility(View.VISIBLE);
                    filterLine.setVisibility(View.GONE);
                    horizontalScrollView.setVisibility(View.GONE);
                    closeButton.setVisibility(View.GONE);
                    searchView.setQuery("", true);
                    } else {
                        listView.setVisibility(View.GONE);
                        upLayout.setVisibility(View.VISIBLE);
                        filterLine.setVisibility(View.GONE);
                        horizontalScrollView.setVisibility(View.GONE);
                        closeButton.setVisibility(View.GONE);
                        searchView.setQuery("", true);

                    }
                }
                //upLayout.setVisibility(View.VISIBLE);
                //filterLine.setVisibility(View.GONE);
                //horizontalScrollView.setVisibility(View.GONE);
                //closeButton.setVisibility(View.GONE);
                //searchView.setQuery("", true);
                //if (listView.getVisibility() == View.VISIBLE) listView.setVisibility(View.GONE);

            }
        });
        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("searchView","onClick");
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.length() == 0) hideProgressBar(searchView);
                if (!receiver.isNetworkAvailable(MainActivity.this)) noInternet();
                else if (query.length() > 0) {
                    filteredList.clear();
                    search = 1;
                    searchURL = "https://www.halkkitabevi.com/index.php?p=Products&q_field_active=0&ctg_id=&q=" + query + "&search=&q_field=&page="+search;
                    sendToFind(query, searchURL, "text");
//                    publisherFilterList.clear();
//                    publisherIDsList.clear();
//                    authorFilterList.clear();
//                    authorIDsList.clear();
//                    filteredList.clear();
//                    arrayListInfoFinal.clear();
//                    search = 1;
//                    showList = true;
//                    mQuery = query;
//                    listView.setVisibility(View.VISIBLE);
//                    if (searchedLayout.isShown()) searchedLayout.setVisibility(View.GONE);
//                    hideKeyboard(searchView);
//                    Utils.bringValues(searchURL,mQuery,"text", bookFinalAdapter,
//                            publisherFilterAdapter, authorFilterAdapter, arrayListInfoFinal,
//                            publisherFilterList, publisherIDsList, authorFilterList, authorIDsList,
//                            horizontalScrollView,
//                            filterLine, searchedList, progressBar1, search);


                    if ((mQuery != null) && (searchedList != null) || hasVoiceFocus) {
                        if(!searchedList.contains(searchView.getQuery().toString())) {
                            try {
                                searched = MainActivity.this.openOrCreateDatabase("Searched", MODE_PRIVATE, null);
                                String execStringSearched = "CREATE TABLE IF NOT EXISTS srchd (search VARCHAR)";
                                searched.execSQL(execStringSearched);
                                String sqlStringSearched = "INSERT INTO srchd (search) VALUES (?)";
                                SQLiteStatement statement = searched.compileStatement(sqlStringSearched);
                                statement.bindString(1, searchView.getQuery().toString());
                                statement.execute();
                                searched.close();
                                hasVoiceFocus = false;
                            } catch (SQLException e) { e.printStackTrace(); }
                        }
                    }
                }
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) { return true; }
        });
        publisherFilter.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(),
                publisherFilter, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                try {
                    search = 1;
//                    if(!searchURL.contains(publisherIDsList.get(position))) {
//                        search = 1;
//                        searchURL = "https://www.halkkitabevi.com/index.php?p=Products&q_field_active=0&ctg_id=&q=" + mQuery + "&search=&q_field=&page="+search;
//
//                    }
                    if(!filteredList.contains(publisherIDsList.get(position))) filteredList.add(publisherIDsList.get(position));
                    else filteredList.remove(publisherIDsList.get(position));
                    if(!searchURL.contains(publisherIDsList.get(position))) {
                        if(!searchURL.contains("prpm[wrt]")) {
                            searchURL = "https://www.halkkitabevi.com/index.php?p=Products&q_field_active=0&ctg_id=&q=" + mQuery + "&search=&q_field=&page="+search;
                            searchURL = searchURL + "&" + publisherIDsList.get(position) + "=" + publisherIDsList.get(position).replaceAll("\\D+","");
                        } else {
                            int start = searchURL.indexOf("page=");
                            int end = searchURL.indexOf("&prpm");
                            String newSearchURL = searchURL.substring(start,end);
                            searchURL = searchURL.replace(newSearchURL, "page="+search);
                            searchURL = searchURL + "&" + publisherIDsList.get(position) + "=" + publisherIDsList.get(position).replaceAll("\\D+","");
                        }
                    } else {
                        int start = searchURL.indexOf("page=");
                        int end = searchURL.indexOf("&prpm");
                        String newSearchURL = searchURL.substring(start,end);
                        searchURL = searchURL.replace(newSearchURL, "page="+search);
                        searchURL = searchURL.replace("&" + publisherIDsList.get(position) + "=" + publisherIDsList.get(position).replaceAll("\\D+",""),"");
                    }
                    Log.d("searchURL",searchURL);
                    sendToFind(mQuery, searchURL, "text");
//                    Utils.bringValues(searchURL,mQuery,"text", bookFinalAdapter,
//                            publisherFilterAdapter, authorFilterAdapter, arrayListInfoFinal,
//                            publisherFilterList, publisherIDsList, authorFilterList, authorIDsList,
//                            horizontalScrollView,
//                            filterLine, searchedList, progressBar1, search);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onLongClick(View view, int position) {

            }
        }));
        authorFilter.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(),
                authorFilter, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                try {
                    search = 1;
                    if(!filteredList.contains(authorIDsList.get(position))) filteredList.add(authorIDsList.get(position));
                    else filteredList.remove(authorIDsList.get(position));
                    if(!searchURL.contains(authorIDsList.get(position))) {
                        if(!searchURL.contains("prpm[pub]")) {
                            searchURL = "https://www.halkkitabevi.com/index.php?p=Products&q_field_active=0&ctg_id=&q=" + mQuery + "&search=&q_field=&page="+search;
                            searchURL = searchURL + "&" + authorIDsList.get(position) + "=" + authorIDsList.get(position).replaceAll("\\D+","");
                        } else {
                            int start = searchURL.indexOf("page=");
                            int end = searchURL.indexOf("&prpm");
                            String newSearchURL = searchURL.substring(start,end);
                            searchURL = searchURL.replace(newSearchURL, "page="+search);
                            searchURL = searchURL + "&" + authorIDsList.get(position) + "=" + authorIDsList.get(position).replaceAll("\\D+","");
                        }

                    } else {
                        int start = searchURL.indexOf("page=");
                        int end = searchURL.indexOf("&prpm");
                        String newSearchURL = searchURL.substring(start,end);
                        searchURL = searchURL.replace(newSearchURL, "page="+search);
                        searchURL = searchURL.replace("&" + authorIDsList.get(position) + "=" + authorIDsList.get(position).replaceAll("\\D+",""),"");
                    }
                    Log.d("searchURL",searchURL);
                    sendToFind(mQuery, searchURL, "text");
//                    Utils.bringValues(searchURL,mQuery,"text", bookFinalAdapter,
//                            publisherFilterAdapter, authorFilterAdapter, arrayListInfoFinal,
//                            publisherFilterList, publisherIDsList, authorFilterList, authorIDsList,
//                            horizontalScrollView,
//                            filterLine, searchedList, progressBar1, search);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onLongClick(View view, int position) {

            }
        }));
        listView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), listView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                    favPosition = position;
                    if (!receiver.isNetworkAvailable(MainActivity.this)) noInternet();
                    else { progressBar.setVisibility(View.VISIBLE);
                        if (favSingleItem != null && favList != null && favList.contains(arrayListInfoFinal.get(position).get("name"))) createIntent(favSingleItem, favList.indexOf(arrayListInfoFinal.get(position).get("name")), "old");
                        else createIntentFinalDetail(arrayListInfoFinal, position, "detail");
                    }
            }
            @Override
            public void onLongClick(View view, int position) {
                progressBar.setVisibility(View.VISIBLE);
                createIntentFinalDetail(arrayListInfoFinal, position, "direct");
            }
        }));
        searchedRecView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), searchedRecView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                searchView.setQuery(searchedList.get(position), true);
            }
            @Override
            public void onLongClick(View view, int position) {
                try {
                    searched = openOrCreateDatabase("Searched", MODE_PRIVATE, null);
                    String execStringSearched = "CREATE TABLE IF NOT EXISTS srchd (search VARCHAR)";
                    searched.execSQL(execStringSearched);
                    String sqlString = "DELETE FROM srchd WHERE search LIKE (?)";
                    SQLiteStatement statement = searched.compileStatement(sqlString);
                    statement.bindString(1, searchedList.get(position));
                    statement.execute();
                    searched.close();
                    searchedList.remove(position);
                    searchedAdapter.notifyDataSetChanged();
                    if (searchedList.size() == 0) searchedLayout.setVisibility(View.GONE);
                    bringSearched();
                } catch (SQLException e) { e.printStackTrace(); }
            }
        }));
    }
    private void sendToFind (String query, String searchURL, String from) {

        publisherFilterList.clear();
        publisherIDsList.clear();
        authorFilterList.clear();
        authorIDsList.clear();
        //filteredList.clear();
        arrayListInfoFinal.clear();
        //search = 1;
        showList = true;
        mQuery = query;
        listView.setVisibility(View.VISIBLE);
        //searchURL = "https://www.halkkitabevi.com/index.php?p=Products&q_field_active=0&ctg_id=&q=" + query + "&search=&q_field=&page="+search;
        if (searchedLayout.isShown()) searchedLayout.setVisibility(View.GONE);
        hideKeyboard(searchView);
        Utils.bringValues(MainActivity.this, searchURL,mQuery,from, bookFinalAdapter,
                publisherFilterAdapter, authorFilterAdapter, arrayListInfoFinal,
                publisherFilterList, publisherIDsList, authorFilterList, authorIDsList,
                horizontalScrollView,
                filterLine, searchedList, progressBar1, search);
    }
    private void openKeyboard() {
        isKeyboardOpen = true;
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null && inputMethodManager.isActive()) inputMethodManager.toggleSoftInputFromWindow(searchView.getWindowToken(), InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }
    public void hideKeyboard(View view) {
        isKeyboardOpen = false;
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
    private void bringSearched() {
        listView.setVisibility(View.GONE);
        /*
        listView.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_flame_searching));*/
        openKeyboard();
        searchedList.clear();
        try {
            searched = openOrCreateDatabase("Searched", MODE_PRIVATE, null);
            String execStringSearched = "CREATE TABLE IF NOT EXISTS srchd (search VARCHAR)";
            searched.execSQL(execStringSearched);
            Cursor cursor = searched.rawQuery("SELECT * FROM srchd", null);
            int searchIx = cursor.getColumnIndex("search");
            if (cursor.moveToFirst()) {
                do { searchedList.add(0, cursor.getString(searchIx));
                } while (cursor.moveToNext());
            }
            cursor.close();
            searched.close();
        } catch (SQLException e) { e.printStackTrace(); }
        if (searchedList.size() > 0) {
            searchedLayout.setVisibility(View.VISIBLE);
            searchedAdapter = new FavouriteAdapter(/*MainActivity.this,*/searchedList, "history");
            searchedRecView.setAdapter(searchedAdapter);
            searchedRecView.setVisibility(View.VISIBLE);
        }
    }
    public void backContext(View view) { cancelContext(); }
    public void cancelContext() {
        nestedScrollView.setScrollingEnabled(true);
        selectAll.setChecked(false);
        indexesForUpdate.clear();
        contextList.clear();
        howMany = 0;
        outer_contextLayout.setVisibility(View.GONE);
        outer_searchLayout.setVisibility(View.VISIBLE);
        findOptimumForAll.setVisibility(View.VISIBLE);
        deleteAllFavs.setVisibility(View.VISIBLE);
        if (shadowImageForContext != null) shadowImageForContext.setVisibility(View.GONE);
        isContext = false;
        /*horRecView.clearAnimation();*/
        refreshAdapter();
    }
    public void deleteMultiple(View view) {
        View alertLayout = getLayoutInflater().inflate(R.layout.custom_click, null);
        final LinearLayout buttonLay = alertLayout.findViewById(R.id.buttons_lay);
        buttonLay.setVisibility(View.VISIBLE);
        final Button buttonCancel = alertLayout.findViewById(R.id.buttonDelete);
        final Button buttonDelete = alertLayout.findViewById(R.id.buttonSeeOrSearch);
        buttonDelete.setVisibility(View.VISIBLE);
        buttonCancel.setVisibility(View.VISIBLE);
        final TextView textView = alertLayout.findViewById(R.id.textView);
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        StringBuilder sb = new StringBuilder();
        sb.append(contextList.toString().replace("[", "").replace("]", "").replaceAll(",", ",\n")).append("\n\nSilinsin mi?\n");
        textView.setText(sb);
        buttonCancel.setText("Vazgeç");
        buttonDelete.setText("Sil");
        builder.setCancelable(true).setView(alertLayout);
        Dialog dialog = builder.create();
        dialog.show();
        buttonCancel.setOnClickListener(v -> dialog.dismiss());
        buttonDelete.setOnClickListener(v -> {
            dialog.dismiss();
            for (int p = 0; p < contextList.size(); p++) {
                String delete = contextList.get(p);
                MainActivity.this.deleteDatabase(delete);
                try {
                    fav = MainActivity.this.openOrCreateDatabase("Favs", MODE_PRIVATE, null);
                    String execStringFav = "CREATE TABLE IF NOT EXISTS fav (name VARCHAR)";
                    fav.execSQL(execStringFav);
                    String sqlString = "DELETE FROM fav WHERE name LIKE (?)";
                    SQLiteStatement statement = fav.compileStatement(sqlString);
                    statement.bindString(1, delete);
                    statement.execute();
                    fav.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            cancelContext();
            bringFavs();
        });
    }
    public void findOptimumForAll(View view) {
        progressBar.setVisibility(View.VISIBLE);
        startActivity(new Intent(MainActivity.this, TableAndBestPriceActivity.class));
        hasfocus = false;
    }
    public void findOptimum(View view) {
            progressBar.setVisibility(View.VISIBLE);//29.07.2019
            hasfocus = false;//29.07.2019
            Intent i = new Intent(MainActivity.this, TableAndBestPriceActivity.class);
            switch (view.getTag().toString()) {
                case "findOptimumForAll": i.putExtra("from", "findOptimumForAll");break;
                case "findOptimumForSelecteds":
                    i.putExtra("from", "findOptimumForSelecteds");
                    i.putStringArrayListExtra("contextList", contextList);
                    break;
            }
            startActivity(i);
            cancelContext();
    }

    public void more(View view) {
            if (!receiver.isNetworkAvailable(MainActivity.this)) noInternet();
            else {
                caseforMore(view.getTag().toString());
                hasfocus = false;
            }
    }
    /*public void more(String categoryName) {
        if (!receiver.isNetworkAvailable(MainActivity.this)) noInternet();
        else {
            caseforMore(categoryName);
            hasfocus = false;
        }
    }*/
    void caseforMore(String categoryName) {
        Intent i = new Intent(MainActivity.this, ShowMoreActivity.class);
        i.putExtra("info", "main");
        i.putExtra("current name", categoryName);
        startActivity(i);
    }
        /*private void caseforMore(String info, String currentName, String currentLink, ArrayList<String> list, ArrayList<String> linkList) {
        Intent i = new Intent(MainActivity.this, ShowMoreActivity.class);
        i.putExtra("info", info);
        i.putExtra("current name", currentName);
        i.putExtra("current link", currentLink);
        i.putStringArrayListExtra("list", list);
        i.putStringArrayListExtra("linkList", linkList);
        startActivity(i);
    }*/
    public void deleteAllFavs(View view) {
            View alertLayout = getLayoutInflater().inflate(R.layout.custom_click, null);
            final LinearLayout buttonLay = alertLayout.findViewById(R.id.buttons_lay);
            buttonLay.setVisibility(View.VISIBLE);
            final Button buttonCancel = alertLayout.findViewById(R.id.buttonDelete);
            final Button buttonDelete = alertLayout.findViewById(R.id.buttonSeeOrSearch);
            buttonDelete.setVisibility(View.VISIBLE);
            buttonCancel.setVisibility(View.VISIBLE);
            final TextView textView = alertLayout.findViewById(R.id.textView);
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            textView.setText("Tüm favoriler silinsin mi?");
            buttonCancel.setText("Vazgeç");
            buttonDelete.setText("Sil");
            builder.setCancelable(true).setView(alertLayout);
            Dialog dialog = builder.create();
            dialog.show();
            buttonCancel.setOnClickListener(v -> dialog.dismiss());
            buttonDelete.setOnClickListener(v -> {
                dialog.dismiss();
                for (int p = 0; p < favList.size(); p++) {
                    String delete = favList.get(p);
                    MainActivity.this.deleteDatabase(delete);
                    try {
                        fav = MainActivity.this.openOrCreateDatabase("Favs", MODE_PRIVATE, null);
                        String execStringFav = "CREATE TABLE IF NOT EXISTS fav (name VARCHAR)";
                        fav.execSQL(execStringFav);
                        String sqlString = "DELETE FROM fav WHERE name LIKE (?)";
                        SQLiteStatement statement = fav.compileStatement(sqlString);
                        statement.bindString(1, delete);
                        statement.execute();
                        fav.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                bringFavs();
            });

    }
    public void update(View view) {
        if (!receiver.isNetworkAvailable(MainActivity.this)) noInternet();
        else {
            View alertLayout = getLayoutInflater().inflate(R.layout.custom_click, null);
            final LinearLayout buttonLay = alertLayout.findViewById(R.id.buttons_lay);
            buttonLay.setVisibility(View.VISIBLE);
            final Button buttonCancel = alertLayout.findViewById(R.id.buttonDelete);
            final Button buttonDelete = alertLayout.findViewById(R.id.buttonSeeOrSearch);
            buttonDelete.setVisibility(View.VISIBLE);
            buttonCancel.setVisibility(View.VISIBLE);
            final TextView textView = alertLayout.findViewById(R.id.textView);
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            StringBuilder sb = new StringBuilder();
            sb.append(contextList.toString().replace("[", "").replace("]", "").replaceAll(",", ",\n")).append("\n\nFiyatlar güncellensin mi?\n");
            textView.setText(sb);
            buttonCancel.setText(R.string.vazgec);
            buttonDelete.setText(R.string.myself);
            builder.setCancelable(true).setView(alertLayout);
            Dialog dialog = builder.create();
            dialog.show();
            buttonCancel.setOnClickListener(v -> dialog.dismiss());
            buttonDelete.setOnClickListener(v -> {
                dialog.dismiss();
                if (!receiver.isNetworkAvailable(MainActivity.this)) noInternet();
                else createIntent(favSingleItem, Integer.parseInt(indexesForUpdate.get(0))/*favList.indexOf(contextList.toString())*//*favPosition*/, "update");
            });
        }
    }
    public void shadow(View view) {
        searchedRecView.setVisibility(View.GONE);
        hideProgressBar(searchView);
        searchView.clearFocus();
    }
    public void shadowForContext(View view) {
        cancelContext();
        shadowImageForContext.setVisibility(View.GONE);
        nestedScrollView.setScrollingEnabled(true);
    }
    /*public void bringFavs() {
        favList = new ArrayList<>();
        favSingleItem = new ArrayList<>();
        try { fav = MainActivity.this.openOrCreateDatabase("Favs", MODE_PRIVATE, null);
            fav.execSQL("CREATE TABLE IF NOT EXISTS fav (name VARCHAR)");
            Cursor cursor = fav.rawQuery("SELECT * FROM fav", null);
            int nameIx = cursor.getColumnIndex("name");
            if (cursor.moveToFirst()) {
                do { favList.add(0, cursor.getString(nameIx));
                } while (cursor.moveToNext());
            }
            cursor.close();
            fav.close();
        } catch (SQLException e) { e.printStackTrace(); }
        for (int p = 0; p < favList.size(); p++) {
            try { database = MainActivity.this.openOrCreateDatabase(StringUtils.replace(favList.get(p), "/", ""), MODE_PRIVATE, null);
                database.execSQL("CREATE TABLE IF NOT EXISTS info (name VARCHAR, author VARCHAR, publisher VARCHAR, cover VARCHAR, individual VARCHAR, coverBig VARCHAR, isbn VARCHAR,description VARCHAR)");
                Cursor cursor2 = database.rawQuery("SELECT * FROM info", null);
                int nameIx = cursor2.getColumnIndex("name");
                int authorIx = cursor2.getColumnIndex("author");
                int publisherIx = cursor2.getColumnIndex("publisher");
                int coverIx = cursor2.getColumnIndex("cover");
                int individualIx = cursor2.getColumnIndex("individual");
                int coverBigIx = cursor2.getColumnIndex("coverBig");
                int isbnIx = cursor2.getColumnIndex("isbn");
                int descriptionIx = cursor2.getColumnIndex("description");
                if (cursor2.moveToFirst()) {
                    do { favSingleItem.add(new SingleItemModel(cursor2.getString(nameIx), cursor2.getString(authorIx), cursor2.getString(publisherIx), cursor2.getString(coverIx), cursor2.getString(individualIx), cursor2.getString(coverBigIx), cursor2.getString(isbnIx), cursor2.getString(descriptionIx)));
                    } while (cursor2.moveToNext());
                }
                cursor2.close();
                database.close();
            } catch (SQLException e) { e.printStackTrace(); }
        }
        categories.setAdapter(new SuggestionAdapter(MainActivity.this, categoryList, "main"));
        categories.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(MainActivity.this);
        llm.setAutoMeasureEnabled(false);
        categories.setLayoutManager(llm);
        categories.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(this, R.anim.layout_animation_down_to_up));
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.START | ItemTouchHelper.END, 0) {
                    @Override
                    public int interpolateOutOfBoundsScroll(@NonNull RecyclerView recyclerView, int viewSize, int viewSizeOutOfBounds, int totalSize, long msSinceStartScroll) {
                        final int direction = (int) Math.signum(viewSizeOutOfBounds);
                        Log.d("direction",""+direction);
                        Log.d("viewSize",""+viewSize);
                        Log.d("viewSizeOutOfBounds",""+viewSizeOutOfBounds);
                        Log.d("totalSize",""+totalSize);
                        Log.d("msSinceStartScroll",""+msSinceStartScroll);
                        return 8 * direction;
                    }
                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                        int fromPosition = viewHolder.getAdapterPosition();
                        int toPosition = target.getAdapterPosition();
                        Collections.swap(categoryList, fromPosition, toPosition);
                        recyclerView.getAdapter().notifyItemMoved(fromPosition, toPosition);
                *//*Log.d("fromPosition",""+fromPosition);
                Log.d("toPosition",""+toPosition);*//*
                        return false;
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

                    }
                });
        itemTouchHelper.attachToRecyclerView(categories);
        categories.addOnItemTouchListener(new RecyclerTouchListener(this, categories, new RecyclerTouchListener.ClickListener() {
            *//*long mLastClickTime = System.currentTimeMillis();
            final long CLICK_TIME_INTERVAL = 300;*//*
            @Override
            public void onClick(View view, final int position) {
                *//*long now = System.currentTimeMillis();
                if (now - mLastClickTime < CLICK_TIME_INTERVAL) {
                    return;
                }
                mLastClickTime = now;*//*
                try { more(categoryList.get(position).getCategoryName());
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
            @Override
            public void onLongClick(View view, int position) { }
        }));
    }*/
    public void bringFavs() {
        favList = new ArrayList<>();
        favSingleItem = new ArrayList<>();
        try { fav = MainActivity.this.openOrCreateDatabase("Favs", MODE_PRIVATE, null);
            fav.execSQL("CREATE TABLE IF NOT EXISTS fav (name VARCHAR)");
            Cursor cursor = fav.rawQuery("SELECT * FROM fav", null);
            int nameIx = cursor.getColumnIndex("name");
            if (cursor.moveToFirst()) {
                do { favList.add(0, cursor.getString(nameIx));
                } while (cursor.moveToNext());
            }
            cursor.close();
            fav.close();
            Log.d("FavList",favList.toString());
        } catch (SQLException e) { e.printStackTrace(); }

        for (int p = 0; p < favList.size(); p++) {
            try { database = MainActivity.this.openOrCreateDatabase(StringUtils.replace(favList.get(p), "/", ""), MODE_PRIVATE, null);
                database.execSQL("CREATE TABLE IF NOT EXISTS info (name VARCHAR, author VARCHAR, publisher VARCHAR, cover VARCHAR, individual VARCHAR, coverBig VARCHAR, isbn VARCHAR,description VARCHAR)");
                Cursor cursor2 = database.rawQuery("SELECT * FROM info", null);
                int nameIx = cursor2.getColumnIndex("name");
                int authorIx = cursor2.getColumnIndex("author");
                int publisherIx = cursor2.getColumnIndex("publisher");
                int coverIx = cursor2.getColumnIndex("cover");
                int individualIx = cursor2.getColumnIndex("individual");
                int coverBigIx = cursor2.getColumnIndex("coverBig");
                int isbnIx = cursor2.getColumnIndex("isbn");
                int descriptionIx = cursor2.getColumnIndex("description");
                if (cursor2.moveToFirst()) {
                    do { favSingleItem.add(new SingleItemModel(cursor2.getString(nameIx), cursor2.getString(authorIx), cursor2.getString(publisherIx), cursor2.getString(coverIx), cursor2.getString(individualIx), cursor2.getString(coverBigIx), cursor2.getString(isbnIx), cursor2.getString(descriptionIx)));
                    } while (cursor2.moveToNext());
                }
                cursor2.close();
                database.close();
            } catch (SQLException e) { e.printStackTrace(); }
        }
        if (favSingleItem.size() >= 1) {
            favAdapter = new SuggestionAdapter(MainActivity.this, selectAll, favSingleItem, contextList, howManySelected, outer_contextLayout, outer_searchLayout, shadowImageForContext, nestedScrollView, update, favLayout, coverLoading, deleteAllFavs, findOptimumForAll);
            horRecView.setAdapter(favAdapter);
            favLayout.setVisibility(View.VISIBLE);
        } else favLayout.setVisibility(View.GONE);
    }
    private void refreshAdapter() {
        favAdapter.contextList = contextList;
        favAdapter.feedItemList = favSingleItem;
        favAdapter.notifyDataSetChanged();
    }
    public void sendIntent(ArrayList<SingleItemModel> singleItem, int position, String volume, String pages, String info) {
        Intent intent = new Intent(MainActivity.this, PriceActivity.class);
        intent.putExtra("info", info);
        intent.putExtra("name", singleItem.get(position).getName());
        intent.putExtra("author", singleItem.get(position).getAuthor());
        intent.putExtra("publisher", singleItem.get(position).getPublisher());
        intent.putExtra("cover", singleItem.get(position).getCover());
        intent.putExtra("description", singleItem.get(position).getDescription());
        intent.putExtra("coverBig", singleItem.get(position).getCoverBig());
        intent.putExtra("isbn", singleItem.get(position).getIsbn());
        intent.putExtra("individual", singleItem.get(position).getIndividual());
        if(volume != null) intent.putExtra("volume", volume);
        if(pages != null) intent.putExtra("pages", pages);
        hasfocus = false;
        startActivity(intent);
        progressBar.setVisibility(View.GONE);
    }
    public void createIntent(ArrayList<SingleItemModel> singleItem, int position, String info) {
        cancelContext();
        try { (new Retrofit.Builder().addConverterFactory(ScalarsConverterFactory.create())
                    .baseUrl("https://www.bkmkitap.com/").client(new OkHttpClient().newBuilder().build()).build())
                    .create(ApiService.class).getPrices(singleItem.get(position).getIndividual()).enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Document doc = Jsoup.parse(response.body());
                        sendIntent(singleItem, position, doc.select("div.col.cilt.col-12 > div:nth-child(1) > span:nth-child(2)").text(), doc.select("div.col.cilt.col-12 > div:nth-child(2) > span:nth-child(2)").text(), info);
                    } else sendIntent(singleItem, position, null, null, info);
                }
                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) { sendIntent(singleItem, position, null, null, info); }
            });
        } catch (Exception e) {
            e.printStackTrace();
            sendIntent(singleItem, position, null, null, info);
        }
    }
    private void createIntentFinalDetail(ArrayList<HashMap<String, String>> arrayList, int position, String info) {
        if (!receiver.isNetworkAvailable(MainActivity.this)) {
            progressBar.setVisibility(View.GONE);
            noInternet();
        } else if (arrayList.get(position).get("name") != null && favList != null && favList.contains(arrayList.get(position).get("name"))) createIntent(favSingleItem, favList.indexOf(arrayList.get(position).get("name")), "old");
        else if (info.equals("direct")) {
            if (arrayForSelectedSites == null || arrayForSelectedSites.isEmpty()) noSelection("ContentTouch");
            else createIntentFinalDetailFinal(arrayList, position, info);
        } else createIntentFinalDetailFinal(arrayList, position, info);
    }
    private void createIntentFinalDetailFinal(ArrayList<HashMap<String, String>> arrayList, int position, String info) {
        if (!Objects.requireNonNull(arrayList.get(position).get("name")).contains("Error")) {
            try {
                Log.d("individual",arrayList.get(position).get("individual"));
                (new Retrofit.Builder().addConverterFactory(ScalarsConverterFactory.create())
                        .baseUrl("https://www.halkkitabevi.com/").client(new OkHttpClient().newBuilder().build()).build())
                        .create(ApiService.class).getPrices(arrayList.get(position).get("individual")).enqueue(new Callback<String>() {

                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Document doc = Jsoup.parse(response.body());
                            String src = doc.select("div.__product_fields > div:nth-child(1) > div:nth-child(3)").text();
                            //int index = src.indexOf("=");
                            Log.d("src", src);
                            String volume /*= doc.select("div.__product_fields > div:nth-child(3) > div:nth-child(3)").text()*/ = null;
                            String pages /*= doc.select("div.__product_fields > div:nth-child(2) > div:nth-child(3)").text()*/ = null;

                            for (Element table : doc.select("div.col2.__col2 > div.__product_fields")) {
                                for (Element row : table.select("div.__product_fields > div")) {
                                    Log.d("row.select", row.select("div").text());
                                    if (row.select("div").text().contains("Sayfa Sayısı")) {
                                        String pages2 = row.select("div").text();
                                        int end = pages2.indexOf("Sayfa Sayısı",1);
                                        pages = pages2.substring(0,end);
                                        Log.d("row.select pages", pages);
                                    } else if (row.select("div").text().contains("Kapak Türü")) {
                                        String volume2 = row.select("div").text();
                                        int end = volume2.indexOf("Kapak Türü",1);
                                        volume = volume2.substring(0,end);
                                        Log.d("row.select volume", volume);
                                    }
                                }
                            }
                            Intent intent = new Intent(MainActivity.this, PriceActivity.class);
                            intent.putExtra("info", info);
                            intent.putExtra("name", arrayList.get(position).get("name"));
                            intent.putExtra("author", arrayList.get(position).get("author"));
                            intent.putExtra("publisher", arrayList.get(position).get("publisher"));
                            intent.putExtra("cover", arrayList.get(position).get("cover"));
                            StringBuilder sb = new StringBuilder();
                            for (Element table : doc.select("div.wysiwyg.prd_description.noContext")) {
                                if (table.select("div.wysiwyg.prd_description.noContext > p") != null && table.select("div.wysiwyg.prd_description.noContext > p").size() > 0) {
                                    for (Element row : table.select("div.wysiwyg.prd_description.noContext > p")) {
                                        if(!row.text().isEmpty()) sb.append("\n\n").append(row.text());
                                    }
                                } else {
                                    for (Element row : table.select("div.wysiwyg.prd_description.noContext")) {
                                        if(!row.text().isEmpty()) sb.append("\n\n").append(row.text());
                                    }
                                }

                            }
                            Log.d("description", sb.toString());
                            intent.putExtra("description", StringUtils.replace(sb.toString(),"\n\n","",1));
                            intent.putExtra("coverBig", arrayList.get(position).get("cover"));
                            intent.putExtra("volume", StringUtils.replace(volume,"Kapak Türü : ",""));
                            intent.putExtra("pages", StringUtils.replace(pages,"Sayfa Sayısı : ",""));
                            intent.putExtra("isbn", src);
                            /*if (!src.substring(index + 1).isEmpty()) intent.putExtra("isbn", src.substring(index + 1));
                            else if (!doc.select("div.col.col-6.centerBlock > div.col.cilt.col-12 > div:nth-child(3) > span:nth-child(2)").text().isEmpty()) intent.putExtra("isbn", doc.select("div.col.col-6.centerBlock > div.col.cilt.col-12 > div:nth-child(3) > span:nth-child(2)").text());
                            else intent.putExtra("isbn", doc.select("div.col.cilt.col-12 > div > span:nth-child(2)").text());*/
                            intent.putExtra("individual", arrayList.get(position).get("individual"));
                            hasfocus = false;
                            startActivity(intent);
                            progressBar.setVisibility(View.GONE);
                        } else progressBar.setVisibility(View.GONE);
                    }
                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(MainActivity.this, "Lütfen internet bağlantınızı kontrol ediniz.", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                progressBar.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Bir hata meydana geldi. Lütfen tekrar deneyiniz.", Toast.LENGTH_SHORT).show();
            }
        } else {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(MainActivity.this, "Lütfen önerileri güncelleyiniz.", Toast.LENGTH_SHORT).show();
        }
    }
//    static class BottomOffsetDecoration extends RecyclerView.ItemDecoration {
//        private int mBottomOffset;
//        BottomOffsetDecoration(int bottomOffset) {
//            mBottomOffset = bottomOffset;
//        }
//        @Override
//        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
//            super.getItemOffsets(outRect, view, parent, state);
//            int dataSize = state.getItemCount();
//            int position = parent.getChildAdapterPosition(view);
//            if (dataSize > 0 && position == dataSize - 1) outRect.set(0, 0, 0, mBottomOffset);
//            else outRect.set(0, 0, 0, 0);
//        }
//    }
    public class BroadcastingInnerClass extends BroadcastReceiver {
        boolean connected = false;
        @Override
        public void onReceive(Context context, Intent intent) { isNetworkAvailable(MainActivity.this); }
        public boolean isNetworkAvailable(Context context) {
            ConnectivityManager connectivity = (ConnectivityManager) MainActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity != null) {
                NetworkInfo[] info = connectivity.getAllNetworkInfo();
                for (NetworkInfo anInfo : info) {
                    if (anInfo.getState() == NetworkInfo.State.CONNECTED) {
                        if (!connected) {
                            connected = true;
                            if (dialog != null && dialog.isShowing()) dialog.dismiss();
//                            mAdView.loadAd(adRequest);
//                            if (mAdView.getVisibility() != View.VISIBLE) mAdView.setVisibility(View.VISIBLE);
                            /*if (!detect.getBoolean("firstRun", true) && !dismiss) {
                                pFirst = true;
                            }*/
                        }
                        return true;
                    }
                }
            }
            connected = false;
            return false;
        }
    }
    private void loadArrayList(ArrayList<String> arrayList, Context mContext) {
//        if (detect.getBoolean("3.8", true)) {
//            detect.edit().putBoolean("3.8", false).apply();
//            clearSelectedSites();
//        }
        arrayList.clear();
        SharedPreferences prefs = mContext.getSharedPreferences("preference2", 0);
        int size = prefs.getInt("arrayForSelectedSites" + "_size", 0);
        for (int i = 0; i < size; i++)
            if (!arrayList.contains(prefs.getString("arrayForSelectedSites" + "_" + i, " "))) arrayList.add(prefs.getString("arrayForSelectedSites" + "_" + i, " "));
    }
//    private void clearSelectedSites() {
//        getSharedPreferences("preference", 0).edit().clear().apply();
//        getSharedPreferences("preference2", 0).edit().clear().apply();
//        if(favList != null && favList.size() > 0) noSelection("updateSites");
//    }
    public void noInternet() {
        View alertLayout = getLayoutInflater().inflate(R.layout.custom_click, null);
        final LinearLayout buttonLay = alertLayout.findViewById(R.id.buttons_lay);
        buttonLay.setVisibility(View.VISIBLE);
        final Button buttonConnect = alertLayout.findViewById(R.id.buttonSeeOrSearch);
        buttonConnect.setVisibility(View.VISIBLE);
        final TextView textView = alertLayout.findViewById(R.id.textView);
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        textView.setText("İnternet bağlantısı yok.");
        buttonConnect.setText("Bağlan");
        builder.setCancelable(true).setView(alertLayout);
        dialog = builder.create();
        dialog.show();
        buttonConnect.setOnClickListener(v -> {
            if (dialog != null && dialog.isShowing()) dialog.dismiss();
            startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
            hasfocus = false;
        });
    }
    public void noSelection(String from) {
        if (from.equals("updateSites")) {
            View alertLayout = getLayoutInflater().inflate(R.layout.custom_click, null);
            final TextView textView = alertLayout.findViewById(R.id.textView);
            final Button buttonUpdate = alertLayout.findViewById(R.id.buttonUpdate);
            final Button buttonMyself = alertLayout.findViewById(R.id.buttonMyself);
            final LinearLayout buttons_lay2 = alertLayout.findViewById(R.id.buttons_lay2);
            StringBuilder sb = new StringBuilder();
            buttons_lay2.setVisibility(View.VISIBLE);
            //buttonUpdate.setVisibility(View.VISIBLE);
            buttonMyself.setVisibility(View.VISIBLE);
            buttonMyself.setText("Tamam");
            sb.append("Bir site isminde değişiklik olduğu için lütfen favoriler listesini güncelleyiniz.");
            //restoreArrayAndArrayList();
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            textView.setText(sb.toString());
            builder.setCancelable(true).setView(alertLayout);
            Dialog d = builder.create();
            d.show();
            /*buttonUpdate.setOnClickListener(v -> {
                d.dismiss();
                p = null;
                SharedPreferences prefs = getSharedPreferences("preference", 0);
                SharedPreferences.Editor editor = prefs.edit();
                SharedPreferences prefs2 = getSharedPreferences("preference2", 0);
                SharedPreferences.Editor editor2 = prefs2.edit();
                String[] sitesAddresses = getResources().getStringArray(R.array.listValues);
                boolean[] checkedItems = new boolean[sitesAddresses.length];
                for (int i = 0; i < checkedItems.length; i++) checkedItems[i] = true;
                editor.putInt("checkedItems" + "_size", checkedItems.length);
                for (int i = 0; i < checkedItems.length; i++) editor.putBoolean("checkedItems" + "_" + i, checkedItems[i]);
                editor.apply();
                Collections.addAll(arrayForSelectedSites, sitesAddresses);
                editor2.putInt("arrayForSelectedSites" + "_size", arrayForSelectedSites.size());
                int t = 0;
                for (int i = 0; i < arrayForSelectedSites.size(); i++) {
                    editor2.putString("arrayForSelectedSites" + "_" + i, arrayForSelectedSites.get(i));
                    t++;
                    if (t == arrayForSelectedSites.size() - 1) progressBar.setVisibility(View.GONE);
                }
                editor2.apply();
            });*/
            buttonMyself.setOnClickListener(v -> {
                d.dismiss();
                //intent("restore");
            });
        } else intent(from);
    }
    private void restoreArrayAndArrayList() {
        SharedPreferences prefs = getSharedPreferences("preference", 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("checkedItems" + "_size", 0);
        editor.clear().apply();
        SharedPreferences prefs2 = getSharedPreferences("preference2", 0);
        SharedPreferences.Editor editor2 = prefs2.edit();
        editor2.putInt("arrayForSelectedSites" + "_size", 0);
        editor2.clear().apply();
        arrayForSelectedSites.clear();
    }
    public void intent(String from) {
        startActivity(new Intent(MainActivity.this, Settings2Activity.class).putExtra("from", from));
        hasfocus = false;
    }
    public void longClick(Context context, Button button, String string) {
        button.setOnLongClickListener(v -> { Toast.makeText(MainActivity.this, string, Toast.LENGTH_SHORT).show();
            return true;
        });
    }
    public void drawerMenu(View view) {
            if (isSearchFocused) searchView.clearFocus();
            else if (!drawer.isDrawerOpen(GravityCompat.START))  {
                    drawer.openDrawer(GravityCompat.START);
                    hideKeyboard(view);
                }
    }
    public void barcodeScan(View view) {
            if (!receiver.isNetworkAvailable(MainActivity.this)) noInternet();
            else if (arrayForSelectedSites == null || arrayForSelectedSites.isEmpty()) noSelection("Scan");
            else {
                startActivity(new Intent(MainActivity.this, ScanActivity.class));
                hasfocus = false;
            }
    }
    public void voice(View view) {
        if (!receiver.isNetworkAvailable(MainActivity.this)) noInternet();
        else promptSpeechInput();
    }

//    public void showKeyboard(View view) {
//        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
//        if (inputMethodManager != null) inputMethodManager.showSoftInputFromInputMethod(view.getWindowToken(), 0);
//    }
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Aradığım kitap/yazar/yayınevi...");
        try {
            hasfocus = false;
            hasVoiceFocus = true;
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) { Toast.makeText(MainActivity.this, "Desteklenmiyor", Toast.LENGTH_SHORT).show(); }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == REQ_CODE_SPEECH_INPUT) {
                if (resultCode == Activity.RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    searchView.clearFocus();
                    searchView.setQuery(result.get(0), true);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) searchView.setFocusedByDefault(true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
        }
    }
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_settings) intent("drawer");
        else if (id == R.id.nav_privacy) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://docs.google.com/document/d/1AMYDOsmjBoZGwzC9uvJlKhq2sfYihhUt5QsZuenToCE/edit?usp=sharing"));
            startActivity(browserIntent);
        } else if (id == R.id.nav_terms) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://docs.google.com/document/d/1kOjwXvV9_Gj4DYVkVgNcrVlqy-IbXeZMQzpsLzFft5Q/edit?usp=sharing"));
            startActivity(browserIntent);
        } else if (id == R.id.nav_share) {
            try {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_SUBJECT, "Çitlembik");
                String sAux = "\nAklımızdan geçen kitapların en ucuz yoldan elimize ulaşması için :\n\n";
                sAux = sAux + "https://play.google.com/store/apps/details?id=" + getPackageName();
                i.putExtra(Intent.EXTRA_TEXT, sAux);
                startActivity(i);
            } catch (Exception e) { e.printStackTrace(); }
        } else if (id == R.id.nav_rate) {
            try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + MainActivity.this.getPackageName())).addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK));
            } catch (ActivityNotFoundException e) { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + MainActivity.this.getPackageName()))); }
        }
        hasfocus = false;
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    public void showProgressBar(SearchView searchView, Context context) {
        int id = searchView.getContext().getResources().getIdentifier("android:id/search_plate", null, null);
        if (searchView.findViewById(id).findViewById(R.id.progressBar1) != null) searchView.findViewById(id).findViewById(R.id.progressBar1).animate().setDuration(200).alpha(1).start();
        else {
            View v = LayoutInflater.from(MainActivity.this).inflate(R.layout.progress_item, null);
            ((ViewGroup) searchView.findViewById(id)).addView(v, 1);
        }
    }
    public void hideProgressBar(SearchView searchView) {
        int id = searchView.getContext().getResources().getIdentifier("android:id/search_plate", null, null);
        if (searchView.findViewById(id).findViewById(R.id.progressBar1) != null)
            searchView.findViewById(id).findViewById(R.id.progressBar1).animate().setDuration(200).alpha(0).start();
    }
}