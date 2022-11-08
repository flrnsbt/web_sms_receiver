package com.nalakstudio.smsreceive;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.viewpager.widget.ViewPager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.MessageQueue;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.appodeal.ads.Appodeal;
import com.appodeal.ads.Native;
import com.appodeal.ads.NativeAd;
import com.appodeal.ads.NativeCallbacks;
import com.appodeal.ads.api.App;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ybq.android.spinkit.sprite.Sprite;
import com.github.ybq.android.spinkit.style.CubeGrid;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.nalakstudio.smsreceive.Adapters.PhoneObjectAdapter;
import com.nalakstudio.smsreceive.CustomViews.CustomViewPager;
import com.nalakstudio.smsreceive.CustomViews.MyCustomDialog;
import com.nalakstudio.smsreceive.CustomViews.ViewPagerAdapter;
import com.nalakstudio.smsreceive.Fragments.Countries;
import com.nalakstudio.smsreceive.Fragments.Home;
import com.nalakstudio.smsreceive.Fragments.Settings;
import com.nalakstudio.smsreceive.Services.UpdatePhoneNumbersDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.jsoup.internal.StringUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements Countries.OnFragmentInteractionListener, Home.OnFragmentInteractionListener, Settings.OnFragmentInteractionListener{
    SpannableString s;
    public BottomNavigationView navigation;
    public CustomViewPager viewPager;
    public Map<String, PhoneObject> phoneObjectsMap = new HashMap<>();
    private List<PhoneObjectAdapter.PhoneObjectAdapterCallBack> phoneObjectAdapterCallBack = new ArrayList<>();
    public List<CountryComparator> countryList = new ArrayList<>();
    public Map<String,Boolean> websitesMap = new HashMap<>();
    TextView progress;
    File phoneNumbersDatabase;
    ViewPagerAdapter adapter;
    public Set<NativeAd> nativeAds = new HashSet<>();
    LinearLayout progressLinearLayout;
    int phoneNumberClicked =0;
    ObjectMapper objectMapper = new ObjectMapper();
    SharedPreferences sharedPreferences;



    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        return super.onCreateView(name, context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences("preferences", MODE_PRIVATE);
        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        ProgressBar progressBar = (ProgressBar)findViewById(R.id.spin_kit);
        Sprite cubeGrid = new CubeGrid();
        progressBar.setIndeterminateDrawable(cubeGrid);
        progress = findViewById(R.id.progress);
        progressLinearLayout = findViewById(R.id.progressLinearLayout);
        navigation.setVisibility(View.GONE);
        progressLinearLayout.setVisibility(View.VISIBLE);
        phoneNumbersDatabase = new File(getApplicationContext().getFilesDir(),"phone_number_database.json");
        viewPager = (CustomViewPager) findViewById(R.id.viewpager);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        adapter = new ViewPagerAdapter(MainActivity.this.getSupportFragmentManager());
        adapter.addFragment(new Home(), "Home");
        adapter.addFragment(new Countries(), "Countries");
        adapter.addFragment(new Settings(), "Settings");
        phoneObjectAdapterCallBack.add((PhoneObjectAdapter.PhoneObjectAdapterCallBack) adapter.getItem(0));
        phoneObjectAdapterCallBack.add((PhoneObjectAdapter.PhoneObjectAdapterCallBack) adapter.getItem(1));
        phoneObjectAdapterCallBack.add((PhoneObjectAdapter.PhoneObjectAdapterCallBack) adapter.getItem(2));
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                cleanMenuItem();
                MenuItem item = navigation.getMenu().getItem(position);
                item.getIcon().setColorFilter(getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
                s = new SpannableString(item.getTitle());
                s.setSpan(new ForegroundColorSpan(getColor(R.color.colorAccent)), 0, s.length(), 0);
                s.setSpan(new StyleSpan(Typeface.BOLD), 0, s.length(), 0);
                item.setTitle(s);
                item.setChecked(true);
                if (position==0 || position==2){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        Window window = getWindow();
                        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                        window.setStatusBarColor(Color.WHITE);
                    }
                }else{
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        Window window = getWindow();
                        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                        window.setStatusBarColor(getColor(R.color.colorAccent));
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        int size = navigation.getMenu().size();
        for (int i = 0; i < size; i++) {
            navigation.getMenu().getItem(i).getIcon().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
        }
        navigation.getMenu().getItem(0).getIcon().setColorFilter(getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
        s = new SpannableString(navigation.getMenu().getItem(0).getTitle());
        s.setSpan(new ForegroundColorSpan(getColor(R.color.colorAccent)), 0, s.length(), 0);
        s.setSpan(new StyleSpan(Typeface.BOLD), 0, s.length(), 0);
        navigation.getMenu().getItem(0).setTitle(s);
        loadDatabase();
        Intent intent = new Intent(MainActivity.this, UpdatePhoneNumbersDatabase.class);
        startService(intent);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        boolean consent=true;
//        if (user from eu){
//            consent=sharedPreferences.getBoolean("consentValue",false);
//        }
//        Appodeal.setTesting(true);
        Appodeal.setAutoCache(Appodeal.NATIVE, false);
        Appodeal.initialize(this, "99befee1b40bc1493ba867fc1a7c6929f947791ca1909fb9", Appodeal.INTERSTITIAL | Appodeal.NATIVE | Appodeal.BANNER_VIEW, consent);
        Appodeal.cache(MainActivity.this, Appodeal.NATIVE,3);
        final int[] n = {0};
        Appodeal.setNativeCallbacks(new NativeCallbacks() {
            @Override
            public void onNativeLoaded() {
                n[0]++;
                List<NativeAd> list = Appodeal.getNativeAds(3);
                    for (int j=0;j<list.size();j++){
                        Iterator<NativeAd> iterator = nativeAds.iterator();
                        if (nativeAds.size()>0){
                            while (iterator.hasNext()){
                                NativeAd nativeAd = iterator.next();
                                if (!nativeAd.getDescription().equals(list.get(j).getDescription())){
                                    nativeAds.add(list.get(j));
                                }
                            }
                        }else{
                            nativeAds.addAll(list);
                        }
                    }

                if (nativeAds.size()<5 && n[0]<20){
                            Appodeal.cache(MainActivity.this, Appodeal.NATIVE,3);
                }else{
                    phoneObjectAdapterCallBack.get(0).updateAdapter();
                    phoneObjectAdapterCallBack.get(1).updateAdapter();
                }
            }

            @Override
            public void onNativeFailedToLoad() {
            }

            @Override
            public void onNativeShown(NativeAd nativeAd) {

            }

            @Override
            public void onNativeShowFailed(NativeAd nativeAd) {

            }

            @Override
            public void onNativeClicked(NativeAd nativeAd) {

            }

            @Override
            public void onNativeExpired() {

            }
        });
        Appodeal.setRequiredNativeMediaAssetType(Native.MediaAssetType.ALL);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.countries:
                    viewPager.setCurrentItem(1);
                    return true;
                case R.id.home:
                    viewPager.setCurrentItem(0);
                    return true;
                case R.id.settings:
                    viewPager.setCurrentItem(2);
                    return true;
            }
            return false;
        }
    };
    public void cleanMenuItem() {
        int size = navigation.getMenu().size();
        for (int i = 0; i < size; i++) {
            s = new SpannableString(navigation.getMenu().getItem(i).getTitle());
            s.setSpan(new ForegroundColorSpan(Color.parseColor("#000000")), 0, s.length(), 0);
            s.setSpan(new StyleSpan(Typeface.NORMAL), 0, s.length(), 0);
            navigation.getMenu().getItem(i).setTitle(s);
            navigation.getMenu().getItem(i).getIcon().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    public void onBackPressed() {
        MyCustomDialog builder = new MyCustomDialog(this, "Exit Application", "Are you sure you want to leave?");
        final AlertDialog dialog = builder.setNegativeButton("No", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        }).create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTypeface(null, Typeface.BOLD);
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTypeface(null, Typeface.BOLD);
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
            }
        });

        dialog.show();
    }

    public static String loadJSONFromAsset(Context context, String filename) {
        String json = null;
        try {
            InputStream is = context.getAssets().open(filename);

            int size = is.available();

            byte[] buffer = new byte[size];

            is.read(buffer);

            is.close();

            json = new String(buffer, "UTF-8");


        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public static String getStringFromFile(String fileName, Context context) throws IOException {
        File file = new File(context.getFilesDir(),fileName);
        if (fileName.equals("WebsitesBase.json")){
            if (!file.exists()){
                file.createNewFile();
                FileWriter fr = new FileWriter(file, true);
                fr.write(loadJSONFromAsset(context,fileName));
                fr.close();
            }
        }
        file.createNewFile();
        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        StringBuilder stringBuilder = new StringBuilder();
        String line = bufferedReader.readLine();
        while (line != null){
            stringBuilder.append(line).append("\n");
            line = bufferedReader.readLine();
        }
        bufferedReader.close();
        return stringBuilder.toString();
    }


    private BroadcastReceiver onBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctxt, final Intent i) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    if (i.hasExtra("map")){
                        if (!phoneObjectsMap.containsKey(((Map<? extends String, ? extends PhoneObject>) i.getSerializableExtra("map")).keySet().toArray()[0])){
                            phoneObjectsMap.putAll((Map<? extends String, ? extends PhoneObject>) i.getSerializableExtra("map"));
                            try {
                                objectMapper.writeValue(phoneNumbersDatabase, phoneObjectsMap.values());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    if (i.hasExtra("updateAdapter")){
                        updateAdapters();
                    }
                    if (i.hasExtra("websiteList")){
                        websitesMap.putAll((Map<String, Boolean>) i.getSerializableExtra("websiteList"));
                        phoneObjectAdapterCallBack.get(2).updateList(websitesMap);
                        phoneObjectAdapterCallBack.get(2).updateAdapter();
                    }
                    if (i.hasExtra("queueNb")){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateLoadingView(i.getIntExtra("queueNb",0));
                            }
                        });
                    }
                }
            });
        }
    };

    public void updateLoadingView(int i){
        progress.setText(i+"%");
        if (i==100){
            progressLinearLayout.setVisibility(View.GONE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.WHITE);
            }
            navigation.setVisibility(View.VISIBLE);
            if (sharedPreferences.getBoolean("firstLaunch",true)){
                sharedPreferences.edit().putBoolean("firstLaunch",false).apply();
            }
        }
    }

    public void updateAdapters(){
        countryList=getCountryList(phoneObjectsMap.values());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MessageQueue.IdleHandler handler = new MessageQueue.IdleHandler() {
                    @Override
                    public boolean queueIdle() {
                        phoneObjectAdapterCallBack.get(0).updateAdapter();
                        phoneObjectAdapterCallBack.get(1).updateAdapter();
                        return false;
                    }
                };
                Looper.myQueue().addIdleHandler(handler);
            }
        });
    }
    public void onResume() {
        super.onResume();
        registerReceiver(onBroadcast, new IntentFilter("updatePhoneObjects"));
    }

    public void onPause() {
        super.onPause();
        unregisterReceiver(onBroadcast);
    }
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(onBroadcast);
    }
    public static List<CountryComparator> getCountryList(Collection<PhoneObject> phoneObjects) {
        List<CountryComparator> countries = new ArrayList<>();
        for (PhoneObject phoneObject: phoneObjects){
            int index=countries.indexOf(phoneObject.getCountryComparator());
            if (index==-1){
                countries.add(phoneObject.getCountryComparator(1));
            }else{
                countries.set(index,phoneObject.getCountryComparator(countries.get(index).getNb()+1));
            }
        }
        Collections.sort(countries, new Comparator<CountryComparator>() {
            @Override
            public int compare(CountryComparator o1, CountryComparator o2) {
                return o1.getCountry().compareTo(o2.getCountry());
            }
        });
        return countries;
    }

    public static class CountryComparator{
        String country_code, country;
        int nb;
        public CountryComparator(String country, String country_code, int nb){
            this.country=country;
            this.country_code=country_code;
            this.nb=nb;
        }


        @Override
        public boolean equals(@Nullable Object obj) {
            if (obj instanceof CountryComparator){
                if(this.country.equals(((CountryComparator)obj).country))
                    return true;
            }
            return false;
        }

        public String getCountry() {
            return country;
        }

        public int getNb() {
            return nb;
        }

        public String getCountry_code() {
            return country_code;
        }

    }

    public static class SortingType{
        public Class type;
        public boolean isDescending;
        private boolean changed;

        public SortingType(Class clas, Boolean boolea){
            type = clas;
            isDescending = boolea;
            setChanged(true);
        }
        public boolean changed(){
            return changed;
        }
        public void change(Class clas, Boolean boolea){
            type = clas;
            isDescending = boolea;
            setChanged(true);
        }
        public void change(SortingType sortingType){
            type=sortingType.type;
            isDescending=sortingType.isDescending;
            setChanged(true);
        }

        public void setChanged(boolean boo){
            changed=boo;
        }
    }

    public static void sort(final MainActivity.SortingType sortingType, List<PhoneObject> list){
        if (sortingType.type == Integer.class){
            if (sortingType.isDescending){
                Collections.sort(list, new Comparator<PhoneObject>() {
                    public int compare(PhoneObject entry1, PhoneObject entry2) {
                        return Long.compare(Long.parseLong(entry2.getAdded_on()), Long.parseLong(entry1.getAdded_on()));
                    }
                });
            }else{
                Collections.sort(list, new Comparator<PhoneObject>() {
                    public int compare(PhoneObject entry1, PhoneObject entry2) {
                        return Long.compare(Long.parseLong(entry1.getAdded_on()), Long.parseLong(entry2.getAdded_on()));
                    }
                });
            }
        }else if(sortingType.type == String.class){
            if (sortingType.isDescending){
                Collections.sort(list, new Comparator<PhoneObject>() {
                    public int compare(PhoneObject entry1, PhoneObject entry2) {
                        return entry2.getCountry_code().compareTo(entry1.getCountry_code());
                    }
                });
            }else{
                Collections.sort(list, new Comparator<PhoneObject>() {
                    public int compare(PhoneObject entry1, PhoneObject entry2) {
                        return entry1.getCountry_code().compareTo(entry2.getCountry_code());
                    }
                });
            }
        }
    }
    public static void showSortPopUp(final View v, Context context, final sortingPopUpCallback sortingPopUpCallback, int id) {
        PopupMenu popup = new PopupMenu(context, v);
        popup.getMenuInflater().inflate(id, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                SortingType sortingType;
                switch (item.getItemId()) {
                    case R.id.recenttoold:
                        ((ImageView)v).setImageDrawable(item.getIcon());
                        sortingType = new MainActivity.SortingType(Integer.class,true);
                        break;
                    case R.id.oldtorecent:
                        ((ImageView)v).setImageDrawable(item.getIcon());
                        sortingType = new MainActivity.SortingType(Integer.class,false);
                        break;
                    case R.id.atoz:
                        ((ImageView)v).setImageDrawable(item.getIcon());
                        sortingType = new MainActivity.SortingType(String.class,false);
                        break;
                    case R.id.ztoa:
                        ((ImageView)v).setImageDrawable(item.getIcon());
                        sortingType = new MainActivity.SortingType(String.class,true);
                        break;
                    default:
                        sortingType = new MainActivity.SortingType(Integer.class,true);
                        break;
                }
                sortingPopUpCallback.toDo(sortingType);
                return true;
            }
        });

        popup.setOnDismissListener(new PopupMenu.OnDismissListener() {
            @Override
            public void onDismiss(PopupMenu menu) {
                v.setSelected(false);
            }
        });
        popup.show();
    }
    public interface sortingPopUpCallback{
        void toDo(SortingType sortingType);
    }

    public void loadDatabase() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (phoneNumbersDatabase.exists()){
                    BufferedReader bufferedReader = null;
                    try {
                        bufferedReader = new BufferedReader(new InputStreamReader(
                                new FileInputStream(phoneNumbersDatabase), "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    String line = null;
                    try {
                        line = bufferedReader.readLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (line !=null && !line.isEmpty()){
                        JsonNode json = null;
                        try {
                            json = objectMapper.readTree(line);
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }
                        int length = json.size(), i=0;
                        for(JsonNode jsonNode :json){
                            phoneObjectsMap.put(jsonNode.get("phone_number").textValue(),new PhoneObject(jsonNode));
                            if (!sharedPreferences.getBoolean("firstLaunch",true)){
                                int finalI = i;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        updateLoadingView(100*(finalI)/length);
                                    }
                                });
                            }
                            i++;
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateLoadingView(100);
                                updateAdapters();
                            }
                        });
                    }
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
    public int getPhoneNumberClicked(){
        return phoneNumberClicked;
    }
    public void setPhoneNumberClicked(int phoneNumberClicked){
        this.phoneNumberClicked=phoneNumberClicked;
    }

    public static int getTotalPhoneNumbers(List<CountryComparator> countryList){
        int i=0;
        for (CountryComparator countryComparator : countryList){
            i+=countryComparator.getNb();
        }
        return i;
    }

}
