package capstone.inovision.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.facebook.internal.Utility;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import capstone.inovision.R;
import capstone.inovision.common.Common;
import capstone.inovision.containerAdapter.PhotoAdapter;
import capstone.inovision.containerViewHolder.DoorActivityViewHolder;
import capstone.inovision.containerViewHolder.MinimizedActivityViewHolder;
import capstone.inovision.containerViewHolder.PhotoViewHolder;
import capstone.inovision.helper.RecyclerItemClickListener;
import capstone.inovision.model.Contact;
import capstone.inovision.model.DoorActivity;
import capstone.inovision.model.Photo;

public class ViewContactScreen extends AppCompatActivity {

    private Toolbar toolbar;
    private String name,contactId;

    private List<Photo> savedPhotoItems =new ArrayList<>();
    private List<Photo> viewAllItems =new ArrayList<>();
    private RecyclerView savedPhotoRecycler;
    private PhotoAdapter savedPhotoAdapter;
    private DatabaseReference RootRef;

    private FirebaseRecyclerAdapter<DoorActivity, MinimizedActivityViewHolder> firebaseRecyclerAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        getSupportActionBar().hide();
        setContentView(R.layout.activity_view_contact_screen);
        RootRef= FirebaseDatabase.getInstance().getReference();
        initBack();
        //initProfile();
        initDeleteContact();
        initUpdateContact();
        //initViewAll();
    }

    private void initActivityLog(){
        CardView activityCard=findViewById(R.id.contacts_activity);
        TextView noActivity=findViewById(R.id.no_activity_door_contact);

        RecyclerView activityRecycler=findViewById(R.id.minimized_activity_recycler);
        activityRecycler.setHasFixedSize(true);
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        activityRecycler.setLayoutManager(layoutManager);
        activityRecycler.setHasFixedSize(true);
        Query query= RootRef.child("DoorActivity").child(Common.firebaseCurrentUserData.getPiId()).child(Common.firebaseCurrentUser.getUid()).orderByChild("contactId").equalTo(contactId);

        FirebaseRecyclerOptions<DoorActivity> options= new FirebaseRecyclerOptions.Builder<DoorActivity>()
                .setQuery(query,DoorActivity.class)
                .setLifecycleOwner(this)
                .build();


        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue()!=null){
                    firebaseRecyclerAdapter=
                            new FirebaseRecyclerAdapter<DoorActivity, MinimizedActivityViewHolder>(options){
                                @NonNull
                                @Override
                                public MinimizedActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                                    View view=LayoutInflater.from(getBaseContext()).inflate(R.layout.minimized_door_activity_item,parent,false);
                                    return new MinimizedActivityViewHolder(view);
                                }
                                @Override
                                protected void onBindViewHolder(@NonNull MinimizedActivityViewHolder holder, int position, @NonNull DoorActivity model) {

                                    holder.location.setText(model.getLocation());
                                    holder.datetime.setText(model.getDateTime());

                                }
                            };
                    activityRecycler.setAdapter(firebaseRecyclerAdapter);

                    firebaseRecyclerAdapter.startListening();

                    query.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                            activityRecycler.smoothScrollToPosition(activityRecycler.getAdapter().getItemCount());
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

                    activityCard.setVisibility(View.VISIBLE);
                    noActivity.setVisibility(View.GONE);
                }else {
                    activityCard.setVisibility(View.GONE);
                    noActivity.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
    private void initViewAll() {

        viewAllItems.clear();
        TextView viewAllTxt=findViewById(R.id.txt_view_photos);
        LayoutInflater layoutInflater=this.getLayoutInflater();
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        int mNoOfColumns = Common.calculateNoOfColumns(getApplicationContext(),140);

        viewAllTxt.setOnClickListener(v -> {

            View view_all = layoutInflater.inflate(R.layout.view_all, null);
            RecyclerView view_all_recycler=view_all.findViewById(R.id.view_all_recycler);
            view_all_recycler.setHasFixedSize(true);


            view_all_recycler.setLayoutManager(new GridLayoutManager(getBaseContext(), mNoOfColumns));
            PhotoAdapter viewAllAdapter = new PhotoAdapter(getBaseContext(), viewAllItems);
            view_all_recycler.addOnItemTouchListener(new RecyclerItemClickListener(this, view_all_recycler ,new RecyclerItemClickListener.OnItemClickListener() {
                @Override public void onItemClick(View view, int position) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(ViewContactScreen.this,android.R.style.Theme_Black_NoTitleBar_Fullscreen);

                    View openPhoto = layoutInflater.inflate(R.layout.open_photo, null);
                    final ImageView img = openPhoto.findViewById(R.id.imageView2);


                    Picasso.get().load(viewAllAdapter.getURI(position)).into(img);
                    alertDialog.setView(openPhoto);
                    AlertDialog dialog=alertDialog.show();

                    final Button exit=openPhoto.findViewById(R.id.exit_button);
                    exit.setOnClickListener(v1 -> {
                        dialog.dismiss();


                    });


                }

                @Override public void onLongItemClick(View view, int position) {
                    // do whatever
                }
            }));
            view_all_recycler.setAdapter(viewAllAdapter);

            initSavedPhotos(name,200,viewAllItems,viewAllAdapter);
            alertDialog.setView(view_all);
            alertDialog.show();
        });


    }

    private void initSavedPhotos(String name,int limit,List list,PhotoAdapter adapter) {
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

    private void initSavedPhotosRecycler() {
        savedPhotoRecycler =findViewById(R.id.saved_photo_recycler);
        savedPhotoRecycler.setHasFixedSize(true);
        savedPhotoRecycler.setLayoutManager(new LinearLayoutManager(getBaseContext(), LinearLayoutManager.HORIZONTAL, false));
        LayoutInflater layoutInflater=this.getLayoutInflater();
        savedPhotoRecycler.addOnItemTouchListener(new RecyclerItemClickListener(this, savedPhotoRecycler ,new RecyclerItemClickListener.OnItemClickListener() {
            @Override public void onItemClick(View view, int position) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(ViewContactScreen.this,android.R.style.Theme_Black_NoTitleBar_Fullscreen);

                View openPhoto = layoutInflater.inflate(R.layout.open_photo, null);

                final ImageView img = openPhoto.findViewById(R.id.imageView2);
                Picasso.get().load(savedPhotoAdapter.getURI(position)).into(img);
                alertDialog.setView(openPhoto);


                AlertDialog dialog=alertDialog.show();

                final Button exit=openPhoto.findViewById(R.id.exit_button);
                exit.setOnClickListener(v1 -> {
                    dialog.dismiss();


                });



            }

            @Override public void onLongItemClick(View view, int position) {
                // do whatever
            }
        }));

        savedPhotoAdapter = new PhotoAdapter(getBaseContext(), savedPhotoItems);
        savedPhotoRecycler.setAdapter(savedPhotoAdapter);

    }

    private void initUpdateContact() {
        ImageView updateContact=findViewById(R.id.update_view_contact);
        updateContact.setOnClickListener(v -> updateContact(contactId));

    }
    private void updateContact(String contactId){
        Intent intent=new Intent(this, AddUpdateContactScreen.class)
                .putExtra("name",name)
                .putExtra("contactId",contactId);
        startActivity(intent);

    }

    private void initDeleteContact() {
        ImageView deleteContact=findViewById(R.id.delete_view_contact);

        deleteContact.setOnClickListener(v -> {
            deleteContact(contactId);

        });
    }

    private void deleteContact(String contactId){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(String.format("Remove %s from contacts",name));
        alertDialog.setNegativeButton("CANCEL", (dialog, which) -> {
            dialog.dismiss();
        });
        alertDialog.setPositiveButton("CONFIRM", (dialog, which) -> {

            RootRef.child(String.format("Contact/%s/%s", Common.firebaseCurrentUser.getUid(),contactId)).setValue(null).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    Toast.makeText(this, "Contact deleted successfully", Toast.LENGTH_SHORT).show();
                }
            });
            Log.e("ViewContact", "deleteContact: "+String.format("dataset/%s/%s|%s", Common.firebaseCurrentUser.getUid(),name,contactId));

            StorageReference pathReference = FirebaseStorage.getInstance().getReference().child("dataset").child(Common.firebaseCurrentUser.getUid()).child(String.format("%s|%s",name.replaceAll(" ","_"),contactId));
            pathReference.listAll().addOnSuccessListener(listResult -> {
                List<StorageReference> list=listResult.getItems();
                int i;
                for (i=0;i<list.size();i++){
                   if(i==list.size()-1){
                       list.get(i).delete()
                               .addOnCompleteListener(task -> {
                                   if(task.isSuccessful()){
                                       finish();
                                   }
                               })
                               .addOnFailureListener(e -> {
                           Toast.makeText(this, "An error occurred deleting contact images", Toast.LENGTH_SHORT).show();
                       });
                   }else{
                       list.get(i).delete()
                               .addOnFailureListener(e -> Toast.makeText(ViewContactScreen.this, "An error occurred deleting contact images", Toast.LENGTH_SHORT).show());

                   }

                }
            });


        });
        alertDialog.show();
    }


    void initBack(){
        toolbar = findViewById(R.id.toolbar_view_contact);
        toolbar.setNavigationOnClickListener(v -> { finish();}
        );
        
    }
    
    void initProfile(){
        savedPhotoItems.clear();
        ImageView picImg=findViewById(R.id.view_contact_img);
        if(getIntent()!=null){
             name=getIntent().getStringExtra("name");
             contactId=getIntent().getStringExtra("contactId");
            RootRef.child(String.format("Contact/%s/%s", Common.firebaseCurrentUser.getUid(),contactId)).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Contact contact = snapshot.getValue(Contact.class);
                    Picasso.get().load(contact.getPic()).resize(getBaseContext().getResources().getDimensionPixelSize(R.dimen.view_contact_photo_width),
                            getBaseContext().getResources().getDimensionPixelSize(R.dimen.view_contact_photo_width)).into(picImg);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            if(!(name.isEmpty()|| name==null) && !(contactId.isEmpty()|| contactId==null)){
                toolbar.setTitle(name);


                initSavedPhotosRecycler();
                initSavedPhotos(name,4,savedPhotoItems,savedPhotoAdapter);

            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initProfile();
        initViewAll();
        initActivityLog();
    }
}