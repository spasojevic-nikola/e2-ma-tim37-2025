package com.example.newhabitquest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AllianceInvitationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String invitationId = intent.getStringExtra("invitationId");
        String receiverUserId = intent.getStringExtra("receiverUserId");

        if (action != null && invitationId != null) {
            AllianceInvitationManager invitationManager = new AllianceInvitationManager(context);

            if ("ACCEPT_INVITATION".equals(action)) {
                invitationManager.acceptInvitation(invitationId, receiverUserId);
            } else if ("REJECT_INVITATION".equals(action)) {
                invitationManager.rejectInvitation(invitationId, receiverUserId);
            }
        }
    }
}
