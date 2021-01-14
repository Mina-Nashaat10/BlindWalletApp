package com.example.blind_wallet.Volunteer.TrackingSystem;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.blind_wallet.R;

public class ListOnlineViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public TextView txtEmail;
    ItemClickListener itemClickListener;
    Button button;
    public ListOnlineViewHolder(View itemView) {
        super(itemView);
        txtEmail=(TextView)itemView.findViewById(R.id.txt_email);
        button = itemView.findViewById(R.id.loc);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View view) {
        itemClickListener.onClick(view,getAdapterPosition());
    }
}
