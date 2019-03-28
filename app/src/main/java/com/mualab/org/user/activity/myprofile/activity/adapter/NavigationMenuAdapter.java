package com.mualab.org.user.activity.myprofile.activity.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.booking.BookingHisoryActivity;
import com.mualab.org.user.activity.businessInvitaion.activity.InvitationActivity;
import com.mualab.org.user.activity.myprofile.activity.activity.EditProfileActivity;
import com.mualab.org.user.activity.myprofile.activity.model.NavigationItem;
import com.mualab.org.user.activity.payment.AllCardActivity;
import com.mualab.org.user.activity.searchBoard.fragment.SearchBoardFragment;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.chat.ChatHistoryActivity;
import com.mualab.org.user.data.local.prefs.Session;
import com.mualab.org.user.data.model.User;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.dialogs.Progress;
import com.mualab.org.user.utils.ConnectionDetector;
import com.mualab.org.user.utils.constants.Constant;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class NavigationMenuAdapter extends RecyclerView.Adapter<NavigationMenuAdapter.ViewHolder> {
private Activity context;
private List<NavigationItem> navigationItems;
private DrawerLayout drawer;
private Listener listener;
private String sSelect = "";
// Constructor of the class
public NavigationMenuAdapter(Activity context, List<NavigationItem> navigationItems, DrawerLayout drawer, Listener listener) {
        this.context = context;
        this.drawer = drawer;
        this.navigationItems = navigationItems;
        this.listener=listener;
        }

public interface Listener{
    void OnClick(int pos);
}

    @Override
    public int getItemCount() {
        return navigationItems.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_nav_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int listPosition) {

        final NavigationItem item = navigationItems.get(listPosition);

        holder.tvMenuItemName.setText(item.itemName);
        holder.ivMenuItem.setImageResource(item.itemImg);

       /* if (!sSelect.equals("")){
            if (item.itemName.equals(sSelect)){
                holder.ivMenuItem.setColorFilter(getColor(context, R.color.white));
                holder.tvMenuItemName.setTextColor(getColor(context, R.color.white));
                holder.rlItem.setBackgroundColor(getColor(context, R.color.colorPrimary));
                holder.line.setVisibility(View.INVISIBLE);
            }else {
                holder.line.setVisibility(View.VISIBLE);
                holder.ivMenuItem.setColorFilter(getColor(context, R.color.colorPrimary));
                holder.tvMenuItemName.setTextColor(getColor(context, R.color.dark_grey));
                holder.rlItem.setBackgroundColor(getColor(context, R.color.white));
            }
        }*/

    }

class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    TextView tvMenuItemName;
    AppCompatImageView ivMenuItem;
    RelativeLayout rlItem;
    View line;
    private ViewHolder(View itemView)
    {
        super(itemView);

        tvMenuItemName =  itemView.findViewById(R.id.tvMenuItemName);
        ivMenuItem =  itemView.findViewById(R.id.ivMenuItem);
        rlItem = itemView.findViewById(R.id.rlItem);
        line = itemView.findViewById(R.id.line);

        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        drawer.closeDrawers();
        sSelect = "";
        NavigationItem item = navigationItems.get(getAdapterPosition());

        switch(getAdapterPosition()) {
            case 0:
                sSelect = item.itemName;
                context.startActivity(new Intent(context,EditProfileActivity.class));
                context.finish();
                break;

            case 1:
                sSelect = item.itemName;
                context.startActivity(new Intent(context,ChatHistoryActivity.class));
                break;


            case 2:
                sSelect = item.itemName;
                context.startActivity(new Intent(context,BookingHisoryActivity.class));
                break;

            case 3:
                sSelect = item.itemName;
                context.startActivity(new Intent(context,AllCardActivity.class)
                        .putExtra("from","profile"));
                break;

            case 4:
                sSelect = item.itemName;
                Intent intent = new Intent(context,InvitationActivity.class);
                context.startActivity(intent);
                break;

            case 5:
                sSelect = item.itemName;
                final String appPackageName = context.getPackageName(); // package name of the app
                try {
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
                break;

            case 6:
                sSelect = item.itemName;
                MyToast.getInstance(context).showDasuAlert("Under development");
                break;

            case 7:

                sSelect = item.itemName;
                SearchBoardFragment.isFavClick = false;
                listener.OnClick(getAdapterPosition());

                break;

            case 8:
                apifortokenUpdate();
                break;
            default:
        }

    }


}

    private void apifortokenUpdate() {
        Session session = Mualab.getInstance().getSessionManager();
        final User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(context, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        apifortokenUpdate();
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        params.put("deviceToken", "");
        params.put("firebaseToken", "");
        //  params.put("followerId", String.valueOf(user.id));
        // params.put("loginUserId", String.valueOf(user.id));

        HttpTask task = new HttpTask(new HttpTask.Builder(context, "updateRecord", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    if (status.equalsIgnoreCase("success")) {

                        FirebaseDatabase.getInstance().getReference()
                                .child("users").child(String.valueOf(Mualab.currentUser.id))
                                .child("firebaseToken").setValue("");

                        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        assert notificationManager != null;
                        notificationManager.cancelAll();
                        FirebaseAuth.getInstance().signOut();
                        Mualab.getInstance().getSessionManager().logout();


                    } else {
                        MyToast.getInstance(context).showDasuAlert(message);
                    }
                    //  showToast(message);
                } catch (Exception e) {
                    Progress.hide(context);
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                try {
                    com.mualab.org.user.utils.Helper helper = new com.mualab.org.user.utils.Helper();
                    if (helper.error_Messages(error).contains("Session")) {
                        Mualab.getInstance().getSessionManager().logout();
                        // MyToast.getInstance(BookingActivity.this).showDasuAlert(helper.error_Messages(error));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        })
                .setAuthToken(user.authToken)
                .setProgress(false)
                .setBody(params, HttpTask.ContentType.APPLICATION_JSON));
        //.setBody(params, "application/x-www-form-urlencoded"));

        task.execute(this.getClass().getName());
    }

}