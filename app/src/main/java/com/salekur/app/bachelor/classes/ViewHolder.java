package com.salekur.app.bachelor.classes;

import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.salekur.app.bachelor.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewHolder extends RecyclerView.ViewHolder {
    public RelativeLayout InfoView;
    public CircleImageView InfoImage;
    public TextView InfoTitle, InfoMessage;
    public ImageView InfoAction;

    public ViewHolder(@NonNull View itemView) {
        super(itemView);

        InfoView = itemView.findViewById(R.id.info_view);
        InfoImage = itemView.findViewById(R.id.info_view_image);
        InfoTitle = itemView.findViewById(R.id.info_view_title);
        InfoMessage = itemView.findViewById(R.id.info_view_message);
        InfoAction = itemView.findViewById(R.id.info_view_action);
    }
}
