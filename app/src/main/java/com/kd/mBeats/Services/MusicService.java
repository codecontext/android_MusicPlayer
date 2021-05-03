package com.kd.mBeats.Services;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.kd.mBeats.Interfaces.ActionPlaying;
import com.kd.mBeats.Models.MusicFiles;

import java.util.ArrayList;

import static com.kd.mBeats.Activities.PlayerActivity.listOfSongs;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener {
    IBinder binder = new MyBinder();
    MediaPlayer mediaPlayer;
    ArrayList<MusicFiles> musicFiles = new ArrayList<>();
    Uri uri;
    int position = -1;
    ActionPlaying actionPlaying;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class MyBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int myPosition = intent.getIntExtra("servicePosition", -1);
        String actionName = intent.getStringExtra("ActionName");

        if(myPosition != -1) {
            playMedia(myPosition);
        }

        if(actionName != null){
            switch (actionName){
                case "playPause":
                    if(actionPlaying != null){
                        actionPlaying.playPauseButtonClicked();
                    }
                    break;

                case "next":
                    if(actionPlaying != null){
                        actionPlaying.nextButtonClicked();
                    }
                    break;

                case "previous":
                    if(actionPlaying != null){
                        actionPlaying.prevButtonClicked();
                    }
                    break;
            }
        }

        return START_STICKY;
    }

    private void playMedia(int startPosition) {
        musicFiles = listOfSongs;
        position = startPosition;

        if(mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();

            if(musicFiles != null){
                createMediaPlayer(position);
                mediaPlayer.start();
            }
        } else {
            createMediaPlayer(position);
            mediaPlayer.start();
        }
    }

    public void start() {
        mediaPlayer.start();
    }

    public void pause() {
        mediaPlayer.pause();
    }

    public boolean isPlaying() {
       return mediaPlayer.isPlaying();
    }

    public void stop() {
        mediaPlayer.stop();
    }

    public void release() {
        mediaPlayer.release();
    }

    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    public void seekTo(int position) {
        mediaPlayer.seekTo(position);
    }

    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    public void createMediaPlayer(int songPosition) {
        position = songPosition;
        uri = Uri.parse(musicFiles.get(position).getPath());
        mediaPlayer = MediaPlayer.create(getBaseContext(), uri);
    }

    public void OnCompleted() {
        mediaPlayer.setOnCompletionListener(this);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(actionPlaying != null) {
            actionPlaying.nextButtonClicked();

            if(mediaPlayer != null){
                createMediaPlayer(position);
                mediaPlayer.start();
                OnCompleted();
            }
        }
    }

    public void setCallback(ActionPlaying actionPlaying){
        this.actionPlaying = actionPlaying;
    }
}
