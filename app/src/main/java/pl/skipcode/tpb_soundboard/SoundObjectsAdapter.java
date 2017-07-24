package pl.skipcode.tpb_soundboard;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

import static pl.skipcode.tpb_soundboard.R.id.itemCardView;

/**
 * Created by Michał Skiba on 15.05.2017.
 */

public class SoundObjectsAdapter extends RecyclerView.Adapter<SoundObjectsAdapter.SoundObjectViewHolder> {

    private final SoundObject[] soundObjects;

    private SoundObjectClickedListener soundObjectClickedListener;

    public SoundObjectsAdapter(SoundObject[] soundObjects) {
        this.soundObjects = soundObjects;
    }

    public void setSoundObjectClickedListener(SoundObjectClickedListener soundObjectClickedListener) {
        this.soundObjectClickedListener = soundObjectClickedListener;
    }

    @Override
    public SoundObjectViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sound_object, parent, false);
        return new SoundObjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SoundObjectViewHolder holder, int position) {
        SoundObject soundObject = soundObjects[position];
        holder.setSoundObject(soundObject);
    }

    @Override
    public int getItemCount() {
        return soundObjects.length;
    }

    private void itemClicked(SoundObject soundObject) {
        if (soundObjectClickedListener != null){
            soundObjectClickedListener.soundObjectClicked(soundObject);
        }
    }

    private void itemButtonClicked(SoundObject soundObject) {
        if (soundObjectClickedListener != null){
            soundObjectClickedListener.soundObjectButtonClicked(soundObject);
        }
    }

    class SoundObjectViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.itemButtonPlay)
        ImageButton itemButtonPlay;
        @BindView(R.id.itemTextView)
        TextView itemTextView;
        @BindView(R.id.itemCardView)
        CardView itemCardView;

        private View itemView;

        private SoundObject soundObject;

        public SoundObjectViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
            itemButtonPlay.setOnClickListener(this);

            this.itemView = itemView;
        }

        public void setSoundObject(SoundObject soundObject) {
            this.soundObject = soundObject;
            itemTextView.setText(soundObject.getTitle());

            if(soundObject.isPlaying()){
                itemButtonPlay.setImageResource(R.drawable.ic_pause_circle);
            }else{
                itemButtonPlay.setImageResource(R.drawable.ic_play_circle);
            }
        }

        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.itemButtonPlay){
                itemButtonClicked(soundObject);
            }else{
                itemClicked(soundObject);
            }
        }
    }

    public interface SoundObjectClickedListener{
        void soundObjectClicked(SoundObject soundObject);
        void soundObjectButtonClicked(SoundObject soundObject);
    }
}
