package com.dianiel.pisopatrol;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity {

    private TextView instructionsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // Initialize instructionsTextView
        instructionsTextView = findViewById(R.id.instructionsTextView);

        Button instructionsButton = findViewById(R.id.instructionsButton);
        instructionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleInstructionsVisibility(v);
            }
        });
    }

    // Method to toggle visibility of instructions TextView
    public void toggleInstructionsVisibility(View view) {
        if (instructionsTextView.getVisibility() == View.VISIBLE) {
            instructionsTextView.setVisibility(View.GONE);
        } else {
            instructionsTextView.setVisibility(View.VISIBLE);
        }
    }
}
