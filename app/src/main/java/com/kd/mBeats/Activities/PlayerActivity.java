package com.kd.mBeats.Activities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kd.mBeats.Interfaces.ActionPlaying;
import com.kd.mBeats.Models.MusicFiles;
import com.kd.mBeats.R;
import com.kd.mBeats.Services.MusicService;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Random;

import static com.kd.mBeats.Activities.MainActivity.LOG_TAG;
import static com.kd.mBeats.Activities.MainActivity.repeatButtonState;
import static com.kd.mBeats.Activities.MainActivity.shuffleButtonState;
import static com.kd.mBeats.Adapters.AlbumDetailsAdapter.albumFiles;
import static com.kd.mBeats.Adapters.MusicAdapter.mFiles;

public class PlayerActivity extends AppCompatActivity
        implements ActionPlaying, ServiceConnection {

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
    ImageView listButton;

    FloatingActionButton playPauseButton;
    SeekBar seekBar;

    final boolean PALETTE_SWATCH_ON = false;
    final String SELECT_SONG_MSG = "Please select a song from the list";
    int position = POSITION_INVALID;

    public static final int POSITION_INVALID = -1;
    public static ArrayList<MusicFiles> listOfSongs = new ArrayList<>();

    static Uri uri;

    private Handler handler = new Handler();

    MusicService musicService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(LOG_TAG, "PlayerActivity onCreate()");

        super.onCreate(savedInstanceState);
        setFullScreen();
        setContentView(R.layout.activity_player);
        getSupportActionBar().hide();

        initViews();

        getPlayIntent();

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if((musicService!= null) && (fromUser)){
                    musicService.seekTo(progress * 1000);
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
                if(musicService != null){
                    if((listOfSongs != null) & (position != POSITION_INVALID)) {
                        int mCurrentPosition = musicService.getCurrentPosition() / 1000;

                        seekBar.setProgress(mCurrentPosition);
                        durationPlayed.setText(milliSecondsToTimer(mCurrentPosition));
                    }
                }
                handler.postDelayed(this, 1000);
            }
        });

        shuffleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if((listOfSongs != null) & (position != POSITION_INVALID)) {
                    if (shuffleButtonState) {
                        shuffleButtonState = false;
                        shuffleButton.setImageResource(R.drawable.ic_shuffle_off);
                    } else {
                        shuffleButtonState = true;
                        shuffleButton.setImageResource(R.drawable.ic_shuffle_on);
                    }
                }
            }
        });

        repeatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((listOfSongs != null) & (position != POSITION_INVALID)) {
                    if (repeatButtonState) {
                        repeatButtonState = false;
                        repeatButton.setImageResource(R.drawable.ic_repeat_off);
                    } else {
                        repeatButtonState = true;
                        repeatButton.setImageResource(R.drawable.ic_repeat_one);
                    }
                }
            }
        });

        listButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                             WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    protected void onResume() {
        Log.e(LOG_TAG, "PlayerActivity onResume()");

        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, this, BIND_AUTO_CREATE);

        playThreadButton();

        if((listOfSongs != null) & (position != POSITION_INVALID)) {
            prevThreadButton();
            nextThreadButton();
        }

        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.e(LOG_TAG, "PlayerActivity onPause()");
        super.onPause();
        unbindService(this);
    }

    @Override
    protected void onDestroy() {
        Log.e(LOG_TAG, "PlayerActivity onDestroy()");
        super.onDestroy();
    }

    private void playThreadButton() {
        Thread playThread = new Thread() {
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

    public void playPauseButtonClicked() {
        if((listOfSongs != null) & (position != POSITION_INVALID)) {
            if (musicService.isPlaying()) {
                musicService.pause();
                playPauseButton.setImageResource(R.drawable.ic_play);
                musicService.showNotification(R.drawable.ic_play);
            } else {
                musicService.start();
                playPauseButton.setImageResource(R.drawable.ic_pause);
                musicService.showNotification(R.drawable.ic_pause);
            }

            seekBar.setMax(musicService.getDuration() / 1000);

            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService != null) {
                        int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this, 1000);
                }
            });
        } else {
            Toast.makeText(getApplicationContext(), SELECT_SONG_MSG, Toast.LENGTH_SHORT).show();
        }
    }

    private void prevThreadButton() {
        Thread prevThread = new Thread() {
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

    public void prevButtonClicked() {
        if(musicService.isPlaying()){
            loadFile("prev");
            musicService.start();
            playPauseButton.setBackgroundResource(R.drawable.ic_pause);
            musicService.showNotification(R.drawable.ic_pause);

        } else {
            loadFile("prev");
            playPauseButton.setBackgroundResource(R.drawable.ic_play);
            musicService.showNotification(R.drawable.ic_play);
        }
        musicService.OnCompleted();
    }

    private void nextThreadButton() {
        Thread nextThread = new Thread() {
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

    public void nextButtonClicked() {
        if(musicService.isPlaying()){
            loadFile("next");
            musicService.start();
            playPauseButton.setBackgroundResource(R.drawable.ic_pause);
            musicService.showNotification(R.drawable.ic_pause);
        } else {
            loadFile("next");
            playPauseButton.setBackgroundResource(R.drawable.ic_play);
            musicService.showNotification(R.drawable.ic_play);
        }
        musicService.OnCompleted();
    }

    void loadFile(String which){
        musicService.stop();
        musicService.release();

        /* If the Prev/Next button is clicked while a song is playing, the prev/next song
           will start playing from the beginning. If the current song is finished/paused,
           then upon prev/next button click, prev/next song will be loaded at the beginning,
           but won't start playing */

        if(shuffleButtonState && !repeatButtonState){
            position = getRandom(listOfSongs.size() - 1);

        } else if(!shuffleButtonState && !repeatButtonState) {
            if (which == "prev") {
                /* Keep the position in the range of song list. If the first song
                   is playing currently, the last song is loaded upon click */
                position = (((position - 1) < 0) ? (listOfSongs.size() - 1) : (position - 1));

            } else if (which == "next") {
                /* Keep the position in the range of song list. If the last song
                   is playing currently, the first song is loaded upon click */
                position = ((position + 1) % listOfSongs.size());

            }
        } else {
            /* This condition indicates the repeat button is set.
               So, don't change the position irrespective of shuffle button */
        }
        saveData(listOfSongs);

        uri = Uri.parse(listOfSongs.get(position).getPath());

        musicService.createMediaPlayer(position);
        readFileMetaData(uri);

        songTitle.setText(listOfSongs.get(position).getTitle());
        artistName.setText(listOfSongs.get(position).getArtist());

        seekBar.setMax(musicService.getDuration() / 1000);

        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(musicService != null){
                    int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                    seekBar.setProgress(mCurrentPosition);
                }
                handler.postDelayed(this, 1000);
            }
        });
    }

    private int getRandom(int i) {
        Random random = new Random();
        return random.nextInt(i+1);
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
        position = getIntent().getIntExtra("position", POSITION_INVALID);
        Log.e(LOG_TAG, "Position received: "+position);

        String sender = getIntent().getStringExtra("sender");
        Log.e(LOG_TAG, "Sender: "+sender);

        if((sender != null) && (sender.equals("albumDetails"))){
            /* List songs from selected album */
            listOfSongs = albumFiles;
            saveData(listOfSongs);
            playSong();
        } else if ((sender != null) && (sender.equals("songsList"))){
            /* List songs from all songs */
            listOfSongs = mFiles;
            saveData(listOfSongs);
            playSong();
        } else {
            /* Load song list from saved shared preference */
            listOfSongs = loadData();
            playSong();
        }
    }

    private void playSong() {
        if(listOfSongs != null) {
            Intent intent = new Intent(this, MusicService.class);
            intent.putExtra("servicePosition", position);
            //startService(intent);
            ContextCompat.startForegroundService(this, intent);
        }
    }

    private void saveData(ArrayList<MusicFiles> list) {
        SharedPreferences preferences = getSharedPreferences("SAVED_SONGLIST", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        Gson gson = new Gson();
        String json = gson.toJson(list);

        editor.putString("songs", json);
        editor.apply();

        Log.e(LOG_TAG, "Saved Song Position: "+position);
        editor.putInt("song_position", position);
        editor.apply();
    }

    private ArrayList<MusicFiles> loadData() {
        ArrayList<MusicFiles> savedList = new ArrayList<>();

        SharedPreferences preferences = getSharedPreferences("SAVED_SONGLIST", MODE_PRIVATE);

        Gson gson = new Gson();
        String json = preferences.getString("songs", null);

        /* Get the type of array list */
        Type type = new TypeToken<ArrayList<MusicFiles>>() {}.getType();

        /* Receive data from gson and save it to array list */
        savedList = gson.fromJson(json, type);

        if (savedList == null) {
            savedList = new ArrayList<>();
        }
        position = preferences.getInt("song_position", POSITION_INVALID);

        return savedList;
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
        playPauseButton = findViewById(R.id.fbPlayPauseButton);
        seekBar = findViewById(R.id.seekBar);
        backButton = findViewById(R.id.ivAppIcon);
        listButton = findViewById(R.id.ivListButton);
    }

    private void readFileMetaData(Uri uri){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri.toString());

        byte[] art = retriever.getEmbeddedPicture();
        Bitmap bitmap;

        if(art != null){
            Glide.with(this)
                    .asBitmap()
                    .load(art)
                    .into(coverArt);

            if(PALETTE_SWATCH_ON) {
                /*  In this logic below, the play screen background, song title and artist name
                    are filled with the color of the Cover Art, dynamically reading from the current song */
                bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
                Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                    @Override
                    public void onGenerated(@Nullable Palette palette) {
                        Palette.Swatch swatch = palette.getDominantSwatch();

                        ImageView gradient = findViewById(R.id.ivGradientShade);
                        ConstraintLayout mContainer = findViewById(R.id.mContainer);
                        gradient.setBackgroundResource(R.drawable.bg_gradient_linear);
                        mContainer.setBackgroundResource(R.drawable.main_bg);

                        if (swatch != null) {
                            GradientDrawable gradientDrawable = new GradientDrawable(
                                    GradientDrawable.Orientation.BOTTOM_TOP,
                                    new int[]{swatch.getRgb(), 0x00000000});
                            gradient.setBackground(gradientDrawable);

                            GradientDrawable gradientDrawableBg = new GradientDrawable(
                                    GradientDrawable.Orientation.BOTTOM_TOP,
                                    new int[]{swatch.getRgb(), swatch.getRgb()});
                            mContainer.setBackground(gradientDrawableBg);

                            /* Set the Song Title and Album text color based on the album*/
                            songTitle.setTextColor(swatch.getTitleTextColor());
                            artistName.setTextColor(swatch.getBodyTextColor());
                        } else {
                            GradientDrawable gradientDrawable = new GradientDrawable(
                                    GradientDrawable.Orientation.BOTTOM_TOP,
                                    new int[]{0xFF000000, 0x00000000});
                            gradient.setBackground(gradientDrawable);

                            GradientDrawable gradientDrawableBg = new GradientDrawable(
                                    GradientDrawable.Orientation.BOTTOM_TOP,
                                    new int[]{0xFF000000, 0xFF000000});
                            mContainer.setBackground(gradientDrawableBg);

                        /* If swatch is not available,
                           Set the Song Title and Album text color to constant WHITE */
                            songTitle.setTextColor(Color.WHITE);
                            artistName.setTextColor(Color.WHITE);
                        }
                    }
                });
            }
        } else {
            Glide.with(this)
                    .asBitmap()
                    .load(R.drawable.ic_music)
                    .into(coverArt);

            ImageView gradient = findViewById(R.id.ivGradientShade);
            ConstraintLayout mContainer = findViewById(R.id.mContainer);
            gradient.setBackgroundResource(R.drawable.bg_gradient_linear);
            mContainer.setBackgroundResource(R.drawable.main_bg);

            /* If no Cover Art is available,
               Set the Song Title and Album text color to constant WHITE */
            songTitle.setTextColor(Color.WHITE);
            artistName.setTextColor(Color.WHITE);
        }

        int totalFileDuration = Integer.parseInt(listOfSongs.get(position).getDuration()) / 1000;
        durationTotal.setText(milliSecondsToTimer(totalFileDuration));
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        MusicService.MyBinder myBinder = (MusicService.MyBinder) service;
        musicService = myBinder.getService();
        musicService.setCallback(this);

        if ((listOfSongs != null)) {
            if ((position != POSITION_INVALID)) {

                uri = Uri.parse(listOfSongs.get(position).getPath());
                Log.e(LOG_TAG, "URI received: " + uri);
                Log.e(LOG_TAG, "Position: " + position);

                readFileMetaData(uri);

                seekBar.setMax((int) musicService.getDuration() / 1000);
                songTitle.setText(listOfSongs.get(position).getTitle());
                artistName.setText(listOfSongs.get(position).getArtist());
                playPauseButton.setImageResource(R.drawable.ic_pause);
                musicService.showNotification(R.drawable.ic_pause);

                musicService.OnCompleted();
            } else {
                Log.e(LOG_TAG, "Invalid song position");
            }
        } else {
            Log.e(LOG_TAG, "Song List Empty");
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        musicService = null;
    }
}