package com.nalakstudio.smsreceive.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.appodeal.ads.NativeAd;
import com.nalakstudio.smsreceive.PhoneObject;
import com.nalakstudio.smsreceive.R;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PhoneObjectAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<PhoneObject> list= new ArrayList<>();
    private Set<NativeAd> nativeAds = new HashSet<>();
    private static int LIST_AD_DELTA = 10;
    private static final int CONTENT_TYPE = 0;
    private static final int AD_TYPE = 1;

    Context context;
    boolean dataChanged = false;
    public PhoneObjectAdapter(List<PhoneObject> list, Set<NativeAd> nativeAds, Context context) {
        this.list = list;
        this.context = context;
        this.nativeAds = nativeAds;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int itemType) {
        if (itemType == CONTENT_TYPE) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.phone_number_item,viewGroup,false);
            return new PhoneObjectHolder(view, context,viewGroup);
        }else{
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.nativeadview,viewGroup,false);
            return new AdHolder(view, context,viewGroup);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (getItemViewType(position) == CONTENT_TYPE) {
            PhoneObject myObject = list.get(getRealPosition(position));
            try {
                ((PhoneObjectHolder)viewHolder).bind(myObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            if (nativeAds.isEmpty()){
                viewHolder.itemView.setVisibility(View.GONE);
                return;
            }else{
                viewHolder.itemView.setVisibility(View.VISIBLE);
            }
            Iterator<NativeAd> iterator = nativeAds.iterator();
            NativeAd nativeAd = iterator.next();
            int i=0;
            while (iterator.hasNext() && i!=getAdRank(position)%nativeAds.size()){
                nativeAd=iterator.next();
                i++;
            }
            if ((getAdRank(position)+getAdRank(position)/nativeAds.size())%nativeAds.size()==0 && getAdRank(position)>=nativeAds.size()){
                if ( nativeAds.size()>1){
                    ((AdHolder)viewHolder).bind(nativeAd, true);
                }else{
                    if (getAdRank(position)%4==0){
                        ((AdHolder)viewHolder).bind(nativeAd, true);
                    }else{
                        ((AdHolder)viewHolder).bind(nativeAd, false);
                    }
                }
            }else{
                ((AdHolder)viewHolder).bind(nativeAd, false);
            }

        }
    }

    @Override
    public int getItemCount() {
        int additionalContent = 0;
        if (list.size() > 0 && LIST_AD_DELTA > 0 && list.size() > LIST_AD_DELTA) {
            additionalContent = 1+(list.size() / LIST_AD_DELTA);
        }
        return list.size() + additionalContent;
    }


    public interface PhoneObjectAdapterCallBack{
        void updateAdapter();
        void updateList();
        void updateList(Map<String,Boolean> map);
    }

    @Override
    public int getItemViewType(int position) {
        if ((position > 0 && ((position-4)% LIST_AD_DELTA == 0)) || position==4) {
            return AD_TYPE;
        }
        return CONTENT_TYPE;
    }

    private int getRealPosition(int position) {
        if (LIST_AD_DELTA == 0) {
            return position;
        } else {
            int i=position - ((position-4)/ LIST_AD_DELTA);
            if (position>=4){
                i=i-1;
            }
            return i;
        }
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    public void filter(PhoneObject.CurrentFilter currentFilter) {
        List<PhoneObject> lis = new ArrayList<>();
        lis.addAll(list);
        if (!currentFilter.isORConstraintsEmpty()){
            Iterator<PhoneObject> iterator = lis.iterator();
            while (iterator.hasNext()){
                PhoneObject phoneObject = iterator.next();
                boolean contains = false;
                for(int i=0; i<currentFilter.getORConstaints().length;i++){
                    if (phoneObject.toValueString().toLowerCase().contains(currentFilter.getORConstaints()[i].toLowerCase())) {
                        contains=true;
                        break;
                    }
                }
                if (!contains){
                    iterator.remove();
                }
            }
        }
        if (!currentFilter.isANDConstraintsEmpty()){
            Iterator<PhoneObject> iterator2 = lis.iterator();
            while (iterator2.hasNext()){
                PhoneObject phoneObject = iterator2.next();
                for(int i=0; i<currentFilter.getANDConstraints().length;i++){
                    if (!phoneObject.toValueString().toLowerCase().contains(currentFilter.getANDConstraints()[i].toLowerCase())) {
                        iterator2.remove();
                    }
                }
            }
        }
            list.clear();
            list.addAll(lis);
            setDataChanged(true);
    }

    public void setDataChanged(boolean dataChanged) {
        this.dataChanged = dataChanged;
    }

    public boolean isDataChanged() {
        return dataChanged;
    }

    public int getAdRank(int position){
        return 1+position/LIST_AD_DELTA;
    }

}