package com.infracity.android.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
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
import com.infracity.android.model.User;
import com.infracity.android.rest.RestService;

import java.io.IOException;

import okhttp3.OkHttpClient;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by pragadeesh on 17/12/16.
 */
public class LandingActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private SharedPreferences sharedPreferences;

    private static final int RESULT_SIGN_IN = 1001;

    GoogleApiClient googleApiClient;
    RestService service;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences(Constants.PREFERENCE, MODE_PRIVATE);
        if(sharedPreferences.getBoolean(Constants.PREFERENCE_IS_LOGGED_IN, false)) {
            startMapActivity();
        } else {
            setContentView(R.layout.activity_sign_in);
            initGoogleClient();
            initRetrofit();
        }
    }

    private void initRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.72.33:3000")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        service = retrofit.create(RestService.class);
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
            if(acct != null) {
                User user = new User();
                user.setDisplayName(acct.getDisplayName());
                user.setEmail(acct.getEmail());
                SignInTask signInTask = new SignInTask();
                signInTask.execute(user);
            } else {
                hideProgressBar();
                Toast.makeText(this, "Couldn't fetch account details", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Sign in failed", Toast.LENGTH_SHORT).show();
            hideProgressBar();
        }
    }

    private class SignInTask extends AsyncTask<User, Void, String> {

        @Override
        protected String doInBackground(User... users) {
            String response = null;
            try {
                Response<User> userResponse = service.signIn(users[0].getEmail()
                        , users[0].getDisplayName()).execute();
                if(userResponse.code() == 201) {
                    User user = userResponse.body();
                    sharedPreferences.edit().putString(Constants.PREFERENCE_DISPLAY_NAME, user.getDisplayName()).apply();
                    sharedPreferences.edit().putString(Constants.PREFERENCE_EMAIL, user.getEmail()).apply();
                    sharedPreferences.edit().putString(Constants.PREFERENCE_UID, user.getUUID()).apply();
                    sharedPreferences.edit().putBoolean(Constants.PREFERENCE_IS_LOGGED_IN, true).apply();
                } else {
                    response = userResponse.message();
                }
            } catch (IOException e) {
                response = e.getMessage();
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String error) {
            if(TextUtils.isEmpty(error)) {
                startMapActivity();
                Toast.makeText(LandingActivity.this, "Sign successful", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(LandingActivity.this, "Sign failed - " + error, Toast.LENGTH_SHORT).show();
            }
            hideProgressBar();
            super.onPostExecute(error);
        }
    }

    private void startMapActivity() {
        Intent intent = new Intent(LandingActivity.this, MapsActivity.class);
        startActivity(intent);
        finish();
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
