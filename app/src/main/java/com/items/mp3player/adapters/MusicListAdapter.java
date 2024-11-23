package com.items.mp3player.adapters;

import static com.items.mp3player.Constants.Native_Position;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.ads.NativeAdLayout;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.material.card.MaterialCardView;
import com.items.mp3player.Ads.AdsControle;
import com.items.mp3player.Constants;
import com.items.mp3player.R;
import com.items.mp3player.db.DBHelper;
import com.items.mp3player.interfaces.OnItemClickListener;
import com.items.mp3player.model.AudioModel;

import java.util.ArrayList;

public class MusicListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    ArrayList<AudioModel> songsList;
    Context context;
    OnItemClickListener listener;
    boolean isFav;
    AdsControle adsControle;
    DBHelper fav;
    int Type_Item = 0, Type_Ads = 1;
    int selectedPosition = -1;  // New: Track selected position

    public MusicListAdapter(ArrayList<AudioModel> songsList, Activity context, OnItemClickListener listener, boolean isFav) {
        this.songsList = songsList;
        this.context = context;
        adsControle = new AdsControle(context);
        this.listener = listener;
        this.isFav = isFav;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == Type_Item) {
            View view = LayoutInflater.from(context).inflate(R.layout.recycler_item, parent, false);
            fav = new DBHelper(context);
            return new ViewHolder(view);
        } else {
            FrameLayout v = (FrameLayout) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.native_layout, parent, false);
            // set the view's size, margins, paddings and layout parameters
            View view = v.findViewById(R.id.Native);
            v.removeView(view);
            ViewHolderAds vie = new ViewHolderAds(view);

            return vie;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        if (holder.getItemViewType() == Type_Item) {
            if (position > Native_Position) {
                position--;
            }
            ViewHolder holder1 = (ViewHolder) holder;
            AudioModel songData = songsList.get(position);
            holder1.titleTextView.setText(songData.getTitle());
            holder1.reader.setText(songData.getArtist());

            // Set favorite icon state
            if (fav.isFavorite(songData.getFileName()))
                holder1.fav_icon.setImageResource(R.drawable.heart_filled);
            else
                holder1.fav_icon.setImageResource(R.drawable.heart_outlined);
            // Update background based on selected position
            update_item_bg(holder1, position, songData);
            if (!isFav)
                holder1.surah_id.setText(songData.getId() + "");
            else
                holder1.surah_id.setText((position + 1) + "");

            // Handle item click to select
            int finalPosition = position;
            holder1.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(finalPosition);

                    // Track selected item
                    int previousPosition = selectedPosition;
                    selectedPosition = finalPosition;

                    // Notify adapter to update the previous and current selection
                    notifyItemChanged(previousPosition);  // Reset the previous selected item
                    notifyItemChanged(selectedPosition);  // Update the new selected item
                }
            });

            // Handle favorite icon click
            holder1.fav_icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onFavClick(finalPosition);
                    if (fav.isFavorite(songData.getFileName()))
                        holder1.fav_icon.setImageResource(R.drawable.heart_filled);
                    else
                        holder1.fav_icon.setImageResource(R.drawable.heart_outlined);
                }
            });
        }
        if (holder.getItemViewType() == Type_Ads) {
            ViewHolderAds holderAds = (ViewHolderAds) holder;
            if (Constants.Ads_Type == 0) {
                adsControle.ShowNative(holderAds.itemView);
            } else if (Constants.Ads_Type == 2) {
                adsControle.ShowNative(holderAds.itemView);
            } else if (Constants.Ads_Type == 1) {
                adsControle.ShowNative(holderAds.itemView);
            }
        }
    }

    // Add this method to MusicListAdapter
    public void setSelectedPosition(int position) {
        int previousPosition = selectedPosition;
        selectedPosition = position;

        // Notify adapter to update both the previous and current selection
        notifyItemChanged(previousPosition);
        notifyItemChanged(selectedPosition);
    }


    // Update the UI based on whether the item is selected or not
    public void update_item_bg(ViewHolder holder, int position, AudioModel song) {
        if (selectedPosition == position) {
            // Highlight the selected item
            holder.titleTextView.setTextColor(context.getResources().getColor(R.color.white));
            holder.reader.setTextColor(context.getResources().getColor(R.color.white));
            holder.card.setCardBackgroundColor(context.getResources().getColor(R.color.main_color));
            if (!fav.isFavorite(song.getFileName()))
                holder.fav_icon.setColorFilter(context.getResources().getColor(R.color.white));
            else
                holder.fav_icon.setColorFilter(context.getResources().getColor(R.color.white));
            holder.id_frame.setColorFilter(context.getResources().getColor(R.color.white));
            holder.surah_id.setTextColor(context.getResources().getColor(R.color.white));

        } else {
            // Set to default style for unselected items
            holder.titleTextView.setTextColor(context.getResources().getColor(R.color.main_color));
            holder.reader.setTextColor(context.getResources().getColor(R.color.black));
            holder.card.setCardBackgroundColor(context.getResources().getColor(R.color.white));
            if (!fav.isFavorite(song.getFileName()))
                holder.fav_icon.setColorFilter(context.getResources().getColor(R.color.main_color));
            else
                holder.fav_icon.setColorFilter(context.getResources().getColor(R.color.main_color));
            holder.id_frame.setColorFilter(context.getResources().getColor(R.color.main_color));
            holder.surah_id.setTextColor(context.getResources().getColor(R.color.black));


        }
    }

    public class ViewHolderAds extends RecyclerView.ViewHolder {
        public LinearLayout Native_containerApplovin;
        public FrameLayout LayoutNative;
        public NativeAdLayout nativeAdLayoutFB;
        public TemplateView templateViewAdmob;

        public ShimmerFrameLayout shimmerFrameLayout;

        public ViewHolderAds(@NonNull View itemView) {
            super(itemView);
            LayoutNative = itemView.findViewById(R.id.Native);
            Native_containerApplovin = itemView.findViewById(R.id.native_adsContent);
            nativeAdLayoutFB = itemView.findViewById(R.id.native_ad_container);
            templateViewAdmob = itemView.findViewById(R.id.AdmobNative);
            shimmerFrameLayout = itemView.findViewById(R.id.shimmer);


        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == Native_Position)
            return Type_Ads;
        else
            return Type_Item;
    }

    @Override
    public int getItemCount() {
        if (songsList.size() > Native_Position)
            return songsList.size() + 1;
        else
            return songsList.size();

    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, surah_id;
        ImageView fav_icon, id_frame;
        TextView reader;
        MaterialCardView card;


        public ViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.music_title_text);
            fav_icon = itemView.findViewById(R.id.fav_icon);
            reader = itemView.findViewById(R.id.artist_text);
            card = itemView.findViewById(R.id.card_root);
            surah_id = itemView.findViewById(R.id.id_surah_text);
            id_frame = itemView.findViewById(R.id.id_frame);
        }
    }
}


