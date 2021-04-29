package com.kd.mBeats.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.kd.mBeats.Models.MusicFiles;
import com.kd.mBeats.R;

import java.util.ArrayList;

import static com.kd.mBeats.Activities.MainActivity.LOG_TAG;
import static com.kd.mBeats.Activities.MainActivity.musicFiles;

public class PlayerActivity extends AppCompatActivity {

    TextView songTitle;
    TextView artistName;
    TextView durationPlayed;
    TextView durationTotal;

    ImageView coverArt;

    ImageView nextButton;
    ImageView prevButton;
    ImageView shuffleButton;
    ImageView repeatButton;
    ImageView backButton;

    FloatingActionButton playPauseButton;
    SeekBar seekBar;

    int position = -1;
    static ArrayList<MusicFiles> listOfSongs = new ArrayList<>();
    static Uri uri;
    static MediaPlayer mediaPlayer;

    private Handler handler = new Handler();
    private Thread playThread, prevThread, nextThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        initViews();
        getPlayIntent();

        songTitle.setText(listOfSongs.get(position).getTitle());
        artistName.setText(listOfSongs.get(position).getArtist());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if((mediaPlayer!= null) && (fromUser)){
                    mediaPlayer.seekTo(progress * 1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mediaPlayer != null){
                    int mCurrentPosition = mediaPlayer.getCurrentPosition() / 1000;

                    seekBar.setProgress(mCurrentPosition);
                    durationPlayed.setText(milliSecondsToTimer(mCurrentPosition));
                }
                handler.postDelayed(this, 1000);
            }
        });
    }

    @Override
    protected void onResume() {
        playThreadButton();
        prevThreadButton();
        nextThreadButton();

        super.onResume();
    }

    private void playThreadButton() {
        playThread = new Thread(){
            @Override
            public void run() {
                super.run();
                playPauseButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playPauseButtonClicked();
                    }
                });
            }
        };
        playThread.start();
    }

    private void playPauseButtonClicked() {
        if(mediaPlayer.isPlaying()){
            mediaPlayer.pause();
            playPauseButton.setImageResource(R.drawable.ic_play);
        } else {
            mediaPlayer.start();
            playPauseButton.setImageResource(R.drawable.ic_pause);
        }

        seekBar.setMax(mediaPlayer.getDuration() / 1000);

        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mediaPlayer != null){
                    int mCurrentPosition = mediaPlayer.getCurrentPosition() / 1000;
                    seekBar.setProgress(mCurrentPosition);
                }
                handler.postDelayed(this, 1000);
            }
        });
    }

    private void prevThreadButton() {
        prevThread = new Thread(){
            @Override
            public void run() {
                super.run();
                prevButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        prevButtonClicked();
                    }
                });
            }
        };
        prevThread.start();
    }

    private void prevButtonClicked() {
        if(mediaPlayer.isPlaying()){
            loadFile("prev");
            mediaPlayer.start();
            playPauseButton.setImageResource(R.drawable.ic_pause);

        } else {
            loadFile("prev");
            playPauseButton.setImageResource(R.drawable.ic_play);
        }
    }

    private void nextThreadButton() {
        nextThread = new Thread(){
            @Override
            public void run() {
                super.run();
                nextButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        nextButtonClicked();
                    }
                });
            }
        };
        nextThread.start();
    }

    private void nextButtonClicked() {
        if(mediaPlayer.isPlaying()){
            loadFile("next");
            mediaPlayer.start();
            playPauseButton.setImageResource(R.drawable.ic_pause);

        } else {
            loadFile("next");
            playPauseButton.setImageResource(R.drawable.ic_play);
        }
    }

    void loadFile(String which){
        mediaPlayer.stop();
        mediaPlayer.release();

        /* If the Prev/Next button is clicked while a song is playing, the prev/next song
           will start playing from the beginning. If the current song is finished/paused,
           then upon prev/next button click, prev/next song will be loaded at the beginning,
           but won't start playing */
        if (which == "prev"){
            /* Keep the position in the range of song list. If the first song
               is playing currently, the last song is loaded upon click */
            position = (((position - 1) < 0) ? (listOfSongs.size() -1) : (position - 1));

        } else if (which == "next"){
            /* Keep the position in the range of song list. If the last song
               is playing currently, the first song is loaded upon click */
            position = ((position+1) % listOfSongs.size());
        }

        uri = Uri.parse(listOfSongs.get(position).getPath());

        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        readFileMetaData(uri);

        songTitle.setText(listOfSongs.get(position).getTitle());
        artistName.setText(listOfSongs.get(position).getArtist());

        seekBar.setMax(mediaPlayer.getDuration() / 1000);

        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mediaPlayer != null){
                    int mCurrentPosition = mediaPlayer.getCurrentPosition() / 1000;
                    seekBar.setProgress(mCurrentPosition);
                }
                handler.postDelayed(this, 1000);
            }
        });
    }

    public static String milliSecondsToTimer(int mCurrentPosition) {
        String totalOut = "";
        String totalNew = "";
        String seconds = String.valueOf(mCurrentPosition % 60);
        String minutes = String.valueOf(mCurrentPosition / 60);
        totalOut = minutes + ":" + seconds;
        totalNew = minutes + ":" + "0" + seconds;

        if(seconds.length() == 1){
            return totalNew;
        } else {
            return  totalOut;
        }
    }

    private void getPlayIntent() {
        position = getIntent().getIntExtra("position", -1);
        listOfSongs = musicFiles;

        if(listOfSongs != null){
            playPauseButton.setImageResource(R.drawable.ic_pause);
            uri = Uri.parse(listOfSongs.get(position).getPath());
            Log.v(LOG_TAG, "URI received: " + uri);
        }

        try {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
            }
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        readFileMetaData(uri);
        seekBar.setMax((int)mediaPlayer.getDuration()/1000);
    }

    private void initViews() {
        songTitle = findViewById(R.id.tvSongTitle);
        artistName = findViewById(R.id.tvArtistName);
        durationPlayed = findViewById(R.id.tvElapsedDuration);
        durationTotal = findViewById(R.id.tvTotalDuration);
        coverArt = findViewById(R.id.ivAlbumArt);
        nextButton = findViewById(R.id.ivNextButton);
        prevButton = findViewById(R.id.ivPrevButton);
        shuffleButton = findViewById(R.id.ivShuffleButton);
        repeatButton = findViewById(R.id.ivRepeatButton);
        backButton = findViewById(R.id.ivBackButton);
        playPauseButton = findViewById(R.id.fbPlayPauseButton);
        seekBar = findViewById(R.id.seekBar);
    }

    private void readFileMetaData(Uri uri){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri.toString());

        byte[] art = retriever.getEmbeddedPicture();

        if(art != null){
            Glide.with(this)
                    .asBitmap()
                    .load(art)
                    .into(coverArt);
        } else {
            Glide.with(this)
                    .asBitmap()
                    .load(R.drawable.ic_music)
                    .into(coverArt);
        }

        int totalFileDuration = Integer.parseInt(listOfSongs.get(position).getDuration()) / 1000;
        durationTotal.setText(milliSecondsToTimer(totalFileDuration));
    }
}