package capstone.inovision.ui.drawer.home;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.appevents.suggestedevents.ViewOnClickListener;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import capstone.inovision.R;
import capstone.inovision.common.Common;
import capstone.inovision.containerViewHolder.DoorActivityViewHolder;
import capstone.inovision.containerViewHolder.SensorActivityViewHolder;
import capstone.inovision.model.Contact;
import capstone.inovision.model.DoorActivity;
import capstone.inovision.model.Sensor;
import capstone.inovision.model.SensorActivity;
import capstone.inovision.ui.AddUpdateContactScreen;
import capstone.inovision.ui.AddUpdateSensorScreen;
import capstone.inovision.ui.DoorActivitiesScreen;
import capstone.inovision.ui.SensorActivitiesScreen;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;

    private View root;

    private DatabaseReference RootRef;
    private DatabaseReference DoorActivity,SensorActivity,Contact,Sensor;
    private CardView doorActivityCard,addfriendCard,sensorActivityCard,addSensorCard,call911;
    private TextView noActivityDoor,noActivitySensor;
    private List<DoorActivity> doorActivitiesList= new ArrayList<>();
    private List<SensorActivity> sensorActivitiesList= new ArrayList<>();
    private FirebaseRecyclerAdapter<DoorActivity, DoorActivityViewHolder> doorFirebaseRecyclerAdapter;
    private FirebaseRecyclerAdapter<SensorActivity, SensorActivityViewHolder> sensorFirebaseRecyclerAdapter;
    private RecyclerView doorsRecycler,sensorsRecycler;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);
        root = inflater.inflate(R.layout.fragment_home, container, false);
        RootRef= FirebaseDatabase.getInstance().getReference();
        initDoorActivity(root);
        initSensorActivity(root);
        initLockAndUnlock(root);

        init911(root);
        return root;
    }


    @Override
    public void onResume() {
        super.onResume();
        if (getActivity().checkSelfPermission( Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                getActivity().requestPermissions( new String[]{Manifest.permission.CALL_PHONE}, 1);
            }
    }

    private void init911(View root){


        BottomSheetDialog emergencyDialog =new BottomSheetDialog(getActivity());
        View bottomSheetView=this.getLayoutInflater().inflate(R.layout.emergency_assistance, null);
        emergencyDialog.setContentView(bottomSheetView);
        CardView dial911=bottomSheetView.findViewById(R.id.dial_911);

        call911=root.findViewById(R.id.button911);
        call911.setOnClickListener(v -> {
            emergencyDialog.show();
        });

        dial911.setOnClickListener(v -> launch911Call());
    }

    private void launch911Call(){
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:123456789"));

        if (ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        startActivity(callIntent);
    }

    public void initLockAndUnlock(View root){
        CardView lockButton=root.findViewById(R.id.lockButton);
        CardView unlockButton=root.findViewById(R.id.unlockButton);

        if(Common.firebaseCurrentPiData==null || Common.firebaseCurrentUserData.getPiId().isEmpty())
            return;



        lockButton.setOnClickListener(v -> RootRef.child("PiData").child(Common.firebaseCurrentUserData.getPiId()).child("doorOpen").setValue(false).addOnCompleteListener(task -> {
            if(task.isSuccessful()){lockButton.setVisibility(View.GONE);unlockButton.setVisibility(View.VISIBLE);}
        }));

        unlockButton.setOnClickListener(v -> RootRef.child("PiData").child(Common.firebaseCurrentUserData.getPiId()).child("doorOpen").setValue(true).addOnCompleteListener(task -> {
            if(task.isSuccessful()){ lockButton.setVisibility(View.VISIBLE);unlockButton.setVisibility(View.GONE);}
        }));

        RootRef.child("PiData").child(Common.firebaseCurrentUserData.getPiId()).child("doorOpen").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue().equals(true)){
                    lockButton.setVisibility(View.VISIBLE);
                    unlockButton.setVisibility(View.GONE);
                }else if(snapshot.getValue().equals(false)){
                    lockButton.setVisibility(View.GONE);
                    unlockButton.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }

    public void initDoorActivity(View root){
        DoorActivity= RootRef.child("DoorActivity").child(Common.firebaseCurrentUserData.getPiId()).child(Common.firebaseCurrentUser.getUid());
        Contact= RootRef.child("Contact").child(Common.firebaseCurrentUser.getUid());
        doorActivityCard=root.findViewById(R.id.doorActivityCard);
        addfriendCard=root.findViewById(R.id.add_friends);

        addfriendCard.setOnClickListener(v -> startActivity(new Intent(getActivity(), AddUpdateContactScreen.class)));
        noActivityDoor=root.findViewById(R.id.no_activity_door);

        DoorActivity.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() == null) {
                    noActivityDoor.setVisibility(View.VISIBLE);
                    doorActivityCard.setVisibility(View.GONE);
                }else{
                    noActivityDoor.setVisibility(View.GONE);
                    doorActivityCard.setVisibility(View.VISIBLE);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        Contact.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() == null) {
                    // The child doesn't exist
                    addfriendCard.setVisibility(View.VISIBLE);
                }else{
                    addfriendCard.setVisibility(View.GONE);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

       //Check if there are no contacts or no door activities



        initDoorRecycler(root);

    }

    void initDoorRecycler(View root){
        Query query= RootRef.child("DoorActivity").child(Common.firebaseCurrentUserData.getPiId()).child(Common.firebaseCurrentUser.getUid()).limitToLast(5);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue()!=null){




                    doorsRecycler=root.findViewById(R.id.door_recycler_home);
                    doorsRecycler.setVisibility(View.VISIBLE);

                    LinearLayoutManager layoutManager=new LinearLayoutManager(getContext());
                    layoutManager.setReverseLayout(true);
                    layoutManager.setStackFromEnd(true);
                    doorsRecycler.setLayoutManager(layoutManager);
                    doorsRecycler.setHasFixedSize(true);


                    FirebaseRecyclerOptions<DoorActivity> options= new FirebaseRecyclerOptions.Builder<DoorActivity>()
                            .setQuery(query,DoorActivity.class)
                            .setLifecycleOwner(getActivity())
                            .build();

                    doorFirebaseRecyclerAdapter =
                            new FirebaseRecyclerAdapter<DoorActivity, DoorActivityViewHolder>(options){


                                @NonNull
                                @Override
                                public DoorActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                                    View view=LayoutInflater.from(getContext()).inflate(R.layout.door_activity_item,parent,false);
                                    return new DoorActivityViewHolder(view);
                                }

                                @Override
                                protected void onBindViewHolder(@NonNull DoorActivityViewHolder viewHolder, int position, @NonNull DoorActivity model) {


                                    viewHolder.itemView.setOnClickListener(v -> startActivity(new Intent(getActivity(),DoorActivitiesScreen.class)));
                                    viewHolder.name.setText(model.getName());
                                    viewHolder.datetime.setText(model.getDateTime());
                                    RootRef.child("Contact").child(Common.firebaseCurrentUser.getUid()).child(model.getContactId()).addListenerForSingleValueEvent(
                                            new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            Contact contact = snapshot.getValue(Contact.class);
                                            Picasso.get().load(contact.getPic()).resize(getActivity().getResources().getDimensionPixelSize(R.dimen.view_contact_photo_width),
                                                    getActivity().getResources().getDimensionPixelSize(R.dimen.view_contact_photo_width)).into(viewHolder.photo);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                   viewHolder.location.setText(model.getLocation());
                                }


                            };


                    doorsRecycler.setAdapter(doorFirebaseRecyclerAdapter);

                    doorFirebaseRecyclerAdapter.startListening();

                    query.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                            doorsRecycler.smoothScrollToPosition(doorsRecycler.getAdapter().getItemCount());
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

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }

    public void initSensorActivity(View root){
        SensorActivity= RootRef.child("SensorActivity").child(Common.firebaseCurrentUserData.getPiId());
        Sensor= RootRef.child("Sensor").child(Common.firebaseCurrentUserData.getPiId());
        sensorActivityCard=root.findViewById(R.id.sensorActivityCard);



        noActivitySensor=root.findViewById(R.id.no_activity_sensor);




        SensorActivity.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() == null) {
                    // The child doesn't exist
                    noActivitySensor.setText("No Activity yet.");
                    noActivitySensor.setVisibility(View.VISIBLE);
                    sensorActivityCard.setVisibility(View.GONE);
                }else{
                    noActivitySensor.setVisibility(View.GONE);
                    sensorActivityCard.setVisibility(View.VISIBLE);
                    if(Common.firebaseCurrentUserData.getPiId().isEmpty()){
                        noActivitySensor.setText("Add Pi-ID in settings");
                        noActivitySensor.setVisibility(View.VISIBLE);
                        sensorActivityCard.setVisibility(View.GONE);
                    }




                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        initSensorRecycler(root);




    }

    void initSensorRecycler(View root){
        Query query= RootRef.child("SensorActivity").child(Common.firebaseCurrentUserData.getPiId()).limitToLast(5);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue()!=null){




                    sensorsRecycler=root.findViewById(R.id.sensor_recycler_home);
                    sensorsRecycler.setVisibility(View.VISIBLE);


                    LinearLayoutManager layoutManager=new LinearLayoutManager(getContext());
                    layoutManager.setReverseLayout(true);
                    layoutManager.setStackFromEnd(true);
                    sensorsRecycler.setLayoutManager(layoutManager);
                    sensorsRecycler.setHasFixedSize(true);


                    FirebaseRecyclerOptions<SensorActivity> options= new FirebaseRecyclerOptions.Builder<SensorActivity>()
                            .setQuery(query,SensorActivity.class)
                            .setLifecycleOwner(getActivity())
                            .build();

                    sensorFirebaseRecyclerAdapter =
                            new FirebaseRecyclerAdapter<SensorActivity, SensorActivityViewHolder>(options){


                                @NonNull
                                @Override
                                public SensorActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                                    View view=LayoutInflater.from(getContext()).inflate(R.layout.sensor_activity_item,parent,false);
                                    return new SensorActivityViewHolder(view);
                                }

                                @Override
                                protected void onBindViewHolder(@NonNull SensorActivityViewHolder viewHolder, int position, @NonNull SensorActivity model) {


                                    viewHolder.itemView.setOnClickListener(v -> startActivity(new Intent(getActivity(), SensorActivitiesScreen.class)));

                                    viewHolder.datetime.setText(model.getDateTime());
                                    viewHolder.status.setText(model.getStatus());

                                    if(model.getSensorId()!=null){
                                    RootRef.child("Sensor").child(Common.firebaseCurrentUserData.getPiId()).child(model.getSensorId()).addListenerForSingleValueEvent(
                                            new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    Sensor sensor = snapshot.getValue(Sensor.class);
                                                    viewHolder.label.setText(sensor.getLabel());
                                                    viewHolder.location.setText(sensor.getLocation());
                                                    String category=sensor.getCategory();
                                                    if(category.equals("Window")){

                                                        viewHolder.categoryImg.setImageResource(R.drawable.ic_window);

                                                    }

                                                    if(category.equals("Garage")){
                                                        viewHolder.categoryImg.setImageResource(R.drawable.ic_private_garage);
                                                    }

                                                    if(category.equals("Carbon Monoxide")){
                                                        viewHolder.categoryImg.setImageResource(R.drawable.ic_radiation);
                                                    }

                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });}
                                }


                            };


                    sensorsRecycler.setAdapter(sensorFirebaseRecyclerAdapter);

                    sensorFirebaseRecyclerAdapter.startListening();

                    query.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                            sensorsRecycler.smoothScrollToPosition(sensorsRecycler.getAdapter().getItemCount());
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

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }

    @Override
    public void onStop() {
        super.onStop();
        if(doorFirebaseRecyclerAdapter !=null){
        doorFirebaseRecyclerAdapter.stopListening();
        }

        if(sensorFirebaseRecyclerAdapter !=null){
            sensorFirebaseRecyclerAdapter.stopListening();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if(doorFirebaseRecyclerAdapter !=null){
            doorFirebaseRecyclerAdapter.startListening();
            }

        if(sensorFirebaseRecyclerAdapter !=null){
            sensorFirebaseRecyclerAdapter.startListening();
        }

    }
}