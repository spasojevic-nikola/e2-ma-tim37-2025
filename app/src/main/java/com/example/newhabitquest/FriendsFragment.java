package com.example.newhabitquest;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FriendsFragment extends Fragment implements FriendsAdapter.OnFriendClickListener {

    private RecyclerView friendsRecyclerView;
    private RecyclerView searchRecyclerView;
    private FriendsAdapter friendsAdapter;
    private FriendsAdapter searchAdapter;
    private EditText searchEditText;
    private Button showQRButton;
    private Button scanQRButton;
    private ImageView qrCodeImage;
    private View qrCodeContainer;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String currentUserId;

    private List<Friend> friendsList;
    private List<Friend> searchResultsList;

    // QR Scanner launcher
    private final androidx.activity.result.ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if (result.getContents() == null) {
                    Toast.makeText(getContext(), "Skeniranje otkazano", Toast.LENGTH_SHORT).show();
                } else {
                    handleScannedQRCode(result.getContents());
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);

        initViews(view);
        initFirebase();
        setupRecyclerViews();
        setupSearch();
        setupQRCode();
        loadFriends();

        return view;
    }

    private void initViews(View view) {
        friendsRecyclerView = view.findViewById(R.id.friends_recycler_view);
        searchRecyclerView = view.findViewById(R.id.search_recycler_view);
        searchEditText = view.findViewById(R.id.search_edit_text);
        showQRButton = view.findViewById(R.id.show_qr_button);
        scanQRButton = view.findViewById(R.id.scan_qr_button);
        qrCodeImage = view.findViewById(R.id.qr_code_image);
        qrCodeContainer = view.findViewById(R.id.qr_code_container);
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }

    private void setupRecyclerViews() {
        friendsList = new ArrayList<>();
        searchResultsList = new ArrayList<>();

        friendsAdapter = new FriendsAdapter(friendsList, this);
        friendsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        friendsRecyclerView.setAdapter(friendsAdapter);

        searchAdapter = new FriendsAdapter(searchResultsList, this);
        searchRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        searchRecyclerView.setAdapter(searchAdapter);
        searchRecyclerView.setVisibility(View.GONE);
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    searchUsers(s.toString());
                    searchRecyclerView.setVisibility(View.VISIBLE);
                    friendsRecyclerView.setVisibility(View.GONE);
                } else {
                    searchRecyclerView.setVisibility(View.GONE);
                    friendsRecyclerView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupQRCode() {
        showQRButton.setOnClickListener(v -> {
            if (qrCodeContainer.getVisibility() == View.VISIBLE) {
                qrCodeContainer.setVisibility(View.GONE);
                showQRButton.setText("Prikaži QR kod");
            } else {
                generateQRCode();
                qrCodeContainer.setVisibility(View.VISIBLE);
                showQRButton.setText("Sakrij QR kod");
            }
        });

        scanQRButton.setOnClickListener(v -> {
            // Start QR scanner
            ScanOptions options = new ScanOptions();
            options.setPrompt("Usmerite ka QR kodu");
            options.setOrientationLocked(false);
            barcodeLauncher.launch(options);
        });
    }

    private void generateQRCode() {
        if (currentUserId == null) return;

        try {
            // Generate QR code sa current user ID
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            String qrData = "FRIEND_REQUEST:" + currentUserId;

            BitMatrix bitMatrix = qrCodeWriter.encode(qrData, BarcodeFormat.QR_CODE, 300, 300);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            qrCodeImage.setImageBitmap(bitmap);
            android.util.Log.d("FriendsFragment", "QR code generated successfully for user: " + currentUserId);

        } catch (WriterException e) {
            android.util.Log.e("FriendsFragment", "Error generating QR code", e);
            Toast.makeText(getContext(), "Greška pri generisanju QR koda", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleScannedQRCode(String qrContent) {
        android.util.Log.d("FriendsFragment", "Scanned QR code: " + qrContent);

        if (qrContent.startsWith("FRIEND_REQUEST:")) {
            String friendUserId = qrContent.substring("FRIEND_REQUEST:".length());

            // Check if it's not the current user
            if (friendUserId.equals(currentUserId)) {
                Toast.makeText(getContext(), "Ne možete dodati sebe kao prijatelja!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if friendship already exists
            checkFriendshipStatus(friendUserId);
        } else {
            Toast.makeText(getContext(), "Nevažeći QR kod", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkFriendshipStatus(String friendUserId) {
        db.collection("friendships")
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("friendId", friendUserId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        Toast.makeText(getContext(), "Već ste prijatelji sa ovim korisnikom!", Toast.LENGTH_SHORT).show();
                    } else {
                        // Check reverse friendship
                        db.collection("friendships")
                                .whereEqualTo("userId", friendUserId)
                                .whereEqualTo("friendId", currentUserId)
                                .get()
                                .addOnSuccessListener(reverseQuerySnapshot -> {
                                    if (!reverseQuerySnapshot.isEmpty()) {
                                        Toast.makeText(getContext(), "Već ste prijatelji sa ovim korisnikom!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        // Get friend details and send request
                                        loadFriendDetailsForQR(friendUserId);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    android.util.Log.e("FriendsFragment", "Error checking reverse friendship", e);
                                    Toast.makeText(getContext(), "Greška pri proveri prijateljstva", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("FriendsFragment", "Error checking friendship", e);
                    Toast.makeText(getContext(), "Greška pri proveri prijateljstva", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadFriendDetailsForQR(String friendUserId) {
        db.collection("users").document(friendUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");
                        String avatar = documentSnapshot.getString("avatar");
                        Long levelLong = documentSnapshot.getLong("level");
                        int level = levelLong != null ? levelLong.intValue() : 1;

                        Friend friend = new Friend(friendUserId, username, avatar, level);

                        // Show confirmation dialog and send friend request
                        showFriendRequestConfirmation(friend);
                    } else {
                        Toast.makeText(getContext(), "Korisnik nije pronađen", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("FriendsFragment", "Error loading friend details", e);
                    Toast.makeText(getContext(), "Greška pri učitavanju korisnika", Toast.LENGTH_SHORT).show();
                });
    }

    private void showFriendRequestConfirmation(Friend friend) {
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Dodaj prijatelja")
                .setMessage("Da li želite da dodate " + friend.getUsername() + " kao prijatelja?")
                .setPositiveButton("Da", (dialog, which) -> {
                    sendFriendRequestFromQR(friend);
                })
                .setNegativeButton("Ne", null)
                .show();
    }

    private void sendFriendRequestFromQR(Friend friend) {
        Map<String, Object> friendship1 = new HashMap<>();
        friendship1.put("userId", currentUserId);
        friendship1.put("friendId", friend.getUserId());
        friendship1.put("status", "accepted");
        friendship1.put("timestamp", System.currentTimeMillis());

        Map<String, Object> friendship2 = new HashMap<>();
        friendship2.put("userId", friend.getUserId());
        friendship2.put("friendId", currentUserId);
        friendship2.put("status", "accepted");
        friendship2.put("timestamp", System.currentTimeMillis());

        // Add both friendships simultaneously
        db.collection("friendships")
                .add(friendship1)
                .addOnSuccessListener(documentReference1 -> {
                    db.collection("friendships")
                            .add(friendship2)
                            .addOnSuccessListener(documentReference2 -> {
                                Toast.makeText(getContext(),
                                        "Uspešno ste dodali " + friend.getUsername() + " kao prijatelja!",
                                        Toast.LENGTH_LONG).show();
                                // Hide QR code container after successful addition
                                qrCodeContainer.setVisibility(View.GONE);
                                showQRButton.setText("Prikaži QR kod");
                                loadFriends(); // Refresh friends list
                            })
                            .addOnFailureListener(e -> {
                                android.util.Log.e("FriendsFragment", "Error adding reverse friendship", e);
                                Toast.makeText(getContext(), "Greška pri dodavanju prijatelja", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("FriendsFragment", "Error adding friendship", e);
                    Toast.makeText(getContext(), "Greška pri dodavanju prijatelja", Toast.LENGTH_SHORT).show();
                });
    }

    private void searchUsers(String query) {
        if (query.length() < 2) return;

        db.collection("users")
                .whereGreaterThanOrEqualTo("username", query)
                .whereLessThanOrEqualTo("username", query + "\uf8ff")
                .limit(20)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    searchResultsList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String userId = document.getId();
                        if (!userId.equals(currentUserId)) { // Don't show current user
                            String username = document.getString("username");
                            String avatar = document.getString("avatar");
                            Long levelLong = document.getLong("level");
                            int level = levelLong != null ? levelLong.intValue() : 1;

                            Friend searchResult = new Friend(userId, username, avatar, level);
                            searchResult.setStatus("search_result");
                            searchResultsList.add(searchResult);
                        }
                    }
                    searchAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("FriendsFragment", "Error searching users", e);
                    Toast.makeText(getContext(), "Greška pri pretrazi korisnika", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadFriends() {
        if (currentUserId == null) return;

        db.collection("friendships")
                .whereEqualTo("userId", currentUserId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        android.util.Log.e("FriendsFragment", "Error loading friends", error);
                        return;
                    }

                    friendsList.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot document : value) {
                            String friendId = document.getString("friendId");
                            String status = document.getString("status");

                            // Load friend details
                            loadFriendDetails(friendId, status);
                        }
                    }
                });
    }

    private void loadFriendDetails(String friendId, String status) {
        db.collection("users").document(friendId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");
                        String avatar = documentSnapshot.getString("avatar");
                        Long levelLong = documentSnapshot.getLong("level");
                        int level = levelLong != null ? levelLong.intValue() : 1;

                        Friend friend = new Friend(friendId, username, avatar, level);
                        friend.setStatus(status);

                        // Check if friend is already in list (to avoid duplicates)
                        boolean exists = false;
                        for (int i = 0; i < friendsList.size(); i++) {
                            if (friendsList.get(i).getUserId().equals(friendId)) {
                                friendsList.set(i, friend);
                                exists = true;
                                break;
                            }
                        }

                        if (!exists) {
                            friendsList.add(friend);
                        }

                        friendsAdapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    public void onFriendClick(Friend friend) {
        // Handle friend click - maybe show friend details
        Toast.makeText(getContext(), "Clicked on " + friend.getUsername(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRemoveFriendClick(Friend friend) {
        if (friend.getStatus().equals("search_result")) {
            // Send friend request
            sendFriendRequest(friend);
        } else {
            // Remove/block friend
            removeFriend(friend);
        }
    }

    @Override
    public void onViewProfileClick(Friend friend) {
        android.content.Intent intent = new android.content.Intent(getContext(), FriendProfileActivity.class);
        intent.putExtra("friendId", friend.getUserId());
        startActivity(intent);
    }

    private void sendFriendRequest(Friend friend) {
        if (currentUserId == null) return;

        Map<String, Object> friendship = new HashMap<>();
        friendship.put("userId", currentUserId);
        friendship.put("friendId", friend.getUserId());
        friendship.put("status", "pending");
        friendship.put("timestamp", System.currentTimeMillis());

        db.collection("friendships")
                .add(friendship)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Zahtev za prijateljstvo poslat!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("FriendsFragment", "Error sending friend request", e);
                    Toast.makeText(getContext(), "Greška pri slanju zahteva", Toast.LENGTH_SHORT).show();
                });
    }

    private void removeFriend(Friend friend) {
        if (currentUserId == null) return;

        db.collection("friendships")
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("friendId", friend.getUserId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        document.getReference().delete();
                    }
                    Toast.makeText(getContext(), "Prijatelj uklonjen", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("FriendsFragment", "Error removing friend", e);
                    Toast.makeText(getContext(), "Greška pri uklanjanju prijatelja", Toast.LENGTH_SHORT).show();
                });
    }
}
