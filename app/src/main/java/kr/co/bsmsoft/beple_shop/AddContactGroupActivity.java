package kr.co.bsmsoft.beple_shop;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;
import kr.co.bsmsoft.beple_shop.adapter.ContactGroupListAdapter;
import kr.co.bsmsoft.beple_shop.adapter.ContactListAdapter;
import kr.co.bsmsoft.beple_shop.common.NetDefine;
import kr.co.bsmsoft.beple_shop.util.SoundSearcher;
import kr.co.bsmsoft.beple_shop.model.ContactGroupModel;
import kr.co.bsmsoft.beple_shop.model.CustomerModel;

public class AddContactGroupActivity extends AppCompatActivity implements NetDefine, View.OnClickListener {

    private final static String TAG = "AddContactGroupActivity";

    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;

    private Toolbar toolbar;
    private ListView contactListView;
    private ContactGroupListAdapter adapter;
    private ContactListAdapter contactAdapter;
    private static ArrayList<ContactGroupModel> contactGroupList;
    private static ArrayList<CustomerModel> contactList;
    private Button btnOK, btnClose;
    private EditText editSearchString;
    private int requestCode;
    SweetAlertDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact_group);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        } else {
            init();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                init();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        requestCode = getIntent().getIntExtra(KEY_REQUEST_CODE, 0);
        Log.i(TAG, "requestCode : " + requestCode);

        switch (requestCode) {
            case REQUEST_CODE_CONTACTS_ACTIVITY:
                setTitle(LABEL_CONTACT);
                contactAdapter = new ContactListAdapter(this, contactList, contactListView);
                contactListView.setAdapter(contactAdapter);
                if(contactList.isEmpty()) new GetContactListTask().execute();
                contactAdapter.notifyDataSetChanged();
                break;
            case REQUEST_CODE_CONTACTS_GROUP_ACTIVITY:
                setTitle(LABEL_CONTACT_GROUP);
                adapter = new ContactGroupListAdapter(this, contactGroupList, contactListView);
                contactListView.setAdapter(adapter);
                if(contactGroupList.isEmpty()) new GetContactListTask().execute();
                break;
        }
    }

    private void init() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        if(contactList == null) contactList = new ArrayList<CustomerModel>();
        if(contactGroupList == null) contactGroupList = new ArrayList<ContactGroupModel>();
        contactListView = (ListView) findViewById(R.id.contactGroupListView);
        contactListView.setDivider(null);
        
        btnOK = (Button) findViewById(R.id.btnContactGroupOk);
        btnClose = (Button) findViewById(R.id.btnContactGroupClose);
        btnOK.setOnClickListener(this);
        btnClose.setOnClickListener(this);
        editSearchString = (EditText) findViewById(R.id.editSearchString);
        editSearchString.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.i(TAG, "editSearchString : afterTextChanged func is called.");
                switch (requestCode) {
                    case REQUEST_CODE_CONTACTS_ACTIVITY:
                        for(CustomerModel item : contactList) {
                            if(SoundSearcher.matchString(item.getCustomerName(), editSearchString.getText().toString())) {
                                item.setVisible(true);
                            } else {
                                item.setVisible(false);
                            }
                        }
                        contactAdapter.notifyDataSetChanged();
                        break;
                    case REQUEST_CODE_CONTACTS_GROUP_ACTIVITY:
                        for(ContactGroupModel item : contactGroupList) {
                            if(SoundSearcher.matchString(item.getGroupName(), editSearchString.getText().toString())) {
                                item.setVisible(true);
                            } else {
                                item.setVisible(false);
                            }
                        }
                        adapter.notifyDataSetChanged();
                        break;
                }
            }
        });
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.btnContactGroupOk) {
            Intent i = new Intent();

            switch (requestCode) {
                case REQUEST_CODE_CONTACTS_ACTIVITY:
                    i.putExtra(KEY_CUSTOMER_LIST, contactList);
                    break;
                case REQUEST_CODE_CONTACTS_GROUP_ACTIVITY:
                    i.putExtra(KEY_CUSTOMER_GROUP_LIST, contactGroupList);
                    break;
            }
            setResult(RESULT_OK, i);
            finish();

        }else if (view.getId() == R.id.btnContactGroupClose) {

            finish();

        }
    }

    public void getContactGroupList() {
        Uri contactUri = ContactsContract.Groups.CONTENT_SUMMARY_URI;
        Cursor cursor = getContentResolver().query(contactUri, new String[] { ContactsContract.Groups._ID, ContactsContract.Groups.TITLE }, null, null, null);

        int count = 0;
        while (cursor.moveToNext()) {
            String group_id = cursor.getString(cursor.getColumnIndex(ContactsContract.Groups._ID));
            String group_title = cursor.getString(cursor.getColumnIndex(ContactsContract.Groups.TITLE));

            Cursor mCursor = getContentResolver().query(ContactsContract.Groups.CONTENT_SUMMARY_URI,
                    new String[]{ContactsContract.Groups.SUMMARY_COUNT},
                    ContactsContract.Groups._ID + " = " + group_id, null, null);
            if(mCursor.moveToFirst()) {
                count = mCursor.getInt(0);
            }

            if(count > 0) {
                ContactGroupModel contactGroup = new ContactGroupModel();
                contactGroup.setGroupId(group_id);
                contactGroup.setGroupName(group_title);
                contactGroup.setGroupMember( getContactList(group_id) );
                contactGroupList.add(contactGroup);

                Log.i(TAG, "getContactGroupList() : group_id[" + group_id + "]");
                Log.i(TAG, "getContactGroupList() : group_title[" + group_title + "]");
                Log.i(TAG, "getContactGroupList() : count[" + count + "]");
            }
        }
    }

    public ArrayList<CustomerModel> getContactList(String groupID) {

        ArrayList<CustomerModel> contactList = new ArrayList<CustomerModel>();
        Uri groupURI = ContactsContract.Data.CONTENT_URI;
        String[] projection = new String[] {
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID };

        Cursor c;

        if(groupID != null) {
            c = getContentResolver().query(
                    groupURI,
                    projection,
                    ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID
                            + "=" + groupID, null, null);

            while (c.moveToNext()) {
                String id = c
                        .getString(c
                                .getColumnIndex(ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID));
                Cursor pCur = getContentResolver().query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        new String[] { id }, null);

                if(pCur.getCount() > 0) {
                    while (pCur.moveToNext()) {
                        CustomerModel data = new CustomerModel();
                        data.setCustomerName(pCur
                                .getString(pCur
                                        .getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)));
                        data.setPhone(pCur
                                .getString(pCur
                                        .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                        contactList.add(data);
                        break; // 최상위 번호 1개만 추가
                    }
                }
                pCur.close();
            }
        } else {
            c = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,
                    null, null, null, null);

            while (c.moveToNext()) {
                String id = c
                        .getString(c
                                .getColumnIndex(ContactsContract.Contacts._ID));
                Cursor pCur = getContentResolver().query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        new String[] { id }, null);

                if(pCur.getCount() > 0) {
                    while (pCur.moveToNext()) {
                        CustomerModel data = new CustomerModel();
                        data.setCustomerName(pCur
                                .getString(pCur
                                        .getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)));
                        data.setPhone(pCur
                                .getString(pCur
                                        .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                        contactList.add(data);
                        Log.i(TAG, "contactList Add : " + data.getCustomerName() + " / " + data.getPhone());
                        break; // 최상위 번호 1개만 추가
                    }
                }
                pCur.close();
            }
        }

        c.close();
        return contactList;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class GetContactListTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            switch (requestCode) {
                case REQUEST_CODE_CONTACTS_ACTIVITY:
                    contactList.addAll(getContactList(null));
                    break;
                case REQUEST_CODE_CONTACTS_GROUP_ACTIVITY:
                    getContactGroupList();
                    break;
            }

            Message msg = UIUpdateHandler.obtainMessage();
            UIUpdateHandler.sendMessage(msg);

            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            pDialog.dismissWithAnimation();
        }

        @Override
        protected void onPreExecute() {
            pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            pDialog.setTitleText("연락처를 불러오는 중입니다.");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected void onProgressUpdate(Void... values) {}
    }

    final Handler UIUpdateHandler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            switch (requestCode) {
                case REQUEST_CODE_CONTACTS_ACTIVITY:
                    contactAdapter.notifyDataSetChanged();
                    break;
                case REQUEST_CODE_CONTACTS_GROUP_ACTIVITY:
                    adapter.notifyDataSetChanged();
                    break;
            }
        }
    };
}
