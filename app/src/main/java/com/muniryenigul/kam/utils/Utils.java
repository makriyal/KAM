package com.muniryenigul.kam.utils;
import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;
import com.muniryenigul.kam.R;
import com.muniryenigul.kam.ers.BookFinalAdapter;
import com.muniryenigul.kam.interfaces.ApiService;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import static com.muniryenigul.kam.MainActivity.tab0selected;
import static com.muniryenigul.kam.MainActivity.tab1selected;
import static com.muniryenigul.kam.MainActivity.tab2selected;

public class Utils {
    private static int all, count, pages;
    private static HashMap<String, String> mapInfoFinal = null;
    private static final ArrayList<HashMap<String, String>> arrayListSpare = new ArrayList<>();

    public static void bringValues(Context context, String searchURL, String mQuery, String from,
                                   BookFinalAdapter adapter, BookFinalAdapter adapter2,
                                   BookFinalAdapter adapter3,
                                   ArrayList<HashMap<String, String>> arrayList,
                                   ArrayList<String> publisherFilterList,
                                   ArrayList<String> publisherIDsList,
                                   ArrayList<String> authorFilterList,
                                   ArrayList<String> authorIDsList,
                                   LinearLayout filterLayout, View filterLine,
                                   ProgressBar progressBar, int search, int currenSitetoSearch,
                                   RecyclerView listView, RecyclerView publisherFilter, RecyclerView authorFilter) {
        String[] baseURLs = context.getResources().getStringArray(R.array.baseURLs);
        if (!from.equals("load")) {
            arrayList.clear();
            if(filterLayout != null) filterLayout.setVisibility(View.GONE);
            if(filterLine != null) filterLine.setVisibility(View.GONE);
        }
        if (from.equals("load")) {
            HashMap<String, String> mapLoad = new HashMap<>();
            mapLoad.put("type", "load");
            arrayList.add(mapLoad);
            adapter.notifyItemInserted(arrayList.size() - 1);
        } else progressBar.setVisibility(View.VISIBLE);
        if (publisherFilterList != null) publisherFilterList.clear();
        if (publisherIDsList != null) publisherIDsList.clear();
        if (authorFilterList != null) authorFilterList.clear();
        if (authorIDsList != null) authorIDsList.clear();
        all = 0;
        count = 0;
        mapInfoFinal = null;
        pages = 0;
        try {
            (new Retrofit.Builder().addConverterFactory(ScalarsConverterFactory.create())
                    .baseUrl(/*baseURL*/baseURLs[currenSitetoSearch]).client(new OkHttpClient().newBuilder().build()).build())
                    .create(ApiService.class).getPrices(searchURL).enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Document doc = Jsoup.parse(response.body());
                        String s;
                        if (currenSitetoSearch == 2) {
                            s = doc.select("a.button.button_pager.button_pager_last").attr("href");
                            try {
                                if (s != null || !s.isEmpty()) {
                                    int start = StringUtils.indexOf(s, "page");
                                    int end;
                                    if (s.contains("prpm")) {
                                        end = StringUtils.indexOf(s, "&prpm");
                                        pages = Integer.parseInt(StringUtils.replace(StringUtils.substring(s, start, end), "page=", ""));
                                    } else
                                        pages = Integer.parseInt(StringUtils.replace(StringUtils.substring(s, start), "page=", ""));
                                }
                            } catch (NumberFormatException e) {
                                pages = search;
                                e.printStackTrace();
                            }
                        } else if (currenSitetoSearch == 0) {
                            s = doc.select("div.Ks_ContentBlokTitle").text();
                            try {
                                int start = StringUtils.indexOf(s, "kelimesi için (");
                                int end = StringUtils.indexOf(s, ") sonuç bulundu");
                                String substring = StringUtils.substring(s, start, end);
                                int kitapsecPages = Integer.parseInt(StringUtils.replace(substring, "kelimesi için (", ""));
                                pages = (int) Math.ceil((double) kitapsecPages / 56);
                            } catch (NumberFormatException e) {
                                pages = search;
                                e.printStackTrace();
                            }
                        } else if (currenSitetoSearch == 1) {
                            s = doc.select("div.pagination > div.results").text();
                            try {
                                int start = StringUtils.indexOf(s, "(");
                                int end = StringUtils.indexOf(s, " Sayfa");
                                pages = Integer.parseInt(StringUtils.replace(StringUtils.substring(s, start, end), "(", ""));
                            } catch (NumberFormatException e) {
                                pages = search;
                                e.printStackTrace();
                            }
                        }

                        if (tab0selected && doc.select("div.Ks_UyariDiv").text().contains("bulunamadı") ||
                                tab1selected && doc.select("div.box.no-padding > div").text().contains("bulamadık") ||
                                tab2selected && doc.select("div.no_product_found").text().contains("bulunamadı")) {
                            listView.setBackground(AppCompatResources.getDrawable(context, R.drawable.no_results));
                            Toast.makeText(context, " ¯\\_(ツ)_/¯ :  Hiçbir sonuç bulunamadı.", Toast.LENGTH_SHORT).show();
                            Toast.makeText(context, "Diğer sitelerdeki sonuçlara bakabilir ya da ISBN ile aramayı deneyebilirsiniz.", Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);
                        } else {
                            if (tab0selected && doc.select("div.Ks_UyariDiv").text().contains("bulunamadı")) {
                                listView.setBackground(AppCompatResources.getDrawable(context, R.drawable.no_results));
                            }
                            if (tab1selected && doc.select("div.box.no-padding > div").text().contains("bulamadık")) {
                                listView.setBackground(AppCompatResources.getDrawable(context, R.drawable.no_results));
                            }
                            if (tab2selected && doc.select("div.no_product_found").text().contains("bulunamadı")) {
                                listView.setBackground(AppCompatResources.getDrawable(context, R.drawable.no_results));
                            }
                            adapter.setMoreDataAvailable(pages != 0 && pages != search);
                            if (mQuery != null && mQuery.matches("\\d+(?:\\.\\d+)?")) {
                                try {
                                    if(doc.select(context.getResources().getStringArray(R.array.queriesNoResult)[currenSitetoSearch]).text().contains("bulunamadı") ||
                                            doc.select(context.getResources().getStringArray(R.array.queriesNoResult)[currenSitetoSearch]).text().contains("bulamadık")) {
                                        listView.setBackground(AppCompatResources.getDrawable(context, R.drawable.no_results));
                                        progressBar.setVisibility(View.GONE);
                                        if (tab0selected && currenSitetoSearch == 0 ||
                                                tab1selected && currenSitetoSearch == 1 ||
                                                tab2selected && currenSitetoSearch == 2 ) {
                                            Toast.makeText(context, " ¯\\_(ツ)_/¯ :  Hiçbir sonuç bulunamadı.", Toast.LENGTH_SHORT).show();
                                            Toast.makeText(context, "Diğer sitelerdeki sonuçlara bakabilirsiniz.", Toast.LENGTH_LONG).show();
                                        }
                                    } else {
                                        mapInfoFinal = new HashMap<>();
                                        String name = doc.select(context.getResources().getStringArray(R.array.queriesDigitName)[currenSitetoSearch]).first().text();
                                        String author = doc.select(context.getResources().getStringArray(R.array.queriesDigitAuthor)[currenSitetoSearch]).first().text();
                                        String publisher = doc.select(context.getResources().getStringArray(R.array.queriesDigitPublisher)[currenSitetoSearch]).first().text();
                                        String cover = doc.select(context.getResources().getStringArray(R.array.queriesDigitCover)[currenSitetoSearch]).first()
                                                .attr(context.getResources().getStringArray(R.array.queriesDigitCoverAttr)[currenSitetoSearch]);
                                        String individual = doc.select(context.getResources().getStringArray(R.array.queriesDigitIndividual)[currenSitetoSearch]).first()
                                                .attr(context.getResources().getStringArray(R.array.queriesDigitIndividualAttr)[currenSitetoSearch]);
                                        mapInfoFinal.put("name", StringUtils.replace(StringUtils.replace(name, author, ""), publisher, ""));
                                        mapInfoFinal.put("author", author);
                                        mapInfoFinal.put("publisher", StringUtils.replace(publisher, ",",""));
                                        if (currenSitetoSearch == 0) mapInfoFinal.put("cover", StringUtils.join("https:", cover));
                                        else mapInfoFinal.put("cover", cover);
                                        mapInfoFinal.put("individual", individual);
                                        arrayList.add(mapInfoFinal);
                                        check("digit", adapter, adapter2, adapter3, filterLayout, filterLine, progressBar, listView, publisherFilter, authorFilter);
                                    }
                                } catch (Resources.NotFoundException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                if(currenSitetoSearch == 0) {
                                    String table = doc.select("div.Ks_ContentSag > div > div > table > tbody > tr:nth-child(2) > td").text();
                                    String publishers = StringUtils.substring(table, StringUtils.indexOf(table, "{\"dizi\""), StringUtils.indexOf(table, "{\"alfaDuz\""));
                                    ArrayList<ArrayList<String>> arrayListP = new ArrayList<>();
                                    ArrayList<ArrayList<String>> arrayListA = new ArrayList<>();
                                    try {
                                        JSONArray obj = new JSONObject(publishers).getJSONArray("alfaDuz");
                                        for (int i = 0; i<obj.length(); i++) {
                                            ArrayList<String> inner = new ArrayList<>();
                                            inner.add(obj.getJSONObject(i).getString("KatAdi"));
                                            inner.add(obj.getJSONObject(i).getString("KatID"));
                                            inner.add(obj.getJSONObject(i).getString("Toplam"));
                                            arrayListP.add(inner);
                                        }
                                        sortNumeric(arrayListP);
                                        for(int j = 0; j<arrayListP.size(); j++) {
                                            publisherIDsList.add(arrayListP.get(j).get(1));
                                            publisherFilterList.add(arrayListP.get(j).get(0) + " (" + arrayListP.get(j).get(2) + ")");
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    try {
                                        JSONArray obj = new JSONObject(publishers).getJSONArray("yzalfaDuz");
                                        for (int i = 0; i<obj.length(); i++) {
                                            ArrayList<String> inner = new ArrayList<>();
                                            inner.add(obj.getJSONObject(i).getString("KatAdi"));
                                            inner.add(obj.getJSONObject(i).getString("KatID"));
                                            inner.add(obj.getJSONObject(i).getString("Toplam"));
                                            arrayListA.add(inner);
                                        }
                                        sortNumeric(arrayListA);
                                        for(int i = 0; i<arrayListA.size(); i++) {
                                            authorIDsList.add(arrayListA.get(i).get(1));
                                            authorFilterList.add(arrayListA.get(i).get(0) + " (" + arrayListA.get(i).get(2) + ")");
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                } else if (currenSitetoSearch == 1) {
                                    for (Element table : doc.select("#publisher-filters-div")) {
                                        for (Element row : table.select("#publisher-filters-div > div.row > span.filter-item")) {
                                            publisherIDsList.add(StringUtils.replace(StringUtils.replace(row.attr("onclick"), "filterSelected('publisher', '", ""), "');", ""));
                                            publisherFilterList.add(row.text());
                                        }
                                    }

                                    for (Element table : doc.select("#manufacturer-filters-div")) {
                                        for (Element row : table.select("#manufacturer-filters-div > div.row > span.filter-item")) {
                                            authorIDsList.add(StringUtils.replace(StringUtils.replace(row.attr("onclick"), "filterSelected('manufacturer', '", ""), "');", ""));
                                            authorFilterList.add(row.text());
                                        }

                                    }
                                } else if (currenSitetoSearch == 2) {
                                    for (Element table : doc.select(context.getResources().getStringArray(R.array.queriesPublisherIDTable)[currenSitetoSearch])) {
                                        for (Element row : table.select(context.getResources().getStringArray(R.array.queriesPublisherIDall)[currenSitetoSearch])) {
                                            publisherIDsList.add(row.select(context.getResources().getStringArray(R.array.queriesPublisherIDs)[currenSitetoSearch])
                                                    .attr(context.getResources().getStringArray(R.array.queriesPublisherIDattr)[currenSitetoSearch]));
                                        }
                                    }
                                    for (Element table : doc.select(context.getResources().getStringArray(R.array.queriesAuthorIDTable)[currenSitetoSearch])) {
                                        for (Element row : table.select(context.getResources().getStringArray(R.array.queriesAuthorIDall)[currenSitetoSearch])) {
                                            authorIDsList.add(row.select(context.getResources().getStringArray(R.array.queriesAuthorIDs)[currenSitetoSearch])
                                                    .attr(context.getResources().getStringArray(R.array.queriesAuthorIDattr)[currenSitetoSearch]));
                                        }
                                    }
                                    for (Element table : doc.select(context.getResources().getStringArray(R.array.queriesPublisherFilterTable)[currenSitetoSearch]))
                                        for (Element row : table.select(context.getResources().getStringArray(R.array.queriesPublisherFilterall)[currenSitetoSearch]))
                                            publisherFilterList.add(row.select(context.getResources().getStringArray(R.array.queriesPublisherFilters)[currenSitetoSearch]).text());
                                    for (Element table : doc.select(context.getResources().getStringArray(R.array.queriesAuthorFilterTable)[currenSitetoSearch]))
                                        for (Element row : table.select(context.getResources().getStringArray(R.array.queriesAuthorFilterall)[currenSitetoSearch]))
                                            authorFilterList.add(row.select(context.getResources().getStringArray(R.array.queriesAuthorFilters)[currenSitetoSearch]).text());
                                }
                                for (Element table : doc.select(context.getResources().getStringArray(R.array.queriesTable)[currenSitetoSearch])) {
                                    all = table.select(context.getResources().getStringArray(R.array.queriesAll)[currenSitetoSearch]).size();
                                    for (Element row : table.select(context.getResources().getStringArray(R.array.queriesAll)[currenSitetoSearch])) {
                                        mapInfoFinal = new HashMap<>();
                                        String name = row.select(context.getResources().getStringArray(R.array.queriesName)[currenSitetoSearch]).text();
                                        String author = row.select(context.getResources().getStringArray(R.array.queriesAuthor)[currenSitetoSearch]).text();
                                        String publisher = row.select(context.getResources().getStringArray(R.array.queriesPublisher)[currenSitetoSearch]).text();
                                        String cover = row.select(
                                                context.getResources().getStringArray(R.array.queriesCover)[currenSitetoSearch])
                                                .attr(context.getResources().getStringArray(R.array.queriesCoverHref)[currenSitetoSearch]);
                                        if (!mapInfoFinal.containsValue(name)
                                                && !mapInfoFinal.containsValue(author)
                                                && !mapInfoFinal.containsValue(publisher)) {
                                            mapInfoFinal.put("name", StringUtils.replace(StringUtils.replace(name, author, ""), publisher, ""));
                                            mapInfoFinal.put("author", author);
                                            mapInfoFinal.put("publisher", StringUtils.replace(publisher, ",",""));
                                            if (currenSitetoSearch == 0) mapInfoFinal.put("cover", StringUtils.join("https:", cover));
                                            else mapInfoFinal.put("cover", cover);
                                            String individual = row.select(
                                                    context.getResources().getStringArray(R.array.queriesIndividual)[currenSitetoSearch])
                                                    .attr(context.getResources().getStringArray(R.array.queriesIndividualHref)[currenSitetoSearch]);
                                            int ix = individual.indexOf("&filter_name=");
                                            if (ix != -1) mapInfoFinal.put("individual", StringUtils.substring(individual, 0, ix));
                                            else mapInfoFinal.put("individual", individual);
                                            if (from.equals("load"))
                                                arrayListSpare.add(mapInfoFinal);
                                            else arrayList.add(mapInfoFinal);
                                        }
                                        check("load", adapter, adapter2, adapter3, filterLayout, filterLine, progressBar, listView, publisherFilter, authorFilter);
                                    }
                                }
                                if (from.equals("load")) {
                                    arrayList.remove(arrayList.size() - 1);
                                    arrayList.addAll(arrayListSpare);
                                    check("load", adapter, adapter2, adapter3, filterLayout, filterLine, progressBar, listView, publisherFilter, authorFilter);
                                    arrayListSpare.clear();
                                }
                                if (search == pages) {
                                    adapter.setMoreDataAvailable(false);
                                    if (tab0selected && currenSitetoSearch ==0 ||
                                            tab1selected && currenSitetoSearch ==1 ||
                                            tab2selected && currenSitetoSearch ==2 ) Toast.makeText(context, "Tüm sonuçlar yüklendi.", Toast.LENGTH_LONG).show();
                                } else adapter.setMoreDataAvailable(true);
                            }
                            if (mapInfoFinal == null) {
                                if (tab0selected && currenSitetoSearch ==0 ||
                                        tab1selected && currenSitetoSearch ==1 ||
                                        tab2selected && currenSitetoSearch ==2 ) {
                                    Toast.makeText(context, " ¯\\_(ツ)_/¯ :  Hiçbir sonuç bulunamadı.", Toast.LENGTH_SHORT).show();
                                    Toast.makeText(context, "Diğer sitelerdeki sonuçlara bakabilir ya da ISBN ile aramayı deneyebilirsiniz.", Toast.LENGTH_LONG).show();
                                }
                                listView.setBackground(AppCompatResources.getDrawable(context, R.drawable.no_results));
                                progressBar.setVisibility(View.GONE);
                            }
                        }
                    } else {
                        Toast.makeText(context, "ಠ_ಠ : Lütfen internet bağlantınızı kontrol ediniz ve yeniden deneyiniz.", Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.GONE);
                    }
                }
                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                    progressBar.setVisibility(View.GONE);
                    listView.setBackground(AppCompatResources.getDrawable(context, R.drawable.error));
                }
            });
        } catch (Exception e) {
            listView.setBackground(AppCompatResources.getDrawable(context, R.drawable.error));
            Toast.makeText(context, "ಠ_ಠ : Lütfen internet bağlantınızı kontrol ediniz ve yeniden deneyiniz.", Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.GONE);
            e.printStackTrace();
        }
    }

    public static void sortNumeric(ArrayList<ArrayList<String>> arrayList) {
        try {
            Collections.sort(arrayList, new Comparator<ArrayList<String>>() {
                @Override
                public int compare(ArrayList<String> o1, ArrayList<String> o2) { return extractInt(o2.get(2).toLowerCase(new Locale("tr", "TR"))) - extractInt(o1.get(2).toLowerCase(new Locale("tr", "TR"))); }
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

    private static void check(String from, BookFinalAdapter adapter,
                              BookFinalAdapter adapter2, BookFinalAdapter adapter3,
                              LinearLayout filterLayout,
                              View filterLine, ProgressBar progressBar,
                              RecyclerView listView, RecyclerView publisherFilter, RecyclerView authorFilter) {
        switch (from) {
            case "load":
            case "text":
                count += 1;
                if (count == all) {
                    if (all != 1) {
                        if(filterLine != null) filterLine.setVisibility(View.VISIBLE);
                        if(filterLayout != null) filterLayout.setVisibility(View.VISIBLE);
                    }
                    listView.getRecycledViewPool().clear();
                    adapter.notifyDataChanged();
                    if (adapter2 != null) {
                        publisherFilter.getRecycledViewPool().clear();
                        adapter2.notifyDataChanged();
                    }
                    if (adapter3 != null) {
                        authorFilter.getRecycledViewPool().clear();
                        adapter3.notifyDataChanged();
                    }
                    progressBar.setVisibility(View.GONE);
                    count = 0;
                }
                break;
            default:
                adapter.notifyDataChanged();
                progressBar.setVisibility(View.GONE);
                break;
        }
    }
}
