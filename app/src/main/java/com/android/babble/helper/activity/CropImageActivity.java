package com.android.babble.helper.activity;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.babble.R;
import com.android.babble.utils.ProgressDialog;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iceteck.silicompressorr.SiliCompressor;
import com.theartofdev.edmodo.cropper.CropImageView;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;


public class CropImageActivity extends AppCompatActivity {
    private CropImageView cropImageView;
    private StorageReference storageReference;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_image);

        Bundle extras = this.getIntent().getExtras();
        Uri imageUri = Uri.parse(extras.getString("imageUri"));

        cropImageView = findViewById(R.id.CropImageView);
        cropImageView.setImageUriAsync(imageUri);

        storageReference = FirebaseStorage.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        progressDialog = ProgressDialog.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        }


        Button saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.showProgress(CropImageActivity.this, "Please wait ...");
                Bitmap croppedBitmap = cropImageView.getCroppedImage();
                File savedimageFile = saveBitmapImageAsFile(croppedBitmap,user.getUid());
                uploadUserPicture(savedimageFile);
            }
        });

    }


    public void uploadUserPicture(File savedImageFile){
        try {

            File newFile = new File(this.getFilesDir(),"fjvnds");
            String filePath = SiliCompressor.with(this).compress(savedImageFile.getPath(), newFile);
            File uploadFile = new File(filePath);


            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            final StorageReference userProfilePicRef = storageReference.child("profilePictures/" + user.getUid());


            UploadTask uploadTask = userProfilePicRef.putFile(Uri.fromFile(uploadFile));
            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return userProfilePicRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        updateProfilePicPathtoDb(String.valueOf(downloadUri));
                    } else {
                    }
                }
            });
        }catch (Exception e){
            Toast.makeText(this, "Error occurred. Try again", Toast.LENGTH_SHORT).show();
            finish();
        }

    }

    private void updateProfilePicPathtoDb(String picturePath) {
        DocumentReference profileRef = db.collection("users").document(user.getUid());
        profileRef
                .update("profile_pic_path", picturePath)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressDialog.hideProgress();
                        Toast.makeText(CropImageActivity.this, "Profile Picture Updated",
                                Toast.LENGTH_SHORT).show();
                        finish();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(CropImageActivity.this, "Error occurred. Try again", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    private File saveBitmapImageAsFile(Bitmap bitmap, String name) {
        File file = new File(this.getFilesDir(), name);

        OutputStream os;
        try {
            os = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
            os.close();
        } catch (Exception e) {
            Toast.makeText(this, "Error occurred. Try again", Toast.LENGTH_SHORT).show();
            finish();
        }
        return file;
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

