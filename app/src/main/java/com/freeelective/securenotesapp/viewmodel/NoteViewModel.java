package com.freeelective.securenotesapp.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.freeelective.securenotesapp.data.Note;
import com.freeelective.securenotesapp.helper.EncryptionUtils;
import com.freeelective.securenotesapp.repository.NoteRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NoteViewModel extends AndroidViewModel {
    private NoteRepository repository;
    private LiveData<List<Note>> allNotes;
    private ExecutorService executorService;

    public NoteViewModel(Application application) {
        super(application);
        repository = new NoteRepository(application);
        allNotes = repository.getAllNotes();
        executorService = Executors.newFixedThreadPool(2);
        try {
            EncryptionUtils.generateKey();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public LiveData<List<Note>> getAllNotes() {
        return allNotes;
    }

    public void insert(Note note) {
        executorService.execute(() -> repository.insert(note));
    }

}
