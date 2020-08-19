package com.codewithnaveen.docscan;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.MyViewHolder> {
    ArrayList<Bitmap> images;
    Context c;
    private MenuItem pdfmenu;
    private MenuItem sharemenu;
    private MenuItem renamemenu;
    private Menu menu;
    public ImageAdapter(Context c, ArrayList<Bitmap> images){
        this.c = c;
        this.images = images;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(c).inflate(R.layout.my_row,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        final Bitmap image = images.get(position);
        holder.img.setImageBitmap(image);
        holder.del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(images.size()>0) {
                    images.remove(position);
                    notifyItemRemoved(position);
                    ((MainActivity)c).visible();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView img;
        ImageButton del;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.myimageView);
            del = itemView.findViewById(R.id.delete_img);
            pdfmenu = itemView.findViewById(R.id.action_settings);
            sharemenu = itemView.findViewById(R.id.action_share);
            renamemenu = itemView.findViewById(R.id.action_Rename);
        }
    }
}
