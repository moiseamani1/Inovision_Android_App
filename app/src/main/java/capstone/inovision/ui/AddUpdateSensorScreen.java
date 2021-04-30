package capstone.inovision.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import capstone.inovision.R;
import capstone.inovision.common.Common;

public class AddUpdateSensorScreen extends AppCompatActivity {


    private EditText labelEdt,locationEdt;
    private Spinner category_spinner;
    private DatabaseReference RootRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        getSupportActionBar().hide();
        setContentView(R.layout.activity_add_update_sensor_screen);
        initBack();
        initFields();
        initUpdateSensor();
        RootRef= FirebaseDatabase.getInstance().getReference();
    }


    private void initBack(){
        Toolbar toolbar = findViewById(R.id.toolbar_add_update_sensor);
        toolbar.setNavigationOnClickListener(v -> { finish();}
        );


    }
    private void initFields(){
        labelEdt=findViewById(R.id.edt_label_add_update_sensor);
        locationEdt=findViewById(R.id.edt_location_add_update_sensor);
        category_spinner=findViewById(R.id.category_spinner);

        if(getIntent()!=null){
            labelEdt.setText(getIntent().getStringExtra("label"));
            locationEdt.setText(getIntent().getStringExtra("location"));
            ArrayAdapter<String> array_spinner=(ArrayAdapter<String>)category_spinner.getAdapter();
            category_spinner. setSelection(array_spinner.getPosition(getIntent().getStringExtra("category")));
            category_spinner.setEnabled(false);
        }

    }
    private void initUpdateSensor(){
        CardView updateButton=findViewById(R.id.add_update_sensor_Button);
        updateButton.setOnClickListener(v -> {
           updateSensor();
        });

    }

    private void updateSensor(){

        if(labelEdt.getText().toString().isEmpty()){
            labelEdt.setError("Label empty");
            return;
        }
        if(locationEdt.getText().toString().isEmpty()){
            locationEdt.setError("Location empty");
            return;
        }
        HashMap<String,String> map=new HashMap<>();
        map.put("label",labelEdt.getText().toString().trim());
        map.put("location",locationEdt.getText().toString().trim());
        if(getIntent()!=null) {
            map.put("category",getIntent().getStringExtra("category"));
            RootRef.child("Sensor").child(Common.firebaseCurrentUser.getUid())
                    .child(getIntent().getStringExtra("id"))
                    .setValue(map).addOnCompleteListener(task -> {
                if(task.isSuccessful())
                    finish();
            });}
    }
}