package com.example.newhabitquest;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AllianceInvitationsAdapter extends RecyclerView.Adapter<AllianceInvitationsAdapter.InvitationViewHolder> {

    private List<AllianceInvitation> invitations;
    private OnInvitationActionListener listener;

    public interface OnInvitationActionListener {
        void onAcceptInvitation(AllianceInvitation invitation);
        void onRejectInvitation(AllianceInvitation invitation);
    }

    public AllianceInvitationsAdapter(List<AllianceInvitation> invitations, OnInvitationActionListener listener) {
        this.invitations = invitations;
        this.listener = listener;
    }

    @NonNull
    @Override
    public InvitationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alliance_invitation, parent, false);
        return new InvitationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InvitationViewHolder holder, int position) {
        AllianceInvitation invitation = invitations.get(position);
        holder.bind(invitation);
    }

    @Override
    public int getItemCount() {
        return invitations.size();
    }

    class InvitationViewHolder extends RecyclerView.ViewHolder {
        private TextView allianceNameText;
        private TextView senderNameText;
        private TextView messageText;
        private TextView timestampText;
        private Button acceptButton;
        private Button rejectButton;

        public InvitationViewHolder(@NonNull View itemView) {
            super(itemView);
            allianceNameText = itemView.findViewById(R.id.alliance_name_text);
            senderNameText = itemView.findViewById(R.id.sender_name_text);
            messageText = itemView.findViewById(R.id.message_text);
            timestampText = itemView.findViewById(R.id.timestamp_text);
            acceptButton = itemView.findViewById(R.id.accept_button);
            rejectButton = itemView.findViewById(R.id.reject_button);
        }

        public void bind(AllianceInvitation invitation) {
            allianceNameText.setText("Savez: " + invitation.getAllianceName());
            senderNameText.setText("Od: " + invitation.getSenderUsername());
            messageText.setText(invitation.getMessage());

            // Format timestamp
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
            String formattedDate = dateFormat.format(new Date(invitation.getTimestamp()));
            timestampText.setText(formattedDate);

            acceptButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAcceptInvitation(invitation);
                }
            });

            rejectButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRejectInvitation(invitation);
                }
            });
        }
    }
}
