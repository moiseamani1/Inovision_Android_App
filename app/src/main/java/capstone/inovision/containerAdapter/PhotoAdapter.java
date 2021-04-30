package capstone.inovision.containerAdapter;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

import capstone.inovision.R;
import capstone.inovision.containerViewHolder.PhotoViewHolder;
import capstone.inovision.model.Photo;
import capstone.inovision.ui.ViewContactScreen;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoViewHolder>{
    private List<Photo> mData;
    private LayoutInflater mInflater;

    private Context context;


    public PhotoAdapter(Context context, List<Photo> photos) {
        this.context=context;
        this.mData=photos;
        this.mInflater = LayoutInflater.from(context);
    }




    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView=mInflater.inflate(R.layout.photo_item,parent,false);
        return new PhotoViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {

        Photo photo=mData.get(position);
        ProgressBar progressBar=holder.progressBar;
        progressBar.setVisibility(View.VISIBLE);


//        Callback callback=;
        if(photo.getUri()!=null){
            Log.d("PhotoAdapter", " Iam loading into image view with URi");
            Picasso.get().load(photo.getUri()).resize(context.getResources().getDimensionPixelSize(R.dimen.preview_width),0).into(holder.photo,new com.squareup.picasso.Callback() {
                @Override
                public void onSuccess() {
                    if (holder.progressBar != null) {
                        holder.progressBar.setVisibility(View.GONE);
                    }
                }
                @Override
                public void onError(Exception e) {
                    Log.d("Photo URi", "onError: "+e.getMessage());

                }
            });





        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }


    public Uri getURI(int position){
        return mData.get(position).getUri();
    }

}
