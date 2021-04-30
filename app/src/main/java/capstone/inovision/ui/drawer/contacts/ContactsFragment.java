package capstone.inovision.ui.drawer.contacts;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.paging.DatabasePagingOptions;
import com.firebase.ui.database.paging.FirebaseRecyclerPagingAdapter;
import com.firebase.ui.database.paging.LoadingState;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import capstone.inovision.R;
import capstone.inovision.common.Common;
import capstone.inovision.containerViewHolder.ContactViewHolder;
import capstone.inovision.model.Contact;
import capstone.inovision.ui.AddUpdateContactScreen;
import capstone.inovision.ui.ViewContactScreen;

public class ContactsFragment extends Fragment {

    private ContactsViewModel contactsViewModel;
    private CardView addfriendCard;
    private View root;
    private RecyclerView contactsRecycler;
    private FirebaseRecyclerAdapter<Contact, ContactViewHolder> firebaseRecyclerAdapter;
    private DatabaseReference RootRef,Contacts;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        contactsViewModel =
                new ViewModelProvider(this).get(ContactsViewModel.class);
        root= inflater.inflate(R.layout.fragment_contacts, container, false);
        RootRef= FirebaseDatabase.getInstance().getReference();
        Contacts= RootRef.child("Contact").child(Common.firebaseCurrentUser.getUid());
        initFriends();
        initSearch();
        initAddFriend(root);
        return root;
    }


    private void initAddFriend(View root){
        addfriendCard=root.findViewById(R.id.add_friends_contact_screen);
        addfriendCard.setOnClickListener(v -> startActivity(new Intent(getActivity(), AddUpdateContactScreen.class)));
    }

    private void initFriends(){

        contactsRecycler=root.findViewById(R.id.contact_recycler_home);
        contactsRecycler.setHasFixedSize(true);
        contactsRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));





    }

    void initRecycler(Query query){


        FirebaseRecyclerOptions<Contact> options= new FirebaseRecyclerOptions.Builder<Contact>()
                .setQuery(query,Contact.class)
                .setLifecycleOwner(this)
                .build();



        firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Contact, ContactViewHolder>(options) {


                    @NonNull
                    @Override
                    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View itemView=LayoutInflater.from(getContext()).inflate(R.layout.contact_item,parent,false);
                        return new ContactViewHolder(itemView);
                    }

                    @Override
                    protected void onBindViewHolder(@NonNull ContactViewHolder holder, int position, @NonNull Contact model) {

                        holder.name.setText(model.getName());
                        Picasso.get().load(model.getPic()).resize(getContext().getResources().getDimensionPixelSize(R.dimen.contact_photo_width),0).into(holder.photo);
                        holder.itemView.setOnClickListener(v -> {
                            Intent intent=new Intent(getActivity(), ViewContactScreen.class)
                                    .putExtra("name",model.getName())
                                    .putExtra("pic",model.getPic())
                                    .putExtra("contactId",firebaseRecyclerAdapter.getRef(position).getKey());
                            startActivity(intent);
                        });

                    }


//                    @Override
//                    protected void onLoadingStateChanged(@NonNull LoadingState state) {
//                        switch (state) {
//                            case LOADING_INITIAL:
//                            case LOADING_MORE:
//                                mSwipeRefreshLayout.setRefreshing(true);
//                                break;
//
//                            case LOADED:
//                                mSwipeRefreshLayout.setRefreshing(false);
//                                break;
//
//                            case ERROR:
//                                Toast.makeText(
//                                        getContext(),
//                                        "Error Occurred!",
//                                        Toast.LENGTH_SHORT
//                                ).show();
//
//                                mSwipeRefreshLayout.setRefreshing(false);
//
//                                break;
//
//                            case FINISHED:
//                                mSwipeRefreshLayout.setRefreshing(false);
//                                break;
//                        }
//                    }
                };


        contactsRecycler.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }


    @Override
    public void onStart() {
        super.onStart();
        initRecycler(Contacts.orderByChild("name"));
    }
    @Override
    public void onStop() {
        super.onStop();
        if(firebaseRecyclerAdapter!=null){
            firebaseRecyclerAdapter.stopListening();
        }

    }


    private void initSearch(){

        MaterialSearchBar searchbox=root.findViewById(R.id.searchbox);

        searchbox.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String searchText=searchbox.getText();
                initRecycler(Contacts.orderByChild("name").startAt(searchText).endAt(searchText+"\uf8ff"));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }


}