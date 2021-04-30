package capstone.inovision.containerViewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import capstone.inovision.R;

public class DoorActivityViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView name,datetime,location;
        public ImageView photo;

        public DoorActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            name=itemView.findViewById(R.id.door_contact_name);
            datetime=itemView.findViewById(R.id.door_datetime);
            location=itemView.findViewById(R.id.door_location);
            photo=itemView.findViewById(R.id.door_contact_image);


            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

        }


    }


