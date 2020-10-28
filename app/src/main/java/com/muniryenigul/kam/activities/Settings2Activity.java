package com.muniryenigul.kam.activities;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.muniryenigul.kam.MainActivity;
import com.muniryenigul.kam.R;
import com.muniryenigul.kam.ers.FavouriteAdapter;
import com.muniryenigul.kam.ers.RecyclerTouchListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
public class Settings2Activity extends AppCompatActivity {
    private String[] sitesAddresses, sitesNames;
    private boolean[] checkedItems;
    private ArrayList<String> arrayForSelectedSites, arrayList;
    private boolean none=true, firstLoad=true;
    private String string;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings2);
        string = getIntent().getStringExtra("from");
        arrayList = new ArrayList<>();
        arrayList.add("Hangi siteler kıyaslansın?");
        FavouriteAdapter adapter = new FavouriteAdapter(Settings2Activity.this, arrayList, "control");
        RecyclerView recyclerView = findViewById(R.id.recView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(Settings2Activity.this));
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(adapter);
        sitesAddresses = getResources().getStringArray(R.array.listValues);
        sitesNames = getResources().getStringArray(R.array.listOptions);
        checkedItems = new boolean[sitesAddresses.length];
        arrayForSelectedSites = new ArrayList<>();
        if (string != null && !string.equals("drawer")) try { click(0); } catch (Exception e) { e.printStackTrace(); }
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(Settings2Activity.this, recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                try { click(position);
                } catch (Exception e) {
                    Toast.makeText(Settings2Activity.this, e.toString(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
            @Override
            public void onLongClick(View view, int position) { }
        }));
    }
    private void click(int position) {
        if(position==0){
            none=true;
            loadArrayList(arrayForSelectedSites, Settings2Activity.this);
            if(firstLoad) loadArray(checkedItems, Settings2Activity.this);
            for (boolean what : checkedItems) {
                if (!what) { none = false; break; }
            }
            AlertDialog.Builder mBuilder = new AlertDialog.Builder(Settings2Activity.this);
            mBuilder.setTitle(arrayList.get(position));
            mBuilder.setCancelable(true);
            mBuilder.setMultiChoiceItems(sitesNames, checkedItems, (dialogInterface, pos, isChecked) -> {
                if (isChecked) {if (!arrayForSelectedSites.contains(sitesAddresses[pos])) arrayForSelectedSites.add(sitesAddresses[pos]);}
                else arrayForSelectedSites.remove(sitesAddresses[pos]);
            });
            mBuilder.setPositiveButton("Tamam",(dialogInterface, which)-> {});
            mBuilder.setNegativeButton("Vazgeç",(dialogInterface, which)-> {});
            mBuilder.setNeutralButton("Tümünü Seç/Bırak", (dialogInterface, which) -> {
                if (!none) {
                    Collections.addAll(arrayForSelectedSites, sitesAddresses);
                    Arrays.fill(checkedItems, true);
                    none=true;
                } else {
                    arrayForSelectedSites.clear();
                    Arrays.fill(checkedItems, false);
                    none=false;
                }
                try { click(0);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(Settings2Activity.this, e.toString(), Toast.LENGTH_SHORT).show();
                }
            });
            AlertDialog mDialog = mBuilder.create();
            mDialog.setCancelable(false);
            mDialog.show();
            Button okButton = mDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            Button cancelButton = mDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
            okButton.setOnClickListener(new CustomListener(mDialog, "OK"));
            cancelButton.setOnClickListener(new CustomListener(mDialog, "Cancel"));
        }
    }
    private class CustomListener implements View.OnClickListener {
        private final Dialog dialog;
        private final String from;
        CustomListener(Dialog dialog, String string) {
            this.dialog = dialog;
            this.from = string;
        }
        @Override
        public void onClick(View v) {
            switch (from) {
                case "OK":
                    boolean result = false;
                    for (boolean what : checkedItems) { if (what) { result = true; break; } }
                    if (result) {
                        storeArray(checkedItems, Settings2Activity.this);
                        storeArrayList(arrayForSelectedSites, Settings2Activity.this);
                        dialog.dismiss();
                        check();
                    } else Toast.makeText(Settings2Activity.this, "Lütfen site seçin ya da çıkmak için Vazgeç'e dokunun.", Toast.LENGTH_SHORT).show();
                    break;
                case "Cancel":
                    dialog.dismiss();
                    firstLoad=true;
                    if(getSharedPreferences("preference", 0).getInt("checkedItems" + "_size", 0)==0) for (int i = 0; i < sitesAddresses.length; i++) { checkedItems[i] = false; }
                    if (!string.equals("drawer")) finish();
                    break;
            }
        }
    }
    private void check() {
        if(string != null) {
            if (string.equals("ShowMore") ||string.equals("ContentTouch") || string.equals("detail"))  finish();
            else if (!string.equals("drawer"))  {
                Intent intent=new Intent(Settings2Activity.this, MainActivity.class);
                intent.putExtra("from", string);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        }
    }
    private void storeArray(boolean[] array, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences("preference", 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("checkedItems" + "_size", array.length);
        for (int i = 0; i < array.length; i++) editor.putBoolean("checkedItems" + "_" + i, array[i]);
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
    private void loadArray(boolean[] array, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences("preference", 0);
        int size = prefs.getInt("checkedItems" + "_size", 0);
        for (int i = 0; i < size; i++) { array[i] = prefs.getBoolean("checkedItems" + "_" + i, false); }
        firstLoad=false;
    }
    private void loadArrayList(ArrayList<String> arrayList, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences("preference2", 0);
        int size = prefs.getInt("arrayForSelectedSites" + "_size", 0);
        for (int i = 0; i < size; i++) if(!arrayList.contains(prefs.getString("arrayForSelectedSites" + "_" + i, " "))) arrayList.add(prefs.getString("arrayForSelectedSites" + "_" + i, " "));
    }
}