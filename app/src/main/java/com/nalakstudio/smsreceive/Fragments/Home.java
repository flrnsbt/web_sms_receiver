package com.nalakstudio.smsreceive.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.appodeal.ads.Appodeal;
import com.appodeal.ads.Native;
import com.appodeal.ads.NativeAd;
import com.appodeal.ads.NativeCallbacks;
import com.nalakstudio.smsreceive.CustomViews.MyCustomDialog;
import com.nalakstudio.smsreceive.CustomViews.PredicateLayout;
import com.nalakstudio.smsreceive.MainActivity;
import com.nalakstudio.smsreceive.Adapters.PhoneObjectAdapter;
import com.nalakstudio.smsreceive.PhoneObject;
import com.nalakstudio.smsreceive.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.nalakstudio.smsreceive.MainActivity.showSortPopUp;
import static com.nalakstudio.smsreceive.MainActivity.sort;

public class Home extends Fragment implements PhoneObjectAdapter.PhoneObjectAdapterCallBack, SwipeRefreshLayout.OnRefreshListener, SearchView.OnQueryTextListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private View view;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private PhoneObjectAdapter phoneObjectAdapter;
    private List<PhoneObject> phoneObjects = new ArrayList<>();
    ImageView imageView, imageView2;
    SearchView searchView;
    static PhoneObject.CurrentFilter currentFilter = new PhoneObject.CurrentFilter();
    static MainActivity.SortingType sortingType = new MainActivity.SortingType(Integer.class,true);

    private OnFragmentInteractionListener mListener;

    public Home() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Home.
     */
    // TODO: Rename and change types and number of parameters
    public static Home newInstance(String param1, String param2) {
        Home fragment = new Home();
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
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_home, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));
        phoneObjectAdapter = new PhoneObjectAdapter(phoneObjects, ((MainActivity)getContext()).nativeAds ,getContext());
        recyclerView.setAdapter(phoneObjectAdapter);
        recyclerView.setHasFixedSize(true);
        searchView=view.findViewById(R.id.searchView);
        imageView = view.findViewById(R.id.imageView);
        imageView2 = view.findViewById(R.id.imageView2);
        view.findViewById(R.id.relativeLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.performClick();
            }
        });
        searchView.setOnQueryTextListener(this);
        AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        autoCompleteTextView.setThreshold(0);
        imageView2.setColorFilter(getContext().getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView.setVisibility(View.GONE);
                imageView2.setVisibility(View.GONE);
            }
        });
        imageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView2.setColorFilter(null);
                imageView2.setSelected(true);
                showFilterPopUp(v);
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView.setSelected(true);
                showSortPopUp(imageView,getContext(), new MainActivity.sortingPopUpCallback() {
                    @Override
                    public void toDo(MainActivity.SortingType sortingType) {
                        Home.sortingType.change(sortingType);
                        updateAdapter();
                    }

                },getResources().getIdentifier("popup","menu",getContext().getPackageName()));

            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                imageView.setVisibility(View.VISIBLE);
                imageView2.setVisibility(View.VISIBLE);
                return false;
            }
        });


        swipeRefreshLayout= view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(
                R.color.refresh_progress_1,
                R.color.refresh_progress_2,
                R.color.refresh_progress_3);

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
    public void updateAdapter() {
        updateList();
        if (!currentFilter.isEmpty()){
            phoneObjectAdapter.filter(currentFilter);
        }
        if (sortingType.changed() || phoneObjectAdapter.isDataChanged()){
            sort(sortingType,phoneObjects);
            sortingType.setChanged(false);
            phoneObjectAdapter.setDataChanged(false);
        }
        phoneObjectAdapter.notifyDataSetChanged();
        recyclerView.scrollToPosition(0);
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


    @Override
    public void onRefresh() {
        Window window = ((MainActivity) getContext()).getWindow();
        ((MainActivity)getContext()).findViewById(R.id.shadow).setVisibility(View.VISIBLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#9C000000"));
        }
        updateAdapter();
        swipeRefreshLayout.setRefreshing(true);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ((MainActivity)getContext()).findViewById(R.id.shadow).setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    window.setStatusBarColor(Color.WHITE);
                }
            }
        }, 3000);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        currentFilter.addANDConstraint(query);
        updateAdapter();
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        currentFilter.addANDConstraint(newText);
        updateAdapter();
        return true;
    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }



    private void showFilterPopUp(View v) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = ((MainActivity)getContext()).getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.colorAccent));
        }
        final PhoneObject.CurrentFilter currentFilter = this.currentFilter;
        final List<CheckBox> checkBoxes = new ArrayList<CheckBox>();
        final List<CheckBox> checkBoxes2 = new ArrayList<CheckBox>();
        ((MainActivity)getContext()).navigation.setVisibility(View.GONE);
        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService (Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.filter_popup, null);
        final PopupWindow popup = new PopupWindow(popupView,LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT,true);
        popup.showAtLocation(view, Gravity.CENTER, 0, 0);
        PredicateLayout predicateLayout = popupView.findViewById(R.id.predicate_layout);
        PredicateLayout predicateLayout2 = popupView.findViewById(R.id.predicate_layout2);
        Button confirm = popupView.findViewById(R.id.confirm);
        final TextView select = popupView.findViewById(R.id.select);
        final TextView select2 = popupView.findViewById(R.id.select2);
        TextView reset = popupView.findViewById(R.id.reset);
        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (select.getText().equals("Select All")){
                    select.setText("Deselect All");
                    for (CheckBox checkBox: checkBoxes){
                        checkBox.setChecked(true);
                    }
                }else {
                    select.setText("Select All");
                    for (CheckBox checkBox: checkBoxes){
                        checkBox.setChecked(false);
                    }
                }
            }
        });
        select2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (select2.getText().equals("Select All")){
                    select2.setText("Deselect All");
                    for (CheckBox checkBox: checkBoxes2){
                        checkBox.setChecked(true);
                    }
                }else {
                    select2.setText("Select All");
                    for (CheckBox checkBox: checkBoxes2){
                        checkBox.setChecked(false);
                    }
                }
            }
        });
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (CheckBox checkBox: checkBoxes){
                    checkBox.setChecked(false);
                }
                for (CheckBox checkBox: checkBoxes2){
                    checkBox.setChecked(false);
                }
            }
        });
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Home.currentFilter = currentFilter;
                updateAdapter();
                popup.dismiss();
            }
        });
        for (final MainActivity.CountryComparator countryComparator: ((MainActivity)getContext()).countryList){
            final CheckBox checkBox = new CheckBox(getContext());
            checkBox.setText(countryComparator.getCountry());
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked){
                        currentFilter.addORConstraint(countryComparator.getCountry().toString());
                    }else {
                        currentFilter.removeORConstraints(countryComparator.getCountry().toString());
                    }
                }
            });
            predicateLayout.addView(checkBox);
            if (currentFilter.contains(countryComparator.getCountry().toLowerCase())){
                checkBox.setChecked(true);
            }
            checkBoxes.add(checkBox);
        }

        for (final Map.Entry<String,Boolean> entry: ((MainActivity)getContext()).websitesMap.entrySet()){
            final CheckBox checkBox = new CheckBox(getContext());
            String string = entry.getKey();
            int i=string.lastIndexOf("/");
            if (i==string.length()-1){
                string=string.substring(0,i);
                i=string.lastIndexOf("/");
            }
            checkBox.setText(string.substring(i+1,string.length()));
            String finalString = string;
            int finalI = i;
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked){
                        currentFilter.addORConstraint(finalString.substring(finalI +1, finalString.length()));
                    }else {
                        currentFilter.removeORConstraints(finalString.substring(finalI +1, finalString.length()));
                    }
                }
            });
            predicateLayout2.addView(checkBox);
            if (currentFilter.contains(string.substring(i+1,string.length()).toLowerCase())){
                checkBox.setChecked(true);
            }
            checkBoxes2.add(checkBox);
        }

        ImageView back = popupView.findViewById(R.id.imageView3);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentFilter.size()!=Home.currentFilter.size()){
                    MyCustomDialog builder = new MyCustomDialog(getContext(), "Filters not saved", "Are you sure you want lo leave?");
                    final AlertDialog dialog = builder.setNegativeButton("No", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            popup.dismiss();
                            dialog.dismiss();
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
                }else{
                    popup.dismiss();
                }
            }
        });
        popup.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                ((MainActivity)getContext()).navigation.setVisibility(View.VISIBLE);
                imageView2.setSelected(false);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Window window = ((MainActivity)getContext()).getWindow();
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    window.setStatusBarColor(Color.WHITE);
                }
            }
        });
    }


}
