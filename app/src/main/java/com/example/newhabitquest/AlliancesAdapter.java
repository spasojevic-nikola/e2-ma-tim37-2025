package com.example.newhabitquest;

import android.content.Context;
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

public class AlliancesAdapter extends RecyclerView.Adapter<AlliancesAdapter.AllianceViewHolder> {

    public interface OnAllianceClickListener {
        void onAllianceClick(Alliance alliance);
        void onJoinAllianceClick(Alliance alliance);
        void onLeaveAllianceClick(Alliance alliance);
    }

    private List<Alliance> alliances;
    private OnAllianceClickListener listener;
    private boolean isMyAlliances;
    private Context context;

    public AlliancesAdapter(List<Alliance> alliances, OnAllianceClickListener listener, boolean isMyAlliances) {
        this.alliances = alliances;
        this.listener = listener;
        this.isMyAlliances = isMyAlliances;
    }

    @NonNull
    @Override
    public AllianceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_alliance, parent, false);
        return new AllianceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AllianceViewHolder holder, int position) {
        Alliance alliance = alliances.get(position);

        holder.allianceNameText.setText(alliance.getName());
        holder.allianceDescriptionText.setText(alliance.getDescription());
        holder.membersCountText.setText(alliance.getMemberIds().size() + "/" + alliance.getMaxMembers() + " članova");
        holder.totalPointsText.setText("Ukupno poena: " + alliance.getTotalPoints());

        // Format created date
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        String createdDate = dateFormat.format(new Date(alliance.getCreatedTimestamp()));
        holder.createdDateText.setText("Kreiran: " + createdDate);

        // Show current task if exists
        if (alliance.getCurrentTask() != null && !alliance.getCurrentTask().isEmpty()) {
            holder.currentTaskText.setVisibility(View.VISIBLE);
            holder.currentTaskText.setText("Trenutni zadatak: " + alliance.getCurrentTask());
        } else {
            holder.currentTaskText.setVisibility(View.GONE);
        }

        // Configure action button based on context
        if (isMyAlliances) {
            holder.actionButton.setText("Napusti");
            holder.actionButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onLeaveAllianceClick(alliance);
                }
            });
        } else {
            if (alliance.canJoin()) {
                holder.actionButton.setText("Pridruži se");
                holder.actionButton.setEnabled(true);
            } else {
                holder.actionButton.setText("Pun");
                holder.actionButton.setEnabled(false);
            }

            holder.actionButton.setOnClickListener(v -> {
                if (listener != null && alliance.canJoin()) {
                    listener.onJoinAllianceClick(alliance);
                }
            });
        }

        // Set click listener for the whole item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAllianceClick(alliance);
            }
        });

        // Show leader indicator
        // TODO: Add logic to show if current user is leader
    }

    @Override
    public int getItemCount() {
        return alliances.size();
    }

    static class AllianceViewHolder extends RecyclerView.ViewHolder {
        TextView allianceNameText;
        TextView allianceDescriptionText;
        TextView membersCountText;
        TextView totalPointsText;
        TextView createdDateText;
        TextView currentTaskText;
        Button actionButton;

        public AllianceViewHolder(@NonNull View itemView) {
            super(itemView);
            allianceNameText = itemView.findViewById(R.id.alliance_name_text);
            allianceDescriptionText = itemView.findViewById(R.id.alliance_description_text);
            membersCountText = itemView.findViewById(R.id.members_count_text);
            totalPointsText = itemView.findViewById(R.id.total_points_text);
            createdDateText = itemView.findViewById(R.id.created_date_text);
            currentTaskText = itemView.findViewById(R.id.current_task_text);
            actionButton = itemView.findViewById(R.id.alliance_action_button);
        }
    }
}
