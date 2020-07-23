package com.cubenama.cubingcompanion;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

public class LoadingScreenController {

    private Dialog loadingDialog;
    private Context context;

    // Constructor to initialize dialog
    LoadingScreenController(Context context)
    {
        // Confirmation dialog
        loadingDialog = new Dialog(context, R.style.Theme_AppCompat_NoActionBar);
        loadingDialog.setCancelable(false);
        loadingDialog.setContentView(R.layout.dialog_box_loading_screen);
        this.context = context;
    }

    // Function to show loader
    public void showLoadingScreen(String loadingMessage)
    {
        // Load spinning cube GIF
        ImageView loadingGifView = loadingDialog.findViewById(R.id.loadingGif);
        Glide.with(context).asGif().load(R.drawable.cube_loading_3).into(loadingGifView);

        // Set loader message
        TextView loadingMessageTextView = loadingDialog.findViewById(R.id.loadingMessageTextView);
        loadingMessageTextView.setText(loadingMessage);

        // Show dialog
        loadingDialog.show();
    }



    // Function to dismiss loader
    public void dismissLoadingScreen()
    {
        loadingDialog.dismiss();
    }
}
