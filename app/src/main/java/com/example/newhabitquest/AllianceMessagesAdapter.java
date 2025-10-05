package com.example.newhabitquest;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AllianceMessagesAdapter extends RecyclerView.Adapter<AllianceMessagesAdapter.MessageViewHolder> {
    private List<AllianceMessage> messages;
    private String currentUserId;
    private SimpleDateFormat timeFormat;

    public AllianceMessagesAdapter(List<AllianceMessage> messages, String currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_alliance_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        AllianceMessage message = messages.get(position);
        holder.bind(message, currentUserId, timeFormat);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout messageContainerRight;
        private LinearLayout messageContainerLeft;
        private TextView messageTextRight;
        private TextView messageTimeRight;
        private TextView senderNameLeft;
        private TextView messageTextLeft;
        private TextView messageTimeLeft;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageContainerRight = itemView.findViewById(R.id.message_container_right);
            messageContainerLeft = itemView.findViewById(R.id.message_container_left);
            messageTextRight = itemView.findViewById(R.id.message_text_right);
            messageTimeRight = itemView.findViewById(R.id.message_time_right);
            senderNameLeft = itemView.findViewById(R.id.sender_name_left);
            messageTextLeft = itemView.findViewById(R.id.message_text_left);
            messageTimeLeft = itemView.findViewById(R.id.message_time_left);
        }

        public void bind(AllianceMessage message, String currentUserId, SimpleDateFormat timeFormat) {
            String timeText = timeFormat.format(new Date(message.getTimestamp()));

            if (message.getSenderUserId().equals(currentUserId)) {
                // Show as user's own message (right side)
                messageContainerRight.setVisibility(View.VISIBLE);
                messageContainerLeft.setVisibility(View.GONE);

                messageTextRight.setText(message.getMessageText());
                messageTimeRight.setText(timeText);
            } else {
                // Show as other user's message (left side)
                messageContainerRight.setVisibility(View.GONE);
                messageContainerLeft.setVisibility(View.VISIBLE);

                senderNameLeft.setText(message.getSenderUsername());
                messageTextLeft.setText(message.getMessageText());
                messageTimeLeft.setText(timeText);
            }
        }
    }
}
