package com.android.babble.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.babble.R;
import com.android.babble.helper.Constants;
import com.android.babble.helper.activity.CropImageActivity;
import com.android.babble.utils.ProgressDialog;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class ChangeBioActivity extends AppCompatActivity {

    private EditText bioEdittext;
    private Button addBioButton;
    private SharedPreferences sharedpreferences;
    private TextView counterTextView;
    String bioString;
    private FirebaseFirestore db;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_bio);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_button);
        }

        bioEdittext = findViewById(R.id.edit_text_bio);
        bioEdittext.addTextChangedListener(mTextEditorWatcher);
        bioEdittext.requestFocus();


        addBioButton = findViewById(R.id.button_change_bio);
        counterTextView = findViewById(R.id.counter_text_view);
        db = FirebaseFirestore.getInstance();
        progressDialog = ProgressDialog.getInstance();

        sharedpreferences = getSharedPreferences(Constants.SHARED_PREFS_USER_SETTINGS, Context.MODE_PRIVATE);
        bioString = sharedpreferences.getString("bio","null");
        if (!bioString.equals("null")){
            bioEdittext.setText(bioString);
            bioEdittext.setSelection(bioEdittext.getText().length());
        }

        addBioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.showProgress(ChangeBioActivity.this, "Updating Bio ...");
                updateBiotoDb(bioEdittext.getText().toString().trim());
                addBioButton.setEnabled(false);
            }
        });

    }

    private final TextWatcher mTextEditorWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //This sets a textview to the current length
            counterTextView.setText(s.length() +" / 30");
        }

        public void afterTextChanged(Editable s) {
            String currentBio = bioEdittext.getText().toString().trim();
            if(currentBio.length()>0 && !currentBio.equals(bioString) ){
                addBioButton.setEnabled(true);
            }else {
                addBioButton.setEnabled(false);
            }
        }
    };

    private void updateBiotoDb(String newBioString) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DocumentReference profileRef = db.collection("users").document(user.getUid());

        profileRef.update("bio", newBioString)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressDialog.hideProgress();
                        Toast.makeText(ChangeBioActivity.this, "Bio Updated",
                                Toast.LENGTH_SHORT).show();
                        finish();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ChangeBioActivity.this, "Error occurred. Try again", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
