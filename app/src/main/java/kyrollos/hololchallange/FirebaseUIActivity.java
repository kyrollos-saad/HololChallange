//TODO: use strings.xml

package kyrollos.hololchallange;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class FirebaseUIActivity extends AppCompatActivity
{
    Context mContext = this;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebase_ui);

        /*firebaseAuth = FirebaseAuth.getInstance();
        final FirebaseUser firebaseCurrentUser = firebaseAuth.getCurrentUser();

        ((Button)findViewById(R.id.sign_up_butt)).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final Dialog emailPassDialogBuilder = new Dialog(mContext);
                emailPassDialogBuilder.setContentView(R.layout.email_pass_layout);
                ((TextView)emailPassDialogBuilder.findViewById(R.id.email_pass_dialog_lbl_txt_vw)).setText("Sign up");
                ((Button)emailPassDialogBuilder.findViewById(R.id.email_pass_submit_butt)).setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        String newEmail = ((EditText)emailPassDialogBuilder.findViewById(R.id.email_edt_txt)).getText().toString();
                        String newPass = ((EditText)emailPassDialogBuilder.findViewById(R.id.pass_edt_txt)).getText().toString();

                        if (newEmail.equals(""))
                        {
                            popAnAlertDialogUp("Error", "Make sure to type an email");
                            return;
                        }
                        if (newPass.equals(""))
                        {
                            popAnAlertDialogUp("Error", "Make sure to type a password");
                            return;
                        }

                        Task<AuthResult> signUpTask = firebaseAuth.createUserWithEmailAndPassword(newEmail, newPass);
                        signUpTask.addOnSuccessListener(new OnSuccessListener<AuthResult>() {@Override public void onSuccess(AuthResult authResult) {
                                popAnAlertDialogUp("Hey!", "Done");
                            }});
                        signUpTask.addOnFailureListener(new OnFailureListener() {@Override public void onFailure(@NonNull Exception e) {
                                popAnAlertDialogUp("Error", e.getMessage());
                            }});

                        emailPassDialogBuilder.cancel();
                    }
                });
                emailPassDialogBuilder.show();
            }
        });

        ((Button)findViewById(R.id.sign_in_butt)).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final Boolean[] keepDialog = {false};
                final Dialog emailPassDialogBuilder = new Dialog(mContext);
                emailPassDialogBuilder.setContentView(R.layout.email_pass_layout);
                ((TextView)emailPassDialogBuilder.findViewById(R.id.email_pass_dialog_lbl_txt_vw)).setText("Sign in");
                emailPassDialogBuilder.findViewById(R.id.email_pass_submit_butt).setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        String email = ((EditText)emailPassDialogBuilder.findViewById(R.id.email_edt_txt)).getText().toString();
                        String pass = ((EditText)emailPassDialogBuilder.findViewById(R.id.pass_edt_txt)).getText().toString();

                        //safety checks
                        if (email.equals(""))
                        {
                            popAnAlertDialogUp("Error", "Make sure to type an email");
                            return;
                        }
                        if (pass.equals(""))
                        {
                            popAnAlertDialogUp("Error", "Make sure to type a password");
                            return;
                        }

                        Task<AuthResult> signUpTask = firebaseAuth.signInWithEmailAndPassword(email, pass);

                        //notifying the user with the results
                        signUpTask.addOnSuccessListener(new OnSuccessListener<AuthResult>() {@Override public void onSuccess(AuthResult authResult) {
                            popAnAlertDialogUp("Hey!", "Done");
                        }});
                        signUpTask.addOnFailureListener(new OnFailureListener() {@Override public void onFailure(@NonNull Exception e) {
                            popAnAlertDialogUp("Error", e.getMessage());
                            keepDialog[0] = true;
                        }});
                        if (keepDialog[0])//if it's a bad email/password format, don't close it, let them try again
                        {
                            keepDialog[0] = false;
                            return;
                        }
                        emailPassDialogBuilder.cancel();
                    }
                });
                emailPassDialogBuilder.show();
            }
        });
        ((Button)findViewById(R.id.sign_out_butt)).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AuthUI.getInstance().signOut(mContext).addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        popAnAlertDialogUp("Sign out", "Done!" + "\n" + task.toString());
                    }
                });
            }
        });*/
    }

    /*@SuppressLint("RestrictedApi")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_FIRST_USER)
            popAnAlertDialogUp("int resultCode", "first user?");
        else if (resultCode == RESULT_CANCELED)
            popAnAlertDialogUp("int resultCode", "Login Canceled");

        else if (resultCode == RESULT_OK)
        {
            IdpResponse idpResponse = (IdpResponse)data.getExtras().get("extra_idp_response");
            if (idpResponse == null)
            {
                popAnAlertDialogUp("Error", "Could not retrieve user info");
                return;
            }
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
