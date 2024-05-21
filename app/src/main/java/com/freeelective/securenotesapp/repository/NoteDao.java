package com.freeelective.securenotesapp.repository;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.freeelective.securenotesapp.data.Note;

import java.util.List;

@Dao
public interface NoteDao {
    @Query("SELECT * FROM note ORDER BY timestamp DESC")
    LiveData<List<Note>> getAllNotes();

    @Query("SELECT * FROM note ORDER BY timestamp DESC")
    List<Note> getAllNotesForBackup();

    @Insert
    void insert(Note note);


}

