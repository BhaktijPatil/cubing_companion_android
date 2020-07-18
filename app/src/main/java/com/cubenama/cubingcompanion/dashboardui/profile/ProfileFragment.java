package com.cubenama.cubingcompanion.dashboardui.profile;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.cubenama.cubingcompanion.R;
import com.cubenama.cubingcompanion.SignInActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.okhttp.Dispatcher;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import io.grpc.Compressor;


public class ProfileFragment extends Fragment {

    // Database reference
    private FirebaseFirestore db;
    // Firestore document reference
    private DocumentReference userDetailsReference;
    // Firebase Storage reference
    private StorageReference profilePictureRef;

    private ImageView backgroundGifView;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        // Load background GIF
        backgroundGifView = root.findViewById(R.id.profilePictureBackgroundImageView);
        Glide.with(this).asGif().load(R.drawable.background_profile).into(backgroundGifView);

        EditText mainEventEditText = root.findViewById(R.id.mainEventEditText);
        EditText nameEditText = root.findViewById(R.id.nameEditText);
        EditText dobEditText = root.findViewById(R.id.dobEditText);
        EditText phoneEditText = root.findViewById(R.id.phoneEditText);

        // Create database instance
        db = FirebaseFirestore.getInstance();

        // Create reference to userDetailsReference details
        SharedPreferences userDetailsSharedPreferences = requireActivity().getSharedPreferences("user_details", Context.MODE_PRIVATE);

        // Create storage instance
        profilePictureRef = FirebaseStorage.getInstance().getReference().child(getString(R.string.db_field_name_profile_pictures)).child(userDetailsSharedPreferences.getString("uid",""));
        userDetailsReference = db.collection(getString(R.string.db_field_name_user_details)).document(userDetailsSharedPreferences.getString("uid",""));

        // Create listener for add picture button
        FloatingActionButton addPictureButton = root.findViewById(R.id.addPictureFloatingActionButton);
        addPictureButton.setOnClickListener(v -> chooseNewImage());

        // Create listener for DOB
        ImageButton dobButton = root.findViewById(R.id.selectDateButton);
        dobButton.setOnClickListener(v -> getDate(root.findViewById(R.id.dobEditText)));

        // Create listener for sign out
        CardView signOutButton = root.findViewById(R.id.signOutCardView);
        signOutButton.setOnClickListener(v->{
            // Signout the current user
            FirebaseAuth.getInstance().signOut();
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
            GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso);
            mGoogleSignInClient.signOut().addOnCompleteListener(requireActivity(), task1 -> {
                // Start login activity
               startActivity(new Intent(requireContext(), SignInActivity.class));
            });
        });

        // Create listener for update profile button
        CardView updateProfileButton = root.findViewById(R.id.updateProfileCardView);
        updateProfileButton.setOnClickListener(v-> {
            String mainEvent = String.valueOf(mainEventEditText.getText());
            String name = String.valueOf(nameEditText.getText());
            String mobile = String.valueOf(phoneEditText.getText());
            String dob = String.valueOf(dobEditText.getText());

            // Some fields are invalid
            if (name.equals("") || !isNameValid(name)) {
                Toast.makeText(requireActivity(), "Name can only contain letters.", Toast.LENGTH_SHORT).show();
            } else if (mobile.equals("") || !isMobileValid(mobile)) {
                Toast.makeText(requireActivity(), "Invalid mobile number.", Toast.LENGTH_SHORT).show();
            }
            // All fields are valid.
            else {
                Map<String, Object> accountDetails = new HashMap<>();
                if(!mainEvent.equals(""))
                    accountDetails.put(getString(R.string.db_field_name_main_event), mainEvent);
                accountDetails.put(getString(R.string.db_field_name_name), name);
                accountDetails.put(getString(R.string.db_field_name_mobile), mobile);
                accountDetails.put(getString(R.string.db_field_name_dob), dob);

                // Update profile on backend
                userDetailsReference.update(accountDetails);
                Toast.makeText(requireActivity(), "Account details updated.", Toast.LENGTH_SHORT).show();
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
                Log.d("CC_PROFILE_READ",  "Data : " + snapshot.getData());
                ImageView profilePictureImageView = root.findViewById(R.id.profilePictureImageView);

                // Main event may not exist in user profile
                if(snapshot.contains(getString(R.string.db_field_name_main_event)))
                {
                    mainEventEditText.setText(snapshot.getString(getString(R.string.db_field_name_main_event)));
                }
                nameEditText.setText(snapshot.getString(getString(R.string.db_field_name_name)));
                dobEditText.setText(snapshot.getString(getString(R.string.db_field_name_dob)));
                phoneEditText.setText(snapshot.getString(getString(R.string.db_field_name_mobile)));
                Glide.with(requireActivity().getApplicationContext()).load(Uri.parse(snapshot.getString(getString(R.string.db_field_name_photo_url)))).into(profilePictureImageView);
            }
            else
                Log.d("CC_PROFILE_READ",  "Data : null");
        });

        return root;
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
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireActivity(), (view, year, monthOfYear, dayOfMonth) -> dateEditText.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year), currYear, currMonth, currDay);
        datePickerDialog.show();
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



    // Function to choose new image
    private void chooseNewImage()
    {
        android.content.Intent intent = new android.content.Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(android.content.Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(android.content.Intent.createChooser(intent, "Choose a Profile Picture."), 1);
    }



    // Get result of choose image here
    @Override
    public void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (resultCode == android.app.Activity.RESULT_OK && requestCode == 1)
            {
                // Get the URI of target image
                Uri imageURI = data.getData();

                // Upload to firebase
                profilePictureRef.putFile(imageURI).addOnSuccessListener(taskSnapshot -> profilePictureRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            Log.d("CC_PROFILE", "Profile picture uploaded");
                            // Upload link for profile picture to userDetailsReference details
                            Map<String, Object> accountDetails = new HashMap<>();
                            accountDetails.put(getString(R.string.db_field_name_photo_url), uri.toString());
                            userDetailsReference.update(accountDetails).addOnCompleteListener(task -> {
                                // reset background GIF
                                Glide.with(this).asGif().load(R.drawable.background_profile).into(backgroundGifView);
                            });
                        }))
                        .addOnProgressListener(taskSnapshot -> {
                            // change background GIF
                            Glide.with(requireActivity()).asGif().load(R.drawable.cube_loading_2).into(backgroundGifView);
                            Toast.makeText(requireActivity(), "Uploading to Database. Please be patient.", Toast.LENGTH_SHORT).show();
                            Log.d("CC_PROFILE", "Profile picture uploading");
                        });
            }
        }
        catch (Exception e) {
            Log.e("EAG_CHOOSE_IMAGE", "Target image selection error ", e);
        }
    }

}
