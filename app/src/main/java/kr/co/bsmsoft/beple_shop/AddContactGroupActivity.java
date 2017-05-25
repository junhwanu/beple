package kr.co.bsmsoft.beple_shop;

import android.content.Context;
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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import cn.pedant.SweetAlert.SweetAlertDialog;
import kr.co.bsmsoft.beple_shop.adapter.ContactGroupListAdapter;
import kr.co.bsmsoft.beple_shop.adapter.ContactListAdapter;
import kr.co.bsmsoft.beple_shop.common.Helper;
import kr.co.bsmsoft.beple_shop.common.NetDefine;
import kr.co.bsmsoft.beple_shop.model.EventModel;
import kr.co.bsmsoft.beple_shop.model.GroupModel;
import kr.co.bsmsoft.beple_shop.net.AbServerTask;
import kr.co.bsmsoft.beple_shop.net.EventTask;
import kr.co.bsmsoft.beple_shop.net.GroupTask;
import kr.co.bsmsoft.beple_shop.util.SoundSearcher;
import kr.co.bsmsoft.beple_shop.model.ContactGroupModel;
import kr.co.bsmsoft.beple_shop.model.CustomerModel;

public class AddContactGroupActivity extends AppCompatActivity implements NetDefine, View.OnClickListener {

    private final static String TAG = "AddContactGroupActivity";

    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    private static final int MSG_LOAD_GROUP_LIST = 1;
    private static final int MSG_LOAD_GROUP_MEMBER = 2;

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
    private MainApp mainApp;
    private Context context;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {

                case MSG_LOAD_GROUP_LIST: {

                    GroupTask task = new GroupTask(mainApp.getToken());

                    task.mCallbacks = new AbServerTask.ServerCallbacks(){

                        @Override
                        public void onSuccess(AbServerTask sender, JSONObject ret) {

                            try {
                                int code = GroupTask.responseCode(ret);
                                if (code == RESPONSE_OK) {

                                    ArrayList<ContactGroupModel> groupList = GroupTask.parseGroupListToContactGroup(ret);
                                    for(int i=0;i<groupList.size();i++) {
                                        Log.i(TAG, "group_nm : " + groupList.get(i).getGroupName());
                                        Log.i(TAG, "member_count : " + groupList.get(i).getGroupMember().size());
                                    }
                                    updateAdapter(groupList);

                                }else{
                                    Helper.sweetAlert(getString(R.string.alert_title),
                                            GroupTask.responseMessage(ret),
                                            SweetAlertDialog.ERROR_TYPE,
                                            context);
                                }

                            } catch (JSONException e) {
                                Helper.alert(e.getLocalizedMessage(), context);
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailed(AbServerTask sender, int code, String msg) {
                            Helper.sweetAlert(getString(R.string.cannot_connect_server),
                                    getString(R.string.alert_title),
                                    SweetAlertDialog.ERROR_TYPE,
                                    context);
                        }

                    };

                    Log.i("AddContactGroupActivity", "shop id is " + mainApp.getShopInfo().getId());
                    task.getGroupList(mainApp.getShopInfo().getId());
                    break;
                }

                case MSG_LOAD_GROUP_MEMBER: {

                    GroupTask task = new GroupTask(mainApp.getToken());

                    task.mCallbacks = new AbServerTask.ServerCallbacks(){

                        @Override
                        public void onSuccess(AbServerTask sender, JSONObject ret) {

                            try {
                                int code = GroupTask.responseCode(ret);
                                if (code == RESPONSE_OK) {

                                    ArrayList<CustomerModel> customers = GroupTask.parseGroupMember(ret);

                                    if(customers.size() > 0) {
                                        for (int i = 0; i < contactGroupList.size(); i++) {
                                            if (contactGroupList.get(i).getId() == customers.get(0).getGroupId()) {
                                                contactGroupList.get(i).setGroupMember(customers);
                                                break;
                                            }
                                        }
                                    }
                                    adapter.notifyDataSetChanged();

                                }else{
                                    Helper.sweetAlert(getString(R.string.alert_title),
                                            GroupTask.responseMessage(ret),
                                            SweetAlertDialog.ERROR_TYPE,
                                            context);
                                }

                            } catch (JSONException e) {
                                Helper.alert(e.getLocalizedMessage(), context);
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailed(AbServerTask sender, int code, String msg) {
                            Helper.sweetAlert(getString(R.string.cannot_connect_server),
                                    getString(R.string.alert_title),
                                    SweetAlertDialog.ERROR_TYPE,
                                    context);
                        }

                    };

                    task.getGroupMember(msg.arg1);
                    break;
                }

            }
        }
    };

    private void updateAdapter(ArrayList<ContactGroupModel> groups) {

        if (groups != null) {
            adapter.addAll(groups);
        }
    }

    private void initDataSet() {
        adapter.clear();
        adapter.notifyDataSetChanged();
        mHandler.obtainMessage(MSG_LOAD_GROUP_LIST).sendToTarget();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact_group);

        mainApp = globalVar.getInstance();
        context = (Context) this;
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

        try {
            switch (requestCode) {
                case REQUEST_CODE_CONTACTS_ACTIVITY:
                    setTitle(LABEL_CONTACT);
                    contactAdapter = new ContactListAdapter(this, contactList, contactListView);
                    contactListView.setAdapter(contactAdapter);
                    if (contactList.isEmpty()) new GetContactListTask().execute();
                    else for (CustomerModel item : contactList) {
                        item.isSelected(0);
                        item.setVisible(true);
                    }
                    contactAdapter.notifyDataSetChanged();
                    break;
                case REQUEST_CODE_CONTACTS_GROUP_ACTIVITY:
                    setTitle(LABEL_CONTACT_GROUP);
                    adapter = new ContactGroupListAdapter(this, contactGroupList, contactListView);
                    contactListView.setAdapter(adapter);
                    if (!contactGroupList.isEmpty()) contactGroupList.clear();
                    new GetContactListTask().execute();
                    break;
                case REQUEST_CODE_SHOP_GROUP_ACTIVITY:
                    setTitle(LABEL_SHOP_GROUP);
                    adapter = new ContactGroupListAdapter(this, contactGroupList, contactListView);
                    contactListView.setAdapter(adapter);
                    if (!contactGroupList.isEmpty()) contactGroupList.clear();
                    new GetContactListTask().execute();
                    break;
            }
        } catch(Exception e) {
            e.printStackTrace();
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
        Log.i(TAG, "contactListView.setOnItemClickListener.");
        contactListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (requestCode) {
                    case REQUEST_CODE_CONTACTS_ACTIVITY:
                        Log.i(TAG, "contactList.get(position).isSelected() : " + contactList.get(position).isSelected());
                        if( contactList.get(position).isSelected() == 1 ) contactList.get(position).isSelected(0);
                        else contactList.get(position).isSelected(1);
                        contactAdapter.notifyDataSetChanged();
                        break;
                    case REQUEST_CODE_CONTACTS_GROUP_ACTIVITY:
                        if( contactGroupList.get(position).isSelected() == 1) contactGroupList.get(position).isSelected(0);
                        else contactGroupList.get(position).isSelected(1);
                        adapter.notifyDataSetChanged();
                        break;
                    case REQUEST_CODE_SHOP_GROUP_ACTIVITY:
                        if( contactGroupList.get(position).isSelected() == 1) contactGroupList.get(position).isSelected(0);
                        else contactGroupList.get(position).isSelected(1);
                        adapter.notifyDataSetChanged();
                        break;
                }
            }
        });
        
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
                    case REQUEST_CODE_SHOP_GROUP_ACTIVITY:
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
                case REQUEST_CODE_SHOP_GROUP_ACTIVITY:
                    ArrayList<ContactGroupModel> arr = new ArrayList<>();
                    for(int idx=0;idx<contactGroupList.size();idx++) {
                        if(contactGroupList.get(idx).isSelected() == 1) {
                            arr.add(contactGroupList.get(idx));
                        }
                    }
                    i.putExtra(KEY_CUSTOMER_GROUP_LIST, arr);
                    break;
            }
            setResult(RESULT_OK, i);
            finish();

        }else if (view.getId() == R.id.btnContactGroupClose) {

            finish();

        }
    }

    public void getShopGroupList() {
        initDataSet();
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
                        data.isSelected(1);
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

    static class GroupNameAscCompare implements Comparator<ContactGroupModel> {

        @Override
        public int compare(ContactGroupModel arg0, ContactGroupModel arg1) {
            // TODO Auto-generated method stub
            return arg0.getGroupName().compareTo(arg1.getGroupName());
        }

    }

    static class NameAscCompare implements Comparator<CustomerModel> {

        @Override
        public int compare(CustomerModel arg0, CustomerModel arg1) {
            // TODO Auto-generated method stub
            return arg0.getCustomerName().compareTo(arg1.getCustomerName());
        }

    }

    private class GetContactListTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            switch (requestCode) {
                case REQUEST_CODE_CONTACTS_ACTIVITY:
                    contactList.addAll(getContactList(null));
                    Collections.sort(contactList, new NameAscCompare());
                    break;
                case REQUEST_CODE_CONTACTS_GROUP_ACTIVITY:
                    getContactGroupList();
                    Collections.sort(contactGroupList, new GroupNameAscCompare());
                    break;
                case REQUEST_CODE_SHOP_GROUP_ACTIVITY:
                    getShopGroupList();
                    Collections.sort(contactGroupList, new GroupNameAscCompare());
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
                case REQUEST_CODE_SHOP_GROUP_ACTIVITY:
                    adapter.notifyDataSetChanged();
                    break;
            }
        }
    };
}
