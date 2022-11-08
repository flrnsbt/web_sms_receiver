package com.nalakstudio.smsreceive.Services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nalakstudio.smsreceive.MainActivity;
import com.nalakstudio.smsreceive.PhoneObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.michaelrocks.libphonenumber.android.NumberParseException;
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil;

public class UpdatePhoneNumbersDatabase extends Service {

    private static final String TAG = "UpdatePhoneNumbers";
    JSONArray websitesArray;
    ObjectMapper objectMapper = new ObjectMapper();
//    HashMap<String, PhoneObject> phoneObjects = new HashMap<>();
    private boolean isRunning  = false;
    int queueNb =0, originalQueueNb =0;
    SharedPreferences sharedPreferences;

    PrintWriter out;



    public UpdatePhoneNumbersDatabase() {
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "Service onCreate");
        isRunning = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sharedPreferences = getSharedPreferences("preferences", MODE_PRIVATE);
        Log.i(TAG, "Service onStartCommand");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    websitesArray = new JSONArray(MainActivity.getStringFromFile("WebsitesBase.json",UpdatePhoneNumbersDatabase.this));
                    websitesArray = getSortedList(websitesArray);
                    queueNb=websitesArray.length();
                    originalQueueNb=websitesArray.length();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                HashMap<String,Boolean> websiteMap = new HashMap<>();
                for (int i=0; i<websitesArray.length();i++){
                    try {
                        JSONObject currentWebsite = websitesArray.getJSONObject(i);
                        if (!currentWebsite.getString("updated").equals("never")){
                            new Content().execute(currentWebsite);
                            websiteMap.put(currentWebsite.getString("url"),true);
                        }else{
                            websiteMap.put(currentWebsite.getString("url"),false);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                getApplicationContext().sendBroadcast(new Intent("updatePhoneObjects").putExtra("websiteList",websiteMap));
                stopSelf();
            }
        }).start();

        return Service.START_STICKY;
    }


    @Override
    public IBinder onBind(Intent arg0) {
        Log.i(TAG, "Service onBind");
        return null;
    }

    @Override
    public void onDestroy() {
        isRunning = false;
        Log.i(TAG, "Service onDestroy");
    }

    private class Content extends AsyncTask<JSONObject, Void, Boolean> {
        JSONObject jsonObject;

        @Override
        protected Boolean doInBackground(JSONObject... jsonObjects) {
            jsonObject = jsonObjects[0];
            Document document = null;
            try {
                if (jsonObjects[0].getString("url").equals("https://receive-sms-free.net/")){
                    int j=1;
                    do {
                        if (j>1){
                            document = Jsoup.connect(jsonObject.getString("url")+"regions/"+j+".html").get();
                        }else{
                            document = Jsoup.connect(jsonObject.getString("url")+"regions/").get();
                        }
                        Elements elements = document.getElementById("ul").getElementsByClass("link_btn trans");
                        if (elements.isEmpty()){
                            break;
                        }
                        for (final Element element: elements){
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    int i=0;
                                    Document document1 =null;
                                    do{
                                        if (i>0){
                                            try {
                                                document1 = Jsoup.connect(element.attr("href")+i+".html").get();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }else{
                                            try {
                                                document1 = Jsoup.connect(element.attr("href")).get();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        if (document1 == null) {
                                            break;
                                        }
                                        Elements elements2 = document1.getElementById("ul").getElementsByTag("A");
                                        if (elements2.isEmpty()){
                                            break;
                                        }
                                        for (Element e : elements2){
                                            String str[] = getCountryFromNumber(e.select("span").text());
                                            getApplicationContext().sendBroadcast(new Intent("updatePhoneObjects").putExtra("map",(Serializable) Collections.singletonMap(e.select("span").text().replaceAll(" ",""), new PhoneObject(e.attr("href"),e.select("span").text().replaceAll(" ",""),str[0],str[1],String.valueOf(new Date().getTime()),"working"))));
                                        }
                                        i++;
                                    }while (true);
                                }
                            }).start();
                        }
                        j++;
                    }while (true);
                }



                else if(jsonObjects[0].getString("url").equals("https://www.bfkdim.com")){
                    int i=0;
                    do{
                        document = Jsoup.connect(jsonObject.getString("url")+"/?page="+i+"&lang=en").get();
                        if (document == null) {
                            break;
                        }
                        Elements elements = document.getElementsByClass("phone_item");
                        if (elements.isEmpty()){
                            break;
                        }
                        for (Element element : elements){
                            if (!element.getElementsByClass("phone_body_top").select("span").select("b").get(1).text().contains("Unavailable")){
                                String phone_number=element.getElementsByClass("sup_phone").text()+element.getElementsByClass("phone").text();
                                phone_number=phone_number.replaceAll(" ","");
                                String str[] = getCountryFromNumber(phone_number);
                                getApplicationContext().sendBroadcast(new Intent("updatePhoneObjects").putExtra("map", (Serializable) Collections.singletonMap(phone_number, new PhoneObject(jsonObjects[0].getString("url")+element.getElementsByTag("A").attr("href"),phone_number,str[0],str[1],String.valueOf(new Date().getTime()),"working"))));
                            }
                        }
                        i++;
                    }while (true);


                }

                else if(jsonObjects[0].getString("url").equals("https://receive-smss.com")){
                        document = Jsoup.connect(jsonObject.getString("url")).get();
                    if (document != null) {

                        Elements elements = document.getElementsByClass("number-boxes-item");
                        for (Element element : elements) {
                            String str[] = getCountryFromNumber(element.getElementsByClass("number-boxes-itemm-number").text());
                            getApplicationContext().sendBroadcast(new Intent("updatePhoneObjects").putExtra("map", (Serializable) Collections.singletonMap(element.getElementsByClass("number-boxes-itemm-number").text().replaceAll(" ", ""), new PhoneObject(jsonObjects[0].getString("url") + element.getElementsByTag("A").attr("href"), element.getElementsByClass("number-boxes-itemm-number").text().replaceAll(" ", ""), str[0], str[1], String.valueOf(new Date().getTime()), "working"))));
                        }

                    }
                }

                else if(jsonObjects[0].getString("url").equals("https://getfreesmsnumber.com")) {
                    int i = 0;
                    document = Jsoup.connect(jsonObject.getString("url") + "/virtual-phone").get();
                    if (document != null) {


                    Elements elements = document.getElementsByClass("card-text");
                    for (Element element : elements) {
                        Document document2 = Jsoup.connect(jsonObjects[0].getString("url") + element.getElementsByTag("A").attr("href").replaceAll(" ", "")).get();
                        if (document2 == null) {
                            break;
                        }
                        Elements elements2 = document2.getElementsByClass("card-text");
                        for (Element element2 : elements2) {
                            String str[] = getCountryFromNumber(element2.getElementsByTag("P").get(0).text());
                            getApplicationContext().sendBroadcast(new Intent("updatePhoneObjects").putExtra("map", (Serializable) Collections.singletonMap(element2.getElementsByTag("P").get(0).text(), new PhoneObject(jsonObjects[0].getString("url") + element2.getElementsByTag("A").attr("href").replaceAll(" ", ""), element2.getElementsByTag("P").get(0).text(), str[0], str[1], String.valueOf(new Date().getTime()), "working"))));
                        }
                    }
                }
                }


            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return true;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected void onPostExecute(Boolean b) {
            super.onPostExecute(b);
            try {
                if (sharedPreferences.getBoolean("firstLaunch",true)){
                    getApplicationContext().sendBroadcast(new Intent("updatePhoneObjects").putExtra("queueNb", 100*(originalQueueNb-queueNb)/originalQueueNb));
                }
                queueNb--;
                jsonObject.put("updated",String.valueOf(new Date().getTime()));
                objectMapper.writeValue(new File(UpdatePhoneNumbersDatabase.this.getFilesDir(),"WebsitesBase.json"),objectMapper.readTree(websitesArray.toString()));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (queueNb==0){
                if (sharedPreferences.getBoolean("firstLaunch",true)){
                    getApplicationContext().sendBroadcast(new Intent("updatePhoneObjects").putExtra("queueNb", 100).putExtra("updateAdapter",true));
                }else{
                    getApplicationContext().sendBroadcast(new Intent("updatePhoneObjects").putExtra("updateAdapter",true));

                }
            }
        }
    }

    public static JSONArray getSortedList(JSONArray array) throws JSONException {
        List<JSONObject> list = new ArrayList<JSONObject>();
        for (int i = 0; i < array.length(); i++) {
            list.add(array.getJSONObject(i));
        }
        Collections.sort(list, new SortBasedOnUpdateTime());

        JSONArray resultArray = new JSONArray(list);

        return resultArray;
    }
    public static class SortBasedOnUpdateTime implements Comparator<JSONObject> {
        /*
         * (non-Javadoc)
         *
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         * lhs- 1st message in the form of json object. rhs- 2nd message in the form
         * of json object.
         */
        @Override
        public int compare(JSONObject lhs, JSONObject rhs) {
            try {
                return lhs.getInt("updated") > rhs.getInt("updated") ? 1 : (lhs
                        .getInt("updated") < rhs.getInt("updated") ? -1 : 0);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return 0;

        }
    }

    public String[] getCountryFromNumber(String phone_number) {
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.createInstance(UpdatePhoneNumbersDatabase.this);
        if (!phone_number.startsWith("+")){
            phone_number="+"+phone_number;
        }
        try {
            String str[] = new String[2];
            str[1]= phoneNumberUtil.getRegionCodeForCountryCode(phoneNumberUtil.parse(phone_number, null).getCountryCode());
            str[0]=new Locale("",str[1]).getDisplayCountry();
            return str;
        } catch (NumberParseException e) {
            Log.e(TAG, "error during parsing a number");
        }
        return null;
    }
}


