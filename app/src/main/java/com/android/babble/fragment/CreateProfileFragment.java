package com.android.babble.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.android.babble.R;
import com.android.babble.activity.SignInActivity;


public class CreateProfileFragment extends Fragment {

    public CreateProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View profileFragment = inflater.inflate(R.layout.fragment_create_profile, container, false);

        Button buttonGotoSignIn = profileFragment.findViewById(R.id.button_goto_signin);
        buttonGotoSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), SignInActivity.class));
                getActivity().finish();
            }
        });


        return profileFragment;
    }

}