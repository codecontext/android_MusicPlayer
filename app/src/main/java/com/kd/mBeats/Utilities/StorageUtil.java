package com.kd.mBeats.Utilities;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kd.mBeats.Models.MusicFiles;

import java.lang.reflect.Type;
import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;
import static com.kd.mBeats.Activities.PlayerActivity.POSITION_INVALID;

public class StorageUtil {

    private final String STORAGE = "com.kd.mBeats.STORED_MUSIC_DATA";
    private SharedPreferences preferences;
    private Context context;

    public StorageUtil(Context context) {
        this.context = context;
    }

    public void storeAudioList(ArrayList<MusicFiles> list) {
        preferences = context.getSharedPreferences(STORAGE, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        Gson gson = new Gson();
        String json = gson.toJson(list);

        editor.putString("audioArrayList", json);
        editor.apply();
    }

    public ArrayList<MusicFiles> loadAudioList() {
        ArrayList<MusicFiles> savedList = new ArrayList<>();
        preferences = context.getSharedPreferences(STORAGE, MODE_PRIVATE);
        Gson gson = new Gson();

        String json = preferences.getString("audioArrayList", null);

        /* Get the type of array list */
        Type type = new TypeToken<ArrayList<MusicFiles>>() {}.getType();

        /* Receive data from gson and save it to array list */
        savedList = gson.fromJson(json, type);

        if (savedList == null) {
            savedList = new ArrayList<>();
        }

        return savedList;
    }

    public void storeAudioIndex(int index) {
        preferences = context.getSharedPreferences(STORAGE, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putInt("audioIndex", index);
        editor.apply();
    }

    public int loadAudioIndex() {
        preferences = context.getSharedPreferences(STORAGE, MODE_PRIVATE);
        return preferences.getInt("audioIndex", POSITION_INVALID);
    }
}
