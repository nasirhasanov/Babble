package com.android.babble.activity;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.babble.R;
import com.android.babble.helper.Constants;
import com.android.babble.utils.ProgressDialog;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.koalap.geofirestore.GeoFire;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddPostActivity extends AppCompatActivity {
    private EditText postEditText;
    private Button addPostButton;
    private TextView counterTextView;
    private CircleImageView profileImage;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    private SharedPreferences sharedPreferencesUser, sharedPreferencesLocation;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        postEditText = findViewById(R.id.post_editText);
        postEditText.addTextChangedListener(mTextEditorWatcher);
        postEditText.requestFocus();

        counterTextView = findViewById(R.id.counter_text_view);
        addPostButton = findViewById(R.id.add_post_button);

        progressDialog = ProgressDialog.getInstance();


        sharedPreferencesUser = getSharedPreferences(Constants.SHARED_PREFS_USER_SETTINGS, Context.MODE_PRIVATE);
        sharedPreferencesLocation = getSharedPreferences(Constants.SHARED_PREFS_LOCATION_SETTINGS, Context.MODE_PRIVATE);

        profileImage = findViewById(R.id.circleView);
        downloadProfilePicture(sharedPreferencesUser.getString("profile_pic_path","null"));
        checkUserProfileInfo(currentUser.getUid());

        ImageView closeImage = findViewById(R.id.close_image);
        closeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        addPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptToPost();
            }
        });
    }


    private final TextWatcher mTextEditorWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //This sets a textview to the current length
            counterTextView.setText(s.length() +" / 200");
        }

        public void afterTextChanged(Editable s) {
            if(postEditText.getText().toString().trim().length()>0){
                addPostButton.setEnabled(true);
            }else {
                addPostButton.setEnabled(false);
            }
            if (null != postEditText.getLayout() && postEditText.getLayout().getLineCount() > 8) {
                postEditText.getText().delete(postEditText.getText().length() - 1, postEditText.getText().length());
            }
        }
    };

    private void downloadProfilePicture(String profile_pic_path) {
        Glide.with(this)
                .load(profile_pic_path)
                .placeholder(R.drawable.ic_default_profile_avatar)
                .error(R.drawable.ic_default_profile_avatar)
                .thumbnail(
                        Glide.with(this)
                                .load(profile_pic_path)
                                .override(75,75))
                .into(profileImage);
    }


    private void checkUserProfileInfo(String uid) {
        DocumentReference userRef = db.collection("users").document(uid);
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String username = String.valueOf(document.get("username"));
                        String profile_pic_path = String.valueOf(document.get("profile_pic_path"));

                        downloadProfilePicture(profile_pic_path);

                        SharedPreferences.Editor editor = sharedPreferencesUser.edit();
                        editor.putString("username", username);
                        editor.putString("profile_pic_path", profile_pic_path);
                        editor.apply();

                    } else {
                        Toast.makeText(AddPostActivity.this, "Error occurred. Try again", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(AddPostActivity.this, "Error occurred. Try again", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }

    private void attemptToPost(){
        progressDialog.showProgress(AddPostActivity.this, "Posting ...");


        String postText = postEditText.getText().toString().trim();
        String userName = sharedPreferencesUser.getString("username",null);
        String userId = currentUser.getUid();
        String userPicPath = sharedPreferencesUser.getString("profile_pic_path",null);
        String countryCode = sharedPreferencesLocation.getString("country_code",null);
        String countryName = sharedPreferencesLocation.getString("country_name",null);
        String fullAddress = sharedPreferencesLocation.getString("full_address",null);
        String latitude = sharedPreferencesLocation.getString("latitude",null);
        String longitude = sharedPreferencesLocation.getString("longitude",null);

        if (userName!=null && countryCode!=null){

            Map<String, Object> postDoc = new HashMap<>();
            postDoc.put("post_text", postText);
            postDoc.put("username",userName);
            postDoc.put("user_id",userId);
            postDoc.put("user_pic", userPicPath);
            postDoc.put("country_code", countryCode);
            postDoc.put("country_name", countryName);
            postDoc.put("full_address", fullAddress);
            postDoc.put("rating", 1);
            postDoc.put("time", System.currentTimeMillis());

            db.collection("posts").add(postDoc)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            progressDialog.hideProgress();
                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });

        }
    }
}
