package com.mualab.org.user.activity.feeds.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.feeds.adapter.FolderItemAdapter;
import com.mualab.org.user.activity.feeds.listener.SaveToFolderListner;
import com.mualab.org.user.activity.feeds.model.FolderInfo;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.dialogs.Progress;
import com.mualab.org.user.utils.ConnectionDetector;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SaveToFolderActivity extends AppCompatActivity {

    private RecyclerView recycler_view;
    private FolderItemAdapter adapter;
    private ArrayList<FolderInfo.DataBean> folderList;
    private AppCompatButton btnSave;
    private int feedId = -1;
    private int pos = 0;
    private boolean fromSlider;
    private ImageView btnAdd;
    private TextView tvNoCardAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_to_folder);

        if (getIntent().getStringExtra("from") != null) {
            if (getIntent().getStringExtra("from").equals("slider"))
                fromSlider = true;
        } else {
            feedId = getIntent().getIntExtra("feedId", feedId);
            pos = getIntent().getIntExtra("pos", 0);
        }

        recycler_view = findViewById(R.id.recycler_view);
        btnSave = findViewById(R.id.btnSave);
        btnAdd = findViewById(R.id.ic_add_chat);
        tvNoCardAdd = findViewById(R.id.tvNoCardAdd);
        btnAdd.setVisibility(View.VISIBLE);

        TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tvHeaderTitle.setText(getString(R.string.save_to_folder));

        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


        LinearLayoutManager manager = new LinearLayoutManager(this);
        folderList = new ArrayList<>();
        adapter = new FolderItemAdapter(btnSave,fromSlider, this, folderList, new FolderItemAdapter.clickListner() {
            @Override
            public void getClick(FolderInfo.DataBean dataBean, String Tag, int pos) {
                if (Tag.equals("OnClickCell")) {
                    // slider click
                    if(!fromSlider){
                        if (dataBean._id != 0) {
                            if(folderList.get(pos).isSelected){
                                for (FolderInfo.DataBean bean : folderList) {
                                    bean.isSelected = false;
                                }
                                folderList.get(pos).isSelected = false;
                                btnSave.setVisibility(View.GONE);
                            }else {
                                for (FolderInfo.DataBean bean : folderList) {
                                    bean.isSelected = false;
                                }
                                folderList.get(pos).isSelected = true;
                                btnSave.setVisibility(View.VISIBLE);
                            }


                            adapter.notifyDataSetChanged();


                            btnSave.setOnClickListener(v -> {
                                saveToFolder(String.valueOf(dataBean._id));
                            });
                        }
                        return;
                    }
                    Intent intent = new Intent(SaveToFolderActivity.this, FolderFeedsActivity.class);
                    intent.putExtra("folderId", dataBean._id);
                    intent.putExtra("folderName", dataBean.folderName);
                    startActivity(intent);
                } else {
                    if (Tag.equals("editFolderName")) {
                        addFolDialog(true, dataBean);
                    } else {
                        if (dataBean._id != 0) {
                            if(folderList.get(pos).isSelected){
                                for (FolderInfo.DataBean bean : folderList) {
                                    bean.isSelected = false;
                                }
                                folderList.get(pos).isSelected = false;
                                btnSave.setVisibility(View.GONE);
                            }else {
                                for (FolderInfo.DataBean bean : folderList) {
                                    bean.isSelected = false;
                                }
                                folderList.get(pos).isSelected = true;
                                btnSave.setVisibility(View.VISIBLE);
                            }


                            adapter.notifyDataSetChanged();


                            btnSave.setOnClickListener(v -> {
                                saveToFolder(String.valueOf(dataBean._id));
                            });
                        }
                    }
                }


            }
        });
        recycler_view.setLayoutManager(manager);
        recycler_view.setAdapter(adapter);


        btnAdd.setOnClickListener(v -> {
            addFolDialog(false, null);
        });


        if (fromSlider) {
            btnAdd.setVisibility(View.GONE);
            btnSave.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getAllFolder();
    }

    private void addFolDialog(boolean isForEdit, FolderInfo.DataBean dataBean) {
        final Dialog dialog = new Dialog(SaveToFolderActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.create_folder_dialog);
        Window window = dialog.getWindow();
        assert window != null;
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        EditText ed_folder_name = dialog.findViewById(R.id.ed_folder_name);
        ImageView iv_cancel = dialog.findViewById(R.id.iv_cancel);
        iv_cancel.setOnClickListener(v -> {
            dialog.cancel();
        });

        if (isForEdit) {
            ed_folder_name.setText(dataBean.folderName);
            ed_folder_name.setSelection(dataBean.folderName.length());
        }

        Button btn_done = dialog.findViewById(R.id.btn_done);
        btn_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String folderName = ed_folder_name.getText().toString().trim();
                if (!folderName.equals("")) {
                    if (isForEdit) {
                        editFolder(folderName, String.valueOf(dataBean._id));
                    } else {
                        createFolder(folderName);
                    }

                    dialog.dismiss();
                } else
                    MyToast.getInstance(SaveToFolderActivity.this).showDasuAlert("Please enter folder name");

            }
        });
        dialog.show();
    }

    private void getAllFolder() {
        //Progress.show(SaveToFolderActivity.this);
        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(SaveToFolderActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        getAllFolder();
                    }
                }
            }).show();
        }

        new HttpTask(new HttpTask.Builder(this, "getFolder", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {

                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    Progress.hide(SaveToFolderActivity.this);

                    if (status.equalsIgnoreCase("success")) {
                        Gson gson = new Gson();
                        folderList.clear();
                        FolderInfo folderInfo = gson.fromJson(response, FolderInfo.class);

                        folderList.addAll(folderInfo.data);
                        adapter.notifyDataSetChanged();
                    } else {
                        // goto 3rd screen for register
                    }

                    if(folderList.size()==0){
                        tvNoCardAdd.setVisibility(View.VISIBLE);
                    }else tvNoCardAdd.setVisibility(View.GONE);

                } catch (Exception e) {
                    e.printStackTrace();
                    Progress.hide(SaveToFolderActivity.this);
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void ErrorListener(VolleyError error) {
                Progress.hide(SaveToFolderActivity.this);
            }
        })
                .setMethod(Request.Method.GET)
                .setProgress(true))
                .execute(this.getClass().getName());

    }

    //Create folder
    private void createFolder(final String folderName) {
        Progress.show(SaveToFolderActivity.this);
        Map<String, String> map = new HashMap<>();
        map.put("folderName", folderName);

        new HttpTask(new HttpTask.Builder(SaveToFolderActivity.this, "createFolder", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                Progress.hide(SaveToFolderActivity.this);
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    if (status.equalsIgnoreCase("success")) {
                        getAllFolder();
                    }else if(message.equals("Folder already exist")){
                        MyToast.getInstance(SaveToFolderActivity.this).showDasuAlert("You can not create multiple folder with same name");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Progress.hide(SaveToFolderActivity.this);
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                Progress.hide(SaveToFolderActivity.this);
            }
        }).setAuthToken(Mualab.currentUser.authToken)
                .setParam(map)
                .setMethod(Request.Method.POST)
                .setProgress(false)
                .setBodyContentType(HttpTask.ContentType.X_WWW_FORM_URLENCODED))
                .execute("createFolder");


    }

    private void editFolder(final String folderName, final String folderId) {
        Progress.show(SaveToFolderActivity.this);
        Map<String, String> map = new HashMap<>();
        map.put("folderName", folderName);
        map.put("folderId", folderId);

        new HttpTask(new HttpTask.Builder(SaveToFolderActivity.this, "editFolder", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                Progress.hide(SaveToFolderActivity.this);
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    //String message = js.getString("message");
                    if (status.equalsIgnoreCase("success")) {
                        Progress.hide(SaveToFolderActivity.this);
                        getAllFolder();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Progress.hide(SaveToFolderActivity.this);
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                Progress.hide(SaveToFolderActivity.this);
            }
        }).setAuthToken(Mualab.currentUser.authToken)
                .setParam(map)
                .setMethod(Request.Method.POST)
                .setProgress(false)
                .setBodyContentType(HttpTask.ContentType.X_WWW_FORM_URLENCODED))
                .execute("editFolder");


    }

    private void saveToFolder(final String folderId) {
        Progress.show(SaveToFolderActivity.this);
        Map<String, String> map = new HashMap<>();
        map.put("feedId", String.valueOf(feedId));
        map.put("folderId", folderId);

        new HttpTask(new HttpTask.Builder(SaveToFolderActivity.this, "saveToFolder", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                Progress.hide(SaveToFolderActivity.this);
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");
                    if (status.equalsIgnoreCase("success")) {
                        SaveToFolderListner.getmInctance().onSave("saveed", pos);
                        Progress.hide(SaveToFolderActivity.this);
                        MyToast.getInstance(SaveToFolderActivity.this).showDasuAlert("Saved to Folder successfully");
                        btnSave.setVisibility(View.GONE);
                        getAllFolder();
                    } else {
                        MyToast.getInstance(SaveToFolderActivity.this).showDasuAlert(message);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Progress.hide(SaveToFolderActivity.this);
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                Progress.hide(SaveToFolderActivity.this);
            }
        }).setAuthToken(Mualab.currentUser.authToken)
                .setParam(map)
                .setMethod(Request.Method.POST)
                .setProgress(false)
                .setBodyContentType(HttpTask.ContentType.X_WWW_FORM_URLENCODED))
                .execute("saveToFolder");


    }

}
