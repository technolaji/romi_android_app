package technolaji.romiglobal;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.attachment.FileClientService;
import com.applozic.mobicomkit.api.conversation.ApplozicMqttIntentService;
import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.api.people.UserIntentService;
import com.applozic.mobicomkit.broadcast.BroadcastService;
import com.applozic.mobicomkit.uiwidgets.AlCustomizationSettings;
import com.applozic.mobicomkit.uiwidgets.conversation.ConversationUIService;
import com.applozic.mobicomkit.uiwidgets.conversation.MessageCommunicator;
import com.applozic.mobicomkit.uiwidgets.conversation.MobiComKitBroadcastReceiver;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.ConversationActivity;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.MobiComKitActivityInterface;
import com.applozic.mobicomkit.uiwidgets.conversation.fragment.ConversationFragment;
import com.applozic.mobicomkit.uiwidgets.conversation.fragment.MobiComQuickConversationFragment;
import com.applozic.mobicomkit.uiwidgets.instruction.ApplozicPermissions;
import com.applozic.mobicomkit.uiwidgets.people.fragment.ProfileFragment;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.file.FileUtils;
import com.applozic.mobicommons.json.GsonUtils;
import com.applozic.mobicommons.people.channel.Channel;
import com.applozic.mobicommons.people.contact.Contact;
import com.google.android.gms.common.api.GoogleApiClient;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HomeActivity extends AppCompatActivity implements MessageCommunicator, MobiComKitActivityInterface, ActivityCompat.OnRequestPermissionsResultCallback, SettingsFragment.OnFragmentInteractionListener {

    public static final int LOCATION_SERVICE_ENABLE = 1001;
    private static int retry;
    public LinearLayout layout;
    public Snackbar snackbar;
    MobiComKitBroadcastReceiver mobiComKitBroadcastReceiver;
    ConversationUIService conversationUIService;
    MobiComQuickConversationFragment mobiComQuickConversationFragment;
    private TextView mTextMessage;
    Fragment fragment;
    protected ConversationFragment conversation;
    ProfileFragment profileFragment;
    private Uri imageUri;
    private ApplozicPermissions applozicPermission;
    AlCustomizationSettings alCustomizationSettings;
    private FrameLayout container;
    File mediaFile;
    protected GoogleApiClient googleApiClient;
    File profilePhotoFile;
    public boolean isTakePhoto;
    public Contact contact;
    public boolean isAttachment;
    private static Uri capturedImageUri;
    private Uri videoFileUri;
    private Toolbar toolbar;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            switch (item.getItemId()) {
                case R.id.navigation_settings:
                    fragment = new SettingsFragment();
                    HomeActivity.this.setTitle("Settings");
                    replaceFragment(fragment, fragment.getTag());
                    break;
                case R.id.navigation_chat:
                    if(mobiComQuickConversationFragment != null){
                        HomeActivity.this.setTitle("Chat");
                        replaceFragment(mobiComQuickConversationFragment, mobiComQuickConversationFragment.getTag());
                    }
                    break;
                case R.id.navigation_profile:
                    profileFragment = new ProfileFragment();
                    HomeActivity.this.setTitle("Profile");
                    String jsonString = FileUtils.loadSettingsJsonFile(getApplicationContext());
                    if (!TextUtils.isEmpty(jsonString)) {
                        alCustomizationSettings = (AlCustomizationSettings) GsonUtils.getObjectFromJson(jsonString, AlCustomizationSettings.class);
                    } else {
                        alCustomizationSettings = new AlCustomizationSettings();
                    }
                    applozicPermission = new ApplozicPermissions(HomeActivity.this, layout);
                    profileFragment.setAlCustomizationSettings(alCustomizationSettings);
                    profileFragment.setApplozicPermissions(applozicPermission);
                    replaceFragment(profileFragment, profileFragment.getTag());
                    break;
                default:
                    fragment = new SettingsFragment();
                    replaceFragment(fragment, fragment.getTag());
                    break;
            }
            return true;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        mobiComQuickConversationFragment =   new MobiComQuickConversationFragment();
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        container = (FrameLayout) findViewById(R.id.content);
        layout = (LinearLayout) findViewById(R.id.footerAd);//this is snack bar layout needed
        conversationUIService = new ConversationUIService(this, mobiComQuickConversationFragment);
        mobiComKitBroadcastReceiver = new MobiComKitBroadcastReceiver(this, conversationUIService);



        Intent lastSeenStatusIntent = new Intent(this, UserIntentService.class);
        lastSeenStatusIntent.putExtra(UserIntentService.USER_LAST_SEEN_AT_STATUS, true);
        startService(lastSeenStatusIntent);

        fragment = new SettingsFragment();
        replaceFragment(fragment, fragment.getTag());
    }

    @Override
    public void onQuickConversationFragmentItemClick(View view, Contact contact, Channel channel, Integer conversationId, String searchString) {
        Intent intent = new Intent(this, ConversationActivity.class);
        intent.putExtra(ConversationUIService.TAKE_ORDER, true);
        intent.putExtra(ConversationUIService.SEARCH_STRING, searchString);
        intent.putExtra(ConversationUIService.CONVERSATION_ID, conversationId);
        if (contact != null) {
            intent.putExtra(ConversationUIService.USER_ID, contact.getUserId());
            intent.putExtra(ConversationUIService.DISPLAY_NAME, contact.getDisplayName());
        } else if (channel != null) {
            intent.putExtra(ConversationUIService.GROUP_ID, channel.getKey());
            intent.putExtra(ConversationUIService.GROUP_NAME, channel.getName());
        }
        startActivity(intent);

    }

    @Override
    public void startContactActivityForResult() {
        conversationUIService.startContactActivityForResult();
    }

    @Override
    public void addFragment(ConversationFragment conversationFragment) {
        replaceFragment(conversationFragment, ConversationUIService.CONVERSATION_FRAGMENT);
        conversation = conversationFragment;

    }

    @Override
    public void updateLatestMessage(Message message, String formattedContactNumber) {
        conversationUIService.updateLatestMessage(message, formattedContactNumber);

    }

    @Override
    public void removeConversation(Message message, String formattedContactNumber) {
        conversationUIService.removeConversation(message, formattedContactNumber);

    }

    @Override
    public void showErrorMessageView(String errorMessage) {
        layout.setVisibility(View.VISIBLE);
        snackbar = Snackbar.make(layout, "mesasge", Snackbar.LENGTH_LONG);
        snackbar.setAction("OK", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbar.dismiss();
            }
        });
        snackbar.setDuration(Snackbar.LENGTH_LONG);
        ViewGroup group = (ViewGroup) snackbar.getView();
        TextView textView = (TextView) group.findViewById(R.id.snackbar_action);
        textView.setTextColor(Color.YELLOW);
        group.setBackgroundColor(getResources().getColor(R.color.error_background_color));
        TextView txtView = (TextView) group.findViewById(R.id.snackbar_text);
        txtView.setMaxLines(5);
        snackbar.show();
    }

    @Override
    public void retry() {
        retry++;
    }

    @Override
    public int getRetryCount() {
        return retry;
    }

    public void dismissErrorMessage() {
        if (snackbar != null) {
            snackbar.dismiss();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mobiComKitBroadcastReceiver, BroadcastService.getIntentFilter());
        Intent subscribeIntent = new Intent(this, ApplozicMqttIntentService.class);
        subscribeIntent.putExtra(ApplozicMqttIntentService.SUBSCRIBE, true);
        startService(subscribeIntent);

        if (!Utils.isInternetAvailable(getApplicationContext())) {
            String errorMessage = getResources().getString(R.string.internet_connection_not_available);
            showErrorMessageView(errorMessage);
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        final String deviceKeyString = MobiComUserPreference.getInstance(this).getDeviceKeyString();
        final String userKeyString = MobiComUserPreference.getInstance(this).getSuUserKeyString();
        Intent intent = new Intent(this, ApplozicMqttIntentService.class);
        intent.putExtra(ApplozicMqttIntentService.USER_KEY_STRING, userKeyString);
        intent.putExtra(ApplozicMqttIntentService.DEVICE_KEY_STRING, deviceKeyString);
        startService(intent);
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mobiComKitBroadcastReceiver);
        super.onPause();
    }

    public void replaceFragment(Fragment fragmentToAdd, String fragmentTag) {
        FragmentManager supportFragmentManager = getSupportFragmentManager();


//         Fragment activeFragment = UIService.getActiveFragment(fragmentActivity);
        FragmentTransaction fragmentTransaction = supportFragmentManager
                .beginTransaction();
        fragmentTransaction.replace(R.id.content, fragmentToAdd,
                fragmentTag);

        if (supportFragmentManager.getBackStackEntryCount() > 1
                && !ConversationUIService.MESSGAE_INFO_FRAGMENT.equalsIgnoreCase(fragmentTag)) {
            supportFragmentManager.popBackStackImmediate();
        }
        fragmentTransaction.addToBackStack(fragmentTag);
        fragmentTransaction.commitAllowingStateLoss();
        supportFragmentManager.executePendingTransactions();
        //Log.i(TAG, "BackStackEntryCount: " + supportFragmentManager.getBackStackEntryCount());
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ConversationUIService.REQUEST_CODE_CONTACT_GROUP_SELECTION && resultCode == RESULT_OK) {
            String userId = data.getStringExtra(ConversationUIService.USER_ID);
            Integer groupId = data.getIntExtra(ConversationUIService.GROUP_ID, -1);
            Intent intent = new Intent(this, ConversationActivity.class);
            if (!TextUtils.isEmpty(userId)) {
                intent.putExtra(ConversationUIService.USER_ID, userId);
            }
            if (groupId != null && groupId != -1 && groupId != 0) {
                intent.putExtra(ConversationUIService.GROUP_ID, groupId);
            }
            intent.putExtra(ConversationUIService.TAKE_ORDER, true);
            startActivity(intent);
        } else {
            try {
                conversationUIService.onActivityResult(requestCode, resultCode, data);
                handleOnActivityResult(requestCode, data);
                if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                    CropImage.ActivityResult result = CropImage.getActivityResult(data);
                    if (resultCode == RESULT_OK) {
                        if (data == null) {
                            return;
                        }
                        if (imageUri != null) {
                            imageUri = result.getUri();
                            if (imageUri != null && profileFragment != null) {
                                profileFragment.handleProfileimageUpload(false, imageUri, profilePhotoFile);
                            }
                        } else {
                            imageUri = result.getUri();
                            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                            String imageFileName = "JPEG_" + timeStamp + "_" + ".jpeg";
                            profilePhotoFile = FileClientService.getFilePath(imageFileName, this, "image/jpeg");
                            if (imageUri != null && profileFragment != null) {
                                profileFragment.handleProfileimageUpload(true, imageUri, profilePhotoFile);
                            }
                        }
                    } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                        Toast.makeText(this, "Cropping failed: " + result.getError(), Toast.LENGTH_LONG).show();
                    }
                }
                if (requestCode == LOCATION_SERVICE_ENABLE) {
                    if (((LocationManager) getSystemService(Context.LOCATION_SERVICE))
                            .isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        googleApiClient.connect();
                    } else {
                        Toast.makeText(HomeActivity.this, R.string.unable_to_fetch_location, Toast.LENGTH_LONG).show();
                    }
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void handleOnActivityResult(int requestCode, Intent intent) {
        switch (requestCode) {
            case ProfileFragment.REQUEST_CODE_ATTACH_PHOTO:
                Uri selectedFileUri = (intent == null ? null : intent.getData());
                imageUri = null;
                beginCrop(selectedFileUri);
                break;
            case ProfileFragment.REQUEST_CODE_TAKE_PHOTO:
                beginCrop(imageUri);
                break;
        }


    }

    void beginCrop(Uri imageUri) {
        try {
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.OFF)
                    .setMultiTouchEnabled(true)
                    .start(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        if (requestCode == PermissionsUtils.REQUEST_STORAGE) {
//            if (PermissionsUtils.verifyPermissions(grantResults)) {
//                showSnackBar(com.applozic.mobicomkit.uiwidgets.R.string.storage_permission_granted);
//                if(isAttachment){
//                    isAttachment = false;
//                    processAttachment();
//                }
//            } else {
//                showSnackBar(com.applozic.mobicomkit.uiwidgets.R.string.storage_permission_not_granted);
//            }
//        }
////        else if (requestCode == PermissionsUtils.REQUEST_LOCATION) {
////            if (PermissionsUtils.verifyPermissions(grantResults)) {
////                showSnackBar(com.applozic.mobicomkit.uiwidgets.R.string.location_permission_granted);
////                processingLocation();
////            } else {
////                showSnackBar(com.applozic.mobicomkit.uiwidgets.R.string.location_permission_not_granted);
////            }
////
////        }
//        else if (requestCode == PermissionsUtils.REQUEST_PHONE_STATE) {
//            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                showSnackBar(com.applozic.mobicomkit.uiwidgets.R.string.phone_state_permission_granted);
//            } else {
//                showSnackBar(com.applozic.mobicomkit.uiwidgets.R.string.phone_state_permission_not_granted);
//            }
//        }
//        else if (requestCode == PermissionsUtils.REQUEST_AUDIO_RECORD) {
//            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                showSnackBar(com.applozic.mobicomkit.uiwidgets.R.string.record_audio_permission_granted);
//                showAudioRecordingDialog();
//            } else {
//                showSnackBar(com.applozic.mobicomkit.uiwidgets.R.string.record_audio_permission_not_granted);
//            }
//        } else if (requestCode == PermissionsUtils.REQUEST_CAMERA) {
//            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                showSnackBar(com.applozic.mobicomkit.uiwidgets.R.string.phone_camera_permission_granted);
//                if (isTakePhoto) {
//                    processCameraAction();
//                } else {
//                    processVideoRecording();
//                }
//            } else {
//                showSnackBar(com.applozic.mobicomkit.uiwidgets.R.string.phone_camera_permission_not_granted);
//            }
//        } else if (requestCode == PermissionsUtils.REQUEST_CONTACT) {
//            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                showSnackBar(com.applozic.mobicomkit.uiwidgets.R.string.contact_permission_granted);
//                processContact();
//            } else {
//                showSnackBar(com.applozic.mobicomkit.uiwidgets.R.string.contact_permission_not_granted);
//            }
//        } else if (requestCode == PermissionsUtils.REQUEST_CAMERA_FOR_PROFILE_PHOTO) {
//            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                showSnackBar(com.applozic.mobicomkit.uiwidgets.R.string.phone_camera_permission_granted);
//                if(profileFragment != null){
//                    profileFragment.processPhotoOption();
//                }
//            } else {
//                showSnackBar(com.applozic.mobicomkit.uiwidgets.R.string.phone_camera_permission_not_granted);
//            }
//        }else if (requestCode == PermissionsUtils.REQUEST_STORAGE_FOR_PROFILE_PHOTO) {
//            if (PermissionsUtils.verifyPermissions(grantResults)) {
//                showSnackBar(com.applozic.mobicomkit.uiwidgets.R.string.storage_permission_granted);
//                if(profileFragment != null){
//                    profileFragment.processPhotoOption();
//                }
//            } else {
//                showSnackBar(com.applozic.mobicomkit.uiwidgets.R.string.storage_permission_not_granted);
//            }
//        }
//        else {
//            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        }
//    }
//
//    public void showSnackBar(int resId) {
//        snackbar = Snackbar.make(layout, resId,
//                Snackbar.LENGTH_SHORT);
//        snackbar.show();
//    }
//
//    public void processAttachment(){
//        if(Utils.hasMarshmallow() && PermissionsUtils.checkSelfForStoragePermission(this)){
//            applozicPermission.requestStoragePermissions();
//        }else {
//            Intent intentPick = new Intent(this, MobiComAttachmentSelectorActivity.class);
//            startActivityForResult(intentPick, MultimediaOptionFragment.REQUEST_MULTI_ATTCAHMENT);
//        }
//    }
//
////    public void processingLocation() {
////        if (alCustomizationSettings.isLocationShareViaMap() && !TextUtils.isEmpty(geoApiKey) && !API_KYE_STRING.equals(geoApiKey)) {
////            Intent toMapActivity = new Intent(this, MobicomLocationActivity.class);
////            startActivityForResult(toMapActivity, MultimediaOptionFragment.REQUEST_CODE_SEND_LOCATION);
////            Log.i("test", "Activity for result strarted");
////
////        } else {
////            //================= START GETTING LOCATION WITHOUT LOADING MAP AND SEND LOCATION AS TEXT===============
////
////            if (!((LocationManager) getSystemService(Context.LOCATION_SERVICE))
////                    .isProviderEnabled(LocationManager.GPS_PROVIDER)) {
////                AlertDialog.Builder builder = new AlertDialog.Builder(this);
////                builder.setTitle(com.applozic.mobicomkit.uiwidgets.R.string.location_services_disabled_title)
////                        .setMessage(com.applozic.mobicomkit.uiwidgets.R.string.location_services_disabled_message)
////                        .setCancelable(false)
////                        .setPositiveButton(com.applozic.mobicomkit.uiwidgets.R.string.location_service_settings, new DialogInterface.OnClickListener() {
////                            public void onClick(DialogInterface dialog, int id) {
////                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
////                                startActivityForResult(intent, LOCATION_SERVICE_ENABLE);
////                            }
////                        })
////                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
////                            public void onClick(DialogInterface dialog, int id) {
////                                dialog.cancel();
////                                Toast.makeText(HomeActivity.this, com.applozic.mobicomkit.uiwidgets.R.string.location_sending_cancelled, Toast.LENGTH_LONG).show();
////                            }
////                        });
////                AlertDialog alert = builder.create();
////                alert.show();
////            } else {
////                googleApiClient.disconnect();
////                googleApiClient.connect();
////            }
////
////            //=================  END ===============
////
////        }
////
////    }
//
//
//    public void showAudioRecordingDialog() {
//
//        if (Utils.hasMarshmallow() && PermissionsUtils.checkSelfPermissionForAudioRecording(this)) {
//            new ApplozicPermissions(this, layout).requestAudio();
//        } else if(PermissionsUtils.isAudioRecordingPermissionGranted(this)) {
//
//            FragmentManager supportFragmentManager = getSupportFragmentManager();
//            DialogFragment fragment = AudioMessageFragment.newInstance();
//
//            FragmentTransaction fragmentTransaction = supportFragmentManager
//                    .beginTransaction().add(fragment, "AudioMessageFragment");
//
//            fragmentTransaction.addToBackStack(null);
//            fragmentTransaction.commitAllowingStateLoss();
//
//        }else{
//
//            if(alCustomizationSettings.getAudioPermissionNotFoundMsg()==null){
//                showSnackBar(com.applozic.mobicomkit.uiwidgets.R.string.applozic_audio_permission_missing);
//            }else{
//                snackbar = Snackbar.make(layout, alCustomizationSettings.getAudioPermissionNotFoundMsg(),
//                        Snackbar.LENGTH_SHORT);
//                snackbar.show();
//            }
//
//        }
//    }
//
//    public void processCameraAction() {
//        try {
//            if (PermissionsUtils.isCameraPermissionGranted(this)) {
//                imageCapture();
//            } else {
//                if (Utils.hasMarshmallow() && PermissionsUtils.checkSelfForCameraPermission(this)) {
//                    applozicPermission.requestCameraPermission();
//                } else {
//                    imageCapture();
//                }
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//
//    public void processVideoRecording() {
//        try{
//            if(PermissionsUtils.isCameraPermissionGranted(this)){
//                showVideoCapture();
//            }else {
//                if (Utils.hasMarshmallow() && PermissionsUtils.checkSelfForCameraPermission(this)) {
//                    applozicPermission.requestCameraPermission();
//                } else {
//                    showVideoCapture();
//                }
//            }
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//    }
//
//    public void processContact(){
//        if(Utils.hasMarshmallow() && PermissionsUtils.checkSelfForContactPermission(this)){
//            applozicPermission.requestContactPermission();
//        }else {
//            Intent contactIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
//            contactIntent.setType(ContactsContract.Contacts.CONTENT_TYPE);
//            startActivityForResult(contactIntent, MultimediaOptionFragment.REQUEST_CODE_CONTACT_SHARE);
//        }
//    }
//    public void imageCapture() {
//        try {
//            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//            String imageFileName = "JPEG_" + timeStamp + "_" + ".jpeg";
//
//            mediaFile = FileClientService.getFilePath(imageFileName, getApplicationContext(), "image/jpeg");
//
//            capturedImageUri = FileProvider.getUriForFile(this,  Utils.getMetaDataValue(this, MobiComKitConstants.PACKAGE_NAME) + ".provider", mediaFile);
//
//            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//
//            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, capturedImageUri);
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//                cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//                ClipData clip =
//                        ClipData.newUri(getContentResolver(), "a Photo", capturedImageUri);
//
//                cameraIntent.setClipData(clip);
//                cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//                cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//
//            } else {
//                List<ResolveInfo> resInfoList =
//                        getPackageManager()
//                                .queryIntentActivities(cameraIntent, PackageManager.MATCH_DEFAULT_ONLY);
//
//                for (ResolveInfo resolveInfo : resInfoList) {
//                    String packageName = resolveInfo.activityInfo.packageName;
//                    grantUriPermission(packageName, capturedImageUri,
//                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//                    grantUriPermission(packageName, capturedImageUri,
//                            Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                }
//            }
//
//            if (cameraIntent.resolveActivity(getApplicationContext().getPackageManager()) != null) {
//                if (mediaFile != null) {
//                    startActivityForResult(cameraIntent, MultimediaOptionFragment.REQUEST_CODE_TAKE_PHOTO);
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//
//    public void showVideoCapture() {
//
//        try{
//            Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
//            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//            String imageFileName = "VID_" + timeStamp + "_" + ".mp4";
//
//            mediaFile = FileClientService.getFilePath(imageFileName, getApplicationContext(), "video/mp4");
//
//            videoFileUri = FileProvider.getUriForFile(this, Utils.getMetaDataValue(this, MobiComKitConstants.PACKAGE_NAME)+".provider",mediaFile);
//
//            videoIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoFileUri);
//
//            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
//                videoIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//                videoIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            } else if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN) {
//                ClipData clip=
//                        ClipData.newUri(getContentResolver(), "a Video", videoFileUri);
//
//                videoIntent.setClipData(clip);
//                videoIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//                videoIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//
//            } else {
//                List<ResolveInfo> resInfoList=
//                        getPackageManager()
//                                .queryIntentActivities(videoIntent, PackageManager.MATCH_DEFAULT_ONLY);
//
//                for (ResolveInfo resolveInfo : resInfoList) {
//                    String packageName = resolveInfo.activityInfo.packageName;
//                    grantUriPermission(packageName, videoFileUri,
//                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//                    grantUriPermission(packageName, videoFileUri,
//                            Intent.FLAG_GRANT_READ_URI_PERMISSION);
//
//                }
//            }
//
//            if (videoIntent.resolveActivity(getApplicationContext().getPackageManager()) != null) {
//                if (mediaFile != null) {
//                    videoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
//                    startActivityForResult(videoIntent, MultimediaOptionFragment.REQUEST_CODE_CAPTURE_VIDEO_ACTIVITY);
//                }
//            }
//
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//
//    }


}
