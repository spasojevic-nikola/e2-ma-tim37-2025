package com.example.newhabitquest;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlliancesFragment extends Fragment implements AlliancesAdapter.OnAllianceClickListener {

    private RecyclerView myAllianceRecyclerView;
    private RecyclerView availableAlliancesRecyclerView;
    private Button createAllianceButton;
    private TextView myAllianceTitle;
    private TextView availableAlliancesTitle;

    private AlliancesAdapter myAllianceAdapter;
    private AlliancesAdapter availableAlliancesAdapter;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String currentUserId;
    private AllianceInvitationManager invitationManager;

    private List<Alliance> myAlliancesList;
    private List<Alliance> availableAlliancesList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alliances, container, false);

        initViews(view);
        initFirebase();
        setupRecyclerViews();
        setupClickListeners();
        loadAlliances();

        return view;
    }

    private void initViews(View view) {
        myAllianceRecyclerView = view.findViewById(R.id.my_alliance_recycler_view);
        availableAlliancesRecyclerView = view.findViewById(R.id.available_alliances_recycler_view);
        createAllianceButton = view.findViewById(R.id.create_alliance_button);
        myAllianceTitle = view.findViewById(R.id.my_alliance_title);
        availableAlliancesTitle = view.findViewById(R.id.available_alliances_title);
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        invitationManager = new AllianceInvitationManager(getContext());
    }

    private void setupRecyclerViews() {
        myAlliancesList = new ArrayList<>();
        availableAlliancesList = new ArrayList<>();

        myAllianceAdapter = new AlliancesAdapter(myAlliancesList, this, true);
        myAllianceRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        myAllianceRecyclerView.setAdapter(myAllianceAdapter);

        availableAlliancesAdapter = new AlliancesAdapter(availableAlliancesList, this, false);
        availableAlliancesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        availableAlliancesRecyclerView.setAdapter(availableAlliancesAdapter);
    }

    private void setupClickListeners() {
        createAllianceButton.setOnClickListener(v -> showCreateAllianceDialog());
    }

    private void showCreateAllianceDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_create_alliance, null);
        EditText nameEditText = dialogView.findViewById(R.id.alliance_name_edit_text);
        EditText descriptionEditText = dialogView.findViewById(R.id.alliance_description_edit_text);

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle("Kreiranje saveza")
                .setView(dialogView)
                .setPositiveButton("Kreiraj", null)
                .setNegativeButton("Otkaži", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(view -> {
                String name = nameEditText.getText().toString().trim();
                String description = descriptionEditText.getText().toString().trim();

                if (name.isEmpty()) {
                    nameEditText.setError("Unesite ime saveza");
                    return;
                }

                if (description.isEmpty()) {
                    descriptionEditText.setError("Unesite opis saveza");
                    return;
                }

                createAlliance(name, description);
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private void createAlliance(String name, String description) {
        if (currentUserId == null) return;

        String allianceId = db.collection("alliances").document().getId();
        Alliance alliance = new Alliance(allianceId, name, description, currentUserId);

        Map<String, Object> allianceData = new HashMap<>();
        allianceData.put("allianceId", alliance.getAllianceId());
        allianceData.put("name", alliance.getName());
        allianceData.put("description", alliance.getDescription());
        allianceData.put("leaderId", alliance.getLeaderId());
        allianceData.put("memberIds", alliance.getMemberIds());
        allianceData.put("currentTask", alliance.getCurrentTask());
        allianceData.put("maxMembers", alliance.getMaxMembers());
        allianceData.put("createdTimestamp", alliance.getCreatedTimestamp());
        allianceData.put("isActive", alliance.isActive());
        allianceData.put("totalPoints", alliance.getTotalPoints());

        db.collection("alliances").document(allianceId)
                .set(allianceData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Savez kreiran uspešno!", Toast.LENGTH_SHORT).show();
                    loadAlliances();
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("AlliancesFragment", "Error creating alliance", e);
                    Toast.makeText(getContext(), "Greška pri kreiranju saveza", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadAlliances() {
        if (currentUserId == null) return;

        // Load user's alliances
        db.collection("alliances")
                .whereArrayContains("memberIds", currentUserId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        android.util.Log.e("AlliancesFragment", "Error loading user alliances", error);
                        return;
                    }

                    myAlliancesList.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot document : value) {
                            Alliance alliance = documentToAlliance(document);
                            if (alliance != null) {
                                myAlliancesList.add(alliance);
                            }
                        }
                    }
                    myAllianceAdapter.notifyDataSetChanged();
                    updateUI();
                });

        // Load available alliances
        db.collection("alliances")
                .whereEqualTo("isActive", true)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        android.util.Log.e("AlliancesFragment", "Error loading available alliances", error);
                        return;
                    }

                    availableAlliancesList.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot document : value) {
                            Alliance alliance = documentToAlliance(document);
                            if (alliance != null &&
                                !alliance.getMemberIds().contains(currentUserId) &&
                                alliance.canJoin()) {
                                availableAlliancesList.add(alliance);
                            }
                        }
                    }
                    availableAlliancesAdapter.notifyDataSetChanged();
                });
    }

    private Alliance documentToAlliance(QueryDocumentSnapshot document) {
        try {
            Alliance alliance = new Alliance();
            alliance.setAllianceId(document.getString("allianceId"));
            alliance.setName(document.getString("name"));
            alliance.setDescription(document.getString("description"));
            alliance.setLeaderId(document.getString("leaderId"));

            List<String> memberIds = (List<String>) document.get("memberIds");
            if (memberIds != null) {
                alliance.setMemberIds(memberIds);
            }

            alliance.setCurrentTask(document.getString("currentTask"));

            Long maxMembers = document.getLong("maxMembers");
            alliance.setMaxMembers(maxMembers != null ? maxMembers.intValue() : 5);

            Long createdTimestamp = document.getLong("createdTimestamp");
            alliance.setCreatedTimestamp(createdTimestamp != null ? createdTimestamp : 0);

            Boolean isActive = document.getBoolean("isActive");
            alliance.setActive(isActive != null ? isActive : true);

            Long totalPoints = document.getLong("totalPoints");
            alliance.setTotalPoints(totalPoints != null ? totalPoints.intValue() : 0);

            Boolean hasMissionActive = document.getBoolean("hasMissionActive");
            alliance.setHasMissionActive(hasMissionActive != null ? hasMissionActive : false);

            return alliance;
        } catch (Exception e) {
            android.util.Log.e("AlliancesFragment", "Error parsing alliance document", e);
            return null;
        }
    }
    private void updateUI() {
        if (myAlliancesList.isEmpty()) {
            myAllianceTitle.setText("Niste član nijednog saveza");
            createAllianceButton.setVisibility(View.VISIBLE);
        } else {
            myAllianceTitle.setText("Vaši savezi (" + myAlliancesList.size() + ")");
            createAllianceButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void onAllianceClick(Alliance alliance) {
        if (alliance.getLeaderId().equals(currentUserId)) {
            showAllianceLeaderOptions(alliance);
        } else {
            showAllianceDetails(alliance);
        }
    }

    private void showAllianceLeaderOptions(Alliance alliance) {
        String[] options = {"Pozovi prijatelje", "Detalji saveza", "Chat saveza", "Ukinuti savez"};

        new AlertDialog.Builder(getContext())
            .setTitle("Opcije vođe saveza")
            .setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0:
                        showInviteFriendsDialog(alliance);
                        break;
                    case 1:
                        showAllianceDetails(alliance);
                        break;
                    case 2:
                        openAllianceChat(alliance);
                        break;
                    case 3:
                        disbandAlliance(alliance);
                        break;
                }
            })
            .show();
    }

    private void showInviteFriendsDialog(Alliance alliance) {
        // Load user's friends
        loadFriendsForInvitation(alliance);
    }

    private void loadFriendsForInvitation(Alliance alliance) {
        android.util.Log.d("AlliancesFragment", "Loading friends for invitation. Current user ID: " + currentUserId);

        db.collection("friendships")
            .whereEqualTo("userId", currentUserId)
            .whereEqualTo("status", "accepted")
            .get()
            .addOnSuccessListener(querySnapshot -> {
                android.util.Log.d("AlliancesFragment", "Found " + querySnapshot.size() + " friendships where user is userId");

                List<String> friendIds = new ArrayList<>();
                for (QueryDocumentSnapshot doc : querySnapshot) {
                    String friendId = doc.getString("friendId");
                    android.util.Log.d("AlliancesFragment", "Friend ID from friendId: " + friendId);
                    if (friendId != null) {
                        friendIds.add(friendId);
                    }
                }

                // Also check reverse friendships
                db.collection("friendships")
                    .whereEqualTo("friendId", currentUserId)
                    .whereEqualTo("status", "accepted")
                    .get()
                    .addOnSuccessListener(reverseSnapshot -> {
                        android.util.Log.d("AlliancesFragment", "Found " + reverseSnapshot.size() + " friendships where user is friendId");

                        for (QueryDocumentSnapshot doc : reverseSnapshot) {
                            String friendId = doc.getString("userId");
                            android.util.Log.d("AlliancesFragment", "Friend ID from userId: " + friendId);
                            if (friendId != null && !friendIds.contains(friendId)) {
                                friendIds.add(friendId);
                            }
                        }

                        android.util.Log.d("AlliancesFragment", "Total unique friend IDs found: " + friendIds.size());

                        if (friendIds.isEmpty()) {
                            Toast.makeText(getContext(), "Nemate prijatelje za pozivanje", Toast.LENGTH_SHORT).show();
                        } else {
                            loadFriendsDetails(alliance, friendIds);
                        }
                    })
                    .addOnFailureListener(e -> {
                        android.util.Log.e("AlliancesFragment", "Error loading reverse friendships", e);
                        Toast.makeText(getContext(), "Greška pri učitavanju prijatelja", Toast.LENGTH_SHORT).show();
                    });
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("AlliancesFragment", "Error loading friendships", e);
                Toast.makeText(getContext(), "Greška pri učitavanju prijatelja", Toast.LENGTH_SHORT).show();
            });
    }

    private void loadFriendsDetails(Alliance alliance, List<String> friendIds) {
        List<Friend> friends = new ArrayList<>();
        int[] loadedCount = {0};
        int totalFriends = friendIds.size();

        if (totalFriends == 0) {
            Toast.makeText(getContext(), "Nemate prijatelje za pozivanje", Toast.LENGTH_SHORT).show();
            return;
        }

        for (String friendId : friendIds) {
            db.collection("users").document(friendId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        // Only add friend if they're not already a member of this specific alliance
                        if (!alliance.getMemberIds().contains(friendId)) {
                            Friend friend = new Friend();
                            friend.setUserId(friendId);
                            friend.setUsername(document.getString("username"));
                            friends.add(friend);
                        }
                    }

                    loadedCount[0]++;
                    if (loadedCount[0] == totalFriends) {
                        if (friends.isEmpty()) {
                            Toast.makeText(getContext(), "Svi vaši prijatelji su već članovi ovog saveza", Toast.LENGTH_SHORT).show();
                        } else {
                            showFriendSelectionDialog(alliance, friends);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    loadedCount[0]++;
                    if (loadedCount[0] == totalFriends) {
                        if (friends.isEmpty()) {
                            Toast.makeText(getContext(), "Svi vaši prijatelji su već članovi ovog saveza", Toast.LENGTH_SHORT).show();
                        } else {
                            showFriendSelectionDialog(alliance, friends);
                        }
                    }
                });
        }
    }

    private void showFriendSelectionDialog(Alliance alliance, List<Friend> friends) {
        if (friends.isEmpty()) {
            Toast.makeText(getContext(), "Nemate dostupnih prijatelja za pozivanje", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] friendNames = new String[friends.size()];
        boolean[] selectedFriends = new boolean[friends.size()];

        for (int i = 0; i < friends.size(); i++) {
            friendNames[i] = friends.get(i).getUsername();
        }

        new AlertDialog.Builder(getContext())
            .setTitle("Pozovi prijatelje u savez")
            .setMultiChoiceItems(friendNames, selectedFriends, (dialog, which, isChecked) -> {
                selectedFriends[which] = isChecked;
            })
            .setPositiveButton("Pošalji pozive", (dialog, which) -> {
                List<Friend> selectedFriendsList = new ArrayList<>();
                for (int i = 0; i < selectedFriends.length; i++) {
                    if (selectedFriends[i]) {
                        selectedFriendsList.add(friends.get(i));
                    }
                }

                if (!selectedFriendsList.isEmpty()) {
                    showInvitationMessageDialog(alliance, selectedFriendsList);
                }
            })
            .setNegativeButton("Otkaži", null)
            .show();
    }

    private void showInvitationMessageDialog(Alliance alliance, List<Friend> selectedFriends) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_invitation_message, null);
        EditText messageEditText = dialogView.findViewById(R.id.invitation_message_edit_text);

        AlertDialog dialog = new AlertDialog.Builder(getContext())
            .setTitle("Poruka uz poziv")
            .setView(dialogView)
            .setPositiveButton("Pošalji", null)
            .setNegativeButton("Otkaži", null)
            .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(view -> {
                String message = messageEditText.getText().toString().trim();
                if (message.isEmpty()) {
                    message = "Pridruži se mom savezu!";
                }

                sendInvitationsToFriends(alliance, selectedFriends, message);
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private void sendInvitationsToFriends(Alliance alliance, List<Friend> selectedFriends, String message) {
        // Get current user's username first
        db.collection("users").document(currentUserId)
            .get()
            .addOnSuccessListener(document -> {
                String senderUsername = document.exists() ?
                    document.getString("username") : "Nepoznat korisnik";

                for (Friend friend : selectedFriends) {
                    invitationManager.sendInvitation(
                        alliance.getAllianceId(),
                        alliance.getName(),
                        friend.getUserId(),
                        senderUsername,
                        message
                    );
                }

                Toast.makeText(getContext(),
                    "Poslato " + selectedFriends.size() + " poziva",
                    Toast.LENGTH_SHORT).show();
            });
    }

    private void showAllianceDetails(Alliance alliance) {
        // Show alliance member options including chat
        String[] options = {"Chat saveza", "Detalji saveza", "Napusti savez"};

        new AlertDialog.Builder(getContext())
            .setTitle("Opcije saveza")
            .setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0:
                        openAllianceChat(alliance);
                        break;
                    case 1:
                        showAllianceDetailsDialog(alliance);
                        break;
                    case 2:
                        leaveAlliance(alliance);
                        break;
                }
            })
            .show();
    }

    private void showAllianceDetailsDialog(Alliance alliance) {
        String details = "Savez: " + alliance.getName() + "\n\n" +
                        "Opis: " + alliance.getDescription() + "\n\n" +
                        "Članovi: " + alliance.getMemberIds().size() + "/" + alliance.getMaxMembers() + "\n\n" +
                        "Ukupni poeni: " + alliance.getTotalPoints();

        new AlertDialog.Builder(getContext())
            .setTitle("Detalji saveza")
            .setMessage(details)
            .setPositiveButton("OK", null)
            .show();
    }

    private void openAllianceChat(Alliance alliance) {
        android.content.Intent chatIntent = new android.content.Intent(getContext(), AllianceChatActivity.class);
        chatIntent.putExtra("allianceId", alliance.getAllianceId());
        chatIntent.putExtra("allianceName", alliance.getName());
        startActivity(chatIntent);
    }

    private void disbandAlliance(Alliance alliance) {
        if (!alliance.canDisband()) {
            Toast.makeText(getContext(), "Ne možete ukinuti savez tokom aktivne misije", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(getContext())
            .setTitle("Ukinuti savez")
            .setMessage("Da li ste sigurni da želite da ukinete savez \"" + alliance.getName() + "\"?")
            .setPositiveButton("Da", (dialog, which) -> {
                // Completely delete the alliance document instead of just setting isActive to false
                db.collection("alliances").document(alliance.getAllianceId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Savez je uspešno obrisan", Toast.LENGTH_SHORT).show();
                        // Refresh the alliances list to reflect the deletion
                        loadAlliances();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Greška pri brisanju saveza", Toast.LENGTH_SHORT).show();
                    });
            })
            .setNegativeButton("Ne", null)
            .show();
    }

    @Override
    public void onJoinAllianceClick(Alliance alliance) {
        joinAlliance(alliance);
    }

    @Override
    public void onLeaveAllianceClick(Alliance alliance) {
        leaveAlliance(alliance);
    }

    private void joinAlliance(Alliance alliance) {
        if (currentUserId == null || !alliance.canJoin()) return;

        List<String> updatedMembers = new ArrayList<>(alliance.getMemberIds());
        updatedMembers.add(currentUserId);

        db.collection("alliances").document(alliance.getAllianceId())
                .update("memberIds", updatedMembers)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Uspešno ste pristupili savezu!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("AlliancesFragment", "Error joining alliance", e);
                    Toast.makeText(getContext(), "Greška pri pristupanju savezu", Toast.LENGTH_SHORT).show();
                });
    }

    private void leaveAlliance(Alliance alliance) {
        if (currentUserId == null) return;

        // Check if user can leave (no active missions)
        if (!alliance.canLeave()) {
            Toast.makeText(getContext(), "Ne možete napustiti savez tokom aktivne misije", Toast.LENGTH_SHORT).show();
            return;
        }

        // Leader cannot leave alliance - must disband it instead
        if (alliance.getLeaderId().equals(currentUserId)) {
            Toast.makeText(getContext(), "Kao vođa ne možete napustiti savez. Možete ga ukinuti.", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(getContext())
            .setTitle("Napusti savez")
            .setMessage("Da li ste sigurni da želite da napustite savez \"" + alliance.getName() + "\"?")
            .setPositiveButton("Da", (dialog, which) -> {
                List<String> updatedMembers = new ArrayList<>(alliance.getMemberIds());
                updatedMembers.remove(currentUserId);

                db.collection("alliances").document(alliance.getAllianceId())
                    .update("memberIds", updatedMembers)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Napustili ste savez", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        android.util.Log.e("AlliancesFragment", "Error leaving alliance", e);
                        Toast.makeText(getContext(), "Greška pri napuštanju saveza", Toast.LENGTH_SHORT).show();
                    });
            })
            .setNegativeButton("Ne", null)
            .show();
    }
}
