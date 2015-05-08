package gg.destiny.app;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Hayk on 5/5/2015.
 */
public class MenuListAdapter extends WearableListView.Adapter {

    private final static String TAG = "MenuListAdapter";

    public static final String[] menus = new String[]{
            "Default Face",
            "Offline Face",
            "Done"
    };

    public static final String[] background_categories = new String[]{
            "default",
            "offline"
    };

    private final int[] icons = new int[]{
            R.drawable.le_ruse,
            R.drawable.da_feels,
            R.drawable.short_arm
    };

    private final Context mContext;
    private final LayoutInflater mInflater;

    public MenuListAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder");
        return new ItemViewHolder(mInflater.inflate(R.layout.list_item, null));
    }

    @Override
    public void onBindViewHolder(WearableListView.ViewHolder holder, int position) {
        ItemViewHolder itemViewHolder = (ItemViewHolder) holder;

        TextView view = itemViewHolder.mTextView;
        view.setText(menus[position]);
        // TODO: pull the current default and offline images to use as icons in the menu


        itemViewHolder.mImageView.setImageResource(icons[position]);

//        String sTag = this.listName+"|"+String.valueOf(imageId);
        itemViewHolder.mImageView.setTag(menus[position]);

        RecyclerView.LayoutParams layoutParams =
                new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);

        int colorPickerItemMargin = 22;// (int) getResources().getDimension(R.dimen.digital_config_color_picker_item_margin);

        // Add margins to first and last item to make it possible for user to tap on them.
        if (position == 0) {
            layoutParams.setMargins(0, colorPickerItemMargin, 0, 0);
        } else if (position == menus.length - 1) {
            layoutParams.setMargins(0, 0, 0, colorPickerItemMargin);
        } else {
            layoutParams.setMargins(0, 0, 0, 0);
        }

        itemViewHolder.itemView.setLayoutParams(layoutParams);
    }

    @Override
    public int getItemCount() {
        return menus.length;
    }
}
