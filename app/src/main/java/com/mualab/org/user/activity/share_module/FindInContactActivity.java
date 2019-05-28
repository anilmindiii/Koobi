package com.mualab.org.user.activity.share_module;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.base.BaseActivity;
import com.mualab.org.user.activity.share_module.adapter.ContactAdapter;
import com.mualab.org.user.activity.share_module.model.Contact;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.dialogs.Progress;
import com.mualab.org.user.utils.constants.Constant;

import java.util.ArrayList;
import java.util.List;

public class FindInContactActivity extends BaseActivity {

    private ProgressBar progressbar;
    private TextView tvNoRecord;

    private List<Contact> contactList;
    private List<Contact> backupList;
    private ContactAdapter contactAdapter;

    private String link = "", from = "", flag = "", filePath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_in_contact);

        if (getIntent() != null) {
            //From -> post, voucher
            from = getIntent().getStringExtra("from");

            if (from.equalsIgnoreCase("voucher")) {
                link = getIntent().getStringExtra("link");
            } else {
                link = getIntent().getStringExtra("link");
                flag = getIntent().getStringExtra("type");
                filePath = getIntent().getStringExtra("filePath");
            }
        }

        TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tvHeaderTitle.setText(R.string.find_in_contacts);

        progressbar = findViewById(R.id.progressbar);
        tvNoRecord = findViewById(R.id.tvNoRecord);

        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());

        contactList = new ArrayList<>();
        backupList = new ArrayList<>();

        EditText et_search = findViewById(R.id.et_search);
        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty()) {
                    contactList.clear();
                    contactList.addAll(backupList);
                    updateAdapter();
                } else {
                    filterContact(s.toString());
                }
            }
        });

        if (checkContactPermission(getActivity())) {
            doGetContact();
        }
    }

    public static boolean checkContactPermission(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
// No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    Constant.MY_PERMISSIONS_REQUEST_CONTACT);
            return false;
        } else {
            return true;
        }
    }

    private void doGetContact() {
        Progress.show(getActivity());
        new Thread(this::readContacts).start();
    }

    public void readContacts() {
        ContentResolver cr = getContentResolver();

        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

        assert cur != null;
        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                Contact contact = new Contact();
                contact.id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                contact.name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                contact.photo = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.Photo.PHOTO_URI));

                if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    // get the phone number
                    Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{contact.id}, null);
                    assert pCur != null;
                    while (pCur.moveToNext()) {
                        contact.phone = pCur.getString(
                                pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    }
                    pCur.close();
                }

                if (!contact.phone.isEmpty()) {
                    contactList.add(contact);
                }
            }
            cur.close();
        }

        new Handler(Looper.getMainLooper()).post(this::updateUI);
    }

    private void updateUI() {
        backupList.clear();
        backupList.addAll(contactList);
        Progress.hide(getActivity());
        RecyclerView recyclerview = findViewById(R.id.recyclerview);

        contactAdapter = new ContactAdapter(contactList, pos -> {
           /* if (from.equalsIgnoreCase("voucher")){
                shareOnSocial(pos, link);
            }else{
                shareOnSocial(pos, flag, filePath, link);
            }*/
            shareOnSocial(pos, link);
        });
        recyclerview.setAdapter(contactAdapter);
        tvNoRecord.setVisibility(contactList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void shareOnSocial(int pos, String link) {
        String content;
        if (from.equalsIgnoreCase("voucher")) {
            content = "Enter the Voucher Code during the checkout process on our app Hurry, it doesn't get better than this!\n Tap "
                    .concat(link).concat(" to use my voucher code");
        } else {
            content = "Check this Out\n".concat(link);
        }

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + contactList.get(pos).phone));
        intent.putExtra("sms_body", content);
        startActivity(intent);
    }

    private void shareOnSocial(int pos, String flag, String filePath, String link) {
        if (flag.equalsIgnoreCase("text")) {
            shareOnSocial(pos, link);
        } else {
            Uri uri = Uri.parse(filePath);

            Intent i = new Intent(Intent.ACTION_SEND);
            i.putExtra("address",contactList.get(pos).phone);
            i.putExtra("sms_body",link);
            i.putExtra(Intent.EXTRA_STREAM,"file:/"+uri);
            i.setType("image/png");
            startActivity(i);
        }
    }

    public void filterContact(String text) {
        contactList.clear();
        for (Contact d : backupList) {
            //or use .equal(text) with you want equal match
            //use .toLowerCase() for better matches
            if (d.name.toLowerCase().contains(text.toLowerCase())) {
                contactList.add(d);
            }
        }
        updateAdapter();
    }

    private void updateAdapter() {
        if (contactAdapter != null)
            contactAdapter.notifyDataSetChanged();
        tvNoRecord.setVisibility(contactList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constant.MY_PERMISSIONS_REQUEST_CONTACT) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                doGetContact();
            } else {
                updateAdapter();
                MyToast.getInstance(getActivity()).showSmallMessage("Need permission to read contact");
            }
        }
    }
}
