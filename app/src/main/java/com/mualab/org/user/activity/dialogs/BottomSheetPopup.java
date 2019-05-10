package com.mualab.org.user.activity.dialogs;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.dialogs.adapter.ItemAdapter;
import com.mualab.org.user.activity.dialogs.adapter.NameDisplayAdapter;
import com.mualab.org.user.activity.dialogs.model.Item;

import java.util.List;

public class BottomSheetPopup extends BottomSheetDialogFragment implements View.OnClickListener {

    private static final String TAG = NameDisplayDialog.class.getSimpleName();

    private List<Item> bookingTypeList;
    private ItemClick itemClickListener;
    private String title = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_bottom_popup, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.main_layout).setOnClickListener(this);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);


        ItemAdapter typeAdapter = new ItemAdapter(bookingTypeList, pos -> {
            dismiss();
            itemClickListener.onClickItem(pos);
        });


        recyclerView.setAdapter(typeAdapter);
    }

    public interface ItemClick{
        void onClickItem(int pos);
        void onDialogDismiss();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        itemClickListener.onDialogDismiss();
        super.onDismiss(dialog);
    }

    public static BottomSheetPopup newInstance(String title, List<Item> bookingTypeList, ItemClick itemClickListener) {
        Bundle args = new Bundle();
        BottomSheetPopup fragment = new BottomSheetPopup();
        fragment.setArguments(args);
        fragment.setInstance(title,bookingTypeList,itemClickListener);
        fragment.setCancelable(true);
        fragment.setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
        return fragment;
    }

    private void setInstance(String title, List<Item> bookingTypeList, ItemClick itemClickListener) {
        this.title = title;
        this.bookingTypeList = bookingTypeList;
        this.itemClickListener = itemClickListener;
    }

    public void show(FragmentManager fragmentManager) {
        if(bookingTypeList.size() != 0)
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
