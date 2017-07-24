package pl.skipcode.tpb_soundboard;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.facebook.messenger.MessengerUtils;
import com.facebook.messenger.ShareToMessengerParams;
import com.google.firebase.analytics.FirebaseAnalytics;

import static android.R.attr.path;
import static android.R.attr.permission;
import static android.R.attr.type;
import static android.R.attr.width;
import static pl.skipcode.tpb_soundboard.SoundObjectsFragment.DEBUG_KEY;

public class TpbSoundBoardActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SoundObjectsAdapter.SoundObjectClickedListener {

    public static final Integer REQUEST_CODE_SHARE_TO_MESSENGER = 1001;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.containerLayout)
    FrameLayout containerLayout;
    @BindView(R.id.nav_view)
    NavigationView navView;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    private FirebaseAnalytics mFirebaseAnalytics;

    private SoundObject[] soundObjects;
    private CharacterObject[] characters;
    private MediaPlayer mediaPlayer;
    private SoundObjectsFragment fragmentOnline;
    private Boolean permissionAlertOpen = false;

    private String[] permissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /*PackageInfo info;*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tpb_sound_board);
        ButterKnife.bind(this);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        toolbar.setTitle(getString(R.string.app_name));

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        navView.setNavigationItemSelectedListener(this);

        checkPermissions();

        characters = CharacterObject.loadArrayFromJson(this, "characters");
        soundObjects = SoundObject.getSoundObjectsFromCharacterObjects(characters);

        navView.setCheckedItem(R.id.nav_all);
        onNavigationItemSelected(navView.getMenu().findItem(R.id.nav_all));
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkPermissions();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id){

            case R.id.nav_all:
                fragmentOnline = SoundObjectsFragment.newInstance(soundObjects);
                replaceFragment(fragmentOnline);
                toolbar.setTitle(item.getTitle());
                break;
            default:
                SoundObject[] sounds = SoundObject.getSoundObjectsFromCharactersByName(characters, item);
                fragmentOnline = SoundObjectsFragment.newInstance(sounds);
                replaceFragment(fragmentOnline);
                toolbar.setTitle(item.getTitle());
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private boolean checkPermissions() {
        int result;

        if(permissionAlertOpen){
            return false;
        }

        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(this, p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 100);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if(permissions.length == 0){
            return;
        }
        boolean allPermissionsGranted = true;
        if(grantResults.length>0){
            for(int grantResult: grantResults){
                if(grantResult != PackageManager.PERMISSION_GRANTED){
                    allPermissionsGranted = false;
                    break;
                }
            }
        }
        if(!allPermissionsGranted){
            boolean somePermissionsForeverDenied = false;
            for(String permission: permissions){
                if(ActivityCompat.shouldShowRequestPermissionRationale(this, permission)){
                    //denied
                    checkPermissions();
                }else{
                    if(ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                        //set to never ask again
                        Log.e("set to never ask again", permission);
                        somePermissionsForeverDenied = true;
                    }
                }
            }
            if(somePermissionsForeverDenied){
                permissionAlertOpen = true;
                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this, R.style.MyDialogTheme);
                alertDialogBuilder.setTitle(getString(R.string.info_title_permission_required))
                        .setMessage(getString(R.string.info_message_permission_required))
                        .setPositiveButton(getString(R.string.action_settings), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.fromParts("package", getPackageName(), null));
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                permissionAlertOpen = false;
                            }
                        })
                        .setCancelable(false)
                        .create()
                        .show();
            }
        } else {
            permissionAlertOpen = false;
            switch (requestCode) {
                //act according to the request code used while requesting the permission(s).
            }
        }
    }

    @Override
    public void soundObjectClicked(SoundObject soundObject) {
        openSoundObjectMenu(soundObject);
    }

    @Override
    public void soundObjectButtonClicked(SoundObject soundObject) {
        playSound(soundObject);
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.containerLayout, fragment);
        fragmentTransaction.commit();
    }

    public void playSound(final SoundObject soundObject) {

        if(mediaPlayer == null){
            mediaPlayer = new MediaPlayer();
        }
        try {

            if (soundObject.isPlaying()){
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = new MediaPlayer();
                fragmentOnline.resetStatusObjects();
            }else{
                if (mediaPlayer.isPlaying()) {
                    fragmentOnline.resetStatusObjects();
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = new MediaPlayer();
                }else{
                    mediaPlayer.reset();
                }

                int fileId = this.getResources().getIdentifier(soundObject.getMp3(), "raw", this.getPackageName());
                AssetFileDescriptor descriptor = getResources().openRawResourceFd(fileId);
                mediaPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
                mediaPlayer.prepare();
                mediaPlayer.start();

                soundObject.setPlaying(true);
                fragmentOnline.refreshAdapter();

                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        fragmentOnline.resetStatusObjects();
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void openSoundObjectMenu(final SoundObject soundObject){
        final String[] options = new String[] { getString(R.string.set_sound_ringtone), getString(R.string.set_sound_notification), getString(R.string.set_sound_alarm), getString(R.string.send_by_messanger) };

        AlertDialogAdapter adapter = new AlertDialogAdapter(getApplicationContext(), R.layout.alert_dialog_list_row, options);

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyDialogTheme);
        builder.setTitle('"' + soundObject.getTitle() + '"');
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                String[] types = new String[]{"RINGTONE", "NOTIFICATION", "ALARM"};

                if(which < 3){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (!Settings.System.canWrite(TpbSoundBoardActivity.this)) {
                            Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                            intent.setData(Uri.parse("package:" + TpbSoundBoardActivity.this.getPackageName()));
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }else{
                            if(!setSoundObjectAsSystemSounds(soundObject, types[which])){
                                Snackbar.make(containerLayout, getString(R.string.info_message_error), Snackbar.LENGTH_LONG)
                                        .setAction(getString(R.string.action_again), new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                openSoundObjectMenu(soundObject);
                                            }
                                        })
                                        .show();
                            }
                        }
                    }
                }else{
                    shareSoundObjectByMessanger(soundObject);
                }
            }
        });
        final AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void shareSoundObjectByMessanger(SoundObject soundObject){
        String mimeType = "audio/mpeg";

        Uri contentUri = Uri.parse("android.resource://"+getPackageName()+"/raw/"+soundObject.getMp3());

        ShareToMessengerParams shareToMessengerParams =
                ShareToMessengerParams.newBuilder(contentUri, mimeType)
                        .build();
        // Sharing from an Activity
        MessengerUtils.shareToMessenger(
                this,
                REQUEST_CODE_SHARE_TO_MESSENGER,
                shareToMessengerParams);

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "1");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Share by messanger");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "image");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }
    public Uri saveSoundObjectOnSdCard(SoundObject soundObject, String type){

        byte[] buffer = null;
        InputStream fIn = null;
        int size = 50;
        String path = "/sdcard/sounds/" + TpbSoundBoardActivity.this.getApplicationInfo().name + "/";
        String filename = soundObject.getTitle() + ".mp3";
        FileOutputStream save;

        try {
            fIn = getResources().openRawResource(
                    getResources().getIdentifier(soundObject.getMp3(),
                            "raw", getPackageName()));
            size = fIn.available();
            buffer = new byte[size];
            fIn.read(buffer);
            fIn.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        boolean exists = (new File(path)).exists();
        if (!exists) {
            new File(path).mkdirs();
        }

        try {
            save = new FileOutputStream(path + filename);
            save.write(buffer);
            save.flush();
            save.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
        } catch (IOException e) {
            // TODO Auto-generated catch block
        }

        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + path + filename)));
        File k = new File(path, filename);

        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DATA, k.getAbsolutePath());
        values.put(MediaStore.MediaColumns.TITLE, soundObject.getTitle());
        values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3");
        values.put(MediaStore.Audio.Media.ARTIST, soundObject.getName());
        switch (type){
            case "RINGTONE":
                values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
                break;
            case "NOTIFICATION":
                values.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);
                break;
            case "ALARM":
                values.put(MediaStore.Audio.Media.IS_ALARM, true);
                break;
            case "MUSIC":
                values.put(MediaStore.Audio.Media.IS_MUSIC, true);
                break;
        }

        Uri uri = MediaStore.Audio.Media.getContentUriForPath(k.getAbsolutePath());
        getContentResolver().delete(uri, MediaStore.MediaColumns.DATA + "=\"" + k.getAbsolutePath() + "\"", null);
        Uri newUri = getContentResolver().insert(uri, values);

        return newUri;
    }

    public boolean setSoundObjectAsSystemSounds(SoundObject soundObject, String type) {

        Uri newUri = saveSoundObjectOnSdCard(soundObject, type);

        switch (type){
            case "RINGTONE":
                RingtoneManager.setActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE, newUri);
                Snackbar.make(containerLayout, getString(R.string.info_message_ringtone), Snackbar.LENGTH_LONG).show();
                break;
            case "NOTIFICATION":
                RingtoneManager.setActualDefaultRingtoneUri(this, RingtoneManager.TYPE_NOTIFICATION, newUri);
                Snackbar.make(containerLayout, getString(R.string.info_message_notification), Snackbar.LENGTH_LONG).show();
                break;
            case "ALARM":
                RingtoneManager.setActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM, newUri);
                Snackbar.make(containerLayout, getString(R.string.info_message_alarm), Snackbar.LENGTH_LONG).show();
                break;
        }

        return true;
    }
}
