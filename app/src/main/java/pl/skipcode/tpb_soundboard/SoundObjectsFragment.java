package pl.skipcode.tpb_soundboard;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.facebook.FacebookSdk.getApplicationContext;
import static pl.skipcode.tpb_soundboard.R.id.adView;

/**
 * A simple {@link Fragment} subclass.
 */
public class SoundObjectsFragment extends Fragment {

    private static final String OBJECTS_KEY = "objects";
    public static final String DEBUG_KEY = SoundObjectsFragment.class.getSimpleName().toString();

    @BindView(R.id.objectsRecycleView)
    RecyclerView objectsRecycleView;
    @BindView(R.id.adView)
    AdView mAdView;

    Unbinder unbinder;

    private SoundObjectsAdapter.SoundObjectClickedListener soundObjectClickedListener;

    private SoundObjectsAdapter adapterSoundObjects;
    private SoundObject[] soundObjects;

    public SoundObjectsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        soundObjectClickedListener = (SoundObjectsAdapter.SoundObjectClickedListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        soundObjectClickedListener = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sound_objects, container, false);
        unbinder = ButterKnife.bind(this, view);

        AdRequest request = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)        // All emulators
                .addTestDevice("624C561799C22DB83D34FFB4E0103550")  // Emulator id you will get in the LogCat verbose
                .build();
        mAdView.loadAd(request);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        soundObjects = (SoundObject[]) getArguments().getSerializable(OBJECTS_KEY);

        objectsRecycleView.setLayoutManager(new GridLayoutManager(getActivity(), 1));
        adapterSoundObjects = new SoundObjectsAdapter(soundObjects);
        adapterSoundObjects.setSoundObjectClickedListener(soundObjectClickedListener);
        objectsRecycleView.setAdapter(adapterSoundObjects);
    }

    public static SoundObjectsFragment newInstance(SoundObject[] objects) {

        Bundle args = new Bundle();
        args.putSerializable(OBJECTS_KEY, objects);

        SoundObjectsFragment fragment = new SoundObjectsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    public void resetStatusObjects(){
        for (SoundObject soundObject : soundObjects) {
            soundObject.setPlaying(false);
        }
        refreshAdapter();

    }

    public void refreshAdapter(){
        adapterSoundObjects.notifyDataSetChanged();
    }
}
