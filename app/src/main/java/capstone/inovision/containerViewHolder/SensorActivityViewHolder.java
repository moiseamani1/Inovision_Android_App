package capstone.inovision.containerViewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import capstone.inovision.R;

public class SensorActivityViewHolder  extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView  label,location,datetime,status;
    public ImageView categoryImg;

    public SensorActivityViewHolder(@NonNull View itemView) {
        super(itemView);
        label=itemView.findViewById(R.id.sensor_txt_label);
        datetime=itemView.findViewById(R.id.sensor_txt_datetime);
        location=itemView.findViewById(R.id.sensor_txt_location);
        status=itemView.findViewById(R.id.sensor_txt_status);
        categoryImg=itemView.findViewById(R.id.sensor_category_icon);
        itemView.setOnClickListener(this);
    }





    @Override
    public void onClick(View v) {

    }
}
