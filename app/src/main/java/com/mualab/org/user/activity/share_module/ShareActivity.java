package com.mualab.org.user.activity.share_module;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.TextView;
import android.widget.Toast;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.base.BaseActivity;
import com.mualab.org.user.activity.share_module.dialog.ShareInvitationDialog;

import java.io.File;

import static com.mualab.org.user.utils.constants.Constant.PERMISSION_REQUEST_CONTACT;

public class ShareActivity extends BaseActivity implements View.OnClickListener {

    private String link = "", from = "", flag = "", filePath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);

        if (getIntent() != null) {
            //From -> post, voucher
            from = getIntent().getStringExtra("from");

            if (from.equalsIgnoreCase("voucher")) {
                tvHeaderTitle.setText(R.string.share_voucher_code);

                link = getIntent().getStringExtra("link");
            } else {
                tvHeaderTitle.setText(R.string.share_post);

                link = getIntent().getStringExtra("link");
                flag = getIntent().getStringExtra("type");
                filePath = getIntent().getStringExtra("filePath");
            }
        }

        findViewById(R.id.btnBack).setOnClickListener(this);
        findViewById(R.id.llShare).setOnClickListener(this);
        findViewById(R.id.llContact).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btnBack:
                onBackPressed();
                break;

            case R.id.llShare:
                ShareInvitationDialog.newInstance(from, link, () -> {
                    String content;
                    if (from.equalsIgnoreCase("voucher")) {
                        content = "Enter the Voucher Code during the checkout process on our app Hurry, " +
                                "it doesn't get better than this!\n Tap " + link + " to use my voucher code";
                        shareOnSocial(content);
                    } else {
                        content = "Check this Out \n" + link;
                        shareOnSocial(flag, filePath, content);
                    }
                }).show(getSupportFragmentManager(), "ShareActivity");
                break;

            case R.id.llContact:
                askForContactPermission();

                break;
        }
    }

    private void shareOnSocial(String text) {
        Intent sharIntent = new Intent(Intent.ACTION_SEND);
        sharIntent.setType("text/plain");
        sharIntent.putExtra(Intent.EXTRA_SUBJECT, "Koobi BIZ Voucher Code");
        sharIntent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(sharIntent, "Share:"));
    }

    private void shareOnSocial(String flag, String filePath, String link) {
        if (flag.equalsIgnoreCase("text")) {
            Intent sharIntent = new Intent(Intent.ACTION_SEND);
            sharIntent.setType("text/plain");
            sharIntent.putExtra(Intent.EXTRA_SUBJECT, "Koobi BIZ");
            sharIntent.putExtra(Intent.EXTRA_TEXT, link);
            startActivity(Intent.createChooser(sharIntent, "Share:"));
        } else {
            File imageFile = new File(filePath);

            Uri uri;
            Intent sharIntent = new Intent(Intent.ACTION_SEND);
            String ext = imageFile.getName().substring(imageFile.getName().lastIndexOf(".") + 1);
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            String type = mime.getMimeTypeFromExtension(ext);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                sharIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                uri = FileProvider.getUriForFile(getActivity(), getPackageName() + ".provider", imageFile);
                sharIntent.setDataAndType(uri, type);
            } else {
                uri = Uri.fromFile(imageFile);
                sharIntent.setDataAndType(uri, type);
            }

            sharIntent.setType("image/png");
            sharIntent.setType("text/plain");
            sharIntent.putExtra(Intent.EXTRA_STREAM, uri);
            sharIntent.putExtra(Intent.EXTRA_SUBJECT, "Koobi BIZ");
            sharIntent.putExtra(Intent.EXTRA_TEXT, link);
            startActivity(Intent.createChooser(sharIntent, "Share:"));
        }
    }

    public void askForContactPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                        Manifest.permission.READ_CONTACTS)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Contacts access needed");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setMessage("please confirm Contacts access");//TODO put real question
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @TargetApi(Build.VERSION_CODES.M)
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            requestPermissions(
                                    new String[]
                                            {Manifest.permission.READ_CONTACTS}
                                    , PERMISSION_REQUEST_CONTACT);
                        }
                    });
                    builder.show();
                    // Show an expanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                } else {

                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.READ_CONTACTS},
                            PERMISSION_REQUEST_CONTACT);

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            }else{
                getContact();
            }
        }
        else{
            getContact();
        }
    }

    private void getContact() {
        Intent intent = new Intent(getActivity(), FindInContactActivity.class);
        if (from.equalsIgnoreCase("voucher")) {
            intent.putExtra("from", from);
            intent.putExtra("link", link);
        } else {
            intent.putExtra("from", from);
            intent.putExtra("type", flag);
            intent.putExtra("filePath", filePath);
            intent.putExtra("link", link);
        }

        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CONTACT: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContact();
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {
                    Toast.makeText(ShareActivity.this,"No permission for contacts",Toast.LENGTH_SHORT);
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }

}}
