package com.example.newhabitquest;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendViewHolder> {

    public interface OnFriendClickListener {
        void onFriendClick(Friend friend);
        void onRemoveFriendClick(Friend friend);
        void onViewProfileClick(Friend friend);
    }

    private List<Friend> friends;
    private OnFriendClickListener listener;
    private Context context;

    public FriendsAdapter(List<Friend> friends, OnFriendClickListener listener) {
        this.friends = friends;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_friend, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        Friend friend = friends.get(position);

        holder.usernameText.setText(friend.getUsername());
        holder.levelText.setText("Level " + friend.getLevel());

        // Set avatar
        if (friend.getAvatar() != null && !friend.getAvatar().isEmpty()) {
            int resId = context.getResources().getIdentifier(friend.getAvatar(), "drawable", context.getPackageName());
            if (resId != 0) {
                holder.avatarImage.setImageResource(resId);
            } else {
                holder.avatarImage.setImageResource(R.drawable.ic_person);
            }
        } else {
            holder.avatarImage.setImageResource(R.drawable.ic_person);
        }

        // Set status text
        String statusText = "";
        switch (friend.getStatus()) {
            case "pending":
                statusText = "Zahtev poslat";
                holder.removeFriendBtn.setText("Otkaži");
                break;
            case "accepted":
                statusText = "Prijatelj";
                holder.removeFriendBtn.setText("Ukloni");
                break;
            case "blocked":
                statusText = "Blokiran";
                holder.removeFriendBtn.setText("Odblokiraj");
                break;
        }
        holder.statusText.setText(statusText);

        // Set click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFriendClick(friend);
            }
        });

        holder.viewProfileBtn.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewProfileClick(friend);
            }
        });

        holder.removeFriendBtn.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemoveFriendClick(friend);
            }
        });
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    public void updateFriends(List<Friend> newFriends) {
        this.friends = newFriends;
        notifyDataSetChanged();
    }

    static class FriendViewHolder extends RecyclerView.ViewHolder {
        ImageView avatarImage;
        TextView usernameText;
        TextView levelText;
        TextView statusText;
        Button viewProfileBtn;
        Button removeFriendBtn;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarImage = itemView.findViewById(R.id.friend_avatar);
            usernameText = itemView.findViewById(R.id.friend_username);
            levelText = itemView.findViewById(R.id.friend_level);
            statusText = itemView.findViewById(R.id.friend_status);
            viewProfileBtn = itemView.findViewById(R.id.view_profile_btn);
            removeFriendBtn = itemView.findViewById(R.id.remove_friend_btn);
        }
    }
}
