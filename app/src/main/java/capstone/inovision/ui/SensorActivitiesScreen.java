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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import capstone.inovision.R;
import capstone.inovision.common.Common;
import capstone.inovision.containerViewHolder.SensorActivityViewHolder;
import capstone.inovision.containerViewHolder.SensorViewHolder;
import capstone.inovision.model.Sensor;
import capstone.inovision.model.SensorActivity;

public class SensorActivitiesScreen extends AppCompatActivity {

    private RecyclerView activitiesRecycler,sensorListRecycler;

    private FirebaseRecyclerAdapter<Sensor, SensorViewHolder> listFirebaseRecyclerAdapter;


    private FirebaseRecyclerAdapter<SensorActivity, SensorActivityViewHolder> firebaseRecyclerAdapter;


    private DatabaseReference RootRef;
    private BottomSheetDialog bottomSheetDialog;
    private String sSensorId,sDateTime;
    private TextView sensorIdText,dateText;
    private Button clearFilter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        getSupportActionBar().hide();
        setContentView(R.layout.activity_sensor_activities_screen);
        RootRef= FirebaseDatabase.getInstance().getReference();
        initBack();
        initFilter();
        initClearFilter();


    }


    private void initSensorList(){

        FirebaseRecyclerOptions<Sensor> options= new FirebaseRecyclerOptions.Builder<Sensor>()
                .setQuery(RootRef.child("Sensor").child(Common.firebaseCurrentUserData.getPiId()), Sensor.class)
                .setLifecycleOwner(this)
                .build();

        sensorListRecycler =findViewById(R.id.sensors_list_recycler);
        sensorListRecycler.setHasFixedSize(true);
        LinearLayoutManager layoutManager=new  LinearLayoutManager(getBaseContext(), LinearLayoutManager.HORIZONTAL, false);

        sensorListRecycler.setLayoutManager(layoutManager);



        listFirebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Sensor, SensorViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull SensorViewHolder viewHolder, int position, @NonNull Sensor model) {
                viewHolder.id.setText(String.format("id:%s",getRef(position).getKey()));
                viewHolder.location.setText(model.getLocation());
                viewHolder.label.setText(model.getLabel());
                String category=model.getCategory();
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

            @NonNull
            @Override
            public SensorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView= LayoutInflater.from(getBaseContext()).inflate(R.layout.sensor_item,parent,false);
                return new SensorViewHolder(itemView);
            }
        };

        sensorListRecycler.setAdapter(listFirebaseRecyclerAdapter);
        listFirebaseRecyclerAdapter.startListening();

    }

    private void initRecycler(Query query){

        FirebaseRecyclerOptions<SensorActivity> options= new FirebaseRecyclerOptions.Builder<SensorActivity>()
                .setQuery(query, SensorActivity.class)
                .setLifecycleOwner(this)
                .build();

        activitiesRecycler =findViewById(R.id.sensor_activities_screen_recycler);
        activitiesRecycler.setHasFixedSize(true);
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        activitiesRecycler.setLayoutManager(layoutManager);



        firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<SensorActivity, SensorActivityViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull SensorActivityViewHolder viewHolder, int position, @NonNull SensorActivity model) {

                viewHolder.datetime.setText(model.getDateTime());
                viewHolder.status.setText(model.getStatus());

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
                        });
            }

            @NonNull
            @Override
            public SensorActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView= LayoutInflater.from(getBaseContext()).inflate(R.layout.sensor_activity_item,parent,false);
                return new SensorActivityViewHolder(itemView);
            }
        };

        activitiesRecycler.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

    }


    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {


        String id=listFirebaseRecyclerAdapter.getRef(item.getOrder()).getKey();
        String category=listFirebaseRecyclerAdapter.getItem(item.getOrder()).getCategory();
        String label=listFirebaseRecyclerAdapter.getItem(item.getOrder()).getLabel();
        String location=listFirebaseRecyclerAdapter.getItem(item.getOrder()).getLocation();

        startActivity(new Intent(getBaseContext(), AddUpdateSensorScreen.class)
                .putExtra("category",category)
                .putExtra("label",label)
                .putExtra("location",location)
                .putExtra("id",id));
        return super.onContextItemSelected(item);
    }





    @Override
    protected void onStart() {
        super.onStart();
        initRecycler(RootRef.child("SensorActivity").child(Common.firebaseCurrentUserData.getPiId()));
        initSensorList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initSensorList();
    }


    private void initClearFilter(){
        clearFilter = findViewById(R.id.clear_filters_sensors);
        clearFilter.setOnClickListener(v -> {
            sensorIdText.setText("ID");
            dateText.setText("Date");
            sDateTime= sSensorId =null;
            initRecycler(RootRef.child("SensorActivity").child(Common.firebaseCurrentUserData.getPiId()));
            clearFilter.setVisibility(View.GONE);
        });

    }

    private void initBack() {
        Toolbar toolbar = findViewById(R.id.toolbar_sensor_activities);
        toolbar.setNavigationOnClickListener(v ->finish());




    }


    void initFilter(){

        CardView label,datetime;
        label=findViewById(R.id.label_filter_sensors);
        datetime=findViewById(R.id.date_filter_sensors);

        label.setOnClickListener(v -> createFilterDialog("SensorId", sSensorId,sDateTime));
        datetime.setOnClickListener(v -> createFilterDialog("Date", sSensorId,sDateTime));


        sensorIdText =findViewById(R.id.label_text_sensors);
        dateText=findViewById(R.id.date_text_sensors);


    }
    private void createFilterDialog(String current,String ...type){


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


    private void filter(String current,MaterialSearchBar searchBar,CalendarView calendarView,String ...type){
        clearFilter.setVisibility(View.VISIBLE);

        Log.e("Filter", "filter type0: "+type[0]);
        Log.e("Filter", "filter type1: "+type[1]);

        if(type[0]==null && type[1]==null){
            if(current.equals("SensorId")){
                sSensorId =searchBar.getText();

                initRecycler(RootRef.child("SensorActivity").child(Common.firebaseCurrentUserData.getPiId())
                        .orderByChild("sensorId").equalTo(sSensorId));
                sensorIdText.setText(String.format("ID:%s", sSensorId));
            }

            if(current.equals("Date")){
                if(sDateTime==null){ SimpleDateFormat df = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());sDateTime=df.format(Calendar.getInstance().getTime());}
                initRecycler(RootRef.child("SensorActivity").child(Common.firebaseCurrentUserData.getPiId())
                        .orderByChild(current.toLowerCase()+"Time").startAt(sDateTime).endAt(sDateTime+"\uf8ff"));
                Toast.makeText(this, sDateTime, Toast.LENGTH_SHORT).show();
                dateText.setText("Date:"+sDateTime);
            }
            bottomSheetDialog.dismiss();
        }

        if(type[0]==null && type[1]!=null){
            if(current.equals("SensorId")){
                sSensorId =searchBar.getText();
                initRecycler(RootRef.child("SensorActivity").child(Common.firebaseCurrentUserData.getPiId())
                        .orderByChild("sensorId_date").startAt(sSensorId +"_"+type[1]).endAt(sSensorId +"_"+type[1]+"\uf8ff"));
                sensorIdText.setText(String.format("ID:%s", sSensorId));
            }

            if(current.equals("Date")){
                if(sDateTime==null){SimpleDateFormat df = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
                    sDateTime=df.format(Calendar.getInstance().getTime());}

                initRecycler(RootRef.child("SensorActivity").child(Common.firebaseCurrentUserData.getPiId())
                        .orderByChild(current.toLowerCase()+"Time").startAt(sDateTime).endAt(sDateTime+"\uf8ff"));

                dateText.setText("Date:"+sDateTime);
            }
            bottomSheetDialog.dismiss();

        }

        if(type[0]!=null && type[1]==null){
            if(current.equals("SensorId")){
                sSensorId =searchBar.getText();
                initRecycler(RootRef.child("SensorActivity").child(Common.firebaseCurrentUserData.getPiId())
                        .orderByChild("sensorId").equalTo(sSensorId));
                sensorIdText.setText(String.format("ID:%s", sSensorId));
            }

            if(current.equals("Date")){
                if(sDateTime==null){SimpleDateFormat df = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
                    sDateTime=df.format(Calendar.getInstance().getTime());}

                initRecycler(RootRef.child("SensorActivity").child(Common.firebaseCurrentUserData.getPiId())
                        .orderByChild("sensorId_date").startAt(type[0]+"_"+sDateTime).endAt(type[0]+"_"+sDateTime+"\uf8ff"));
                dateText.setText("Date:"+sDateTime);


            }
            bottomSheetDialog.dismiss();
        }

        if(type[0]!=null && type[1]!=null){
            if(current.equals("SensorId")){
                sSensorId =searchBar.getText();
                initRecycler(RootRef.child("SensorActivity").child(Common.firebaseCurrentUserData.getPiId())
                        .orderByChild("sensorId_date").equalTo(sSensorId +"_"+type[1]+"\uf8ff"));
                sensorIdText.setText(String.format("ID:%s", sSensorId));
            }
            if(current.equals("Date")){
                if(sDateTime==null){SimpleDateFormat df = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
                    sDateTime=df.format(Calendar.getInstance().getTime());}
                initRecycler(RootRef.child("SensorActivity").child(Common.firebaseCurrentUserData.getPiId())
                        .orderByChild("sensorId_date").equalTo(type[0]+"_"+sDateTime+"\uf8ff"));
                dateText.setText("Date:"+sDateTime);
            }

            bottomSheetDialog.dismiss();
        }



    }

}