package technolaji.romiglobal;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;


import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.translation.language.Language;
import com.applozic.mobicommons.commons.core.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import technolaji.romiglobal.utils.CustomList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SettingsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    ListView list;
    String[] web = {
            "Invite",
            "Languages",
            "Translate",
            "Help"
//            "Change Display Name"
    };
    Integer[] imageId = {
            R.drawable.ic_mail_black_24dp,
            R.drawable.ic_flag_black_24dp,
            R.drawable.ic_compare_arrows_black_24dp,
            R.drawable.ic_help_black_24dp
//            R.drawable.ic_mode_edit_black_24dp

    };
    private AutoCompleteTextView acLanguage;

    private OnFragmentInteractionListener mListener;
    private String strLanguage;
    private Map<String, Language> languageMap = new TreeMap<String, Language>(String.CASE_INSENSITIVE_ORDER);
    private MobiComUserPreference userPreferences = MobiComUserPreference.getInstance(getContext());

    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_blank, container, false);
        acLanguage = (AutoCompleteTextView) view.findViewById(R.id.ac_settings_language);

        CustomList adapter = new
                CustomList(getContext(), web, imageId);
        list = (ListView) view.findViewById(R.id.list);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                switch (position) {
                    case 0:
                        String SHARE_TEXT = "share_text";
                        String inviteMessage;
                        inviteMessage = Utils.getMetaDataValue(getActivity().getApplicationContext(), SHARE_TEXT);

                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_SEND)
                                .setType("text/plain").putExtra(Intent.EXTRA_TEXT, inviteMessage);

                        List<Intent> targetedShareIntents = new ArrayList<Intent>();

                        List<ResolveInfo> resInfo = getActivity().getPackageManager().queryIntentActivities(intent, 0);
                        if (!resInfo.isEmpty()) {
                            for (ResolveInfo resolveInfo : resInfo) {
                                String packageName = resolveInfo.activityInfo.packageName;
                                Intent targetedShareIntent = new Intent(Intent.ACTION_SEND);
                                targetedShareIntent.setType("text/plain")
                                        .setAction(Intent.ACTION_SEND)
                                        .putExtra(Intent.EXTRA_TEXT, inviteMessage)
                                        .setPackage(packageName);
                                targetedShareIntents.add(targetedShareIntent);
                            }
                            Intent chooserIntent = Intent.createChooser(targetedShareIntents.remove(0), "Share Via");
                            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedShareIntents.toArray(new Parcelable[]{}));
                            startActivity(chooserIntent);
                        }

                        break;
                    case 1:
                        final Dialog dialog = new Dialog(getContext());
                        dialog.setContentView(R.layout.dialog_ac_language);
                        acLanguage = (AutoCompleteTextView) dialog.findViewById(R.id.ac_language_dialog);
                        Button finish = (Button) dialog.findViewById(R.id.finish_dialog_button);

                        ArrayAdapter<String> languageAdapter = new ArrayAdapter<String>(
                                getActivity(), android.R.layout.simple_dropdown_item_1line,
                                getResources().getStringArray(R.array.languages_array));
                        acLanguage.setAdapter(languageAdapter);
                        acLanguage.setThreshold(1);
                        finish.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                switch (checkFields()) {
                                    case 0:
                                        userPreferences.setLanguage(languageMap.get(strLanguage));
                                        dialog.dismiss();
                                        break;
                                    case 1:
                                        acLanguage.setError("Please select language from list");
                                        break;
                                    case 2:
                                        acLanguage.setError("Please select a language");
                                        break;
                                    default:
                                        break;
                                }
                            }
                        });
                        dialog.show();
                        break;
                    case 2:
                        if (userPreferences.isUser_translate()) {
                            Log.i("Translate function", String.valueOf(userPreferences.isUser_translate()));
                            userPreferences.setUser_translate(false);
                            Toast.makeText(getContext(), "Translate Off", Toast.LENGTH_SHORT).show();
                            Log.i("Translate function", String.valueOf(userPreferences.isUser_translate()));
                        } else {
                            userPreferences.setUser_translate(true);
                            Toast.makeText(getContext(), "Translate On", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 3:
                        CharSequence colors[] = new CharSequence[]{"Privacy Policy", "Contact Us"};
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("Help");
                        builder.setItems(colors, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int position) {

                                switch (position) {

                                    case 0:
                                        String url = "http://www.romiglobal.com/docs/RomiPrivacyPolicy.pdf";
                                        Intent i = new Intent(Intent.ACTION_VIEW);
                                        i.setData(Uri.parse(url));
                                        startActivity(i);
                                    case 1:
                                        Intent emailIntent = new Intent(Intent.ACTION_SEND);

                                        String aEmailList[] = {"romisupport@romiglobal.com "};

                                        emailIntent.putExtra(Intent.EXTRA_EMAIL, aEmailList);

                                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Help");

                                        emailIntent.setType("plain/text");
                                        emailIntent.putExtra(Intent.EXTRA_TEXT, "My message body.");

                                        startActivity(Intent.createChooser(emailIntent, "Send mail"));
                                }


                            }
                        });
                        builder.show();
                        break;
//                    case 4:
//                        final Dialog dialog1 = new Dialog(getContext());
//                        dialog1.setContentView(R.layout.dialog_edit_name);
//                        final EditText newName = (EditText) dialog1.findViewById(R.id.et_name_dialog);
//                        Button end = (Button) dialog1.findViewById(R.id.finish_edit_button);
//                        end.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                String name = newName.getText().toString();
//                                if(!name.isEmpty()){
//                                    UserService.getInstance(getContext()).updateDisplayNameORImageLink(name,null,null,null);
//                                    userPreferences.setDisplayName(name);
//                                    dialog1.dismiss();
//                                }{
//
//                                    newName.setError("Enter your name");
//                                }
//                            }
//                        });
//                        dialog1.show();
                    default:
                        break;
                }

            }
        });

        return view;
    }

    private int checkFields() {
        strLanguage = acLanguage.getText().toString();
        Log.i("LANGUAGE", strLanguage);
        String languages[] = getResources().getStringArray(R.array.languages_array);
        Set<String> language = new HashSet<String>(Arrays.asList(languages));


        for (Language language1 : Language.values()) {
            languageMap.put(language1.toString(), Language.valueOf(language1.toString()));
        }
        int errorCode;
        if (!strLanguage.isEmpty()) {
            if (language.contains(strLanguage)) {
                errorCode = 0;
                Log.i("ERROR CODE 1", "0");
            } else {
                errorCode = 1;
                Log.i("ERROR CODE 1", "1");
            }
        } else {
            errorCode = 2;
            Log.i("ERROR CODE 1", "2");
        }
        return errorCode;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
