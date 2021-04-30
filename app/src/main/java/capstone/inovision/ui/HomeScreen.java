package capstone.inovision.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import capstone.inovision.R;
import capstone.inovision.Remote.APIService;
import capstone.inovision.common.Common;
import capstone.inovision.model.DoorActivity;
import capstone.inovision.model.MyResponse;
import capstone.inovision.model.Notification;
import capstone.inovision.model.Sender;
import capstone.inovision.model.Token;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeScreen extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private DatabaseReference RootRef;
    private APIService mService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mService= Common.getFCMService();
        DrawerLayout drawer = findViewById(R.id.drawer_layout);


        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_contacts, R.id.nav_settings)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        View headerView=navigationView.getHeaderView(0);
        TextView profileName=headerView.findViewById(R.id.client_name_sidebar);
        profileName.setText(String.format("Hi %s,", FirebaseAuth.getInstance().getCurrentUser().getDisplayName()));

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener( this, instanceIdResult -> {
            String newToken = instanceIdResult.getToken();
            updateToken(newToken);
            Log.e("newToken",newToken);

        });

        initNotifications();
    }

    private void updateToken(String newToken) {
        FirebaseDatabase db= FirebaseDatabase.getInstance();
        DatabaseReference tokens=db.getReference("Tokens");
        Token token=new Token(newToken,false);
        tokens.child(Common.firebaseCurrentUser.getUid()).setValue(token);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home_screen, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


    private void initNotifications(){

        RootRef= FirebaseDatabase.getInstance().getReference();
        if(!Common.firebaseCurrentUserData.getPiId().isEmpty()){
            RootRef.child("DoorActivity").child(Common.firebaseCurrentUserData.getPiId()).child(Common.firebaseCurrentUser.getUid()).limitToLast(5).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    DoorActivity doorActivity=snapshot.getValue(DoorActivity.class);
                    Log.e("Notifications", "onChildAdded: Door activity triggered for "+ doorActivity.getName());
                    Calendar now = Calendar.getInstance();
                    Calendar tmp = (Calendar) now.clone();
                    tmp.add(Calendar.MINUTE, -5);

                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy kk:mm");
                    String tnow = sdf.format(now.getTime());

                    String dateTime=doorActivity.getDateTime();
                    Log.e("Visual time", "on: "+tnow);
                    Log.e("Visual time", "on: "+dateTime);
                    if(tnow.compareTo(dateTime)==0 ){

                        sendNotificationDoorActivity(doorActivity.getName(),doorActivity.getLocation(),dateTime);
                    }


                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }

    }

    void sendNotificationDoorActivity(String visitor,String location,String time){
        DatabaseReference token=RootRef.child("Tokens");
        token.child(Common.firebaseCurrentUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        Token token=dataSnapshot.getValue(Token.class);
                        final Notification notification=new Notification(String.format("%s spotted at the %s at %s.",visitor,location,time),"Inovision");
                        Sender content=new Sender(token.getToken(),notification);
                        mService.sendNotification(content).enqueue(new Callback<MyResponse>() {
                            @Override
                            public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {



                            }

                            @Override
                            public void onFailure(Call<MyResponse> call, Throwable t) {
                                Log.e("ERROR",t.getMessage());

                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


    }

    @Override
    public void onBackPressed() {

    }
}