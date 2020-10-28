package com.muniryenigul.kam.ers;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.muniryenigul.kam.MainActivity;
import com.muniryenigul.kam.R;
import com.muniryenigul.kam.utils.Utils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import static com.muniryenigul.kam.MainActivity.filteredList;
public class BookFinalAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int TYPE_ITEM = 0;
    private Context context;
    private ArrayList<HashMap<String, String>> feedItemList, arrayList;
    private ArrayList<String> filter, filter2/*, filterList*/, publisherFilterList, publisherIDsList,authorFilterList,
            authorIDsList, searchedList;
    private OnLoadMoreFinalListener loadMoreListener;
    private boolean isLoading = false, isMoreDataAvailable = true;
    private String type, str, str2, mQuery;
    private HorizontalScrollView horizontalScrollView;
    private View filterLine;
    private ProgressBar progressBar;

    public interface OnLoadMoreFinalListener {
        void onLoadMore();
    }
    public void setLoadMoreListener(OnLoadMoreFinalListener loadMoreListener) {
        this.loadMoreListener = loadMoreListener;
    }
    public BookFinalAdapter(Context context, ArrayList<HashMap<String, String>> feedItemList) {
        this.feedItemList = feedItemList;
        this.context = context;
    }

    public BookFinalAdapter(Context context, ArrayList<String> filter, ArrayList<String> filter2,
                            String str, String str2/*, String mQuery,
                            ArrayList<HashMap<String, String>> arrayList,
                            ArrayList<String> publisherFilterList,
                            ArrayList<String> publisherIDsList,
                            ArrayList<String> authorFilterList,
                            ArrayList<String> authorIDsList,
                            HorizontalScrollView horizontalScrollView, View filterLine,
                            ArrayList<String> searchedList, ProgressBar progressBar*/) {

        this.filter = filter;
        this.filter2 = filter2;
        //this.filterList = filterList;
        this.str = str;
        this.str2 = str2;
        this.context = context;
        /*this.mQuery = mQuery;
        this.arrayList = arrayList;
        this.publisherFilterList = publisherFilterList;
        this.publisherIDsList = publisherIDsList;
        this.authorFilterList = authorFilterList;
        this.authorIDsList = authorIDsList;
        this.searchedList = searchedList;
        this.horizontalScrollView = horizontalScrollView;
        this.filterLine = filterLine;
        this.progressBar = progressBar;*/
    }

    public BookFinalAdapter(Context context, ArrayList<HashMap<String, String>> feedItemList, String type) {
        this.feedItemList = feedItemList;
        this.context = context;
        this.type = type;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (i == TYPE_ITEM) {
            if (str != null && str.equals("filter")) return new ItemHolder(inflater.inflate(R.layout.filter, viewGroup, false));
            else if (type != null && !type.equals("")) return new ItemHolder(inflater.inflate(R.layout.list_sugs, viewGroup, false));
            else return new ItemHolder(inflater.inflate(R.layout.list_items, viewGroup, false));
        } else return new LoadHolder(inflater.inflate(R.layout.progress_item, viewGroup, false));
    }
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        if (i >= getItemCount() - 1 && isMoreDataAvailable && !isLoading && loadMoreListener != null) {
            isLoading = true;
            loadMoreListener.onLoadMore();
        } /*else if(isLoading) Toast.makeText(context, "Tüm sonuçlar yüklendi.", Toast.LENGTH_LONG).show();*/
        if(str != null) {
            /*Log.d("filteredList",filteredList.toString());
            Log.d("filter",filter.toString());
            Log.d("filter2",filter2.toString());*/
            ((ItemHolder) viewHolder).filterName.setText(filter.get(i));
            if(!filteredList.contains(filter2.get(i))) {
                //Log.d("onBindViewHolder","!filterList.contains(filter.get(i))");
                ((ItemHolder) viewHolder).filterCheckBox.setChecked(false);
            }
            else {
                //Log.d("onBindViewHolder","!filterList.contains(filter.get(i))");
                ((ItemHolder) viewHolder).filterCheckBox.setChecked(true);
            }

        } else if (getItemViewType(i) == TYPE_ITEM) {
            //Log.d("onBindViewHolder","str = null");
            ((ItemHolder) viewHolder).bindData(feedItemList.get(i));
        }
    }
    @Override
    public int getItemViewType(int position) {
        if (feedItemList != null && feedItemList.get(position).get("type") != null && Objects.requireNonNull(feedItemList.get(position).get("type")).equals("load"))
            return 1;
        else return TYPE_ITEM;
    }
    @Override
    public int getItemCount() {
        if(str != null) return filter.size();
        else return feedItemList.size();
    }
    private void refreshAdapter() {

        synchronized(BookFinalAdapter.this){ BookFinalAdapter.this.notifyDataSetChanged(); }
    }
    class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener, AdapterView.OnLongClickListener {

        private TextView filterName, name, author, publisher;
        private ImageView cover;
        private String type, str;
        private ProgressBar progressBar;
        private CheckBox filterCheckBox;
        private LinearLayout filterLayout;

        ItemHolder(View itemView) {
            super(itemView);
            this.type = BookFinalAdapter.this.type;
            this.str = BookFinalAdapter.this.str;
            if(str != null) {
                this.filterName = itemView.findViewById(R.id.filterName);
                this.filterCheckBox = itemView.findViewById(R.id.filterCheckBox);
                this.filterLayout = itemView.findViewById(R.id.filterLayout);
                //this.filterCheckBox.setOnClickListener(this);
            } else if (type != null && !type.equals("")) {
                this.cover = itemView.findViewById(R.id.itemImage);
                this.progressBar = itemView.findViewById(R.id.progressBar);
            } else {
                this.cover = itemView.findViewById(R.id.coverImage);
                this.name = itemView.findViewById(R.id.nameText);
                this.author = itemView.findViewById(R.id.authorText);
                this.publisher = itemView.findViewById(R.id.publisherText);
                this.progressBar = itemView.findViewById(R.id.progressBar);
            }
        }

        void bindData(HashMap<String, String> resultp) {
            if (type != null && !type.equals("")) {
                Picasso.get().load(resultp.get("cover"))/*.fit().centerCrop()*/.error(R.drawable./*error*/ic_virus).into(cover, new Callback() {
                            @Override public void onSuccess() {
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                            @Override public void onError(Exception e) {
                                progressBar.setVisibility(View.INVISIBLE);
                                cover.setScaleType(ImageView.ScaleType.FIT_CENTER);
                            }
                        });
            } else {
                if (Objects.requireNonNull(resultp.get("author")).equals("Bulunamadı")) {
                    String string = "¯\\_(ツ)_/¯ : İlgili bir sonuç bulunamadı.";
                    Charset.forName("UTF-8").encode(string);
                    name.setText(string);
                    author.setVisibility(View.GONE);
                    publisher.setVisibility(View.GONE);
                    cover.setVisibility(View.GONE);
                } else if (Objects.requireNonNull(resultp.get("author")).equals("ಠ_ಠ  : Response Failure")
                        || Objects.requireNonNull(resultp.get("author")).equals("ಠ_ಠ  : Response Error")) {
                    String string = "ಠ_ಠ  : Bağlantı Hatası";
                    Charset.forName("UTF-8").encode(string);
                    name.setText(string);
                    author.setVisibility(View.GONE);
                    publisher.setVisibility(View.GONE);
                    cover.setVisibility(View.GONE);
                } else {
                    name.setText(resultp.get("name"));
                    author.setText(resultp.get("author"));
                    publisher.setText(resultp.get("publisher"));
                    Picasso.get().load(resultp.get("cover")).fit().centerCrop().error(R.drawable./*error*/ic_virus).into(cover, new Callback() {
                                @Override public void onSuccess() { progressBar.setVisibility(View.INVISIBLE); }
                                @Override public void onError(Exception e) { }
                            });
                }
            }
        }

        @Override
        public void onClick(View v) {
            /*Log.d("publisherFilterAdapter","onClick");
            if(!filteredList.contains(filter.get(getAdapterPosition()))) filteredList.add(filter.get(getAdapterPosition()));
            else filteredList.remove(filter.get(getAdapterPosition()));
            refreshAdapter();*/
        }

        @Override
        public boolean onLongClick(View v) {
            return false;
        }
    }
    static class LoadHolder extends RecyclerView.ViewHolder {
        LoadHolder(View itemView) {
            super(itemView);
        }
    }
    public void setMoreDataAvailable(boolean moreDataAvailable) {
        isMoreDataAvailable = moreDataAvailable;
    }
    public void notifyDataChanged() {
        notifyDataSetChanged();
        isLoading = false;
    }
}