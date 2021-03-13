package com.muniryenigul.kam.fragments;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.muniryenigul.kam.R;
import com.muniryenigul.kam.activities.PriceActivity;
import com.muniryenigul.kam.activities.Settings2Activity;
import com.muniryenigul.kam.ers.BookFinalAdapter;
import com.muniryenigul.kam.ers.RecyclerTouchListener;
import com.muniryenigul.kam.interfaces.ApiService;
import com.muniryenigul.kam.models.SingleItemModel;
import com.muniryenigul.kam.utils.Utils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import static com.muniryenigul.kam.MainActivity.arrayForSelectedSites;
import static com.muniryenigul.kam.MainActivity.favList;
import static com.muniryenigul.kam.MainActivity.favPosition;
import static com.muniryenigul.kam.MainActivity.halkmQuery;
import static com.muniryenigul.kam.MainActivity.hasfocus;
import static com.muniryenigul.kam.MainActivity.favSingleItem;
import static com.muniryenigul.kam.MainActivity.filteredList;
import static com.muniryenigul.kam.MainActivity.receiver;
import static com.muniryenigul.kam.MainActivity.halkQuery;
import static com.muniryenigul.kam.MainActivity.mQuery;
import static com.muniryenigul.kam.MainActivity.search;

public class FragmentHalk extends Fragment {
    private BookFinalAdapter bookFinalAdapter, publisherFilterAdapter, authorFilterAdapter;
    private ArrayList<HashMap<String, String>> arrayListInfoFinal;
    private ArrayList<String> publisherFilterList, publisherIDsList, authorFilterList, authorIDsList;
    private LinearLayout filterLayout;
    private View filterLine;
    private ProgressBar progressBar;
    private RecyclerView listView, publisherFilter, authorFilter;
    private GridLayoutManager glm;
    private final LinearLayout searchedLayout;

    public FragmentHalk(LinearLayout searchedLayout) {
        this.searchedLayout = searchedLayout;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_halk, container, false);
        progressBar = view.findViewById(R.id.progressBar);
        filterLayout = view.findViewById(R.id.filterLayout);
        filterLine = view.findViewById(R.id.filterLine);
        listView = view.findViewById(R.id.listView);
        publisherFilter = view.findViewById(R.id.publisherFilter);
        authorFilter = view.findViewById(R.id.authorFilter);
        arrayListInfoFinal = new ArrayList<>();
        bookFinalAdapter = new BookFinalAdapter(getActivity(), arrayListInfoFinal);
        bookFinalAdapter.setLoadMoreListener(() -> listView.post(this::loadMore));
        listView.setHasFixedSize(true);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(listView.getContext(), DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(Objects.requireNonNull(AppCompatResources.getDrawable(requireContext(), R.drawable.line)));
        listView.addItemDecoration(dividerItemDecoration);

        if (getResources().getConfiguration().orientation == 2) {
            if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE) glm = new GridLayoutManager(getActivity(), 3);
            else glm = new GridLayoutManager(getActivity(), 2);
        } else {
            if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE) glm = new GridLayoutManager(getActivity(), 2);
            else glm = new GridLayoutManager(getActivity(), 1);
        }
        listView.setLayoutManager(glm);
        listView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(getActivity(), R.anim.layout_animation_down_to_up));
        listView.setAdapter(bookFinalAdapter);
        listView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                hideKeyboard(listView);
                searchedLayout.setVisibility(View.GONE);
            }
        });
        publisherFilterList = new ArrayList<>();
        publisherIDsList = new ArrayList<>();
        authorFilterList = new ArrayList<>();
        authorIDsList = new ArrayList<>();
        publisherFilterAdapter = new BookFinalAdapter(getActivity(), publisherFilterList, publisherIDsList, "filter");
        authorFilterAdapter = new BookFinalAdapter(getActivity(), authorFilterList, authorIDsList,"filter");

        publisherFilter.setAdapter(publisherFilterAdapter);
        authorFilter.setAdapter(authorFilterAdapter);
        publisherFilter.setHasFixedSize(true);
        authorFilter.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setAutoMeasureEnabled(false);
        LinearLayoutManager llm2 = new LinearLayoutManager(getActivity());
        llm2.setAutoMeasureEnabled(false);
        publisherFilter.setLayoutManager(llm);
        authorFilter.setLayoutManager(llm2);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = height / 2;
        listView.setLayoutParams(params);

        ViewGroup.LayoutParams paramForPublisherFilter = publisherFilter.getLayoutParams();
        paramForPublisherFilter.width =  ( width / 2 ) - 4;
        publisherFilter.setLayoutParams(paramForPublisherFilter);

        ViewGroup.LayoutParams paramForAuthorFilter = authorFilter.getLayoutParams();
        paramForAuthorFilter.width =  ( width / 2 ) - 4;
        authorFilter.setLayoutParams(paramForAuthorFilter);

        publisherFilter.addOnItemTouchListener(new RecyclerTouchListener(getActivity(),
                publisherFilter, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                try {
                    search = 1;
                    if(!filteredList.contains(publisherIDsList.get(position))) filteredList.add(publisherIDsList.get(position));
                    else filteredList.remove(publisherIDsList.get(position));
                    if(!halkQuery.contains(publisherIDsList.get(position))) {
                        if(!halkQuery.contains("prpm[wrt]")) {
                            halkQuery = "https://www.halkkitabevi.com/index.php?p=Products&q_field_active=0&ctg_id=&q=" + mQuery + "&search=&q_field=&page="+search;
                        } else {
                            int start = halkQuery.indexOf("page=");
                            int end = halkQuery.indexOf("&prpm");
                            String newhalkQuery = halkQuery.substring(start,end);
                            halkQuery = halkQuery.replace(newhalkQuery, "page="+search);
                        }
                        halkQuery = halkQuery + "&" + publisherIDsList.get(position) + "=" + publisherIDsList.get(position).replaceAll("\\D+","");
                    } else {
                        int start = halkQuery.indexOf("page=");
                        int end = halkQuery.indexOf("&prpm");
                        String newhalkQuery = halkQuery.substring(start,end);
                        halkQuery = halkQuery.replace(newhalkQuery, "page="+search);
                        halkQuery = halkQuery.replace("&" + publisherIDsList.get(position) + "=" + publisherIDsList.get(position).replaceAll("\\D+",""),"");
                    }
                    Utils.bringValues(requireActivity(), halkQuery, mQuery,"text", bookFinalAdapter,
                            publisherFilterAdapter, authorFilterAdapter, arrayListInfoFinal,
                            publisherFilterList, publisherIDsList, authorFilterList, authorIDsList,
                            filterLayout, filterLine, progressBar, search, 2,
                            listView, publisherFilter, authorFilter);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onLongClick(View view, int position) {}
        }));

        authorFilter.addOnItemTouchListener(new RecyclerTouchListener(getActivity(),
                authorFilter, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                try {
                    search = 1;
                    if(!filteredList.contains(authorIDsList.get(position))) filteredList.add(authorIDsList.get(position));
                    else filteredList.remove(authorIDsList.get(position));
                    if(!halkQuery.contains(authorIDsList.get(position))) {
                        if(!halkQuery.contains("prpm[pub]")) {
                            halkQuery = "https://www.halkkitabevi.com/index.php?p=Products&q_field_active=0&ctg_id=&q=" + mQuery + "&search=&q_field=&page="+search;
                        } else {
                            int start = halkQuery.indexOf("page=");
                            int end = halkQuery.indexOf("&prpm");
                            String newhalkQuery = halkQuery.substring(start,end);
                            halkQuery = halkQuery.replace(newhalkQuery, "page="+search);
                        }
                        halkQuery = halkQuery + "&" + authorIDsList.get(position) + "=" + authorIDsList.get(position).replaceAll("\\D+","");
                    } else {
                        int start = halkQuery.indexOf("page=");
                        int end = halkQuery.indexOf("&prpm");
                        String newhalkQuery = halkQuery.substring(start,end);
                        halkQuery = halkQuery.replace(newhalkQuery, "page="+search);
                        halkQuery = halkQuery.replace("&" + authorIDsList.get(position) + "=" + authorIDsList.get(position).replaceAll("\\D+",""),"");
                    }
                    //sendToFind(mQuery, halkQuery);
                    Utils.bringValues(requireActivity(), halkQuery, mQuery, "text", bookFinalAdapter,
                            publisherFilterAdapter, authorFilterAdapter, arrayListInfoFinal,
                            publisherFilterList, publisherIDsList, authorFilterList, authorIDsList,
                            filterLayout, filterLine, progressBar, search, 2,
                            listView, publisherFilter, authorFilter);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onLongClick(View view, int position) {}
        }));

        listView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), listView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                favPosition = position;
                if (!receiver.isNetworkAvailable(getActivity())) noInternet();
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
        Utils.bringValues(requireActivity(), halkQuery, mQuery,"text", bookFinalAdapter,
                publisherFilterAdapter, authorFilterAdapter, arrayListInfoFinal,
                publisherFilterList, publisherIDsList, authorFilterList, authorIDsList,
                filterLayout, filterLine, progressBar, search, 2,
                listView, publisherFilter, authorFilter);

        return view;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (glm.findFirstCompletelyVisibleItemPosition() == -1) glm.scrollToPosition(glm.findFirstVisibleItemPosition());
        else glm.scrollToPosition(glm.findFirstCompletelyVisibleItemPosition());
        if (newConfig.orientation == 2) {
            if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE) {
                glm.setSpanCount(3);
            } else {
                glm.setSpanCount(2);
            }
        } else {
            if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE) {
                glm.setSpanCount(2);
            } else {
                glm.setSpanCount(1);
            }
        }
        listView.setLayoutManager(glm);
        listView.setAdapter(bookFinalAdapter);
    }

    private void loadMore() {
        if(halkQuery.contains("prpm[pub]") || halkQuery.contains("prpm[wrt]")) {
            search++;
            int start = halkQuery.indexOf("page=");
            int end = halkQuery.indexOf("&prpm");
            String newhalkQuery = halkQuery.substring(start,end);
            halkQuery = halkQuery.replace(newhalkQuery, "page="+search);
            Utils.bringValues(requireActivity(), halkQuery, mQuery, "load", bookFinalAdapter,
                    publisherFilterAdapter, authorFilterAdapter, arrayListInfoFinal,
                    publisherFilterList, publisherIDsList, authorFilterList, authorIDsList,
                    filterLayout, filterLine, progressBar, search, 2,
                    listView, publisherFilter, authorFilter);
        }
        else {
            search++;
            Utils.bringValues(requireActivity(),
                    "https://www.halkkitabevi.com/index.php?p=Products&q_field_active=0&ctg_id=&q=" + halkmQuery + "&search=&q_field=&page="+search,
                    mQuery,"load", bookFinalAdapter,
                    publisherFilterAdapter, authorFilterAdapter, arrayListInfoFinal,
                    publisherFilterList, publisherIDsList, authorFilterList, authorIDsList,
                    filterLayout, filterLine, progressBar, search, 2,
                    listView, publisherFilter, authorFilter);
        }
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
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
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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
        startActivity(new Intent(getContext(), Settings2Activity.class).putExtra("from", from));
        hasfocus = false;
    }

    public void noInternet() {
        View alertLayout = getLayoutInflater().inflate(R.layout.custom_click, null);
        final LinearLayout buttonLay = alertLayout.findViewById(R.id.buttons_lay);
        buttonLay.setVisibility(View.VISIBLE);
        final Button buttonConnect = alertLayout.findViewById(R.id.buttonSeeOrSearch);
        buttonConnect.setVisibility(View.VISIBLE);
        final TextView textView = alertLayout.findViewById(R.id.textView);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        textView.setText("İnternet bağlantısı yok.");
        buttonConnect.setText("Bağlan");
        builder.setCancelable(true).setView(alertLayout);
        Dialog dialog = builder.create();
        dialog.show();
        buttonConnect.setOnClickListener(v -> {
            if (dialog != null && dialog.isShowing()) dialog.dismiss();
            startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
            hasfocus = false;
        });
    }

    public void sendIntent(ArrayList<SingleItemModel> singleItem, int position, String volume, String pages, String info) {
        Intent intent = new Intent(getContext(), PriceActivity.class);
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
        //cancelContext();
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
        if (!receiver.isNetworkAvailable(getContext())) {
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
                            Intent intent = new Intent(getContext(), PriceActivity.class);
                            intent.putExtra("info", info);
                            intent.putExtra("name", arrayList.get(position).get("name"));
                            intent.putExtra("author", arrayList.get(position).get("author"));
                            intent.putExtra("publisher", arrayList.get(position).get("publisher"));
                            intent.putExtra("cover", arrayList.get(position).get("cover"));
                            StringBuilder sb = new StringBuilder();
                            for (Element table : doc.select("div.wysiwyg")) {
                                for (Element row : table.select("div.wysiwyg > p")) {
                                    if(!row.text().isEmpty()) sb.append("\n\n").append(row.text());
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
                        Toast.makeText(getContext(), "Lütfen internet bağlantınızı kontrol ediniz.", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Bir hata meydana geldi. Lütfen tekrar deneyiniz.", Toast.LENGTH_SHORT).show();
            }
        } else {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(getContext(), "Lütfen önerileri güncelleyiniz.", Toast.LENGTH_SHORT).show();
        }
    }
}
