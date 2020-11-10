package com.muniryenigul.kam.ers;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.snackbar.Snackbar;
import com.muniryenigul.kam.models.CategoryModel;
import com.muniryenigul.kam.utils.LockableNestedScrollView;
import com.muniryenigul.kam.R;
import com.muniryenigul.kam.activities.PriceActivity;
import com.muniryenigul.kam.activities.Settings2Activity;
import com.muniryenigul.kam.models.HowMuchAndWhere;
import com.muniryenigul.kam.models.SingleItemModel;
import com.muniryenigul.kam.interfaces.ApiService;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import static android.content.Context.MODE_PRIVATE;
import static com.muniryenigul.kam.MainActivity.dismiss;
import static com.muniryenigul.kam.MainActivity.favList;
import static com.muniryenigul.kam.MainActivity.favSingleItem;
import static com.muniryenigul.kam.MainActivity.howMany;
import static com.muniryenigul.kam.MainActivity.isContext;
import static com.muniryenigul.kam.MainActivity.sugPosition;
import static com.muniryenigul.kam.activities.PriceActivity.database;
import static com.muniryenigul.kam.activities.PriceActivity.fav;
import static com.muniryenigul.kam.activities.PriceActivity.askToUpdate;
import static com.muniryenigul.kam.MainActivity.favPosition;
import static com.muniryenigul.kam.MainActivity.indexesForUpdate;
public class SuggestionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private CheckBox selectAll;
    public ArrayList<SingleItemModel> feedItemList;
    private ArrayList<HowMuchAndWhere> arrayListComparison;
    private SingleItemModel singleItemModel;
    private Context context;
    private ArrayList<HashMap<String, String>> arrayListInfoFinal;
    private final int TYPE_ITEM = 0;
    private BroadcastingInnerClass receiver;
    private Dialog dialog;
    private ImageView coverLoading;
    private ArrayList<String> arrayForSelectedSites;
    private TextView howManySelected;
    private RelativeLayout outer_contextLayout;
    private LinearLayout outer_searchLayout, favLayout;
    private ImageView shadowImageForContext;
    private LockableNestedScrollView nestedScrollView;
    private Button update, deleteAllFavs, findOptimumForAll;
    public ArrayList<String> contextList;
    private RecyclerView recyclerView;
    private String from, volume, pages;
    private ArrayList<CategoryModel> categoryList;
    public class BroadcastingInnerClass extends BroadcastReceiver {
        boolean connected = false;
        @Override
        public void onReceive(Context context, Intent intent) {
            isNetworkAvailable(context);
        }
        public boolean isNetworkAvailable(Context context) {
            ConnectivityManager connectivity = (ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE);
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
    public SuggestionAdapter(Context context, ArrayList<HashMap<String, String>> arrayListInfoFinal, String type, ImageView coverLoading, ArrayList<String> arrayForSelectedSites) {
        this.arrayListInfoFinal = arrayListInfoFinal;
        this.context = context;
        this.coverLoading = coverLoading;
        this.arrayForSelectedSites = arrayForSelectedSites;
        receiver = new BroadcastingInnerClass();
        context.registerReceiver(receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }
    public SuggestionAdapter(Context context, ArrayList<HowMuchAndWhere> arrayListComparison) {
        this.arrayListComparison = arrayListComparison;
        this.context = context;
    }
    public SuggestionAdapter(Context context, ArrayList<CategoryModel> categoryList, String from) {
        this.categoryList = categoryList;
        this.from = from;
        this.context = context;
    }
    public SuggestionAdapter(Context context, ArrayList<HowMuchAndWhere> arrayListComparison, ArrayList<String> arrayForSelectedSites, SingleItemModel singleItemModel, RecyclerView recyclerView, String volume, String pages, String from) {
        this.arrayListComparison = arrayListComparison;
        this.arrayForSelectedSites = arrayForSelectedSites;
        this.singleItemModel = singleItemModel;
        this.recyclerView = recyclerView;
        this.context = context;
        this.from = from;
        this.volume = volume;
        this.pages = pages;
    }
    public SuggestionAdapter(Context context, CheckBox selectAll, ArrayList<SingleItemModel> feedItemList, ArrayList<String> contextList, TextView howManySelected, RelativeLayout outer_contextLayout, LinearLayout outer_searchLayout, ImageView shadowImageForContext, LockableNestedScrollView nestedScrollView, Button update, LinearLayout favLayout, ImageView coverLoading, Button deleteAllFavs, Button findOptimumForAll) {
        this.feedItemList = feedItemList;
        this.selectAll = selectAll;
        this.context = context;
        this.contextList = contextList;
        this.howManySelected = howManySelected;
        this.outer_contextLayout = outer_contextLayout;
        this.outer_searchLayout = outer_searchLayout;
        this.shadowImageForContext = shadowImageForContext;
        this.nestedScrollView = nestedScrollView;
        this.update = update;
        this.deleteAllFavs = deleteAllFavs;
        this.findOptimumForAll = findOptimumForAll;
        this.favLayout = favLayout;
        this.coverLoading = coverLoading;
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        try {
            if (arrayListComparison != null && from == null) return new CustomViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.comparison, null));
            else if (feedItemList != null && feedItemList.equals(favSingleItem)) return new CustomViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item, null));
            else if(from != null && from.equals("price")) return new CustomViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_sites_and_prices, null));
            else if (from != null && from.equals("main")) {
                if(i == 0 && favSingleItem.size() > 0) return new CustomViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item, null));
                else return new CustomViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.category, viewGroup,false));
            } else if (i == TYPE_ITEM) return new CustomViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_sugs, null));
            else return new LoadHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.progress_item, null));
        } catch (Exception e) {
            e.printStackTrace();
            return new LoadHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.progress_item, null));
        }
    }
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder customViewHolder, int i) {
        try {
            if(arrayListComparison != null && from == null) {
                Log.d("onBindViewHolder","1");
                if(arrayListComparison.get(i).getHowMuch()==null) {
                    ((CustomViewHolder) customViewHolder).bestSiteItem.setVisibility(View.GONE);
                    double color;
                    double min=0;
                    double max=arrayListComparison.size();
                    color=100*(i-min)/(max-min);
                    double r=(175 * color) / 100;
                    double g=(175 * (100 - color)) / 100;
                    double b=0;
                    ((CustomViewHolder) customViewHolder).textViewSite.setTextColor(Color.rgb((int)r,(int)g,(int)b));
                    ((CustomViewHolder) customViewHolder).textViewPrice.setTextColor(Color.rgb((int)r,(int)g,(int)b));
                    ((CustomViewHolder) customViewHolder).textViewCargo.setTextColor(Color.rgb((int)r,(int)g,(int)b));
                    ((CustomViewHolder) customViewHolder).textViewTotalPrice.setTextColor(Color.rgb((int)r,(int)g,(int)b));
                    ((CustomViewHolder) customViewHolder).textViewPayment.setTextColor(Color.rgb((int)r,(int)g,(int)b));
                    ((CustomViewHolder) customViewHolder).textViewCreditCard.setTextColor(Color.rgb((int)r,(int)g,(int)b));
                    ((CustomViewHolder) customViewHolder).textViewTransfer.setTextColor(Color.rgb((int)r,(int)g,(int)b));
                    ((CustomViewHolder) customViewHolder).textViewPaypal.setTextColor(Color.rgb((int)r,(int)g,(int)b));
                    ((CustomViewHolder) customViewHolder).textViewPayCash.setTextColor(Color.rgb((int)r,(int)g,(int)b));
                    ((CustomViewHolder) customViewHolder).textViewPayCard.setTextColor(Color.rgb((int)r,(int)g,(int)b));
                    ((CustomViewHolder) customViewHolder).textViewMore.setTextColor(Color.rgb((int)r,(int)g,(int)b));
                    ((CustomViewHolder) customViewHolder).textViewInfo.setTextColor(Color.rgb((int)r,(int)g,(int)b));
                } else {
                    ((CustomViewHolder) customViewHolder).bestSiteItem.setVisibility(View.VISIBLE);
                    ((CustomViewHolder) customViewHolder).bestSiteItem.setText(arrayListComparison.get(i).getHowMuch());
                }
                if(arrayListComparison.get(i).getSite()==null) ((CustomViewHolder) customViewHolder).textViewSite.setVisibility(View.GONE); else {
                    ((CustomViewHolder) customViewHolder).textViewSite.setVisibility(View.VISIBLE);
                    ((CustomViewHolder) customViewHolder).textViewSite.setText(arrayListComparison.get(i).getSite());
                }
                if(arrayListComparison.get(i).getPrice()==null) ((CustomViewHolder) customViewHolder).textViewPrice.setVisibility(View.GONE); else {
                    ((CustomViewHolder) customViewHolder).textViewPrice.setVisibility(View.VISIBLE);
                    ((CustomViewHolder) customViewHolder).textViewPrice.setText(arrayListComparison.get(i).getPrice());
                }
                if(arrayListComparison.get(i).getCargo()==null) ((CustomViewHolder) customViewHolder).textViewCargo.setVisibility(View.GONE); else {
                    ((CustomViewHolder) customViewHolder).textViewCargo.setVisibility(View.VISIBLE);
                    if(arrayListComparison.get(i).isFree()) ((CustomViewHolder) customViewHolder).textViewCargo.setCompoundDrawablesWithIntrinsicBounds(R.drawable.free_shipping,0,0,0);
                    else ((CustomViewHolder) customViewHolder).textViewCargo.setCompoundDrawablesWithIntrinsicBounds(R.drawable.truck,0,0,0);
                    String cargoText = arrayListComparison.get(i).getCargo();
                    if (cargoText.contains("Ücretsiz") && arrayListComparison.get(i).getCargo().contains("Hızlı")) ((CustomViewHolder) customViewHolder).textViewCargo.setText(StringUtils.replaceOnce(cargoText," || Hızlı Teslimat",""));
                    else if (cargoText.contains("Ücretsiz") && arrayListComparison.get(i).getCargo().contains("Express")) ((CustomViewHolder) customViewHolder).textViewCargo.setText(StringUtils.replaceOnce(cargoText," || Express Teslimat",""));
                    else ((CustomViewHolder) customViewHolder).textViewCargo.setText(cargoText);
                }
                if(arrayListComparison.get(i).getTotalPrice()==null) ((CustomViewHolder) customViewHolder).textViewTotalPrice.setVisibility(View.GONE); else {
                    ((CustomViewHolder) customViewHolder).textViewTotalPrice.setVisibility(View.VISIBLE);
                    ((CustomViewHolder) customViewHolder).textViewTotalPrice.setText(StringUtils.join("Toplam : ",arrayListComparison.get(i).getTotalPrice()));
                }
                ((CustomViewHolder) customViewHolder).textViewPayment.setText(R.string.payment_methods);
                if(!arrayListComparison.get(i).isHasCard()) ((CustomViewHolder) customViewHolder).textViewCreditCard.setVisibility(View.GONE); else {
                    ((CustomViewHolder) customViewHolder).textViewCreditCard.setVisibility(View.VISIBLE);
                    ((CustomViewHolder) customViewHolder).textViewCreditCard.setText(StringUtils.join(context.getResources().getString(R.string.credit_card)," / ",context.getResources().getString(R.string.bank_card)));
                }
                if(!arrayListComparison.get(i).isHasTransfer()) ((CustomViewHolder) customViewHolder).textViewTransfer.setVisibility(View.GONE); else {
                    ((CustomViewHolder) customViewHolder).textViewTransfer.setVisibility(View.VISIBLE);
                    ((CustomViewHolder) customViewHolder).textViewTransfer.setText(StringUtils.join(context.getResources().getString(R.string.transfer)," / ",context.getResources().getString(R.string.eft)));
                }
                if(!arrayListComparison.get(i).isHasPayPal()) ((CustomViewHolder) customViewHolder).textViewPaypal.setVisibility(View.GONE); else {
                    ((CustomViewHolder) customViewHolder).textViewPaypal.setVisibility(View.VISIBLE);
                    ((CustomViewHolder) customViewHolder).textViewPaypal.setText(context.getResources().getString(R.string.payPal));
                }
                if(arrayListComparison.get(i).getPayAtTheDoorCash()==null) ((CustomViewHolder) customViewHolder).textViewPayCash.setVisibility(View.GONE); else {
                    ((CustomViewHolder) customViewHolder).textViewPayCash.setVisibility(View.VISIBLE);
                    ((CustomViewHolder) customViewHolder).textViewPayCash.setText(arrayListComparison.get(i).getPayAtTheDoorCash());
                }
                if(arrayListComparison.get(i).getPayAtTheDoorCard()==null) ((CustomViewHolder) customViewHolder).textViewPayCard.setVisibility(View.GONE); else {
                    ((CustomViewHolder) customViewHolder).textViewPayCard.setVisibility(View.VISIBLE);
                    ((CustomViewHolder) customViewHolder).textViewPayCard.setText(arrayListComparison.get(i).getPayAtTheDoorCard());
                }
                if(arrayListComparison.get(i).getOtherMethods()==null || arrayListComparison.get(i).getOtherMethods().equals("")|| arrayListComparison.get(i).getOtherMethods().equals("null")) ((CustomViewHolder) customViewHolder).textViewMore.setVisibility(View.GONE); else {
                    ((CustomViewHolder) customViewHolder).textViewMore.setVisibility(View.VISIBLE);
                    ((CustomViewHolder) customViewHolder).textViewMore.setText(arrayListComparison.get(i).getOtherMethods());
                }
                if(arrayListComparison.get(i).getInfo()==null || arrayListComparison.get(i).getInfo().equals("null")|| arrayListComparison.get(i).getInfo().equals("")) ((CustomViewHolder) customViewHolder).textViewInfo.setVisibility(View.GONE); else {
                    ((CustomViewHolder) customViewHolder).textViewInfo.setVisibility(View.VISIBLE);
                    ((CustomViewHolder) customViewHolder).textViewInfo.setText(arrayListComparison.get(i).getInfo());
                }
            } else if(feedItemList!=null) {
                ArrayList<HowMuchAndWhere> arrayListPrice = new ArrayList<>();
                Picasso.get().load(feedItemList.get(i).getCover()).error(R.drawable./*error*/ic_virus).resize(/*133,200*/160, 240).into(((CustomViewHolder) customViewHolder).imageView, new Callback() {
                            @Override
                            public void onSuccess() {
                                ((CustomViewHolder) customViewHolder).progressBar.setVisibility(View.INVISIBLE);
                                ((CustomViewHolder) customViewHolder).siteText.setVisibility(View.VISIBLE);
                                ((CustomViewHolder) customViewHolder).imageView.setVisibility(View.VISIBLE);
                                try { database = context.openOrCreateDatabase(StringUtils.replace(feedItemList.get(i).getName(), "/", ""), MODE_PRIVATE, null);
                                    database.execSQL("CREATE TABLE IF NOT EXISTS data (site VARCHAR, price VARCHAR, url VARCHAR)");
                                    Cursor cursor = database.rawQuery("SELECT * FROM data", null);
                                    int siteIx = cursor.getColumnIndex("site");
                                    int priceIx = cursor.getColumnIndex("price");
                                    int urlIx = cursor.getColumnIndex("url");
                                    arrayListPrice.clear();
                                    if (cursor.moveToFirst()) { do { arrayListPrice.add(new HowMuchAndWhere(cursor.getString(siteIx), cursor.getString(priceIx), cursor.getString(urlIx))); } while (cursor.moveToNext()); }
                                    cursor.close();
                                    database.close();
                                } catch (SQLException e) { e.printStackTrace(); }
                                int same = 0;
                                for (int s = 0; s < arrayListPrice.size(); s++) { if (arrayListPrice.get(s).getPrice().equals("¯\\_(ツ)_/¯") || arrayListPrice.get(s).getPrice().equals("ಠ_ಠ") || arrayListPrice.get(s).getPrice().equals("□")) same = same + 1; }
                                if (same == arrayListPrice.size()) ((CustomViewHolder) customViewHolder).siteText.setText("¯\\_(ツ)_/¯");
                                else {
                                    try {
                                        StringBuilder sb=new StringBuilder();
                                        sb.append(arrayListPrice.get(0).getSite());
                                        int counter=0;
                                        for (int s = 1; s < arrayListPrice.size(); s++) {
                                            if (Integer.parseInt(arrayListPrice.get(s).getPrice().replaceAll("\\D", "0")) - Integer.parseInt(arrayListPrice.get(0).getPrice().replaceAll("\\D", "0"))==0) {
                                                sb.append(" \n").append(arrayListPrice.get(s).getSite());
                                                counter++;
                                                if (counter == 2) {
                                                    sb.append(" \n").append(". . .");
                                                    break;
                                                }
                                            } else break;
                                        }
                                        ((CustomViewHolder) customViewHolder).siteText.setText(sb.toString());
                                        ((CustomViewHolder) customViewHolder).siteText.setMaxLines(counter+2);
                                    } catch (NumberFormatException e) {
                                        e.printStackTrace();
                                    }
                                }
                                if(!contextList.contains(feedItemList.get(i).getName())) ((CustomViewHolder) customViewHolder).cv.setBackgroundColor(ContextCompat.getColor(context, R.color.colorWhite));
                                else ((CustomViewHolder) customViewHolder).cv.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
                                ((CustomViewHolder) customViewHolder).switchControl.setVisibility(View.GONE);
                            }
                            @Override
                            public void onError(Exception e) { }
                        });
            } else if (arrayListInfoFinal != null && arrayListInfoFinal.size() != 0) {
                if (arrayListInfoFinal.get(i).get("cover") != null && !arrayListInfoFinal.get(i).get("cover").isEmpty()) {
                    Picasso.get().load(arrayListInfoFinal.get(i).get("cover")).fit().centerCrop().error(R.drawable./*error*/ic_virus).into(((CustomViewHolder) customViewHolder).imageView, new Callback() {
                                @Override
                                public void onSuccess() { ((CustomViewHolder) customViewHolder).progressBar.setVisibility(View.INVISIBLE); }
                                @Override
                                public void onError(Exception e) {
                                    dismiss = false;
                                    ((CustomViewHolder) customViewHolder).progressBar.setVisibility(View.INVISIBLE);
                                    ((CustomViewHolder) customViewHolder).imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                                }
                            });
                } else {
                    dismiss = false;
                    ((CustomViewHolder) customViewHolder).progressBar.setVisibility(View.INVISIBLE);
                    ((CustomViewHolder) customViewHolder).imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    Picasso.get().load(R.drawable./*error*/ic_virus).error(R.drawable./*error*/ic_virus).into(((CustomViewHolder) customViewHolder).imageView);
                }
            } else if(from != null && from.equals("price")/* && arrayListComparison.get(i).getPrice() != "□"*/) {
                ((CustomViewHolder) customViewHolder).textSite.setText(arrayListComparison.get(i).getSite());
                ((CustomViewHolder) customViewHolder).textPrice.setText(arrayListComparison.get(i).getPrice());
            }  else if(from != null & from.equals("main")) {
                if(/*i == 0 && */favSingleItem.size() > 0) {
                    for (int m = 0; m<favSingleItem.size(); m++) {
                        ArrayList<HowMuchAndWhere> arrayListPrice = new ArrayList<>();
                        try { Picasso.get().load(favSingleItem.get(i).
                                    getCover()).error(R.drawable.ic_virus).
                                    into(((CustomViewHolder) customViewHolder).imageView,
                                            new Callback() {
                                @Override
                                public void onSuccess() {
                                    ((CustomViewHolder) customViewHolder).progressBar.setVisibility(View.INVISIBLE);
                                    ((CustomViewHolder) customViewHolder).siteText.setVisibility(View.VISIBLE);
                                    ((CustomViewHolder) customViewHolder).imageView.setVisibility(View.VISIBLE);
                                    try { database = context.openOrCreateDatabase(StringUtils.replace(favSingleItem.get(i).getName(), "/", ""), MODE_PRIVATE, null);
                                        database.execSQL("CREATE TABLE IF NOT EXISTS data (site VARCHAR, price VARCHAR, url VARCHAR)");
                                        Cursor cursor = database.rawQuery("SELECT * FROM data", null);
                                        int siteIx = cursor.getColumnIndex("site");
                                        int priceIx = cursor.getColumnIndex("price");
                                        int urlIx = cursor.getColumnIndex("url");
                                        arrayListPrice.clear();
                                        if (cursor.moveToFirst()) { do { arrayListPrice.add(new HowMuchAndWhere(cursor.getString(siteIx), cursor.getString(priceIx), cursor.getString(urlIx))); } while (cursor.moveToNext()); }
                                        cursor.close();
                                        database.close();
                                    } catch (SQLException e) { e.printStackTrace(); }
                                    int same = 0;
                                    for (int s = 0; s < arrayListPrice.size(); s++) { if (arrayListPrice.get(s).getPrice().equals("¯\\_(ツ)_/¯") || arrayListPrice.get(s).getPrice().equals("ಠ_ಠ") || arrayListPrice.get(s).getPrice().equals("□")) same = same + 1; }
                                    if (same == arrayListPrice.size()) ((CustomViewHolder) customViewHolder).siteText.setText("¯\\_(ツ)_/¯");
                                    else {
                                        try {
                                            StringBuilder sb=new StringBuilder();
                                            sb.append(arrayListPrice.get(0).getSite());
                                            int counter=0;
                                            for (int s = 1; s < arrayListPrice.size(); s++) {
                                                if (Integer.parseInt(arrayListPrice.get(s).getPrice().replaceAll("\\D", "0")) - Integer.parseInt(arrayListPrice.get(0).getPrice().replaceAll("\\D", "0"))==0) {
                                                    sb.append(" \n").append(arrayListPrice.get(s).getSite());
                                                    counter++;
                                                    if (counter == 2) {
                                                        sb.append(" \n").append(". . .");
                                                        break;
                                                    }
                                                } else break;
                                            }
                                            ((CustomViewHolder) customViewHolder).siteText.setText(sb.toString());
                                            ((CustomViewHolder) customViewHolder).siteText.setMaxLines(counter+2);
                                        } catch (NumberFormatException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    if(contextList != null && !contextList.contains(feedItemList.get(i).getName())) ((CustomViewHolder) customViewHolder).cv.setBackgroundColor(ContextCompat.getColor(context, R.color.colorWhite));
                                    else ((CustomViewHolder) customViewHolder).cv.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
                                    ((CustomViewHolder) customViewHolder).switchControl.setVisibility(View.GONE);
                                }
                                @Override
                                public void onError(Exception e) { }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) ((CustomViewHolder) customViewHolder).imageView.setBackgroundDrawable(categoryList.get(i).getDrawable());
                    else ((CustomViewHolder) customViewHolder).imageView.setBackground(categoryList.get(i).getDrawable());
                    if(((CustomViewHolder) customViewHolder).textSite != null) ((CustomViewHolder) customViewHolder).textSite.setText(categoryList.get(i).getCategoryName());
                }
            } else {
                ((CustomViewHolder) customViewHolder).progressBar.setVisibility(View.INVISIBLE);
                ((CustomViewHolder) customViewHolder).imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                Picasso.get().load(R.drawable./*error*/ic_virus).error(R.drawable./*error*/ic_virus).into(((CustomViewHolder) customViewHolder).imageView);
            }
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }
    @Override
    public int getItemViewType(int position) {
        if (arrayListInfoFinal != null && arrayListInfoFinal.get(position).get("type") != null && arrayListInfoFinal.get(position).get("type").equals("load")) return 1;
        else return TYPE_ITEM;
    }
    @Override
    public int getItemCount() {
        int size;
        if (arrayListComparison != null && from == null ) size = arrayListComparison.size();
        else if (feedItemList != null && feedItemList.equals(favSingleItem)) size = favSingleItem.size();
        else if (arrayListInfoFinal != null) size = arrayListInfoFinal.size();
        else if(from != null && from.equals("price")) size = arrayListComparison.size();
        else if(from != null && from.equals("main")) {
            if(favSingleItem.size() > 0) size = categoryList.size() + 1;
            else size = categoryList.size();
        } else size = 0;
        return size;
    }
    private void refreshAdapter() {
        feedItemList = favSingleItem;
        synchronized(SuggestionAdapter.this){ SuggestionAdapter.this.notifyDataSetChanged(); }
    }
    class CustomViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, AdapterView.OnLongClickListener {
        private ImageView imageView;
        private TextView bestSiteItem, textSite, textPrice, siteText,textViewSite,textViewPrice,textViewCargo,textViewTotalPrice, textViewPayment,textViewPayCash,textViewPayCard,textViewInfo,textViewMore, textViewCreditCard,textViewTransfer,textViewPaypal;
        private ProgressBar progressBar;
        private CardView cv;
        private Switch switchControl;
        CustomViewHolder(View view) {
            super(view);
            imageView = view.findViewById(R.id.itemImage);
            textSite = view.findViewById(R.id.textSite);
            bestSiteItem = view.findViewById(R.id.bestSiteItem);
            textPrice = view.findViewById(R.id.textPrice);
            siteText = view.findViewById(R.id.siteText);
            textViewSite = view.findViewById(R.id.textViewSite);
            textViewPrice = view.findViewById(R.id.textViewPrice);
            textViewCargo = view.findViewById(R.id.textViewCargo);
            textViewTotalPrice = view.findViewById(R.id.textViewTotalPrice);
            textViewPayment = view.findViewById(R.id.textViewPayment);
            textViewCreditCard = view.findViewById(R.id.textViewCreditCard);
            textViewTransfer = view.findViewById(R.id.textViewTransfer);
            textViewPaypal = view.findViewById(R.id.textViewPaypal);
            textViewPayCash = view.findViewById(R.id.textViewPayCash);
            textViewPayCard = view.findViewById(R.id.textViewPayCard);
            textViewMore = view.findViewById(R.id.textViewMore);
            textViewInfo = view.findViewById(R.id.textViewInfo);
            progressBar = view.findViewById(R.id.progressBar);
            cv = view.findViewById(R.id.cardView);
            switchControl = view.findViewById(R.id.switchControl);
            if(arrayListComparison == null || from != null && from.equals("price")) cv.setOnClickListener(this);
            if(arrayListComparison == null || from != null && from.equals("price")) cv.setOnLongClickListener(this);
        }
        @Override
        public boolean onLongClick(View view) {
            if (view != null) {
                if(coverLoading != null && coverLoading.getVisibility() != View.VISIBLE || progressBar != null && progressBar.getVisibility() != View.VISIBLE) {
                    try {
                        if (feedItemList != null && feedItemList.equals(favSingleItem)) {
                            favPosition = getAdapterPosition();
                            isContext = true;
                            nestedScrollView.smoothScrollTo(0, 0);
                            nestedScrollView.setScrollingEnabled(false);
                            outer_contextLayout.setVisibility(View.VISIBLE);
                            outer_searchLayout.setVisibility(View.INVISIBLE);
                            findOptimumForAll.setVisibility(View.GONE);
                            deleteAllFavs.setVisibility(View.GONE);
                            RelativeLayout.LayoutParams paramsForAuthorButton = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                            paramsForAuthorButton.setMargins(0, favLayout.getMeasuredHeight() + outer_searchLayout.getMeasuredHeight() + 25, 0, 0);
                            if(shadowImageForContext!=null&&paramsForAuthorButton!=null) {
                                shadowImageForContext.setLayoutParams(paramsForAuthorButton);
                                shadowImageForContext.setVisibility(View.VISIBLE);
                            }
                            checkContext(getAdapterPosition());
                        } else {
                            progressBar.setVisibility(View.VISIBLE);
                            int itemPosition = getAdapterPosition();
                            if (view.getId() == R.id.cardView) {
                                if (!receiver.isNetworkAvailable(context)) {
                                    progressBar.setVisibility(View.GONE);
                                    noInternet();
                                } else if (arrayForSelectedSites == null || arrayForSelectedSites.isEmpty()) {
                                    progressBar.setVisibility(View.GONE);
                                    if(!arrayListInfoFinal.get(itemPosition).get("name").contains("Response Error")) context.startActivity(new Intent(context, Settings2Activity.class).putExtra("from", "ContentTouch"));
                                } else if (arrayListInfoFinal.get(itemPosition).get("name") != null && favList != null && favList.contains(arrayListInfoFinal.get(itemPosition).get("name"))) createIntent(null, favSingleItem, favList.indexOf(arrayListInfoFinal.get(itemPosition).get("name")), "old");
                                else createIntentFinalDetailFinal(arrayListInfoFinal, itemPosition, "direct");
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(context, "Bir hata meydana geldi. Lütfen yeniden deneyiniz.", Toast.LENGTH_LONG).show();
                    }
                } else try {
                    changeValue(getAdapterPosition());
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(context, "Bir hata meydana geldi. Lütfen yeniden deneyiniz.", Toast.LENGTH_LONG).show();
                }
            }
            return true;
        }
        @Override
        public void onClick(View view) {
            try {
                if (view != null) {
                    int itemPosition = getAdapterPosition();
                    sugPosition = getAdapterPosition();
                    if(coverLoading != null && coverLoading.getVisibility()!=View.VISIBLE || progressBar != null && progressBar.getVisibility()!=View.VISIBLE) {
                        if (feedItemList != null && feedItemList.equals(favSingleItem)) {
                            if (isContext) checkContext(getAdapterPosition());
                            else { View alertLayout = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_click, null);
                                final LinearLayout buttonLay = alertLayout.findViewById(R.id.buttons_lay);
                                buttonLay.setVisibility(View.VISIBLE);
                                final Button buttonDelete = alertLayout.findViewById(R.id.buttonDelete);
                                final Button buttonSee = alertLayout.findViewById(R.id.buttonSeeOrSearch);
                                buttonDelete.setVisibility(View.VISIBLE);
                                buttonSee.setVisibility(View.VISIBLE);
                                final TextView textView = alertLayout.findViewById(R.id.textView);
                                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                StringBuilder sb = new StringBuilder();
                                sb.append(favSingleItem.get(getAdapterPosition()).getName());
                                if (!favSingleItem.get(getAdapterPosition()).getAuthor().isEmpty()) sb.append(" - ").append(favSingleItem.get(getAdapterPosition()).getAuthor());
                                textView.setText(sb);
                                buttonDelete.setText(R.string.delete);
                                buttonSee.setText(R.string.see);
                                builder.setCancelable(true).setView(alertLayout);
                                Dialog dialog = builder.create();
                                dialog.show();
                                buttonDelete.setOnClickListener(v -> {
                                    dialog.dismiss();
                                    try {
                                        if(getAdapterPosition()<favList.size()) {
                                            String delete = favList.get(getAdapterPosition());
                                            context.deleteDatabase(delete);
                                            fav = context.openOrCreateDatabase("Favs", MODE_PRIVATE, null);
                                            String execStringFav = "CREATE TABLE IF NOT EXISTS fav (name VARCHAR)";
                                            fav.execSQL(execStringFav);
                                            String sqlString = "DELETE FROM fav WHERE name LIKE (?)";
                                            SQLiteStatement statement = fav.compileStatement(sqlString);
                                            statement.bindString(1, delete);
                                            statement.execute();
                                            fav.close();
                                            bringFavs();
                                        } else Toast.makeText(context, "Bir hata meydana geldi. Lütfen farklı bir yol deneyiniz.", Toast.LENGTH_LONG).show();
                                    } catch (SQLException e) { e.printStackTrace();
                                        Toast.makeText(context, "Bir hata meydana geldi. Lütfen farklı bir yol deneyiniz.", Toast.LENGTH_LONG).show();
                                    }

                                });
                                buttonSee.setOnClickListener(v -> {
                                    dialog.dismiss();
                                    progressBar.setVisibility(View.VISIBLE);
                                    createIntent(null, favSingleItem, getAdapterPosition(), "old");
                                });
                            }
                        } else { progressBar.setVisibility(View.VISIBLE);
                            if (view.getId() == R.id.cardView && !arrayListInfoFinal.get(itemPosition).get("name").contains("Error")) {
                                if (!receiver.isNetworkAvailable(context)) {
                                    progressBar.setVisibility(View.GONE);
                                    noInternet();
                                } else if (arrayListInfoFinal.get(itemPosition).get("name") != null && favList != null && favList.contains(arrayListInfoFinal.get(itemPosition).get("name"))) createIntent(null, favSingleItem, favList.indexOf(arrayListInfoFinal.get(itemPosition).get("name")), "old");
                                else createIntentFinalDetailFinal(arrayListInfoFinal, itemPosition, "detail");
                            }
                        }
                    } else {
                        if(arrayListComparison.get(itemPosition).getURL().isEmpty()) Snackbar.make(view, "Seçili olmayan site.", Snackbar.LENGTH_LONG).setActionTextColor(Color.YELLOW).setAction("Seç ve Yenile", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String[] array = context.getResources().getStringArray(R.array.listOptions);
                                for(int i = 0; i<array.length; i++) {
                                    if(array[i].equals(arrayListComparison.get(itemPosition).getSite())) {
                                        arrayForSelectedSites.add(context.getResources().getStringArray(R.array.listValues)[i]);
                                        storeArray(i, context);
                                        storeArrayList(arrayForSelectedSites, context);
                                        loadArrayList(context);
                                        createIntent(singleItemModel,null, itemPosition,"refresh");
                                        break;
                                    }
                                }
                            }
                        }).show();
                        else if(arrayListComparison.get(itemPosition).getPrice().equals("ಠ_ಠ")) Snackbar.make(view, "Bağlantı hatası: "+arrayListComparison.get(itemPosition).getURL(), Snackbar.LENGTH_LONG).setActionTextColor(Color.YELLOW).setAction("Sayfayı Gör", v1 -> context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(arrayListComparison.get(itemPosition).getURL())))).show();
                        else context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(arrayListComparison.get(itemPosition).getURL())));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "Bir hata meydana geldi. Lütfen yeniden deneyiniz.", Toast.LENGTH_LONG).show();
            }
        }
        private void loadArrayList(Context mContext) {
            SharedPreferences prefs = mContext.getSharedPreferences("preference2", 0);
            int size = prefs.getInt("arrayForSelectedSites" +"_size", 0);
            ArrayList<String> arrayList = new ArrayList<>();
            for(int i=0;i<size;i++) arrayList.add(prefs.getString("arrayForSelectedSites" + "_" + i," "));
        }
        private void storeArray(int i,Context mContext) {
            SharedPreferences prefs = mContext.getSharedPreferences("preference", 0);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("checkedItems" + "_" + i, true);
            editor.apply();
        }
        private void storeArrayList(ArrayList<String> arrayList, Context mContext) {
            Set<String> set = new HashSet<>(arrayList);
            arrayList.clear();
            arrayList.addAll(set);
            SharedPreferences prefs = mContext.getSharedPreferences("preference2", 0);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("arrayForSelectedSites" + "_size", arrayList.size());
            for (int i = 0; i < arrayList.size(); i++) editor.putString("arrayForSelectedSites" + "_" + i, arrayList.get(i));
            editor.apply();
        }
        private void checkContext(int position) {
            if (position >= 0) {
                if(!indexesForUpdate.contains(Integer.toString(position))) {
                    indexesForUpdate.add(Integer.toString(position));
                } else indexesForUpdate.remove(Integer.toString(position));
                if (!contextList.contains(favList.get(position))) {
                    howMany++;
                    howManySelected.setText(String.valueOf(howMany));
                    howManySelected.setVisibility(View.VISIBLE);
                    contextList.add(favList.get(position));
                } else {
                    howMany--;
                    howManySelected.setText(String.valueOf(howMany));
                    contextList.remove(favList.get(position));
                    selectAll.setChecked(false);
                }
            }
            if(favList.size() >= 2) selectAll.setVisibility(View.VISIBLE);
            else selectAll.setVisibility(View.GONE);
            if (contextList.size() == 0) {
                selectAll.setChecked(false);
                indexesForUpdate.clear();
                howMany = 0;
                isContext = false;
                outer_contextLayout.setVisibility(View.GONE);
                outer_searchLayout.setVisibility(View.VISIBLE);
                findOptimumForAll.setVisibility(View.VISIBLE);
                deleteAllFavs.setVisibility(View.VISIBLE);
                shadowImageForContext.setVisibility(View.GONE);
                nestedScrollView.setScrollingEnabled(true);
            } else if (contextList.size() == 1) update.setVisibility(View.VISIBLE);
            else update.setVisibility(View.GONE);
            refreshAdapter();
        }
        void bringFavs() {
            favList = new ArrayList<>();
            favSingleItem = new ArrayList<>();
            try { fav = context.openOrCreateDatabase("Favs", MODE_PRIVATE, null);
                fav.execSQL("CREATE TABLE IF NOT EXISTS fav (name VARCHAR)");
                Cursor cursor = fav.rawQuery("SELECT * FROM fav", null);
                int nameIx = cursor.getColumnIndex("name");
                if (cursor.moveToFirst()) { do { favList.add(0, cursor.getString(nameIx)); } while (cursor.moveToNext()); }
                cursor.close();
                fav.close();
            } catch (SQLException e) { e.printStackTrace(); }
            for (int p = 0; p < favList.size(); p++) {
                try { database = context.openOrCreateDatabase(StringUtils.replace(favList.get(p), "/", ""), MODE_PRIVATE, null);
                    database.execSQL("CREATE TABLE IF NOT EXISTS info (name VARCHAR, author VARCHAR, publisher VARCHAR, cover VARCHAR, individual VARCHAR, coverBig VARCHAR, isbn VARCHAR,description VARCHAR)");
                    Cursor cursor2 = PriceActivity.database.rawQuery("SELECT * FROM info", null);
                    int nameIx = cursor2.getColumnIndex("name");
                    int authorIx = cursor2.getColumnIndex("author");
                    int publisherIx = cursor2.getColumnIndex("publisher");
                    int coverIx = cursor2.getColumnIndex("cover");
                    int individualIx = cursor2.getColumnIndex("individual");
                    int coverBigIx = cursor2.getColumnIndex("coverBig");
                    int isbnIx = cursor2.getColumnIndex("isbn");
                    int descriptionIx = cursor2.getColumnIndex("description");
                    if (cursor2.moveToFirst()) { do { favSingleItem.add(new SingleItemModel(cursor2.getString(nameIx), cursor2.getString(authorIx), cursor2.getString(publisherIx), cursor2.getString(coverIx), cursor2.getString(individualIx), cursor2.getString(coverBigIx), cursor2.getString(isbnIx), cursor2.getString(descriptionIx))); } while (cursor2.moveToNext()); }
                    cursor2.close();
                    database.close();
                } catch (SQLException e) { e.printStackTrace(); }
            }
            if (favList.size() >= 1) favLayout.setVisibility(View.VISIBLE);
            else favLayout.setVisibility(View.GONE);
            refreshAdapter();
        }
        private void noInternet() {
            View alertLayout = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_click, null);
            final LinearLayout buttonLay = alertLayout.findViewById(R.id.buttons_lay);
            buttonLay.setVisibility(View.VISIBLE);
            final Button buttonConnect = alertLayout.findViewById(R.id.buttonSeeOrSearch);
            buttonConnect.setVisibility(View.VISIBLE);
            final TextView textView = alertLayout.findViewById(R.id.textView);
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            textView.setText("İnternet bağlantısı yok.");
            buttonConnect.setText("Bağlan");
            builder.setCancelable(true).setView(alertLayout);
                dialog = builder.create();
                dialog.show();
                buttonConnect.setOnClickListener(v -> {
                    if (dialog != null && dialog.isShowing()) dialog.dismiss();
                    context.startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                });
        }
        private void changeValue(int position) {
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
                    if(!editText1.getText().toString().equals("")) editText2.requestFocus();
                    return true;
                }
                return false;
            });
            editText2.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
                    if (inputMethodManager != null) inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0)
                            ;return true;
                }
                return false;
            });
            editText2.setOnFocusChangeListener((v, hasFocus) -> {
                editText2.post(new Runnable() {
                    @Override
                    public void run() {
                        InputMethodManager inputMethodManager= (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                        Objects.requireNonNull(inputMethodManager.showSoftInput(editText1, InputMethodManager.SHOW_IMPLICIT));
                    }
                });
            });
            editText1.setOnFocusChangeListener((v, hasFocus) -> editText1.post(new Runnable() {
                @Override
                public void run() {
                    InputMethodManager inputMethodManager= (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    Objects.requireNonNull(inputMethodManager.showSoftInput(editText1, InputMethodManager.SHOW_IMPLICIT));
                }
            }));
            editText1.requestFocus();
            buttonAbsence.setOnClickListener(v -> {
                String site = arrayListComparison.get(position).getSite();
                int p = position;
                if (dialog != null && dialog.isShowing()) dialog.dismiss();
                arrayListComparison.get(position).setPrice("¯\\_(ツ)_/¯");
                sortNumeric();
                synchronized(SuggestionAdapter.this){
                    recyclerView.getRecycledViewPool().clear();
                    SuggestionAdapter.this.notifyDataSetChanged();
                }
                askToUpdate = true;
                try {
                    for(HowMuchAndWhere h : arrayListComparison) {
                        if(h.getSite().equals(site)) {
                            p = arrayListComparison.indexOf(h);
                            break;
                        }
                    }
                    recyclerView.smoothScrollToPosition(p);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            buttonChange.setOnClickListener(v -> {
                if(editText1.getText().toString().equals("")){
                    editText1.setError("Tamsayı değerini giriniz.");
                } else if(editText1.getText().toString().contains(".") ||
                        editText1.getText().toString().startsWith("0")){
                    editText1.setError("Değeri tamsayı olarak giriniz.");
                } else {
                    String site = arrayListComparison.get(position).getSite();
                    int p = position;
                    if (dialog != null && dialog.isShowing()) dialog.dismiss();
                    if(editText2.getText().toString().equals("")) arrayListComparison.get(position).setPrice(StringUtils.join(editText1.getText().toString(),",","00"," TL"));
                    else if(editText2.getText().length() == 1) arrayListComparison.get(position).setPrice(StringUtils.join(editText1.getText().toString(),",",editText2.getText().toString(),"0"," TL"));
                    else arrayListComparison.get(position).setPrice(StringUtils.join(editText1.getText().toString(),",",StringUtils.substring(editText2.getText().toString(),0,2)," TL"));
                    sortNumeric();
                    synchronized(SuggestionAdapter.this){
                        recyclerView.getRecycledViewPool().clear();
                        SuggestionAdapter.this.notifyDataSetChanged();
                    }
                    askToUpdate = true;
                    try {
                        for(HowMuchAndWhere h : arrayListComparison) {
                            if(h.getSite().equals(site)) {
                                p = arrayListComparison.indexOf(h);
                                break;
                            }
                        }
                        recyclerView.smoothScrollToPosition(p);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        private void sortNumeric() {
            try {
                Collections.sort(arrayListComparison, new Comparator<HowMuchAndWhere>() {
                    @Override
                    public int compare(HowMuchAndWhere o1, HowMuchAndWhere o2) { return extractInt(o1.getPrice()) - extractInt(o2.getPrice()); }
                    int extractInt(String s) {
                        if (s.isEmpty()) s = "9999999";
                        else if (s.equals("ಠ_ಠ")) s = "99999999";
                        else if (s.equals("□")) s = "999999999";
                        else s = s.replace("¯\\_(ツ)_/¯", "999999").replaceAll("\\D", "");
                        return s.isEmpty() ? 0 : Integer.parseInt(s);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        private void createIntentFinalDetailFinal(ArrayList<HashMap<String, String>> arrayList, int position, String way) {
            if (arrayList != null && !arrayList.get(position).get("name").contains("Error")) {
                try { (new Retrofit.Builder().addConverterFactory(ScalarsConverterFactory.create()).baseUrl("https://www.bkmkitap.com/").client(new OkHttpClient().newBuilder().build()).build())
                            .create(ApiService.class).getPrices(arrayList.get(position).get("individual")).enqueue(new retrofit2.Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                Document doc = Jsoup.parse(response.body());
                                String src = doc.select("div.fl.col-12 > iframe").attr("src");
                                int index = src.indexOf("=");
                                String volume = doc.select("div.col.cilt.col-12 > div:nth-child(1) > span:nth-child(2)").text();
                                String pages = doc.select("div.col.cilt.col-12 > div:nth-child(2) > span:nth-child(2)").text();
                                String isbn;
                                Intent intent = new Intent(context, PriceActivity.class);
                                intent.putExtra("info", way);
                                intent.putExtra("name", arrayList.get(position).get("name"));
                                intent.putExtra("author", arrayList.get(position).get("author"));
                                intent.putExtra("publisher", arrayList.get(position).get("publisher"));
                                intent.putExtra("cover", arrayList.get(position).get("cover"));
                                StringBuilder sb = new StringBuilder();
                                for (Element table : doc.select("#productDetailTab")) { for (Element row : table.select("#productDetailTab > div > p")) { sb.append(row.text()).append("\n")/*.append("\n")*/; } }
                                intent.putExtra("description", sb.toString());
                                intent.putExtra("coverBig", doc.select("#productImage > li > a > span > img").attr("src"));
                                intent.putExtra("volume", volume);
                                if (!doc.select("span[itemprop=isbn]").text().isEmpty()) isbn = doc.select("span[itemprop=isbn]").text();
                                else if (!src.substring(index + 1).isEmpty()) isbn = src.substring(index + 1);
                                else if (!doc.select("div.col.col-6.centerBlock > div.col.cilt.col-12 > div:nth-child(3) > span:nth-child(2)").text().isEmpty()) isbn = doc.select("div.col.col-6.centerBlock > div.col.cilt.col-12 > div:nth-child(3) > span:nth-child(2)").text();
                                else if (!doc.select("#productRight > div.col.col-6.centerBlock > div.col.cilt.col-12 > div:nth-child(2) > span:nth-child(2)").text().isEmpty()) isbn = doc.select("#productRight > div.col.col-6.centerBlock > div.col.cilt.col-12 > div:nth-child(2) > span:nth-child(2)").text();
                                else isbn = doc.select("div.col.cilt.col-12 > div > span:nth-child(2)").text();
                                intent.putExtra("isbn", isbn);
                                if(!pages.equals(isbn)) intent.putExtra("pages", pages);
                                intent.putExtra("individual", arrayList.get(position).get("individual"));
                                intent.putExtra("position",position);
                                intent.putExtra("arrayList",arrayList);
                                context.startActivity(intent);
                                progressBar.setVisibility(View.GONE);
                            } else progressBar.setVisibility(View.GONE);
                        }
                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) { progressBar.setVisibility(View.GONE); }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    progressBar.setVisibility(View.GONE);
                }
            } else if(progressBar != null) progressBar.setVisibility(View.GONE);
        }
        void sendIntent(SingleItemModel singleItemModel, ArrayList<SingleItemModel> singleItem, int position, String volume, String pages, String info) {
            String name, author, publisher, cover,  description, coverBig, isbn, individual;
            if(singleItem != null) {
                name = singleItem.get(position).getName();
                author = singleItem.get(position).getAuthor();
                publisher = singleItem.get(position).getPublisher();
                cover = singleItem.get(position).getCover();
                description = singleItem.get(position).getDescription();
                coverBig = singleItem.get(position).getCoverBig();
                isbn = singleItem.get(position).getIsbn();
                individual = singleItem.get(position).getIndividual();
            } else {
                name = singleItemModel.getName();
                author = singleItemModel.getAuthor();
                publisher = singleItemModel.getPublisher();
                cover = singleItemModel.getCover();
                description = singleItemModel.getDescription();
                coverBig = singleItemModel.getCoverBig();
                isbn = singleItemModel.getIsbn();
                individual = singleItemModel.getIndividual();
            }
            Intent intent = new Intent(context, PriceActivity.class);
            intent.putExtra("info", info);
            intent.putExtra("name", name);
            intent.putExtra("author", author);
            intent.putExtra("publisher", publisher);
            intent.putExtra("cover", cover);
            intent.putExtra("description", description);
            intent.putExtra("coverBig", coverBig);
            intent.putExtra("isbn", isbn);
            intent.putExtra("individual", individual);
            if(volume != null) intent.putExtra("volume", volume);
            if(pages != null) intent.putExtra("pages", pages);
            context.startActivity(intent);
            if(progressBar != null) progressBar.setVisibility(View.GONE);
            if(info.equals("refresh")) ((Activity)context).finish();
        }
        private void createIntent(SingleItemModel singleItemModel, ArrayList<SingleItemModel> singleItem, int position, String info) {
            String cover, individual;
            if(singleItem != null) {
                cover = singleItem.get(position).getCoverBig();
                individual = singleItem.get(position).getIndividual();
            } else {
                cover = singleItemModel.getCoverBig();
                individual = singleItemModel.getIndividual();
            }
            if(progressBar != null) {
                Picasso.get().load(cover).fit().error(R.drawable./*error*/ic_virus).into(coverLoading, new com.squareup.picasso.Callback() {
                    @Override public void onSuccess() { coverLoading.setVisibility(View.VISIBLE); }
                    @Override public void onError(Exception e) {progressBar.setVisibility(View.VISIBLE); }
                });
            }
            if(volume != null && pages != null) sendIntent(singleItemModel, singleItem, position, volume, pages, info);
            else {
                try { (new Retrofit.Builder().addConverterFactory(ScalarsConverterFactory.create())
                        .baseUrl("https://www.halkkitabevi.com/").client(new OkHttpClient().newBuilder().build()).build())
                        .create(ApiService.class).getPrices(individual).enqueue(new retrofit2.Callback<String>() {
                            @Override
                            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Document doc = Jsoup.parse(response.body());
                                    sendIntent(singleItemModel, singleItem, position, doc.select("div.__product_fields > div:nth-child(3) > div:nth-child(3)").text(), doc.select("div.__product_fields > div:nth-child(2) > div:nth-child(3)").text(), info);
                                } else {
                                    sendIntent(singleItemModel, singleItem, position, null, null, info);
                                    coverLoading.setVisibility(View.GONE);
                                }
                            }
                            @Override
                            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                                sendIntent(singleItemModel, singleItem, position, null, null, info);
                                coverLoading.setVisibility(View.GONE);
                            }
                        });
                } catch (Exception e) {
                    e.printStackTrace();
                    sendIntent(singleItemModel, singleItem, position, null, null, info);
                    coverLoading.setVisibility(View.GONE);
                }
            }
        }
    }
    static class LoadHolder extends RecyclerView.ViewHolder {
        LoadHolder(View itemView) { super(itemView); }
    }
}