package com.example.vbijok.PictureRecognization;


import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.provider.MediaStore;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button captureButton;
    Button libraryButton;
    Button analysisButton;
    ImageView imageView;

    //Declaration des constantes code de retour des requetes intent
    private static final int PHOTO_LIB_REQUEST = 1;
    private static final int CAMERA_PIC_REQUEST = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        captureButton = (Button) findViewById(R.id.captureButton);
        captureButton.setOnClickListener(this);

        libraryButton = (Button) findViewById(R.id.libraryButton);
        libraryButton.setOnClickListener(this);

        analysisButton = (Button) findViewById(R.id.analysisButton);
        analysisButton.setOnClickListener(this);

        imageView = (ImageView) findViewById(R.id.imageView);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.captureButton:
                Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(takePicture, CAMERA_PIC_REQUEST);// démarre l'activé photo
                break;

            case R.id.libraryButton:
                startPhotoLibraryActivity();// démarre l'activé librairy
                break;

            case R.id.analysisButton:
                break;
        }

    }

    protected void startPhotoLibraryActivity() {
        Intent photoLibIntent = new Intent();
        photoLibIntent.setType("image/*");
        photoLibIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(photoLibIntent, PHOTO_LIB_REQUEST);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent){
        if (requestCode==PHOTO_LIB_REQUEST && resultCode==RESULT_OK){
            Uri photoUri = intent.getData();
            imageView.setImageURI(photoUri);
        }

        if (requestCode==CAMERA_PIC_REQUEST && resultCode==RESULT_OK){
            Bundle extras = intent.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);
        }
    }
}

