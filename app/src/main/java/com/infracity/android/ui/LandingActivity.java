package com.infracity.android.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.infracity.android.Constants;
import com.infracity.android.R;

/**
 * Created by pragadeesh on 17/12/16.
 */
public class LandingActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private SharedPreferences sharedPreferences;

    private static final int RESULT_SIGN_IN = 1001;

    GoogleApiClient googleApiClient;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        initGoogleClient();
        sharedPreferences = getSharedPreferences(Constants.PREFERENCE, MODE_PRIVATE);
    }

    private void initGoogleClient() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void signIn() {
        showProgressBar("Logging in...");
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signInIntent, RESULT_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == RESULT_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            Toast.makeText(this, "Sign in successful", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Sign in failed", Toast.LENGTH_SHORT).show();
            hideProgressBar();
        }
    }

    private void showProgressBar(String message) {
        if(progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            if(progressDialog.getWindow() != null ) {
                progressDialog.getWindow().setGravity(Gravity.BOTTOM);
            }
            progressDialog.setCancelable(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage(message);
        }
        progressDialog.show();
    }

    private void hideProgressBar() {
        if(progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}
