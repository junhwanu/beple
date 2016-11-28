package kr.co.bsmsoft.beple_shop;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
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
import java.util.regex.Pattern;

import cn.pedant.SweetAlert.SweetAlertDialog;
import kr.co.bsmsoft.beple_shop.adapter.ContactGroupListAdapter;
import kr.co.bsmsoft.beple_shop.adapter.ContactListAdapter;
import kr.co.bsmsoft.beple_shop.common.Helper;
import kr.co.bsmsoft.beple_shop.common.NetDefine;
import kr.co.bsmsoft.beple_shop.common.SoundSearcher;
import kr.co.bsmsoft.beple_shop.model.ContactGroupModel;
import kr.co.bsmsoft.beple_shop.model.CustomerModel;

public class AddContactGroupActivity extends AppCompatActivity implements NetDefine, View.OnClickListener {

    private final static String TAG = "AddContactGroupActivity";

    private Toolbar toolbar;
    private ListView contactListView;
    private ContactGroupListAdapter adapter;
    private ContactListAdapter contactAdapter;
    private ArrayList<ContactGroupModel> contactGroupList;
    private ArrayList<CustomerModel> contactList;
    private Button btnOK, btnClose;
    private EditText editSearchString;
    private int requestCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact_group);

        init();
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
                if(contactList.isEmpty()) contactList.addAll(getContactList(null));
                contactAdapter.notifyDataSetChanged();
                break;
            case REQUEST_CODE_CONTACTS_GROUP_ACTIVITY:
                setTitle(LABEL_CONTACT_GROUP);
                adapter = new ContactGroupListAdapter(this, contactGroupList, contactListView);
                contactListView.setAdapter(adapter);
                if(contactGroupList.isEmpty()) getContactGroupList();
                break;
        }
    }

    private void init() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        contactList = new ArrayList<CustomerModel>();
        contactGroupList = new ArrayList<ContactGroupModel>();
        contactListView = (ListView) findViewById(R.id.contactGroupListView);

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

        adapter.notifyDataSetChanged();
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
}
