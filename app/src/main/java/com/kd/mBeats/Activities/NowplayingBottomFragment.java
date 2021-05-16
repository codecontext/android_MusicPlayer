package com.kd.mBeats.Activities;

import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.kd.mBeats.R;

import static com.kd.mBeats.Activities.MainActivity.ARTISH_TO_FRAG;
import static com.kd.mBeats.Activities.MainActivity.PATH_TO_FRAG;
import static com.kd.mBeats.Activities.MainActivity.SHOW_MINI_PLAYER;
import static com.kd.mBeats.Activities.MainActivity.SONGTITLE_TO_FRAG;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NowplayingBottomFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NowplayingBottomFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    ImageView prevButton;
    ImageView nextButton;
    ImageView albumArt;
    TextView songTitle;
    TextView artistName;
    FloatingActionButton playPauseButton;

    View view;


    public NowplayingBottomFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment NowplayingBottomFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NowplayingBottomFragment newInstance(String param1, String param2) {
        NowplayingBottomFragment fragment = new NowplayingBottomFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_nowplaying_bottom, container, false);

        songTitle = view.findViewById(R.id.songTitleMini);
        artistName = view.findViewById(R.id.artistNameMini);
        albumArt = view.findViewById(R.id.albumArtMini);
        prevButton = view.findViewById(R.id.skipPrevMini);
        nextButton = view.findViewById(R.id.skipNextMini);
        playPauseButton = view.findViewById(R.id.playPauseMini);

        songTitle.setSelected(true);
        artistName.setSelected(true);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if(SHOW_MINI_PLAYER){
            if(PATH_TO_FRAG != null) {
                byte[] art = getAlbumArt(PATH_TO_FRAG);
                if(art != null) {
                    Glide.with(getContext())
                            .load(art)
                            .into(albumArt);
                } else {
                    Glide.with(getContext())
                            .load(R.drawable.ic_music)
                            .into(albumArt);
                }
                songTitle.setText(SONGTITLE_TO_FRAG);
                artistName.setText(ARTISH_TO_FRAG);
            }
        }
    }

    private byte[] getAlbumArt(String uri){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        byte[] art = {0};

        try {
            retriever.setDataSource(uri);
            art = retriever.getEmbeddedPicture();
            retriever.release();
        }catch (Exception e) {
            e.printStackTrace();
        }

        return art;
    }
}