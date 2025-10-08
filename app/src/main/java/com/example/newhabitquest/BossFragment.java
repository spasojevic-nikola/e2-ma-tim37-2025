package com.example.newhabitquest;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BossFragment extends Fragment {
    private int currentPP = 0;
    private String userId;
    private FirebaseFirestore db;
    private DocumentReference userRef;
    private List<Map<String, Object>> potions;
    private List<Map<String, Object>> clothes;
    private List<Map<String, Object>> weapons;
    private TextView tvCurrentPP;
    private LinearLayout layoutPotions, layoutClothes, layoutWeapons, layoutActiveEquipment;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_boss, container, false);

        // Initialize views
        //tvCurrentPP = view.findViewById(R.id.tv_current_pp_boss);
        //EditText inputPP = view.findViewById(R.id.input_pp_boss);
        //Button btnAddPP = view.findViewById(R.id.btn_add_pp_boss);
        layoutPotions = view.findViewById(R.id.layout_potions);
        layoutClothes = view.findViewById(R.id.layout_clothes);
        layoutWeapons = view.findViewById(R.id.layout_weapons);
        layoutActiveEquipment = view.findViewById(R.id.layout_active_equipment);
        Button btnStartFight = view.findViewById(R.id.btn_start_fight);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (userId != null) {
            userRef = db.collection("users").document(userId);
            loadUserData();
        }

        // Set up event listeners
        //btnAddPP.setOnClickListener(v -> addPP(inputPP));
        btnStartFight.setOnClickListener(v -> startBossFight());

        return view;
    }

    private void loadUserData() {
        userRef.addSnapshotListener((snapshot, e) -> {
            if (e != null || snapshot == null || !snapshot.exists()) return;

            // Load PP
            if (snapshot.contains("pp")) {
                Long ppLong = snapshot.getLong("pp");
                currentPP = ppLong != null ? ppLong.intValue() : 0;
                updatePPDisplay();
            }

            // Load equipment lists
            potions = loadEquipmentList(snapshot, "potions");
            clothes = loadEquipmentList(snapshot, "clothes");
            weapons = loadEquipmentList(snapshot, "weapons");

            // Update UI
            showEquipment(layoutPotions, potions, "Napitci");
            showEquipment(layoutClothes, clothes, "Odeća");
            showEquipment(layoutWeapons, weapons, "Oružje");
            showActiveEquipment();
        });
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> loadEquipmentList(com.google.firebase.firestore.DocumentSnapshot snapshot, String field) {
        if (snapshot.contains(field)) {
            Object obj = snapshot.get(field);
            if (obj instanceof List) {
                return (List<Map<String, Object>>) obj;
            }
        }
        return new ArrayList<>();
    }

    private void updatePPDisplay() {
        if (tvCurrentPP != null) {
            tvCurrentPP.setText("PP: " + currentPP);
        }
    }

    private void addPP(EditText inputPP) {
        String ppStr = inputPP.getText().toString().trim();
        if (ppStr.isEmpty()) {
            Toast.makeText(getContext(), "Unesite broj PP!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int pp = Integer.parseInt(ppStr);
            if (pp <= 0) {
                Toast.makeText(getContext(), "PP mora biti pozitivan broj!", Toast.LENGTH_SHORT).show();
                return;
            }

            currentPP += pp;
            updatePPDisplay();
            inputPP.setText("");

            if (userRef != null) {
                userRef.update("pp", currentPP);
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Unesite valjan broj!", Toast.LENGTH_SHORT).show();
        }
    }

    private void showEquipment(LinearLayout layout, List<Map<String, Object>> equipment, String type) {
        layout.removeAllViews();

        if (equipment == null || equipment.isEmpty()) {
            TextView tv = new TextView(getContext());
            tv.setText("Nema kupljenih " + type.toLowerCase());
            tv.setTextColor(getResources().getColor(android.R.color.darker_gray));
            layout.addView(tv);
            return;
        }

        for (Map<String, Object> item : equipment) {
            createEquipmentRow(layout, item, type);
        }
    }

    private void createEquipmentRow(LinearLayout layout, Map<String, Object> item, String type) {
        String name = (String) item.get("name");
        boolean active = Boolean.TRUE.equals(item.get("active"));
        int quantity = getIntValue(item, "quantity", 1);
        int remainingBattles = getIntValue(item, "remainingBattles", 0);
        boolean singleUse = Boolean.TRUE.equals(item.get("singleUse"));

        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 12, 0, 12);

        // Equipment info
        TextView tvInfo = new TextView(getContext());
        StringBuilder info = new StringBuilder(name);
        info.append(" (").append(quantity).append("x)");

        if (type.equals("Napitci") && singleUse) {
            info.append(" - Jednokratni");
        } else if (type.equals("Odeća") && active && remainingBattles > 0) {
            info.append(" - Preostalo: ").append(remainingBattles).append(" borbi");
        } else if (type.equals("Oružje")) {
            info.append(" - Trajno");
        }

        // Removed the "AKTIVNO" text display
        // Set text color based on active status without showing "AKTIVNO" text
        if (active) {
            tvInfo.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            tvInfo.setTextColor(getResources().getColor(android.R.color.primary_text_light));
        }

        tvInfo.setText(info.toString());
        tvInfo.setTextSize(14f);
        tvInfo.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(tvInfo);

        // Activate button - za sve tipove opreme
        if (quantity > 0 && !active) {
            Button btnActivate = new Button(getContext());
            btnActivate.setText("Aktiviraj");
            btnActivate.setTextSize(12f);
            btnActivate.setOnClickListener(v -> activateEquipment(item, type));
            row.addView(btnActivate);
        }

        layout.addView(row);
    }

    private int getIntValue(Map<String, Object> map, String key, int defaultValue) {
        Object value = map.get(key);
        if (value instanceof Long) {
            return ((Long) value).intValue();
        } else if (value instanceof Integer) {
            return (Integer) value;
        }
        return defaultValue;
    }

    private void activateEquipment(Map<String, Object> item, String type) {
        String name = (String) item.get("name");

        // Za sve tipove opreme samo označavamo kao aktivno - ne smanjujemo količinu odmah
        item.put("active", true);

        // Set specific properties based on type
        if (type.equals("Odeća")) {
            item.put("remainingBattles", 2);
        }

        // Update in Firebase
        String field = getFieldName(type);
        if (userRef != null && !field.isEmpty()) {
            List<Map<String, Object>> list = getEquipmentList(type);
            userRef.update(field, list)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), name + " je aktivirano!", Toast.LENGTH_SHORT).show();
                    showActiveEquipment(); // Refresh active equipment display
                })
                .addOnFailureListener(e ->
                    Toast.makeText(getContext(), "Greška pri aktivaciji!", Toast.LENGTH_SHORT).show());
        }
    }

    private void updateEquipmentListForProfile(String equipmentName, boolean removeCompletely) {
        if (userRef == null) return;

        userRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists() && snapshot.contains("equipment")) {
                List<String> equipmentList = new ArrayList<>();
                Object eqObj = snapshot.get("equipment");
                if (eqObj instanceof List) {
                    for (Object o : (List<?>) eqObj) {
                        if (o instanceof String) equipmentList.add((String) o);
                    }
                }

                // Pronađi i ažuriraj opremu u listi
                for (int i = 0; i < equipmentList.size(); i++) {
                    String s = equipmentList.get(i);
                    if (s.startsWith(equipmentName)) {
                        if (removeCompletely) {
                            // Ukloni potpuno
                            equipmentList.remove(i);
                        } else {
                            // Smanji količinu
                            int count = 1;
                            int idx1 = s.indexOf('(');
                            int idx2 = s.indexOf(')');
                            if (idx1 != -1 && idx2 != -1 && idx2 > idx1) {
                                try {
                                    count = Integer.parseInt(s.substring(idx1 + 1, idx2).trim());
                                } catch (Exception ignore) {}
                            }
                            count--;
                            if (count > 0) {
                                equipmentList.set(i, equipmentName + " (" + count + ")");
                            } else {
                                equipmentList.remove(i);
                            }
                        }
                        break;
                    }
                }

                // Ažuriraj u Firebase
                userRef.update("equipment", equipmentList);
            }
        });
    }

    private String getFieldName(String type) {
        switch (type) {
            case "Napitci": return "potions";
            case "Odeća": return "clothes";
            case "Oružje": return "weapons";
            default: return "";
        }
    }

    private List<Map<String, Object>> getEquipmentList(String type) {
        switch (type) {
            case "Napitci": return potions;
            case "Odeća": return clothes;
            case "Oružje": return weapons;
            default: return new ArrayList<>();
        }
    }

    private void showActiveEquipment() {
        layoutActiveEquipment.removeAllViews();

        boolean hasActiveEquipment = false;

        // Show active potions
        hasActiveEquipment = showActiveEquipmentOfType(potions, "🧪", android.R.color.holo_blue_dark) || hasActiveEquipment;

        // Show active clothes
        hasActiveEquipment = showActiveEquipmentOfType(clothes, "👕", android.R.color.holo_green_dark) || hasActiveEquipment;

        // Show active weapons
        hasActiveEquipment = showActiveEquipmentOfType(weapons, "⚔️", android.R.color.holo_red_dark) || hasActiveEquipment;

        if (!hasActiveEquipment) {
            TextView tv = new TextView(getContext());
            tv.setText("Nema aktivirane opreme");
            tv.setTextColor(getResources().getColor(android.R.color.darker_gray));
            layoutActiveEquipment.addView(tv);
        }
    }

    private boolean showActiveEquipmentOfType(List<Map<String, Object>> equipment, String icon, int colorRes) {
        boolean hasActive = false;

        if (equipment != null) {
            for (Map<String, Object> item : equipment) {
                if (Boolean.TRUE.equals(item.get("active"))) {
                    hasActive = true;
                    String name = (String) item.get("name");
                    int remainingBattles = getIntValue(item, "remainingBattles", 0);
                    boolean singleUse = Boolean.TRUE.equals(item.get("singleUse"));

                    TextView tv = new TextView(getContext());
                    StringBuilder text = new StringBuilder(icon).append(" ").append(name);

                    if (remainingBattles > 0) {
                        text.append(" (").append(remainingBattles).append(" borbi)");
                    } else if (singleUse) {
                        text.append(" (jednokratni)");
                    } else {
                        text.append(" (trajno)");
                    }

                    tv.setText(text.toString());
                    tv.setTextColor(getResources().getColor(colorRes));
                    tv.setPadding(0, 4, 0, 4);
                    layoutActiveEquipment.addView(tv);
                }
            }
        }

        return hasActive;
    }

    private void startBossFight() {
        // Consume activated equipment and update profile equipment list
        List<String> consumedItems = new ArrayList<>();
        consumeActivatedEquipment(consumedItems);

        // Update equipment list for profile
        updateEquipmentListAfterBattle(consumedItems);

        // Update Firebase
        updateEquipmentInFirebase();

        Toast.makeText(getContext(), "Borba sa bossom završena! Aktivirana oprema je iskorišćena.", Toast.LENGTH_LONG).show();

        // Refresh displays
        showEquipment(layoutPotions, potions, "Napitci");
        showEquipment(layoutClothes, clothes, "Odeća");
        showEquipment(layoutWeapons, weapons, "Oružje");
        showActiveEquipment();
    }

    private void consumeActivatedEquipment(List<String> consumedItems) {
        // Sada smanjujemo količinu napitaka tek kada se klikne Start Fight
        if (potions != null) {
            potions.removeIf(potion -> {
                if (Boolean.TRUE.equals(potion.get("active"))) {
                    String potionName = (String) potion.get("name");
                    int currentQuantity = getIntValue(potion, "quantity", 1);
                    boolean singleUse = Boolean.TRUE.equals(potion.get("singleUse"));

                    if (singleUse) {
                        // Za jednokratne napitke, smanjujemo količinu
                        if (currentQuantity > 1) {
                            potion.put("quantity", currentQuantity - 1);
                            potion.put("active", false); // Deaktiviraj ovaj napitak
                            consumedItems.add(potionName); // Dodaj u listu za ažuriranje profila
                            return false; // Keep the item
                        } else {
                            // Ako je poslednji, uklanjamo ga potpuno
                            consumedItems.add(potionName);
                            return true; // Remove the item
                        }
                    } else {
                        // Trajni napitci ostaju aktivni
                        return false;
                    }
                }
                return false;
            });
        }

        // Smanjujemo trajanje odeće
        if (clothes != null) {
            clothes.removeIf(cloth -> {
                if (Boolean.TRUE.equals(cloth.get("active"))) {
                    String clothName = (String) cloth.get("name");
                    int remaining = getIntValue(cloth, "remainingBattles", 0);
                    if (remaining > 1) {
                        cloth.put("remainingBattles", remaining - 1);
                        return false; // Keep the item
                    } else {
                        // Dodaj u listu potrošenih predmeta
                        consumedItems.add(clothName);
                        return true; // Remove the item
                    }
                }
                return false;
            });
        }
    }

    private void updateEquipmentListAfterBattle(List<String> consumedItems) {
        if (userRef == null) return;

        userRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists() && snapshot.contains("equipment")) {
                List<String> equipmentList = new ArrayList<>();
                Object eqObj = snapshot.get("equipment");
                if (eqObj instanceof List) {
                    for (Object o : (List<?>) eqObj) {
                        if (o instanceof String) equipmentList.add((String) o);
                    }
                }

                // Ažuriraj equipment listu na osnovu potrošenih predmeta
                for (String consumedItem : consumedItems) {
                    for (int i = 0; i < equipmentList.size(); i++) {
                        String item = equipmentList.get(i);
                        if (item.startsWith(consumedItem)) {
                            // Izvuci količinu iz stringa
                            int count = 1;
                            int idx1 = item.indexOf('(');
                            int idx2 = item.indexOf(')');
                            if (idx1 != -1 && idx2 != -1 && idx2 > idx1) {
                                try {
                                    count = Integer.parseInt(item.substring(idx1 + 1, idx2).trim());
                                } catch (Exception ignore) {}
                            }

                            // Smanji količinu
                            count--;
                            if (count > 0) {
                                equipmentList.set(i, consumedItem + " (" + count + ")");
                            } else {
                                equipmentList.remove(i);
                            }
                            break;
                        }
                    }
                }

                // Ažuriraj u Firebase
                userRef.update("equipment", equipmentList);
            }
        });
    }

    private void updateEquipmentInFirebase() {
        if (userRef != null) {
            userRef.update("potions", potions);
            userRef.update("clothes", clothes);
        }
    }
}
