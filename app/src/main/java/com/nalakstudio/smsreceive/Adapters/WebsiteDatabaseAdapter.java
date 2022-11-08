package com.nalakstudio.smsreceive.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nalakstudio.smsreceive.R;

import java.util.ArrayList;
import java.util.Map;

public class WebsiteDatabaseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    Map<String,Boolean> map;
    Context context;

    public WebsiteDatabaseAdapter(Map<String,Boolean> map, Context context) {
        this.map = map;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.website_database_item,parent,false);
        return new WebsiteDatabaseHolder(view,context);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        String string = new ArrayList<String>(map.keySet()).get(position);
        ((WebsiteDatabaseHolder)holder).bind(string,map.get(string),position);
    }

    @Override
    public int getItemCount() {
        return map.size();
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }
}
