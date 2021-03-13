package com.muniryenigul.kam.ers;
import android.annotation.SuppressLint;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.content.res.Resources;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import com.muniryenigul.kam.R;
import java.util.ArrayList;
import static com.muniryenigul.kam.MainActivity.detect;
import static com.muniryenigul.kam.MainActivity.favList;

public class FavouriteAdapter extends RecyclerView.Adapter<FavouriteAdapter.MyViewHolder> {
    private final String from;
    private final ArrayList<String> searchedItems;
    private Context context;
    private ArrayAdapter<String> spinnerAdapter;

    public FavouriteAdapter(ArrayList<String> arrayList, String from) {
        this.searchedItems = arrayList;
        this.from = from;
    }

    public FavouriteAdapter(Context context, ArrayList<String> arrayList, String from) {
        this.searchedItems = arrayList;
        this.from = from;
        this.context = context;
        String[] limitItems = new String[]{"5", "10", "20"};
        spinnerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, limitItems);
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        private final ImageView historyImage;
        private final TextView siteText;
        private final ProgressBar progressBar;
        private final SwitchCompat switchControl;
        private final CardView cardView;
        private final Spinner limitSpinner;

        MyViewHolder(View view) {
            super(view);
            historyImage = view.findViewById(R.id.historyImage);
            siteText = view.findViewById(R.id.searchedText);
            progressBar = view.findViewById(R.id.progressBar);
            switchControl = view.findViewById(R.id.switchControl);
            cardView = view.findViewById(R.id.cardView);
            limitSpinner = view.findViewById(R.id.limitSpinner);
        }
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new MyViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item, null));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder customViewHolder, int i) {
        try {
            customViewHolder.siteText.setRotation(0);
            customViewHolder.siteText.setText(searchedItems.get(i));
            customViewHolder.siteText.setVisibility(View.VISIBLE);
            customViewHolder.historyImage.setVisibility(View.GONE);
            customViewHolder.progressBar.setVisibility(View.INVISIBLE);
            if(from!=null&&from.equals("history")) {
                customViewHolder.limitSpinner.setVisibility(View.GONE);
                customViewHolder.historyImage.setVisibility(View.VISIBLE);
                customViewHolder.switchControl.setVisibility(View.GONE);
            } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if(from!=null&&from.equals("control") && i==1) {
                    customViewHolder.limitSpinner.setVisibility(View.GONE);
                    customViewHolder.switchControl.setVisibility(View.VISIBLE);
                    if(favList.size() > 0  && detect.getBoolean("control", false)) {
                        customViewHolder.switchControl.setChecked(true);
                        customViewHolder.switchControl.setEnabled(true);
                        customViewHolder.siteText.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
                    } else {
                        customViewHolder.switchControl.setChecked(false);
                        customViewHolder.switchControl.setEnabled(false);
                        customViewHolder.siteText.setTextColor(context.getResources().getColor(R.color.text_color));
                    }
                } else if(from!=null&&from.equals("control") && i==2) {
                    customViewHolder.limitSpinner.setVisibility(View.GONE);
                    customViewHolder.switchControl.setVisibility(View.GONE);
                    if(favList.size() > 0 && detect.getBoolean("control", false/*true*/)) customViewHolder.siteText.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
                    else customViewHolder.siteText.setTextColor(context.getResources().getColor(R.color.text_color));
                } else if(from!=null&&from.equals("control") && i==3) {
                    customViewHolder.switchControl.setVisibility(View.GONE);
                    customViewHolder.limitSpinner.setVisibility(View.VISIBLE);
                    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    customViewHolder.limitSpinner.setAdapter(spinnerAdapter);
                    customViewHolder.limitSpinner.setSelection(detect.getInt("limit", 0));
                    customViewHolder.limitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            detect.edit().putInt("limit", position).apply();
                            detect.edit().putInt("selectedLimit", Integer.parseInt(parent.getItemAtPosition(position).toString())).apply();
                        }
                        @Override
                        public void onNothingSelected(AdapterView<?> parent) { }
                    });
                } else {
                    customViewHolder.switchControl.setVisibility(View.GONE);
                    customViewHolder.limitSpinner.setVisibility(View.GONE);
                }
            } else customViewHolder.cardView.setVisibility(View.GONE);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return searchedItems.size();
    }
}