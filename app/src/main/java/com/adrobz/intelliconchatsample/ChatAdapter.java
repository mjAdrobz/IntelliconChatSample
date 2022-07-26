package com.adrobz.intelliconchatsample;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.adrobz.intelliconlibrary.Model.ChatMessages;
import com.adrobz.intelliconlibrary.MyLibrary;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    ArrayList<ChatMessages> chatList;
    MyLibrary myLibrary;
    String userId;
    MediaPlayer mediaPlayer;


    public ChatAdapter(ArrayList<ChatMessages> chatList, String userId, MediaPlayer mediaPlayer, MyLibrary myLibrary) {
        this.chatList = chatList;
        this.myLibrary = myLibrary;
        this.userId = userId;
        this.mediaPlayer = mediaPlayer;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.chat_item_layout, viewGroup, false);
        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        mediaPlayer = new MediaPlayer();
        if (!chatList.get(viewHolder.getAdapterPosition()).getAuthor().equals(userId)) {
            viewHolder.adminMessage.setText(chatList.get(viewHolder.getAdapterPosition()).getText());
            viewHolder.adminMessage.setVisibility(View.VISIBLE);
            viewHolder.userMessage.setVisibility(View.GONE);
            viewHolder.ivBasicVideo.setVisibility(View.GONE);
            viewHolder.ivBasicImage.setVisibility(View.GONE);
            viewHolder.attachmentAudioLayout.setVisibility(View.GONE);
            viewHolder.document.setVisibility(View.GONE);
            if (!chatList.get(viewHolder.getAdapterPosition()).getOptions().isEmpty()) {
                viewHolder.optionRecyclerView.setVisibility(View.VISIBLE);
                viewHolder.optionRecyclerView.setAdapter(new OptionsAdapter(chatList.get(viewHolder.getAdapterPosition()).getOptions(), chatList, chatList.get(viewHolder.getAdapterPosition()).getCid(), myLibrary));
            } else {
                viewHolder.optionRecyclerView.setVisibility(View.GONE);
            }
        } else if (chatList.get(viewHolder.getAdapterPosition()).getAttachment() != null) {

            if (chatList.get(viewHolder.getAdapterPosition()).getAttachment().type.equals("image")) {
                viewHolder.userMessage.setVisibility(View.GONE);
                viewHolder.adminMessage.setVisibility(View.GONE);
                viewHolder.ivBasicVideo.setVisibility(View.GONE);
                viewHolder.attachmentAudioLayout.setVisibility(View.GONE);
                viewHolder.document.setVisibility(View.GONE);
                Picasso.get().load(chatList.get(viewHolder.getAdapterPosition()).getAttachment().payload.url).into(viewHolder.ivBasicImage);
                viewHolder.ivBasicImage.setVisibility(View.VISIBLE);
            } else if (chatList.get(viewHolder.getAdapterPosition()).getAttachment().type.equals("video")) {
                viewHolder.userMessage.setVisibility(View.GONE);
                viewHolder.adminMessage.setVisibility(View.GONE);
                viewHolder.ivBasicImage.setVisibility(View.GONE);
                viewHolder.attachmentAudioLayout.setVisibility(View.GONE);
                viewHolder.document.setVisibility(View.GONE);
                viewHolder.ivBasicVideo.setVideoURI(Uri.parse(chatList.get(viewHolder.getAdapterPosition()).getAttachment().payload.url));
                viewHolder.ivBasicVideo.setVisibility(View.VISIBLE);
                viewHolder.ivBasicVideo.requestFocus();
                //viewHolder.ivBasicVideo.start();
            }
            else if (chatList.get(viewHolder.getAdapterPosition()).getAttachment().type.equals("document")) {
                viewHolder.userMessage.setVisibility(View.GONE);
                viewHolder.adminMessage.setVisibility(View.GONE);
                viewHolder.ivBasicImage.setVisibility(View.GONE);
                viewHolder.attachmentAudioLayout.setVisibility(View.GONE);
               viewHolder.ivBasicVideo.setVisibility(View.GONE);
                viewHolder.document.setVisibility(View.VISIBLE);
            }else {
                viewHolder.userMessage.setVisibility(View.GONE);
                viewHolder.adminMessage.setVisibility(View.GONE);
                viewHolder.ivBasicVideo.setVisibility(View.GONE);
                viewHolder.ivBasicImage.setVisibility(View.GONE);
                viewHolder.document.setVisibility(View.GONE);
                viewHolder.attachmentAudioLayout.setVisibility(View.VISIBLE);
                viewHolder.ivBasicAudio.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(mediaPlayer.isPlaying()){
                            viewHolder.ivBasicAudio.setBackgroundResource(R.drawable.play_arrow_24);
                            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mp) {
                                    viewHolder.ivBasicAudio.setBackgroundResource(R.drawable.play_arrow_24);
                                }
                            });
                            mediaPlayer.stop();
                        }
                        else{
                            mediaPlayer = new MediaPlayer();
                            try {
                                viewHolder.ivBasicAudio.setBackgroundResource(R.drawable.stop_24);
                                mediaPlayer.setDataSource(chatList.get(viewHolder.getAdapterPosition()).getAttachment().payload.url);
                                mediaPlayer.prepare();
                                mediaPlayer.start();

                                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                    @Override
                                    public void onCompletion(MediaPlayer mp) {
                                        viewHolder.ivBasicAudio.setBackgroundResource(R.drawable.play_arrow_24);
                                        mp.stop();
                                    }
                                });

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }

        } else {
            viewHolder.userMessage.setText(chatList.get(viewHolder.getAdapterPosition()).getText());
            viewHolder.adminMessage.setVisibility(View.GONE);
            viewHolder.ivBasicImage.setVisibility(View.GONE);
            viewHolder.ivBasicVideo.setVisibility(View.GONE);
            viewHolder.attachmentAudioLayout.setVisibility(View.GONE);
            viewHolder.userMessage.setVisibility(View.VISIBLE);


        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return chatList.isEmpty() ? 0 : chatList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayoutManager optionsAdapterLayoutManager;
        TextView adminMessage;
        TextView userMessage;
        RecyclerView optionRecyclerView;
        ImageView ivBasicImage;
        VideoView ivBasicVideo;
        ImageButton ivBasicAudio;
        TextView audio;
        ImageView document;
        LinearLayout attachmentAudioLayout;


        public ViewHolder(View view) {
            super(view);

            adminMessage = view.findViewById(R.id.admin_message);
            userMessage = view.findViewById(R.id.user_message);
            ivBasicImage = view.findViewById(R.id.attachmentImage);
            ivBasicVideo = view.findViewById(R.id.attachmentVideo);
            optionRecyclerView = view.findViewById(R.id.option_recyclerview);
            attachmentAudioLayout = view.findViewById(R.id.attachmentAudioLayout);
            audio = view.findViewById(R.id.attachmentAudio);
            document = view.findViewById(R.id.document);
            ivBasicAudio = view.findViewById(R.id.mic_btn);
            ivBasicAudio.setBackgroundResource(R.drawable.play_arrow_24);
            MediaController mediaController = new MediaController(view.getContext());
            mediaController.setAnchorView(ivBasicVideo);
            ivBasicVideo.setMediaController(mediaController);
            optionsAdapterLayoutManager = new LinearLayoutManager(view.getContext(), LinearLayoutManager.HORIZONTAL, false);
            optionRecyclerView.setLayoutManager(optionsAdapterLayoutManager);

        }

    }
}
