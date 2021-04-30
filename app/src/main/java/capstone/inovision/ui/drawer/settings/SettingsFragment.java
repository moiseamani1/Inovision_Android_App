package capstone.inovision.ui.drawer.settings;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.MacAddress;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.NetworkSpecifier;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import android.net.wifi.WifiNetworkSuggestion;
import android.os.Build;
import android.os.Bundle;
import android.os.PatternMatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthSettings;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import capstone.inovision.R;
import capstone.inovision.common.Common;
import capstone.inovision.model.PiData;
import capstone.inovision.model.UserData;
import capstone.inovision.ui.HomeScreen;
import capstone.inovision.ui.LoginScreen;

import static android.Manifest.permission_group.LOCATION;
import static com.facebook.FacebookSdk.getApplicationContext;

public class SettingsFragment extends Fragment {

    private SettingsViewModel settingsViewModel;
    private CardView logout;
    private GoogleSignInClient mGoogleSignInClient;
    private View root;
    private TextView profileName,profileEmail,piId,profilePhone;
    private BottomSheetDialog updateProfileBottomSheetDialog,updatePiIdBottomSheetDialog,updatePhoneBottomSheetDialog;
    private DatabaseReference RootRef;


    private boolean mVerificationInProgress = false;
    private static final String KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress";
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String mVerificationId;
    private static final String TAG = "PhoneAuth";
    private FirebaseAuth mAuth;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        settingsViewModel =
                new ViewModelProvider(this).get(SettingsViewModel.class);
        root = inflater.inflate(R.layout.fragment_settings, container, false);
        RootRef= FirebaseDatabase.getInstance().getReference();
        mAuth=FirebaseAuth.getInstance();


        initLogout();
        initUpdateProfile();
        initProfileDetails();
        initPiId();
        initUserData();
        initUpdatePhone();

        initLegal();




        return root;
    }


    @Override
    public void onResume() {
        super.onResume();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if(getActivity().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 87);
            }
        }
    }



    private void initLegal(){

        CardView terms=root.findViewById(R.id.terms_card);
        CardView privacy=root.findViewById(R.id.privacy_card);


        String pdf_url="https://docs.google.com/document/d/1T-ecHyw_14O1ALVmGF1119P-0WCc1g9fTAmnTQafgTg/edit?usp=sharing";

        String pdf_url2="https://docs.google.com/document/d/1T-ecHyw_14O1ALVmGF1119P-0WCc1g9fTAmnTQafgTg/edit?usp=sharing";


        terms.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(pdf_url));
            startActivity(browserIntent);
        });

        privacy.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(pdf_url2));
            startActivity(browserIntent);
        });



    }














    private  void initUpdatePhone(){
        ImageView updatePhone=root.findViewById(R.id.update_phone_icon);

        updatePhoneBottomSheetDialog =new BottomSheetDialog(getActivity());
        View bottomSheetView=this.getLayoutInflater().inflate(R.layout.phone_verification, null);
        updatePhoneBottomSheetDialog.setContentView(bottomSheetView);
        EditText PhoneEdt=bottomSheetView.findViewById(R.id.edt_phone_number_field);
        updatePhone.setOnClickListener(v -> {updatePhoneBottomSheetDialog.show();PhoneEdt.setText(Common.firebaseCurrentUserData.getPhone());});
        PhoneEdt.setText(Common.firebaseCurrentUserData.getPhone());
        bottomSheetView.findViewById(R.id.update_phone_Button).setOnClickListener(v -> updatePhone(PhoneEdt,updatePhoneBottomSheetDialog));

    }

    private void updatePhone(EditText p,BottomSheetDialog b){
        String phone=p.getText().toString().trim();
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle("Enter Code");

        View code_sent = this.getLayoutInflater().inflate(R.layout.code_sent, null);
        EditText codeEditText=code_sent.findViewById(R.id.code_edt);
        //TODO code resend functionality and display of loading spinning progress bars
        Button resend=code_sent.findViewById(R.id.resend_button);
        Button verify=code_sent.findViewById(R.id.verify_code);
        alertDialog.setView(code_sent);

        if(phone.isEmpty()){
            b.dismiss();
            return;
        }


        final AlertDialog[] alertDialog1 = new AlertDialog[1];

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:" + credential);

                mVerificationInProgress = false;
                if (credential != null) {
                    if (credential.getSmsCode() != null) {
                        if(Common.firebaseCurrentUserData.getPhone().isEmpty()) {
                            mAuth.getCurrentUser().linkWithCredential(credential).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Common.firebaseCurrentUserData.setPhone(codeEditText.getText().toString().trim());
                                    profilePhone.setText(Common.firebaseCurrentUserData.getPhone());
                                    RootRef.child("UserData").child(Common.firebaseCurrentUser.getUid()).child("phone").setValue(Common.firebaseCurrentUserData.getPhone());
                                    alertDialog1[0].dismiss();
                                    b.dismiss();
                                }
                            });

                        }else if(!Common.firebaseCurrentUserData.getPhone().isEmpty()){
                            mAuth.getCurrentUser().unlink("phone").addOnCompleteListener(task ->{
                                    if(task.isSuccessful()){
                                        mAuth.getCurrentUser().linkWithCredential(credential)
                                                .addOnCompleteListener(task1 -> {
                                                    if(task1.isSuccessful()){
                                                        mVerificationInProgress=false;
                                                        Common.firebaseCurrentUserData.setPhone(phone);
                                                        profilePhone.setText(Common.firebaseCurrentUserData.getPhone());
                                                        RootRef.child("UserData").child(Common.firebaseCurrentUser.getUid()).child("phone").setValue(Common.firebaseCurrentUserData.getPhone());
                                                        alertDialog1[0].dismiss();
                                                        b.dismiss();
                                }
                            });}
                            }
                            );

                        }


                    } else {

                    }
                }

            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e);
                mVerificationInProgress = false;

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // ...
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // ...
                }

                // Show a message and update the UI
                // ...
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;
                verify.setOnClickListener(v -> {
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, codeEditText.getText().toString().trim());
                    if(credential!=null && Common.firebaseCurrentUserData.getPhone().isEmpty()){
                        mAuth.getCurrentUser().linkWithCredential(credential).addOnCompleteListener(task -> {
                            if(task.isSuccessful()){
                                mVerificationInProgress=false;
                                Common.firebaseCurrentUserData.setPhone(phone);
                                profilePhone.setText(Common.firebaseCurrentUserData.getPhone());
                                RootRef.child("UserData").child(Common.firebaseCurrentUser.getUid()).child("phone").setValue(Common.firebaseCurrentUserData.getPhone());
                                alertDialog1[0].dismiss();
                                b.dismiss();
                            }


                        });
                    }else if(credential!=null && !Common.firebaseCurrentUserData.getPhone().isEmpty()){
                        mAuth.getCurrentUser().unlink("phone").addOnCompleteListener(task ->{
                                if(task.isSuccessful()){
                                    mAuth.getCurrentUser().linkWithCredential(credential).addOnCompleteListener(task1 -> {
                                    if(task1.isSuccessful()){
                                        mVerificationInProgress=false;
                                        Common.firebaseCurrentUserData.setPhone(phone);
                                        profilePhone.setText(Common.firebaseCurrentUserData.getPhone());
                                        RootRef.child("UserData").child(Common.firebaseCurrentUser.getUid()).child("phone").setValue(Common.firebaseCurrentUserData.getPhone());
                                        alertDialog1[0].dismiss();
                                        b.dismiss();
                            }
                        });}}


                        );

                    }
                });
                alertDialog1[0] =alertDialog.show();


                // ...
            }
        };


        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phone)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(getActivity())                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
        mVerificationInProgress = true;



    }

    private void initUserData(){
        Switch switchTextMsg=root.findViewById(R.id.textMessagesSwitch);
        switchTextMsg.setChecked(Common.firebaseCurrentUserData.isTextMessageReceiveOn());
        switchTextMsg.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RootRef.child("UserData").child(Common.firebaseCurrentUser.getUid()).child("textMessageReceiveOn").setValue(isChecked).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    Common.firebaseCurrentUserData.setTextMessageReceiveOn(isChecked);
                }
            });
        });

        Switch switchSpeaker=root.findViewById(R.id.switchSpeaker);
        Switch switchIsArmed=root.findViewById(R.id.switchArmed);
        if(Common.firebaseCurrentUserData.getPiId().isEmpty()){
            switchSpeaker.setEnabled(false);
            switchIsArmed.setEnabled(false);
            return;
        }

        switchSpeaker.setChecked(Common.firebaseCurrentPiData.isSpeakerAnnouncementOn());
        switchSpeaker.setOnCheckedChangeListener((buttonView, isChecked) -> RootRef.child("PiData").child(Common.firebaseCurrentUserData.getPiId()).child("speakerAnnouncementOn").setValue(isChecked).addOnCompleteListener(task -> {
            if(task.isSuccessful())
                Common.firebaseCurrentPiData.setSpeakerAnnouncementOn(isChecked);
        }));

        RootRef.child("PiData").child(Common.firebaseCurrentUserData.getPiId()).child("speakerAnnouncementOn").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue().equals(true)){
                    Common.firebaseCurrentPiData.setSpeakerAnnouncementOn(true);
                    switchSpeaker.setChecked(true);
                }
                if(snapshot.getValue().equals(false)){
                    Common.firebaseCurrentPiData.setSpeakerAnnouncementOn(false);
                    switchSpeaker.setChecked(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



        switchIsArmed.setChecked(Common.firebaseCurrentPiData.isArmed());
        switchIsArmed.setOnCheckedChangeListener((buttonView, isChecked) -> RootRef.child("PiData").child(Common.firebaseCurrentUserData.getPiId()).child("armed").setValue(isChecked).addOnCompleteListener(task -> {
            if(task.isSuccessful())
                Common.firebaseCurrentPiData.setArmed(isChecked);
        }));



        RootRef.child("PiData").child(Common.firebaseCurrentUserData.getPiId()).child("armed").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue().equals(true)){
                    Common.firebaseCurrentPiData.setArmed(true);
                    switchIsArmed.setChecked(true);
                }
                if(snapshot.getValue().equals(false)){
                    Common.firebaseCurrentPiData.setArmed(false);
                    switchIsArmed.setChecked(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });





    }

    private void initPiId(){
        piId=root.findViewById(R.id.pi_id_txt);
        if(!Common.firebaseCurrentUserData.getPiId().isEmpty()){
            piId.setText(Common.firebaseCurrentUserData.getPiId());
        }

        initUpdatePiId();

    }
    private void initUpdatePiId(){
        ImageView updatePiId=root.findViewById(R.id.update_pi_icon);

        updatePiIdBottomSheetDialog =new BottomSheetDialog(getActivity());
        View bottomSheetView=this.getLayoutInflater().inflate(R.layout.update_pi_id, null);
        updatePiIdBottomSheetDialog.setContentView(bottomSheetView);
        EditText PiEdt=bottomSheetView.findViewById(R.id.edt_update_pi_id);
        updatePiId.setOnClickListener(v -> {updatePiIdBottomSheetDialog.show();PiEdt.setText(Common.firebaseCurrentUserData.getPiId());});
        PiEdt.setText(Common.firebaseCurrentUserData.getPiId());
        bottomSheetView.findViewById(R.id.update_pi_Button).setOnClickListener(v -> updatePiId(PiEdt,updatePiIdBottomSheetDialog));

    }
    private void updatePiId(EditText p,BottomSheetDialog b){
        String piId=p.getText().toString().trim();

        if(piId.isEmpty()){
            b.dismiss();
            return;
        }


        UserData userdata=new UserData();
        userdata.setPiId(piId);
        userdata.setTextMessageReceiveOn(Common.firebaseCurrentUserData.isTextMessageReceiveOn());
        userdata.setPhone(Common.firebaseCurrentUserData.getPhone());

        RootRef.child("UserData").child(Common.firebaseCurrentUser.getUid()).setValue(userdata).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Common.firebaseCurrentUserData=userdata;
                FirebaseDatabase.getInstance().getReference().child("PiData").child(piId).addListenerForSingleValueEvent(new ValueEventListener() {
                 @Override
                 public void onDataChange(@NonNull DataSnapshot snapshot) {
                     if(snapshot.getValue()==null){
                         initPiData(piId);
                     }else{
                         PiData piData=snapshot.getValue(PiData.class);
                         Common.firebaseCurrentPiData=piData;
                     }


                 }

                 @Override
                 public void onCancelled(@NonNull DatabaseError error) {

                 }
             });




       this.piId.setText(piId);
       b.dismiss();
            }
        });


    }


    private void initPiData(String PiId){
        PiData piData=new PiData();
        piData.setArmed(true);
        piData.setDoorOpen(false);
        piData.setSpeakerAnnouncementOn(true);
        piData.setPiAndCloudSynced(true);
        piData.setPiModified(false);
        piData.setCloudModified(false);

        FirebaseDatabase.getInstance().getReference().child("PiData").child(PiId).setValue(piData).addOnCompleteListener(task->{
            Common.firebaseCurrentPiData=piData;
        });

    }

    private void initProfileDetails(){
        profileName=root.findViewById(R.id.profile_name);
        profileEmail=root.findViewById(R.id.profile_email);
        profilePhone=root.findViewById(R.id.phone_number_txt);

        profileName.setText(Common.firebaseCurrentUser.getDisplayName());
        profileEmail.setText(Common.firebaseCurrentUser.getEmail());

        if(!Common.firebaseCurrentUserData.getPhone().isEmpty()){
            profilePhone.setText(Common.firebaseCurrentUserData.getPhone());
        }

    }

    private void initUpdateProfile(){
         updateProfileBottomSheetDialog =new BottomSheetDialog(getActivity());
        View bottomSheetView=this.getLayoutInflater().inflate(R.layout.update_profile, null);
        updateProfileBottomSheetDialog.setContentView(bottomSheetView);
        ImageView update_icon=root.findViewById(R.id.update_profile_icon);
        update_icon.setOnClickListener(v-> updateProfileBottomSheetDialog.show());


        EditText nameEdt=bottomSheetView.findViewById(R.id.edt_update_name);nameEdt.setText(Common.firebaseCurrentUser.getDisplayName());
        EditText emailEdt=bottomSheetView.findViewById(R.id.edt_update_email);emailEdt.setText(Common.firebaseCurrentUser.getEmail());


        bottomSheetView.findViewById(R.id.update_profile_Button)
                .setOnClickListener(v->updateProfile(nameEdt,emailEdt, updateProfileBottomSheetDialog));

        bottomSheetView.findViewById(R.id.delete_profile_Button)
                .setOnClickListener(v->deleteProfile());


    }

    private void updateProfile(EditText n,EditText e,BottomSheetDialog b){
        String name=n.getText().toString().trim();
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        user.updateProfile(profileUpdates).addOnCompleteListener(task -> {
            profileName.setText(name);
        });
        String email=e.getText().toString().trim();
        user.updateEmail(email).addOnCompleteListener(task -> {
            profileEmail.setText(email);
        });
        b.dismiss();

    }

    private void deleteProfile(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle("Do you confirm deleting your account?");
        View reauthenticate = this.getLayoutInflater().inflate(R.layout.reauthenticate, null);
        EditText email=reauthenticate.findViewById(R.id.edt_reauthenticate_email);
        EditText password=reauthenticate.findViewById(R.id.edt_reauthenticate_password);
        alertDialog.setView(reauthenticate);

        alertDialog.setPositiveButton("CONFIRM", (dialog, which) -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            AuthCredential credential=null, googleCredential=null,facebookCredential=null;
            try{
             credential = EmailAuthProvider
                    .getCredential(email.getText().toString().trim(), password.getText().toString());

            }catch (Exception ignored){
            }

            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getActivity());
            if(acct != null) {
                 googleCredential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
                 credential=googleCredential;
            }
            if (AccessToken.getCurrentAccessToken() != null && com.facebook.Profile.getCurrentProfile() != null){
             facebookCredential= FacebookAuthProvider.getCredential(AccessToken.getCurrentAccessToken().getToken());
             credential=facebookCredential;
            }



            if (user != null) {

                user.reauthenticate(credential).addOnCompleteListener(task -> {

                    if(task.isSuccessful()){
                        user.delete().addOnCompleteListener(t -> {
                            if(t.isSuccessful()){

                                LoginManager.getInstance().logOut();


                                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                        .requestIdToken(getString(R.string.default_web_client_id))
                                        .requestEmail()
                                        .build();
                                mGoogleSignInClient = GoogleSignIn.getClient(getActivity(),gso);
                                mGoogleSignInClient.signOut();
                                startActivity(new Intent(getActivity(), LoginScreen.class));
                                updateProfileBottomSheetDialog.dismiss();
                                dialog.dismiss();
                                getActivity().finish();

                            }else{
                                Log.d("DELETING", "deleteProfile: Something went wrong with deleting the account");
                            }
                        });
                    }else{
                        Log.d("DELETING", "deleteProfile: Something went wrong with re-authenticating the account");
                    }
                });



            }




        });

        alertDialog.setNegativeButton("CANCEL", (dialog, which) -> {
           dialog.dismiss();
        });

        alertDialog.show();

    }


    private void initLogout(){
        logout=root.findViewById(R.id.logout_card);
        logout.setOnClickListener(v->{
            FirebaseAuth.getInstance().signOut();
            if (AccessToken.getCurrentAccessToken() != null && com.facebook.Profile.getCurrentProfile() != null){
                LoginManager.getInstance().logOut();
            }
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();
            mGoogleSignInClient = GoogleSignIn.getClient(getActivity(),gso);
            mGoogleSignInClient.signOut();


            Common.firebaseCurrentUserData=null;Common.firebaseCurrentUser=null;Common.firebaseCurrentPiData=null;
            startActivity(new Intent(getActivity(), LoginScreen.class));
            getActivity().finish();


        });



    }

//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, mVerificationInProgress);
//    }
//
//    @Override
//    protected void onRestoreInstanceState(Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//        mVerificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS);
//    }



}