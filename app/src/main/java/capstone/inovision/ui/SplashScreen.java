package capstone.inovision.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import capstone.inovision.R;
import capstone.inovision.common.Common;
import capstone.inovision.model.PiData;
import capstone.inovision.model.UserData;

public class SplashScreen extends AppCompatActivity {

    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        getSupportActionBar().hide();
        setContentView(R.layout.activity_splash_screen);


        mAuth= FirebaseAuth.getInstance();
        FirebaseUser user=mAuth.getCurrentUser();
        if(user!=null){
            Common.firebaseCurrentUser=user;
            initProceed(user.getUid());
        }else{
            startActivity(new Intent(getBaseContext(), LoginScreen.class));
            finish();
        }
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
}