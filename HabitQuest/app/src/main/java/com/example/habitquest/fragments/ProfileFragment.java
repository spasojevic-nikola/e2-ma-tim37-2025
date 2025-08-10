package com.example.habitquest.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.habitquest.DatabaseHelper;
import com.example.habitquest.R;
import com.example.habitquest.SessionManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    private DatabaseHelper dbHelper;

    private ImageView imageAvatar, imageQRCode;
    private TextView textUsername, textLevelTitle, textPP, textXP, textCoins, textBadgeCount;
    private LinearLayout layoutBadges, layoutEquipment;

    private int userId;

    private static final String TABLE_USER_PROFILES = "user_profiles";

    private SessionManager sessionManager; //dodajem ovo da bih imao prikaz za bas onog ulogovanog korisnika

    private void logAllUserProfiles() {
        Cursor cursor = dbHelper.getReadableDatabase().query(
                DatabaseHelper.TABLE_USER_PROFILES,
                null, // sve kolone
                null, null, null, null, null);

        if (cursor != null) {
            Log.d("ProfileFragment", "Svi user_profiles u bazi:");
            while (cursor.moveToNext()) {
                int userId = cursor.getInt(cursor.getColumnIndexOrThrow("user_id"));
                int level = cursor.getInt(cursor.getColumnIndexOrThrow("level"));
                String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                int powerPoints = cursor.getInt(cursor.getColumnIndexOrThrow("power_points"));
                int xp = cursor.getInt(cursor.getColumnIndexOrThrow("experience_points"));
                int coins = cursor.getInt(cursor.getColumnIndexOrThrow("coins"));
                Log.d("ProfileFragment", "user_id: " + userId + ", level: " + level + ", title: " + title +
                        ", power_points: " + powerPoints + ", xp: " + xp + ", coins: " + coins);
            }
            cursor.close();
        } else {
            Log.d("ProfileFragment", "Cursor je null");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("ProfileFragment", "onCreateView POZVAN");

        //ova dva ispod mi u principu ne trebaju
        dbHelper = new DatabaseHelper(getContext());  // inicijalizuj ovde
        logAllUserProfiles();

        sessionManager = new SessionManager(getContext()); //dodajem ovo da bih prikazao bas onog ulogovanog korisnika

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        dbHelper = new DatabaseHelper(getContext());
        dbHelper.insertTestUsersIfNotExists(1, 2);
        dbHelper.insertTestUserProfilesIfNotExists(1, 2);
        dbHelper.insertUserBadgesIfNotExists(1);
        dbHelper.insertUserBadgesIfNotExists(2);
        dbHelper.insertUserEquipmentIfNotExists(1);
        dbHelper.insertUserEquipmentIfNotExists(2);
        Log.d("ProfileFragment", "insertTestUserProfileIfNotExists POZVAN");

        imageAvatar = view.findViewById(R.id.imageAvatar);
        imageQRCode = view.findViewById(R.id.imageQRCode);
        textUsername = view.findViewById(R.id.textUsername);
        textLevelTitle = view.findViewById(R.id.textLevelTitle);
        textPP = view.findViewById(R.id.textPP);
        textXP = view.findViewById(R.id.textXP);
        textCoins = view.findViewById(R.id.textCoins);
        textBadgeCount = view.findViewById(R.id.textBadgeCount);
        layoutBadges = view.findViewById(R.id.layoutBadges);
        layoutEquipment = view.findViewById(R.id.layoutEquipment);

        userId = getLoggedInUserId();
        if (userId == -1) {
            Log.d("ProfileFragment", "Nema ulogovanog korisnika!");
            // Opcionalno: preusmeri na login ili prikaži poruku
        } else {
            Log.d("ProfileFragment", "Ulogovani userId: " + userId);
        }
        Log.d("ProfileFragment", "userId je " + userId);


        loadUserProfile();
        loadUserBadges();
        loadUserEquipment();

        return view;
    }

    private int getLoggedInUserId() {
        int userId = sessionManager.getUserId();
        if (userId == -1) {
            Log.d(TAG, "Nema ulogovanog korisnika!");
        }
        return userId;
    }


    private void loadUserProfile() {

        Log.d("ProfileFragment", "Usao u loaduserprofile  " + userId);
        Cursor profileCursor = dbHelper.getUserProfile(userId);

        if (profileCursor == null) {
            Log.e("ProfileFragment", "Kursor je null! Verovatno SQL upit nije uspeo.");
        } else {
            Log.d("ProfileFragment", "Kursor nije null, broj redova: " + profileCursor.getCount());
        }

        if (profileCursor != null && profileCursor.moveToFirst()) {
            Log.d("ProfileFragment", "usao sam u ovaj if ");
            int level = profileCursor.getInt(profileCursor.getColumnIndexOrThrow("level"));
            String title = profileCursor.getString(profileCursor.getColumnIndexOrThrow("title"));
            int powerPoints = profileCursor.getInt(profileCursor.getColumnIndexOrThrow("power_points"));
            int experiencePoints = profileCursor.getInt(profileCursor.getColumnIndexOrThrow("experience_points"));
            int coins = profileCursor.getInt(profileCursor.getColumnIndexOrThrow("coins"));
            String qrCodeText = profileCursor.getString(profileCursor.getColumnIndexOrThrow("qr_code"));

            // Postavi textove
            textLevelTitle.setText("Level " + level + " - " + title);
            Log.d("ProfileFragment", "postavio nivo = " + level);
            textPP.setText("Snaga: " + powerPoints);
            textXP.setText("XP: " + experiencePoints);
            textCoins.setText("Novčići: " + coins);
            Log.d("ProfileFragment", "postavio novcice = " + coins);

            Log.d("ProfileFragment", "Učitavam novcice za userId = " + coins);

            // TODO: učitaj i postavi QR kod sa qrCodeText (npr. generiši bitmap QR koda)
            // Za test možeš ostaviti postojeći drawable

        }
        profileCursor.close();

        // Za username i avatar ih možeš učitati iz users tabele:
        Cursor userCursor = dbHelper.getReadableDatabase().query(
                DatabaseHelper.TABLE_USERS,
                new String[]{DatabaseHelper.COLUMN_USERNAME, DatabaseHelper.COLUMN_AVATAR},
                DatabaseHelper.COLUMN_ID + "=?",
                new String[]{String.valueOf(userId)},
                null, null, null);

        if (userCursor != null && userCursor.moveToFirst()) {
            String username = userCursor.getString(userCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USERNAME));
            int avatarResId = userCursor.getInt(userCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AVATAR));

            textUsername.setText(username);
            if (avatarResId != 0) {
                imageAvatar.setImageResource(avatarResId);
            } else {
                imageAvatar.setImageResource(R.drawable.avatar_default);
            }
        }
        if(userCursor != null) userCursor.close();
    }

    private void loadUserBadges() {
        layoutBadges.removeAllViews();

        Cursor badgeCursor = dbHelper.getUserBadges(userId);
        int badgeCount = 0;

        if (badgeCursor != null) {
            while (badgeCursor.moveToNext()) {
                badgeCount++;

                String badgeIconName = badgeCursor.getString(badgeCursor.getColumnIndexOrThrow("badge_icon"));
                String badgeName = badgeCursor.getString(badgeCursor.getColumnIndexOrThrow("badge_name"));

                ImageView badgeView = new ImageView(getContext());

                // Pretpostavimo da su badge_icon nazivi drawable resursa
                int resId = getResources().getIdentifier(badgeIconName, "drawable", getContext().getPackageName());
                if (resId != 0) {
                    badgeView.setImageResource(resId);
                } else {
                    badgeView.setImageResource(R.drawable.ic_badge_placeholder); // neki default icon
                }

                // Podešavanje dimenzija i margine za bedževe
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(100, 100);
                params.setMargins(8, 0, 8, 0);
                badgeView.setLayoutParams(params);

                badgeView.setContentDescription(badgeName);
                layoutBadges.addView(badgeView);
            }
            badgeCursor.close();
        }

        textBadgeCount.setText("Broj bedževa: " + badgeCount);
    }

    private void loadUserEquipment() {
        layoutEquipment.removeAllViews();

        Cursor equipmentCursor = dbHelper.getUserEquipment(userId);
        if (equipmentCursor != null) {
            while (equipmentCursor.moveToNext()) {
                String equipmentIconName = equipmentCursor.getString(equipmentCursor.getColumnIndexOrThrow("equipment_icon"));
                String equipmentName = equipmentCursor.getString(equipmentCursor.getColumnIndexOrThrow("equipment_name"));

                ImageView equipmentView = new ImageView(getContext());

                int resId = getResources().getIdentifier(equipmentIconName, "drawable", getContext().getPackageName());
                if (resId != 0) {
                    equipmentView.setImageResource(resId);
                } else {
                    equipmentView.setImageResource(R.drawable.ic_equipment_placeholder);
                }

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(100, 100);
                params.setMargins(8, 0, 8, 0);
                equipmentView.setLayoutParams(params);

                equipmentView.setContentDescription(equipmentName);
                layoutEquipment.addView(equipmentView);
            }
            equipmentCursor.close();
        }
    }
}
