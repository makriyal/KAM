package com.muniryenigul.kam.activities;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.Settings;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import static com.muniryenigul.kam.MainActivity.favList;
import static com.muniryenigul.kam.MainActivity.favSingleItem;
import static com.muniryenigul.kam.activities.ShowMoreActivity.searched;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.snackbar.Snackbar;
import com.muniryenigul.kam.MainActivity;
import com.muniryenigul.kam.interfaces.ApiService;
import com.muniryenigul.kam.models.SingleItemModel;
import com.muniryenigul.kam.services.PriceServiceWithRetrofit;
import com.muniryenigul.kam.R;
import com.muniryenigul.kam.ers.SuggestionAdapter;
import com.muniryenigul.kam.models.HowMuchAndWhere;
import com.squareup.picasso.Picasso;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Locale;
import java.util.Objects;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;
public class PriceActivity extends AppCompatActivity {
    private RelativeLayout sortLayout;
    public static boolean askToUpdate= false;
    private boolean afterDetailSearch=false;
    public static boolean stillSearch=false;
    private String intentInfo, description, name, author, publisher, coverBig, isbn, volume, pages;
    private ArrayList<HowMuchAndWhere> arrayListPrice;
    public static SQLiteDatabase database, fav;
    private SuggestionAdapter adapter;
    private Button buttonFindPrices2;
    int all, position;
    private ProgressBar progressBar2;
    private ImageView coverLoading;
    private FloatingActionButton fab, fabUpdate, fab2, fabUpdate2;
    private ArrayList<String> arrayForSelectedSites;
    private BroadcastingInnerClass receiver;
    private Dialog dialog;
    private TextView textViewVolumeAndPage, textDescription, textViewVolumeAndPage2, textDescription2;
    private BottomSheetBehavior behavior;
    private RelativeLayout linear, linear2;
    private RecyclerView recyclerView;
    private CoordinatorLayout coordinator;
    private SingleItemModel singleItemModel;
    private Snackbar snackbar;
    @Override
    protected void onStart() { super.onStart();
        register(receiver);
    }
    private void register(BroadcastingInnerClass receiver) {
        registerReceiver(receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }
    @Override
    protected void onStop() { super.onStop();
        unregisterReceiver(receiver);
    }
    @Override
    protected void onResume() { super.onResume();
        loadArrayList(arrayForSelectedSites,PriceActivity.this);
        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    public void createIntent(ArrayList<SingleItemModel> singleItem, int position, String info) {
        try { (new Retrofit.Builder().addConverterFactory(ScalarsConverterFactory.create())
                    .baseUrl("https://www.bkmkitap.com/").client(new OkHttpClient().newBuilder().build()).build())
                    .create(ApiService.class).getPrices(singleItem.get(position).getIndividual()).enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    if (response.isSuccessful() && response.body() != null) {
                            Document doc = Jsoup.parse(response.body());
                            Intent intent = new Intent(PriceActivity.this, PriceActivity.class);
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
                        searched = false;
                        finish();
                    }
                }
                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) { /*progressBar.setVisibility(View.GONE);*/ }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bdc);
        receiver = new BroadcastingInnerClass();
        coordinator = findViewById(R.id.coordinator);
        volume = getIntent().getStringExtra("volume");
        pages = getIntent().getStringExtra("pages");
        name = getIntent().getStringExtra("name");
        author = getIntent().getStringExtra("author");
        publisher = getIntent().getStringExtra("publisher");
        coverBig = getIntent().getStringExtra("coverBig");
        isbn = getIntent().getStringExtra("isbn");
        description = getIntent().getStringExtra("description");
        intentInfo = getIntent().getStringExtra("info");
        singleItemModel = new SingleItemModel(name, author, publisher, getIntent().getStringExtra("cover"), getIntent().getStringExtra("individual"), coverBig, isbn, description);
        position = getIntent().getIntExtra("position",0);
        arrayListPrice = new ArrayList<>();
        buttonFindPrices2 = findViewById(R.id.buttonSeeOrSearch2);
        sortLayout = findViewById(R.id.sortLayout);
        textViewVolumeAndPage = findViewById(R.id.textViewVolumeAndPage);
        textViewVolumeAndPage2 = findViewById(R.id.textViewVolumeAndPage2);
        TextView nameText = findViewById(R.id.nameText);
        TextView nameText2 = findViewById(R.id.nameText2);
        textDescription = findViewById(R.id.textDescription);
        textDescription2 = findViewById(R.id.textDescription2);
        arrayForSelectedSites=new ArrayList<>();
        loadArrayList(arrayForSelectedSites,PriceActivity.this);
        all = getResources().getStringArray(R.array.listOptions).length;
        progressBar2 = findViewById(R.id.progressBar2);
        recyclerView = findViewById(R.id.recylerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        recyclerView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(this, R.anim.layout_animation_down_to_up));
        coverLoading = findViewById(R.id.coverLoading);
        ImageView toolbar_image = findViewById(R.id.toolbar_image);
        ImageView toolbar_image2 = findViewById(R.id.toolbar_image2);
        linear = findViewById(R.id.linear);
        linear2 = findViewById(R.id.linear2);
        behavior = BottomSheetBehavior.from(linear2);
        Picasso mPicasso = Picasso.get();
        mPicasso.load(coverBig).error(R.drawable./*error*/ic_virus).into(toolbar_image);
        mPicasso.load(coverBig).error(R.drawable./*error*/ic_virus).into(toolbar_image2);
        nameText.setText(name);
        nameText2.setText(name);
        fab = findViewById(R.id.fab);
        fab2 = findViewById(R.id.fab2);
        fabUpdate = findViewById(R.id.fabUpdate);
        fabUpdate2 = findViewById(R.id.fabUpdate2);
        Button buttonAuthor = findViewById(R.id.buttonAuthor);
        Button buttonAuthor2 = findViewById(R.id.buttonAuthor2);
        Button buttonPublisher = findViewById(R.id.buttonPublisher);
        Button buttonPublisher2 = findViewById(R.id.buttonPublisher2);
        buttonAuthor2.setText(author);
        buttonAuthor.setText(author);
        buttonPublisher2.setText(publisher);
        buttonPublisher.setText(publisher);
        LinearLayout.LayoutParams paramsForAuthorButton = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, author.length());
        paramsForAuthorButton.setMargins(24, 48, 12, 24);
        buttonAuthor2.setLayoutParams(paramsForAuthorButton);
        buttonAuthor.setLayoutParams(paramsForAuthorButton);
        LinearLayout.LayoutParams paramsForPublisherButton = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, publisher.length());
        paramsForPublisherButton.setMargins(12, 48, 24, 24);
        buttonPublisher2.setLayoutParams(paramsForPublisherButton);
        buttonPublisher.setLayoutParams(paramsForPublisherButton);
        fab.setOnLongClickListener(view -> {
            try {
                if (intentInfo.equalsIgnoreCase("old")||intentInfo.equalsIgnoreCase("update")) {
                    PriceActivity.this.deleteDatabase(name);
                    try { deleteDB(); } catch (SQLException e) { catchAndSend("deleteError"); e.printStackTrace();}
                    searched = false;
                    PriceActivity.this.finish();
                } else if (intentInfo.equalsIgnoreCase("detail")||intentInfo.equalsIgnoreCase("new from Sugs")){
                    if(afterDetailSearch ) {
                        if(!favList.contains(name)) saveProcess(null);
                        else Toast.makeText(this, "Bu kitap zaten favoriler listesinde.", Toast.LENGTH_LONG).show();
                    } else return false;
                } else if(!favList.contains(name)) saveProcess(null);
                else Toast.makeText(this, "Bu kitap zaten favoriler listesinde.", Toast.LENGTH_LONG).show();
            } catch (Exception e) { e.printStackTrace(); catchAndSend("delay error"); }
            return true;
        });
        fab2.setOnLongClickListener(view -> {
            try {
                if (intentInfo.equalsIgnoreCase("old")||intentInfo.equalsIgnoreCase("update")) {
                    PriceActivity.this.deleteDatabase(name);
                    try { deleteDB(); } catch (SQLException e) { catchAndSend("deleteError"); e.printStackTrace();}
                    searched = false;
                    PriceActivity.this.finish();
                } else if (intentInfo.equalsIgnoreCase("detail")||intentInfo.equalsIgnoreCase("new from Sugs")){
                    if(afterDetailSearch ) {
                        if(!favList.contains(name)) saveProcess(null);
                        else Toast.makeText(this, "Bu kitap zaten favoriler listesinde.", Toast.LENGTH_LONG).show();
                    } else return false;
                } else if(!favList.contains(name)) saveProcess(null);
                else Toast.makeText(this, "Bu kitap zaten favoriler listesinde.", Toast.LENGTH_LONG).show();
            } catch (Exception e) { e.printStackTrace(); catchAndSend("delay error"); }
            return true;
        });
        fab.setOnClickListener(view -> {
            try {
                if (intentInfo.equalsIgnoreCase("old")||intentInfo.equalsIgnoreCase("update")) {
                    delete();
                } else if (intentInfo.equalsIgnoreCase("detail")||intentInfo.equalsIgnoreCase("new from Sugs")) {
                    if(!afterDetailSearch) {
                        if (arrayForSelectedSites == null || arrayForSelectedSites.isEmpty()) startActivity(new Intent(PriceActivity.this, Settings2Activity.class).putExtra("from", intentInfo));
                        else if(!receiver.isNetworkAvailable(PriceActivity.this)) alert("internet");
                        else if(favList != null && !favList.contains(name)) search(" ");
                        else {
                            try {
                                Snackbar.make(view, "Bu kitap zaten favoriler listesinde.", Snackbar.LENGTH_LONG).setActionTextColor(Color.YELLOW).setAction("Fiyatları Gör", v -> createIntent(favSingleItem, favList.indexOf(name), "old")).show();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } else if(!favList.contains(name)) save();
                    else Toast.makeText(PriceActivity.this, "Bu kitap zaten favoriler listesinde.", Toast.LENGTH_LONG).show();
                } else if(!favList.contains(name)) save();
                else Toast.makeText(PriceActivity.this, "Bu kitap zaten favoriler listesinde.", Toast.LENGTH_LONG).show();
            } catch (Exception e) { e.printStackTrace(); catchAndSend("delay error"); }
        });
        fab2.setOnClickListener(view -> {
            try { if (intentInfo.equalsIgnoreCase("old")||intentInfo.equalsIgnoreCase("update")) delete();
                else if (intentInfo.equalsIgnoreCase("detail")||intentInfo.equalsIgnoreCase("new from Sugs")) {
                    if(!afterDetailSearch) {
                        if (arrayForSelectedSites == null || arrayForSelectedSites.isEmpty()) startActivity(new Intent(PriceActivity.this, Settings2Activity.class).putExtra("from", intentInfo));
                        else if(!receiver.isNetworkAvailable(PriceActivity.this)) alert("internet");
                        else if(!favList.contains(name)) search(" ");
                        else {
                            try {
                                Snackbar.make(view, "Bu kitap zaten favoriler listesinde.", Snackbar.LENGTH_LONG).setActionTextColor(Color.YELLOW).setAction("Fiyatları Gör", v -> createIntent(favSingleItem, favList.indexOf(name), "old")).show();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } else if(!favList.contains(name)) save();
                    else Toast.makeText(PriceActivity.this, "Bu kitap zaten favoriler listesinde.", Toast.LENGTH_LONG).show();
                } else if(!favList.contains(name)) save();
                else Toast.makeText(PriceActivity.this, "Bu kitap zaten favoriler listesinde.", Toast.LENGTH_LONG).show();
            } catch (Exception e) { e.printStackTrace(); catchAndSend("delay error"); }
        });
        fabUpdate.setOnClickListener(v -> {
            @SuppressLint("InflateParams") View alertLayout = getLayoutInflater().inflate(R.layout.custom_click, null);
            final LinearLayout buttons_lay= alertLayout.findViewById(R.id.buttons_lay);
            buttons_lay.setVisibility(View.VISIBLE);
            final Button buttonCancel = alertLayout.findViewById(R.id.buttonDelete);
            final Button buttonUpdate = alertLayout.findViewById(R.id.buttonSeeOrSearch);
            buttonCancel.setVisibility(View.VISIBLE);
            buttonUpdate.setVisibility(View.VISIBLE);
            final TextView textView = alertLayout.findViewById(R.id.textView);
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(PriceActivity.this);
            textView.setText(R.string.sure_to_update);
            buttonCancel.setText(R.string.cancel);
            buttonUpdate.setText(R.string.myself);
            builder.setCancelable(true).setView(alertLayout);
            Dialog dialog = builder.create();
            dialog.show();
            buttonUpdate.setOnClickListener(v2 -> {
                dialog.dismiss();
                if(!receiver.isNetworkAvailable(PriceActivity.this)) alert("internet");
                else search("3");
            });
            buttonCancel.setOnClickListener(v2 -> dialog.dismiss());
        });
        fabUpdate2.setOnClickListener(v -> {
            @SuppressLint("InflateParams") View alertLayout = getLayoutInflater().inflate(R.layout.custom_click, null);
            final LinearLayout buttons_lay= alertLayout.findViewById(R.id.buttons_lay);
            buttons_lay.setVisibility(View.VISIBLE);
            final Button buttonCancel = alertLayout.findViewById(R.id.buttonDelete);
            final Button buttonUpdate = alertLayout.findViewById(R.id.buttonSeeOrSearch);
            buttonCancel.setVisibility(View.VISIBLE);
            buttonUpdate.setVisibility(View.VISIBLE);
            final TextView textView = alertLayout.findViewById(R.id.textView);
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(PriceActivity.this);
            textView.setText(R.string.sure_to_update);
            buttonCancel.setText(R.string.cancel);
            buttonUpdate.setText(R.string.myself);
            builder.setCancelable(true).setView(alertLayout);
            Dialog dialog = builder.create();
            dialog.show();
            buttonUpdate.setOnClickListener(v2 -> {
                dialog.dismiss();
                if(!receiver.isNetworkAvailable(PriceActivity.this)) alert("internet");
                else search("3");
            });
            buttonCancel.setOnClickListener(v2 -> dialog.dismiss());
        });
        if(intentInfo != null) {
            if (intentInfo.equalsIgnoreCase("old")) {
                searched = true;
                commonInCommon();
                bringSearched();
            } else if (intentInfo.equalsIgnoreCase("detail")) {
                common();
                freeMemory();
            } else if (intentInfo.equalsIgnoreCase("new from Sugs")) {
                freeMemory();
                common();
                search(" ");
            } else if (intentInfo.equalsIgnoreCase("update") || intentInfo.equalsIgnoreCase("refresh")) {
                freeMemory();
                if(favList.contains(name)) fab2.setImageDrawable(ContextCompat.getDrawable(PriceActivity.this,R.drawable.deletedocument));
                commonInCommon();
                search("3");
            } else {
                freeMemory();
                commonInCommon();
                search(" ");
            }
        }
    }
    private void freeMemory(){
        System.runFinalization();
        Runtime.getRuntime().gc();
        System.gc();
    }
    private void bringSearched() {
        freeMemory();
        try {
            database = openOrCreateDatabase(StringUtils.replace(name, "/", ""), MODE_PRIVATE, null);
            database.execSQL("CREATE TABLE IF NOT EXISTS data (site VARCHAR, price VARCHAR, url VARCHAR)");
            Cursor cursor = database.rawQuery("SELECT * FROM data", null);
            int siteIx = cursor.getColumnIndex("site");
            int priceIx = cursor.getColumnIndex("price");
            int urlIx = cursor.getColumnIndex("url");
            if (cursor.moveToFirst()) {
                do { arrayListPrice.add(new HowMuchAndWhere(cursor.getString(siteIx), cursor.getString(priceIx), cursor.getString(urlIx))); }
                while (cursor.moveToNext());
            }
            cursor.close();
            database.close();
            ofAdapter();
            fab2.setImageDrawable(ContextCompat.getDrawable(PriceActivity.this,R.drawable.deletedocument));
            fabUpdate2.setVisibility(View.VISIBLE);
            fabUpdate2.show();
            buttonFindPrices2.setVisibility(View.GONE);
            linear.setVisibility(View.GONE);
            linear2.setVisibility(View.VISIBLE);
        } catch (SQLException e) { catchAndSend("databaseError"); e.printStackTrace();}
        freeMemory();
    }
    private void common() {
        fab.setImageDrawable(ContextCompat.getDrawable(PriceActivity.this,R.drawable.search));
        sortLayout.setVisibility(View.GONE);
        fabUpdate.hide();
        commonInCommon();
    }
    private void commonInCommon() {
        try {
            StringBuilder sb = new StringBuilder();
            if(volume != null) {
                if(!volume.isEmpty()) {
                    if(pages != null) {
                        if(!pages.isEmpty()) sb.append(volume).append(", ").append(pages).append(" Sayfa");
                        else sb.append(volume);
                    } else sb.append(volume);
                } else {
                    if(pages != null) {
                        if(!pages.isEmpty()) sb.append(pages).append(" Sayfa");
                        else sb.append("GONE");
                    } else sb.append("GONE");
                }
            } else {
                if(pages != null) {
                    if(!pages.isEmpty()) sb.append(pages).append(" Sayfa");
                    else sb.append("GONE");
                }else sb.append("GONE");
            }
            if(sb.toString().contains("GONE")) {
                textViewVolumeAndPage.setVisibility(View.GONE);
                textViewVolumeAndPage2.setVisibility(View.GONE);
            } else {
                textViewVolumeAndPage.setVisibility(View.VISIBLE);
                textViewVolumeAndPage2.setVisibility(View.VISIBLE);
                textViewVolumeAndPage.setText(sb.toString());
                textViewVolumeAndPage2.setText(sb.toString());
            }
            if(description.isEmpty()) {
                textDescription.setVisibility(View.GONE);
                textDescription2.setVisibility(View.GONE);
                (findViewById(R.id.view)).setVisibility(View.GONE);
                (findViewById(R.id.view22)).setVisibility(View.GONE);
            } else if(description.contains("*")) {
                textDescription.setText(StringUtils.replace(description,"\\*",""));
                textDescription2.setText(StringUtils.replace(description,"\\*",""));
            } else {
                textDescription.setText(description);
                textDescription2.setText(description);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void delete() {
        @SuppressLint("InflateParams") View alertLayout = getLayoutInflater().inflate(R.layout.custom_click, null);
        final LinearLayout buttons_lay= alertLayout.findViewById(R.id.buttons_lay);
        buttons_lay.setVisibility(View.VISIBLE);
        final Button buttonCancel = alertLayout.findViewById(R.id.buttonDelete);
        final Button buttonDelete = alertLayout.findViewById(R.id.buttonSeeOrSearch);
        buttonCancel.setVisibility(View.VISIBLE);
        buttonDelete.setVisibility(View.VISIBLE);
        final TextView textView = alertLayout.findViewById(R.id.textView);
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(PriceActivity.this);
        textView.setText(R.string.sure_to_delete);
        buttonCancel.setText(R.string.cancel);
        buttonDelete.setText(R.string.delete);
        builder.setCancelable(true).setView(alertLayout);
        Dialog dialog = builder.create();
        dialog.show();
        buttonDelete.setOnClickListener(v -> {
            dialog.dismiss();
            PriceActivity.this.deleteDatabase(name);
            try { deleteDB(); } catch (SQLException e) { catchAndSend("deleteError"); e.printStackTrace();}
            searched = false;
            PriceActivity.this.finish();
        });
        buttonCancel.setOnClickListener(v -> dialog.dismiss());
    }
    private void saveDB(String nameDB, String table, String name2, String author2, String publisher2, String cover, String individual, String coverBig2, String isbn2, String description2) {
        SQLiteDatabase database = openOrCreateDatabase(nameDB, MODE_PRIVATE, null);
        SQLiteStatement statement;
        if(table.equals("info")) {
            database.execSQL(StringUtils.join("CREATE TABLE IF NOT EXISTS ",table," (",name2," VARCHAR, ",author2," VARCHAR, ",publisher2," VARCHAR, ",cover," VARCHAR, ",individual," VARCHAR, ",coverBig2," VARCHAR, ",isbn2," VARCHAR, ",description2," VARCHAR)"));
            statement = database.compileStatement(StringUtils.join("INSERT INTO ",table," (",name2,", ",author2,", ",publisher2,", ",cover,", ",individual,", ",coverBig2,", ",isbn2,", ",description2,")"," VALUES (?, ?, ?, ?, ?, ?, ?, ?)"));
            statement.bindString(1, name);
            statement.bindString(2, author);
            statement.bindString(3, publisher);
            statement.bindString(4, getIntent().getStringExtra("cover"));
            statement.bindString(5, getIntent().getStringExtra("individual"));
            statement.bindString(6, coverBig);
            statement.bindString(7, isbn/*.replaceAll("\\D", "")*/);
            statement.bindString(8, description);
            statement.execute();
        }
        else if(table.equals("fav")) {
            database.execSQL(StringUtils.join("CREATE TABLE IF NOT EXISTS ",table," (name VARCHAR)"));
            statement = database.compileStatement(StringUtils.join("INSERT INTO ",table," (",name2,")"," VALUES (?)"));
            statement.bindString(1, name);
            statement.execute();
        }
        else {
            for (int z = 0; z < arrayListPrice.size(); z++) {
                database.execSQL(StringUtils.join("CREATE TABLE IF NOT EXISTS ",table," (",name2," VARCHAR, ",author2," VARCHAR, ",publisher2," VARCHAR)"));
                statement = database.compileStatement(StringUtils.join("INSERT INTO ",table," (",name2,", ",author2,", ",publisher2,")"," VALUES (?, ?, ?)"));
                statement.bindString(1, arrayListPrice.get(z).getSite());
                statement.bindString(2, arrayListPrice.get(z).getPrice());
                statement.bindString(3, arrayListPrice.get(z).getURL());
                statement.execute();
            }
        }
        database.close();
    }
    private void deleteDB() {
        fav = openOrCreateDatabase("Favs", MODE_PRIVATE, null);
        fav.execSQL("CREATE TABLE IF NOT EXISTS fav (name VARCHAR)");
        SQLiteStatement statement = fav.compileStatement("DELETE FROM fav WHERE name LIKE (?)");
        statement.bindString(1, name);
        statement.execute();
        fav.close();
    }
    private void catchAndSend(String error) {
        Intent intent=new Intent(PriceActivity.this, MainActivity.class);
        intent.putExtra("info", error);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        searched = false;
        PriceActivity.this.finish();
    }
    private void save() {
        @SuppressLint("InflateParams") View alertLayout = getLayoutInflater().inflate(R.layout.custom_click, null);
        final LinearLayout buttons_lay= alertLayout.findViewById(R.id.buttons_lay);
        buttons_lay.setVisibility(View.VISIBLE);
        final Button buttonCancel = alertLayout.findViewById(R.id.buttonDelete);
        final Button buttonFav = alertLayout.findViewById(R.id.buttonSeeOrSearch);
        buttonCancel.setVisibility(View.VISIBLE);
        buttonFav.setVisibility(View.VISIBLE);
        final TextView textView = alertLayout.findViewById(R.id.textView);
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(PriceActivity.this);
        textView.setText(getString(R.string.addFav));
        buttonCancel.setText(getString(R.string.cancel));
        buttonFav.setText(getString(R.string.add));
        builder.setCancelable(true).setView(alertLayout);
        Dialog dialog = builder.create();
        dialog.show();
        buttonCancel.setOnClickListener(v -> dialog.dismiss());
        buttonFav.setOnClickListener(v -> saveProcess(dialog));
    }
    public void addToFavs(View view) {
        if(!favList.contains(name)) save();
        else Toast.makeText(PriceActivity.this, "Bu kitap zaten favoriler listesinde.", Toast.LENGTH_LONG).show();
    }
    private void saveProcess(Dialog dialog) {
        progressBar2.setVisibility(View.VISIBLE);
        coordinator.setEnabled(false);
        if(dialog!=null) dialog.dismiss();
        try { saveDB(StringUtils.replace(name, "/", ""),"info","name","author","publisher","cover","individual","coverBig","isbn","description");
            saveDB("Favs","fav","name",null,null,null,null,null,null,null);
        } catch (Exception e) { catchAndSend("saveError"); e.printStackTrace();}
        try { sortNumeric();
            saveDB(StringUtils.replace(name, "/", ""),"data","site","price","url",null,null,null,null,null);
        } catch (Exception e) { catchAndSend("saveError"); e.printStackTrace();}
        searched = false;
        PriceActivity.this.finish();
    }
    public void alert(String which) {
        @SuppressLint("InflateParams") View alertLayout = getLayoutInflater().inflate(R.layout.custom_click, null);
        final LinearLayout buttons_lay= alertLayout.findViewById(R.id.buttons_lay);
        buttons_lay.setVisibility(View.VISIBLE);
        final Button buttonConnect = alertLayout.findViewById(R.id.buttonSeeOrSearch);
        final Button buttonCancel = alertLayout.findViewById(R.id.buttonDelete);
        buttonConnect.setVisibility(View.VISIBLE);
        final TextView textView = alertLayout.findViewById(R.id.textView);
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(PriceActivity.this);
        textView.setText(R.string.no_internet);
        if(which.equals("internet")) buttonConnect.setText(R.string.connect);
        else { buttonCancel.setVisibility(View.VISIBLE);
            buttonCancel.setText(R.string.dontUpdate);
            buttonConnect.setText(R.string.myself);
            textView.setText(R.string.sure_to_update);
        }
        builder.setCancelable(true).setView(alertLayout);
        dialog = builder.create();
        dialog.show();
        buttonConnect.setOnClickListener(v -> {
            dialog.dismiss();
            if(which.equals("internet")) startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
            else { progressBar2.setVisibility(View.VISIBLE);
                updateDatabase();
                askToUpdate = false;
                searched = false;
                finish();
            }
        });
        buttonCancel.setOnClickListener(v -> {
            askToUpdate = false;
            dialog.dismiss();
            searched = false;
            finish();
        });
    }
    public void more(View view) {
        if(!receiver.isNetworkAvailable(PriceActivity.this)) alert("internet");
        else {
            Intent i = new Intent(PriceActivity.this, ShowMoreActivity.class);
            i.putExtra("info",/*"price"*/intentInfo);
            switch (view.getTag().toString()) {
                case "author":
                    i.putExtra("current name", author);
                    i.putExtra("current link", "https://www.bkmkitap.com/arama?q="+author);
                    break;
                case "publisher":
                    i.putExtra("current name", publisher);
                    i.putExtra("current link", "https://www.bkmkitap.com/arama?q="+publisher);
                    break;
            }
            i.putExtra("book name", name);
            startActivity(i);
        }

    }
    public void findPrices(View view) {
        try {
            if (arrayForSelectedSites == null || arrayForSelectedSites.isEmpty()) startActivity(new Intent(PriceActivity.this, Settings2Activity.class).putExtra("from", intentInfo));
            else if(!receiver.isNetworkAvailable(PriceActivity.this)) alert("internet");
            else { fab.setVisibility(View.GONE);fab.hide();
                fabUpdate.setVisibility(View.GONE);fabUpdate.hide();
                if(!favList.contains(name)) search(" ");
                else {
                    try {
                        Snackbar.make(view, "Bu kitap zaten favoriler listesinde.", Snackbar.LENGTH_LONG).setActionTextColor(Color.YELLOW).setAction("Fiyatları Gör", view1 -> createIntent(favSingleItem, favList.indexOf(name), "old")).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            buttonFindPrices2.setVisibility(View.GONE);
            e.printStackTrace();
        }

    }
    private void loadArrayList(ArrayList<String> arrayList, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences("preference2", 0);
        int size = prefs.getInt("arrayForSelectedSites" +"_size", 0);
        arrayList.clear();
        for(int i=0;i<size;i++) arrayList.add(prefs.getString("arrayForSelectedSites" + "_" + i," "));
    }
    private void search(String s) {
        coverLoading.setVisibility(View.VISIBLE);
        stillSearch=true;
        freeMemory();
        String isbnFinal = StringUtils.strip(isbn);
        int length = isbnFinal.length();
        if(length>13) isbn = StringUtils.substring(isbnFinal,length-13);
        else isbn = isbnFinal;
        try {
            behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            try {
                snackbar = Snackbar.make(linear2, "Lütfen bekleyiniz...", Snackbar.LENGTH_INDEFINITE);
                snackbar.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(intentInfo.equals("detail")||intentInfo.equals("new from Sugs")) {
                afterDetailSearch=true;
                fab2.setImageDrawable(ContextCompat.getDrawable(PriceActivity.this,R.drawable.addtofavorites));
                (findViewById(R.id.view222)).setVisibility(View.GONE);
            }
            searched = true;
            linear.setVisibility(View.GONE);
            sortLayout.setVisibility(View.GONE);
            linear2.setVisibility(View.GONE);
            Intent intent = new Intent(PriceActivity.this, PriceServiceWithRetrofit.class);
            ResultReceiver myResultReceiver = new MyResultReceiver(null);
            intent.putExtra("from","PA");
            intent.putExtra("receiver", myResultReceiver);
            intent.putExtra("name", name);
            intent.putExtra("author", author);
            intent.putExtra("publisher", publisher);
            intent.putExtra("isbn", isbn);
            intent.putExtra("description", description);
            intent.putExtra("code", s);
            intent.putStringArrayListExtra("selections", arrayForSelectedSites);
            startService(intent);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            Toast.makeText(this, "Lütfen tekrar deneyiniz.", Toast.LENGTH_SHORT).show();
        }
        freeMemory();
    }
    public void expand(View view) { behavior.setState(BottomSheetBehavior.STATE_EXPANDED); }
    public class BroadcastingInnerClass extends BroadcastReceiver {
        boolean connected = false;
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
                        }
                        return true;
                    }
                }
            }
            connected = false;
            return false;
        }
    }
    @Override
    public void onBackPressed() {
        if (behavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
        else {
            if(askToUpdate) {
                if (intentInfo!=null&&intentInfo.equals("old") ||intentInfo!=null&&intentInfo.equals("update")) alert("askToUpdate");
                else {
                    askToUpdate = false;
                    stillSearch = false;
                    searched = false;
                    finish();
                }
            } else {
                stillSearch = false;
                searched = false;
                finish();
            }
        }
    }
        public class MyResultReceiver extends ResultReceiver {
        Handler handler = new Handler();
        MyResultReceiver(Handler handler) {
            super(handler);
        }
        @Override
        protected void onReceiveResult(int resultCode, final Bundle resultData) {
            if (resultCode == 1 && resultData != null) {
                handler.post(() -> {

                    try {
                        arrayListPrice = resultData.getParcelableArrayList("arrayListPrice");
                        sortNumeric();
                    }
                    catch (ConcurrentModificationException ignored) {
                        ignored.printStackTrace();
                    }
                    if (resultData.getString("code") != null && Objects.requireNonNull(resultData.getString("code")).equalsIgnoreCase("3")
                            && (intentInfo!=null&&intentInfo.equals("old") || intentInfo != null && intentInfo.equals("update") || intentInfo != null && intentInfo.equals("refresh") && favList != null && favList.contains(name))) updateDatabase();
                });
            } else if (resultCode == 2 && resultData != null) {
                sortLayout.setVisibility(View.VISIBLE);
                linear2.setVisibility(View.VISIBLE);
                handler.post(PriceActivity.this::situations);
            }
            super.onReceiveResult(resultCode, resultData);
        }
    }

    private void updateDatabase() {
            PriceActivity.this.deleteDatabase(name);
            try { deleteDB(); } catch (SQLException e) { catchAndSend("updateError"); e.printStackTrace();}
            try { saveDB(StringUtils.replace(name, "/", ""),"info","name","author","publisher","cover","individual","coverBig","isbn","description");
                saveDB("Favs","fav","name",null,null,null,null,null,null,null);
            } catch (Exception e) { e.printStackTrace(); }
            try {
                sortNumeric();
                saveDB(StringUtils.replace(name, "/", ""),"data","site","price","url",null,null,null,null,null);
            } catch (Exception e) { e.printStackTrace(); }
    }
private void situations() {
    int notFound = 0, error = 0, unselect = 0, localAll = 0;
    localAll = arrayListPrice.size();
    for(int r = 0 ; r < localAll ; r++ ){
        switch (arrayListPrice.get(r).getPrice()) {
            case "¯\\_(ツ)_/¯": notFound++; break;
            case "ಠ_ಠ": error++; break;
            case "□": unselect++; break;
        }
    }
    if(stillSearch) {
        String message;
        if(localAll == notFound + error) {
            if(arrayForSelectedSites.size() == 1) message = "Seçilen sitede fiyat bulunamadı; stokta yok, satış dışı ya da temin edilemiyor :(";
            else message = "Seçilen sitelerde fiyat bulunamadı; stokta yok, satış dışı ya da temin edilemiyor :(";
        }
        else {
            int result = localAll -notFound - error;
            message = +result+" sitede fiyat bulundu :)";
        }
        try {
            snackbar = Snackbar.make(linear2, message, Snackbar.LENGTH_LONG);
            snackbar.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
    public void ofAdapter() {
        try {
            adapter = new SuggestionAdapter(PriceActivity.this, arrayListPrice, arrayForSelectedSites, singleItemModel, recyclerView, volume, pages,"price");
            recyclerView.setAdapter(adapter);
            recyclerView.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void sortNumeric() {
        try {
            Collections.sort(arrayListPrice, new Comparator<HowMuchAndWhere>() {
                @Override
                public int compare(HowMuchAndWhere o1, HowMuchAndWhere o2) { return extractInt(o1.getPrice().toLowerCase(new Locale("tr", "TR"))) - extractInt(o2.getPrice().toLowerCase(new Locale("tr", "TR"))); }
                int extractInt(String s) {
                    if (s.isEmpty()) s = "9999999";
                    else if (s.equals("ಠ_ಠ")) s = "99999999";
                    else if (s.equals("□")) s = "999999999";
                    else s = s.replace("¯\\_(ツ)_/¯", "999999").replaceAll("\\D", "");
                    return s.isEmpty() ? 0 : Integer.parseInt(s);
                }
            });
            ofAdapter();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void sortNumeric2() {
        Collections.sort(arrayListPrice, new Comparator<HowMuchAndWhere>() {
            @Override
            public int compare(HowMuchAndWhere o2, HowMuchAndWhere o1) { return extractInt(o1.getPrice()) - extractInt(o2.getPrice()); }
            int extractInt(String s) {
                if (s.isEmpty()) s = "9999999";
                else if (s.equals("ಠ_ಠ")) s = "99999999";
                else if (s.equals("□")) s = "999999999";
                else s = s.replace("¯\\_(ツ)_/¯", "999999").replaceAll("\\D", "");
                return s.isEmpty() ? 0 : Integer.parseInt(s);
            }
        });
        int q = arrayListPrice.size();
        for (int l = 0; l < q; l++) {
            if (arrayListPrice.get(0).getPrice().equals("¯\\_(ツ)_/¯") || arrayListPrice.get(0).getPrice().equals("ಠ_ಠ") || arrayListPrice.get(0).getPrice().equals("□")) {
                HowMuchAndWhere h=arrayListPrice.get(0);
                arrayListPrice.add(h);
                arrayListPrice.remove(0);
            }
        }
        synchronized(PriceActivity.this) {
            recyclerView.getRecycledViewPool().clear();
            adapter.notifyDataSetChanged();
        }
    }
    public void sortAlpha2(View view) {
        Collections.sort(arrayListPrice, (o1, o2) -> {
            Collator collator = Collator.getInstance(new Locale("tr", "TR"));
            return collator.compare(StringUtils.replace(o2.getSite().toLowerCase(new Locale("tr", "TR")), " ", ""),
                    StringUtils.replace(o1.getSite().toLowerCase(new Locale("tr", "TR")), " ", ""));
        });
        synchronized(PriceActivity.this){
            recyclerView.getRecycledViewPool().clear();
            adapter.notifyDataSetChanged();
        }
    }
    public void sortAlpha(View view) {
        Collections.sort(arrayListPrice, (o1, o2) -> {
            Collator collator = Collator.getInstance(new Locale("tr", "TR"));
            return collator.compare(StringUtils.replace(o1.getSite().toLowerCase(new Locale("tr", "TR")), " ", ""),
                    StringUtils.replace(o2.getSite().toLowerCase(new Locale("tr", "TR")), " ", ""));
        });
        synchronized(PriceActivity.this){
            recyclerView.getRecycledViewPool().clear();
            adapter.notifyDataSetChanged();
        }
    }
    public void sortNumer(View view) {sortNumeric(); }
    public void sortNumer2(View view) {sortNumeric2(); }
}