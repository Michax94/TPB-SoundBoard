package pl.skipcode.tpb_soundboard;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

/**
 * Created by Michał Skiba on 15.05.2017.
 */

public class CharacterObject implements Serializable {

    private String name;
    private SoundObject[] soundObjects;

    public CharacterObject() {
    }

    public CharacterObject(String name) {
        this.name = name;
    }

    public CharacterObject(JSONObject jsonObject) throws JSONException {
        name = jsonObject.getString("name");
        JSONArray soundObjects = jsonObject.optJSONArray("sounds");
        if(soundObjects != null){
            this.soundObjects = SoundObject.getSoundObjectsFromJsonArray(jsonObject.optJSONArray("sounds"), name);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SoundObject[] getSoundObjects() {
        return soundObjects;
    }

    public void setSoundObjects(SoundObject[] soundObjects) {
        this.soundObjects = soundObjects;
    }

    public boolean hasSounds() {
        return  soundObjects != null && soundObjects.length > 0;
    }

    @NonNull
    private static CharacterObject[] getCharacterObjectFromJsonArray(JSONArray jsonArray) throws JSONException {
        CharacterObject[] characterObjects = new CharacterObject[jsonArray.length()];

        for(int i = 0; i<jsonArray.length(); i++){
            CharacterObject characterObject = new CharacterObject(jsonArray.getJSONObject(i));
            characterObjects[i] = characterObject;
        }
        return characterObjects;
    }

    public static CharacterObject[] loadArrayFromJson(Context context, String type){
        try {
            String json = loadStringFromAssets(context, "tpb.json");
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject.getJSONArray(type);

            return getCharacterObjectFromJsonArray(jsonArray);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new CharacterObject[0];
    }

    public static String loadStringFromAssets(Context context, String fileName) throws IOException {
        InputStream inputStream = context.getAssets().open(fileName);
        int size = inputStream.available();
        byte[] buffer = new byte[size];

        inputStream.read(buffer);
        inputStream.close();

        return new String(buffer, "UTF-8");
    }
}
