package com.nalakstudio.smsreceive.Fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.nalakstudio.smsreceive.Adapters.PhoneObjectAdapter;
import com.nalakstudio.smsreceive.MainActivity;
import com.nalakstudio.smsreceive.PhoneObject;
import com.nalakstudio.smsreceive.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.nalakstudio.smsreceive.MainActivity.getTotalPhoneNumbers;
import static com.nalakstudio.smsreceive.MainActivity.showSortPopUp;
import static com.nalakstudio.smsreceive.MainActivity.sort;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Countries.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Countries#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Countries extends Fragment implements PhoneObjectAdapter.PhoneObjectAdapterCallBack, SwipeRefreshLayout.OnRefreshListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private TableLayout tableLayout;
    View view;
    View popupView;
    PopupWindow popup;
    ImageView back;
    SwipeRefreshLayout swipeRefreshLayout;
    private List<PhoneObject> phoneObjects= new ArrayList<>();
    PhoneObject.CurrentFilter currentFilter = new PhoneObject.CurrentFilter();
    ViewGroup container;
    private PhoneObjectAdapter phoneObjectAdapter;
    static MainActivity.SortingType sortingType = new MainActivity.SortingType(Integer.class,true);
    ImageView sort;
    DisplayMetrics displayMetrics = new DisplayMetrics();

    private OnFragmentInteractionListener mListener;

    public Countries() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Countries.
     */
    // TODO: Rename and change types and number of parameters
    public static Countries newInstance(String param1, String param2) {
        Countries fragment = new Countries();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_countries, container, false);
        this.container = container;
        tableLayout = view.findViewById(R.id.tableLayout);
        popupView = inflater.inflate(R.layout.phone_from_country, container,false);
        RecyclerView recyclerView = popupView.findViewById(R.id.recyclerView);
        swipeRefreshLayout = popupView.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(
                R.color.refresh_progress_1,
                R.color.refresh_progress_2,
                R.color.refresh_progress_3);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));
        phoneObjectAdapter = new PhoneObjectAdapter(phoneObjects,((MainActivity)getContext()).nativeAds,getContext());
        recyclerView.setAdapter(phoneObjectAdapter);
        recyclerView.setHasFixedSize(true);
        ((Activity) getContext()).getWindowManager()
                .getDefaultDisplay()
                .getMetrics(displayMetrics);
        back=popupView.findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TranslateAnimation animation = new TranslateAnimation(0,displayMetrics.widthPixels, 0, 0);
                animation.setDuration(800);
                animation.setRepeatCount(0);
                popupView.startAnimation(animation);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        popup.dismiss();
                        popup=null;
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            }
        });
        sort=popupView.findViewById(R.id.imageView);
        sort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSortPopUp(v, getContext(), new MainActivity.sortingPopUpCallback() {
                    @Override
                    public void toDo(MainActivity.SortingType sortingType) {
                        Countries.sortingType.change(sortingType);
                        updateRecyclerView();
                    }
                },getResources().getIdentifier("popup2","menu",getContext().getPackageName()));
            }
        });
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onRefresh() {
        Window window = ((MainActivity) getContext()).getWindow();
        popupView.findViewById(R.id.shadow).setVisibility(View.VISIBLE);
        updateRecyclerView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#9C000000"));
        }
        swipeRefreshLayout.setRefreshing(true);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                popupView.findViewById(R.id.shadow).setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    window.setStatusBarColor(Color.WHITE);
                }
            }
        }, 3000);
    }

    @Override
    public void updateAdapter() {
        ((TextView)view.findViewById(R.id.textView3)).setText("Country ("+getTotalPhoneNumbers(((MainActivity)getContext()).countryList)+")");
        updateTableRow();
    }

    @Override
    public void updateList() {
        phoneObjects.clear();
        phoneObjects.addAll(((MainActivity)getContext()).phoneObjectsMap.values());
        phoneObjectAdapter.setDataChanged(true);
    }

    @Override
    public void updateList(Map<String, Boolean> map) {

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public void updateTableRow(){
        view.post(new Runnable() {
            @Override
            public void run() {
                tableLayout.removeAllViews();
                TableRow tr = new TableRow(getContext());
                tr.setLayoutParams(new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT));
                tr.setGravity(Gravity.CENTER_HORIZONTAL);

                int i=1;
                for (final MainActivity.CountryComparator countryComparator : ((MainActivity)getActivity()).countryList){
                    if (i%3==0){
                        tr = new TableRow(getContext());
                        tr.setLayoutParams(new TableRow.LayoutParams(
                                TableRow.LayoutParams.MATCH_PARENT,
                                TableRow.LayoutParams.WRAP_CONTENT));

                    }
                    View view = LayoutInflater.from(getContext()).inflate(R.layout.country_item,tr,false);
                    TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    int dim = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
                    layoutParams.setMargins(dim,dim,dim,dim);
                    layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
                    view.setLayoutParams(layoutParams);
                    ((TextView)view.findViewById(R.id.textView4)).setText(countryComparator.getCountry()+" ("+countryComparator.getNb()+")");
                    ((ImageView)view.findViewById(R.id.imageView4)).setImageResource(getContext().getResources().getIdentifier(countryComparator.getCountry_code().toLowerCase(),"drawable",getContext().getPackageName()));
                    tr.addView(view);
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            popup = new PopupWindow(popupView, LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT,true);
                            popup.showAtLocation(container, Gravity.CENTER, 0, 0);
                            TranslateAnimation animation = new TranslateAnimation(displayMetrics.widthPixels, 0, 0, 0);
                            animation.setDuration(800);
                            animation.setRepeatCount(0);
                            popupView.startAnimation(animation);
                            ImageView countryFlag= popupView.findViewById(R.id.country_img);
                            TextView countryName = popupView.findViewById(R.id.country_name);
                            currentFilter.addANDConstraint(countryComparator.getCountry());
                            countryName.setText(countryComparator.getCountry());
                            countryFlag.setImageResource(getResources().getIdentifier(countryComparator.getCountry_code().toLowerCase(),"drawable",getContext().getPackageName()));
                            updateRecyclerView();
                        }
                    });
                    if (i%3==0){
                        tableLayout.addView(tr);
                    }
                    i++;
                }
            }
        });
    }


    void updateRecyclerView(){
       updateList();
        int i=0;
        if (!currentFilter.isEmpty()){
            phoneObjectAdapter.filter(currentFilter);
        }
        if (sortingType.changed() || phoneObjectAdapter.isDataChanged()){
            sort(sortingType,phoneObjects);
            sortingType.setChanged(false);
            phoneObjectAdapter.setDataChanged(false);
        }
        phoneObjectAdapter.notifyDataSetChanged();
    }
}
