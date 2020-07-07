package com.cubenama.cubingcompanion;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.cardview.widget.CardView;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private SharedPreferences userDetailsSharedPreferences;

    private PopupWindow popupWindow;

    // Database reference
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create database instance
        db = FirebaseFirestore.getInstance();

        // Create reference to userDetailsReference details
        userDetailsSharedPreferences = getSharedPreferences("user_details", Context.MODE_PRIVATE);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ImageButton expandMenuButton = findViewById(R.id.expandMenuButton);

        // Create the navigation drawer
        mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_home, R.id.nav_profile, R.id.nav_faq)
                .setDrawerLayout(drawer)
                .build();

        DocumentReference userDetailsReference = db.collection(getString(R.string.db_field_name_user_details)).document(userDetailsSharedPreferences.getString("uid", ""));
        userDetailsReference.get().addOnCompleteListener(userDetailsTask -> {
            // Check if user's WCA ID is linked to the account
            if (!userDetailsTask.getResult().contains(getString(R.string.db_field_name_wca_id))) {
                Log.d("CC_ACCOUNT", "Linking account with WCA ID");
                // Get inflater for the current activity
                LayoutInflater inflater = getLayoutInflater();
                // Inflate the popup window layout (link WCA ID to account)
                View popupLinkAccountView = inflater.inflate(R.layout.popup_profile, null);
                // Setup popup window
                popupWindow = new PopupWindow(popupLinkAccountView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, true);
                // Show popup view at the center
                popupWindow.showAtLocation(popupLinkAccountView, Gravity.CENTER, 0, 0);
                // Deny access if account is not linked
                popupWindow.setOnDismissListener(() -> {
                            // Ensure WCA ID is unique
                           userDetailsReference.get().addOnCompleteListener(task -> {
                                if(task.getResult().getString(getString(R.string.db_field_name_wca_id)) == null)
                                {
                                    Toast.makeText(this, "Please link account with WCA ID to continue",Toast.LENGTH_LONG).show();
                                    // Sign out the current user
                                    FirebaseAuth.getInstance().signOut();
                                    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
                                    GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
                                    mGoogleSignInClient.signOut().addOnCompleteListener(this, task1 -> {
                                        // Start login activity
                                        new Handler().postDelayed(()->startActivity(new Intent(this, SignInActivity.class)),2000);
                                    });
                                }
                            });
                });

                EditText wcaIdEditText = popupLinkAccountView.findViewById(R.id.wcaIdEditText);
                EditText nameEditText = popupLinkAccountView.findViewById(R.id.nameEditText);
                EditText dobEditText = popupLinkAccountView.findViewById(R.id.dobEditText);
                EditText phoneEditText = popupLinkAccountView.findViewById(R.id.phoneEditText);

                // Fill out information obtained via Google Sign In
                if (userDetailsTask.getResult().get(getString(R.string.db_field_name_mobile)) != null) {
                    phoneEditText.setText(userDetailsTask.getResult().getString(getString(R.string.db_field_name_mobile)));
                }
                if (userDetailsTask.getResult().get(getString(R.string.db_field_name_name)) != null) {
                    nameEditText.setText(userDetailsTask.getResult().getString(getString(R.string.db_field_name_name)));
                }

                // Load spinning cube GIF
                ImageView loadingGifView = popupLinkAccountView.findViewById(R.id.loadingGifView);
                Glide.with(this).asGif().load(R.drawable.cube_loading_5).into(loadingGifView);

                // Create Listener for DOB
                ImageView dobButton = popupLinkAccountView.findViewById(R.id.selectDateButton);
                dobButton.setOnClickListener(v -> getDate(popupLinkAccountView.findViewById(R.id.dobEditText)));

                // Create listener for link account button
                CardView linkAccountButton = popupLinkAccountView.findViewById(R.id.linkAccountCardView);
                linkAccountButton.setOnClickListener(v -> {
                    String wcaId = String.valueOf(wcaIdEditText.getText());
                    String name = String.valueOf(nameEditText.getText());
                    String mobile = String.valueOf(phoneEditText.getText());
                    String dob = String.valueOf(dobEditText.getText());

                    // Some fields are invalid.
                    if (wcaId.equals("") || !isWcaIdValid(wcaId)) {
                        Toast.makeText(this, "Invalid WCA ID.", Toast.LENGTH_SHORT).show();
                    } else if (name.equals("") || !isNameValid(name)) {
                        Toast.makeText(this, "Name can only contain letters.", Toast.LENGTH_SHORT).show();
                    } else if (dob.equals("")) {
                        Toast.makeText(this, "Select Date of Birth.", Toast.LENGTH_SHORT).show();
                    } else if (mobile.equals("") || !isMobileValid(mobile)) {
                        Toast.makeText(this, "Invalid mobile number.", Toast.LENGTH_SHORT).show();
                    }
                    // All fields are valid.
                    else {
                        // Ensure WCA ID is unique
                        db.collection(getString(R.string.db_field_name_user_details)).whereEqualTo(getString(R.string.db_field_name_wca_id), wcaId).get()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        // WCA ID unique
                                        if (task.getResult().size() == 0) {
                                            Map<String, Object> accountDetails = new HashMap<>();
                                            accountDetails.put(getString(R.string.db_field_name_wca_id), wcaId);
                                            accountDetails.put(getString(R.string.db_field_name_name), name);
                                            accountDetails.put(getString(R.string.db_field_name_mobile), mobile);
                                            accountDetails.put(getString(R.string.db_field_name_dob), dob);

                                            userDetailsReference.update(accountDetails);
                                            Toast.makeText(this, "Account linked successfully.", Toast.LENGTH_SHORT).show();
                                            popupWindow.dismiss();
                                        }
                                        // WCA ID duplicate
                                        else {
                                            Toast.makeText(this, "WCA ID already registered.", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Log.d("CC_WCA_ID", "Error getting documents ", task.getException());
                                    }
                                });
                    }

                });
            }

        });

        // Add listener to update UI when data changes
        userDetailsReference.addSnapshotListener((snapshot, e) -> {
            // Check for exception
            if (e != null) {
                Log.w("CC_PROFILE_READ", "Unable to listen for data.", e);
                return;
            }
            if (snapshot != null && snapshot.exists()) {
                Log.d("CC_PROFILE_READ", "Data : " + snapshot.getData());
                updateUserDetailsUI(String.valueOf(snapshot.get(getString(R.string.db_field_name_name))), String.valueOf(snapshot.get(getString(R.string.db_field_name_wca_id))), String.valueOf(snapshot.get(getString(R.string.db_field_name_photo_url))));
            } else
                Log.d("CC_PROFILE_READ", "Data : null");
        });

        // Inflate the navigation drawer
        expandMenuButton.setOnClickListener(v -> mAppBarConfiguration.getDrawerLayout().openDrawer(Gravity.LEFT));

        NavController navController = Navigation.findNavController(this, R.id.dashboard_nav_host_fragment);
        NavigationUI.setupWithNavController(navigationView, navController);
    }


    // Function to load user details into UI
    private void updateUserDetailsUI(String name, String wcaId, String photoURL) {
        // Update userDetailsReference name
        TextView userNameTextView = findViewById(R.id.cuberNameTextView);
        TextView userNameTextViewAlt = findViewById(R.id.cuberNameTextViewAlt);
        userNameTextView.setText(name);
        userNameTextViewAlt.setText(name);

        // Update WCA ID
        TextView wcaIdTextView = findViewById(R.id.wcaIdTextView);
        TextView wcaIdTextViewAlt = findViewById(R.id.wcaIdTextViewAlt);
        wcaIdTextView.setText(wcaId);
        wcaIdTextViewAlt.setText(wcaId);

        // Update profile picture
        ImageView profilePictureImageView = findViewById(R.id.miniProfilePictureImageView);
        Glide.with(this).load(Uri.parse(photoURL)).into(profilePictureImageView);

        Log.d("CC_UPDATE_UI", "Name : " + name + " WCA ID : " + wcaId);
    }


    // Function to get date from DatePicker
    private void getDate(EditText dateEditText) {
        // Get Current Date
        final Calendar c = Calendar.getInstance();
        int currYear = c.get(Calendar.YEAR);
        int currMonth = c.get(Calendar.MONTH);
        int currDay = c.get(Calendar.DAY_OF_MONTH);

        // Setup DatePicker Dialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, monthOfYear, dayOfMonth) -> dateEditText.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year), currYear, currMonth, currDay);
        datePickerDialog.show();
    }


    // Function to check validity of WCA ID
    private boolean isWcaIdValid(String wcaId) {
        // ID has to be 10 characters long
        if (wcaId.length() != 10)
            return false;
        else {
            // Divide WCA ID into sub components
            String year = wcaId.substring(0, 4);
            String name = wcaId.substring(4, 8);
            String id = wcaId.substring(8, 10);

            // WCA ID FORMAT YYYY-ABCD-ID
            if (!year.matches("^[0-9]*$"))
                return false;
            if (!name.matches("^[A-Z]*$"))
                return false;
            return id.matches("^[0-9]*$");
        }
    }


    // Function to check validity of mobile number
    private boolean isMobileValid(String mobile) {
        // mobile number can only plus,dash and numbers
        if (!mobile.matches("^[-0-9+]*$"))
            return false;
        // ID has to be atleast 8 characters long
        return mobile.length() >= 8;
    }


    // Function to check validity of name
    private boolean isNameValid(String name) {
        // mobile number can only plus,dash and numbers
        return name.matches("^[A-Z a-z]*$");
    }


    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.dashboard_nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
    }


    // Function to handle back button presses
    @Override
    public void onBackPressed() {
        // Disable back button if popup window is showing
        if (popupWindow != null && popupWindow.isShowing())
            Toast.makeText(this, "Link Account with WCA ID to continue.", Toast.LENGTH_LONG).show();
        else
            super.onBackPressed();
    }
}