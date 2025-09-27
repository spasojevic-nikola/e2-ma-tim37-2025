package com.example.newhabitquest;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize equipment data for logged in user
        initializeUserEquipment();

        Button logoutBtn = findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(v -> {
            try {
                // Clear any active fragments first to prevent crashes
                getSupportFragmentManager().popBackStack();

                // Sign out from Firebase
                FirebaseAuth.getInstance().signOut();

                // Create intent for login activity
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

                // Start login activity
                startActivity(intent);

                // Don't call finish() immediately - let the activity transition complete

            } catch (Exception e) {
                android.util.Log.e("MainActivity", "Error during logout", e);
                // If there's an error, just restart the login activity
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                Fragment selectedFragment = null;
                if (item.getItemId() == R.id.nav_profile) {
                    selectedFragment = new ProfileFragment();
                } else if (item.getItemId() == R.id.nav_tasks) {
                    selectedFragment = new TasksFragment();
                } else if (item.getItemId() == R.id.nav_stats) {
                    selectedFragment = new StatsFragment();
                } else if (item.getItemId() == R.id.nav_boss) {
                    selectedFragment = new BossFragment();
                } else if (item.getItemId() == R.id.nav_equipment) {
                    selectedFragment = new EquipmentFragment();
                }
                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
                    return true;
                }
                return false;
            }
        });
        // Show ProfileFragment by default
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();
        }
    }

    private void initializeUserEquipment() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference equipmentRef = db.collection("equipment");

        // Check if equipment already exists for this user
        equipmentRef.whereEqualTo("userId", userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().isEmpty()) {
                android.util.Log.d("MainActivity", "Creating default equipment for user: " + userId);
                createDefaultEquipmentForUser(equipmentRef, userId);
            }
        });
    }

    private void createDefaultEquipmentForUser(CollectionReference equipmentRef, String userId) {
        // Create all default equipment items
        java.util.List<Map<String, Object>> equipmentItems = java.util.Arrays.asList(
            // Napici
            createEquipmentMap("potion_power_20", "Napitak snage 20%", "napici",
                "Jednokratno povećanje snage za 20% u sledećoj borbi", 100, "power_boost", 20, false, 1, false, false, 0, 0, userId),
            createEquipmentMap("potion_power_40", "Napitak snage 40%", "napici",
                "Jednokratno povećanje snage za 40% u sledećoj borbi", 150, "power_boost", 40, false, 1, false, false, 0, 0, userId),
            createEquipmentMap("potion_permanent_5", "Eliksir trajne snage 5%", "napici",
                "Trajno povećanje snage za 5%", 500, "permanent_power", 5, true, 0, false, false, 0, 0, userId),
            createEquipmentMap("potion_permanent_10", "Eliksir trajne snage 10%", "napici",
                "Trajno povećanje snage za 10%", 1200, "permanent_power", 10, true, 0, false, false, 0, 0, userId),
            createEquipmentMap("potion_health", "Napitak zdravlja", "napici",
                "Vraća 30 HP tokom borbe", 80, "health_restore", 30, false, 1, false, false, 0, 0, userId),
            createEquipmentMap("potion_mana", "Napitak mane", "napici",
                "Vraća 20 MP tokom borbe", 60, "mana_restore", 20, false, 1, false, false, 0, 0, userId),

            // Odeća
            createEquipmentMap("gloves", "Čelične rukavice", "odeca",
                "Povećanje snage za 10% tokom 2 borbe", 120, "power_boost", 10, false, 2, false, false, 0, 0, userId),
            createEquipmentMap("shield", "Vitezov štit", "odeca",
                "Povećanje šanse uspešnog napada za 10% tokom 2 borbe", 130, "attack_chance", 10, false, 2, false, false, 0, 0, userId),
            createEquipmentMap("boots", "Brze čizme", "odeca",
                "40% šanse za dodatni napad tokom 2 borbe", 180, "extra_attack", 40, false, 2, false, false, 0, 0, userId),
            createEquipmentMap("armor", "Ratna oprema", "odeca",
                "Smanjuje primljenu štetu za 15% tokom 2 borbe", 200, "damage_reduction", 15, false, 2, false, false, 0, 0, userId),
            createEquipmentMap("helmet", "Šlem heroja", "odeca",
                "Povećava XP za 25% tokom 2 borbe", 160, "xp_boost", 25, false, 2, false, false, 0, 0, userId),

            // Oružje
            createEquipmentMap("sword", "Mač osvajača", "oruzje",
                "Trajno povećanje snage za 5%", 0, "permanent_power", 5, true, 0, false, false, 0, 0, userId),
            createEquipmentMap("bow", "Luk zlatnog strjelca", "oruzje",
                "Trajno povećanje novca za 5%", 0, "coin_boost", 5, true, 0, false, false, 0, 0, userId),
            createEquipmentMap("axe", "Sekira berserka", "oruzje",
                "Trajno povećanje kritičnog napada za 8%", 0, "critical_chance", 8, true, 0, false, false, 0, 0, userId),
            createEquipmentMap("staff", "Štap mudrosti", "oruzje",
                "Trajno povećanje XP za 10%", 0, "xp_boost", 10, true, 0, false, false, 0, 0, userId)
        );

        // Save all equipment items
        for (Map<String, Object> equipmentData : equipmentItems) {
            String equipmentId = userId + "_" + equipmentData.get("id"); // Unique ID combining userId and equipmentId
            equipmentRef.document(equipmentId).set(equipmentData)
                .addOnSuccessListener(aVoid -> android.util.Log.d("MainActivity", "Equipment created: " + equipmentId))
                .addOnFailureListener(e -> android.util.Log.e("MainActivity", "Failed to create equipment: " + equipmentId, e));
        }
    }

    private Map<String, Object> createEquipmentMap(String id, String name, String type, String description,
                                                  int price, String effectType, int effectValue, boolean isPermanent,
                                                  int duration, boolean isOwned, boolean isActive, int quantity, int remainingDuration, String userId) {
        Map<String, Object> equipment = new HashMap<>();
        equipment.put("id", id);
        equipment.put("userId", userId); // Dodajem userId polje
        equipment.put("name", name);
        equipment.put("type", type);
        equipment.put("description", description);
        equipment.put("price", price);
        equipment.put("effectType", effectType);
        equipment.put("effectValue", effectValue);
        equipment.put("permanent", isPermanent);
        equipment.put("duration", duration);
        equipment.put("owned", isOwned);
        equipment.put("active", isActive);
        equipment.put("quantity", quantity);
        equipment.put("remainingDuration", remainingDuration);
        return equipment;
    }
}