package com.muniryenigul.kam.activities;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.muniryenigul.kam.MainActivity;
import com.muniryenigul.kam.R;
import com.muniryenigul.kam.ers.BookFinalAdapter;
import com.muniryenigul.kam.ers.RecyclerTouchListener;
import com.muniryenigul.kam.models.SingleItemModel;
import com.muniryenigul.kam.interfaces.ApiService;
import com.muniryenigul.kam.services.ServiceWithRetrofit;
import com.squareup.picasso.Picasso;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import static com.muniryenigul.kam.MainActivity.favList;
import static com.muniryenigul.kam.MainActivity.favSingleItem;
import static com.muniryenigul.kam.activities.PriceActivity.database;
import static com.muniryenigul.kam.activities.PriceActivity.fav;
import static com.muniryenigul.kam.activities.PriceActivity.stillSearch;
public class ShowMoreActivity extends AppCompatActivity {
    private RecyclerView recShowMore;
    private ProgressBar progressBar;
    private ArrayList<HashMap<String, String>> arrayList,arrayListSpare;
    private ArrayList<String> arrayForSelectedSites;
    private BookFinalAdapter bookFinalAdapter;
    private Spinner spinner;
    private BroadcastingInnerClass receiver;
    private Dialog dialog;
    public int search=1, all, count;
    public static int pages;
    private String mQuery, currentNameBook;
    private HashMap<String, String> mapInfoFinal;
    private ImageView coverLoading;
    private boolean firstRun=false;
    private GridLayoutManager glm;
    public static boolean searched = false;
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(glm.findFirstCompletelyVisibleItemPosition()==-1) glm.scrollToPosition(glm.findFirstVisibleItemPosition());
        else glm.scrollToPosition(glm.findFirstCompletelyVisibleItemPosition());
        if(newConfig.orientation==2) {
            switch(getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) {
                case Configuration.SCREENLAYOUT_SIZE_LARGE: glm.setSpanCount(5);break;
                case Configuration.SCREENLAYOUT_SIZE_NORMAL: glm.setSpanCount(4);break;
                case Configuration.SCREENLAYOUT_SIZE_SMALL: glm.setSpanCount(3);break;
                default:
            }
        } else {
            switch(getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) {
                case Configuration.SCREENLAYOUT_SIZE_LARGE: glm.setSpanCount(3); break;
                case Configuration.SCREENLAYOUT_SIZE_NORMAL: glm.setSpanCount(2); break;
                case Configuration.SCREENLAYOUT_SIZE_SMALL: glm.setSpanCount(1); break;
                default:
            }
        }
        recShowMore.setLayoutManager(glm);
        recShowMore.setAdapter(bookFinalAdapter);
    }
    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(receiver);
    }
    @Override
    protected void onStart() {
        super.onStart();
        register(receiver);
    }
    private void register(BroadcastingInnerClass receiver) { registerReceiver(receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)); }
    @Override
    public void onBackPressed() {
        if(coverLoading.getVisibility()==View.VISIBLE) coverLoading.setVisibility(View.GONE);
        else {
            stillSearch = false;
            super.onBackPressed();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_more);
        receiver = new BroadcastingInnerClass();
        arrayList=new ArrayList<>();
        arrayListSpare = new ArrayList<>();
        spinner = findViewById(R.id.spinner);
        coverLoading = findViewById(R.id.coverLoading);
        arrayForSelectedSites = new ArrayList<>();
        loadArrayList(arrayForSelectedSites,this);
        recShowMore=findViewById(R.id.recShowMore);
        progressBar=findViewById(R.id.progressBar);
        if(getResources().getConfiguration().orientation==2) {
            switch(getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) {
                case Configuration.SCREENLAYOUT_SIZE_LARGE: glm = new GridLayoutManager(this, 5); break;
                case Configuration.SCREENLAYOUT_SIZE_NORMAL: glm = new GridLayoutManager(this, 4); break;
                case Configuration.SCREENLAYOUT_SIZE_SMALL: glm = new GridLayoutManager(this, 3); break;
                default:
            }
        } else {
            switch(getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) {
                case Configuration.SCREENLAYOUT_SIZE_LARGE: glm = new GridLayoutManager(this, 3); break;
                case Configuration.SCREENLAYOUT_SIZE_NORMAL: glm = new GridLayoutManager(this, 2); break;
                case Configuration.SCREENLAYOUT_SIZE_SMALL: glm = new GridLayoutManager(this, 1); break;
                default:
            }
        }
        recShowMore.setLayoutManager(glm);
        recShowMore.setHasFixedSize(true);
        recShowMore.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recShowMore, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                progressBar.setVisibility(View.VISIBLE);
                try { createIntentFinal(arrayList, position, "detail");
                } catch (Exception e) {
                    Toast.makeText(ShowMoreActivity.this, ":( Bir hata meydana geldi.", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
            @Override public void onLongClick(View view, int position) {
                progressBar.setVisibility(View.VISIBLE);
                try { createIntentFinal(arrayList, position, "direct");
                } catch (Exception e) {
                    Toast.makeText(ShowMoreActivity.this, ":( Bir hata meydana geldi.", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }));
        if (!receiver.isNetworkAvailable()) {
            firstRun=false;
            noInternet();
        } else {
            firstRun=true;
            load();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        bringFavs();
        loadArrayList(arrayForSelectedSites,ShowMoreActivity.this);
        coverLoading.setVisibility(View.GONE);
    }
    public void bringFavs() {
        favList = new ArrayList<>();
        favSingleItem = new ArrayList<>();
        try {
            fav = ShowMoreActivity.this.openOrCreateDatabase("Favs", MODE_PRIVATE, null);
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
            try {
                database = ShowMoreActivity.this.openOrCreateDatabase(StringUtils.replace(favList.get(p), "/", ""), MODE_PRIVATE, null);
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
    }
    public void home(View view) {
        caseforMore("home",null,null);
        finish();
    }
    private void createIntentFinal(ArrayList<HashMap<String, String>> arrayList, int position, String way) {
        currentNameBook=arrayList.get(position).get("name");
        if (!receiver.isNetworkAvailable()) {
            if (dialog != null) dialog.dismiss();
            progressBar.setVisibility(View.GONE);
            noInternet();
        } else {
            if (!Objects.requireNonNull(arrayList.get(position).get("name")).contains("Error")) {
                try { (new Retrofit.Builder().addConverterFactory(ScalarsConverterFactory.create())
                            .baseUrl("https://www.bkmkitap.com/").client(new OkHttpClient().newBuilder().build()).build())
                            .create(ApiService.class).getPrices(arrayList.get(position).get("individual")).enqueue(new Callback<String>() {
                        @SuppressLint("InflateParams")
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                Document doc = Jsoup.parse(response.body());
                                StringBuilder sb = new StringBuilder();
                                progressBar.setVisibility(View.GONE);
                                if (arrayList.get(position).get("name") != null && favList != null && favList.contains(arrayList.get(position).get("name"))) {
                                    Log.d("dialog","1");
                                    if (!receiver.isNetworkAvailable()) {
                                        progressBar.setVisibility(View.GONE);
                                        noInternet();
                                    } else if (searched && getIntent().getStringExtra("book name") != null && getIntent().getStringExtra("book name").equals(currentNameBook)) finish();
                                    else createIntent(favSingleItem, favList.indexOf(arrayList.get(position).get("name")), "old");
                                } else if (/*searched && */getIntent().getStringExtra("book name") != null && getIntent().getStringExtra("book name").equals(currentNameBook)) finish();
                                else { Log.d("dialog","3");
                                    if (!receiver.isNetworkAvailable()) {
                                        progressBar.setVisibility(View.GONE);
                                        noInternet();
                                    } else if(way.equals("direct") && arrayForSelectedSites.isEmpty()) noSelection("ContentTouch");
                                    else {
                                        String src = doc.select("div.fl.col-12 > iframe").attr("src");
                                        for (Element table : doc.select("#productDetailTab")) {
                                            for (Element row : table.select("#productDetailTab > div > p")) {
                                                sb.append(row.text()).append("\n").append("\n");
                                            }
                                        }
                                        int index = src.indexOf("=");
                                        Intent intent = new Intent(ShowMoreActivity.this, PriceActivity.class);
                                        intent.putExtra("info", way);
                                        intent.putExtra("name", arrayList.get(position).get("name"));
                                        intent.putExtra("author", arrayList.get(position).get("author"));
                                        intent.putExtra("publisher", arrayList.get(position).get("publisher"));
                                        intent.putExtra("cover", arrayList.get(position).get("cover"));
                                        intent.putExtra("description", sb.toString());
                                        intent.putExtra("coverBig", arrayList.get(position).get("cover"));
                                        if (!src.substring(index + 1).isEmpty()) intent.putExtra("isbn", src.substring(index + 1));
                                        else if (!doc.select("div.col.col-6.centerBlock > div.col.cilt.col-12 > div:nth-child(3) > span:nth-child(2)").text().isEmpty()) intent.putExtra("isbn", doc.select("div.col.col-6.centerBlock > div.col.cilt.col-12 > div:nth-child(3) > span:nth-child(2)").text());
                                        else intent.putExtra("isbn", doc.select("div.col.cilt.col-12 > div > span:nth-child(2)").text());
                                        intent.putExtra("individual", arrayList.get(position).get("individual"));
                                        intent.putExtra("volume", doc.select("div.col.cilt.col-12 > div:nth-child(1) > span:nth-child(2)").text());
                                        intent.putExtra("pages", doc.select("div.col.cilt.col-12 > div:nth-child(2) > span:nth-child(2)").text());
                                        startActivity(intent);
                                    }
                                }
                            } else {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(ShowMoreActivity.this, "Lütfen tekrar deneyiniz.", Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(ShowMoreActivity.this, "Lütfen tekrar deneyiniz.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ShowMoreActivity.this, "Lütfen tekrar deneyiniz.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    private void caseforMore(String info, String currentName, String currentLink) {
        Intent i;
        if(info.equals("home"))  {
            i= new Intent(ShowMoreActivity.this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        } else {
            i= new Intent(ShowMoreActivity.this, ShowMoreActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        }
        i.putExtra("info", info);
        i.putExtra("current name", currentName);
        i.putExtra("current link", currentLink);
        i.putStringArrayListExtra("list", null);
        i.putStringArrayListExtra("linkList", null);
        startActivity(i);
    }
    public void createIntent(ArrayList<SingleItemModel> singleItem, int position, String info) {
        Picasso.get().load(singleItem.get(position).getCoverBig()).fit().error(R.drawable./*error*/ic_virus).into(coverLoading, new com.squareup.picasso.Callback() {
                    @Override public void onSuccess() {
                        coverLoading.setVisibility(View.VISIBLE);
                    }
                    @Override public void onError(Exception e) { }
                });
            try {
                (new Retrofit.Builder().addConverterFactory(ScalarsConverterFactory.create())
                        .baseUrl("https://www.bkmkitap.com/").client(new OkHttpClient().newBuilder().build()).build())
                        .create(ApiService.class).getPrices(singleItem.get(position).getIndividual()).enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            if (arrayForSelectedSites == null || arrayForSelectedSites.isEmpty()) noSelection("ContentTouch");
                            else {
                                Document doc = Jsoup.parse(response.body());
                                Intent intent = new Intent(ShowMoreActivity.this, PriceActivity.class);
                                intent.putExtra("info", info);
                                intent.putExtra("name", singleItem.get(position).getName());
                                intent.putExtra("author", singleItem.get(position).getAuthor());
                                intent.putExtra("publisher", singleItem.get(position).getPublisher());
                                intent.putExtra("cover", singleItem.get(position).getCover());
                                intent.putExtra("description", singleItem.get(position).getDescription());
                                intent.putExtra("coverBig", singleItem.get(position).getCoverBig());
                                intent.putExtra("isbn", singleItem.get(position).getIsbn());
                                intent.putExtra("individual", singleItem.get(position).getIndividual());
                                intent.putExtra("volume", doc.select("div.col.cilt.col-12 > div:nth-child(1) > span:nth-child(2)").text());
                                intent.putExtra("pages", doc.select("div.col.cilt.col-12 > div:nth-child(2) > span:nth-child(2)").text());
                                startActivity(intent);
                            }
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) { progressBar.setVisibility(View.GONE); }
                });
            } catch (Exception e) {
                e.printStackTrace();
                progressBar.setVisibility(View.GONE);
            }
    }
    public void noSelection(String from) {
        firstRun=true;
        intent(from);
    }
    public void intent(String from) { startActivity(new Intent(ShowMoreActivity.this, Settings2Activity.class).putExtra("from", from)); }
    private void loadArrayList(ArrayList<String> arrayList, Context mContext) {
        arrayList.clear();
        SharedPreferences prefs = mContext.getSharedPreferences("preference2", 0);
        int size = prefs.getInt("arrayForSelectedSites" + "_size", 0);
        for (int i = 0; i < size; i++) arrayList.add(prefs.getString("arrayForSelectedSites" + "_" + i, " "));
    }
    public void noInternet() {
        View alertLayout = getLayoutInflater().inflate(R.layout.custom_click, null);
        final LinearLayout buttons_lay= alertLayout.findViewById(R.id.buttons_lay);
        buttons_lay.setVisibility(View.VISIBLE);
        final Button buttonConnect = alertLayout.findViewById(R.id.buttonSeeOrSearch);
        buttonConnect.setVisibility(View.VISIBLE);
        final TextView textView = alertLayout.findViewById(R.id.textView);
        AlertDialog.Builder builder = new AlertDialog.Builder(ShowMoreActivity.this);
        textView.setText("İnternet bağlantısı yok.");
        buttonConnect.setText("Bağlan");
        builder.setCancelable(true).setView(alertLayout);
        dialog = builder.create();
        dialog.show();
        buttonConnect.setOnClickListener(v -> {
            if (dialog != null) dialog.dismiss();
            startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
        });
    }
    public class BroadcastingInnerClass extends BroadcastReceiver {
        boolean connected = false;
        @Override
        public void onReceive(Context context, Intent intent) {
            isNetworkAvailable();
        }
        public boolean isNetworkAvailable() {
            ConnectivityManager connectivity = (ConnectivityManager) ShowMoreActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity != null) {
                NetworkInfo[] info = connectivity.getAllNetworkInfo();
                if (info != null) {
                    for (NetworkInfo anInfo : info) {
                        if (anInfo.getState() == NetworkInfo.State.CONNECTED) {
                            if (!connected) {
                                connected = true;
                                if (dialog != null && dialog.isShowing()) dialog.dismiss();
                                if(!firstRun) load();
                            }
                            return true;
                        }
                    }
                }
            }
            connected = false;
            return false;
        }
    }
    private void load() {
        ArrayAdapter<String> spinnerAdapter;
        List<String> list=new ArrayList<>();
        List<String> listLink=new ArrayList<>();
        if(getIntent().getStringExtra("info")!=null) {
            if(!getIntent().getStringExtra("info").equals("main")) {
                list.add(getIntent().getStringExtra("current name"));
                listLink.add(getIntent().getStringExtra("current link"));
            } else {
                switch (getIntent().getStringExtra("current name")) {
                    case /*"Bilim - Mühendislik"*/"moreScience":
                        list = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.bilim)));
                        listLink = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.bilim_link)));
                        break;
                    case /*"Sınavlara Hazırlık"*/"moreSinav":
                        list = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.sinavlara_hazirlik)));
                        listLink = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.sinavlara_hazirlik_link)));
                        break;
                    case /*"Edebiyat"*/"moreEdebiyat":
                        list = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.edebiyat)));
                        listLink = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.edebiyat_link)));
                        break;
                    case /*"Hukuk"*/"moreHukuk":
                        list = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.hukuk)));
                        listLink = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.hukuk_link)));
                        break;
                    case /*"Felsefe"*/"moreFelsefe":
                        list = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.felsefe)));
                        listLink = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.felsefe_link)));
                        break;
                    case /*"Ekonomi"*/"moreEkonomi":
                        list = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.ekonomi)));
                        listLink = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.ekonomi_link)));
                        break;
                    case /*"Sanat - Mimarlık"*/"moreSanat":
                        list = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.sanat_mimarlik)));
                        listLink = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.sanat_mimarlik_link)));
                        break;
                    case /*"Sosyoloji"*/"moreSosyoloji":
                        list = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.sosyoloji)));
                        listLink = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.sosyoloji_link)));
                        break;
                    case /*"Politika"*/"morePolitika":
                        list = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.politika)));
                        listLink = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.politika_link)));
                        break;
                    case /*"Tarih"*/"moreTarih":
                        list = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.tarih)));
                        listLink = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.tarih_link)));
                        break;
                    case /*"Sağlık"*/"moreSaglik":
                        list = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.saglik)));
                        listLink = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.saglik_link)));
                        break;
                    case /*"Çocuk"*/"moreCocuk":
                        list = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.cocuk)));
                        listLink = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.cocuk_link)));
                        break;
                    case /*"Bilgisayar - Mobil*/"moreBilgisayarMobil":
                        list = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.bilgisayar_mobil)));
                        listLink = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.bilgisayar_mobil_link)));
                        break;
                    case /*"Hobi"*/"moreHobi":
                        list = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.hobi)));
                        listLink = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.hobi_link)));
                        break;
                    case /*"Spor"*/"moreSpor":
                        list = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.spor)));
                        listLink = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.spor_link)));
                        break;
                    case /*"Turizm - Gezi - Rehber"*/"moreTurizm":
                        list = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.turizm)));
                        listLink = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.turizm_link)));
                        break;
                    case "moreMedya":
                        list = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.media)));
                        listLink = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.media_link)));
                        break;
                }
            }
            spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list){
                Typeface tfavv;
                int color = ResourcesCompat.getColor(getResources(),R.color.colorPrimaryDark,null);
                public View getView(int position, View convertView, android.view.ViewGroup parent) {
                    tfavv = ResourcesCompat.getFont(getApplicationContext(), R.font.josefinsans_medium);
                    TextView v = (TextView) super.getView(position, convertView, parent);
                    v.setTypeface(tfavv);
                    v.setTextColor(color);
                    v.setTextSize(20);
                    return v;
                }

                public View getDropDownView(int position, View convertView, android.view.ViewGroup parent) {
                    TextView v = (TextView) super.getView(position, convertView, parent);
                    v.setTypeface(tfavv);
                    v.setTextColor(color);
                    v.setTextSize(32);
                    return v;
                }
            };
            Objects.requireNonNull(spinnerAdapter).setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(spinnerAdapter);
            spinner.setSelection(spinnerAdapter.getPosition(getIntent().getStringExtra("current name")));
            if(getIntent().getStringExtra("info")!=null && !getIntent().getStringExtra("info").equals("main")) spinner.setEnabled(false);
            List<String> finalListLink = listLink;
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    mQuery= finalListLink.get(parent.getSelectedItemPosition());
                    if (!receiver.isNetworkAvailable()) noInternet();
                    else searchSug(mQuery);
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        } else Toast.makeText(this, "Bir hata meydana geldi. Lütfen yeniden deneyiniz.", Toast.LENGTH_LONG).show();
    }
    private void searchSug(String link) {
        stillSearch=true;
        progressBar.setVisibility(View.VISIBLE);
        search=1;
        Intent intent = new Intent(ShowMoreActivity.this, ServiceWithRetrofit.class);
        intent.putExtra("receiver", new Receiver(null));
        intent.putExtra("link", link);
        startService(intent);
    }
    public class Receiver extends ResultReceiver {
        Handler handler = new Handler();
        Receiver(Handler handler) {
            super(handler);
        }
        @Override
        protected void onReceiveResult(int resultCode, final Bundle resultData) {
            if (resultCode == 1 && resultData != null) {
                handler.post(() -> {
                    try {
                        arrayList = (ArrayList<HashMap<String, String>>) resultData.getSerializable("arraylist");
                        bookFinalAdapter = new BookFinalAdapter(ShowMoreActivity.this, arrayList,"arraylist");
                        bookFinalAdapter.setLoadMoreListener(() -> recShowMore.post(this::loadMore));
                        if(pages==0 && stillSearch) {
                            bookFinalAdapter.setMoreDataAvailable(false);
                            Toast.makeText(ShowMoreActivity.this, "Tüm sonuçlar yüklendi.", Toast.LENGTH_SHORT).show();
                        } else bookFinalAdapter.setMoreDataAvailable(true);
                        recShowMore.setAdapter(bookFinalAdapter);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    progressBar.setVisibility(View.GONE);
                });
            }
            super.onReceiveResult(resultCode, resultData);
        }
        private void loadMore() {
            HashMap<String, String> mapLoad = new HashMap<>();
            mapLoad.put("type", "load");
            arrayList.add(mapLoad);
            bookFinalAdapter.notifyItemInserted(arrayList.size() - 1);
            search++;
            all = 0;
            count = 0;
            String newQuery;
            if(!mQuery.contains("arama")) newQuery=StringUtils.join(mQuery , "?pg=" ,search);
            else newQuery=StringUtils.join(mQuery , "&pg=" ,search);
            try {
                (new Retrofit.Builder().addConverterFactory(ScalarsConverterFactory.create())
                        .baseUrl("https://www.bkmkitap.com/").client(new OkHttpClient().newBuilder().build()).build())
                        .create(ApiService.class)
                        .getPrices(newQuery)
                        .enqueue(new Callback<String>() {
                            @Override
                            public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Document doc = Jsoup.parse(response.body());
                                    for (Element table : doc.select("div.fl.col-12.catalogWrapper")) {
                                        all = table.select("div.col.col-2.col-md-4.col-sm-6.col-xs-6.p-right.mb.productItem.zoom.ease").size();
                                        for (Element row : table.select("div.col.col-2.col-md-4.col-sm-6.col-xs-6.p-right.mb.productItem.zoom.ease")) {
                                            mapInfoFinal = new HashMap<>();
                                            if (!mapInfoFinal.containsValue(row.select("a.fl.col-12.text-description.detailLink").text()) && !mapInfoFinal.containsValue(row.select("#productModelText").text()) && !mapInfoFinal.containsValue(row.select("a.col.col-12.text-title.mt").text())) {
                                                mapInfoFinal.put("name", row.select("a.fl.col-12.text-description.detailLink").text());
                                                mapInfoFinal.put("author", row.select("#productModelText").text());
                                                mapInfoFinal.put("publisher", row.select("a.col.col-12.text-title.mt").text());
                                                mapInfoFinal.put("cover", row.select("div:nth-child(1) > a > span > img").attr("src"));
                                                mapInfoFinal.put("individual", "https://www.bkmkitap.com" + row.select("a.fl.col-12.text-description.detailLink").attr("href"));
                                                arrayListSpare.add(mapInfoFinal);
                                            }
                                            check("text", bookFinalAdapter);
                                        }
                                    }
                                    arrayList.remove(arrayList.size() - 1);
                                    arrayList.addAll(arrayListSpare);
                                    check("load", bookFinalAdapter);
                                    recShowMore.getRecycledViewPool().clear();
                                    bookFinalAdapter.notifyDataChanged();
                                    arrayListSpare.clear();
                                    if (search == pages) {
                                        bookFinalAdapter.setMoreDataAvailable(false);
                                        Toast.makeText(ShowMoreActivity.this, "Tüm sonuçlar yüklendi.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                            @Override
                            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) { }
                        });
            } catch (Exception e) { e.printStackTrace(); }
        }
    }
    private void check(String from, RecyclerView.Adapter adapter) {
        if(from.equals("text")) {
            count += 1;
            if (count == all) {
                recShowMore.getRecycledViewPool().clear();
                adapter.notifyDataSetChanged();
            }
        } else {
            recShowMore.getRecycledViewPool().clear();
            adapter.notifyDataSetChanged();
        }
    }
}