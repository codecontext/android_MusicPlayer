package com.kd.mBeats.Adapters;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.kd.mBeats.Activities.PlayerActivity;
import com.kd.mBeats.Models.MusicFiles;
import com.kd.mBeats.R;

import java.io.File;
import java.util.ArrayList;

import static com.kd.mBeats.Activities.MainActivity.LOG_TAG;
import static com.kd.mBeats.Activities.PlayerActivity.milliSecondsToTimer;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.SongsViewHolder> {

    private Context mContext;
    private ArrayList<MusicFiles> mFiles;

    public MusicAdapter(Context context, ArrayList<MusicFiles>files) {
        this.mFiles = files;
        this.mContext = context;
    }

    @NonNull
    @Override
    public SongsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.music_items, parent, false);

        return new SongsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongsViewHolder holder, int position) {
        byte[] image = getAlbumArt(mFiles.get(position).getPath());
        if (image != null) {
            Glide.with(mContext)
                    .asBitmap()
                    .load(image)
                    .into(holder.albumArt);
        } else {
            Glide.with(mContext)
                    .asBitmap()
                    .load(R.drawable.ic_music)
                    .into(holder.albumArt);
        }

        holder.songTitle.setText(mFiles.get(position).getTitle());
        holder.albumName.setText(mFiles.get(position).getAlbum());

        int totalSongDuration = Integer.parseInt(mFiles.get(position).getDuration()) / 1000;
        holder.songDuration.setText(milliSecondsToTimer(totalSongDuration));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, PlayerActivity.class);
                intent.putExtra("position", position);
                mContext.startActivity(intent);
            }
        });

        holder.menuMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(mContext, v);
                popupMenu.getMenuInflater().inflate(R.menu.popup, popupMenu.getMenu());
                popupMenu.show();

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.delete:
                                createDeletionAlertDialog(position, v);
                                break;
                        }
                        return true;
                    }
                });
            }
        });
    }

    private void createDeletionAlertDialog(int position, View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage("Confirm Deletion?");
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.v(LOG_TAG, "File Delete Position: "+position);
                deleteFile(position, v);
            }
        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.create();
        builder.show();
    }

    private void deleteFile(int position, View v) {

        try {
            Uri contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    Long.parseLong(mFiles.get(position).getId()));
            Log.v(LOG_TAG, "Deletion URI: "+ contentUri);

            /* This code below will permanently delete the selected file from storage*/
            File file = new File(mFiles.get(position).getPath());
            boolean deleted = file.delete();

            if (deleted) {
                mContext.getContentResolver().delete(contentUri, null, null);
                mFiles.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, mFiles.size());

                Snackbar.make(v, "File Deleted", Snackbar.LENGTH_LONG)
                        .show();
            } else {
                /* May be file is in SD card, and API level 19 and above */
                Snackbar.make(v, "Can't Delete File", Snackbar.LENGTH_LONG)
                        .show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return mFiles.size();
    }

    public class SongsViewHolder extends RecyclerView.ViewHolder{
        ImageView albumArt;
        ImageView menuMore;

        TextView songTitle;
        TextView albumName;
        TextView songDuration;

        public SongsViewHolder(@NonNull View itemView) {
            super(itemView);

            albumArt = itemView.findViewById(R.id.albumArt);
            menuMore = itemView.findViewById(R.id.menuMore);
            songTitle = itemView.findViewById(R.id.songTitle);
            albumName = itemView.findViewById(R.id.albumTitle);
            songDuration = itemView.findViewById(R.id.songDuration);
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
