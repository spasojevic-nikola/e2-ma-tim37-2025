package com.example.newhabitquest;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class EquipmentManager {
    private FirebaseFirestore db;
    private CollectionReference equipmentRef;
    private String userId;

    public EquipmentManager(String userId) {
        this.userId = userId;
        this.db = FirebaseFirestore.getInstance();
        this.equipmentRef = db.collection("equipment"); // Glavna equipment kolekcija
    }

    public interface EquipmentBonusCallback {
        void onBonusCalculated(int powerBonus, int attackChanceBonus, int extraAttackChance, int coinBonus);
    }

    // Calculate total bonuses from active equipment
    public void calculateActiveBonuses(EquipmentBonusCallback callback) {
        equipmentRef.whereEqualTo("userId", userId).whereEqualTo("active", true).get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    int totalPowerBonus = 0;
                    int totalAttackChanceBonus = 0;
                    int totalExtraAttackChance = 0;
                    int totalCoinBonus = 0;

                    for (QueryDocumentSnapshot document : task.getResult()) {
                        try {
                            Map<String, Object> data = document.getData();
                            String effectType = (String) data.get("effectType");
                            Long effectValue = (Long) data.get("effectValue");

                            if (effectType != null && effectValue != null) {
                                switch (effectType) {
                                    case "power_boost":
                                    case "permanent_power":
                                        totalPowerBonus += effectValue.intValue();
                                        break;
                                    case "attack_chance":
                                        totalAttackChanceBonus += effectValue.intValue();
                                        break;
                                    case "extra_attack":
                                        totalExtraAttackChance += effectValue.intValue();
                                        break;
                                    case "coin_boost":
                                        totalCoinBonus += effectValue.intValue();
                                        break;
                                }
                            }
                        } catch (Exception e) {
                            android.util.Log.e("EquipmentManager", "Error processing active equipment", e);
                        }
                    }

                    callback.onBonusCalculated(totalPowerBonus, totalAttackChanceBonus, totalExtraAttackChance, totalCoinBonus);
                } else {
                    callback.onBonusCalculated(0, 0, 0, 0);
                }
            });
    }

    // Reduce duration of active clothing after boss fight
    public void processBossFightEffects() {
        equipmentRef.whereEqualTo("userId", userId).whereEqualTo("active", true).get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        try {
                            Map<String, Object> data = document.getData();
                            String type = (String) data.get("type");
                            Boolean permanent = (Boolean) data.get("permanent");
                            Long remainingDuration = (Long) data.get("remainingDuration");
                            String equipmentId = (String) data.get("id");

                            // Use compound document ID
                            String docId = userId + "_" + equipmentId;

                            // Handle single-use potions
                            if ("napici".equals(type) && (permanent == null || !permanent)) {
                                Map<String, Object> updates = new HashMap<>();
                                updates.put("active", false);
                                equipmentRef.document(docId).update(updates);
                            }

                            // Handle clothing duration
                            else if ("odeca".equals(type) && remainingDuration != null && remainingDuration > 0) {
                                int newDuration = remainingDuration.intValue() - 1;
                                Map<String, Object> updates = new HashMap<>();
                                updates.put("remainingDuration", newDuration);

                                if (newDuration <= 0) {
                                    updates.put("active", false);
                                }

                                equipmentRef.document(docId).update(updates);
                            }
                        } catch (Exception e) {
                            android.util.Log.e("EquipmentManager", "Error processing boss fight effects", e);
                        }
                    }
                }
            });
    }

    // Get active equipment for display
    public void getActiveEquipment(ActiveEquipmentCallback callback) {
        equipmentRef.whereEqualTo("userId", userId).whereEqualTo("active", true).get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    List<Equipment> activeEquipment = new ArrayList<>();

                    for (QueryDocumentSnapshot document : task.getResult()) {
                        try {
                            Map<String, Object> data = document.getData();
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

                            activeEquipment.add(equipment);
                        } catch (Exception e) {
                            android.util.Log.e("EquipmentManager", "Error processing active equipment document", e);
                        }
                    }

                    callback.onActiveEquipmentLoaded(activeEquipment);
                } else {
                    callback.onActiveEquipmentLoaded(new ArrayList<>());
                }
            });
    }

    public interface ActiveEquipmentCallback {
        void onActiveEquipmentLoaded(List<Equipment> activeEquipment);
    }

    // Calculate dynamic prices based on user's previous boss rewards
    public static int calculateDynamicPrice(int basePercentage, int lastBossReward) {
        return (basePercentage * lastBossReward) / 100;
    }

    // Update equipment prices based on user's progress
    public void updateEquipmentPrices(int lastBossReward) {
        equipmentRef.whereEqualTo("userId", userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    try {
                        Map<String, Object> data = document.getData();
                        String type = (String) data.get("type");
                        String equipmentId = (String) data.get("id");

                        if (!"oruzje".equals(type)) { // Weapons are not purchasable
                            int newPrice = 0;
                            switch (equipmentId) {
                                case "potion_power_20":
                                    newPrice = calculateDynamicPrice(50, lastBossReward);
                                    break;
                                case "potion_power_40":
                                    newPrice = calculateDynamicPrice(70, lastBossReward);
                                    break;
                                case "potion_permanent_5":
                                    newPrice = calculateDynamicPrice(200, lastBossReward);
                                    break;
                                case "potion_permanent_10":
                                    newPrice = calculateDynamicPrice(1000, lastBossReward);
                                    break;
                                case "gloves":
                                case "shield":
                                    newPrice = calculateDynamicPrice(60, lastBossReward);
                                    break;
                                case "boots":
                                    newPrice = calculateDynamicPrice(80, lastBossReward);
                                    break;
                            }

                            if (newPrice > 0) {
                                String docId = userId + "_" + equipmentId;
                                Map<String, Object> priceUpdate = new HashMap<>();
                                priceUpdate.put("price", newPrice);
                                equipmentRef.document(docId).update(priceUpdate);
                            }
                        }
                    } catch (Exception e) {
                        android.util.Log.e("EquipmentManager", "Error updating prices", e);
                    }
                }
            }
        });
    }
}
