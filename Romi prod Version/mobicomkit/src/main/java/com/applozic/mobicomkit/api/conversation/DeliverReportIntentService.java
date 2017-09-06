package com.applozic.mobicomkit.api.conversation;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;

/**
 * Created by ninu on 16/07/17.
 */

public class DeliverReportIntentService extends IntentService {


    public static String PAIRED_MESSAGE_KEY_STRING = "PairedMessageKeyString";

    public static String MESSAGE_KEY_ACTION = "MESSAGE_KEY_ACTION";


    public DeliverReportIntentService() {
        super("DeliverReportIntentService");
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        if (intent != null && MESSAGE_KEY_ACTION.equals(intent.getAction())) {
            MessageClientService messageClientService = new MessageClientService(DeliverReportIntentService.this);
            MobiComUserPreference mobiComUserPreference = MobiComUserPreference.getInstance(DeliverReportIntentService.this);
            String pairedMessageKeyString = intent.getStringExtra(PAIRED_MESSAGE_KEY_STRING);
            if (!TextUtils.isEmpty(pairedMessageKeyString)) {
                messageClientService.updateDeliveryStatus(pairedMessageKeyString, mobiComUserPreference.getUserId(), mobiComUserPreference.getContactNumber());
            }
        }
    }
}
