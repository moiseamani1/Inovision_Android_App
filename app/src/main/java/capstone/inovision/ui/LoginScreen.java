package capstone.inovision.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Arrays;

import capstone.inovision.R;
import capstone.inovision.common.Common;
import capstone.inovision.model.PiData;
import capstone.inovision.model.UserData;


public class LoginScreen extends AppCompatActivity {

    private TextView signup,forgotPassword;
    private EditText mEmail,mPassword;
    private CardView mLogin;
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
        setContentView(R.layout.activity_login_screen);


        mAuth=FirebaseAuth.getInstance();
        initRedirectSignUp();
        initLogin();
        initGoogleLogin();
        initFacebookLogin();
        initPasswordRecovery();


    }


    private void initProceed(String uid){
        FirebaseDatabase.getInstance().getReference().child("UserData").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue()==null){
                    initUserDataAndProceed(uid);
                }else{
                    UserData userData=snapshot.getValue(UserData.class);
                    Common.firebaseCurrentUserData=userData;
                    String PiId=Common.firebaseCurrentUserData.getPiId();

                    if(PiId.isEmpty()){
                        startActivity(new Intent(getBaseContext(), HomeScreen.class));
                        finish();
                    }else{
                        FirebaseDatabase.getInstance().getReference().child("PiData").child(PiId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.getValue()==null){
                                    initPiDataAndProceed(PiId);
                                }else{
                                    PiData piData=snapshot.getValue(PiData.class);
                                    Common.firebaseCurrentPiData=piData;
                                    startActivity(new Intent(getBaseContext(), HomeScreen.class));
                                    finish();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }



                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initPiDataAndProceed(String PiId){
        PiData piData=new PiData();
        piData.setArmed(true);
        piData.setDoorOpen(false);
        piData.setSpeakerAnnouncementOn(true);
        piData.setPiAndCloudSynced(true);
        piData.setPiModified(false);
        piData.setCloudModified(false);

        FirebaseDatabase.getInstance().getReference().child("PiData").child(PiId).setValue(piData).addOnCompleteListener(task->{
            Common.firebaseCurrentPiData=piData;
            startActivity(new Intent(getBaseContext(), HomeScreen.class));
            finish();

        });

    }


    private void initUserDataAndProceed(String uid){

        UserData userData=new UserData();
        userData.setPiId("");
        userData.setPhone("");
        userData.setTextMessageReceiveOn(false);

        FirebaseDatabase.getInstance().getReference().child("UserData").child(uid).setValue(userData).addOnCompleteListener(task -> {
            Common.firebaseCurrentUserData=userData;
            startActivity(new Intent(this, HomeScreen.class));
            finish();

        });
    }



    private void initPasswordRecovery(){
        forgotPassword=findViewById(R.id.forgot_password_txt);
        forgotPassword.setOnClickListener(v->{
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle("Recover Password");

            View recovery_email = this.getLayoutInflater().inflate(R.layout.recovery_email, null);
            EditText recoveryEditText=recovery_email.findViewById(R.id.edt_recovery_email);
            alertDialog.setView(recovery_email);
            alertDialog.setMessage("An email would be sent to you with instructions to recover your password.");
            alertDialog.setNegativeButton("CANCEL", (dialog, which) -> {
                dialog.dismiss();
            });
            alertDialog.setPositiveButton("CONFIRM", (dialog, which) -> {
                mAuth.sendPasswordResetEmail(recoveryEditText.getText().toString())
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(LoginScreen.this, "Email sent successfully", Toast.LENGTH_SHORT).show();
                            }
                        });
            });

            alertDialog.show();
        });
    }
    private void initRedirectSignUp(){
        signup=findViewById(R.id.signup_on_login_screen_txt);
        signup.setOnClickListener(v -> {
            startActivity(new Intent(this,SignupScreen.class));
            finish();
        });

    }


    private void initLogin(){
        mEmail =findViewById(R.id.login_email);
        mPassword=findViewById(R.id.login_password);
        mLogin=findViewById(R.id.loginButton);
        mLogin.setOnClickListener(v -> {
            String email=mEmail.getText().toString().trim();
            String password=mPassword.getText().toString().trim();
            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    Common.firebaseCurrentUser=FirebaseAuth.getInstance().getCurrentUser();
                    initProceed(Common.firebaseCurrentUser.getUid());
                }else{
                    Toast.makeText(this,"Username/Password does not exist",Toast.LENGTH_SHORT).show();
                }

            });
        });

    }


    private void initGoogleLogin(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        LinearLayout ggl=findViewById(R.id.ggl_login_button);
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        ggl.setOnClickListener(v->{
            googleLogin();
        });




    }


    private void googleLogin(){
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
                           initProceed(Common.firebaseCurrentUser.getUid());

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Snackbar.make(findViewById(android.R.id.content), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();

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




    private void initFacebookLogin(){

        mCallbackManager = CallbackManager.Factory.create();

        LoginButton loginButton = findViewById(R.id.login_button);
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

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG_FB, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Common.firebaseCurrentUser=user;
                          initProceed(Common.firebaseCurrentUser.getUid());
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG_FB, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginScreen.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }

                        // ...
                    }
                });
    }

}