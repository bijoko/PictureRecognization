package com.example.vbijok.PictureRecognization;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import cz.msebera.android.httpclient.Header;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button captureButton;
    Button libraryButton;
    Button analysisButton;
    ImageView imageView;



    private static final int PHOTO_LIB_REQUEST = 1;
    private static final int CAMERA_PIC_REQUEST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // on active la fonction Onclick

        captureButton = (Button) findViewById(R.id.captureButton);
        captureButton.setOnClickListener(this);

        libraryButton = (Button) findViewById(R.id.libraryButton);
        libraryButton.setOnClickListener(this);

        analysisButton = (Button) findViewById(R.id.analysisButton);
        analysisButton.setOnClickListener(this);

        imageView = (ImageView) findViewById(R.id.imageView);

    }

    // en fonction de l'id on démarre une activité spécifique
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
                search();
                break;

        }
    }

    // Intent => nouvelle instance
    protected void startPhotoLibraryActivity() {
        Intent photoLibIntent = new Intent();
        photoLibIntent.setType("image/*");
        photoLibIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(photoLibIntent, PHOTO_LIB_REQUEST);
    }

    // photo : deja en bitmap => get data , librarie, conversion a faire et get URI
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == PHOTO_LIB_REQUEST && resultCode == RESULT_OK) {
            Uri photoUri = intent.getData();
            imageView.setImageURI(photoUri);
        }

        if (requestCode == CAMERA_PIC_REQUEST && resultCode == RESULT_OK) {
            Bundle extras = intent.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);
        }
    }

    //Remplace le Bitmap en fichier pour l'envoyer

    private File bitmapToFile(Bitmap bitmap, String name) {
        File imageFile = new File(getApplicationContext().getFilesDir().getPath() + "/" + name + ".jpg");

        OutputStream os;
        try {
            os = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 10, os);
            os.flush();//clean
            os.close();
        } catch (Exception e) {
            Log.e(MainActivity.class.getSimpleName(), "Error writing bitmap", e);
        }

        return imageFile;
    }
    private void search() {
        RequestParams param = new RequestParams();

        try {
            Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
            File file = bitmapToFile(bitmap, "random");
            param.put("file", file, "image/jpg");// instanciation des clés + content type
        } catch (NullPointerException e) {
            Toast.makeText(MainActivity.this, "Sélectionnez une image ...", Toast.LENGTH_SHORT).show();
            return;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        // POST IMG
        HttpUtils.post("img_searches/", param, new JsonHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(MainActivity.this, "l'API n'est pas accessible ...", Toast.LENGTH_LONG).show();
            }
                // récupère la location de la réponse JSON avec search id
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("API_response", "POST response:" + response);
                try {
                    String search_id = (String) new JSONObject(response.toString()).get("location");
                    Log.d("API_response", "POST id de la ressource: " + search_id);

                    // GET IMG
                    HttpUtils.get( search_id, new RequestParams(), new JsonHttpResponseHandler() {
                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                            Toast.makeText(MainActivity.this, "l'API n'est pas accessible ...", Toast.LENGTH_LONG).show();
                        }
                            //Instanciation de la nouvelle activité
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            Log.d("API_response", "GET response" + response.toString());
                            Intent intent = new Intent(getBaseContext(), ResultsActivity.class);
                            ByteArrayOutputStream bStream = new ByteArrayOutputStream();
                            Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bStream);
                            byte[] byteArray = bStream.toByteArray();
                            System.out.println(byteArray);
                            intent.putExtra("image",byteArray);// image de l'user , tableau de bit
                            startActivity(intent);
                            intent.putExtra("response", response.toString());//Transforme le JSON en string
                            startActivity(intent);
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
