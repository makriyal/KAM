package com.muniryenigul.kam.activities;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Build;
import android.os.Bundle;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import com.muniryenigul.kam.MainActivity;
import com.muniryenigul.kam.R;
//import com.muniryenigul.kam.ers.AlarmReceiver;
import com.muniryenigul.kam.utils.Utils;
import com.muniryenigul.kam.ers.FavouriteAdapter;
import com.muniryenigul.kam.ers.RecyclerTouchListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import static com.muniryenigul.kam.MainActivity.detect;
import static com.muniryenigul.kam.MainActivity.favList;
public class Settings2Activity extends AppCompatActivity {
    private String[] sitesAddresses, sitesNames;
    private boolean[] checkedItems;
    private ArrayList<String> arrayForSelectedSites, arrayList;
    private boolean none=true, firstLoad=true;
    private Calendar calendar;
    private int timeHour, timeMinute, mYear, mMonth, presentDay, choosenDay;
    private FavouriteAdapter adapter;
    private Dialog dialog;
    private String string;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings2);
        string = getIntent().getStringExtra("from");
        calendar = Calendar.getInstance();
        timeHour = calendar.get(Calendar.HOUR_OF_DAY);
        timeMinute = calendar.get(Calendar.MINUTE);
        mYear = calendar.get(Calendar.YEAR);
        mMonth = calendar.get(Calendar.MONTH);
        presentDay = calendar.get(Calendar.DAY_OF_MONTH);
        choosenDay = calendar.get(Calendar.DAY_OF_MONTH);
        arrayList = new ArrayList<>();
        arrayList.add("Hangi siteler kıyaslansın?");
        /*arrayList.add("Fiyatlar kontrol edilsin mi?");
        arrayList.add("Kontrol sıklığı ne olsun?");
        arrayList.add("Ana sayfadaki kategorilerde kaç kitap gösterilsin?");*/
        adapter = new FavouriteAdapter(Settings2Activity.this,arrayList,"control");
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
    /*public void setAlarm(String from) {
        detect.edit().putBoolean("once_a_day", false).apply();
        detect.edit().putBoolean("once_in_three_days", false).apply();
        detect.edit().putBoolean("once_a_week", false).apply();
        detect.edit().putBoolean(from, true).apply();
        detect.edit().putBoolean("userSet", true).apply();
        AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(Settings2Activity.this, AlarmReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(Settings2Activity.this, 0, intent, 0);
        if (alarmMgr != null) alarmMgr.cancel(alarmIntent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && detect.getBoolean("userChoice", true)) {
            Utils.cancelJob(Settings2Activity.this);
            Date dat  = new Date();
            Calendar cal_now = Calendar.getInstance();
            Calendar cal = Calendar.getInstance();
            cal_now.setTime(dat);
            cal.setTimeInMillis(System.currentTimeMillis());
            detect.edit().putBoolean("control", true).apply();
            cal.set(Calendar.HOUR_OF_DAY, timeHour);
            cal.set(Calendar.MINUTE, timeMinute);
            cal.set(Calendar.SECOND, 10);
            int msSet = timeHour * 60 * 60 * 1000 + timeMinute * 60 * 1000 + 10 * 1000;
            int triggerAt;
            int msNow = dat.getHours() * 60 * 60 * 1000 + dat.getMinutes() * 60 * 1000 + dat.getSeconds() * 1000;
            if(cal.before(cal_now)) {
                if(from.equals("once_a_day")) triggerAt = 86400000 + msSet - msNow;
                else if(from.equals("once_in_three_days")) triggerAt = 3*86400000 + msSet - msNow;
                else triggerAt = (choosenDay-presentDay)*86400000 + msSet - msNow;
            } else {
                if(from.equals("once_a_day")) triggerAt = 86400000 - msSet + msNow;
                else if(from.equals("once_in_three_days")) triggerAt = 3*86400000 - msSet + msNow;
                else triggerAt = (choosenDay-presentDay)*86400000 - msSet + msNow;
            }
            detect.edit().putInt("choosenDay",choosenDay).apply();
            detect.edit().putInt("presentDay",presentDay).apply();
            if (alarmMgr != null) alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + triggerAt*//*60 * 1000*//*, alarmIntent);
        }
    }*/
    /*public void alarm() {
        @SuppressLint("InflateParams") View alertLayout = getLayoutInflater().inflate(R.layout.radio_group, null);
        final RadioButton radioButtonOnceADay = alertLayout.findViewById(R.id.radioOneDay);
        final RadioButton radioButtonOnceInThreeDays = alertLayout.findViewById(R.id.radioThreeDays);
        final RadioButton radioButtonOnceAWeek = alertLayout.findViewById(R.id.radioOneWeek);
        if (detect.getBoolean("once_a_day", false)) radioButtonOnceADay.setChecked(true);
        else if (detect.getBoolean("once_in_three_days", true)) radioButtonOnceInThreeDays.setChecked(true);
        else if (detect.getBoolean("once_a_week", true)) radioButtonOnceAWeek.setChecked(true);
        final RadioGroup radioPeriod = alertLayout.findViewById(R.id.radioPeriod);
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(Settings2Activity.this);
        builder.setCancelable(true).setView(alertLayout);
        dialog = builder.create();
        dialog.show();
        radioPeriod.setOnCheckedChangeListener((group, checkedId) -> {
            if(checkedId == R.id.radioOneDay) { timer("once_a_day");
            } else if(checkedId == R.id.radioThreeDays) { timer("once_in_three_days");
            } else daySet();
            dialog.dismiss();
        });
    }*/
    /*private void daySet() {
        DatePickerDialog.OnDateSetListener listener = (view, year, month, dayOfMonth) -> {
            mYear = year;
            mMonth = month;
            presentDay = dayOfMonth;
            timer("once_a_week");
        };
        final DatePickerDialog datePickerDialog = new DatePickerDialog(Settings2Activity.this, listener, mYear, mMonth, presentDay);
        DatePicker datePicker = datePickerDialog.getDatePicker();
        calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, +7);
        long oneMonthAhead = calendar.getTimeInMillis();
        datePicker.setMaxDate(oneMonthAhead);
        datePicker.setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }*/
    /*private void timer(String from) {
        TimePickerDialog.OnTimeSetListener listener = (view, hourOfDay, minute) -> {
            timeHour = hourOfDay;
            timeMinute = minute;
            //setAlarm(from);
        };
        TimePickerDialog tmd = new TimePickerDialog(Settings2Activity.this, listener, timeHour, timeMinute, true);
        tmd.show();
    }*/
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
                    for (int i = 0; i < checkedItems.length; i++) { checkedItems[i] = true; }
                    none=true;
                } else {
                    arrayForSelectedSites.clear();
                    for (int i = 0; i < checkedItems.length; i++) { checkedItems[i] = false; }
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
        } /*else if(position == 2 && favList.size() > 0 && detect.getBoolean("control", false)) alarm();
        else if(position == 1 && favList.size() > 0) {
            if (detect.getBoolean("control", false)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Utils.cancelJob(Settings2Activity.this);
                    detect.edit().putBoolean("control", false).apply();
                    detect.edit().putBoolean("userChoice", false).apply();
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (!detect.getBoolean("userSet", false)) Utils.scheduleJob(getApplicationContext()*//*, 25, 61, 61, 3*//*);
                    else Utils.scheduleJob(getApplicationContext()*//*, detect.getInt("hour",25), detect.getInt("minute",61), 10, detect.getInt("days",3)*//*);
                    detect.edit().putBoolean("control", true).apply();
                    detect.edit().putBoolean("userChoice", true).apply();
                }
            }
            adapter.refreshAdapter();
        } else if(!detect.getBoolean("userChoice", true) && position != 3) Toast.makeText(this, "Evvela fiyat alarmını çalıştırınız -üstteki düğme-.", Toast.LENGTH_SHORT).show();
        else if(position != 3) Toast.makeText(this, "Evvela Favoriler listesine kitap ekleyiniz.", Toast.LENGTH_SHORT).show();*/
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