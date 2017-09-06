package technolaji.romiglobal;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;


import com.applozic.mobicomkit.api.translation.language.Language;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


/**
 * Created by bolajiabiodun on 05/04/2017.
 */

public class ProfileDetailsFragment extends Fragment {

    private TextInputLayout country, language, status;
    private EditText etStatus;
    private AutoCompleteTextView acLanguage, acCountry;
    private String strCountry, strLanguage, strStatus;
    private CommunicationFrag commFrag;
    private Button next;
    private Map<String, Language> languageMap = new TreeMap<String, Language>(String.CASE_INSENSITIVE_ORDER);

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        commFrag = (CommunicationFrag) getActivity();
        next = (Button) getActivity().findViewById(R.id.signup_next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.8F);
                v.startAnimation(buttonClick);

                switch (checkFields()) {
                    case 0:
                        commFrag.setLanguage(languageMap.get(strLanguage));
                        commFrag.setCountry(strCountry);
                        commFrag.setStatus(strStatus);
                        commFrag.prepareSignUp();
                        break;
                    case 1:
                        language.setError("Please select language from list");
                        break;
                    case 2:
                        language.setError("Please select a language");
                        break;
                    case 3:
                        country.setError("Please select a country");
                        break;
                    default:
                        break;
                }
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_details_fragment, container, false);
        country = (TextInputLayout) view.findViewById(R.id.ti_country);
        language = (TextInputLayout) view.findViewById(R.id.ti_language);
        status = (TextInputLayout) view.findViewById(R.id.ti_status);

        acLanguage = (AutoCompleteTextView) view.findViewById(R.id.ac_language);
        acCountry = (AutoCompleteTextView) view.findViewById(R.id.ac_country);
        etStatus = (EditText) view.findViewById(R.id.et_status);


        ArrayAdapter<String> languageAdapter = new ArrayAdapter<String>(
                getActivity(), android.R.layout.simple_dropdown_item_1line,
                getResources().getStringArray(R.array.languages_array));
        acLanguage.setAdapter(languageAdapter);
        acLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View arg0) {
                acLanguage.showDropDown();
            }
        });

        Locale[] locale = Locale.getAvailableLocales();
        ArrayList<String> countries = new ArrayList<String>();
        String country;
        for (Locale loc : locale) {
            country = loc.getDisplayCountry();
            if (country.length() > 0 && !countries.contains(country)) {
                countries.add(country);
            }
        }
        Collections.sort(countries, String.CASE_INSENSITIVE_ORDER);

        ArrayAdapter<String> countryAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_dropdown_item_1line, countries);
        acCountry.setAdapter(countryAdapter);
        acCountry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View arg0) {
                acCountry.showDropDown();
            }
        });
        return view;
    }


    private int checkFields() {

        strCountry = acCountry.getText().toString();
        strLanguage = acLanguage.getText().toString();
        strStatus = etStatus.getText().toString();
        String languages[] = getResources().getStringArray(R.array.languages_array);
        Set<String> language = new HashSet<String>(Arrays.asList(languages));

        for (Language language1 : Language.values()) {
            languageMap.put(language1.toString(), Language.valueOf(language1.toString()));
        }



        int errorCode = 0;

        if (!strCountry.isEmpty()) {
            if (!strLanguage.isEmpty()) {
                if (language.contains(strLanguage)) {
                    errorCode = 0;
                } else {
                    errorCode = 1;
                }
            } else {
                errorCode = 2;
            }
        } else {
            errorCode = 3;
        }
        return errorCode;
    }



}
