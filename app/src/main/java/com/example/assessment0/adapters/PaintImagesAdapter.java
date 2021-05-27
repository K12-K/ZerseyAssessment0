package com.example.assessment0.adapters;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.assessment0.R;
import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class PaintImagesAdapter extends ListAdapter<String, PaintImagesAdapter.PaintImagesViewHolder> {

    private Context context;
    private OnItemClickListener listener;

    public PaintImagesAdapter(Context context) {
        super(DIFF_CALLBACK);
        this.context = context;
    }

    public static final DiffUtil.ItemCallback<String> DIFF_CALLBACK = new DiffUtil.ItemCallback<String>() {
        @Override
        public boolean areItemsTheSame(@NonNull String oldItem, @NonNull String newItem) {
            return oldItem.trim().equals(newItem.trim());
        }

        @Override
        public boolean areContentsTheSame(@NonNull String oldItem, @NonNull String newItem) {
            return oldItem.trim().equals(newItem.trim());
        }
    };

    @NonNull
    @Override
    public PaintImagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PaintImagesViewHolder(LayoutInflater.from(context).inflate(R.layout.item_paint_images, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PaintImagesViewHolder holder, int position) {
        Picasso.get().load(getItem(position).trim())
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(holder.ivPaintImages);
    }

    public String getStringImageUrlAt(int position) {
        return getItem(position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public class PaintImagesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView ivPaintImages;
        public PaintImagesViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPaintImages = itemView.findViewById(R.id.ivPaintImages);
            ivPaintImages.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.onItemClick(getAdapterPosition());
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
}
