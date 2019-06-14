package com.mualab.org.user.activity.artist_profile.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.artist_profile.activity.AboutUsActivity;
import com.mualab.org.user.activity.artist_profile.activity.ArtistProfileActivity;
import com.mualab.org.user.activity.artist_profile.activity.ArtistServicesActivity;
import com.mualab.org.user.activity.artist_profile.activity.CertificateActivity;
import com.mualab.org.user.activity.artist_profile.model.UserProfileData;
import com.mualab.org.user.activity.base.BaseFragment;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.utils.constants.Constant;


public class ProfileComboFragment1 extends BaseFragment implements View.OnClickListener {

    private ArtistProfileActivity bizProfileActivity;
    private UserProfileData profileData;

    public static ProfileComboFragment1 newInstance(UserProfileData profileData) {

        Bundle args = new Bundle();

        ProfileComboFragment1 fragment = new ProfileComboFragment1();
        fragment.setArguments(args);
        args.putSerializable("profileData",profileData);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile_combo_fragment1, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(getActivity() != null){
            bizProfileActivity = (ArtistProfileActivity) getActivity();
        }

        TextView tvServiceCount = view.findViewById(R.id.tvServiceCount);
        TextView tvServiceCount_txt = view.findViewById(R.id.tvServiceCount_txt);

        TextView tvCertificateCount = view.findViewById(R.id.tvCertificateCount);
        TextView tvCertificateCount_txt = view.findViewById(R.id.tvCertificateCount_txt);

        if(getArguments() != null){
            profileData = (UserProfileData) getArguments().getSerializable("profileData");

            tvServiceCount.setText(profileData.serviceCount+"");
            tvServiceCount_txt.setText(Constant.adds(Integer.parseInt(profileData.serviceCount),"Service"));

            tvCertificateCount.setText(profileData.certificateCount+"");
            tvCertificateCount_txt.setText(Constant.adds(Integer.parseInt(profileData.certificateCount),"Qualification"));

        }

        view.findViewById(R.id.llServices).setOnClickListener(this);
        view.findViewById(R.id.llCertificate).setOnClickListener(this);
        view.findViewById(R.id.llAboutUs).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.llServices:
                if (bizProfileActivity != null && bizProfileActivity.profileData != null) {
                    if (!bizProfileActivity.profileData.serviceCount.equals("0")) {
                        Intent intent = new Intent(bizProfileActivity, ArtistServicesActivity.class);
                        intent.putExtra("artistId", bizProfileActivity.artistId);
                            startActivity(intent);
                    } else MyToast.getInstance(getActivity()).showDasuAlert("No services added");
                }
                break;

            case R.id.llAboutUs:
                if (bizProfileActivity != null && bizProfileActivity.profileData != null) {
                    Intent intent = new Intent(bizProfileActivity, AboutUsActivity.class);
                    intent.putExtra("artistId", bizProfileActivity.artistId);
                    intent.putExtra("bio", bizProfileActivity.profileData.bio);
                    if (bizProfileActivity.artistId.equals(Mualab.getInstance().getSessionManager().getUser().id)) {
                        startActivity(intent);
                    } else {
                        if (!bizProfileActivity.profileData.bio.isEmpty()) {
                            startActivity(intent);
                        } else
                            MyToast.getInstance(getBaseActivity()).showDasuAlert("No about us added");
                    }
                }
                break;

            case R.id.llCertificate:
                if (bizProfileActivity != null && !bizProfileActivity.profileData.certificateCount.equalsIgnoreCase("0")) {
                    Intent intent = new Intent(bizProfileActivity, CertificateActivity.class);
                    intent.putExtra("artistId", bizProfileActivity.artistId);
                    intent.putExtra("from", "profile");
                    startActivity(intent);
                }else MyToast.getInstance(getBaseActivity()).showDasuAlert("No qualifications added");
                break;
        }
    }

}
