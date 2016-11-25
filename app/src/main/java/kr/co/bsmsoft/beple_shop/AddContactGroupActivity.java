package kr.co.bsmsoft.beple_shop;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.regex.Pattern;

import cn.pedant.SweetAlert.SweetAlertDialog;
import kr.co.bsmsoft.beple_shop.adapter.ContactGroupListAdapter;
import kr.co.bsmsoft.beple_shop.common.Helper;
import kr.co.bsmsoft.beple_shop.common.NetDefine;
import kr.co.bsmsoft.beple_shop.model.ContactGroupModel;
import kr.co.bsmsoft.beple_shop.model.CustomerModel;

public class AddContactGroupActivity extends AppCompatActivity implements NetDefine, View.OnClickListener {

    private final static String TAG = "AddContactGroupActivity";

    private Toolbar toolbar;
    private ListView contactListView;
    private ContactGroupListAdapter adapter;
    private ArrayList<ContactGroupModel> contactGroupList;
    private Button btnOK, btnClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact_group);

        init();
        getContactGroupList();
    }

    private void init() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        contactGroupList = new ArrayList<>();
        contactListView = (ListView) findViewById(R.id.contactGroupListView);
        adapter = new ContactGroupListAdapter(this, contactGroupList, contactListView);
        contactListView.setAdapter(adapter);

        btnOK = (Button) findViewById(R.id.btnContactGroupOk);
        btnClose = (Button) findViewById(R.id.btnContactGroupClose);
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.btnOk) {

            Intent i = new Intent();
            i.putExtra(KEY_CUSTOMER_GROUP_LIST, contactGroupList);
            setResult(RESULT_OK, i);

            finish();

        }else if (view.getId() == R.id.btnClose) {

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

        Cursor c = getContentResolver().query(
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

            while (pCur.moveToNext()) {
                CustomerModel data = new CustomerModel();
                data.setCustomerName(pCur
                        .getString(pCur
                                .getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)));
                data.setPhone(pCur
                        .getString(pCur
                                .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                contactList.add(data);
            }

            pCur.close();
        }

        return contactList;
    }
}
