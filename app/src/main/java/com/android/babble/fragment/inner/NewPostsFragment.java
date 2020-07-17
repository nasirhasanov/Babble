package com.android.babble.fragment.inner;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.babble.R;
import com.android.babble.adapter.NewPostsAdapter;
import com.android.babble.model.NewPostData;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class NewPostsFragment extends Fragment {

    private List<NewPostData> postData;
    private RecyclerView recyclerView;
    private NewPostsAdapter adapter;
    private TextView noPostsTextView;
    private FirebaseUser currentUser;
    private SpinKitView spinKitView;
    private FirebaseFirestore db;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View newPostFragment = inflater.inflate(R.layout.fragment_new_posts, container, false);

        noPostsTextView = newPostFragment.findViewById(R.id.no_posts_yet);
        spinKitView = newPostFragment.findViewById(R.id.progressBar);

        recyclerView = newPostFragment.findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        postData = new ArrayList<>();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        fetchNewPosts();

        return newPostFragment;
    }

    private void fetchNewPosts() {
        spinKitView.setVisibility(View.VISIBLE);
        Query first = db.collection("posts")
                .whereEqualTo("country_code","AZ");
        first.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.getResult()!=null){
                    spinKitView.setVisibility(View.GONE);
                    for (QueryDocumentSnapshot document : task.getResult()) {

                            NewPostData newPostData = new NewPostData();

                            newPostData.setUserName(String.valueOf(document.get("username")));
                            newPostData.setUserId(String.valueOf(document.get("user_id")));
                            newPostData.setPostText(String.valueOf(document.get("post_text")));
                            newPostData.setScore(Integer.parseInt(String.valueOf(document.get("rating"))));
                            newPostData.setTimeStamp(String.valueOf(document.get("time")));
                            newPostData.setUserPic(String.valueOf(document.get("user_pic")));
                            newPostData.setCountryCode(String.valueOf(document.get("country_code")));
                            newPostData.setCountryName(String.valueOf(document.get("country_name")));
                            newPostData.setFullAddress(String.valueOf(document.get("full_address")));

                            postData.add(newPostData);
                        }
                    if (!postData.isEmpty()) {
                        adapter = new NewPostsAdapter(postData, getContext());
                        recyclerView.setAdapter(adapter);
                        Collections.reverse(postData);
                        adapter.notifyDataSetChanged();
                    }else{
                        spinKitView.setVisibility(View.GONE);
                        noPostsTextView.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    }
                    }else{

                    Toast.makeText(getContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}