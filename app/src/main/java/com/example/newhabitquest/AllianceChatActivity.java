package com.example.newhabitquest;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AllianceChatActivity extends AppCompatActivity {

    private RecyclerView messagesRecyclerView;
    private EditText messageInput;
    private ImageButton sendButton;
    private ImageButton backButton;
    private TextView allianceNameText;

    private AllianceMessagesAdapter messagesAdapter;
    private List<AllianceMessage> messagesList;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String currentUserId;
    private String currentUsername;
    private String allianceId;
    private String allianceName;

    private ListenerRegistration messagesListener;
    private AllianceNotificationManager notificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alliance_chat);

        // Get alliance details from intent
        allianceId = getIntent().getStringExtra("allianceId");
        allianceName = getIntent().getStringExtra("allianceName");

        if (allianceId == null || allianceName == null) {
            Toast.makeText(this, "Greška: Podaci o savezu nisu dostupni", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        initFirebase();
        setupRecyclerView();
        setupClickListeners();
        loadMessages();
        getCurrentUserInfo();
    }

    private void initViews() {
        messagesRecyclerView = findViewById(R.id.messages_recycler_view);
        messageInput = findViewById(R.id.message_input);
        sendButton = findViewById(R.id.send_button);
        backButton = findViewById(R.id.back_button);
        allianceNameText = findViewById(R.id.alliance_name_text);

        allianceNameText.setText(allianceName + " - Chat");
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        notificationManager = new AllianceNotificationManager(this);

        if (currentUserId == null) {
            Toast.makeText(this, "Korisnik nije ulogovan", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupRecyclerView() {
        messagesList = new ArrayList<>();
        messagesAdapter = new AllianceMessagesAdapter(messagesList, currentUserId);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Show latest messages at bottom
        messagesRecyclerView.setLayoutManager(layoutManager);
        messagesRecyclerView.setAdapter(messagesAdapter);
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());

        sendButton.setOnClickListener(v -> sendMessage());

        // Send message on Enter key
        messageInput.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });
    }

    private void getCurrentUserInfo() {
        if (currentUserId == null) return;

        db.collection("users").document(currentUserId)
            .get()
            .addOnSuccessListener(document -> {
                if (document.exists()) {
                    currentUsername = document.getString("username");
                    if (currentUsername == null) {
                        currentUsername = "Nepoznat korisnik";
                    }
                }
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("AllianceChatActivity", "Error getting user info", e);
                currentUsername = "Nepoznat korisnik";
            });
    }

    private void loadMessages() {
        if (allianceId == null) {
            android.util.Log.w("AllianceChatActivity", "Cannot load messages - allianceId is null");
            return;
        }

        try {
            messagesListener = db.collection("alliance_messages")
                .whereEqualTo("allianceId", allianceId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        android.util.Log.e("AllianceChatActivity", "Error loading messages: " + error.getMessage(), error);
                        // If there's an indexing error, try without ordering
                        loadMessagesWithoutOrder();
                        return;
                    }

                    if (value != null && !value.isEmpty()) {
                        try {
                            // Clear existing messages and reload all to avoid duplicates
                            messagesList.clear();

                            for (QueryDocumentSnapshot document : value) {
                                AllianceMessage message = documentToMessage(document);
                                if (message != null) {
                                    messagesList.add(message);
                                }
                            }

                            // Sort messages by timestamp manually if needed
                            messagesList.sort((m1, m2) -> Long.compare(m1.getTimestamp(), m2.getTimestamp()));

                            // Notify adapter and scroll to bottom
                            messagesAdapter.notifyDataSetChanged();
                            if (!messagesList.isEmpty()) {
                                messagesRecyclerView.scrollToPosition(messagesList.size() - 1);
                            }
                        } catch (Exception e) {
                            android.util.Log.e("AllianceChatActivity", "Error processing messages", e);
                        }
                    }
                });
        } catch (Exception e) {
            android.util.Log.e("AllianceChatActivity", "Exception setting up message listener", e);
            // Fallback to loading without order
            loadMessagesWithoutOrder();
        }
    }

    private void loadMessagesWithoutOrder() {
        try {
            messagesListener = db.collection("alliance_messages")
                .whereEqualTo("allianceId", allianceId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        android.util.Log.e("AllianceChatActivity", "Error loading messages without order", error);
                        Toast.makeText(this, "Greška pri učitavanju poruka", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        try {
                            messagesList.clear();

                            for (QueryDocumentSnapshot document : value) {
                                AllianceMessage message = documentToMessage(document);
                                if (message != null) {
                                    messagesList.add(message);
                                }
                            }

                            // Sort manually by timestamp
                            messagesList.sort((m1, m2) -> Long.compare(m1.getTimestamp(), m2.getTimestamp()));

                            messagesAdapter.notifyDataSetChanged();
                            if (!messagesList.isEmpty()) {
                                messagesRecyclerView.scrollToPosition(messagesList.size() - 1);
                            }
                        } catch (Exception e) {
                            android.util.Log.e("AllianceChatActivity", "Error processing messages in fallback", e);
                        }
                    }
                });
        } catch (Exception e) {
            android.util.Log.e("AllianceChatActivity", "Exception in fallback message loading", e);
        }
    }

    private AllianceMessage documentToMessage(QueryDocumentSnapshot document) {
        try {
            AllianceMessage message = new AllianceMessage();
            message.setMessageId(document.getId());
            message.setAllianceId(document.getString("allianceId"));
            message.setSenderUserId(document.getString("senderUserId"));
            message.setSenderUsername(document.getString("senderUsername"));
            message.setMessageText(document.getString("messageText"));

            Long timestamp = document.getLong("timestamp");
            message.setTimestamp(timestamp != null ? timestamp : 0);

            return message;
        } catch (Exception e) {
            android.util.Log.e("AllianceChatActivity", "Error parsing message document", e);
            return null;
        }
    }

    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();

        if (TextUtils.isEmpty(messageText)) {
            return;
        }

        if (currentUsername == null) {
            Toast.makeText(this, "Učitavanje korisničkih podataka...", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUserId == null || allianceId == null) {
            Toast.makeText(this, "Greška: Nedostaju podaci korisnika ili saveza", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Create message
            String messageId = db.collection("alliance_messages").document().getId();
            AllianceMessage message = new AllianceMessage(messageId, allianceId, currentUserId, currentUsername, messageText);

            // Save message to Firestore
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("messageId", message.getMessageId());
            messageData.put("allianceId", message.getAllianceId());
            messageData.put("senderUserId", message.getSenderUserId());
            messageData.put("senderUsername", message.getSenderUsername());
            messageData.put("messageText", message.getMessageText());
            messageData.put("timestamp", message.getTimestamp());

            // Clear input immediately to prevent double-sending
            messageInput.setText("");

            db.collection("alliance_messages").document(messageId)
                .set(messageData)
                .addOnSuccessListener(aVoid -> {
                    android.util.Log.d("AllianceChatActivity", "Message sent successfully");

                    // Send notifications to other alliance members
                    sendNotificationToAllianceMembers(message);
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("AllianceChatActivity", "Error sending message", e);
                    Toast.makeText(this, "Greška pri slanju poruke: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    // Restore the message text if sending failed
                    messageInput.setText(messageText);
                });
        } catch (Exception e) {
            android.util.Log.e("AllianceChatActivity", "Exception in sendMessage", e);
            Toast.makeText(this, "Neočekivana greška pri slanju poruke", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendNotificationToAllianceMembers(AllianceMessage message) {
        if (message == null || allianceId == null) {
            android.util.Log.w("AllianceChatActivity", "Cannot send notification - message or allianceId is null");
            return;
        }

        try {
            // Get alliance members and send notification to each (except sender)
            db.collection("alliances").document(allianceId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        Object memberIdsObj = document.get("memberIds");
                        if (memberIdsObj instanceof List<?>) {
                            @SuppressWarnings("unchecked")
                            List<String> memberIds = (List<String>) memberIdsObj;
                            if (memberIds != null && !memberIds.isEmpty()) {
                                for (String memberId : memberIds) {
                                    if (memberId != null && !memberId.equals(currentUserId)) {
                                        try {
                                            // Send notification to this member
                                            notificationManager.createLiveNotification(
                                                memberId,
                                                "Nova poruka u savezu",
                                                message.getSenderUsername() + ": " + message.getMessageText(),
                                                "ALLIANCE_MESSAGE",
                                                allianceId
                                            );
                                        } catch (Exception e) {
                                            android.util.Log.e("AllianceChatActivity", "Error sending notification to member: " + memberId, e);
                                        }
                                    }
                                }
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("AllianceChatActivity", "Error getting alliance members for notifications", e);
                });
        } catch (Exception e) {
            android.util.Log.e("AllianceChatActivity", "Exception in sendNotificationToAllianceMembers", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (messagesListener != null) {
            messagesListener.remove();
        }
    }
}
