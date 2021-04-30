package capstone.inovision.containerViewHolder;

import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import capstone.inovision.ItemClickListener;
import capstone.inovision.R;

public class SensorViewHolder  extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnCreateContextMenuListener {

    public TextView label,location,id;
    public ImageView categoryImg;
    public ItemClickListener itemClickListener;

    public SensorViewHolder(@NonNull View itemView){
        super(itemView);
        label=itemView.findViewById(R.id.list_sensor_txt_label);
        location=itemView.findViewById(R.id.list_sensor_txt_location);
        categoryImg=itemView.findViewById(R.id.list_sensor_category_icon);
        id= itemView.findViewById(R.id.list_sensor_txt_id);
        itemView.setOnClickListener(this);
        itemView.setOnCreateContextMenuListener(this);
    }





    @Override
    public void onClick(View v) {
//        itemClickListener.onClick(v,getAdapterPosition(),false);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.add(0,0,getAdapterPosition(),"UPDATE");
    }
}
