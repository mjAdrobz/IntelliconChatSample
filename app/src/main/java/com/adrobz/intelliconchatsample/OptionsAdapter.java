package com.adrobz.intelliconchatsample;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.recyclerview.widget.RecyclerView;

import com.adrobz.intelliconlibrary.Model.ChatMessages;
import com.adrobz.intelliconlibrary.MyLibrary;

import java.util.ArrayList;

public class OptionsAdapter extends RecyclerView.Adapter<OptionsAdapter.ViewHolder> {
    ArrayList<String> options;
    ArrayList<ChatMessages> chatMessages;
    MyLibrary myLibrary;
    String cId;

    public OptionsAdapter(ArrayList<String> options, ArrayList<ChatMessages> chatMessages, String cId, MyLibrary myLibrary) {
        this.options = options;
        this.chatMessages = chatMessages;
        this.cId = cId;
        this.myLibrary = myLibrary;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.option_item_layout, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        viewHolder.optionBtn.setText(options.get(position));
        viewHolder.optionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myLibrary.sendMessage(viewHolder.optionBtn.getText().toString(), cId);
            }
        });
    }
    @Override
    public int getItemCount() {
        return options.isEmpty() ? 0 : options.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        Button optionBtn;


        public ViewHolder(View view) {
            super(view);
            optionBtn = view.findViewById(R.id.option_btn);
        }

    }
}
