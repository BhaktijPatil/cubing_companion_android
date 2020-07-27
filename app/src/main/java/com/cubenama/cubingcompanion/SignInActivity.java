package com.cubenama.cubingcompanion;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignInActivity extends AppCompatActivity {

    // Time for which splash screen should be shown
    private static final int SPLASH_SCREEN_TIME = 3000;
    private static final long VERSION_ID = 8;
    private static final int RC_SIGN_IN = 69;

    private FirebaseAuth mAuth;

    // Database reference
    private FirebaseFirestore db;

    // Shared preferences to store user details
    SharedPreferences userDetailsSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // Create database instance
        db = FirebaseFirestore.getInstance();

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Create reference to cuber details
        userDetailsSharedPreferences = getSharedPreferences("user_details", Context.MODE_PRIVATE);

        // Load spinning cube GIF
        ImageView loadingGifView = findViewById(R.id.loadingGifView);
        Glide.with(this).asGif().load(R.drawable.cube_loading_1).into(loadingGifView);

        // Request app permissions permission
        new Handler().postDelayed(this::requestPermissions, SPLASH_SCREEN_TIME);
    }



    // Function to authenticate cuber using Google sign-in
    private void authenticateUser()
    {
        // User is already signed in
        if(mAuth.getCurrentUser() != null)
        {
            // Add account UID to shared preferences
            userDetailsSharedPreferences.edit().putString("uid", mAuth.getCurrentUser().getUid()).apply();
            // Initiate user dashboard
            startActivity(new Intent(SignInActivity.this, MainActivity.class));
            // Finish sign-in activity
            finish();

        }
        // User hasn't already signed in
        else
        {
            // Configure Google sign-in
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();

            GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

            // Start activity for Sign-in
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        }
    }



    // Function to handle result of google sign-in popup
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign successful (authenticate with Firebase)
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d("CC_USER_LOGIN", "firebaseAuthWithGoogle:" + account.getId());
                // Exchange google sign in token for firebase auth token
                firebaseAuthWithGoogle(account.getIdToken());
            }
            catch (ApiException e) {
                Log.w("CC_USER_LOGIN", "Google sign in failed", e);
                // User clicks outside the Sign In popup
                if(e.getStatusCode() == 12501)
                    Toast.makeText(SignInActivity.this, "Please sign-in to continue", Toast.LENGTH_SHORT).show();
                // Restart authentication in case of failure
                authenticateUser();
            }
        }
    }



    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, signInTask -> {
                    if (signInTask.isSuccessful()) {
                        // Firebase authentication successful
                        Log.d("CC_USER_LOGIN", "Firebase Authentication successful");
                        // Firebase user
                        FirebaseUser user = mAuth.getCurrentUser();
                        // Add account UID to shared preferences
                        userDetailsSharedPreferences.edit().putString("uid", user.getUid()).apply();

                        // Check if account UID exists in database
                        DocumentReference userDetailsReference = db.collection(getString(R.string.db_field_name_user_details)).document(userDetailsSharedPreferences.getString("uid",""));
                        userDetailsReference.get().addOnCompleteListener(userDetailsTask -> {
                            // Add account to firebase if it does not exist
                            if(!userDetailsTask.getResult().exists()) {
                                // Get new user's details from firebase
                                Map<String, Object> newCuber = new HashMap<>();
                                newCuber.put(getString(R.string.db_field_name_name), user.getDisplayName());
                                newCuber.put(getString(R.string.db_field_name_email), user.getEmail());
                                newCuber.put(getString(R.string.db_field_name_mobile), user.getPhoneNumber());
                                newCuber.put(getString(R.string.db_field_name_photo_url), user.getPhotoUrl().toString());
                                userDetailsReference.set(newCuber);
                            }
                            // Initiate user dashboard
                            startActivity(new Intent(SignInActivity.this, MainActivity.class));
                            // Finish sign-in activity
                            finish();
                        });
                    }
                    else {
                        // Firebase authentication unsuccessful
                        Log.w("CC_USER_LOGIN", "Firebase Authentication Failed", signInTask.getException());
                        Toast.makeText(this, "Sign In error. Please try again.", Toast.LENGTH_SHORT).show();
                        // Restart authentication in case of failure
                        authenticateUser();
                    }
                });
    }



    // Function to request application permissions
    private void requestPermissions()
    {
        // Check if current application version is valid
        db.collection(getString(R.string.db_field_name_app_details)).document(getString(R.string.db_field_name_app_version)).get().addOnCompleteListener(appVersionTask -> {
           if(appVersionTask.getResult().getLong(getString(R.string.db_field_name_min_version_id)) <= VERSION_ID)
               // No permissions needed yet
               authenticateUser();
           else 
           {
               Toast.makeText(this, "A newer version is available. Please update to continue.", Toast.LENGTH_LONG).show();
               new Handler().postDelayed(this::finish, SPLASH_SCREEN_TIME);
           }
        });
        
    }
}