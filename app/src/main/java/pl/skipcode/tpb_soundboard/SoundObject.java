package pl.skipcode.tpb_soundboard;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MenuItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;

import static android.R.attr.id;

/**
 * Created by Michał Skiba on 15.05.2017.
 */

public class SoundObject implements Serializable {

    private String mp3, name, title;
    private Boolean isPlaying;

    public SoundObject() {
    }

    public SoundObject(JSONObject jsonObject) throws JSONException {
        mp3 = jsonObject.getString("mp3");
        title = jsonObject.getString("title");
        name = jsonObject.optString("name");
        isPlaying = false;
    }

    public Boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(Boolean playing) {
        isPlaying = playing;
    }

    public String getMp3() {
        return mp3;
    }

    public void setMp3(String mp3) {
        this.mp3 = mp3;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public static SoundObject[] getSoundObjectsFromCharactersByName(CharacterObject[] characters, MenuItem item) {

        Integer i = 0;
        for (CharacterObject character : characters) {
            String name = item.getTitle().toString().toLowerCase().replaceAll("-","");
            if(character.getName().toLowerCase().equals(name)){
                return characters[i].getSoundObjects();
            }
            i++;
        }

        return new SoundObject[0];
    }

    @NonNull
    public static SoundObject[] getSoundObjectsFromJsonArray(JSONArray jsonArray, String name) throws JSONException {
        SoundObject[] soundObjects = new SoundObject[jsonArray.length()];

        for(int i = 0; i<jsonArray.length(); i++){
            SoundObject soundObject = new SoundObject(jsonArray.getJSONObject(i));
            soundObject.setName(name);
            soundObjects[i] = soundObject;
        }
        return soundObjects;
    }

    public static SoundObject[] getSoundObjectsFromCharacterObjects(CharacterObject[] characterObjects) {

        ArrayList<SoundObject> arrayList = new ArrayList<>();
        for (CharacterObject characterObject : characterObjects){
            if (characterObject.hasSounds()){

                SoundObject[] soundObjects = characterObject.getSoundObjects();
                for (SoundObject soundObject : soundObjects) {
                    soundObject.setName(characterObject.getName());
                    arrayList.add(soundObject);
                }
            }
        }

        SoundObject[] soundObjects = new SoundObject[arrayList.size()];

        return  arrayList.toArray(soundObjects);
    }
}
