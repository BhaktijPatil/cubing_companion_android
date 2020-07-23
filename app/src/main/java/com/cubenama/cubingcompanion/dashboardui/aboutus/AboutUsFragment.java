package com.cubenama.cubingcompanion.dashboardui.aboutus;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cubenama.cubingcompanion.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class AboutUsFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_about_us, container, false);

        // About me links
        ImageView aboutMeGmailImageView = root.findViewById(R.id.gmailImageView);
        ImageView aboutMeTwitterImageView = root.findViewById(R.id.twitterImageView);
        ImageView aboutMeGithubImageView = root.findViewById(R.id.githubImageView);

        aboutMeTwitterImageView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/realdesdes"));
            startActivity(intent);
        });

        aboutMeGmailImageView.setOnClickListener(v -> {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "bhaktij1999@gmail.com", null));
            startActivity(Intent.createChooser(emailIntent, null));
        });

        aboutMeGithubImageView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/BhaktijPatil"));
            startActivity(intent);
        });

        // About us links
        ImageView aboutUsGmailImageView = root.findViewById(R.id.cubenamaGmailImageView);
        ImageView aboutUsWebsiteImageView = root.findViewById(R.id.cubenamaWebsiteImageView);
        ImageView aboutUsYoutubeImageView = root.findViewById(R.id.cubenamaYoutubeImageView);
        ImageView aboutUsInstagramImageView = root.findViewById(R.id.cubenamaInstagramImageView);
        ImageView aboutUsFacebookImageView = root.findViewById(R.id.cubenamaFacebookImageView);

        aboutUsFacebookImageView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/cubenama"));
            startActivity(intent);
        });

        aboutUsGmailImageView.setOnClickListener(v -> {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "teamcubenama@gmail.com", null));
            startActivity(Intent.createChooser(emailIntent, null));
        });

        aboutUsInstagramImageView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/cubenama/"));
            startActivity(intent);
        });

        aboutUsYoutubeImageView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/channel/UC2AqPMLYBwY_E5jU0mO2dWA"));
            startActivity(intent);
        });

        aboutUsWebsiteImageView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.cubenama.com/"));
            startActivity(intent);
        });

        // App ratings
        ImageView downVoteImageView = root.findViewById(R.id.thumbsDownImageView);
        ImageView upVoteImageView = root.findViewById(R.id.thumbsUpImageView);

        TextView likeCountTextView = root.findViewById(R.id.likeCountTextView);

        // Create database instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SharedPreferences userDetailsSharedPreferences = requireActivity().getSharedPreferences("user_details", Context.MODE_PRIVATE);;

        CollectionReference userVotesReference =  db.collection(getString(R.string.db_field_name_app_details)).document(getString(R.string.db_field_name_app_ratings)).collection(getString(R.string.db_field_name_user_votes));
        userVotesReference.addSnapshotListener((queryDocumentSnapshots, e) -> {
            // Calculate recommendations count
            long recommendationsCount = 0;
            for(QueryDocumentSnapshot snapshot : queryDocumentSnapshots)
            {
                if(snapshot.getBoolean(getString(R.string.db_field_name_is_up_vote)))
                {
                    recommendationsCount ++;
                    if(snapshot.getId().equals(userDetailsSharedPreferences.getString("uid", "")))
                        upVoteImageView.setColorFilter(requireActivity().getColor(R.color.colorAccent));
                }
                else
                {
                    recommendationsCount --;
                    if(snapshot.getId().equals(userDetailsSharedPreferences.getString("uid", "")))
                        downVoteImageView.setColorFilter(requireActivity().getColor(R.color.colorAccent));
                }
            }
            likeCountTextView.setText( recommendationsCount + " cubers recommend");

            // Up vote & Down vote listeners
            Map<String, Object> ratingDetails = new HashMap<>();
            upVoteImageView.setOnClickListener(v -> {
                upVoteImageView.setColorFilter(requireActivity().getColor(R.color.colorAccent));
                downVoteImageView.setColorFilter(requireActivity().getColor(R.color.colorPrimary));
                ratingDetails.put(getString(R.string.db_field_name_is_up_vote), true);
                userVotesReference.document(userDetailsSharedPreferences.getString("uid", "")).set(ratingDetails);
            });
            downVoteImageView.setOnClickListener(v -> {
                upVoteImageView.setColorFilter(requireActivity().getColor(R.color.colorPrimary));
                downVoteImageView.setColorFilter(requireActivity().getColor(R.color.colorAccent));
                ratingDetails.put(getString(R.string.db_field_name_is_up_vote), false);
                userVotesReference.document(userDetailsSharedPreferences.getString("uid", "")).set(ratingDetails);
            });
        });

        return root;
    }
}
