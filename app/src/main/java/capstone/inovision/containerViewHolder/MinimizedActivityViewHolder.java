package capstone.inovision.containerViewHolder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import capstone.inovision.R;

public class MinimizedActivityViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView datetime,location;

    public MinimizedActivityViewHolder(@NonNull View itemView) {
        super(itemView);

        datetime=itemView.findViewById(R.id.minimized_date);
        location=itemView.findViewById(R.id.minimized_location);

    }

    @Override
    public void onClick(View v) {

    }
}
