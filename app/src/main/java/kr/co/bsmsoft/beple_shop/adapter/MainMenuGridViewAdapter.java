package kr.co.bsmsoft.beple_shop.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import kr.co.bsmsoft.beple_shop.R;
import kr.co.bsmsoft.beple_shop.common.NetDefine;
import kr.co.bsmsoft.beple_shop.model.MenuModel;

public class MainMenuGridViewAdapter extends ArrayAdapter<MenuModel> implements NetDefine {

    private final static String TAG = "MainMenuGridViewAdapter";

    private Context context;
    private ArrayList<MenuModel> items = new ArrayList<MenuModel>();
    private LayoutInflater vi;

    public MainMenuGridViewAdapter(Context context, ArrayList<MenuModel> data) {
        super(context, R.layout.cell_main_menu, data);
        this.context = context;
        this.items = data;
        this.vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    class ViewHolder {
        ImageView imgPhoto;
        TextView txtTitle;
    }

    public void add(MenuModel item) {

    	items.add(item);
    }
    
    public ArrayList<MenuModel> getItems() {
    	
    	return items;
    }
    
    public MenuModel getItem(int position) {
    	
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

        v = vi.inflate(R.layout.cell_main_menu, null);
        holder.imgPhoto = (ImageView) v.findViewById(R.id.imgPhoto);
        holder.txtTitle = (TextView) v.findViewById(R.id.txtTitle);

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
        MenuModel item = items.get(position);

        if (v == null) {

            v = createView(position);
            holder = (ViewHolder) v.getTag();

        } else{

            holder = (ViewHolder) v.getTag();
        }

        holder.imgPhoto.setImageResource(item.getImageRes());
        holder.txtTitle.setText(item.getTitle());

        return v;

    }

}
