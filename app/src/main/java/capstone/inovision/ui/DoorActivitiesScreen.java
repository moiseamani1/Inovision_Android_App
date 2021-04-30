package capstone.inovision.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import capstone.inovision.R;
import capstone.inovision.common.Common;
import capstone.inovision.containerViewHolder.ContactViewHolder;
import capstone.inovision.containerViewHolder.DoorActivityViewHolder;
import capstone.inovision.model.Contact;
import capstone.inovision.model.DoorActivity;

public class DoorActivitiesScreen extends AppCompatActivity {

    private RecyclerView recycler;
    private FirebaseRecyclerAdapter<DoorActivity, DoorActivityViewHolder> firebaseRecyclerAdapter;
    private DatabaseReference RootRef;
    private BottomSheetDialog bottomSheetDialog;
    private String sName,sDateTime,sLocation;
    private TextView nameText,locationText,dateText;
    private Button clearFilter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        getSupportActionBar().hide();
        setContentView(R.layout.activity_door_activities_screen);
        RootRef= FirebaseDatabase.getInstance().getReference();
        initBack();
        initFilter();
        initClearFilter();

    }

    void initClearFilter(){
         clearFilter = findViewById(R.id.clear_filters);
         clearFilter.setOnClickListener(v -> {
             nameText.setText("Name");
             locationText.setText("Location");
             dateText.setText("Date");
             sName=sDateTime=sLocation=null;
             initRecycler(RootRef.child("DoorActivity").child(Common.firebaseCurrentUserData.getPiId()).child(Common.firebaseCurrentUser.getUid()));
             clearFilter.setVisibility(View.GONE);
         });

    }

    void initBack() {
        Toolbar toolbar = findViewById(R.id.toolbar_door_activities);
        toolbar.setNavigationOnClickListener(v ->finish());




    }


    @Override
    protected void onStart() {
        super.onStart();
        initRecycler(RootRef.child("DoorActivity").child(Common.firebaseCurrentUserData.getPiId()).child(Common.firebaseCurrentUser.getUid()));
    }


    @Override
    public void onStop() {
        super.onStop();
        firebaseRecyclerAdapter.stopListening();
    }

    void initRecycler(Query query){

        FirebaseRecyclerOptions<DoorActivity> options= new FirebaseRecyclerOptions.Builder<DoorActivity>()
                .setQuery(query, DoorActivity.class)
                .setLifecycleOwner(this)
                .build();

        recycler=findViewById(R.id.door_activities_screen_recycler);
        recycler.setHasFixedSize(true);
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recycler.setLayoutManager(layoutManager);



        firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<DoorActivity, DoorActivityViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull DoorActivityViewHolder holder, int position, @NonNull DoorActivity model) {
                holder.name.setText(model.getName());
                holder.datetime.setText(model.getDateTime());
                holder.location.setText(model.getLocation());
                RootRef.child("Contact").child(Common.firebaseCurrentUser.getUid()).child(model.getContactId()).addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Contact contact = snapshot.getValue(Contact.class);
                                Picasso.get().load(contact.getPic()).resize(getBaseContext().getResources().getDimensionPixelSize(R.dimen.view_contact_photo_width),
                                        getBaseContext().getResources().getDimensionPixelSize(R.dimen.view_contact_photo_width)).into(holder.photo);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
            }

            @NonNull
            @Override
            public DoorActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView= LayoutInflater.from(getBaseContext()).inflate(R.layout.door_activity_item,parent,false);
                return new DoorActivityViewHolder(itemView);
            }
        };

        recycler.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

    }

    void initFilter(){

        CardView name,datetime,location;
        name=findViewById(R.id.name_filter_doors);
        datetime=findViewById(R.id.date_filter_doors);
        location=findViewById(R.id.location_filter_doors);
        name.setOnClickListener(v -> createFilterDialog("Name",sName,sLocation,sDateTime));
        datetime.setOnClickListener(v -> createFilterDialog("Date",sName,sLocation,sDateTime));
        location.setOnClickListener(v -> createFilterDialog("Location",sName,sLocation,sDateTime));

         nameText=findViewById(R.id.name_text_doors);
         locationText=findViewById(R.id.location_text_doors);
        dateText=findViewById(R.id.date_text_doors);


    }
    void createFilterDialog(String current,String ...type){

        Log.e("Filter", "filter type0: "+type[0]);
        Log.e("Filter", "filter type1: "+type[1]);
        Log.e("Filter", "filter type2: "+type[2]);


        bottomSheetDialog =new BottomSheetDialog(this);
        View bottomSheetView=this.getLayoutInflater().inflate(R.layout.filter, null);
        bottomSheetDialog.setContentView(bottomSheetView);
        TextView filterTitle=bottomSheetView.findViewById(R.id.filter_text);
        filterTitle.setText(String.format("Filter:%s",current));
        TextView applyFilter=bottomSheetView.findViewById(R.id.apply_filter_txt);
        CalendarView calendarView=bottomSheetView.findViewById(R.id.calendarView);
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth);
            sDateTime = sdf.format(calendar.getTime());
        });
        MaterialSearchBar searchBar=bottomSheetView.findViewById(R.id.filterbox);
        applyFilter.setOnClickListener(v->filter(current,searchBar,calendarView,type));
        bottomSheetDialog.show();

        if(current.equals("Date")){
            searchBar.setVisibility(View.GONE);
            sDateTime=null;
        }else{
            calendarView.setVisibility(View.GONE);

        }




    }

    void filter(String current,MaterialSearchBar searchBar,CalendarView calendarView,String ...type){

        clearFilter.setVisibility(View.VISIBLE);

        if(type[0]==null && type[1]==null&& type[2]==null){
        if(current.equals("Name")){
            sName=searchBar.getText();
            initRecycler(RootRef.child("DoorActivity").child(Common.firebaseCurrentUserData.getPiId()).child(Common.firebaseCurrentUser.getUid())
                    .orderByChild(current.toLowerCase()).startAt(sName).endAt(sName+"\uf8ff"));
            nameText.setText(String.format("Name:%s",sName));
        }
        if(current.equals("Location")){
            sLocation=searchBar.getText();
            initRecycler(RootRef.child("DoorActivity").child(Common.firebaseCurrentUserData.getPiId()).child(Common.firebaseCurrentUser.getUid())
                    .orderByChild(current.toLowerCase()).startAt(sLocation).endAt(sLocation+"\uf8ff"));
            locationText.setText(String.format("Location:%s",sLocation));

        }
        if(current.equals("Date")){

            if(sDateTime==null){SimpleDateFormat df = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
            sDateTime=df.format(Calendar.getInstance().getTime());}
            initRecycler(RootRef.child("DoorActivity").child(Common.firebaseCurrentUserData.getPiId()).child(Common.firebaseCurrentUser.getUid())
                    .orderByChild(current.toLowerCase()+"Time").startAt(sDateTime).endAt(sDateTime+"\uf8ff"));
            dateText.setText("Date:"+sDateTime);

            }
        bottomSheetDialog.dismiss();
        }

        if(type[0]==null && type[1]==null&& type[2]!=null){
            if(current.equals("Name")){
                sName=searchBar.getText();
                initRecycler(RootRef.child("DoorActivity").child(Common.firebaseCurrentUserData.getPiId()).child(Common.firebaseCurrentUser.getUid())
                        .orderByChild("name_date").startAt(sName+"_"+type[2]).endAt(sName+"_"+type[2]+"\uf8ff"));
                nameText.setText(String.format("Name:%s",sName));
            }
            if(current.equals("Location")){
                sLocation=searchBar.getText();
                initRecycler(RootRef.child("DoorActivity").child(Common.firebaseCurrentUserData.getPiId()).child(Common.firebaseCurrentUser.getUid())
                        .orderByChild("location_date").startAt(sLocation+"_"+type[2]).endAt(sLocation+"_"+type[2]+"\uf8ff"));
                locationText.setText(String.format("Location:%s",sLocation));

            }
            if(current.equals("Date")){

                if(sDateTime==null){SimpleDateFormat df = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
                    sDateTime=df.format(Calendar.getInstance().getTime());}

                initRecycler(RootRef.child("DoorActivity").child(Common.firebaseCurrentUserData.getPiId()).child(Common.firebaseCurrentUser.getUid())
                        .orderByChild(current.toLowerCase()+"Time").startAt(sDateTime).endAt(sDateTime+"\uf8ff"));
                dateText.setText("Date:"+sDateTime);

            }
            bottomSheetDialog.dismiss();
        }

        if(type[0]==null && type[1]!=null&& type[2]==null){
            if(current.equals("Name")){
                sName=searchBar.getText();
                initRecycler(RootRef.child("DoorActivity").child(Common.firebaseCurrentUserData.getPiId()).child(Common.firebaseCurrentUser.getUid())
                        .orderByChild("name_location").equalTo(sName+"_"+type[1]));
                nameText.setText(String.format("Name:%s",sName));
            }
            if(current.equals("Location")){
                sLocation=searchBar.getText();
                initRecycler(RootRef.child("DoorActivity").child(Common.firebaseCurrentUserData.getPiId()).child(Common.firebaseCurrentUser.getUid())
                        .orderByChild(current.toLowerCase()).startAt(sLocation).endAt(sLocation+"\uf8ff"));
                locationText.setText(String.format("Location:%s",sLocation));

            }
            if(current.equals("Date")){

                if(sDateTime==null){SimpleDateFormat df = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
                    sDateTime=df.format(Calendar.getInstance().getTime());}

                initRecycler(RootRef.child("DoorActivity").child(Common.firebaseCurrentUserData.getPiId()).child(Common.firebaseCurrentUser.getUid())
                        .orderByChild("location_date").equalTo(type[1]+"_"+sDateTime+"\uf8ff"));
                dateText.setText("Date:"+sDateTime);
            }
            bottomSheetDialog.dismiss();
        }

        if(type[0]==null && type[1]!=null&& type[2]!=null){
            if(current.equals("Name")){
                sName=searchBar.getText();
                initRecycler(RootRef.child("DoorActivity").child(Common.firebaseCurrentUserData.getPiId()).child(Common.firebaseCurrentUser.getUid())
                        .orderByChild("name_location_date")
                        .startAt(sName+"_"+type[1]+"_"+type[2])
                        .endAt(sName+"_"+type[1]+"_"+type[2]+"\uf8ff")
                );
                nameText.setText(String.format("Name:%s",sName));
            }
            if(current.equals("Location")){
                sLocation=searchBar.getText();
                initRecycler(RootRef.child("DoorActivity").child(Common.firebaseCurrentUserData.getPiId()).child(Common.firebaseCurrentUser.getUid())
                        .orderByChild("location_date")
                        .startAt(sLocation+"_"+type[2])
                        .endAt(sLocation+"_"+type[2]+"\uf8ff")
                );
                locationText.setText(String.format("Location:%s",sLocation));
            }
            if(current.equals("Date")){

                if(sDateTime==null){SimpleDateFormat df = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
                    sDateTime=df.format(Calendar.getInstance().getTime());}
                initRecycler(RootRef.child("DoorActivity").child(Common.firebaseCurrentUserData.getPiId()).child(Common.firebaseCurrentUser.getUid())
                        .orderByChild("location_date").startAt(type[1]+"_"+sDateTime)
                        .endAt((type[1]+"_"+sDateTime+"\uf8ff")));
                dateText.setText("Date:"+sDateTime);
            }
            bottomSheetDialog.dismiss();
        }

        if(type[0]!=null && type[1]==null&& type[2]==null){
            if(current.equals("Name")){
                sName=searchBar.getText();
                initRecycler(RootRef.child("DoorActivity").child(Common.firebaseCurrentUserData.getPiId()).child(Common.firebaseCurrentUser.getUid())
                        .orderByChild(current.toLowerCase()).startAt(sName).endAt(sName+"\uf8ff"));
                nameText.setText(String.format("Name:%s",sName));
            }
            if(current.equals("Location")){
                sLocation=searchBar.getText();
                initRecycler(RootRef.child("DoorActivity").child(Common.firebaseCurrentUserData.getPiId()).child(Common.firebaseCurrentUser.getUid())
                        .orderByChild("name_location").equalTo(type[0]+"_"+sLocation));
                locationText.setText(String.format("Location:%s",sLocation));
            }
            if(current.equals("Date")){

                if(sDateTime==null){SimpleDateFormat df = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
                    sDateTime=df.format(Calendar.getInstance().getTime());}
                initRecycler(RootRef.child("DoorActivity").child(Common.firebaseCurrentUserData.getPiId()).child(Common.firebaseCurrentUser.getUid())
                        .orderByChild("name_date")
                        .startAt(type[0]+"_"+sDateTime)
                        .endAt(type[0]+"_"+sDateTime+"\uf8ff"));
                dateText.setText("Date:"+sDateTime);
            }
            bottomSheetDialog.dismiss();
        }

        if(type[0]!=null && type[1]==null&& type[2]!=null){
            if(current.equals("Name")){
                sName=searchBar.getText();
                initRecycler(RootRef.child("DoorActivity").child(Common.firebaseCurrentUserData.getPiId()).child(Common.firebaseCurrentUser.getUid())
                        .orderByChild("name_date")
                        .startAt(sName+"_"+type[2])
                        .endAt(sName+"_"+type[2]+"\uf8ff"));
                nameText.setText(String.format("Name:%s",sName));
            }
            if(current.equals("Location")){
                sLocation=searchBar.getText();
                initRecycler(RootRef.child("DoorActivity").child(Common.firebaseCurrentUserData.getPiId()).child(Common.firebaseCurrentUser.getUid())
                        .orderByChild("name_location_date")
                        .startAt(type[0]+"_"+sLocation+"_"+type[2])
                        .endAt(type[0]+"_"+sLocation+"_"+type[2]+"\uf8ff"));
                locationText.setText(String.format("Location:%s",sLocation));
            }
            if(current.equals("Date")){

                if(sDateTime==null){SimpleDateFormat df = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
                    sDateTime=df.format(Calendar.getInstance().getTime());}
                initRecycler(RootRef.child("DoorActivity").child(Common.firebaseCurrentUserData.getPiId()).child(Common.firebaseCurrentUser.getUid())
                        .orderByChild("name_date")
                        .startAt(type[0]+"_"+sDateTime)
                        .endAt(type[0]+"_"+sDateTime+"\uf8ff"));
                dateText.setText("Date:"+sDateTime);
            }
            bottomSheetDialog.dismiss();
        }

        if(type[0]!=null && type[1]!=null&& type[2]==null){
            if(current.equals("Name")){
                sName=searchBar.getText();
                initRecycler(RootRef.child("DoorActivity").child(Common.firebaseCurrentUserData.getPiId()).child(Common.firebaseCurrentUser.getUid())
                        .orderByChild("name_location").equalTo(sName+"_"+type[1]));
                nameText.setText(String.format("Name:%s",sName));
            }
            if(current.equals("Location")){
                sLocation=searchBar.getText();
                initRecycler(RootRef.child("DoorActivity").child(Common.firebaseCurrentUserData.getPiId()).child(Common.firebaseCurrentUser.getUid())
                        .orderByChild("name_location").equalTo(type[0]+"_"+sLocation));
                locationText.setText(String.format("Location:%s",sLocation));
            }
            if(current.equals("Date")){
                if(sDateTime==null){SimpleDateFormat df = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
                    sDateTime=df.format(Calendar.getInstance().getTime());}
                initRecycler(RootRef.child("DoorActivity").child(Common.firebaseCurrentUserData.getPiId()).child(Common.firebaseCurrentUser.getUid())
                        .orderByChild("name_location_date")
                        .startAt(type[0]+"_"+type[1]+"_"+sDateTime)
                        .endAt(type[0]+"_"+type[1]+"_"+sDateTime+"\uf8ff"));
                dateText.setText("Date:"+sDateTime);
            }
            bottomSheetDialog.dismiss();
        }

        if(type[0]!=null && type[1]!=null && type[2]!=null){

            if(current.equals("Name")){
                sName=searchBar.getText();
                initRecycler(RootRef.child("DoorActivity").child(Common.firebaseCurrentUserData.getPiId()).child(Common.firebaseCurrentUser.getUid())
                        .orderByChild("name_location_date")
                        .startAt(sName+"_"+type[1]+"_"+type[2])
                        .endAt(sName+"_"+type[1]+"_"+type[2]+"\uf8ff")
                );
                nameText.setText(String.format("Name:%s",sName));
            }
            if(current.equals("Location")){
                sLocation=searchBar.getText();
                initRecycler(RootRef.child("DoorActivity").child(Common.firebaseCurrentUserData.getPiId()).child(Common.firebaseCurrentUser.getUid())
                        .orderByChild("name_location_date")
                        .startAt(type[0]+"_"+sLocation+"_"+type[2])
                        .endAt(type[0]+"_"+sLocation+"_"+type[2]+"\uf8ff"));
                locationText.setText(String.format("Location:%s",sLocation));

            }
            if(current.equals("Date")){
                if(sDateTime==null){SimpleDateFormat df = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
                    sDateTime=df.format(Calendar.getInstance().getTime());}
                initRecycler(RootRef.child("DoorActivity").child(Common.firebaseCurrentUserData.getPiId()).child(Common.firebaseCurrentUser.getUid())
                        .orderByChild("name_location_date")
                        .startAt(type[0]+"_"+type[1]+"_"+sDateTime)
                        .endAt(type[0]+"_"+type[1]+"_"+sDateTime+"\uf8ff"));
                dateText.setText("Date:"+sDateTime);
            }
            bottomSheetDialog.dismiss();
            return;

        }



    }

}



