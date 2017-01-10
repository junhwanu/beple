package kr.co.bsmsoft.beple_shop;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;

import java.util.ArrayList;
import java.util.regex.Pattern;

import cn.pedant.SweetAlert.SweetAlertDialog;
import kr.co.bsmsoft.beple_shop.adapter.PhoneListAdapter;
import kr.co.bsmsoft.beple_shop.common.Helper;
import kr.co.bsmsoft.beple_shop.common.NetDefine;
import kr.co.bsmsoft.beple_shop.model.ContactGroupModel;
import kr.co.bsmsoft.beple_shop.model.CustomerModel;

public class AddPhoneNumberActivity extends AppCompatActivity implements NetDefine, AdapterView.OnItemClickListener, View.OnClickListener {

    private Toolbar toolbar;
    private MainApp mainApp;
    private Button btnClose, btnOk, btnAddPhone, btnContact, btnContactGroup;
    private PhoneListAdapter adapter;
    private ListView customerListView;
    private ArrayList<CustomerModel> customerList;
    private EditText editPhone;
    private PopupWindow pwContact;
    private int mWidthPixels, mHeightPixels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_phone_number);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent i = getIntent();
        customerList = i.getParcelableArrayListExtra(KEY_CUSTOMER_LIST);

        mainApp = globalVar.getInstance();

        customerListView = (ListView)findViewById(R.id.customerListView);
        customerListView.setOnItemClickListener(this);
        adapter = new PhoneListAdapter(this, customerList);
        adapter.setListView(customerListView);
        customerListView.setAdapter(adapter);

        btnOk = (Button)findViewById(R.id.btnOk);
        btnClose = (Button)findViewById(R.id.btnClose);
        btnAddPhone = (Button)findViewById(R.id.btnAddPhone);
        btnContact = (Button)findViewById(R.id.btnContact);
        btnContactGroup = (Button)findViewById(R.id.btnContactGroup);
        editPhone = (EditText)findViewById(R.id.editPhone);

        btnOk.setOnClickListener(this);
        btnClose.setOnClickListener(this);
        btnAddPhone.setOnClickListener(this);
        btnContact.setOnClickListener(this);
        btnContactGroup.setOnClickListener(this);

        // 화면 사이즈 계산
        WindowManager w = getWindowManager();
        Display d = w.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        d.getMetrics(metrics);
        // since SDK_INT = 1;
        mWidthPixels = metrics.widthPixels;
        mHeightPixels = metrics.heightPixels;

        // 상태바와 메뉴바의 크기를 포함해서 재계산
        if (Build.VERSION.SDK_INT >= 14 && Build.VERSION.SDK_INT < 17)
            try {
                mWidthPixels = (Integer) Display.class.getMethod("getRawWidth").invoke(d);
                mHeightPixels = (Integer) Display.class.getMethod("getRawHeight").invoke(d);
            } catch (Exception ignored) {
            }
        // 상태바와 메뉴바의 크기를 포함
        if (Build.VERSION.SDK_INT >= 17)
            try {
                Point realSize = new Point();
                Display.class.getMethod("getRealSize", Point.class).invoke(d, realSize);
                mWidthPixels = realSize.x;
                mHeightPixels = realSize.y;
            } catch (Exception ignored) {

            }
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.btnOk) {

            ArrayList<CustomerModel> ret =  adapter.getItems();
            Intent i = new Intent();
            i.putExtra(KEY_CUSTOMER_LIST, ret);
            setResult(RESULT_OK, i);

            finish();

        }else if (view.getId() == R.id.btnClose) {

            finish();

        }else if (view.getId() == R.id.btnAddPhone) {

            String phone = editPhone.getText().toString();

            String regex = "^01(?:0|1[6-9])(?:\\d{3}|\\d{4})\\d{4}$";
            boolean okPattern = Pattern.matches(regex, phone);
            if (!okPattern) {
                Helper.sweetAlert("핸드폰 번호 형식이 일치하지 않습니다.", "알림", SweetAlertDialog.WARNING_TYPE, this);
                editPhone.setText("");
                return;
            }
            addPhoneNumber(phone, "");
            editPhone.setText("");

        }else if (view.getId() == R.id.btnContact) {

            Intent mIntent = new Intent(AddPhoneNumberActivity.this, AddContactGroupActivity.class);
            mIntent.putExtra(KEY_REQUEST_CODE, REQUEST_CODE_CONTACTS_ACTIVITY);
            startActivityForResult(mIntent, REQUEST_CODE_CONTACTS_ACTIVITY);
        }else if (view.getId() == R.id.btnContactGroup) {
/*
            LayoutInflater inflater = (LayoutInflater) AddPhoneNumberActivity.this
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View layout = inflater.inflate(R.layout.popup_contact_group,
                    (ViewGroup) findViewById(R.id.popup_layout));

            pwContact = new PopupWindow(layout, mWidthPixels-500, mHeightPixels-1000, true);
            pwContact.showAtLocation(layout, Gravity.CENTER, 0, 0);
            */
            Intent mIntent = new Intent(AddPhoneNumberActivity.this, AddContactGroupActivity.class);
            mIntent.putExtra(KEY_REQUEST_CODE, REQUEST_CODE_CONTACTS_GROUP_ACTIVITY);
            startActivityForResult(mIntent, REQUEST_CODE_CONTACTS_GROUP_ACTIVITY);
        }
    }

    private void addPhoneNumber(String phoneNumber, String name) {

        CustomerModel customer = new CustomerModel();
        customer.setPhone(phoneNumber);

        if (name.length() > 0) {
            customer.setCustomerName(name);
        }

        customer.isSelected(1);
        adapter.addItem(customer);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case REQUEST_CODE_CONTACTS_ACTIVITY: {

                if (resultCode != RESULT_OK) return;

                ArrayList<CustomerModel> contactList = data.getParcelableArrayListExtra(KEY_CUSTOMER_LIST);
                for(CustomerModel customer : contactList) {
                    if(customer.isSelected() == 1) adapter.addItem(customer);
                }

                adapter.notifyDataSetChanged();
                break;
            }

            case REQUEST_CODE_CONTACTS_GROUP_ACTIVITY: {

                if(resultCode == RESULT_OK) {
                    String TAG = "AddPhoneNumber";
                    Log.i(TAG, "REQUEST_CODE_CONTACTS_GROUP_ACTIVITY, RESULT_OK");
                    ArrayList<ContactGroupModel> contactGroupModels = data.getParcelableArrayListExtra(KEY_CUSTOMER_GROUP_LIST);
                    Log.i(TAG, "contactGroupList count is " + contactGroupModels.size());
                    for(ContactGroupModel contactGroup : contactGroupModels) {
                        if(contactGroup.isSelected() == 1) {
                            for (CustomerModel customer : contactGroup.getGroupMember()) {
                                adapter.addItem(customer);
                            }
                        }
                    }

                    adapter.notifyDataSetChanged();
                    break;
                }
            }
        }
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

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

    }


}
