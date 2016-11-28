package kr.co.bsmsoft.beple_shop.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import kr.co.bsmsoft.beple_shop.R;
import kr.co.bsmsoft.beple_shop.common.NetDefine;
import kr.co.bsmsoft.beple_shop.model.ContactGroupModel;
import kr.co.bsmsoft.beple_shop.model.CustomerModel;

/**
 * Created by Office on 2016-11-18.
 */

public class ContactListAdapter extends ArrayAdapter<CustomerModel> implements NetDefine {

    private final static String TAG = "ContactListAdapter";

    private Context context;
    private ArrayList<CustomerModel> items = new ArrayList<CustomerModel>();
    private LayoutInflater vi;
    private ListView mListView;

    public ContactListAdapter(Context context, ArrayList<CustomerModel> data, ListView listview) {
        super(context, R.layout.cell_customer_list);
        this.context = context;
        this.items = data;
        this.mListView = listview;
        this.vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    class ViewHolder {
        TextView txtPhone;
        TextView txtCustomerName;
        CheckBox chkSelected;
    }

    public void addAll(ArrayList<CustomerModel> groups) {
        items.addAll(groups);
        notifyDataSetChanged();
    }

    public ArrayList<CustomerModel> getItems() {

        return items;
    }

    public void addItem(CustomerModel item) {
        items.add(item);
    }

    public void removeItem(int position) {
        items.remove(position);
    }
    public CustomerModel getItem(int position) {

        return items.get(position);
    }

    public int getCount() {

        return items.size();
    }

    public void clear() {
        items.clear();
    }

    private View createView(int position) {

        ContactListAdapter.ViewHolder holder = null;
        View v = null;
        holder = new ContactListAdapter.ViewHolder();

        v = vi.inflate(R.layout.cell_customer_list, null);
        holder.txtPhone = (TextView) v.findViewById(R.id.txtPhone);
        holder.txtCustomerName = (TextView) v.findViewById(R.id.txtCustomerName);
        holder.chkSelected = (CheckBox) v.findViewById(R.id.checkBox);
        holder.chkSelected.setOnClickListener(mOnMenuClickListener);

        v.setTag(holder);
        return v;
    }

    private View.OnClickListener  mOnMenuClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            View parent = (View)v.getParent();
            int position = mListView.getPositionForView(parent);
            CustomerModel customer = items.get(position);

            CheckBox chk = (CheckBox)v;
            if (chk.isChecked()) {
                customer.isSelected(1);
            }else{
                customer.isSelected(0);
            }
        }
    };


    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {

        return true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ContactListAdapter.ViewHolder holder = null;
        View v = convertView;
        CustomerModel item = items.get(position);

        if(!item.isVisible()) return vi.inflate(R.layout.cell_null_item, null);

        v = createView(position);
        holder = (ContactListAdapter.ViewHolder) v.getTag();

        /*
        if (v == null) {
            v = createView(position);
            holder = (ContactListAdapter.ViewHolder) v.getTag();
        } else{
            holder = (ContactListAdapter.ViewHolder) v.getTag();
        }*/

        try {
            holder.txtPhone.setText(item.getPhone());
            holder.txtCustomerName.setText(String.valueOf(item.getCustomerName()));
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "item.getPhone : " + item.getPhone());
            Log.i(TAG, "item.getCustomerName : " + item.getCustomerName());
        }
        if (item.isSelected() == 1) {
            holder.chkSelected.setChecked(true);
        }else{
            holder.chkSelected.setChecked(false);
        }

        return v;

    }

}