package com.example.newhabitquest;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TasksFragment extends Fragment {
    // Stanje korisnika
    private int currentXP = 0;
    private int currentLevel = 1;
    private int currentPP = 0;
    private String currentTitle = "Pocetnik";
    private String userId;
    private FirebaseFirestore db;
    private DocumentReference userRef;

    // Titule za prva tri nivoa
    private final String[] titles = {"Pocetnik", "Iskusni", "Veteran"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);
        EditText inputXP = view.findViewById(R.id.input_xp);
        Button btnAddXP = view.findViewById(R.id.btn_add_xp);
        TextView tvCurrentXP = view.findViewById(R.id.tv_current_xp);
        TextView tvCurrentLevel = view.findViewById(R.id.tv_current_level);
        TextView tvXPToNext = view.findViewById(R.id.tv_xp_to_next);
        TextView tvCurrentPP = view.findViewById(R.id.tv_current_pp);
        TextView tvTitle = view.findViewById(R.id.tv_title);


        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (userId != null) {
            userRef = db.collection("users").document(userId);
            // Učitaj podatke iz baze
            userRef.get().addOnSuccessListener(snapshot -> {
                if (snapshot.exists()) {
                    if (snapshot.contains("xp")) currentXP = snapshot.getLong("xp").intValue();
                    if (snapshot.contains("level")) currentLevel = snapshot.getLong("level").intValue();
                    if (snapshot.contains("pp")) currentPP = snapshot.getLong("pp").intValue();
                    if (snapshot.contains("title")) currentTitle = snapshot.getString("title");
                    if (currentTitle == null || currentTitle.isEmpty()) currentTitle = "Pocetnik";
                    updateUI(tvCurrentXP, tvCurrentLevel, tvXPToNext, tvCurrentPP, tvTitle);
                }
            });
        }

        // Prikaz početnog stanja
        updateUI(tvCurrentXP, tvCurrentLevel, tvXPToNext, tvCurrentPP, tvTitle);

        btnAddXP.setOnClickListener(v -> {
            String xpStr = inputXP.getText().toString().trim();
            if (xpStr.isEmpty()) {
                Toast.makeText(getContext(), "Unesite broj XP-a!", Toast.LENGTH_SHORT).show();
                return;
            }
            int xp = Integer.parseInt(xpStr);
            if (xp <= 0) {
                Toast.makeText(getContext(), "XP mora biti pozitivan broj!", Toast.LENGTH_SHORT).show();
                return;
            }
            addXP(xp);
            updateUI(tvCurrentXP, tvCurrentLevel, tvXPToNext, tvCurrentPP, tvTitle);
            inputXP.setText("");
            // Sačuvaj u bazu
            saveToFirestore();
        });
        return view;
    }

    // Dodavanje XP-a i napredovanje kroz nivoe
    private void addXP(int xp) {
        currentXP += xp;
        int nextLevelXP = getXPForLevel(currentLevel);
        boolean leveledUp = false;
        while (currentXP >= nextLevelXP) {
            currentXP -= nextLevelXP;
            currentLevel++;
            leveledUp = true;
            // PP logika
            if (currentLevel == 2) {
                currentPP = 40;
            } else if (currentLevel > 2) {
                currentPP += Math.round(0.75f * currentPP);
            }
            // Titula
            if (currentLevel <= titles.length) {
                currentTitle = titles[currentLevel - 1];
            } else {
                currentTitle = "Legenda";
            }
            nextLevelXP = getXPForLevel(currentLevel);
        }
        if (leveledUp) {
            Toast.makeText(getContext(), "Čestitamo! Novi nivo: " + currentLevel, Toast.LENGTH_SHORT).show();
        }
    }

    // Formula za XP potreban za nivo
    private int getXPForLevel(int level) {
        if (level == 1) return 200;
        int prevXP = getXPForLevel(level - 1);
        int result = prevXP * 2 + prevXP / 2;
        // Zaokruži na prvu narednu stotinu
        if (result % 100 != 0) {
            result = ((result / 100) + 1) * 100;
        }
        return result;
    }

    // Prikaz trenutnog stanja
    private void updateUI(TextView tvCurrentXP, TextView tvCurrentLevel, TextView tvXPToNext, TextView tvCurrentPP, TextView tvTitle) {
        tvCurrentXP.setText(String.format(Locale.getDefault(), "Trenutni XP: %d", currentXP));
        tvCurrentLevel.setText(String.format(Locale.getDefault(), "Nivo: %d", currentLevel));
        tvXPToNext.setText(String.format(Locale.getDefault(), "XP do sledećeg nivoa: %d", getXPForLevel(currentLevel) - currentXP));
        tvCurrentPP.setText(String.format(Locale.getDefault(), "PP: %d", currentPP));
        tvTitle.setText(String.format(Locale.getDefault(), "Titula: %s", currentTitle));
    }

    // Čuvanje podataka u Firestore
    private void saveToFirestore() {
        if (userRef == null) return;
        Map<String, Object> data = new HashMap<>();
        data.put("xp", currentXP);
        data.put("level", currentLevel);
        data.put("pp", currentPP);
        data.put("title", currentTitle);
        data.put("titula", currentTitle); // Dodato za sinhronizaciju sa ProfileFragment
        userRef.update(data);
    }
}
