package capstone.inovision.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;

import capstone.inovision.R;
import capstone.inovision.common.Common;
import capstone.inovision.model.PiData;
import capstone.inovision.model.UserData;

public class SignupScreen extends AppCompatActivity {


    private TextView signup,terms;
    private CheckBox checkbox;
    private EditText mEmail,mPassword,mConfirmPassword,mName;
    private CardView mSignup;
    private FirebaseAuth mAuth;
    


    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "GOOGLE";

    private static final String TAG_FB = "FACEBOOK";
    private static final String EMAIL = "email";
    private CallbackManager mCallbackManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        getSupportActionBar().hide();
        setContentView(R.layout.activity_signup_screen);
        mAuth= FirebaseAuth.getInstance();

        initRedirectLogin();
        initSignup();
        initGoogleSignup();
        initFacebookSignup();


        terms=findViewById(R.id.signup_terms_txt);
        terms.setMovementMethod(LinkMovementMethod.getInstance());
        checkbox=findViewById(R.id.signup_checkbox);









    }

    private void initUserDataAndProceed(String uid){

        UserData userData=new UserData();
        userData.setPiId("");
        userData.setPhone("");
        userData.setTextMessageReceiveOn(true);

        FirebaseDatabase.getInstance().getReference().child("UserData").child(uid).setValue(userData).addOnCompleteListener(task -> {
            Common.firebaseCurrentUserData=userData;
            startActivity(new Intent(SignupScreen.this, HomeScreen.class));
            finish();
        });
    }


    private void initRedirectLogin(){
        Toolbar toolbar = findViewById(R.id.signup_toolbar);
        toolbar.setNavigationOnClickListener(v -> { startActivity(new Intent(SignupScreen.this,LoginScreen.class));finish();}
                );

    }

    private boolean isValid(boolean isChecked){
        if(!isChecked) {
            Snackbar.make(findViewById(android.R.id.content), "Please check terms and conditions box to proceed.", Snackbar.LENGTH_LONG).show();
            return false;
        }
       return isChecked;
    }

    private boolean isValid(boolean isChecked, String... params){
        boolean flag=true;

        if(!isChecked) {
            Snackbar.make(findViewById(android.R.id.content), "Please check terms and conditions box to proceed.", Snackbar.LENGTH_LONG).show();
            flag= false;
        }

        if(params[0].isEmpty()){
            mName.setError("Name field empty.");
            flag= false;
        }
        if(params[1].isEmpty()){
            mEmail.setError("Email field empty.");
            flag= false;
        }
        if(params[2].isEmpty()){
            mPassword.setError("Password field empty.");
            flag= false;
        }

        if(!params[3].equals(params[2])){
            mConfirmPassword.setError("Passwords do not match.");
            flag= false;
        }
        if(params[3].isEmpty()){
            mConfirmPassword.setError("Confirm password field empty.");
            flag= false;
        }


        return flag;
    }

    private void initSignup(){

        mName=findViewById(R.id.edt_name);
        mEmail =findViewById(R.id.edt_email);
        mPassword=findViewById(R.id.edt_password);
        mConfirmPassword=findViewById(R.id.edt_confirm_password);
        mSignup=findViewById(R.id.signupButton);
        mSignup.setOnClickListener(v -> {
            String name=mName.getText().toString().trim();
            String email=mEmail.getText().toString().trim();
            String password=mPassword.getText().toString().trim();
            String confirmPassword=mConfirmPassword.getText().toString().trim();

            if (!isValid(checkbox.isChecked(),name,email,password,confirmPassword))
                return;

            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(name).build();
                    user.updateProfile(profileUpdates).addOnCompleteListener(task1 -> {
                        if(task1.isComplete()){
                            user.sendEmailVerification();
                            Common.firebaseCurrentUser=user;
                            initUserDataAndProceed(Common.firebaseCurrentUser.getUid());
                        }

                    });

                }else{
                    Toast.makeText(this,"Something went wrong creating account",Toast.LENGTH_SHORT).show();
                }

            });
        });

    }
    private void initGoogleSignup(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        LinearLayout ggl=findViewById(R.id.ggl_sign_up_button);
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        ggl.setOnClickListener(v->{
            if(isValid(checkbox.isChecked()))
                googleSignup();
        });




    }


    private void googleSignup(){
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Common.firebaseCurrentUser=user;
                            initUserDataAndProceed(Common.firebaseCurrentUser.getUid());


                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());

                        }

                        // ...
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }
        mCallbackManager.onActivityResult(requestCode, resultCode, data);

    }

    private void initFacebookSignup(){

        mCallbackManager = CallbackManager.Factory.create();

        LoginButton loginButton = findViewById(R.id.fb_sign_up_button);
        loginButton.setReadPermissions(Arrays.asList(EMAIL));

        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG_FB, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG_FB, "facebook:onCancel");
                // ...
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG_FB, "facebook:onError", error);
                // ...
            }
        });

    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG_FB, "handleFacebookAccessToken:" + token);

        if(!isValid(checkbox.isChecked())){return;}

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG_FB, "signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        Common.firebaseCurrentUser=user;
                        initUserDataAndProceed(Common.firebaseCurrentUser.getUid());

                    } else {
                        // If sign in fails, display a message to the user.
                        LoginManager.getInstance().logOut();
                        Toast.makeText(SignupScreen.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();

                    }

                    // ...
                });
    }


}