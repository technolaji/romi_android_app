package technolaji.romiglobal;


import com.applozic.mobicomkit.api.translation.language.Language;





/**
 * Created by bolajiabiodun on 02/10/2016.
 */

public interface CommunicationFrag {

    void setNames(String firstName, String lastName);

    void setEmail(String email);

    void setUserName(String userName);

    void setPassword(String password);

    void prepareSignUp();

    void setPhoneNumber(String phoneNumber);

    void setCountry(String country);

    void setLanguage(Language language);

    void setStatus(String status);

}


