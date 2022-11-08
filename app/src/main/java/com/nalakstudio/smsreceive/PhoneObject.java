package com.nalakstudio.smsreceive;

import android.content.Context;

import com.fasterxml.jackson.databind.JsonNode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import io.michaelrocks.libphonenumber.android.PhoneNumberUtil;

public class PhoneObject implements java.io.Serializable {
    private String url, phone_number, country, country_code, status, added_on;
    private PhoneNumberUtil phoneNumberUtil;

    public PhoneObject(String url, String phone_number, String country, String country_code, String added_on, String status) {
        this.url = url;
        this.phone_number = phone_number;
        this.country = country;
        this.country_code = country_code;
        this.added_on = added_on;
        this.status = status;
    }
    public PhoneObject(JsonNode jsonNode) {
        this.url = jsonNode.get("url").textValue();
        this.phone_number = jsonNode.get("phone_number").textValue();
        this.country =jsonNode.get("country").textValue();
        this.country_code = jsonNode.get("country_code").textValue();
        this.added_on = jsonNode.get("added_on").textValue();
        this.status = jsonNode.get("status").textValue();
    }

    public String getCountry_code() {
        return country_code;
    }

    public String getCountry() {
        return country;
    }


    public String getPhone_number() {
        return phone_number;
    }

    public String getStatus() {
        return status;
    }

    public String getAdded_on(){
        return added_on;
    }

    public String getUrl() {
        return url;
    }

    public String getPrettyUrl() {
        String string =getUrl();
        int i=string.lastIndexOf("/");
        if (i==string.length()-1){
            string=string.substring(0,i);
            i=string.lastIndexOf("/");
        }
        return string.substring(i+1,string.length());
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setCountry_code(String country_code) {
        this.country_code = country_code;
    }

    public void setUrl(String url){
        this.url = url;
    }

    public void setPhone_number(String phone_number){
        this.phone_number = phone_number;
    }

    public void setStatus(String status){
        this.status = status;
    }

    public void setAdded_on(String added_on){
        this.added_on = added_on;
    }

    public String toString(){
        return "{\"url\":\""+url+"\",\"phone_number\":\""+phone_number+"\",\"added_on\":\""+ added_on +"\",\"country\":\""+country+"\",\"country_code\":\""+ country_code +"\",\"status\":\"working\"}";
    }

    public String toValueString(){
        return getPrettyUrl()+" "+ phone_number+" "+ country_code +" "+country+" working";
    }

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;


    public static String getTimeAgo(long time, Context ctx) {
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        long now = new Date().getTime();
        if (time > now || time <= 0) {
            return null;
        }

        // TODO: localize
        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return "just now";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "a minute ago";
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + " minutes ago";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "an hour ago";
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + " hours ago";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "yesterday";
        } else {
            return diff / DAY_MILLIS + " days ago";
        }
    }



    public static class CurrentFilter{
        Set<String> ORConstraints = new HashSet<>();
        String ANDConstraints;

         public CurrentFilter(){
            ORConstraints = new HashSet<>();
            ANDConstraints = "";
        }

        public String[] getORConstaints(){
            return  ORConstraints.toArray(new String[ORConstraints.size()]);
        }

        public boolean isORConstraintsEmpty(){
             return ORConstraints.isEmpty();
        }

        public boolean isANDConstraintsEmpty(){
             return ANDConstraints.isEmpty();
        }

        public String[] getANDConstraints(){
             if (ANDConstraints.contains(",")){
                 String[] str = ANDConstraints.split(",");
                 for (int j=0;j<str.length;j++){
                     StringBuilder stringBuilder = new StringBuilder(str[j]);
                     for (int i=0;i<stringBuilder.length();i++){
                         if (stringBuilder.charAt(i)==' '){
                             if (i==0 || i==stringBuilder.length()){
                                stringBuilder.deleteCharAt(i);
                                i--;
                             }else if(stringBuilder.charAt(i+1)==' ' || stringBuilder.charAt(i-1)==' '){
                                 stringBuilder.deleteCharAt(i);
                                 i--;
                             }
                         }
                     }
                     str[j]=stringBuilder.toString();
                 }
                 return str;
             }else{
                 if (ANDConstraints.equals("")){
                     return new String[]{};
                 }else{
                     return new String[]{ANDConstraints};
                 }
             }
        }

        public int size(){
            return ORConstraints.size()+getANDConstraints().length;
        }

        public void addORConstraint(String ORConstraint){
            ORConstraints.add(ORConstraint);
        }

        public void addANDConstraint(String ANDConstraint){
            this.ANDConstraints=ANDConstraint;
        }

        public void addORConstraints(String[] ORConstraints){
            this.ORConstraints.addAll(Arrays.asList(ORConstraints));
        }


        public void addORConstraints(List<String> ORConstraints){
            this.ORConstraints.addAll(ORConstraints);
        }


        public void removeORConstraints(String ORConstraint){
            this.ORConstraints.remove(ORConstraint);
        }



        public void clearAll(){
            ORConstraints.clear();
            ANDConstraints="";
        }

        public void clearORConstraints(){
            ORConstraints.clear();
        }

        public void clearANDConstraints(){
            ANDConstraints="";
        }

        public boolean isEmpty(){
            if (ORConstraints.isEmpty() && ANDConstraints.isEmpty()){
                return true;
            }else {
                return false;
            }
        }

        public boolean contains(String str){
            if (ORConstraints.toString().toLowerCase().contains(str) || ANDConstraints.toLowerCase().contains(str)) {
                return true;
            }else {
                return false;
            }
        }

        public String toString(){
             String str="";
             if (ORConstraints.size()!=0){
                 str+=ORConstraints.toString().replaceAll(","," ||");
             }
             if (ANDConstraints.length()!=0){
                 str+=ANDConstraints.replaceAll(","," &&");
             }
             return str;
        }
    }

    public MainActivity.CountryComparator getCountryComparator(){
        return new MainActivity.CountryComparator(getCountry(),getCountry_code(),0);
    }

    public MainActivity.CountryComparator getCountryComparator(int i){
        return new MainActivity.CountryComparator(getCountry(),getCountry_code(),i);
    }


}
