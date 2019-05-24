package com.mualab.org.user.activity.share_module.dialog;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.base.BaseDialog;


public class ShareInvitationDialog extends BaseDialog implements View.OnClickListener {

    private ShareCallback callback;
    private String link;
    private String from;

    public ShareInvitationDialog() {
    }

    public static ShareInvitationDialog newInstance(String from, String link, ShareCallback callback) {

        Bundle args = new Bundle();

        ShareInvitationDialog fragment = new ShareInvitationDialog();
        fragment.setArguments(args);
        fragment.setInstanceData(from, link, callback);
        return fragment;
    }

    private void setInstanceData(String from, String link, ShareCallback callback) {
        this.from = from;
        this.link = link.trim();
        this.callback = callback;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_share_invitation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        TextView tvMsg = view.findViewById(R.id.tvMsg);
        tvMsg.setText(from.equalsIgnoreCase("voucher")
                ? R.string.send_this_voucher_link_to_people_you_want_to_message_with_on_koobibiz
                : R.string.send_this_post_link_to_people_you_want_to_message_with_on_koobibiz);

        TextView tvLink = view.findViewById(R.id.tvLink);
        tvLink.setText(link);

        view.findViewById(R.id.btnCancel).setOnClickListener(this);
        view.findViewById(R.id.btnOk).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btnCancel:
                dismiss();
                break;

            case R.id.btnOk:
                assert callback != null;
                dismiss();
                callback.onShareClick();
                break;
        }
    }


}
