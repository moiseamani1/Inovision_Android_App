package capstone.inovision.containerViewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import capstone.inovision.R;

public class ContactViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView name;
    public ImageView photo;

    public ContactViewHolder(@NonNull View itemView) {
        super(itemView);

        name=itemView.findViewById(R.id.contact_item_name);
        photo=itemView.findViewById(R.id.contact_item_image);


        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

    }


}
