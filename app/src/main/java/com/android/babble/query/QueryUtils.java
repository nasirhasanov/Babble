package com.android.babble.query;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryUtils {




    public static void addNewPost(String postText,String userId, String userName, String userPic, String countryCode){

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> postDoc = new HashMap<>();
        postDoc.put("desc", postText);
        postDoc.put("user_name",userName);
        postDoc.put("user_id",userId);
        postDoc.put("user_pic", userPic);
        postDoc.put("score", 1);
        postDoc.put("time", FieldValue.serverTimestamp());
        postDoc.put("country_code", countryCode);

        db.collection("posts").add(postDoc)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                       // documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });

    }
}
