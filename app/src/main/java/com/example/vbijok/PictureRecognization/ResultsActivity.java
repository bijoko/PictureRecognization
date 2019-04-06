package com.example.vbijok.PictureRecognization;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;

public class ResultsActivity extends AppCompatActivity {
    ImageView picture1;
    ImageView picture2;
    ImageView picture3;
    ImageView picture4;
    ImageView picture5;


    private Toast backtoast;

@Override
    public void onBackPressed() {
            if(backtoast!=null&&backtoast.getView().getWindowToken()!=null) {

                //other stuff...
                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                startActivity(intent);
            } else {
                backtoast = Toast.makeText(this, "Press back to exit", Toast.LENGTH_SHORT);
                backtoast.show();
            }
        }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        Bundle extras = getIntent().getExtras();
        String responseString = extras.getString("response");
        JSONArray responseJSON;
        ArrayList<String> pictureList;




        picture1 = (ImageView) findViewById(R.id.imageView);
        picture2 = (ImageView) findViewById(R.id.imageView3);
        picture3 = (ImageView) findViewById(R.id.imageView4);
        picture4 = (ImageView) findViewById(R.id.imageView5);
        picture5 = (ImageView) findViewById(R.id.imageView6);

        Bitmap bmp;
        byte[] byteArray = extras.getByteArray("image");
        bmp = BitmapFactory.decodeByteArray(byteArray,0,byteArray.length);
        picture5.setImageBitmap(bmp);

        try {
            responseJSON = new JSONObject(responseString).getJSONArray("results");
            //System.out.println(responseJSON);
            pictureList = new ArrayList<String>();
            for (int i = 0; i < responseJSON.length(); i++) {
                JSONObject picture = responseJSON.getJSONObject(i);
                //System.out.println(picture);
                //System.out.print(responseJSON.length());
                String url = (String) picture.get("image_url");
                pictureList.add(url);
            }
            new DownloadImageTask((picture1)).execute(pictureList.get(0));
            new DownloadImageTask((picture2)).execute(pictureList.get(1));
            new DownloadImageTask((picture3)).execute(pictureList.get(2));
            new DownloadImageTask((picture4)).execute(pictureList.get(3));
            System.out.println(pictureList);
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }
}

 class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    ImageView bmImage;

    public DownloadImageTask(ImageView bmImage) {
        this.bmImage = bmImage;
    }

    protected Bitmap doInBackground(String... urls) {
        String urldisplay = urls[0];
        Bitmap mIcon11 = null;
        try {
            InputStream in = new java.net.URL(urldisplay).openStream();
            mIcon11 = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return mIcon11;
    }

    protected void onPostExecute(Bitmap result) {
        bmImage.setImageBitmap(result);
    }
}
