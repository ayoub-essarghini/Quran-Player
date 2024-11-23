package com.items.mp3player.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class AudioModel implements Parcelable {
    private String path;
    private String fileName;
    private long duration;

    private String artist;
    private String title;

    int id;

    // Constructor
    public AudioModel(String path, String fileName, long duration, String artist, String title) {
        this.path = path;
        this.fileName = fileName;
        this.duration = duration;

        this.artist = artist;
        this.title = title;
    }


    protected AudioModel(Parcel in) {
        path = in.readString();
        fileName = in.readString();
        duration = in.readLong();
        artist = in.readString();
        title = in.readString();
    }

    public static final Creator<AudioModel> CREATOR = new Creator<AudioModel>() {
        @Override
        public AudioModel createFromParcel(Parcel in) {
            return new AudioModel(in);
        }

        @Override
        public AudioModel[] newArray(int size) {
            return new AudioModel[size];
        }
    };

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }


    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(path);
        parcel.writeString(fileName);
        parcel.writeLong(duration);
        parcel.writeString(artist);
        parcel.writeString(title);
    }
}

