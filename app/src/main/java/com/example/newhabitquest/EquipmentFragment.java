package com.example.newhabitquest;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.tabs.TabLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class EquipmentFragment extends Fragment implements EquipmentAdapter.OnEquipmentClickListener {
    private TabLayout tabLayout;
    private RecyclerView recyclerView;
    private EquipmentAdapter adapter;
    private List<Equipment> allEquipment;
    private List<Equipment> currentList;
    private FirebaseFirestore db;
    private CollectionReference equipmentRef, userRef;
    private String currentUserId;
    private int userCoins;
    private com.google.firebase.firestore.ListenerRegistration equipmentListener;
    private com.google.firebase.firestore.ListenerRegistration userCoinsListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_equipment, container, false);

        tabLayout = view.findViewById(R.id.tabLayout);
        recyclerView = view.findViewById(R.id.recyclerView);

        // Check if user is logged in
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(getContext(), "Morate biti ulogovani da pristupite opremi!", Toast.LENGTH_SHORT).show();
            return view;
        }

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (currentUserId == null) {
            Toast.makeText(getContext(), "Greška prilikom dohvatanja korisničkog ID-ja!", Toast.LENGTH_SHORT).show();
            return view;
        }

        try {
            db = FirebaseFirestore.getInstance();
            equipmentRef = db.collection("equipment"); // Glavna equipment kolekcija
            userRef = db.collection("users");

            setupRecyclerView();
            setupTabs();
            loadUserCoins();
            initializeEquipment();
        } catch (Exception e) {
            android.util.Log.e("EquipmentFragment", "Error initializing Firestore", e);
            Toast.makeText(getContext(), "Greška pri učitavanju opreme: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        return view;
    }

    private void setupRecyclerView() {
        allEquipment = new ArrayList<>();
        currentList = new ArrayList<>();
        adapter = new EquipmentAdapter(currentList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterEquipmentByType(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void filterEquipmentByType(int position) {
        if (currentList == null || allEquipment == null) {
            android.util.Log.e("EquipmentFragment", "Lists are null in filterEquipmentByType");
            return;
        }

        currentList.clear();
        String type = getTypeByPosition(position);
        android.util.Log.d("EquipmentFragment", "Filtering by type: " + type);

        for (Equipment equipment : allEquipment) {
            if (equipment != null && equipment.getType() != null && equipment.getType().equals(type)) {
                currentList.add(equipment);
            }
        }

        android.util.Log.d("EquipmentFragment", "Filtered " + currentList.size() + " items for type " + type);

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        } else {
            android.util.Log.e("EquipmentFragment", "Adapter is null");
        }
    }

    private String getTypeByPosition(int position) {
        switch (position) {
            case 0: return "napici";
            case 1: return "odeca";
            case 2: return "oruzje";
            default: return "napici";
        }
    }

    private void loadUserCoins() {
        userCoinsListener = userRef.document(currentUserId).addSnapshotListener((snapshot, e) -> {
            // Add fragment validation and auth check
            if (!isAdded() || getContext() == null || FirebaseAuth.getInstance().getCurrentUser() == null) {
                return;
            }

            if (e != null) {
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                Long coinsLong = snapshot.getLong("coins");
                userCoins = coinsLong != null ? coinsLong.intValue() : 0;
            }
        });
    }

    private void initializeEquipment() {
        // Add small delay to allow MainActivity to finish creating equipment
        new android.os.Handler().postDelayed(() -> {
            equipmentRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (task.getResult().isEmpty()) {
                        android.util.Log.d("EquipmentFragment", "No equipment found, loading will happen via MainActivity");
                        // Don't create here, MainActivity will handle it
                        setupEmptyState();
                    } else {
                        android.util.Log.d("EquipmentFragment", "Loading existing equipment...");
                        loadEquipment();
                    }
                } else {
                    android.util.Log.e("EquipmentFragment", "Error checking equipment", task.getException());
                }
            });
        }, 1000); // 1 second delay
    }

    private void setupEmptyState() {
        // Show loading or empty state while MainActivity creates equipment
        android.util.Log.d("EquipmentFragment", "Setting up empty state");

        // Try to load equipment after another delay
        new android.os.Handler().postDelayed(() -> {
            loadEquipment();
        }, 2000);
    }

    private void createDefaultEquipment() {
        android.util.Log.d("EquipmentFragment", "Starting to create default equipment");
        List<Equipment> defaultEquipment = getDefaultEquipment();

        for (Equipment equipment : defaultEquipment) {
            Map<String, Object> equipmentMap = new HashMap<>();
            equipmentMap.put("id", equipment.getId());
            equipmentMap.put("name", equipment.getName());
            equipmentMap.put("type", equipment.getType());
            equipmentMap.put("description", equipment.getDescription());
            equipmentMap.put("price", equipment.getPrice());
            equipmentMap.put("effectType", equipment.getEffectType());
            equipmentMap.put("effectValue", equipment.getEffectValue());
            equipmentMap.put("permanent", equipment.isPermanent());
            equipmentMap.put("duration", equipment.getDuration());
            equipmentMap.put("owned", equipment.isOwned());
            equipmentMap.put("active", equipment.isActive());
            equipmentMap.put("quantity", equipment.getQuantity());
            equipmentMap.put("remainingDuration", equipment.getRemainingDuration());

            equipmentRef.document(equipment.getId()).set(equipmentMap)
                .addOnSuccessListener(aVoid -> android.util.Log.d("EquipmentFragment", "Successfully saved: " + equipment.getName()))
                .addOnFailureListener(e -> android.util.Log.e("EquipmentFragment", "Failed to save: " + equipment.getName(), e));
        }

        // Load equipment after creating
        loadEquipment();
    }

    private List<Equipment> getDefaultEquipment() {
        List<Equipment> equipment = new ArrayList<>();

        // Napici
        equipment.add(new Equipment("potion_power_20", "Napitak snage 20%", "napici",
            "Jednokratno povećanje snage za 20% u sledećoj borbi", 100, "power_boost", 20, false, 1));
        equipment.add(new Equipment("potion_power_40", "Napitak snage 40%", "napici",
            "Jednokratno povećanje snage za 40% u sledećoj borbi", 150, "power_boost", 40, false, 1));
        equipment.add(new Equipment("potion_permanent_5", "Eliksir trajne snage 5%", "napici",
            "Trajno povećanje snage za 5%", 500, "permanent_power", 5, true, 0));
        equipment.add(new Equipment("potion_permanent_10", "Eliksir trajne snage 10%", "napici",
            "Trajno povećanje snage za 10%", 1200, "permanent_power", 10, true, 0));
        equipment.add(new Equipment("potion_health", "Napitak zdravlja", "napici",
            "Vraća 30 HP tokom borbe", 80, "health_restore", 30, false, 1));
        equipment.add(new Equipment("potion_mana", "Napitak mane", "napici",
            "Vraća 20 MP tokom borbe", 60, "mana_restore", 20, false, 1));

        // Odeća
        equipment.add(new Equipment("gloves", "Čelične rukavice", "odeca",
            "Povećanje snage za 10% tokom 2 borbe", 120, "power_boost", 10, false, 2));
        equipment.add(new Equipment("shield", "Vitezov štit", "odeca",
            "Povećanje šanse uspešnog napada za 10% tokom 2 borbe", 130, "attack_chance", 10, false, 2));
        equipment.add(new Equipment("boots", "Brze čizme", "odeca",
            "40% šanse za dodatni napad tokom 2 borbe", 180, "extra_attack", 40, false, 2));
        equipment.add(new Equipment("armor", "Ratna oprema", "odeca",
            "Smanjuje primljenu štetu za 15% tokom 2 borbe", 200, "damage_reduction", 15, false, 2));
        equipment.add(new Equipment("helmet", "Šlem heroja", "odeca",
            "Povećava XP za 25% tokom 2 borbe", 160, "xp_boost", 25, false, 2));

        // Oružje
        equipment.add(new Equipment("sword", "Mač osvajača", "oruzje",
            "Trajno povećanje snage za 5%", 0, "permanent_power", 5, true, 0));
        equipment.add(new Equipment("bow", "Luk zlatnog strzelca", "oruzje",
            "Trajno povećanje novca za 5%", 0, "coin_boost", 5, true, 0));
        equipment.add(new Equipment("axe", "Sekira berserka", "oruzje",
            "Trajno povećanje kritičnog napada za 8%", 0, "critical_chance", 8, true, 0));
        equipment.add(new Equipment("staff", "Štap mudrosti", "oruzje",
            "Trajno povećanje XP za 10%", 0, "xp_boost", 10, true, 0));

        return equipment;
    }

    private void loadEquipment() {
        if (equipmentRef == null) {
            android.util.Log.e("EquipmentFragment", "equipmentRef is null");
            return;
        }

        // Query equipment for current user only
        equipmentListener = equipmentRef.whereEqualTo("userId", currentUserId).addSnapshotListener((value, e) -> {
            // Add fragment validation and auth check
            if (!isAdded() || getContext() == null || FirebaseAuth.getInstance().getCurrentUser() == null) {
                return;
            }

            if (e != null) {
                android.util.Log.e("EquipmentFragment", "Listen failed.", e);
                if (getContext() != null && isAdded()) {
                    Toast.makeText(getContext(), "Greška pri učitavanju: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                return;
            }

            if (value == null) {
                android.util.Log.e("EquipmentFragment", "QuerySnapshot is null");
                return;
            }

            allEquipment.clear();
            try {
                for (QueryDocumentSnapshot doc : value) {
                    try {
                        Map<String, Object> data = doc.getData();
                        Equipment equipment = new Equipment();

                        equipment.setId((String) data.get("id"));
                        equipment.setName((String) data.get("name"));
                        equipment.setType((String) data.get("type"));
                        equipment.setDescription((String) data.get("description"));

                        Long price = (Long) data.get("price");
                        equipment.setPrice(price != null ? price.intValue() : 0);

                        equipment.setEffectType((String) data.get("effectType"));

                        Long effectValue = (Long) data.get("effectValue");
                        equipment.setEffectValue(effectValue != null ? effectValue.intValue() : 0);

                        Boolean permanent = (Boolean) data.get("permanent");
                        equipment.setPermanent(permanent != null ? permanent : false);

                        Long duration = (Long) data.get("duration");
                        equipment.setDuration(duration != null ? duration.intValue() : 0);

                        Boolean owned = (Boolean) data.get("owned");
                        equipment.setOwned(owned != null ? owned : false);

                        Boolean active = (Boolean) data.get("active");
                        equipment.setActive(active != null ? active : false);

                        Long quantity = (Long) data.get("quantity");
                        equipment.setQuantity(quantity != null ? quantity.intValue() : 0);

                        Long remainingDuration = (Long) data.get("remainingDuration");
                        equipment.setRemainingDuration(remainingDuration != null ? remainingDuration.intValue() : 0);

                        allEquipment.add(equipment);
                        android.util.Log.d("EquipmentFragment", "Loaded equipment: " + equipment.getName());
                    } catch (Exception docException) {
                        android.util.Log.e("EquipmentFragment", "Error processing document: " + doc.getId(), docException);
                    }
                }
                filterEquipmentByType(tabLayout.getSelectedTabPosition());
            } catch (Exception generalException) {
                android.util.Log.e("EquipmentFragment", "General error in loadEquipment", generalException);
                if (getContext() != null && isAdded()) {
                    Toast.makeText(getContext(), "Greška pri procesiranju opreme", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onBuyClick(Equipment equipment) {
        android.util.Log.d("EquipmentFragment", "Buy button clicked for: " + equipment.getName());

        // Check if fragment is still valid
        if (getContext() == null || !isAdded()) {
            return;
        }

        // Check if we have enough coins
        if (userCoins < equipment.getPrice()) {
            Toast.makeText(getContext(), "Nemate dovoljno novčića!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Simple update - just add equipment to user document
            Map<String, Object> updates = new HashMap<>();
            updates.put("coins", userCoins - equipment.getPrice());

            // Simple equipment addition - just add the name
            userRef.document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    // Check if fragment is still valid in callback
                    if (getContext() == null || !isAdded()) {
                        return;
                    }

                    try {
                        List<String> equipmentList = new ArrayList<>();

                        // Get existing equipment safely
                        if (documentSnapshot.exists()) {
                            Object equipmentField = documentSnapshot.get("equipment");
                            if (equipmentField instanceof List) {
                                for (Object item : (List<?>) equipmentField) {
                                    if (item instanceof String) {
                                        equipmentList.add((String) item);
                                    }
                                }
                            }
                        }

                        // Handle equipment with quantities
                        String equipmentName = equipment.getName();
                        boolean found = false;

                        android.util.Log.d("EquipmentFragment", "Looking for equipment: " + equipmentName);
                        android.util.Log.d("EquipmentFragment", "Current equipment list: " + equipmentList.toString());

                        // Check if equipment already exists and update quantity
                        for (int i = 0; i < equipmentList.size(); i++) {
                            String item = equipmentList.get(i);
                            android.util.Log.d("EquipmentFragment", "Checking item: " + item);

                            // Check if this item matches our equipment (with or without quantity)
                            String itemBaseName = extractBaseNameFromEquipmentString(item);
                            android.util.Log.d("EquipmentFragment", "Item base name: " + itemBaseName);

                            if (itemBaseName.equals(equipmentName)) {
                                // Extract current quantity and increment
                                int currentQuantity = extractQuantityFromEquipmentString(item);
                                android.util.Log.d("EquipmentFragment", "Found match! Current quantity: " + currentQuantity);
                                equipmentList.set(i, equipmentName + " (" + (currentQuantity + 1) + ")");
                                android.util.Log.d("EquipmentFragment", "Updated to: " + equipmentName + " (" + (currentQuantity + 1) + ")");
                                found = true;
                                break;
                            }
                        }

                        // If not found, add with quantity 1
                        if (!found) {
                            android.util.Log.d("EquipmentFragment", "Equipment not found, adding new: " + equipmentName + " (1)");
                            equipmentList.add(equipmentName + " (1)");
                        }

                        android.util.Log.d("EquipmentFragment", "Final equipment list: " + equipmentList.toString());

                        updates.put("equipment", equipmentList);

                        // Update user document
                        userRef.document(currentUserId).update(updates)
                            .addOnSuccessListener(aVoid -> {
                                if (getContext() != null && isAdded()) {
                                    Toast.makeText(getContext(), "Kupljeno: " + equipment.getName(), Toast.LENGTH_SHORT).show();
                                    // Update local coins immediately to prevent inconsistent state
                                    userCoins = userCoins - equipment.getPrice();
                                    // Equipment list will be updated automatically via real-time listener
                                }
                            })
                            .addOnFailureListener(e -> {
                                android.util.Log.e("EquipmentFragment", "Failed to update user document", e);
                                if (getContext() != null && isAdded()) {
                                    Toast.makeText(getContext(), "Greška pri kupovini", Toast.LENGTH_SHORT).show();
                                }
                            });

                    } catch (Exception e) {
                        android.util.Log.e("EquipmentFragment", "Error processing purchase", e);
                        if (getContext() != null && isAdded()) {
                            Toast.makeText(getContext(), "Greška pri obradi kupovine", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("EquipmentFragment", "Failed to get user document", e);
                    if (getContext() != null && isAdded()) {
                        Toast.makeText(getContext(), "Greška pri dobijanju korisničkih podataka", Toast.LENGTH_SHORT).show();
                    }
                });

        } catch (Exception e) {
            android.util.Log.e("EquipmentFragment", "Critical error in buy", e);
            if (getContext() != null && isAdded()) {
                Toast.makeText(getContext(), "Kritična greška", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateUserAfterPurchase(Equipment equipment) {
        try {
            userRef.document(currentUserId).get().addOnSuccessListener(documentSnapshot -> {
                try {
                    // Deduct coins
                    Map<String, Object> userUpdate = new HashMap<>();
                    userUpdate.put("coins", userCoins - equipment.getPrice());

                    // Handle equipment list
                    List<String> userEquipment = new ArrayList<>();
                    if (documentSnapshot.exists()) {
                        Object equipmentObj = documentSnapshot.get("equipment");
                        if (equipmentObj instanceof List) {
                            List<?> existingEquipment = (List<?>) equipmentObj;
                            for (Object item : existingEquipment) {
                                if (item instanceof String) {
                                    userEquipment.add((String) item);
                                }
                            }
                        }
                    }

                    // Add new equipment
                    String equipmentName = equipment.getName();
                    if (equipment.getType().equals("napici")) {
                        // For potions, handle quantity carefully
                        boolean found = false;
                        for (int i = 0; i < userEquipment.size(); i++) {
                            String currentItem = userEquipment.get(i);
                            if (currentItem != null && currentItem.startsWith(equipmentName)) {
                                // Extract and increment quantity safely
                                int quantity = extractQuantityFromString(currentItem) + 1;
                                userEquipment.set(i, equipmentName + " (" + quantity + ")");
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            userEquipment.add(equipmentName + " (1)");
                        }
                    } else {
                        // For non-potions, just add if not present
                        if (!userEquipment.contains(equipmentName)) {
                            userEquipment.add(equipmentName);
                        }
                    }

                    userUpdate.put("equipment", userEquipment);
                    userRef.document(currentUserId).update(userUpdate)
                        .addOnSuccessListener(aVoid -> {
                            android.util.Log.d("EquipmentFragment", "User updated successfully");
                            Toast.makeText(getContext(), "Kupljena oprema: " + equipment.getName(), Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            android.util.Log.e("EquipmentFragment", "Failed to update user", e);
                            Toast.makeText(getContext(), "Greška pri ažuriranju korisnika", Toast.LENGTH_SHORT).show();
                        });

                } catch (Exception e) {
                    android.util.Log.e("EquipmentFragment", "Error processing user document", e);
                    Toast.makeText(getContext(), "Greška pri obradi korisničkih podataka", Toast.LENGTH_SHORT).show();
                }
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("EquipmentFragment", "Failed to get user document", e);
                Toast.makeText(getContext(), "Greška pri dohvatanju korisničkih podataka", Toast.LENGTH_SHORT).show();
            });

        } catch (Exception e) {
            android.util.Log.e("EquipmentFragment", "Error in updateUserAfterPurchase", e);
            Toast.makeText(getContext(), "Greška pri ažuriranju korisnika", Toast.LENGTH_SHORT).show();
        }
    }

    private int extractQuantityFromString(String equipmentString) {
        try {
            if (equipmentString.contains("(") && equipmentString.contains(")")) {
                int startIndex = equipmentString.indexOf("(") + 1;
                int endIndex = equipmentString.indexOf(")");
                if (startIndex < endIndex) {
                    String quantityStr = equipmentString.substring(startIndex, endIndex);
                    // Remove any non-numeric characters except digits
                    quantityStr = quantityStr.replaceAll("[^0-9]", "");
                    if (!quantityStr.isEmpty()) {
                        return Integer.parseInt(quantityStr);
                    }
                }
            }
        } catch (Exception e) {
            android.util.Log.e("EquipmentFragment", "Error extracting quantity from: " + equipmentString, e);
        }
        return 1; // Default quantity
    }

    private int extractQuantityFromEquipmentString(String equipmentString) {
        try {
            if (equipmentString.contains("(") && equipmentString.contains(")")) {
                int startIndex = equipmentString.indexOf("(") + 1;
                int endIndex = equipmentString.indexOf(")");
                if (startIndex < endIndex) {
                    String quantityStr = equipmentString.substring(startIndex, endIndex);
                    // Remove any non-numeric characters except digits
                    quantityStr = quantityStr.replaceAll("[^0-9]", "");
                    if (!quantityStr.isEmpty()) {
                        return Integer.parseInt(quantityStr);
                    }
                }
            }
        } catch (Exception e) {
            android.util.Log.e("EquipmentFragment", "Error extracting quantity from equipment string: " + equipmentString, e);
        }
        return 1; // Default quantity if not found or error
    }

    private String extractBaseNameFromEquipmentString(String equipmentString) {
        try {
            if (equipmentString.contains("(")) {
                // Return everything before the first opening parenthesis, trimmed
                return equipmentString.substring(0, equipmentString.indexOf("(")).trim();
            } else {
                // No parenthesis, return the whole string trimmed
                return equipmentString.trim();
            }
        } catch (Exception e) {
            android.util.Log.e("EquipmentFragment", "Error extracting base name from: " + equipmentString, e);
            return equipmentString; // Return original string if error
        }
    }

    @Override
    public void onActivateClick(Equipment equipment) {
        android.util.Log.d("EquipmentFragment", "Activate button clicked for: " + equipment.getName());

        if (getContext() == null || !isAdded()) {
            return;
        }

        try {
            // Update user's equipment list to decrease quantity
            userRef.document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (getContext() == null || !isAdded()) {
                        return;
                    }

                    try {
                        List<String> equipmentList = new ArrayList<>();

                        // Get existing equipment safely
                        if (documentSnapshot.exists()) {
                            Object equipmentField = documentSnapshot.get("equipment");
                            if (equipmentField instanceof List) {
                                for (Object item : (List<?>) equipmentField) {
                                    if (item instanceof String) {
                                        equipmentList.add((String) item);
                                    }
                                }
                            }
                        }

                        // Find and decrease quantity of the equipment
                        String equipmentName = equipment.getName();
                        boolean found = false;

                        for (int i = 0; i < equipmentList.size(); i++) {
                            String item = equipmentList.get(i);
                            if (item.startsWith(equipmentName)) {
                                int currentQuantity = extractQuantityFromEquipmentString(item);

                                if (currentQuantity > 1) {
                                    // Decrease quantity
                                    equipmentList.set(i, equipmentName + " (" + (currentQuantity - 1) + ")");
                                } else {
                                    // Remove item if quantity becomes 0
                                    equipmentList.remove(i);
                                }
                                found = true;
                                break;
                            }
                        }

                        if (found) {
                            Map<String, Object> updates = new HashMap<>();
                            updates.put("equipment", equipmentList);

                            userRef.document(currentUserId).update(updates)
                                .addOnSuccessListener(aVoid -> {
                                    if (getContext() != null && isAdded()) {
                                        Toast.makeText(getContext(), "Iskorišćeno: " + equipment.getName(), Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    android.util.Log.e("EquipmentFragment", "Failed to update equipment quantity", e);
                                    if (getContext() != null && isAdded()) {
                                        Toast.makeText(getContext(), "Greška pri korišćenju opreme", Toast.LENGTH_SHORT).show();
                                    }
                                });
                        } else {
                            if (getContext() != null && isAdded()) {
                                Toast.makeText(getContext(), "Oprema nije pronađena", Toast.LENGTH_SHORT).show();
                            }
                        }

                    } catch (Exception e) {
                        android.util.Log.e("EquipmentFragment", "Error processing equipment activation", e);
                        if (getContext() != null && isAdded()) {
                            Toast.makeText(getContext(), "Greška pri obradi aktivacije", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("EquipmentFragment", "Failed to get user document for activation", e);
                    if (getContext() != null && isAdded()) {
                        Toast.makeText(getContext(), "Greška pri dobijanju korisničkih podataka", Toast.LENGTH_SHORT).show();
                    }
                });

        } catch (Exception e) {
            android.util.Log.e("EquipmentFragment", "Critical error in activate", e);
            if (getContext() != null && isAdded()) {
                Toast.makeText(getContext(), "Kritična greška pri aktivaciji", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Clean up listeners when fragment is destroyed
        if (equipmentListener != null) {
            equipmentListener.remove();
            equipmentListener = null;
        }
        if (userCoinsListener != null) {
            userCoinsListener.remove();
            userCoinsListener = null;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        // Clean up listeners when fragment is detached
        if (equipmentListener != null) {
            equipmentListener.remove();
            equipmentListener = null;
        }
        if (userCoinsListener != null) {
            userCoinsListener.remove();
            userCoinsListener = null;
        }
    }
}
