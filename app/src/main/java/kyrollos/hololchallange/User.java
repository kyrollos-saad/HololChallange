package kyrollos.hololchallange;

import android.net.Uri;

import java.util.HashMap;

public class User
{
    String firebaseUid = null;
    String email = null;
    String name = null;
    String type = null;
    String info = null;//for clinics only
    String phone1 = null;
    String phone2 = null;
    String phone3 = null;
    String photoUri = null;
    String profileComplete = "false";

    public User() {}

    User(String firebaseUid, String email)
    {
        this.firebaseUid = firebaseUid;
        this.email = email;
    }

    public User(String firebaseUid, String email, String name, String phone1, String phone2, String phone3, String type, Uri photoUri, boolean profileComplete)
    {
        this.firebaseUid = firebaseUid;
        this.email = email;
        this.name = name;
        this.phone1 = phone1;
        this.phone2 = phone2;
        this.phone3 = phone3;
        this.type = type;
        this.photoUri = photoUri.toString();
        this.profileComplete = String.valueOf(profileComplete);
    }

    public User(String firebaseUid, String email, String name, String phone1, String phone2, String phone3, String type, String photoUri, String profileComplete)
    {
        this.firebaseUid = firebaseUid;
        this.email = email;
        this.name = name;
        this.phone1 = phone1;
        this.phone2 = phone2;
        this.phone3 = phone3;
        this.type = type;
        this.photoUri = photoUri;
        this.profileComplete = profileComplete;
    }

    void refreshIsProfileCompleteVariable()
    {
        if (firebaseUid == null || email == null || name == null || type == null || phone1 == null)
        {
            profileComplete = "false";
        }
        else
            profileComplete = "true";

        if (type != null && type.equals("clinic") && info == null)
            profileComplete = "false";
    }

    //set

    public void setEmail(String email)
    {
        this.email = email;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setPhone1(String phone1) {this.phone1 = phone1;}
    public void setPhone2(String phone2) {this.phone1 = phone2;}
    public void setPhone3(String phone3) {this.phone1 = phone3;}

    public void setType(String type)
    {
        this.type = type;
    }

    public void setPhotoUri(Uri photoUri)
    {
        this.photoUri = photoUri.toString();
    }

    public void setProfileComplete(boolean profileComplete)
    {
        this.profileComplete = String.valueOf(profileComplete);
    }

    public void setInfo(String info)
    {
        this.info = info;
    }

    //get

    public String getInfo()
    {
        return info;
    }

    public String getFirebaseUid()
    {

        return firebaseUid;
    }

    public String getEmail()
    {
        return email;
    }

    public String getName()
    {
        return name;
    }

    public String getPhone1() {return phone1;}
    public String getPhone2() {return phone2;}
    public String getPhone3() {return phone3;}

    public String getType()
    {
        return type;
    }

    public Uri getPhotoUri()
    {
        if (photoUri == null)
            return null;
        return Uri.parse(photoUri);
    }

    public String isProfileComplete()
    {
        return profileComplete;
    }
    public boolean isProfileCompleteb()
    {
        return profileComplete.equals("true");
    }
}
