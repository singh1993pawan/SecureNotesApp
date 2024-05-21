package com.freeelective.securenotesapp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.freeelective.securenotesapp.data.Note;
import com.freeelective.securenotesapp.databinding.ActivityMainBinding;
import com.freeelective.securenotesapp.helper.BackupWorker;
import com.freeelective.securenotesapp.helper.NoteListAdapter;
import com.freeelective.securenotesapp.ui.SignInActivity;
import com.freeelective.securenotesapp.viewmodel.NoteViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private NoteViewModel noteViewModel;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);
        binding.setViewModel(noteViewModel);
        binding.setLifecycleOwner(this);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        final NoteListAdapter adapter = new NoteListAdapter(new NoteListAdapter.NoteDiff());
        recyclerView.setAdapter(adapter);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        noteViewModel.getAllNotes().observe(this, notes -> {
            adapter.submitList(notes);
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> showAddNoteDialog());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            askNotificationPermission();
        }

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        }

        PeriodicWorkRequest backupRequest = new PeriodicWorkRequest.Builder(BackupWorker.class, 20, TimeUnit.MINUTES)
                .build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "BackupWork",
                ExistingPeriodicWorkPolicy.REPLACE,
                backupRequest
        );
    }

    private void showAddNoteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_note, null);
        builder.setView(dialogView);

        EditText noteTitle = dialogView.findViewById(R.id.note_title);
        EditText noteContent = dialogView.findViewById(R.id.note_content);
        EditText noteCategory = dialogView.findViewById(R.id.note_category);
        Button saveButton = dialogView.findViewById(R.id.save_button);

        AlertDialog dialog = builder.create();

        saveButton.setOnClickListener(v -> {
            String title = noteTitle.getText().toString();
            String content = noteContent.getText().toString();
            String category = noteCategory.getText().toString();
            if (!title.isEmpty() && !content.isEmpty()) {
                Note note = new Note();
                note.setTitle(title);
                note.setContent(content);
                note.setTimestamp(String.valueOf(System.currentTimeMillis()));
                note.setCategory(category);
                noteViewModel.insert(note);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void askNotificationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
                new AlertDialog.Builder(this)
                        .setTitle("Notification Permission Needed")
                        .setMessage("This app needs the Notification permission to notify you about important updates.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
                            }
                        })
                        .create()
                        .show();
            } else {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            });
}