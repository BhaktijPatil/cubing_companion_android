package com.cubenama.cubingcompanion;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
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
import com.google.android.material.navigation.NavigationView;
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

    NavigationView navigationView;

    // Database reference
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create database instance
        db = FirebaseFirestore.getInstance();

        // Create reference to cuber details
        userDetailsSharedPreferences = getSharedPreferences("user_details", Context.MODE_PRIVATE);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        ImageButton expandMenuButton = findViewById(R.id.expandMenuButton);

        // Create the navigation drawer
        mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_home, R.id.nav_profile, R.id.nav_faq)
                .setDrawerLayout(drawer)
                .build();

        DocumentReference cuber = db.collection("cuber_details").document(userDetailsSharedPreferences.getString("uid",""));
        cuber.get().addOnCompleteListener(cuberTask -> {
            DocumentSnapshot cuberDocumentSnapshot = cuberTask.getResult();

            // Check if user's WCA ID is linked to the account
            if(!cuberDocumentSnapshot.contains("wca_id")) {
                Log.d("CC_ACCOUNT", "Linking account with WCA ID");
                // Get inflater for the current activity
                LayoutInflater inflater = getLayoutInflater();
                // Inflate the popup window layout (link WCA ID to account)
                View popupLinkAccountView = inflater.inflate(R.layout.popup_profile, null);
                // Setup popup window
                PopupWindow popupWindow = new PopupWindow(popupLinkAccountView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, true);
                // Show popup view at the center
                popupWindow.showAtLocation(popupLinkAccountView, Gravity.CENTER, 0,0);

                EditText wcaIdEditText = popupLinkAccountView.findViewById(R.id.wcaIdEditText);
                EditText nameEditText = popupLinkAccountView.findViewById(R.id.nameEditText);
                EditText dobEditText = popupLinkAccountView.findViewById(R.id.dobEditText);
                EditText phoneEditText = popupLinkAccountView.findViewById(R.id.phoneEditText);

                // Fill out information obtained via Google Sign In
                if(cuberDocumentSnapshot.get("mobile") != null)
                {
                    phoneEditText.setText(cuberDocumentSnapshot.get("mobile").toString());
                }
                if(cuberDocumentSnapshot.get("name") != null)
                {
                    nameEditText.setText(cuberDocumentSnapshot.get("name").toString());
                }

                // Load spinning cube GIF
                ImageView loadingGifView = popupLinkAccountView.findViewById(R.id.loadingGifView);
                Glide.with(this).asGif().load(R.drawable.cube_loading_5).into(loadingGifView);

                // Create Listener for DOB
                ImageButton dobButton = popupLinkAccountView.findViewById(R.id.selectDateButton);
                dobButton.setOnClickListener(v->getDate(popupLinkAccountView.findViewById(R.id.dobEditText)));

                // Create listener for link account button
                CardView linkAccountButton = popupLinkAccountView.findViewById(R.id.linkAccountCardView);
                linkAccountButton.setOnClickListener(v->{
                    String wcaId = String.valueOf(wcaIdEditText.getText());
                    String name = String.valueOf(nameEditText.getText());
                    String mobile = String.valueOf(phoneEditText.getText());
                    String dob = String.valueOf(dobEditText.getText());

                    // Some fields are invalid.
                    if(wcaId.equals("") || !isWcaIdValid(wcaId))
                    {
                        Toast.makeText(this, "Invalid WCA ID.", Toast.LENGTH_SHORT).show();
                    }
                    else if(name.equals("") || !isNameValid(name))
                    {
                        Toast.makeText(this, "Name can only contain letters.", Toast.LENGTH_SHORT).show();
                    }
                    else if(dob.equals(""))
                    {
                        Toast.makeText(this, "Select Date of Birth.", Toast.LENGTH_SHORT).show();
                    }
                    else if(mobile.equals("") || !isMobileValid(mobile))
                    {
                        Toast.makeText(this, "Invalid mobile number.", Toast.LENGTH_SHORT).show();
                    }
                    // All fields are valid.
                    else
                    {
                        // Ensure WCA ID is unique
                        db.collection("cuber_details").whereEqualTo("wca_id", wcaId).get()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        // WCA ID unique
                                        if(task.getResult().size() == 0)
                                        {
                                            Map<String, Object> accountDetails = new HashMap<>();
                                            accountDetails.put("wca_id", wcaId);
                                            accountDetails.put("name", name);
                                            accountDetails.put("mobile", mobile);
                                            accountDetails.put("dob", dob);

                                            db.collection("cuber_details").document(userDetailsSharedPreferences.getString("uid","")).update(accountDetails);
                                            Toast.makeText(this, "Account linked successfully.", Toast.LENGTH_SHORT).show();
                                            popupWindow.dismiss();
                                        }
                                        // WCA ID duplicate
                                        else
                                        {
                                            Toast.makeText(this, "WCA ID already registered.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    else {
                                        Log.d("CC_WCA_ID", "Error getting documents ", task.getException());
                                    }
                                });
                    }

                });
            }

        });

        // Add listener to update UI when data changes
        cuber.addSnapshotListener((snapshot, e) -> {
            // Check for exception
            if (e != null) {
                Log.w("CC_PROFILE_READ", "Unable to listen for data.", e);
                return;
            }
            if (snapshot != null && snapshot.exists()) {
                Log.d("CC_PROFILE_READ",  "Data : " + snapshot.getData());
                updateUserDetailsUI(String.valueOf(snapshot.get("name")), String.valueOf(snapshot.get("wca_id")), String.valueOf(snapshot.get("photo_url")));
                }
            else
                Log.d("CC_PROFILE_READ",  "Data : null");
        });

        // Inflate the navigation drawer
        expandMenuButton.setOnClickListener(v-> mAppBarConfiguration.getDrawerLayout().openDrawer(Gravity.LEFT));

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(navigationView, navController);
    }



    // Function to load user details into UI
    private void updateUserDetailsUI(String name, String wcaId, String photoURL)
    {
        // Update cuber name
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
    private void getDate(EditText dateEditText)
    {
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
    private boolean isWcaIdValid(String wcaId)
    {
        // ID has to be 10 characters long
        if(wcaId.length() != 10)
            return false;
        else
        {
            // Divide WCA ID into sub components
            String year = wcaId.substring(0,4);
            String name = wcaId.substring(4,8);
            String id = wcaId.substring(8,10);

            // WCA ID FORMAT YYYY-ABCD-ID
            if (!year.matches("^[0-9]*$"))
                return false;
            if (!name.matches("^[A-Z]*$"))
                return false;
            return id.matches("^[0-9]*$");
        }
    }



    // Function to check validity of mobile number
    private boolean isMobileValid(String mobile)
    {
        // mobile number can only plus,dash and numbers
        if (!mobile.matches("^[-0-9+]*$"))
            return false;
        // ID has to be 10 characters long
        return mobile.length() >= 10;
    }



    // Function to check validity of name
    private boolean isNameValid(String name)
    {
        // mobile number can only plus,dash and numbers
        return name.matches("^[A-Z a-z]*$");
    }



    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
    }
}
