package com.example.assessment0;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.example.assessment0.adapters.PaintImagesAdapter;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class PaintImagesActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView recyclerViewPaintImages;

    // Firebase
    private FirebaseFirestore firebaseFirestore;
    private List<String> paintImagesUrlList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paint_images);

        // initialize views
        toolbar = findViewById(R.id.toolbarPaintImages);
        recyclerViewPaintImages = findViewById(R.id.recyclerViewPaintImages);

        // support action bar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }

        // firebase firestore
        String userUniqueId = getIntent().getStringExtra("userUniqueId");
        if (userUniqueId.equals(null))
            onBackPressed();
        firebaseFirestore = FirebaseFirestore.getInstance();

        // recyclerview
        PaintImagesAdapter paintImagesAdapter = new PaintImagesAdapter(this);
        recyclerViewPaintImages.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerViewPaintImages.setAdapter(paintImagesAdapter);
        paintImagesAdapter.setOnItemClickListener(new PaintImagesAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                String imageUrl = paintImagesAdapter.getStringImageUrlAt(position).trim();

                // Layout Inflator
                LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View viewproblemimage = layoutInflater.inflate(R.layout.uploaded_image_layout, null);
                Dialog builder = new Dialog(PaintImagesActivity.this);
                builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
                builder.getWindow().setBackgroundDrawable(
                        new ColorDrawable(Color.TRANSPARENT)
                );
                builder.setCanceledOnTouchOutside(true);
                builder.setContentView(viewproblemimage);
                PhotoView photoView = viewproblemimage.findViewById(R.id.imageViewUploadedImage);
                try {
                    Picasso.get().load(imageUrl)
                            .into(photoView);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                builder.show();


            }
        });

        firebaseFirestore.collection("PaintImages " + userUniqueId).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("PaintImagesActivity", "error ::: getting images ::: " + error.getMessage());
                    return;
                }
                if (value.equals(null) || value.isEmpty()) {
                    Toast.makeText(PaintImagesActivity.this, "No Paints!!!", Toast.LENGTH_SHORT).show();
                    return;
                }

                for (QueryDocumentSnapshot image : value) {
                    if (!image.get("ImageUrl").toString().trim().equals(null))
                        paintImagesUrlList.add(0, image.get("ImageUrl").toString());
                }
                paintImagesAdapter.submitList(paintImagesUrlList);
            }
        });

    }

    // on back arrow click
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}