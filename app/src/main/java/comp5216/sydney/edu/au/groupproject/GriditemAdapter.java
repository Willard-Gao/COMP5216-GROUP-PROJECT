package comp5216.sydney.edu.au.groupproject;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.io.File;
import java.util.List;

public class GriditemAdapter extends BaseAdapter {
    Context context;
    List<File> list;

    public GriditemAdapter(Context context, List<File> list) {
        this.context = context;
        this.list = list;
    }



    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ImageView imageView;
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        view = layoutInflater.inflate(R.layout.grid_list_item, null);
        imageView = view.findViewById(R.id.photoImg);
        imageView.setImageURI(Uri.parse(getItem(i).toString()));
        imageView.setLayoutParams(new GridView.LayoutParams(500, 500));
        imageView.setPadding(8, 8, 8, 8);
        return imageView;
    }
}
