package com.codewithnaveen.docscan;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.MyViewHolder> {
    ArrayList<Bitmap> images;
    Context c;
    boolean check =false;
    private MenuItem pdfmenu;
    private MenuItem sharemenu;
    private MenuItem renamemenu;
    private Menu menu;
    //ItemClickListener itemClickListener;
    public ImageAdapter(Context c, ArrayList<Bitmap> images){
        this.c = c;
        this.images = images;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_row,parent,false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        final Bitmap image = images.get(position);
        holder.img.setImageBitmap(image);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView img,imgshow;
        ImageButton del;
        TextView count;
        public MyViewHolder(@NonNull final View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.myimageView);
            del = itemView.findViewById(R.id.delete_img);
            pdfmenu = itemView.findViewById(R.id.action_settings);
            sharemenu = itemView.findViewById(R.id.action_share);
            renamemenu = itemView.findViewById(R.id.action_Rename);
            count = itemView.findViewById(R.id.count);

            del.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(images.size()>0) {
                        try {
                            images.remove(getAdapterPosition());
                            Toast.makeText(c, "this is " + getAdapterPosition(), Toast.LENGTH_SHORT).show();
                            notifyItemRemoved(getAdapterPosition());
                            ((MainActivity) c).visible();
                        }catch (IndexOutOfBoundsException e){
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    public Uri getImageUri(Context c, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(c.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }
}
