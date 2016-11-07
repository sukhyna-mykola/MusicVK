package com.sukhyna_mykola.vkmusic;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by mikola on 05.11.2016.
 */

public class User implements Serializable {
    byte[] photo;



    String name;
    int id;

    public byte[] getPhoto() {
        return photo;
    }

    public void setPhoto(byte[] photo) {
        this.photo = photo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User(byte[] photo, String name, int id) {

        this.photo = photo;
        this.name = name;
        this.id = id;
    }
}
