package com.android.babble.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.android.babble.R;
import com.android.babble.activity.SettingsActivity;
import com.android.babble.adapter.HomePagerAdapter;
import com.android.babble.fragment.inner.MyNotificationsFragment;
import com.android.babble.fragment.inner.MyPostsFragment;
import com.android.babble.helper.Constants;
import com.android.babble.helper.activity.CropImageActivity;
import com.android.babble.helper.activity.ShowProfilePhotoActivity;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private ViewPager viewPager;
    private TabLayout tabLayout;
    private CircleImageView profileImage;
    private TextView usernameTextView, bioTextView, scoreTextView;
    private Uri mCropImageUri;
    private boolean hasProfilePic = false;
    private FirebaseFirestore db;
    private StorageReference storageReference;
    private SharedPreferences sharedpreferences;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth firebaseAuth;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    if (getActivity()!=null){
                        Intent intent = getActivity().getIntent();
                        getActivity().finish();
                        startActivity(intent); }
                }
            }
        };
        firebaseAuth.addAuthStateListener(authListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View profileFragment = inflater.inflate(R.layout.fragment_profile, container, false);

        Toolbar toolbar = profileFragment.findViewById(R.id.toolbar);
        if (getActivity()!=null){
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);}
        setHasOptionsMenu(true);

        tabLayout = profileFragment.findViewById(R.id.tab_layout);
        viewPager = profileFragment.findViewById(R.id.view_pager);

        usernameTextView = profileFragment.findViewById(R.id.username);
        bioTextView = profileFragment.findViewById(R.id.bio);
        scoreTextView = profileFragment.findViewById(R.id.score);
        profileImage = profileFragment.findViewById(R.id.profile_pic);
        profileImage.setEnabled(false);

        sharedpreferences = getActivity().getSharedPreferences(Constants.SHARED_PREFS_USER_SETTINGS, Context.MODE_PRIVATE);
        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();


        if (FirebaseAuth.getInstance().getCurrentUser()!=null){
            setLatestProfileInfo();
            updateProfileInfo();
        }
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!hasProfilePic) {
                    CropImage.startPickImageActivity(getContext(), ProfileFragment.this);
                }else{
                    showProfileImageDialog();
                }
            }
        });

        return profileFragment;
    }

    private void setLatestProfileInfo() {
        String username = sharedpreferences.getString("username","null");
        String bio = sharedpreferences.getString("bio","null");
        String profile_pic_path = sharedpreferences.getString("profile_pic_path","null");
        String score = sharedpreferences.getString("score","null");

        if (getContext()!=null && !username.equals("null") && !score.equals("null")){
            String myUserName = getContext().getResources().getString(R.string.username_text_view, username);
            String scoreText = getContext().getResources().getString(R.string.my_points_string, score);
            usernameTextView.setText(myUserName);
            scoreTextView.setText(scoreText);
        }
        if (!bio.equals("null")){
            bioTextView.setVisibility(View.VISIBLE);
            bioTextView.setText(sharedpreferences.getString("bio",null));
        }
        if (getActivity()!=null) {
            downloadProfilePicture(profile_pic_path);
        }
    }

    private void showProfileImageDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final String[] animals = {"View Photo", "Choose Another", "Remove" };
        builder.setItems(animals, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        Intent intent = new Intent(getActivity(), ShowProfilePhotoActivity.class);
                        intent.putExtra("photoUrl", sharedpreferences.getString("profile_pic_path",null));
                        startActivity(intent);
                        break;
                    case 1:
                        CropImage.startPickImageActivity(getContext(), ProfileFragment.this);
                        break;
                    case 2:
                        removeProfilePhoto();
                        break;
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void removeProfilePhoto() {
        DocumentReference userRef = db.collection("users")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid());
        Map<String, Object> updates = new HashMap<>();
        updates.put("profile_pic_path", FieldValue.delete());
        userRef.update(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                StorageReference userProfilePicRef = storageReference.child("profilePictures/" + FirebaseAuth.getInstance().getCurrentUser().getUid());

                userProfilePicRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if (getActivity()!=null)
                        Toast.makeText(getActivity(), "Photo removed", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (authListener != null) {
            firebaseAuth.removeAuthStateListener(authListener);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setUpViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void updateProfileInfo(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference userProfileRef = db.collection("users").document(userId);
        userProfileRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {

                    if (snapshot.exists()) {

                        String username = String.valueOf(snapshot.get("username"));
                        String profile_pic_path = String.valueOf(snapshot.get("profile_pic_path"));
                        String bio = String.valueOf(snapshot.get("bio"));
                        String score = String.valueOf(snapshot.get("score"));


                        if (!bio.equals("null")) {
                            bioTextView.setVisibility(View.VISIBLE);
                            bioTextView.setText(bio);
                        }else{
                            bioTextView.setVisibility(View.GONE);
                            bioTextView.setText(null);
                        }
                        if (getContext()!=null) {
                            String myUsernameText = getContext().getResources().getString(R.string.username_text_view, username);
                            String scoreText = getContext().getResources().getString(R.string.my_points_string, score);
                            scoreTextView.setText(scoreText);
                            usernameTextView.setText(myUsernameText);

                        }

                        hasProfilePic = !profile_pic_path.equals("null");

                        if (getActivity()!=null) {
                            downloadProfilePicture(String.valueOf(snapshot.get("profile_pic_path")));
                        }
                        profileImage.setEnabled(true);

                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putString("username", username);
                        editor.putString("profile_pic_path", profile_pic_path);
                        editor.putString("score", score);
                        editor.putString("bio", bio);
                        editor.apply();

                    } }  });}

    private void downloadProfilePicture(String profile_pic_path) {
        Glide.with(getActivity().getApplicationContext())
                .load(profile_pic_path)
                .placeholder(R.drawable.ic_default_profile_avatar)
                .thumbnail(
                        Glide.with(this)
                                .load(profile_pic_path)
                                .override(50,50))
                .into(profileImage);
    }


    private void setUpViewPager(ViewPager viewPager) {
        HomePagerAdapter adapter = new HomePagerAdapter(getChildFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        adapter.addFragment(new MyNotificationsFragment(),"Notifications");
        adapter.addFragment(new MyPostsFragment(),"My Posts");
        viewPager.setAdapter(adapter);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_profile, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(getActivity(), SettingsActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri imageUri = CropImage.getPickImageResultUri(getActivity(), data);

            if (CropImage.isReadExternalStoragePermissionsRequired(getActivity(), imageUri)) {
                mCropImageUri = imageUri;
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE);
            } else {
                Intent intent = new Intent(getActivity(), CropImageActivity.class);
                intent.putExtra("imageUri", imageUri.toString());
                startActivity(intent); }
        }
    }
    public void onRequestPermissionsResult(int requestCode, String permissions[], @NonNull int[] grantResults) {
        if (requestCode == CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                CropImage.startPickImageActivity(getActivity());
            } else {
                Toast.makeText(getActivity(), "Required permissions are not granted", Toast.LENGTH_LONG).show();
            }
        }
        if (requestCode == CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE) {
            if (mCropImageUri != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(getActivity(), CropImageActivity.class);
                intent.putExtra("imageUri", mCropImageUri.toString());
                startActivity(intent);
            } else {
                Toast.makeText(getActivity(), "Required permissions are not granted", Toast.LENGTH_LONG).show();
            }
        }
    }
}

