package com.example.blink22.criminalintent;

import java.util.Date;
import java.util.UUID;

/**
 * Created by blink22 on 5/29/18.
 */
public class Crime {
    private UUID mId;
    private String mTitle;
    private Date mDate;
    private String mSuspect;
    private boolean mSolved;

    public String getPhone() {
        return mPhone;
    }

    public void setPhone(String phone) {
        this.mPhone = phone;
    }

    private String mPhone;

    public Crime(){
        this(UUID.randomUUID());
    }

    public Crime (UUID id){
        mId = id;
        mDate = new Date();
    }

    public String getSuspect() {
        return mSuspect;
    }

    public void setSuspect(String suspect) {
        this.mSuspect = suspect;
    }

    public String getTitle() {
        return mTitle;
    }

    public UUID getId() {
        return mId;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public boolean isSolved() {
        return mSolved;
    }

    public Date getDate() {
        return mDate;
    }

    public void setSolved(boolean solved) {
        mSolved = solved;
    }

    public void setDate(Date date) {
        mDate = date;
    }
}
