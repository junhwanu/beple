package kr.co.bsmsoft.beple_shop.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;

import kr.co.bsmsoft.beple_shop.R;
import kr.co.bsmsoft.beple_shop.common.NetDefine;
import kr.co.bsmsoft.beple_shop.model.ImageModel;

public class PhotoGridViewAdapter extends ArrayAdapter<ImageModel> implements NetDefine {

    private final static String TAG = "PhotoGridViewAdapter";

    private Context context;
    private ArrayList<ImageModel> items = new ArrayList<ImageModel>();
    private Boolean noImage = true;
    private LayoutInflater vi;

    public PhotoGridViewAdapter(Context context, ArrayList<ImageModel> data) {
        super(context, R.layout.cell_grid_photo, data);
        this.context = context;
        this.items = data;
        this.vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    class ViewHolder {
        ImageView imgPhoto;
        ImageView imgRemove;
    }

    public void add(ImageModel item) {

    	items.add(item);
    }
    
    public ArrayList<ImageModel> getItems() {
    	
    	return items;
    }
    
    public ImageModel getItem(int position) {
    	
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

        v = vi.inflate(R.layout.cell_grid_photo, null);
        holder.imgPhoto = (ImageView) v.findViewById(R.id.imgPhoto);
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
        ImageModel item = items.get(position);

        if (v == null) {

            v = createView(position);
            holder = (ViewHolder) v.getTag();

        } else{
            holder = (ViewHolder) v.getTag();
        }

        if (item.getFileUrl() != null) {
            Glide
                    .with(context)
                    .load(item.getServerAddress() + "/" + item.getFileUrl())
                    .centerCrop()
                    .placeholder(R.drawable.bg_default_photo)
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(holder.imgPhoto);
        }else {
            holder.imgPhoto.setImageBitmap(null);
        }

        return v;

    }

    public Boolean getNoImage() {
        return noImage;
    }

    public void setNoImage(Boolean noImage) {
        this.noImage = noImage;
    }
}
