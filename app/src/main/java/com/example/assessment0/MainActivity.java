package com.example.assessment0;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.dhaval2404.colorpicker.ColorPickerDialog;
import com.github.dhaval2404.colorpicker.MaterialColorPickerDialog;
import com.github.dhaval2404.colorpicker.listener.ColorListener;
import com.github.dhaval2404.colorpicker.model.ColorShape;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.slider.RangeSlider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    private ImageView ivUndoMain, ivRedoMain, ivSaveMain, ivColorMain, ivBrushMain, ivEraseMain;
    private RangeSlider rangeSliderMain;
    private PaintWindow paintWindowMain;

    private FirebaseStorage firebaseStorage;
    private FirebaseFirestore firebaseFirestore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize views
        init();
        rangeSliderMain.setValueFrom(0f);
        rangeSliderMain.setValueTo(100f);
//        rangeSliderMain.bringToFront();
//        rangeSliderMain.invalidate();
        paintWindowMain.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                paintWindowMain.initializeBitmapCanvas(paintWindowMain.getMeasuredWidth(), paintWindowMain.getMeasuredHeight());
                paintWindowMain.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        // firebase
        firebaseStorage = FirebaseStorage.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        // onClicks
        ivUndoMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paintWindowMain.undo();
            }
        });

        ivRedoMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paintWindowMain.redo();
            }
        });

        ivSaveMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("kk", "Uploading Image...");
                uploadImageToDatabase(paintWindowMain.save());
            }
        });

        ivColorMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // using ColorPickerDialog
                new ColorPickerDialog.Builder(MainActivity.this)
                        .setTitle("Choose Color")
                        .setColorShape(ColorShape.SQAURE)
                        .setDefaultColor(paintWindowMain.currentColor)
                        .setColorListener(new ColorListener() {
                            @Override
                            public void onColorSelected(int i, String s) {
                                paintWindowMain.setColor(i);
                            }
                        });
                // using MaterialPickerDialog
                new MaterialColorPickerDialog.Builder(MainActivity.this)
                        .setTitle("Choose Color")
                        .setColorShape(ColorShape.SQAURE)
                        .setDefaultColor(paintWindowMain.currentColor)
                        .setColorListener(new ColorListener() {
                            @Override
                            public void onColorSelected(int i, String s) {
                                paintWindowMain.setColor(i);
                            }
                        })
                        .show();
            }
        });

        ivBrushMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rangeSliderMain.getVisibility() == View.VISIBLE && !paintWindowMain.getEraser())
                    rangeSliderMain.setVisibility(View.GONE);
                else
                    rangeSliderMain.setVisibility(View.VISIBLE);
                paintWindowMain.setEraser(false);
                // set range of slider to currentStrokeWidth
                rangeSliderMain.setValues((float) paintWindowMain.currentStrokeWidth);
            }
        });

        ivEraseMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rangeSliderMain.getVisibility() == View.VISIBLE && paintWindowMain.getEraser())
                    rangeSliderMain.setVisibility(View.GONE);
                else
                    rangeSliderMain.setVisibility(View.VISIBLE);
                paintWindowMain.setEraser(true);
                // set range of slider to currentEraserWidth
                rangeSliderMain.setValues((float) paintWindowMain.currentEraserWidth);
            }
        });

        rangeSliderMain.addOnChangeListener(new RangeSlider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull RangeSlider slider, float value, boolean fromUser) {
                if (paintWindowMain.getEraser())
                    paintWindowMain.setEraserWidth((int) value);
                else
                    paintWindowMain.setStrokeWidth((int) value);
            }
        });
    }

    private void init() {
        ivUndoMain = findViewById(R.id.ivUndoMain);
        ivRedoMain = findViewById(R.id.ivRedoMain);
        ivSaveMain = findViewById(R.id.ivSaveMain);
        ivColorMain = findViewById(R.id.ivColorMain);
        ivBrushMain = findViewById(R.id.ivBrushMain);
        ivEraseMain = findViewById(R.id.ivEraseMain);
        rangeSliderMain = findViewById(R.id.rangeSliderMain);
        paintWindowMain = findViewById(R.id.paintWindowMain);
    }

    private void uploadImageToDatabase(Bitmap bitmap) {
        String currentDTUTC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100, baos);

        @SuppressLint("SimpleDateFormat") final StorageReference reference = firebaseStorage.getReference()
                .child("PaintImages")
                .child(currentDTUTC + ".jpeg");

        reference.putBytes(baos.toByteArray())
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(MainActivity.this, "Image Saved!!", Toast.LENGTH_SHORT).show();
                        saveDataInFirestore(currentDTUTC);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("MainActivity","onFailure: saving image to database method, "+e.getCause());
                        Toast.makeText(MainActivity.this, "Error Saving Image!!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveDataInFirestore(String currentDTUTC) {
        DocumentReference documentReference = firebaseFirestore.collection("PaintImages").document(currentDTUTC);

        Map<String, Object> image = new HashMap<>();
        image.put("Name", currentDTUTC + ".jpeg");

        documentReference.set(image)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override public void onSuccess(Void aVoid) {
                        Log.i("MainActivity", "onSuccess: image name stored in Firestore");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override public void onFailure(@NonNull Exception e) {
                        Log.i("MainActivity", "onFailure: image name stored in Firestore");
                    }
                });
    }
}