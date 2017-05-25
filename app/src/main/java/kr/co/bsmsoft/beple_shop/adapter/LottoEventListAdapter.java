/**
 * 사용자 목록 리스트뷰 adapter
 */
package kr.co.bsmsoft.beple_shop.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


import java.util.ArrayList;

import kr.co.bsmsoft.beple_shop.R;
import kr.co.bsmsoft.beple_shop.common.Helper;
import kr.co.bsmsoft.beple_shop.common.NetDefine;
import kr.co.bsmsoft.beple_shop.model.EventModel;

public class LottoEventListAdapter extends ArrayAdapter<EventModel> implements NetDefine {

    private final static String TAG = "SmsEventListAdapter";

    private Context context;
    private ArrayList<EventModel> items = new ArrayList<EventModel>();
    private LayoutInflater vi;

    public LottoEventListAdapter(Context context, ArrayList<EventModel> data) {
        super(context, R.layout.cell_lotto_list);
        this.context = context;
        this.items = data;
        this.vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    class ViewHolder {
        TextView txtTimes;
        TextView txtEventName;
        TextView txtRegDt;
        TextView txtTarget;
        TextView txtStatus;
    }

    public void addAll(ArrayList<EventModel> users) {
        items.addAll(users);
        notifyDataSetChanged();
    }

    public ArrayList<EventModel> getItems() {

        return items;
    }

    public void removeItem(int position) {
        items.remove(position);
    }
    public EventModel getItem(int position) {

        return items.get(position);
    }

    public int getCount() {

        return items.size();
    }

    public void clear() {
        items.clear();
    }

    private View createView(int position) {

        ViewHolder holder = null;
        View v = null;
        holder = new ViewHolder();

        v = vi.inflate(R.layout.cell_lotto_list, null);
        holder.txtTimes = (TextView) v.findViewById(R.id.txtTimes);
        holder.txtEventName = (TextView) v.findViewById(R.id.txtEventName);
        holder.txtRegDt = (TextView) v.findViewById(R.id.txtRegDt);
        holder.txtTarget = (TextView) v.findViewById(R.id.txtTarget);
        holder.txtStatus = (TextView) v.findViewById(R.id.txtStatus);

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
        EventModel item = items.get(position);

        if (v == null) {

            v = createView(position);
            holder = (ViewHolder) v.getTag();

        } else{

            holder = (ViewHolder) v.getTag();
        }

        holder.txtTimes.setText(String.format("%d 회차", item.getTimes()));
        holder.txtEventName.setText(item.getEventNm());
        holder.txtRegDt.setText(Helper.formatTimeString(item.getRegDt()));
        holder.txtTarget.setText(String.format("대상 : %d명", item.getcCount()));
        holder.txtStatus.setText(item.getStatusName());

        if (item.getStatus() == EventModel.STATUS_READY) {
            holder.txtStatus.setBackgroundResource(R.drawable.tag_2);
        }else{
            holder.txtStatus.setBackgroundResource(R.drawable.tag_3);
        }
        return v;

    }


}
