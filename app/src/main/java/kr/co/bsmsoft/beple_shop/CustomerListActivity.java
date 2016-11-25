package kr.co.bsmsoft.beple_shop;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

import kr.co.bsmsoft.beple_shop.adapter.CustomerListAdapter;
import kr.co.bsmsoft.beple_shop.common.NetDefine;
import kr.co.bsmsoft.beple_shop.model.CustomerModel;

public class CustomerListActivity extends AppCompatActivity implements NetDefine, AdapterView.OnItemClickListener, View.OnClickListener {

    private Toolbar toolbar;
    private MainApp mainApp;
    private Button btnClose, btnOk;
    private CustomerListAdapter adapter;
    private ListView customerListView;

    private ArrayList<CustomerModel> customerList;

    private ArrayAdapter<String> stringAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_list);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent i = getIntent();
        customerList = i.getParcelableArrayListExtra(KEY_CUSTOMER_LIST);

        mainApp = globalVar.getInstance();

        customerListView = (ListView)findViewById(R.id.customerListView);
        customerListView.setOnItemClickListener(this);
        adapter = new CustomerListAdapter(this, customerList, customerListView);
        customerListView.setAdapter(adapter);

        btnOk = (Button)findViewById(R.id.btnOk);
        btnClose = (Button)findViewById(R.id.btnClose);

        btnOk.setOnClickListener(this);
        btnClose.setOnClickListener(this);

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
