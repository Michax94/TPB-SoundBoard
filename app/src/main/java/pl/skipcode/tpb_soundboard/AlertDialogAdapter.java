package pl.skipcode.tpb_soundboard;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by Michał Skiba on 18.05.2017.
 */

public class AlertDialogAdapter extends ArrayAdapter<String> {

    private final String[] options;
    private ViewHolder holder;
    private Context context;

    public AlertDialogAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull String[] objects) {
        super(context, resource, objects);
        this.options = objects;
        this.context = context;
    }

    class ViewHolder {
        ImageView itemIcon;
        TextView itemTitle;
        View itemMessangerButton;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        final LayoutInflater inflater = (LayoutInflater) getApplicationContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.alert_dialog_list_row, null);

            holder = new ViewHolder();
            holder.itemIcon = (ImageView) convertView.findViewById(R.id.itemIcon);
            holder.itemTitle = (TextView) convertView.findViewById(R.id.itemTitle);
            holder.itemMessangerButton = (View) convertView.findViewById(R.id.itemMessangerButton);
            convertView.setTag(holder);
        } else {
            // view already defined, retrieve view holder
            holder = (ViewHolder) convertView.getTag();
        }

        holder.itemMessangerButton.setClickable(false);

        if(options[position].equals(context.getString(R.string.send_by_messanger))){
            holder.itemMessangerButton.setVisibility(View.VISIBLE);
            holder.itemTitle.setVisibility(View.GONE);
            holder.itemIcon.setVisibility(View.GONE);
        }else{
            Drawable icon = null;
            if(options[position].equals(context.getString(R.string.set_sound_ringtone))){
                icon = context.getResources().getDrawable(R.drawable.ic_ring_tone);
            }else if(options[position].equals(context.getString(R.string.set_sound_notification))){
                icon = context.getResources().getDrawable(R.drawable.ic_notifications);
            }else{
                icon = context.getResources().getDrawable(R.drawable.ic_alarm);
            }
            holder.itemMessangerButton.setVisibility(View.GONE);
            holder.itemTitle.setVisibility(View.VISIBLE);
            holder.itemIcon.setVisibility(View.VISIBLE);
            holder.itemTitle.setText(options[position]);
            holder.itemIcon.setImageDrawable(icon);
        }

        return convertView;
    }

}
