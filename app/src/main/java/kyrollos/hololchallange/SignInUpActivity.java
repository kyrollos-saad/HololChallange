package kyrollos.hololchallange;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.appevents.AppEventsLogger;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.linkedin.platform.APIHelper;
import com.linkedin.platform.AccessToken;
import com.linkedin.platform.LISessionManager;
import com.linkedin.platform.errors.LIApiError;
import com.linkedin.platform.errors.LIAuthError;
import com.linkedin.platform.listeners.ApiListener;
import com.linkedin.platform.listeners.ApiResponse;
import com.linkedin.platform.listeners.AuthListener;
import com.linkedin.platform.utils.Scope;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class SignInUpActivity extends AppCompatActivity
{
    int GOOGLE_SIGN_IN_REQUEST_CODE = 21;
    int FACEBOOK_SIGN_IN_REQUEST_CODE = 64206;
    int LINKDIN_SIGN_IN_REQUEST_CODE = 23;

    Context mContext = this;
    CallbackManager fbCallbackManager;
    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;
    DatabaseReference usersRef;
    SharedPreferences.Editor lastLoginSharedPreferenceEditor;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in_up);

        final EditText emailEdtTxt = (EditText)findViewById(R.id.login_email_edt_txt);
        final EditText passEdtTxt = findViewById(R.id.login_pass_edt_txt);
        Button signInButt = findViewById(R.id.login_signin_butt);
        Button signUpButt = findViewById(R.id.login_signup_butt);
        TextView forgorPassTxtVw = findViewById(R.id.login_forgot_pass_txt_vw);
        ImageView gglButt = findViewById(R.id.login_ggl_img_butt);
        ImageView fbButt = findViewById(R.id.login_fb_img_butt);
        ImageView lnkdnButt = findViewById(R.id.login_lnkdn_img_butt);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        usersRef = databaseReference.child("users");

        lastLoginSharedPreferenceEditor = getSharedPreferences("last_login", MODE_PRIVATE).edit();

        ////////////////// sign in/up with social media //////////////////
        //1- google
        gglButt.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v)
        {
            GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.web_client_id))
                    .requestEmail()
                    .build();
            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(mContext, googleSignInOptions);
            Intent signInIntent = googleSignInClient.getSignInIntent();
            ((Activity)mContext).startActivityForResult(signInIntent, GOOGLE_SIGN_IN_REQUEST_CODE);
        }});

        //2- facebook
        fbButt.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v)
        {
            List<String> permissionsToRequest = Arrays.asList("email", "public_profile");
            LoginManager.getInstance().logInWithReadPermissions((Activity)mContext, permissionsToRequest);

            fbCallbackManager = CallbackManager.Factory.create();
            LoginManager.getInstance().registerCallback(fbCallbackManager, new FacebookCallback<LoginResult>()
            {
                @Override
                public void onSuccess(LoginResult loginResult)
                {
                    final Profile[] fbProfile = {Profile.getCurrentProfile()};

                    if (fbProfile[0] == null)
                    {
                        ProfileTracker profileTracker = new ProfileTracker() {@Override protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile)
                        {
                            fbProfile[0] = currentProfile;
                            System.out.println("/// old profile: " + oldProfile.getName());
                            System.out.println("/// current profile: " + currentProfile.getName());
                        }};
                    }

                    GraphRequest fbGraphRequest = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {@Override public void onCompleted(JSONObject object, GraphResponse response)
                    {
                        System.out.println("/// GraphRequest response: " + response.toString());

                        try
                        {
                            System.out.println("/// GraphRequest email: " + object.getString("email"));
                            System.out.println("/// \n\nthe whole JSON object\n\n\n" + object.toString());
                        }
                        catch (JSONException e)
                        {
                            System.out.println("/// error: " + e.toString());
                        }
                    }});

                    //get facebook credential
                    AuthCredential fbCredential = FacebookAuthProvider.getCredential(loginResult.getAccessToken().getToken());
                    //attempt to sign in using facebook credential
                    final Task<AuthResult> fbSigninTask = firebaseAuth.signInWithCredential(fbCredential);
                    //on success
                    fbSigninTask.addOnSuccessListener(new OnSuccessListener<AuthResult>() {@Override public void onSuccess(AuthResult authResult)
                    {
                        HashMap<String, Object> newFacebookUser = new HashMap<>();
                        newFacebookUser.put(authResult.getUser().getUid(), new User(authResult.getUser().getUid(), authResult.getUser().getEmail()));
                        usersRef.updateChildren(newFacebookUser);
                        lastLoginSharedPreferenceEditor.putString("provider", "f").apply();

                        if (progressDialog != null)
                            progressDialog.cancel();

                        goToHomeActivity(authResult.getUser().getUid(), 'e');
                    }});
                    //on failure
                    fbSigninTask.addOnFailureListener(new OnFailureListener() {@Override public void onFailure(@NonNull Exception e)
                    {
                        popAnAlertDialogUp("Error", e.getMessage());
                    }});
                }

                @Override
                public void onCancel()
                {
                    Toast.makeText(mContext, "Login Canceled", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(FacebookException error)
                {
                    popAnAlertDialogUp("Error", error.getMessage());
                }
            });
        }});

        //3- linked in
        lnkdnButt.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v)
        {
            LISessionManager.getInstance(mContext).init((Activity) mContext, Scope.build(Scope.R_BASICPROFILE, Scope.R_EMAILADDRESS), new AuthListener()
            {
                @Override
                public void onAuthSuccess()
                {
                    //get the credential using REST API
                    APIHelper liApiHelper = new APIHelper();
                    String restUrl = "https://api.linkedin.com/v1/people/~:(id,first-name,last-name,picture-url,email-address)";
                    liApiHelper.getRequest(mContext, restUrl, new ApiListener()
                    {
                        @Override
                        public void onApiSuccess(ApiResponse apiResponse)
                        {
                            JSONObject responseJson = apiResponse.getResponseDataAsJson();
                            String s = "";
                            try
                            {
                                s += responseJson.get("id").toString() + "\n";
                                s += responseJson.get("first-name").toString() + "\n";
                                s += responseJson.get("last-name").toString() + "\n";
                                s += responseJson.get("picture-url").toString() + "\n";
                                s += responseJson.get("email-address").toString();

                                popAnAlertDialogUp("[debug]", s);
                            }
                            catch (JSONException e) {popAnAlertDialogUp("Error", e.getMessage());}
                        }
                        @Override
                        public void onApiError(LIApiError LIApiError) {popAnAlertDialogUp("Error", LIApiError.toString());}
                    });
                }
                @Override
                public void onAuthError(LIAuthError error) { popAnAlertDialogUp("Error", error.toString()); }
            }, true);
        }});


        ////////////////// email forgot password //////////////////
        forgorPassTxtVw.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v)
        {
            final Dialog emailDialog = new Dialog(mContext);
            //set dialog layout
            emailDialog.setContentView(R.layout.email_pass_layout);
            //reference child views
            TextView dialogLbl = (TextView)emailDialog.findViewById(R.id.email_pass_dialog_lbl_txt_vw);
            final EditText emailEdtTxt = (EditText)emailDialog.findViewById(R.id.email_edt_txt);
            Button submitButt = emailDialog.findViewById(R.id.email_pass_submit_butt);
            //hide the account type and password fields
            emailDialog.findViewById(R.id.pass_edt_txt).setVisibility(View.GONE);

            dialogLbl.setText("Reset Password");

            //when this line was here, the variable was empty, but when i moved it inside the on click it worked, i have no idea why TODO:investigate
            //final String emailToRecover = emailEdtTxt.getText().toString();

            //send reset password request to Firebase
            submitButt.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v)
            {
                String emailToRecover = emailEdtTxt.getText().toString();
                //safety check
                if (emailToRecover.equals(""))
                {
                    popAnAlertDialogUp("Error", "Make sure to type an email");
                    return;
                }
                //send password reset request
                Task<Void> passwordResetTask = firebaseAuth.sendPasswordResetEmail(emailToRecover);
                //on success
                passwordResetTask.addOnSuccessListener(new OnSuccessListener<Void>() {@Override public void onSuccess(Void aVoid)
                {
                    popAnAlertDialogUp("Done", "Check your email to reset your password");
                }});
                //on failure
                passwordResetTask.addOnFailureListener(new OnFailureListener() {@Override public void onFailure(@NonNull Exception e)
                {
                    popAnAlertDialogUp("Error", e.getMessage().toString() + "\nTry Again");
                }});
            }});
            emailDialog.show();
        }});

        ////////////////// sign in with email //////////////////
        signInButt.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v)
        {
            final String email = emailEdtTxt.getText().toString();
            final String pass = passEdtTxt.getText().toString();

            //safety checks
            if (email.equals("") || pass.equals(""))
            {
                popAnAlertDialogUp("Error", "Make sure to type your email and password");
                return;
            }

            final Task<AuthResult> signInTask = firebaseAuth.signInWithEmailAndPassword(email, pass);

            //on success
            signInTask.addOnSuccessListener(new OnSuccessListener<AuthResult>() {@Override public void onSuccess(AuthResult authResult)
            {
                //in case the user didn't verify their email
                if (!authResult.getUser().isEmailVerified())
                {
                    popAnAlertDialogUp("Error", "You need to verify the email before using the app. Check your email inbox (or spam)");
                    return;
                }
                //go to home
                lastLoginSharedPreferenceEditor.putString("provider", "e").apply();
                lastLoginSharedPreferenceEditor.putString("email", email).apply();
                lastLoginSharedPreferenceEditor.putString("pass", pass).apply();

                if (progressDialog != null)
                    progressDialog.cancel();

                goToHomeActivity(authResult.getUser().getUid(), 'e');
            }});
            //on failure
            signInTask.addOnFailureListener(new OnFailureListener() {@Override public void onFailure(@NonNull Exception e)
            {
                popAnAlertDialogUp("Error", e.getMessage());
            }});
        }});

        ////////////////// sign up with email //////////////////
        signUpButt.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v)
        {
            final boolean[] keepDialog = {false};
            final Dialog emailPassDialog = new Dialog(mContext);
            emailPassDialog.setContentView(R.layout.email_pass_layout);
            ((TextView) emailPassDialog.findViewById(R.id.email_pass_dialog_lbl_txt_vw)).setText("Sign up");

            emailPassDialog.findViewById(R.id.email_pass_submit_butt).setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v)
            {
                String newEmail = ((EditText) emailPassDialog.findViewById(R.id.email_edt_txt)).getText().toString();
                String newPass = ((EditText) emailPassDialog.findViewById(R.id.pass_edt_txt)).getText().toString();

                //safety checks
                if (newEmail.equals("") || newPass.equals(""))
                {
                    popAnAlertDialogUp("Error", "Make sure to type your new email and password");
                    return;
                }

                //attempt to create an account with the given credentials
                Task<AuthResult> signUpTask = firebaseAuth.createUserWithEmailAndPassword(newEmail, newPass);

                //on success
                signUpTask.addOnSuccessListener(new OnSuccessListener<AuthResult>() {@Override public void onSuccess(AuthResult authResult)
                {
                    //init a new user
                    //firebase user id and patient data(including a redundant firebase user id again
                    HashMap<String, Object> newUserHashMap = new HashMap<>();
                    newUserHashMap.put(authResult.getUser().getUid(), new User(authResult.getUser().getUid(), authResult.getUser().getEmail()));
                    //update database
                    usersRef.updateChildren(newUserHashMap);
                    //send verification email
                    authResult.getUser().sendEmailVerification();
                    //notify the user
                    if (authResult.getUser().isEmailVerified())
                        popAnAlertDialogUp("Done", "Welcome back!");
                    else
                        popAnAlertDialogUp("Done", "You need to verify the email before using the app. Check your email inbox (or spam)");
                }});

                //on failure
                signUpTask.addOnFailureListener(new OnFailureListener() {@Override public void onFailure(@NonNull Exception e)
                {
                    popAnAlertDialogUp("Error", e.getMessage().toString() + "\nTry again");
                    keepDialog[0] = true;
                }});

                if (keepDialog[0])
                {
                    keepDialog[0] = false;
                    return;
                }
                emailPassDialog.cancel();
            }});
            emailPassDialog.show();
        }});


        /////////////////// check for last logins ///////////////////
        String lastLogin = getIntent().getStringExtra("provider");
        if (lastLogin != null)
        {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Loading most recent profile...");
            progressDialog.setCancelable(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();

            switch (lastLogin)
            {
                case "g":
                    gglButt.callOnClick();
                    break;
                case "f":
                    fbButt.callOnClick();
                    break;
                case "l":
                    lnkdnButt.callOnClick();
                    break;
                case "e":
                    String email = getIntent().getStringExtra("email");
                    String pass = getIntent().getStringExtra("pass");
                    emailEdtTxt.setText(email);
                    passEdtTxt.setText(pass);
                    signInButt.callOnClick();
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (fbCallbackManager != null)
            fbCallbackManager.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GOOGLE_SIGN_IN_REQUEST_CODE)
        {
            Task<GoogleSignInAccount> googleSignInTask = GoogleSignIn.getSignedInAccountFromIntent(data);
            try
            {
                GoogleSignInAccount googleSignInAccount = googleSignInTask.getResult();
                //authenticate firebase with google

                //get the idToken (per account unlike uid which is unique per the current firebase project only
                String userIdToken = googleSignInAccount.getIdToken();
                //get firebase compatible credentials
                final AuthCredential authCredential = GoogleAuthProvider.getCredential(userIdToken, null);
                //attempt to connect to firebase with those credentials
                Task<AuthResult> firebaseSignInWithGoogleCredential = firebaseAuth.signInWithCredential(authCredential);
                //on success
                firebaseSignInWithGoogleCredential.addOnSuccessListener(new OnSuccessListener<AuthResult>() {@Override public void onSuccess(AuthResult authResult)
                {
                    HashMap<String, Object> newGoogleUser = new HashMap<>();
                    newGoogleUser.put(authResult.getUser().getUid(), new User(authResult.getUser().getUid(), authResult.getUser().getEmail()));
                    usersRef.updateChildren(newGoogleUser);
                    lastLoginSharedPreferenceEditor.putString("provider", "g").apply();

                    if (progressDialog != null)
                        progressDialog.cancel();

                    goToHomeActivity(authResult.getUser().getUid(), 'g');
                }});
            }
            catch (Exception e) { popAnAlertDialogUp("Error", "Could not sign in with Google"); }
        }
        else if (requestCode == FACEBOOK_SIGN_IN_REQUEST_CODE)
        {
            //implementation is at LoginManager.getInstance().registerCallback in the onCreate
            //because el beh msh 3awez yb3at requestCode zy el nas w 3awez el callback bta3to howa
        }
        else if (requestCode == LINKDIN_SIGN_IN_REQUEST_CODE)
        {

        }
    }

    void goToHomeActivity(String uid, char provider)//provider could be g or f or l
    {
        //TODO: implement
        //create intent -> put extra uid -> start activity
        Intent homeIntent = new Intent(this, HomeActivity.class).putExtra("uid", uid).putExtra("provider", String.valueOf(provider));
        startActivity(homeIntent);
    }

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
