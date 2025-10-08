package com.example.newhabitquest;

import com.google.firebase.firestore.PropertyName;

public class Equipment {
    private String id;
    private String name;
    private String type; // "napici", "odeca", "oruzje"
    private String description;
    private int price;
    private String effectType; // "power_boost", "attack_chance", "extra_attack", "permanent_power", "coin_boost"
    private int effectValue; // percentage or flat value
    @PropertyName("permanent")
    private boolean isPermanent;
    private int duration; // number of boss fights for temporary effects
    @PropertyName("owned")
    private boolean isOwned;
    @PropertyName("active")
    private boolean isActive;
    private int quantity; // for potions
    private int remainingDuration; // for active clothing

    private boolean singleUse; // Dodato za napitke

    public Equipment() {
        // Default constructor for Firebase
    }

    public Equipment(String id, String name, String type, String description, int price,
                    String effectType, int effectValue, boolean isPermanent, int duration) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.description = description;
        this.price = price;
        this.effectType = effectType;
        this.effectValue = effectValue;
        this.isPermanent = isPermanent;
        this.duration = duration;
        this.isOwned = false;
        this.isActive = false;
        this.quantity = 0;
        this.remainingDuration = 0;

        // Postavi singleUse na osnovu tipa napitka
        if (type.equals("napici")) {
            // Napitci koji nisu trajni su jednokratni
            this.singleUse = !isPermanent;
        } else {
            this.singleUse = false;
        }
    }

    // Getters and setters with Firestore annotations
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }

    public String getEffectType() { return effectType; }
    public void setEffectType(String effectType) { this.effectType = effectType; }

    public int getEffectValue() { return effectValue; }
    public void setEffectValue(int effectValue) { this.effectValue = effectValue; }

    @PropertyName("permanent")
    public boolean getPermanent() { return isPermanent; }
    @PropertyName("permanent")
    public void setPermanent(boolean permanent) { isPermanent = permanent; }
    // Keep old method for compatibility
    public boolean isPermanent() { return isPermanent; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    @PropertyName("owned")
    public boolean getOwned() { return isOwned; }
    @PropertyName("owned")
    public void setOwned(boolean owned) { isOwned = owned; }
    // Keep old method for compatibility
    public boolean isOwned() { return isOwned; }

    @PropertyName("active")
    public boolean getActive() { return isActive; }
    @PropertyName("active")
    public void setActive(boolean active) { isActive = active; }
    // Keep old method for compatibility
    public boolean isActive() { return isActive; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public int getRemainingDuration() { return remainingDuration; }
    public void setRemainingDuration(int remainingDuration) { this.remainingDuration = remainingDuration; }

    public boolean getSingleUse() { return singleUse; }
    public void setSingleUse(boolean singleUse) { this.singleUse = singleUse; }
}
