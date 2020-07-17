package com.android.babble.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.LinkMovementMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.babble.MainActivity;
import com.android.babble.R;
import com.android.babble.interfaces.OnUsernameCheckListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {
    private EditText emailEditText, passwordEditText, usernameEditText ;
    private TextView termsTextView, linkToSignIn;
    private FirebaseFirestore db;
    private Button signUpButton;
    private ImageView usernameCheck;
    private boolean emailOk, usernameOk, passwordOk;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        emailEditText = findViewById(R.id.edit_text_email);
        emailEditText.addTextChangedListener(new EditTextWatcher(emailEditText));;

        passwordEditText = findViewById(R.id.edit_text_password);
        passwordEditText.addTextChangedListener(new EditTextWatcher(passwordEditText));;

        usernameEditText = findViewById(R.id.edit_text_username);
        usernameEditText.addTextChangedListener(new EditTextWatcher(usernameEditText));;

        termsTextView = findViewById(R.id.text_view_terms_conditions);
        signUpButton = findViewById(R.id.button_signup);
        usernameCheck = findViewById(R.id.username_checked);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        linkToSignIn = findViewById(R.id.link_to_signin);
        linkToSignIn.setText(Html.fromHtml(getString(R.string.don_t_have_account) + "<b><font color=#D32F2F>" + getString(R.string.sign_in) + "</font></b>"));
        linkToSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
                finish();
            }
        });


        if (!isNetworkAvailable()){
            Toast.makeText(getApplicationContext(), getString(R.string.check_connection), Toast.LENGTH_SHORT).show();
            return;
        }

        setUpTermsofService();


        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isNetworkAvailable()){
                    Toast.makeText(getApplicationContext(), getString(R.string.check_connection), Toast.LENGTH_SHORT).show();
                    return;
                }

                final String email = emailEditText.getText().toString().trim();
                final String password = passwordEditText.getText().toString().trim();
                final String username = usernameEditText.getText().toString().trim();

                signUpwithEmailandPassword(email, password, username);
            }
        });

    }


    private void isCheckUsername(final String username, final OnUsernameCheckListener listener){
        if (!username.contains(" ")){
            Query query = db.collection("users")
                    .whereEqualTo("username", username);

            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()){
                                if(task.getResult().size() == 0){
                                    listener.onSuccess(false);
                                }else{
                                    listener.onSuccess(true);
                                }
                            }
                        }
                    });
        }else {
            usernameEditText.setError("White space is not allowed");
            usernameOk = false;
        }

    }

    private void addUserToDb(String username) {
        FirebaseUser user = firebaseAuth.getCurrentUser();

        Map<String, Object> userDoc = new HashMap<>();
        userDoc.put("username", username);
        userDoc.put("uid", user.getUid());
        userDoc.put("email", user.getEmail());
        userDoc.put("bio", "I am new User");
        userDoc.put("score", 1);

        db.collection("users").document(firebaseAuth.getCurrentUser().getUid())
                .set(userDoc)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            firebaseAuth.getCurrentUser().sendEmailVerification();
                            startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                            finish();
                        }
                    }
                });


    }

    public void showHide(View view) {
        if(view.getId()==R.id.show_hide_button){

            if(passwordEditText.getTransformationMethod().equals(PasswordTransformationMethod.getInstance())){
                ((ImageView)(view)).setImageResource(R.drawable.ic_show_password);

                //Show Password
                passwordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                passwordEditText.setSelection(passwordEditText.getText().length());

            }
            else{
                ((ImageView)(view)).setImageResource(R.drawable.ic_hide_password);

                //Hide Password
                passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                passwordEditText.setSelection(passwordEditText.getText().length());

            }
        }
    }

    public void setUpTermsofService(){
        String text = "By signing up, you agree to the Terms of Service and acknowledge the Privacy Policy.";
        SpannableString spannableString = new SpannableString(text);

        ClickableSpan termsOfService = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent intent = new Intent(getApplicationContext(),WebViewActivity.class);
                intent.putExtra("url", "https://snikpik-1.flycricket.io/terms.html");
                startActivity(intent);
            }
        };
        ForegroundColorSpan backgroundColorSpan1 = new ForegroundColorSpan(Color.BLACK);
        spannableString.setSpan(termsOfService, 32,48, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(backgroundColorSpan1, 32,48, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        ClickableSpan privacyPolicy = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent intent = new Intent(getApplicationContext(),WebViewActivity.class);
                intent.putExtra("url", "https://snikpik-1.flycricket.io/privacy.html");
                startActivity(intent);
            }
        };
        ForegroundColorSpan backgroundColorSpan2 = new ForegroundColorSpan(Color.BLACK);
        spannableString.setSpan(privacyPolicy, 69,83, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(backgroundColorSpan2, 69,83, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);


        termsTextView.setText(spannableString);
        termsTextView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public void checkButton(){
        if (passwordOk && emailOk && usernameOk){
            signUpButton.setEnabled(true);
        }else{
            signUpButton.setEnabled(false);
        }
    }
    private void signUpwithEmailandPassword(final String email,final String password, final String username){

        emailEditText.clearFocus();
        passwordEditText.clearFocus();
        usernameEditText.clearFocus();
        signUpButton.setEnabled(false);

        firebaseAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()){
                            if(firebaseAuth.getCurrentUser()!=null) {
                                addUserToDb(username);
                            }

                        }else{

                            if (task.getException() instanceof FirebaseAuthUserCollisionException){
                                emailEditText.setError("Email is already in use");
                                emailEditText.requestFocus();
                            } else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException){
                                emailEditText.setError("Email is not valid");
                                emailEditText.requestFocus();
                            } else if (task.getException() instanceof FirebaseAuthWeakPasswordException) {
                                passwordEditText.setError("Password is weak");
                                passwordEditText.requestFocus();
                            }else{
                                Toast.makeText(SignUpActivity.this, getString(R.string.auth_failed), Toast.LENGTH_SHORT).show();
                            }

                        }
                    }
                });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private class EditTextWatcher implements TextWatcher{

        private View view;
        private EditTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        public void afterTextChanged(Editable editable) {
            switch(view.getId()){

                case R.id.edit_text_email:
                    emailOk = false;
                    if (emailEditText.getText().toString().trim().length()>6){
                    if (isEmailValid(emailEditText.getText().toString().trim())) {
                        emailOk = true;
                    }else{
                        emailOk = false;
                        emailEditText.setError("Email is not valid");
                    }
                        checkButton();
                    }
                    break;

                case R.id.edit_text_username:
                    usernameOk = false;
                    usernameCheck.setVisibility(View.GONE);

                    if (usernameEditText.getText().length() > 3){
                        isCheckUsername(usernameEditText.getText().toString().trim(), new OnUsernameCheckListener() {
                            @Override
                            public void onSuccess(boolean isRegistered) {
                                if (isRegistered){
                                    usernameCheck.setVisibility(View.GONE);
                                    usernameEditText.setError("Username already exists");
                                    usernameEditText.requestFocus();
                                    usernameOk = false;
                                }else {
                                    usernameEditText.setError(null);
                                    usernameCheck.setVisibility(View.VISIBLE);
                                    usernameOk = true;
                                }
                                checkButton();
                            }
                        });
                    }
                    break;

                case R.id.edit_text_password:
                    passwordOk = false;
                    final String password = passwordEditText.getText().toString().trim();
                    if (password.length()<6){
                        passwordEditText.setError("Password should be at least 6 char");
                    }else{
                        passwordOk = true;
                    }
                    checkButton();

                    break;
            }

        }
    }
}
