/**
 * 사용자 목록 리스트뷰 adapter
 */
package kr.co.bsmsoft.beple_shop.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import kr.co.bsmsoft.beple_shop.R;
import kr.co.bsmsoft.beple_shop.common.NetDefine;
import kr.co.bsmsoft.beple_shop.model.CustomerModel;

public class PhoneListAdapter extends ArrayAdapter<CustomerModel> implements NetDefine {

    private final static String TAG = "PhoneListAdapter";

    private Context context;
    private ArrayList<CustomerModel> items = new ArrayList<CustomerModel>();
    private LayoutInflater vi;
    private ListView mListView;

    public PhoneListAdapter(Context context, ArrayList<CustomerModel> data) {
        super(context, R.layout.cell_phone_list);
        this.context = context;
        this.items = data;
        this.vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    class ViewHolder {
        TextView txtPhone;
        Button btnDelete;
    }

    public void addItem(CustomerModel item) {

        boolean bFind = false;

        for (CustomerModel phone : items) {
            if (phone.getPhone().equals(item.getPhone())){
                bFind = true;
                break;
            }
        }
        if (!bFind) {
            items.add(item);
        }
    }

    public ArrayList<CustomerModel> getItems() {

    	return items;
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

    private View createView() {

        ViewHolder holder = null;
        View v = null;
        holder = new ViewHolder();

        v = vi.inflate(R.layout.cell_phone_list, null);
        holder.txtPhone = (TextView) v.findViewById(R.id.txtPhone);
        holder.btnDelete = (Button) v.findViewById(R.id.btnDelete);
        holder.btnDelete.setOnClickListener(mOnDeleteClickListener);

        v.setTag(holder);
        return v;
    }

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

        ViewHolder holder = null;
        View v = convertView;
        CustomerModel item = items.get(position);

        if (v == null) {

            v = createView();
            holder = (ViewHolder) v.getTag();

        } else{

            holder = (ViewHolder) v.getTag();
        }

        holder.txtPhone.setText(item.getPhone());

        return v;

    }

    private View.OnClickListener mOnDeleteClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            final int position = mListView.getPositionForView((View) v.getParent());
            items.remove(position);
            notifyDataSetChanged();
        }
    };

    public ListView getmListView() {
        return mListView;
    }

    public void setListView(ListView mListView) {
        this.mListView = mListView;
    }

}
