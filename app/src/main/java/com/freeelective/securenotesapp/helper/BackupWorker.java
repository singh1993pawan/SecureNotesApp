package com.freeelective.securenotesapp.helper;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.freeelective.securenotesapp.MainActivity;
import com.freeelective.securenotesapp.R;
import com.freeelective.securenotesapp.data.Note;
import com.freeelective.securenotesapp.repository.NoteDao;
import com.freeelective.securenotesapp.repository.NoteDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;


public class BackupWorker extends Worker {
    private static final String TAG = "BackupWorker";
    private static final int NOTIFICATION_ID = 12345;
    private static final String CHANNEL_ID = "BackupChannel";
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    private NoteDao noteDao;

    public BackupWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        NoteDatabase db = NoteDatabase.getDatabase(context);
        noteDao = db.noteDao();
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Backup work started");
        createNotificationChannel();
        backupNotes();
        return Result.success();
    }

    private void backupNotes() {
        new Thread(() -> {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user == null) {
                return;
            }

            List<Note> notes = fetchNotesFromDatabase();
            if (notes == null || notes.isEmpty()) {
                return;
            }

            File notesFile = new File(getApplicationContext().getFilesDir(), "notes_backup.txt");
            try (FileOutputStream fos = new FileOutputStream(notesFile)) {
                for (Note note : notes) {
                    fos.write(note.toString().getBytes());
                }
            } catch (IOException e) {
                return;
            }

            StorageReference storageRef = storage.getReference();
            StorageReference notesRef = storageRef.child("backups/" + user.getUid() + "/notes_backup.txt");
            UploadTask uploadTask = notesRef.putFile(Uri.fromFile(notesFile));

            uploadTask.addOnSuccessListener(taskSnapshot -> {
                sendBackupNotification("Backup Completed", "Your notes have been successfully backed up to Firebase Storage.");
            }).addOnFailureListener(e -> {
            });
        }).start();
    }

    private List<Note> fetchNotesFromDatabase() {
        return noteDao.getAllNotesForBackup();
    }

    private void sendBackupNotification(String title, String text) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Backup Notifications";
            String description = "Notifications for backup";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }
}


