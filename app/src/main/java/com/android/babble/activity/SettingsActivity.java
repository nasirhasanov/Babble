package com.android.babble.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.android.babble.BuildConfig;
import com.android.babble.R;
import com.android.babble.helper.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static com.android.babble.helper.Constants.SHARED_PREFS_USER_SETTINGS;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_button);
            getSupportActionBar().setTitle(getString(R.string.settings));
        }
        // load settings fragment
        getSupportFragmentManager().beginTransaction().replace(R.id.container, new MainPreferenceFragment()).commit();
    }

    public static class MainPreferenceFragment extends PreferenceFragmentCompat {

        private FirebaseAuth mAuth;
        private FirebaseAuth.AuthStateListener authListener;
        private FirebaseUser currentUser;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            // Load the preferences from an XML resource
            setPreferencesFromResource(R.xml.pref_settings, rootKey);

            mAuth = FirebaseAuth.getInstance();
            currentUser = mAuth.getCurrentUser();

            authListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user == null) {
                        Toast.makeText(getActivity(), "Logged out from account", Toast.LENGTH_SHORT).show();
                        getActivity().finish();
                    }
                }
            };

            Preference changeBio = findPreference(getString(R.string.key_change_bio));
            changeBio.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(getActivity(), ChangeBioActivity.class));
                    return true;
                }
            });

            // feedback preference click listener
            Preference changePass = findPreference(getString(R.string.key_change_password));
            changePass.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(getActivity(), ChangePasswordActivity.class));
                    return true;
                }
            });

            // feedback preference click listener
            Preference deleteAccount = findPreference(getString(R.string.key_delete_account));
            deleteAccount.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(getActivity(), DeleteAccountActivity.class));
                    return true;
                }
            });

            // feedback preference click listener
            Preference contactSupport = findPreference(getString(R.string.key_contact_support));
            contactSupport.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                            "mailto","koanapps@gmail.com", null));
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Support Request");
                    emailIntent.putExtra(Intent.EXTRA_TEXT, "");
                    startActivity(Intent.createChooser(emailIntent, "Send Email"));
                    return true;
                }
            });

            // feedback preference click listener
            Preference termsOfService = findPreference(getString(R.string.key_terms_of_service));
            termsOfService.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(getActivity(), WebViewActivity.class);
                    intent.putExtra("url", Constants.URL_TERMS_OF_SERVICE);
                    startActivity(intent);
                    return true;
                }
            });

            Preference privacyPolicy = findPreference(getString(R.string.key_privacy_policy));
            privacyPolicy.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(getActivity(), WebViewActivity.class);
                    intent.putExtra("url", Constants.URL_PRIVACY_POLICY);
                    startActivity(intent);
                    return true;
                }
            });

            // feedback preference click listener
            Preference versionNumber = findPreference(getString(R.string.key_version_name));
            String versionName = BuildConfig.VERSION_NAME;
            versionNumber.setSummary(versionName);


            Preference logOut = findPreference("log_out");
            logOut.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    signOut();
                    return true;
                }
            });
        }


        @Override
        public void onStart() {
            super.onStart();
            mAuth.addAuthStateListener(authListener);
        }

        @Override
        public void onStop() {
            super.onStop();
            if (authListener != null) {
                mAuth.removeAuthStateListener(authListener);
            }
        }

        public void signOut(){
            if (getActivity()!=null){
            SharedPreferences settings = getActivity().getSharedPreferences(SHARED_PREFS_USER_SETTINGS, MODE_PRIVATE);
            settings.edit().clear().apply();}
            mAuth.signOut();
        }


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }



}
