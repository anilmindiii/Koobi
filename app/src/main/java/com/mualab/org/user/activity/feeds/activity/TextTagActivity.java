package com.mualab.org.user.activity.feeds.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.explore.model.ExSearchTag;
import com.mualab.org.user.activity.feeds.adapter.TagTextHorizontalAdapter;
import com.mualab.org.user.activity.feeds.adapter.TextTagAdapter;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.local.prefs.Session;
import com.mualab.org.user.data.model.User;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.listener.RecyclerViewScrollListener;
import com.mualab.org.user.utils.KeyboardUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TextTagActivity extends AppCompatActivity {

    private RecyclerViewScrollListener endlesScrollListener;
    private RecyclerView rcv_horizontal;
    private String searchKeyword = "";
    private List<ExSearchTag> list;
    private List<ExSearchTag> tempTxtTagHoriList;
    private LinearLayout ll_loadingBox;
    private ProgressBar progress_bar;
    private TextView tv_msg,tv_done;
    private TextTagAdapter textTagAdapter;
    private TagTextHorizontalAdapter adapter;
    private String uniTextUserNames = "";
    private View view_div;
    private Map<String,ExSearchTag> idsMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_tag);

        EditText searchview = findViewById(R.id.ed_search);
        ll_loadingBox = findViewById(R.id.ll_loadingBox);
        rcv_horizontal = findViewById(R.id.rcv_horizontal);
        progress_bar = findViewById(R.id.progress_bar);
        tv_msg = findViewById(R.id.tv_msg);
        tv_done = findViewById(R.id.tv_done);
        view_div = findViewById(R.id.view_div);
        RecyclerView mRecyclerViewSomeOneToBeTagged = findViewById(R.id.rv_some_one_to_be_tagged);
        list = new ArrayList<>();
        tempTxtTagHoriList = new ArrayList<>();
        idsMap = new HashMap<>();

        findViewById(R.id.iv_back_press).setOnClickListener(v -> onBackPressed());

        tv_done.setOnClickListener(v -> {
            for(int i=0;i<tempTxtTagHoriList.size();i++){
                uniTextUserNames = " @"+ tempTxtTagHoriList.get(i).uniTxt + uniTextUserNames;
            }

            if (uniTextUserNames.startsWith(" @")) {
                uniTextUserNames = uniTextUserNames.startsWith(" @") ? uniTextUserNames.substring(1) : uniTextUserNames;
            }

            Intent intent = new Intent();
            intent.putExtra("idsMap", (Serializable) idsMap);
            intent.putExtra("caption",uniTextUserNames);
            intent.putExtra("tagCount",tempTxtTagHoriList.size());
            intent.putExtra("tempTxtTagHoriList", (Serializable) tempTxtTagHoriList);
            setResult(RESULT_OK,intent);
            finish();
        });


        if(getIntent().getSerializableExtra("tempTxtTagHoriList") != null){
            tempTxtTagHoriList = (List<ExSearchTag>) getIntent().getSerializableExtra("tempTxtTagHoriList");
            idsMap = (Map<String, ExSearchTag>) getIntent().getSerializableExtra("idsMap");
            InnerAdapter(tempTxtTagHoriList);
            // adapter.notifyDataSetChanged();
            if(tempTxtTagHoriList.size() != 0){
                view_div.setVisibility(View.VISIBLE);
            }else view_div.setVisibility(View.GONE);
        }


        searchview.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                String newText = s.toString().trim();
                searchKeyword = newText;
                list.clear();
                textTagAdapter.notifyDataSetChanged();
                endlesScrollListener.resetState();
                callSearchPeopleAPI(newText, 0);

            }
        });

        textTagAdapter = new TextTagAdapter(idsMap,TextTagActivity.this, list, (tempTxtTagList) -> {
           // allIds = ids;
            tempTxtTagHoriList = tempTxtTagList;

            InnerAdapter(tempTxtTagHoriList);

            if(tempTxtTagHoriList.size() != 0){
                view_div.setVisibility(View.VISIBLE);
            }else view_div.setVisibility(View.GONE);

        },tempTxtTagHoriList);

        mRecyclerViewSomeOneToBeTagged.setAdapter(textTagAdapter);
        LinearLayoutManager lm = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        mRecyclerViewSomeOneToBeTagged.setLayoutManager(lm);

        endlesScrollListener = new RecyclerViewScrollListener(lm) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                //textTagAdapter.showHideLoading(true);
                callSearchPeopleAPI(searchKeyword, page);
            }

            @Override
            public void onScroll(RecyclerView view, int dx, int dy) {
                if (dy > 0 ) {
                    KeyboardUtil.hideKeyboard(view, TextTagActivity.this);
                }

            }
        };
        endlesScrollListener.resetState();
        mRecyclerViewSomeOneToBeTagged.addOnScrollListener(endlesScrollListener);

        if (list.size()==0)
            callSearchPeopleAPI("", 0);
    }

    private void InnerAdapter(List<ExSearchTag> tempTxtTagList) {
        adapter = new TagTextHorizontalAdapter(idsMap,tempTxtTagList, TextTagActivity.this, searchTag -> {

            for(int i =0;i<list.size();i++){
                if(list.get(i).id == searchTag.id){
                    list.get(i).isCheck = false;
                }
            }

            textTagAdapter.notifyDataSetChanged();

            if(tempTxtTagHoriList.size() != 0){
                view_div.setVisibility(View.VISIBLE);
            }else view_div.setVisibility(View.GONE);
        });


        LinearLayoutManager manager = new LinearLayoutManager(TextTagActivity.this, RecyclerView.HORIZONTAL, false);
        rcv_horizontal.setLayoutManager(manager);
        rcv_horizontal.setAdapter(adapter);
    }


    private void callSearchPeopleAPI(final String searchKeyWord, int pageNo){
        progress_bar.setVisibility(View.VISIBLE);
        Session session = Mualab.getInstance().getSessionManager();
        User user = session.getUser();

        Map<String, String> params = new HashMap<>();
        params.put("userId", String.valueOf(user.id));
        params.put("type", "people");
        params.put("page", ""+pageNo);
        params.put("limit", "20");
        params.put("search", searchKeyWord);
        //String tag = TAG + exSearchType;
        Mualab.getInstance().cancelPendingRequests("people");
        new HttpTask(new HttpTask.Builder(TextTagActivity.this, "exploreSearch", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    progress_bar.setVisibility(View.GONE);
                    //mSomeOneAdapter.showHideLoading(false);
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    //String message = js.getString("message");
                    if (status.equalsIgnoreCase("success")) {

                        Gson gson = new Gson();
                        JSONArray array=null;
                        if(js.has("topList"))
                            array= js.getJSONArray("topList");
                        else if(js.has("peopleList"))
                            array= js.getJSONArray("peopleList");
                        else if(js.has("placeList"))
                            array= js.getJSONArray("placeList");
                        else if(js.has("hasTagList"))
                            array= js.getJSONArray("hasTagList");

                        if(array!=null){
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject jsonObject = array.getJSONObject(i);
                                ExSearchTag searchTag = gson.fromJson(String.valueOf(jsonObject), ExSearchTag.class);

                                searchTag.type = 1;
                                searchTag.title = searchTag.uniTxt;
                                searchTag.desc = searchTag.postCount+" post";

                                list.add(searchTag);
                            }
                        }
                        textTagAdapter.notifyDataSetChanged();

                        for (String entry : idsMap.keySet()) {
                            ExSearchTag value = idsMap.get(entry);

                            for(int j =0;j<list.size();j++){
                                String id = String.valueOf(list.get(j).id);
                                if(id.equals(entry)){
                                    list.get(j).isCheck = true;
                                }
                            } }

                        textTagAdapter.notifyDataSetChanged();
                    }
                    tv_msg.setVisibility(View.GONE);
                    if(list.size()==0){
                        tv_msg.setText(getString(R.string.no_data_found));
                    }else {
                        ll_loadingBox.setVisibility(View.GONE);
                    }
                    //  showToast(message);
                } catch (Exception e) {
                    e.printStackTrace();
                    progress_bar.setVisibility(View.GONE);
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                progress_bar.setVisibility(View.GONE);
                //mSomeOneAdapter.showHideLoading(false);
            }})
                .setParam(params)
                .setProgress(false)
                .setBodyContentType(HttpTask.ContentType.X_WWW_FORM_URLENCODED))
                .execute("people");
    }
}
