package com.mualab.org.user.activity.dialogs;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.mualab.org.user.R;
import com.mualab.org.user.activity.dialogs.adapter.NameDisplayAdapter;

import java.util.ArrayList;
import java.util.List;


public class NameDisplayDialog extends BottomSheetDialogFragment implements View.OnClickListener {

    private static final String TAG = NameDisplayDialog.class.getSimpleName();

    private List<String> bookingTypeListA;
    private ItemClickListener itemClickListener;
    private String title = "";

    public NameDisplayDialog() {}

    public static NameDisplayDialog newInstance(String title, ArrayList<String> bookingTypeList, ItemClickListener itemClickListener) {

        Bundle args = new Bundle();

        NameDisplayDialog fragment = new NameDisplayDialog();
        //fragment.setIsBottomTrue();
        fragment.setArguments(args);
        //fragment.setTheme(R.style.BottomSelectionDialogTheme);
        fragment.setInstance(title, bookingTypeList, itemClickListener);
        fragment.setCancelable(true);
        fragment.setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
        return fragment;
    }

    private void setInstance(String title, List<String> bookingTypeList, ItemClickListener itemClickListener) {
        this.title = title;
        this.bookingTypeListA = bookingTypeList;
        this.itemClickListener = itemClickListener;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_bottom, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvTitle = view.findViewById(R.id.tvTitle);
        tvTitle.setText(title);

        view.findViewById(R.id.main_layout).setOnClickListener(this);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);

        view.findViewById(R.id.tvNoRecord).setVisibility(bookingTypeListA.isEmpty() ? View.VISIBLE : View.GONE);

        NameDisplayAdapter typeAdapter = new NameDisplayAdapter(bookingTypeListA, pos -> {
            dismiss();
            itemClickListener.onItemClick(pos);
        });

        recyclerView.setAdapter(typeAdapter);

    }

    public void show(FragmentManager fragmentManager) {
        if(bookingTypeListA != null && bookingTypeListA.size() != 0)
        super.show(fragmentManager, TAG);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_layout:
               dismiss();
                break;
        }
    }


}
