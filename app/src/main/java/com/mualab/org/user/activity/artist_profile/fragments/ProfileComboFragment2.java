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
import com.mualab.org.user.activity.artist_profile.activity.ArtistProfileActivity;
import com.mualab.org.user.activity.artist_profile.activity.FollowersActivity;
import com.mualab.org.user.activity.artist_profile.model.UserProfileData;
import com.mualab.org.user.activity.base.BaseFragment;
import com.mualab.org.user.utils.constants.Constant;

import java.io.Serializable;


public class ProfileComboFragment2 extends BaseFragment implements View.OnClickListener {


    private ArtistProfileActivity bizProfileActivity;
    private UserProfileData profileData;


    public static ProfileComboFragment2 newInstance(UserProfileData profileData) {
        Bundle args = new Bundle();
        ProfileComboFragment2 fragment = new ProfileComboFragment2();
        fragment.setArguments(args);
        args.putSerializable("profileData",profileData);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile_combo_fragment2, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(getActivity() != null){
            bizProfileActivity = (ArtistProfileActivity) getActivity();
        }

        TextView tv_profile_followers = view.findViewById(R.id.tv_profile_followers);
        TextView tv_profile_followers_txt = view.findViewById(R.id.tv_profile_followers_txt);

        TextView tv_profile_following = view.findViewById(R.id.tv_profile_following);
        TextView tv_profile_following_txt = view.findViewById(R.id.tv_profile_following_txt);

        TextView tv_profile_post = view.findViewById(R.id.tv_profile_post);
        TextView tv_profile_post_txt = view.findViewById(R.id.tv_profile_post_txt);

        if(getArguments() != null){
            profileData = (UserProfileData) getArguments().getSerializable("profileData");

            tv_profile_followers.setText(profileData.followersCount+"");
            tv_profile_followers_txt.setText(Constant.adds(Integer.parseInt(profileData.followersCount),"Follower"));

            tv_profile_following.setText(profileData.followingCount+"");
            //tv_profile_following_txt.setText(Constant.adds(Integer.parseInt(profileData.followingCount),"Following"));

            tv_profile_post.setText(profileData.postCount+"");
            tv_profile_post_txt.setText(Constant.adds(Integer.parseInt(profileData.postCount),"Post"));

        }

        view.findViewById(R.id.llFollowers).setOnClickListener(this);
        view.findViewById(R.id.llFollowing).setOnClickListener(this);
        view.findViewById(R.id.llPost).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.llFollowing:
                if (bizProfileActivity != null /*&& bizProfileActivity.verifyProfile()*/) {
                    Intent intent = new Intent(bizProfileActivity, FollowersActivity.class);
                    intent.putExtra("isFollowers", false);
                    intent.putExtra("artistId", bizProfileActivity.artistId);
                    startActivityForResult(intent, 10);
                }
                break;

            case R.id.llFollowers:
                if (bizProfileActivity != null /*&& bizProfileActivity.verifyProfile()*/) {
                    Intent intent = new Intent(bizProfileActivity, FollowersActivity.class);
                    intent.putExtra("isFollowers", true);
                    intent.putExtra("artistId", bizProfileActivity.artistId);
                    startActivityForResult(intent, 10);
                    //startActivity(new Intent(mContext,FollowersActivity.class));
                }
                break;

            case R.id.llPost:
                if (bizProfileActivity != null) bizProfileActivity.updateViewType(R.id.ly_feeds);
                break;
        }
    }
}
