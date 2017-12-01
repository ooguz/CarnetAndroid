package com.spisoft.quicknote.browser;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.spisoft.quicknote.Note;
import com.spisoft.quicknote.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by alexandre on 03/02/16.
 */
public class NoteAdapter extends RecyclerView.Adapter {

    private static final int NOTE = 0;
    protected final Context mContext;
    private final HashMap<Note, String> mText;
    protected List<Object> mNotes;
    private ArrayList<Object> mSelelectedNotes;

    public NoteAdapter(Context context, List<Object> notes) {
        super();
        mContext = context;
        mNotes = notes;
        mText = new HashMap<Note, String>();
    }

    public void setNotes(List<Object> notes) {
        int i = 0;
        List<Object> oldNotes = new ArrayList<>(mNotes);
        List<Object> oldNotes1 = new ArrayList<>(mNotes);
        List<Object> newNotes = new ArrayList<>(notes);
        List<Object> toRemove = new ArrayList<>();
        mNotes = notes;
        for (Object note : oldNotes1) {
            if (!notes.contains(note)) {
                i = oldNotes.indexOf(note);
                oldNotes.remove(i);
                notifyItemRemoved(i);
            }
        }
        for (Object note : notes) {
            if (!oldNotes.contains(note)) {
                notifyItemInserted(oldNotes.size());
                oldNotes.add(note);
            }
        }
        i = 0;
        oldNotes1 = new ArrayList<>(oldNotes);
        for (Object note : oldNotes) {
            int newPos = notes.indexOf(note);
            int currentPos = oldNotes1.indexOf(note);
            if (newPos >= 0 && newPos != currentPos) {
                newNotes.remove(note);
                Log.d("notedebug", "notifyItemMoved(" + i + ", " + newPos + ")");
                oldNotes1.remove(currentPos);
                oldNotes1.add(newPos, note);
                notifyItemMoved(currentPos, newPos);

            }
            if (newPos == -1)
                toRemove.add(note);

            i++;
        }


    }

    public void addNote(Object note) {
        mNotes.add(note);
        notifyItemInserted(mNotes.size() - 1);
    }

    public void setText(Note note, String text) {
        mText.put(note, text);
        int index = mNotes.indexOf(note);
        mNotes.remove(index);
        mNotes.add(index, note);
        notifyItemChanged(index);

    }

    public void onItemMove(int adapterPosition, int adapterPosition1) {
        Note note = (Note) mNotes.get(adapterPosition);
        mNotes.remove(adapterPosition);
        mNotes.add(adapterPosition1, note);
        notifyItemMoved(adapterPosition, adapterPosition1);
    }

    public void toggleNote(Object note, View v) {
        if (mSelelectedNotes == null)
            mSelelectedNotes = new ArrayList<>();
        if (mSelelectedNotes.contains(note)) {
            Log.d("selectdebug", "remove");
            mSelelectedNotes.remove(note);
        } else {
            Log.d("selectdebug", "add");
            mSelelectedNotes.add(note);
        }
        notifyItemChanged(mNotes.indexOf(note));
    }

    public List<Object> getSelectedObjects() {
        return mSelelectedNotes;
    }

    public void clearSelection() {
        if (mSelelectedNotes != null)
            mSelelectedNotes.clear();
        notifyDataSetChanged();
    }

    public class NoteViewHolder extends RecyclerView.ViewHolder {


        private final View mCard;

        public NoteViewHolder(View itemView) {
            super(itemView);
            mCard = itemView.findViewById(R.id.cardview);
        }

        public void setName(String title) {
            ((TextView) itemView.findViewById(R.id.name_tv)).setText(title);
        }

        public void setNote(final Note note) {

            mCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mSelelectedNotes != null && mSelelectedNotes.size() > 0)
                        mOnNoteClick.onLongClick(note, view);
                    else
                        mOnNoteClick.onNoteClick(note, view);
                }
            });
            mCard.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    mOnNoteClick.onLongClick(note, view);
                    return true;
                }
            });
            itemView.findViewById(R.id.optionsButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnNoteClick.onInfoClick(note, view);
                }
            });
            setName(note.title);
            setText(note.shortText);
            setDate(note.mMetadata.last_modification_date);
            setKeywords(note.mMetadata.keywords);
        }

        public void setText(String s) {
            if (s != null)
                ((TextView) itemView.findViewById(R.id.text_tv)).setText(s);
            else
                ((TextView) itemView.findViewById(R.id.text_tv)).setText("");
        }

        public void setDate(long lastModified) {
            if (lastModified == -1)
                ((TextView) itemView.findViewById(R.id.date_tv)).setText("");
            else {
                String dateString = new SimpleDateFormat("dd/MM/yyyy").format(new Date(lastModified));
                ((TextView) itemView.findViewById(R.id.date_tv)).setText(dateString);
            }
        }

        public void setKeywords(List<String> keywords) {
            ViewGroup keywordsView = (ViewGroup) itemView.findViewById(R.id.keywords);
            keywordsView.removeAllViews();
            LayoutInflater inflater = LayoutInflater.from(mContext);
            for (String keyword : keywords) {
                View keyview = inflater.inflate(R.layout.keyword_item, null);
                ((TextView) keyview.findViewById(R.id.textView)).setText(keyword);
                keywordsView.addView(keyview);

            }
        }

        public void setSelected(boolean contains) {
            mCard.setSelected(contains);

        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d("notedebug", "" + viewType);
        if (viewType == NOTE)
            return new NoteViewHolder(LayoutInflater.from(mContext).inflate(R.layout.grid_note_layout, null));
        else return null;
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == NOTE) {
            Note note = (Note) mNotes.get(position);
            NoteViewHolder viewHolder = (NoteViewHolder) holder;
            viewHolder.setNote(note);

            viewHolder.setSelected(mSelelectedNotes != null && mSelelectedNotes.contains(note));
            Log.d("notedebug", note.title);
        }

    }

    public int getItemViewType(int position) {
        if (mNotes.get(position) instanceof Note)
            return NOTE;
        return -1;
    }

    OnNoteItemClickListener mOnNoteClick;

    void setOnNoteClickListener(OnNoteItemClickListener listener) {
        mOnNoteClick = listener;
    }

    public interface OnNoteItemClickListener {
        public void onNoteClick(Note note, View v);

        void onInfoClick(Note note, View v);

        void onLongClick(Note note, View v);
    }

    @Override
    public int getItemCount() {
        Log.d("notedebug", "mNotes " + mNotes.size());

        return mNotes.size();
    }
}