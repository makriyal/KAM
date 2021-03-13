package com.muniryenigul.kam.fragments;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
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
import static com.muniryenigul.kam.MainActivity.favSingleItem;
import static com.muniryenigul.kam.MainActivity.filteredList;
import static com.muniryenigul.kam.MainActivity.hasfocus;
import static com.muniryenigul.kam.MainActivity.kitapyurduQuery;
import static com.muniryenigul.kam.MainActivity.mQuery;
import static com.muniryenigul.kam.MainActivity.receiver;
import static com.muniryenigul.kam.MainActivity.search;

public class FragmentKitapyurdu extends Fragment {
    private BookFinalAdapter bookFinalAdapter, publisherFilterAdapter, authorFilterAdapter;
    private ArrayList<HashMap<String, String>> arrayListInfoFinal;
    private ArrayList<String> publisherFilterList, publisherIDsList, authorFilterList, authorIDsList;
    private LinearLayout filterLayout;
    private View filterLine;
    private ProgressBar progressBar;
    private RecyclerView listView, publisherFilter, authorFilter;
    private GridLayoutManager glm;
    private final LinearLayout searchedLayout;

    public FragmentKitapyurdu(LinearLayout searchedLayout) {
        this.searchedLayout = searchedLayout;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_kitapyurdu, container, false);
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
                    if(!kitapyurduQuery.contains(publisherIDsList.get(position))) {
                        if(kitapyurduQuery.contains("&filter_manufacturer=")) {
                            int appliedFilterIndex = kitapyurduQuery.indexOf("&filter_manufacturer=");
                            int lastIndex = kitapyurduQuery.lastIndexOf("&");
                            String appliedFilter = kitapyurduQuery.substring(appliedFilterIndex, lastIndex).replace("&filter_manufacturer=", "");
                            kitapyurduQuery = "https://www.kitapyurdu.com/index.php?route=product/search&filter_name=" +
                                    mQuery + "&filter_manufacturer=" + appliedFilter + "&filter_publisher=" + publisherIDsList.get(position) + "&page=" + search;
                        } else kitapyurduQuery = "https://www.kitapyurdu.com/index.php?route=product/search&filter_name=" +
                                    mQuery + "&filter_publisher=" + publisherIDsList.get(position) + "&page=" + search;
                    } else {
                        if(kitapyurduQuery.contains("&filter_manufacturer=")) {
                            int appliedAuthorFilterIndex = kitapyurduQuery.indexOf("&filter_manufacturer=");
                            String sub = kitapyurduQuery.substring(appliedAuthorFilterIndex);
                            int lastIndex = sub.lastIndexOf("&");
                            String appliedAuthorFilter = sub.substring(lastIndex).replace("&filter_manufacturer=", "");
                            kitapyurduQuery = "https://www.kitapyurdu.com/index.php?route=product/search&filter_name=" +
                                    mQuery + "&filter_manufacturer=" + appliedAuthorFilter + "&page=" + search;
                        } else kitapyurduQuery = "https://www.kitapyurdu.com/index.php?route=product/search&filter_name=" +
                                    mQuery + "&page=" + search;
                    }
                    Utils.bringValues(requireActivity(), kitapyurduQuery, mQuery,"text", bookFinalAdapter,
                            publisherFilterAdapter, authorFilterAdapter, arrayListInfoFinal,
                            publisherFilterList, publisherIDsList, authorFilterList, authorIDsList,
                            filterLayout, filterLine, progressBar, search, 1,
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
                    if(!kitapyurduQuery.contains(authorIDsList.get(position))) {
                        if(kitapyurduQuery.contains("&filter_publisher=")) {
                            int appliedFilterIndex = kitapyurduQuery.indexOf("&filter_publisher=");
                            int pageIndex = kitapyurduQuery.indexOf("&page=");
                            String appliedFilter = kitapyurduQuery.substring(appliedFilterIndex, pageIndex).replace("&filter_publisher=", "");
                            kitapyurduQuery = "https://www.kitapyurdu.com/index.php?route=product/search&filter_name=" +
                                    mQuery + "&filter_manufacturer=" + authorIDsList.get(position) + "&filter_publisher=" + appliedFilter + "&page=" + search;
                        } else kitapyurduQuery = "https://www.kitapyurdu.com/index.php?route=product/search&filter_name=" +
                                    mQuery + "&filter_manufacturer=" + authorIDsList.get(position) + "&page=" + search;
                    } else {
                        if(kitapyurduQuery.contains("&filter_publisher=")) {
                            int appliedPublisherFilterIndex = kitapyurduQuery.indexOf("&filter_publisher=");
                            String sub = kitapyurduQuery.substring(appliedPublisherFilterIndex);
                            int lastIndex = sub.lastIndexOf("&");
                            String appliedPublisherFilter = sub.substring(lastIndex).replace("&filter_publisher=", "");
                            kitapyurduQuery = "https://www.kitapyurdu.com/index.php?route=product/search&filter_name=" +
                                    mQuery + "&filter_publisher=" + appliedPublisherFilter + "&page=" + search;
                        } else kitapyurduQuery = "https://www.kitapyurdu.com/index.php?route=product/search&filter_name=" +
                                    mQuery + "&page=" + search;
                    }
                    Utils.bringValues(requireActivity(), kitapyurduQuery, mQuery,"text", bookFinalAdapter,
                            publisherFilterAdapter, authorFilterAdapter, arrayListInfoFinal,
                            publisherFilterList, publisherIDsList, authorFilterList, authorIDsList,
                            filterLayout, filterLine, progressBar, search, 1,
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
        Utils.bringValues(requireActivity(), kitapyurduQuery, mQuery,"text", bookFinalAdapter,
                publisherFilterAdapter, authorFilterAdapter, arrayListInfoFinal,
                publisherFilterList, publisherIDsList, authorFilterList, authorIDsList,
                filterLayout, filterLine, progressBar, search, 1,
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
            } else glm.setSpanCount(2);
        } else {
            if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE) {
                glm.setSpanCount(2);
            } else glm.setSpanCount(1);
        }
        listView.setLayoutManager(glm);
        listView.setAdapter(bookFinalAdapter);
    }

    private void loadMore() {
        try {
            int start = kitapyurduQuery.indexOf("page=");
            String page = kitapyurduQuery.substring(start).replace("page=", "");
            int searches = Integer.parseInt(page);
            searches++;
            search = searches;
            kitapyurduQuery = StringUtils.join(kitapyurduQuery.substring(0, start), "page=", searches);
            Utils.bringValues(requireActivity(), kitapyurduQuery, mQuery, "load", bookFinalAdapter,
                    publisherFilterAdapter, authorFilterAdapter, arrayListInfoFinal,
                    publisherFilterList, publisherIDsList, authorFilterList, authorIDsList,
                    filterLayout, filterLine, progressBar, search, 1,
                    listView, publisherFilter, authorFilter);
        } catch (NumberFormatException e) {
            e.printStackTrace();
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
            try { (new Retrofit.Builder().addConverterFactory(ScalarsConverterFactory.create())
                        .baseUrl("https://www.kitapyurdu.com/").client(new OkHttpClient().newBuilder().build()).build())
                        .create(ApiService.class).getPrices(arrayList.get(position).get("individual")).enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Document doc = Jsoup.parse(response.body());
                            String src = doc.select("div.attributes > table > tbody > tr:nth-child(1) > td:nth-child(2)").text();
                            if(!src.matches("\\d+(?:\\.\\d+)?")) src = doc.select("div.attributes > table > tbody > tr:nth-child(2 > td:nth-child(2)").text();
                            if(!src.matches("\\d+(?:\\.\\d+)?")) src = doc.select("div.attributes > table > tbody > tr:nth-child(3) > td:nth-child(2)").text();
                            if(!src.matches("\\d+(?:\\.\\d+)?")) src = doc.select("div.attributes > table > tbody > tr:nth-child(4) > td:nth-child(2)").text();
                            if(!src.matches("\\d+(?:\\.\\d+)?")) src = doc.select("div.attributes > table > tbody > tr:nth-child(5) > td:nth-child(2)").text();
                            if(!src.matches("\\d+(?:\\.\\d+)?")) src = doc.select("div.attributes > table > tbody > tr:nth-child(6) > td:nth-child(2)").text();
                            String volume = doc.select("div.attributes > table > tbody > tr:nth-child(1)").text();
                            if(!volume.contains("Cilt Tipi")) volume = doc.select("div.attributes > table > tbody > tr:nth-child(2)").text();
                            if(!volume.contains("Cilt Tipi")) volume = doc.select("div.attributes > table > tbody > tr:nth-child(3)").text();
                            if(!volume.contains("Cilt Tipi")) volume = doc.select("div.attributes > table > tbody > tr:nth-child(4)").text();
                            if(!volume.contains("Cilt Tipi")) volume = doc.select("div.attributes > table > tbody > tr:nth-child(5)").text();
                            if(!volume.contains("Cilt Tipi")) volume = doc.select("div.attributes > table > tbody > tr:nth-child(6)").text();
                            String pages = doc.select("div.attributes > table > tbody > tr:nth-child(1)").text();
                            if(!pages.contains("Sayfa Sayısı")) pages = doc.select("div.attributes > table > tbody > tr:nth-child(2)").text();
                            if(!pages.contains("Sayfa Sayısı")) pages = doc.select("div.attributes > table > tbody > tr:nth-child(3)").text();
                            if(!pages.contains("Sayfa Sayısı")) pages = doc.select("div.attributes > table > tbody > tr:nth-child(4)").text();
                            if(!pages.contains("Sayfa Sayısı")) pages = doc.select("div.attributes > table > tbody > tr:nth-child(5)").text();
                            if(!pages.contains("Sayfa Sayısı")) pages = doc.select("div.attributes > table > tbody > tr:nth-child(6)").text();
                            Intent intent = new Intent(getContext(), PriceActivity.class);
                            intent.putExtra("info", info);
                            intent.putExtra("name", arrayList.get(position).get("name"));
                            intent.putExtra("author", arrayList.get(position).get("author"));
                            intent.putExtra("publisher", arrayList.get(position).get("publisher"));
                            intent.putExtra("cover", arrayList.get(position).get("cover"));
                            intent.putExtra("description", doc.select("#description_text > span").text());
                            intent.putExtra("coverBig", doc.select("div.pr_images > div > div > a > img").attr("src"));
                            intent.putExtra("volume", StringUtils.replace(volume,  "Cilt Tipi: ", ""));
                            intent.putExtra("pages", StringUtils.replace(pages,  "Sayfa Sayısı: ", ""));
                            intent.putExtra("isbn", src);
                            intent.putExtra("individual", arrayList.get(position).get("individual"));
                            hasfocus = false;
                            startActivity(intent);
                        }
                        progressBar.setVisibility(View.GONE);
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