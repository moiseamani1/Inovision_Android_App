package capstone.inovision.containerViewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import capstone.inovision.R;

public class PhotoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public ImageView photo;
    public ProgressBar progressBar;

    public PhotoViewHolder(@NonNull View itemView) {
        super(itemView);
        photo = itemView.findViewById(R.id.saved_photo_img);
        progressBar=itemView.findViewById(R.id.progressBar);

        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

    }
}
