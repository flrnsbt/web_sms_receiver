package com.nalakstudio.smsreceive.Adapters;

import android.content.Context;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nalakstudio.smsreceive.MainActivity;
import com.nalakstudio.smsreceive.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class WebsiteDatabaseHolder extends RecyclerView.ViewHolder {

    Switch Switch;
    ObjectMapper objectMapper = new ObjectMapper();
    JSONArray websitesArray;
    JSONObject currentWebsite;
    Context context;


    public WebsiteDatabaseHolder(@NonNull View itemView, Context context) {
        super(itemView);
        Switch=itemView.findViewById(R.id.switch4);
        this.context = context;
        try {
            websitesArray = new JSONArray(MainActivity.getStringFromFile("WebsitesBase.json", context));
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void bind(String string, boolean b, int position) {
        if (!string.isEmpty()){
            try {
                currentWebsite = websitesArray.getJSONObject(position);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            int i=string.lastIndexOf("/");
            if (i==string.length()-1){
                string=string.substring(0,i);
                i=string.lastIndexOf("/");
            }
            Switch.setText(string.substring(i+1,string.length()));
            Switch.setChecked(b);
            Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    try {
                        if (isChecked){
                            currentWebsite.put("updated",String.valueOf(new Date().getTime()));
                        }else{
                            currentWebsite.put("updated","never");
                        }
                        objectMapper.writeValue(new File(context.getFilesDir(),"WebsitesBase.json"),objectMapper.readTree(websitesArray.toString()));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (JsonGenerationException e) {
                        e.printStackTrace();
                    } catch (JsonMappingException e) {
                        e.printStackTrace();
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            });
        }
    }
}
