package capstone.inovision.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import capstone.inovision.R;
import capstone.inovision.common.Common;
import capstone.inovision.containerAdapter.PhotoAdapter;
import capstone.inovision.model.Contact;
import capstone.inovision.model.Photo;

public class AddUpdateContactScreen extends AppCompatActivity {

    private static final String TAG = "Add Contact Screen";
    FirebaseStorage storage;
    private static final int SELECT_PICTURES = 1;
    private CardView uploadPhoto,addContact;
    private List<Photo> previewItems=new ArrayList<>();
    private RecyclerView previewRecycler;
    private PhotoAdapter previewAdapter;
    Toolbar toolbar;
    private boolean previewChanged =false;

    private DatabaseReference RootRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        getSupportActionBar().hide();
        setContentView(R.layout.activity_add_update_contact_screen);

        initBack();
        initFileUpload();
    }


    void initBack(){
            toolbar = findViewById(R.id.toolbar_add_update_contact);
            toolbar.setNavigationOnClickListener(v -> { finish();}
            );


    }
    void initFileUpload(){
        RootRef= FirebaseDatabase.getInstance().getReference();
        storage = FirebaseStorage.getInstance();
        initPreviewRecycler();
        uploadPhoto=findViewById(R.id.uploadButton);
        uploadPhoto.setOnClickListener(v -> {
            if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){ requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},SELECT_PICTURES);
            }else{ pickImage();}
        });

        if(getIntent()!=null ){
            String nameExtra=getIntent().getStringExtra("name");
            String contactIdExtra=getIntent().getStringExtra("contactId");
            if(!(nameExtra==null) && !( contactIdExtra==null)){
                initUpdateContact(nameExtra,contactIdExtra);
                toolbar.setTitle("Update Friend");
            }else{
                initAddContact();
            }
        }

    }
    void initAddContact(){
        EditText name=findViewById(R.id.edt_name_add_update_contact);
        addContact=findViewById(R.id.add_update_contact_Button);
        addContact.setOnClickListener(v -> createContact(name.getText().toString().trim()));

    }
    void initUpdateContact(String nameExtra,String contactIdExtra){
        EditText name=findViewById(R.id.edt_name_add_update_contact);
        name.setText(nameExtra);
        name.setEnabled(false);
        addContact=findViewById(R.id.add_update_contact_Button);
        TextView addUpdateContactText=findViewById(R.id.add_update_contact_txt);
        addUpdateContactText.setText("Update Friend");
        addContact.setOnClickListener(v -> updateContact(nameExtra,contactIdExtra));
        prepopulateRecycler(nameExtra,200,previewItems,previewAdapter,contactIdExtra);



    }
    void prepopulateRecycler(String name,int limit,List list,PhotoAdapter adapter,String contactId){

        //TODO handle first and last name
        name=name.replaceAll(" ","_").toLowerCase();

        StorageReference pathReference = FirebaseStorage.getInstance().getReference().child(String.format("dataset/%s/%s|%s", Common.firebaseCurrentUser.getUid(),name,contactId));
        pathReference.list(limit).addOnSuccessListener(listResult -> {
            list.clear();
            for (StorageReference item : listResult.getItems()) {
                item.getDownloadUrl().addOnSuccessListener(uri ->
                        { list.add(new Photo(uri));adapter.notifyDataSetChanged();}
                );

            }
        });
    }

    void updateContact(String name,String contactId){
        DatabaseReference ref=RootRef.child("Contact").child(Common.firebaseCurrentUser.getUid()).child(contactId);
        Contact contact=new Contact(name);
        name=name.replaceAll(" ","_").toLowerCase();

        if (hasPreviewChanged()) {
            StorageReference pathReference = FirebaseStorage.getInstance().getReference().child("dataset").child(Common.firebaseCurrentUser.getUid()).child(String.format("%s|%s",name,contactId));
            pathReference.listAll().addOnSuccessListener(listResult -> {
                List<StorageReference> list=listResult.getItems();
                int i;
                for (i=0;i<list.size();i++){
                        list.get(i).delete()
                                .addOnFailureListener(e -> Toast.makeText(this, "An error occurred deleting contact images", Toast.LENGTH_SHORT).show());
                }
            });


        for ( int i=0;i<previewItems.size();i++){
            if(i==previewItems.size()-1){
                final StorageReference storeRef=storage.getReference().child(String.format("%s/%s/%s|%s/%d", "dataset", Common.firebaseCurrentUser.getUid(),name,contactId,i));
                Task<Uri> urlTask = storeRef
                        .putFile(previewItems.get(i).getUri()).continueWithTask(task -> {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }
                            return storeRef.getDownloadUrl();
                        }).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Uri downloadUri = task.getResult();
                                contact.setPic(downloadUri.toString());
                                ref.setValue(contact).addOnCompleteListener(task1 -> {
                                    if(task1.isSuccessful()){
                                        finish();
                                    }
                                });
                            } else {
                                // Handle failures

                            }
                        });
            }else {

                storage.getReference().child(String.format("%s/%s/%s|%s/%d", "dataset", Common.firebaseCurrentUser.getUid(),name,contactId,i))
                        .putFile(previewItems.get(i).getUri());
            }

        }


    }else { finish();}
    }
    boolean hasPreviewChanged(){
        return previewChanged;
    }


    void createContact(String name){
        Contact contact=new Contact(name);
        DatabaseReference ref=RootRef.child("Contact").child(Common.firebaseCurrentUser.getUid()).push();
        String key=ref.getKey();
        name=name.replaceAll(" ","_").toLowerCase();

        for ( int i=0;i<previewItems.size();i++){
            if(i==previewItems.size()-1){
                //TODO first and last name
                final StorageReference storeRef=storage.getReference().child(String.format("%s/%s/%s|%s/%d", "dataset", Common.firebaseCurrentUser.getUid(),name,key,i));
                Task<Uri> urlTask = storeRef
                        .putFile(previewItems.get(i).getUri()).continueWithTask(task -> {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }
                            return storeRef.getDownloadUrl();
                        }).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Uri downloadUri = task.getResult();
                                contact.setPic(downloadUri.toString());
                                ref.setValue(contact).addOnCompleteListener(task1 -> {
                                    if(task1.isSuccessful()){
                                        finish();
                                    }
                                });
                            } else {
                                // Handle failures

                            }
                        });
            }else {
                //TODO first and last name
                storage.getReference().child(String.format("%s/%s/%s|%s/%d", "dataset", Common.firebaseCurrentUser.getUid(),name,key,i))
                        .putFile(previewItems.get(i).getUri());
            }

        }




    }

    void initPreviewRecycler(){
        previewRecycler=findViewById(R.id.add_update_photos_recycler);
        previewRecycler.setHasFixedSize(true);
        previewRecycler.setLayoutManager(new LinearLayoutManager(getBaseContext(), LinearLayoutManager.HORIZONTAL, false));
        previewAdapter = new PhotoAdapter(getBaseContext(), previewItems);
        previewRecycler.setAdapter(previewAdapter);
    }


    void pickImage(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURES);


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == SELECT_PICTURES) {
            if(resultCode == Activity.RESULT_OK) {
                previewChanged =true;
                previewItems.clear();
                if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount(); //evaluate the count before the for loop --- otherwise, the count is evaluated every loop.
                    for (int i = 0; i < count; i++) {
                        previewItems.add(new Photo(data.getClipData().getItemAt(i).getUri()));
                    }
                    previewAdapter.notifyDataSetChanged();

                } else if (data.getData() != null) {
                    previewItems.add(new Photo(data.getData()));
                    previewAdapter.notifyDataSetChanged();
                } else {
                    previewAdapter.notifyDataSetChanged();
                }
            }
        }else{
            Log.d(TAG, "onActivityResult: Request code doesn't match");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==SELECT_PICTURES){
            if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                pickImage();
            }else{
                Toast.makeText(this, "Permission was not granted to access images", Toast.LENGTH_SHORT).show();
            }

        }
    }


}