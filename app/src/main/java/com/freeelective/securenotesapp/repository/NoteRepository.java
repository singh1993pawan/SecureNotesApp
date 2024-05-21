package com.freeelective.securenotesapp.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.freeelective.securenotesapp.data.Note;

import java.util.List;

public class NoteRepository {
    private NoteDao noteDao;

    public NoteRepository(Context context) {
        NoteDatabase db = NoteDatabase.getDatabase(context);
        noteDao = db.noteDao();
    }

    public LiveData<List<Note>> getAllNotes() {
        LiveData<List<Note>> encryptedNotes = noteDao.getAllNotes();
        MutableLiveData<List<Note>> decryptedNotes = new MutableLiveData<>();

        encryptedNotes.observeForever(notes -> {
            for (Note note : notes) {
                note.decryptContent();
            }
            decryptedNotes.setValue(notes);
        });

        return decryptedNotes;
    }

    public void insert(Note note) {
        NoteDatabase.databaseWriteExecutor.execute(() -> {
            note.encryptContent();
            noteDao.insert(note);
        });
    }
}
