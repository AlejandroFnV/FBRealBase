package com.example.fbrealbase;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ChatSentence implements Parcelable {

    private String sentenceEn, sentenceEs, talker, time;

    public ChatSentence() {
    }

    public ChatSentence(String sentenceEn, String sentenceEs, String talker, String time) {
        this.sentenceEn = sentenceEn;
        this.sentenceEs = sentenceEs;
        this.talker = talker;
        this.time = time;
    }

    protected ChatSentence(Parcel in) {
        sentenceEn = in.readString();
        sentenceEs = in.readString();
        talker = in.readString();
        time = in.readString();
    }

    public static final Creator<ChatSentence> CREATOR = new Creator<ChatSentence>() {
        @Override
        public ChatSentence createFromParcel(Parcel in) {
            return new ChatSentence(in);
        }

        @Override
        public ChatSentence[] newArray(int size) {
            return new ChatSentence[size];
        }
    };

    public String getSentenceEn() {
        return sentenceEn;
    }

    public String getSentenceEs() {
        return sentenceEs;
    }

    public String getTalker() {
        return talker;
    }

    public String getTime() {
        return time;
    }

    public void setSentenceEn(String sentenceEn) {
        this.sentenceEn = sentenceEn;
    }

    public void setSentenceEs(String sentenceEs) {
        this.sentenceEs = sentenceEs;
    }

    public void setTalker(String talker) {
        this.talker = talker;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String
    toString() {
        return "ChatSentence{" +
                "sentenceEn='" + sentenceEn + '\'' +
                ", sentenceEs='" + sentenceEs + '\'' +
                ", talker='" + talker + '\'' +
                ", time='" + time + '\'' +
                '}';
    }

    //Convierte objetos en un Sring Object
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("sentenceEn", sentenceEn);
        result.put("sentenceEs", sentenceEs);
        result.put("talker", talker);
        result.put("time", time);
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(sentenceEn);
        dest.writeString(sentenceEs);
        dest.writeString(talker);
        dest.writeString(time);
    }
}
