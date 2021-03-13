package com.muniryenigul.kam.ers;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.muniryenigul.kam.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import static com.muniryenigul.kam.MainActivity.filteredList;

public class BookFinalAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int TYPE_ITEM = 0;
    private final Context context;
    private ArrayList<HashMap<String, String>> feedItemList;
    private ArrayList<String> filter, filter2;
    private OnLoadMoreFinalListener loadMoreListener;
    private boolean isLoading = false, isMoreDataAvailable = true;
    private String type, str;

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

    public BookFinalAdapter(Context context, ArrayList<String> filter, ArrayList<String> filter2, String str) {
        this.filter = filter;
        this.filter2 = filter2;
        this.str = str;
        this.context = context;
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
        try {
            if (i == TYPE_ITEM) {
                if (str != null && str.equals("filter")) return new ItemHolder(inflater.inflate(R.layout.filter, viewGroup, false));
                else if (type != null && !type.equals("")) return new ItemHolder(inflater.inflate(R.layout.list_sugs, viewGroup, false));
                else return new ItemHolder(inflater.inflate(R.layout.list_items, viewGroup, false));
            } else return new LoadHolder(inflater.inflate(R.layout.progress_item, viewGroup, false));
        } catch (Exception e) {
            e.printStackTrace();
            return new LoadHolder(inflater.inflate(R.layout.progress_item, viewGroup, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        try {
            if (i >= getItemCount() - 1 && isMoreDataAvailable && !isLoading && loadMoreListener != null) {
                isLoading = true;
                loadMoreListener.onLoadMore();
            }
            if(str != null) {
                ((ItemHolder) viewHolder).filterName.setText(filter.get(i));
                ((ItemHolder) viewHolder).filterCheckBox.setChecked(filteredList.contains(filter2.get(i)));
            } else if (getItemViewType(i) == TYPE_ITEM) ((ItemHolder) viewHolder).bindData(feedItemList.get(i));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemViewType(int position) {
        try {
            if (feedItemList != null && feedItemList.get(position).get("type") != null && Objects.requireNonNull(feedItemList.get(position).get("type")).equals("load")) return 1;
            else return TYPE_ITEM;
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
    }

    @Override
    public int getItemCount() {
        if(str != null) return filter.size();
        else return feedItemList.size();
    }

    class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener, AdapterView.OnLongClickListener {
        private TextView filterName, name, author, publisher;
        private ImageView cover;
        private final String type;
        private ProgressBar progressBar;
        private CheckBox filterCheckBox;

        ItemHolder(View itemView) {
            super(itemView);
            this.type = BookFinalAdapter.this.type;
            String str = BookFinalAdapter.this.str;
            if(str != null) {
                this.filterName = itemView.findViewById(R.id.filterName);
                this.filterCheckBox = itemView.findViewById(R.id.filterCheckBox);
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
                try {
                    Picasso.get().load(resultp.get("cover")).error(R.drawable.ic_virus).into(cover, new Callback() {
                                @Override public void onSuccess() {
                                    progressBar.setVisibility(View.INVISIBLE);
                                }
                                @Override public void onError(Exception e) {
                                    progressBar.setVisibility(View.INVISIBLE);
                                    cover.setScaleType(ImageView.ScaleType.FIT_CENTER);
                                }
                            });
                } catch (Exception e) {
                    e.printStackTrace();
                    progressBar.setVisibility(View.INVISIBLE);
                }
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
                    try {
                        Picasso.get().load(resultp.get("cover")).fit().centerCrop().error(R.drawable.ic_virus).into(cover, new Callback() {
                                    @Override public void onSuccess() { progressBar.setVisibility(View.INVISIBLE); }
                                    @Override public void onError(Exception e) { }
                                });
                    } catch (Exception e) {
                        e.printStackTrace();
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                }
            }
        }

        @Override
        public void onClick(View v) { }

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