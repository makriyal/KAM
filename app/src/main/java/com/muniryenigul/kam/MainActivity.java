package com.muniryenigul.kam;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import androidx.annotation.NonNull;
import com.google.android.material.navigation.NavigationView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
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
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import static com.muniryenigul.kam.activities.PriceActivity.database;
import static com.muniryenigul.kam.activities.PriceActivity.fav;
import com.google.android.material.tabs.TabLayout;
import com.muniryenigul.kam.activities.PriceActivity;
import com.muniryenigul.kam.activities.Settings2Activity;
import com.muniryenigul.kam.activities.ShowMoreActivity;
import com.muniryenigul.kam.activities.TableAndBestPriceActivity;
import com.muniryenigul.kam.ers.ViewPagerAdapter;
import com.muniryenigul.kam.ers.FavouriteAdapter;
import com.muniryenigul.kam.ers.RecyclerTouchListener;
import com.muniryenigul.kam.ers.SuggestionAdapter;
import com.muniryenigul.kam.fragments.FragmentHalk;
import com.muniryenigul.kam.fragments.FragmentKitapsec;
import com.muniryenigul.kam.fragments.FragmentKitapyurdu;
import com.muniryenigul.kam.models.SingleItemModel;
import com.muniryenigul.kam.interfaces.ApiService;
import com.muniryenigul.kam.utils.LockableNestedScrollView;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    public static boolean tab0selected = false, tab1selected = false, tab2selected = false, tab3selected = false;
    public static String halkQuery, halkmQuery, kitapsecQuery, kitapyurduQuery, bkmQuery, mQuery, kitapsecmQuery;
    private TextView searchViewText;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private CoordinatorLayout coordinator;
    private boolean isKeyboardOpen = false;
    private ProgressBar progressBar;
    public CheckBox selectAll;
    private LockableNestedScrollView nestedScrollView;
    private FavouriteAdapter searchedAdapter;
    private Button update, deleteAllFavs, findOptimumForAll;
    private TextView howManySelected;
    private RecyclerView searchedRecView;
    private RecyclerView horRecView;
    private SuggestionAdapter favAdapter;
    public static ArrayList<SingleItemModel> favSingleItem;
    private ImageView closeButton, shadowImage, shadowImageForContext, coverLoading;
    public static ArrayList<String> favList;
    public static ArrayList<String> arrayForSelectedSites;
    private ArrayList<String> searchedList, contextList;
    private final int REQ_CODE_SPEECH_INPUT = 200;
    public static int search = 1;
    static public int sugPosition, favPosition, howMany = 0;
    private ArrayList<HashMap<String, String>> arrayListInfoFinal;
    public static BroadcastingInnerClass receiver;
    private SearchView searchView;
    public static boolean hasfocus = false, isContext = false, dismiss = false, hasVoiceFocus = false;
    private SQLiteDatabase searched;
    private LinearLayout favLayout, searchedLayout, outer_searchLayout, upLayout;
    private RelativeLayout outer_contextLayout;
    public static SharedPreferences detect;
    private Dialog dialog;
    private DrawerLayout drawer;
    static public ArrayList<String> indexesForUpdate;
    public static ArrayList<String> filteredList;
    @Override
    protected void onPause() { super.onPause();
        hideKeyboard();
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        try {
            /*if (intent.getStringExtra("info") != null && intent.getStringExtra("info").equalsIgnoreCase("couldn't find")) {
                Toast.makeText(this, "couldn't find", Toast.LENGTH_LONG).show();
                filteredList.clear();
                search = 1;
                searchURL = "https://www.kitapyurdu.com/index.php?route=product/search&filter_name=" + mQuery + "&page=" + search;
                currenSitetoSearch = 1;
                sendToFind(mQuery, searchURL);
            } else*/
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
//                    case "Scan":
//                        hasfocus = false;
//                        startActivity(new Intent(MainActivity.this, ScanActivity.class));
//                        break;
                    case "Voice": promptSpeechInput();break;
                    case "QueryTextFocusChange":
                        searchView.requestFocus();
                        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                        //shadowImage.setVisibility(View.VISIBLE);
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
        isKeyboardOpen = true;
        if (!isContext) bringFavs();
        loadArrayList(arrayForSelectedSites, MainActivity.this);
        progressBar.setVisibility(View.GONE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(receiver);
        hideKeyboard();
    }

    @Override
    protected void onStart() {
        super.onStart();
        register(receiver);
    }

    private void register(BroadcastingInnerClass receiver) { registerReceiver(receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)); }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) drawer.closeDrawer(GravityCompat.START);
        else if (isContext) cancelContext();
        else if (coordinator.getVisibility() == View.VISIBLE) {
            coordinator.setVisibility(View.GONE);
            upLayout.setVisibility(View.VISIBLE);
            closeButton.setVisibility(View.GONE);
            searchViewText.setText("");
            shadowImage.setVisibility(View.GONE);
        } else if (searchedLayout.getVisibility() == View.VISIBLE) shadow(searchView);
        else this.moveTaskToBack(true);
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
        contextList = new ArrayList<>();
        detect = getSharedPreferences("com.muniryenigul.kam", MODE_PRIVATE);
        progressBar = findViewById(R.id.progressBar);
        arrayForSelectedSites = new ArrayList<>();
        coverLoading = findViewById(R.id.coverLoading);
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
        longClick(MainActivity.this, findViewById(R.id.voiceButton), "Sesli Arama: Mikrofonu Aç");
        searchView = findViewById(R.id.searchView);
        searchView.setQueryHint("Kitap / Yazar / Yayınevi / ISBN");
        favLayout = findViewById(R.id.favLayout);
        upLayout = findViewById(R.id.upLayout);
        searchedLayout = findViewById(R.id.searchedLayout);
        filteredList = new ArrayList<>();
        searchedRecView = findViewById(R.id.searchedRecView);
        searchedRecView.setHasFixedSize(true);
        searchedRecView.addItemDecoration(new DividerItemDecoration(searchedRecView.getContext(), DividerItemDecoration.VERTICAL));
        searchedRecView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, false));
        searchedRecView.scheduleLayoutAnimation();
        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        final NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        shadowImage = findViewById(R.id.shadowImage);
        shadowImageForContext = findViewById(R.id.shadowImageForContext);
        searchViewText = searchView.findViewById(searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null));
        searchViewText.setTextSize(20);
        searchViewText.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        searchViewText.setTypeface(ResourcesCompat.getFont(getApplicationContext(), R.font.josefinsans_medium));
        closeButton = searchView.findViewById(searchView.getContext().getResources().getIdentifier("android:id/search_close_btn", null, null));
        closeButton.setImageDrawable(ContextCompat.getDrawable(MainActivity.this,R.drawable.cancel));
        closeButton.setOnClickListener(v1 -> {
            //coordinator.setVisibility(View.GONE);
            hideProgressBar(searchView);
            closeButton.setVisibility(View.GONE);
            bringSearched();
            searchView.setQuery("", true);
            //searchView.requestFocus();
            openKeyboard();
        });
        viewPager = findViewById(R.id.viewpager);
        tabLayout = findViewById(R.id.tabs);
        coordinator = findViewById(R.id.coordinator);

        searchViewText.setOnClickListener(v -> {
            isKeyboardOpen = true;
        });

        searchView.setOnQueryTextFocusChangeListener((v12, hasFocus) -> {
            if (hasFocus) {
                hasfocus = hasFocus;
                bringSearched();
                upLayout.setVisibility(View.GONE);
            } else {
                if (drawer.isDrawerOpen(GravityCompat.START)) drawer.closeDrawer(GravityCompat.START);
                else {
                    hideKeyboard();
                    shadowImage.setVisibility(View.GONE);
                    searchedLayout.setVisibility(View.GONE);
                    coordinator.setVisibility(View.GONE);
                    upLayout.setVisibility(View.VISIBLE);
                    searchViewText.setText("");
                }
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                upLayout.setVisibility(View.GONE);
                searchedLayout.setVisibility(View.GONE);
                outer_contextLayout.setVisibility(View.GONE);
                coordinator.setVisibility(View.VISIBLE);

                if (!receiver.isNetworkAvailable(MainActivity.this)) noInternet();
                else if (query.length() == 0) hideProgressBar(searchView);
                else {
                    filteredList.clear();
                    search = 1;
                    mQuery = query;
                    sendToFind();
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
            public boolean onQueryTextChange(String newText) {
                return true; }

        });

        searchedRecView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), searchedRecView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                if(!receiver.isNetworkAvailable(MainActivity.this)) noInternet();
                else searchView.setQuery(searchedList.get(position), true);
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

    @SuppressLint("LongLogTag")
    private void setupTabIcons() {
        Objects.requireNonNull(tabLayout.getTabAt(0)).setCustomView(LayoutInflater.from(this).inflate(R.layout.tab_kitapsec, null));
        Objects.requireNonNull(tabLayout.getTabAt(1)).setCustomView(LayoutInflater.from(this).inflate(R.layout.tab_kitapyurdu, null));
        Objects.requireNonNull(tabLayout.getTabAt(2)).setCustomView(LayoutInflater.from(this).inflate(R.layout.tab_halk, null));
        //Objects.requireNonNull(tabLayout.getTabAt(3)).setCustomView(LayoutInflater.from(this).inflate(R.layout.tab_bkm_unselected, null));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    tab0selected = true; tab1selected = false; tab2selected = false; tab3selected = false;
                }
                else if (tab.getPosition() == 1) {
                    tab0selected = false; tab1selected = true; tab2selected = false; tab3selected = false;
                }
                else if (tab.getPosition() == 2) {
                    tab0selected = false; tab1selected = false; tab2selected = true; tab3selected = false;
                }
                else {
                    tab0selected = false; tab1selected = false; tab2selected = false; tab3selected = true;
                }
                if(Objects.requireNonNull(tabLayout.getTabAt(tab.getPosition())).getCustomView() != null) {
                    Objects.requireNonNull(Objects.requireNonNull(tabLayout.getTabAt(tab.getPosition())).getCustomView()).findViewById(R.id.siteName).setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                Objects.requireNonNull(Objects.requireNonNull(tabLayout.getTabAt(tab.getPosition())).getCustomView()).findViewById(R.id.siteName).setVisibility(View.GONE);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    tab0selected = true; tab1selected = false; tab2selected = false; tab3selected = false;
                }
                else if (tab.getPosition() == 1) {
                    tab0selected = false; tab1selected = true; tab2selected = false; tab3selected = false;
                }
                else if (tab.getPosition() == 2) {
                    tab0selected = false; tab1selected = false; tab2selected = true; tab3selected = false;
                }
                else {
                    tab0selected = false; tab1selected = false; tab2selected = false; tab3selected = true;
                }
                Objects.requireNonNull(Objects.requireNonNull(tabLayout.getTabAt(tab.getPosition())).getCustomView()).findViewById(R.id.siteName).setVisibility(View.VISIBLE);
            }
        });

        Objects.requireNonNull(tabLayout.getTabAt(0)).select();
    }

    private void setupViewPager() {
        halkmQuery = mQuery.replaceAll(" ", "+");
        halkQuery = "https://www.halkkitabevi.com/index.php?p=Products&q_field_active=0&ctg_id=&q=" + halkmQuery + "&search=&q_field=&page=" + search;
        kitapyurduQuery = "https://www.kitapyurdu.com/index.php?route=product/search&filter_name=" + mQuery + "&page=" + search;
        bkmQuery = "https://www.bkmkitap.com/arama?q=" + mQuery + "&customerType=Ziyaretci&pg=" + search;
        kitapsecmQuery = mQuery.replaceAll("ö","%F6").replaceAll("Ö","%D6")
                .replaceAll("ğ","%F0").replaceAll("Ğ","%D0")
                .replaceAll("İ","%DD")
                .replaceAll("ü","%FC").replaceAll("Ü","%DC")
                .replaceAll("ç","%E7").replaceAll("Ç","%C7")
                .replaceAll("ş","%FE").replaceAll("Ş","%DE")
                .replaceAll("ı","%FD").replaceAll(" ","+");
        kitapsecQuery = "https://www.kitapsec.com/Arama/index.php?a=" + kitapsecmQuery + "&AnaKat=&arama=" + search + "-6-0a0-0-0-0-0-0";
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new FragmentKitapsec(searchedLayout), "Kitapseç");
        adapter.addFragment(new FragmentKitapyurdu(searchedLayout), "Kitapyurdu");
        adapter.addFragment(new FragmentHalk(searchedLayout), "Halk Kitabevi");
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void sendToFind() {
        if (searchedLayout.isShown()) searchedLayout.setVisibility(View.GONE);
        hideKeyboard();
        setupViewPager();
        setupTabIcons();
    }

    private void openKeyboard() {
        isKeyboardOpen = true;
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null && inputMethodManager.isActive()) inputMethodManager.toggleSoftInputFromWindow(searchView.getWindowToken(), InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    public void hideKeyboard() {
        isKeyboardOpen = false;
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) inputMethodManager.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
    }

    private void bringSearched() {
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
            searchedAdapter = new FavouriteAdapter(searchedList, "history");
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

    void caseforMore(String categoryName) {
        Intent i = new Intent(MainActivity.this, ShowMoreActivity.class);
        i.putExtra("info", "main");
        i.putExtra("current name", categoryName);
        startActivity(i);
    }

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
        shadowImage.setVisibility(View.GONE);
        upLayout.setVisibility(View.VISIBLE);
        hideKeyboard();
        searchView.clearFocus();
    }

    public void shadowForContext(View view) {
        cancelContext();
        shadowImageForContext.setVisibility(View.GONE);
        nestedScrollView.setScrollingEnabled(true);
    }

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
                (new Retrofit.Builder().addConverterFactory(ScalarsConverterFactory.create())
                        .baseUrl("https://www.halkkitabevi.com/").client(new OkHttpClient().newBuilder().build()).build())
                        .create(ApiService.class).getPrices(arrayList.get(position).get("individual")).enqueue(new Callback<String>() {

                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Document doc = Jsoup.parse(response.body());
                            String src = doc.select("div.__product_fields > div:nth-child(1) > div:nth-child(3)").text();
                            String volume = null;
                            String pages = null;
                            for (Element table : doc.select("div.col2.__col2 > div.__product_fields")) {
                                for (Element row : table.select("div.__product_fields > div")) {
                                    if (row.select("div").text().contains("Sayfa Sayısı")) {
                                        String pages2 = row.select("div").text();
                                        int end = pages2.indexOf("Sayfa Sayısı",1);
                                        pages = pages2.substring(0,end);
                                    } else if (row.select("div").text().contains("Kapak Türü")) {
                                        String volume2 = row.select("div").text();
                                        int end = volume2.indexOf("Kapak Türü",1);
                                        volume = volume2.substring(0,end);
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
                            intent.putExtra("description", StringUtils.replace(sb.toString(),"\n\n","",1));
                            intent.putExtra("coverBig", arrayList.get(position).get("cover"));
                            intent.putExtra("volume", StringUtils.replace(volume,"Kapak Türü : ",""));
                            intent.putExtra("pages", StringUtils.replace(pages,"Sayfa Sayısı : ",""));
                            intent.putExtra("isbn", src);
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
        arrayList.clear();
        SharedPreferences prefs = mContext.getSharedPreferences("preference2", 0);
        int size = prefs.getInt("arrayForSelectedSites" + "_size", 0);
        for (int i = 0; i < size; i++)
            if (!arrayList.contains(prefs.getString("arrayForSelectedSites" + "_" + i, " "))) arrayList.add(prefs.getString("arrayForSelectedSites" + "_" + i, " "));
    }

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
            buttonMyself.setVisibility(View.VISIBLE);
            buttonMyself.setText("Tamam");
            sb.append("Bir site isminde değişiklik olduğu için lütfen favoriler listesini güncelleyiniz.");
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            textView.setText(sb.toString());
            builder.setCancelable(true).setView(alertLayout);
            Dialog d = builder.create();
            d.show();
            buttonMyself.setOnClickListener(v -> {
                d.dismiss();
            });
        } else intent(from);
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
        boolean isSearchFocused = false;
        if (isSearchFocused) searchView.clearFocus();
            else if (!drawer.isDrawerOpen(GravityCompat.START))  {
                    drawer.openDrawer(GravityCompat.START);
                    hideKeyboard();
                }
    }

    public void voice(View view) {
        if (!receiver.isNetworkAvailable(MainActivity.this)) noInternet();
        else promptSpeechInput();
    }

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

    public void hideProgressBar(SearchView searchView) {
        int id = searchView.getContext().getResources().getIdentifier("android:id/search_plate", null, null);
        if (searchView.findViewById(id).findViewById(R.id.progressBar1) != null)
            searchView.findViewById(id).findViewById(R.id.progressBar1).animate().setDuration(200).alpha(0).start();
    }
}