package com.example.newhabitquest;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AllianceInvitationsFragment extends Fragment implements AllianceInvitationsAdapter.OnInvitationActionListener {

    private RecyclerView invitationsRecyclerView;
    private TextView noInvitationsText;
    private AllianceInvitationsAdapter invitationsAdapter;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String currentUserId;
    private AllianceInvitationManager invitationManager;

    private List<AllianceInvitation> pendingInvitations;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alliance_invitations, container, false);

        initViews(view);
        initFirebase();
        setupRecyclerView();
        loadPendingInvitations();

        return view;
    }

    private void initViews(View view) {
        invitationsRecyclerView = view.findViewById(R.id.invitations_recycler_view);
        noInvitationsText = view.findViewById(R.id.no_invitations_text);
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        invitationManager = new AllianceInvitationManager(getContext());
    }

    private void setupRecyclerView() {
        pendingInvitations = new ArrayList<>();
        invitationsAdapter = new AllianceInvitationsAdapter(pendingInvitations, this);
        invitationsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        invitationsRecyclerView.setAdapter(invitationsAdapter);
    }

    private void loadPendingInvitations() {
        if (currentUserId == null) return;

        android.util.Log.d("InvitationsFragment", "Loading pending invitations for user: " + currentUserId);

        db.collection("alliance_invitations")
            .whereEqualTo("receiverUserId", currentUserId)
            .whereEqualTo("status", AllianceInvitation.InvitationStatus.PENDING.toString())
            .addSnapshotListener((value, error) -> {
                if (error != null) {
                    android.util.Log.e("InvitationsFragment", "Error loading invitations", error);
                    return;
                }

                pendingInvitations.clear();
                if (value != null) {
                    android.util.Log.d("InvitationsFragment", "Found " + value.size() + " pending invitations");
                    for (QueryDocumentSnapshot document : value) {
                        AllianceInvitation invitation = documentToInvitation(document);
                        if (invitation != null) {
                            pendingInvitations.add(invitation);
                            android.util.Log.d("InvitationsFragment", "Added invitation: " + invitation.getAllianceName());
                        }
                    }
                } else {
                    android.util.Log.d("InvitationsFragment", "No pending invitations found");
                }

                invitationsAdapter.notifyDataSetChanged();
                updateUI();
            });
    }

    private AllianceInvitation documentToInvitation(QueryDocumentSnapshot document) {
        try {
            AllianceInvitation invitation = new AllianceInvitation();
            invitation.setInvitationId(document.getString("invitationId"));
            invitation.setAllianceId(document.getString("allianceId"));
            invitation.setAllianceName(document.getString("allianceName"));
            invitation.setSenderUserId(document.getString("senderUserId"));
            invitation.setSenderUsername(document.getString("senderUsername"));
            invitation.setReceiverUserId(document.getString("receiverUserId"));
            invitation.setMessage(document.getString("message"));

            Long timestamp = document.getLong("timestamp");
            invitation.setTimestamp(timestamp != null ? timestamp : 0);

            String status = document.getString("status");
            if (status != null) {
                invitation.setStatus(AllianceInvitation.InvitationStatus.valueOf(status));
            }

            return invitation;
        } catch (Exception e) {
            android.util.Log.e("InvitationsFragment", "Error parsing invitation document", e);
            return null;
        }
    }

    private void updateUI() {
        if (pendingInvitations.isEmpty()) {
            noInvitationsText.setVisibility(View.VISIBLE);
            invitationsRecyclerView.setVisibility(View.GONE);
            noInvitationsText.setText("Nemate pending poziva za saveze");
        } else {
            noInvitationsText.setVisibility(View.GONE);
            invitationsRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onAcceptInvitation(AllianceInvitation invitation) {
        invitationManager.acceptInvitation(invitation.getInvitationId(), currentUserId);
    }

    @Override
    public void onRejectInvitation(AllianceInvitation invitation) {
        invitationManager.rejectInvitation(invitation.getInvitationId(), currentUserId);
    }
}
