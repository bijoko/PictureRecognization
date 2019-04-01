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


    //Declaration des constantes code de retour des requetes intent
    private static final int PHOTO_LIB_REQUEST = 1;
    private static final int CAMERA_PIC_REQUEST = 2;
   //private String SERVER_URL = "http:/192.168.0.0/uusapp/addimage/";

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
                    search();
                break;

        }
    }

    protected void startPhotoLibraryActivity() {
        Intent photoLibIntent = new Intent();
        photoLibIntent.setType("image/*");
        photoLibIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(photoLibIntent, PHOTO_LIB_REQUEST);
    }

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

    //private static final String IMGUR_CLIENT_ID = "...";
    //private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/jpg");

    private File bitmapToFile(Bitmap bitmap, String name) {
        File imageFile = new File(getApplicationContext().getFilesDir().getPath() + "/" + name + ".jpg");

        OutputStream os;
        try {
            os = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
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
            param.put("file", file, "image/jpg");
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

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("API_response", "POST response:" + response);
                try {
                    String search_id = (String) new JSONObject(response.toString()).get("id");
                    Log.d("API_response", "POST id de la ressource: " + search_id);

                    // GET IMG
                    HttpUtils.get("img_searches/" + search_id, new RequestParams(), new JsonHttpResponseHandler() {
                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                            Toast.makeText(MainActivity.this, "l'API n'est pas accessible ...", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            Log.d("API_response", "GET response" + response.toString());
                            Intent intent = new Intent(getBaseContext(), ResultsActivity.class);
                            intent.putExtra("response", response.toString());
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


    /*
    private final OkHttpClient client = new OkHttpClient();



    public void run()  throws Exception {
// Use the imgur image upload API as documented at https://api.imgur.com/endpoints/image
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addPart(
                        Headers.of("Content-Disposition", "form-data; name=\"title\""),
                        RequestBody.create(null, "Square Logo"))
                .addPart(
                        Headers.of("Content-Disposition", "form-data; name=\"image\""),
                        RequestBody.create(MEDIA_TYPE_PNG, new File("/Carte SD/DCIM/CAMERA/test.jpg")))
                .build();

        Request request = new Request.Builder()
                .header("Authorization", "Client-ID " + IMGUR_CLIENT_ID)
                .url("http://192.168.43.46:8001/img_searches/")
                .post(requestBody)
                .build();

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

        System.out.println(response.body().string());
    }
}

    private class UploadFileToServer extends AsyncTask<Void, Integer, String> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            return uploadFile;
        }

        private String uploadFile(){
            String responseString = null;
            Log.d("Log", "File path" + opFilePath);
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(Config.FILE_UPLOAD_URL);
            try {
                AndroidMultiPartEntity entity = new AndroidMultiPartEntity(
                        new AndroidMultiPartEntity.ProgressListener() {
                            @Override
                            public void transferred(long num) {
                                publishProgress((int) ((num / (float) totalSize) * 100));
                        }

        }
    }
}

    private class AndroidMultiPartEntity {
    }


*/




