package com.freeelective.securenotesapp.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.freeelective.securenotesapp.helper.EncryptionUtils;

import java.io.Serializable;

import javax.crypto.SecretKey;

@Entity(tableName = "note")
public class Note implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;
    private String content;
    private String timestamp;
    private String category;

    public Note() {
    }

    public Note(String title, String content, String timestamp, String category) {
        this.title = title;
        this.content = content;
        this.timestamp = timestamp;
        this.category = category;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void encryptContent() {
        try {
            this.content = EncryptionUtils.encrypt(this.content);
            this.title = EncryptionUtils.encrypt(this.title);
            this.category = EncryptionUtils.encrypt(this.category);
            this.timestamp = EncryptionUtils.encrypt(this.timestamp);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void decryptContent() {
        try {
            this.content = EncryptionUtils.decrypt(this.content);
            this.title = EncryptionUtils.decrypt(this.title);
            this.category = EncryptionUtils.decrypt(this.category);
            this.timestamp = EncryptionUtils.decrypt(this.timestamp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}