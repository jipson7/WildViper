package ca.uoit.caleb.wildviper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;

public class LoginActivity extends FragmentActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private GoogleApiClient mGoogleApiClient;

    public static final int RC_SIGNIN = 9021;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        /**
         * Request basic profile info and email
         */
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .build();

        /**
         * Build a GoogleApiClient with access to the Google Sign-In API and the
         * options specified by gso.
         */
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        setupSigninButton();
    }

    /**
     * Attach Click handler to sign in button and increase size
     */
    private void setupSigninButton() {
        SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(this);
        signInButton.setSize(SignInButton.SIZE_WIDE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        /**
         * Check to see if user has already signed in.
         */
        OptionalPendingResult<GoogleSignInResult> pendingResult =
                Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (pendingResult.isDone()) {
            // Credentials are available from previous signin
            handleSignInResult(pendingResult.get(), false);
        } else {
            // No immediate credentials available, fetch them asynchronously
            showProgressIndicator();
            pendingResult.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(@NonNull GoogleSignInResult result) {
                    handleSignInResult(result, false);
                    hideProgressIndicator();
                }
            });
        }
    }

    private void showProgressIndicator() {
        ProgressBar bar = (ProgressBar) findViewById(R.id.loading_indicator);
        bar.setVisibility(View.VISIBLE);
    }

    private void hideProgressIndicator() {
        ProgressBar bar = (ProgressBar) findViewById(R.id.loading_indicator);
        bar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //TODO implement
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
        }
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGNIN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode) == RC_SIGNIN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result, true);
        }
    }

    private void handleSignInResult(GoogleSignInResult result, boolean firstSignIn) {
        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();
            String resultMessage = acct.getDisplayName() + " signed in successfully.";
            saveUserData(acct);
            if (firstSignIn) {
                Toast.makeText(getApplicationContext(), resultMessage, Toast.LENGTH_LONG).show();
            }
            startMainActivity();
        }
    }

    /**
     * Save data from Google sign in to shared preferences
     * @param acct The Google Account object
     */
    private void saveUserData(GoogleSignInAccount acct) {
        Context context = getApplicationContext();
        UserData.saveData(acct, context);
    }

    private void startMainActivity() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
    }
}
