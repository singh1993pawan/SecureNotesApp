package com.freeelective.securenotesapp.helper;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.freeelective.securenotesapp.R;
import com.freeelective.securenotesapp.data.Note;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.crypto.SecretKey;

public class NoteListAdapter extends ListAdapter<Note, NoteListAdapter.NoteViewHolder> {

    public NoteListAdapter(@NonNull DiffUtil.ItemCallback<Note> diffCallback) {
        super(diffCallback);
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_item, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note currentNote = getItem(position);
        try {
            holder.bind(currentNote);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        private final TextView noteTitleView;
        private final TextView noteContentView;
        private final TextView noteTimestampView;
        private final TextView noteCategoryView;

        private NoteViewHolder(View itemView) {
            super(itemView);
            noteTitleView = itemView.findViewById(R.id.note_title);
            noteContentView = itemView.findViewById(R.id.note_content);
            noteTimestampView = itemView.findViewById(R.id.note_timestamp);
            noteCategoryView = itemView.findViewById(R.id.note_category);
        }

        private String formatTimestamp(long timestamp) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        }

        public void bind(Note decryptedNote) {
            noteTitleView.setText(decryptedNote.getTitle());
            noteContentView.setText(decryptedNote.getContent());
            noteTimestampView.setText(formatTimestamp(Long.parseLong(decryptedNote.getTimestamp())));
            noteCategoryView.setText(decryptedNote.getCategory());
        }
    }

    public static class NoteDiff extends DiffUtil.ItemCallback<Note> {
        @Override
        public boolean areItemsTheSame(@NonNull Note oldItem, @NonNull Note newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Note oldItem, @NonNull Note newItem) {
            return oldItem.equals(newItem);
        }
    }
}
