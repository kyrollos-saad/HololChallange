////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////                                      ////////////////////////////////
//////////////////////////                                      ////////////////////////////////
//////////////////////////  important note:                     ////////////////////////////////
//////////////////////////  i am retrieving ALL the clinics     ////////////////////////////////
//////////////////////////  and its info from the database.     ////////////////////////////////
//////////////////////////  i know this is inefficient but      ////////////////////////////////
//////////////////////////  it doesn't matter in this case      ////////////////////////////////
//////////////////////////  because the project scale is small  ////////////////////////////////
//////////////////////////                                      ////////////////////////////////
//////////////////////////                                     ////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////








package kyrollos.hololchallange;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class HomeActivity extends AppCompatActivity
{
    DatabaseReference usersRef;
    String uid;
    String provider;
    User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        uid = getIntent().getStringExtra("uid");

        Button signOutButt = findViewById(R.id.home_sign_out_butt);

        usersRef = FirebaseDatabase.getInstance().getReference().child("users");

        usersRef.child(uid).child("email").addValueEventListener(new ValueEventListener() {@Override public void onDataChange(@NonNull DataSnapshot dataSnapshot)
        {
            ((TextView)findViewById(R.id.home_usr_name_txt_vw)).setText(dataSnapshot.getValue().toString());
        }
        @Override public void onCancelled(@NonNull DatabaseError databaseError)
        {
        }});

        provider = getIntent().getStringExtra("provider");
        signOutButt.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v)
        {
            if (provider != null)
            {
                if (!provider.equals("l"))
                    FirebaseAuth.getInstance().signOut();

                SharedPreferences.Editor sharedPreferencesEditor = getSharedPreferences("last_login", MODE_PRIVATE).edit();
                sharedPreferencesEditor.remove("provider");
                sharedPreferencesEditor.commit();

                Intent intent = new Intent(HomeActivity.this, SignInUpActivity.class);

                finish();
            }
        }});

        currentUser = new User();
        usersRef.child(uid).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                for (DataSnapshot userField : dataSnapshot.getChildren())
                {
                    try
                    {
                        switch (userField.getKey())
                        {
                            case "email":
                                currentUser.setEmail(userField.getValue().toString());
                                break;
                            case "profileComplete":
                                currentUser.setProfileComplete(userField.getValue().equals("true"));
                                break;
                            case "type":
                                currentUser.setType(userField.getValue().toString());
                                break;
                            case "name":
                                currentUser.setName(userField.getValue().toString());
                                break;
                            case "info":
                                currentUser.setInfo(userField.getValue().toString());
                                break;
                            case "photoUri":
                                currentUser.setPhotoUri(Uri.parse(userField.getValue().toString()));
                                break;
                            case "phones":
                                HashMap<String,Object> phonesArrayList = new HashMap<>();

                                int counter = 1;
                                for (DataSnapshot phonesSnapShot : userField.getChildren())
                                {
                                    phonesArrayList.put("phone" + String.valueOf(counter), phonesSnapShot.getValue().toString());
                                    counter++;
                                }

                                currentUser.setPhone1(phonesArrayList.get("phone1").toString());
                                currentUser.setPhone3(phonesArrayList.get("phone2").toString());
                                currentUser.setPhone2(phonesArrayList.get("phone3").toString());
                                break;

                            default:
                                break;
                        }
                    }
                    catch (Exception e) {}
                }

                if (currentUser.getType() == null)
                {
                    final Dialog patientOrClinicDialog = new Dialog(HomeActivity.this);
                    patientOrClinicDialog.setContentView(R.layout.two_butts_layout);
                    patientOrClinicDialog.findViewById(R.id.tbl_patient_butt).setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v)
                    {
                        currentUser.setType("patient");
                        patientOrClinicDialog.cancel();
                        continueOnCreate();
                    }});
                    patientOrClinicDialog.findViewById(R.id.tbl_clinic_butt).setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v)
                    {
                        currentUser.setType("clinic");
                        patientOrClinicDialog.cancel();
                        continueOnCreate();
                    }});
                    patientOrClinicDialog.setCancelable(false);
                    patientOrClinicDialog.show();
                }
                else
                    continueOnCreate();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {popAnAlertDialogUp("Hey!", "Retrieving user's info process was canceled");}
        });


        //get the rest of the user info from the database
        //load image and name
        //if any of the info is missing, send the user to the complete profile activity
        //load all the clinics from the database
        //ask what type of clinic the user wants
        //show them accordingly
        //on click on a clinic a popup dialog appears showing the info and the option to chat
    }

    //this is a quick fix for a certain null pointer exception and could be fixed by wait() notify() approach
    void continueOnCreate()
    {
        currentUser.refreshIsProfileCompleteVariable();
        if (!currentUser.isProfileComplete().equals("true"))
        {
            //show complete profile dialog
            final Dialog completeProfileDialog = new Dialog(HomeActivity.this);

            completeProfileDialog.setContentView(R.layout.complete_profile_layout);

            View infoTxtVw = completeProfileDialog.findViewById(R.id.cpl_info_txt_vw);
            View submitButt = completeProfileDialog.findViewById(R.id.cpl_submit);
            final EditText emailEdtTxt = (EditText)completeProfileDialog.findViewById(R.id.cpl_email);
            final EditText nameEdtTxt = (EditText)completeProfileDialog.findViewById(R.id.cpl_name);
            final EditText phone1EdtTxt = (EditText)completeProfileDialog.findViewById(R.id.cpl_phone_1);
            final EditText phone2EdtTxt = (EditText)completeProfileDialog.findViewById(R.id.cpl_phone_2);
            final EditText phone3EdtTxt = (EditText)completeProfileDialog.findViewById(R.id.cpl_phone_3);
            final EditText infoEdtTxt = (EditText)completeProfileDialog.findViewById(R.id.cpl_info);

            if (currentUser.getType() != null && !currentUser.getType().equals("clinic"))
            {
                infoEdtTxt.setVisibility(View.GONE);
                infoTxtVw.setVisibility(View.GONE);
            }

            //populate views with pre-submitted values if any
            if (currentUser.getEmail() != null)
                emailEdtTxt.setText(currentUser.getEmail());
            if (currentUser.getName() != null)
                nameEdtTxt.setText(currentUser.getName());

            try
            {
                if (currentUser.getPhone1() != null)
                    phone1EdtTxt.setText(currentUser.getPhone1());
                if (currentUser.getPhone2() != null)
                    phone2EdtTxt.setText(currentUser.getPhone2());
                if (currentUser.getPhone3() != null)
                    phone3EdtTxt.setText(currentUser.getPhone3());
            }
            catch (Exception e){}

            if (currentUser.getType().equals("clinic"))
                if (currentUser.getInfo() != null)
                    infoEdtTxt.setText(currentUser.getInfo());

            //get data from views and submit them to the database
            submitButt.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v)
            {
                HashMap<String,Object> tempPhonesArray = new HashMap<>();

                if (!emailEdtTxt.getText().toString().equals(""))
                    currentUser.setEmail(emailEdtTxt.getText().toString());
                else
                {
                    popAnAlertDialogUp("Error", "You forgot to type your email");
                    return;
                }
                if (!nameEdtTxt.getText().toString().equals(""))
                    currentUser.setName(nameEdtTxt.getText().toString());
                else
                {
                    popAnAlertDialogUp("Error", "You forgot to type your name");
                    return;
                }

                if (!phone1EdtTxt.getText().toString().equals(""))
                    currentUser.setPhone1(phone1EdtTxt.getText().toString());
                else
                {
                    popAnAlertDialogUp("Error", "You have to type at least one contact number");
                    return;
                }
                if (!phone2EdtTxt.getText().toString().equals(""))
                    currentUser.setPhone2(phone2EdtTxt.getText().toString());
                if (!phone3EdtTxt.getText().toString().equals(""))
                    currentUser.setPhone3(phone3EdtTxt.getText().toString());

                if (currentUser.getType().equals("clinic"))
                {
                    if (!infoEdtTxt.getText().toString().equals(""))
                        currentUser.setInfo(infoEdtTxt.getText().toString());
                    else
                    {
                        popAnAlertDialogUp("Error", "You have to type some info about your clinic");
                        return;
                    }
                }

                HashMap<String, Object> currentUserMap = new HashMap<>();
                currentUser = new User(currentUser.getFirebaseUid(), currentUser.getEmail(), currentUser.getName(), currentUser.getPhone1(), currentUser.getPhone2(), currentUser.getPhone3(), currentUser.getType(), "", currentUser.isProfileComplete());
                currentUserMap.put(currentUser.getFirebaseUid(), currentUser);
                try
                {
                    usersRef.updateChildren(currentUserMap).addOnFailureListener(new OnFailureListener()
                    {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {
                            popAnAlertDialogUp("[debug] error", e.toString());
                        }
                    });
                }
                catch (Exception e)
                {
                    System.out.println("/// error" + e.toString());
                }

                completeProfileDialog.cancel();
            }});

            completeProfileDialog.setCancelable(false);
            completeProfileDialog.show();
        }
    }

    /*boolean backIsPressed = false;
    @Override
    public void onBackPressed()
    {
        if (!backIsPressed)
        {
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_LONG).show();
            backIsPressed = true;
        }
        else
        {
            ActivityCompat.finishAffinity(HomeActivity.this);
            finish();
        }
    }*/

    void popAnAlertDialogUp(Object title, Object msg)
    {
        if (title == null)
            title = "null";
        if (msg == null)
            msg = "null";
        new AlertDialog.Builder(this)
                .setTitle(title.toString())
                .setMessage(msg.toString())
                .show();
    }
}

/*


error exit app


popAnAlertDialogUp("Error", "Couldn't retrieve user's data.\nExiting...");

                        new Handler().postDelayed(new Runnable() {@Override public void run()
                        {
                            runOnUiThread(new Runnable() {@Override public void run()
                            {
                                ActivityCompat.finishAffinity(HomeActivity.this);
                                finish();
                            }});
                        }}, 3000);
 */