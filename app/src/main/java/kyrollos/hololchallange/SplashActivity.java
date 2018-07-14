package kyrollos.hololchallange;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;

import java.security.MessageDigest;

public class SplashActivity extends AppCompatActivity
{
    Context mContext = this;
    PackageInfo info;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        printhashkey();


        long splashScreenStartTime = System.currentTimeMillis();

        final Intent intent = new Intent(mContext, SignInUpActivity.class);

        //////////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////////
        /////////////  what i had in mind was that      //////////////////
        /////////////  this class checks for the last   //////////////////
        /////////////  login and do it automatically,   //////////////////
        /////////////  if there's none, then go to the  //////////////////
        /////////////  sign in activity, so this is a   //////////////////
        /////////////  temporary solution               //////////////////
        //////////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////////

        SharedPreferences lastLoginSharedPreferences = getSharedPreferences("last_login", MODE_PRIVATE);
        switch (lastLoginSharedPreferences.getString("provider", ""))
        {
            case "g":
                intent.putExtra("provider", "g");
                break;
            case "f":
                intent.putExtra("provider", "f");
                break;
            case "l":
                intent.putExtra("provider", "l");
                break;
            case "e":
                intent.putExtra("provider", "e");
                intent.putExtra("email", lastLoginSharedPreferences.getString("email", ""));
                intent.putExtra("pass", lastLoginSharedPreferences.getString("pass", ""));
                break;
            default:
                break;
        }

        //Intent homeIntent = new Intent(this, HomeActivity.class).putExtra();
        //startActivity();

        //else go to sign in/up activity
        //start the activity no sooner than 4 seconds from the beginning of this activity
        new Handler().postDelayed(new Runnable() {@Override public void run() {
            startActivity(intent);
        }}, 4000/* - System.currentTimeMillis() - splashScreenStartTime*/);
    }

    public void printhashkey()
    {
        try {
            info = getPackageManager().getPackageInfo("kyrollos.hololchallange", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md;
                md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String something = new String(Base64.encode(md.digest(), 0));
                //String something = new String(Base64.encodeBytes(md.digest()));
                System.out.println("/// " + something);
            }
        } catch (Exception e)
        {
            System.out.println("/// " + e.toString());
        }
    }
}
