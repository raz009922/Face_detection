package com.example.valutop.face_detection;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;

import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int RQS_LOADIMAGE = 1;
    ImageView myImage;
    Button takePhoto;
    Button detect;
    Button load;
    TextView text1;
    TextView text2;
    Bitmap myBitmap;
   
    //protected Bitmap b = new Bitmap();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        myImage = (ImageView) findViewById(R.id.image);
        text1 = (TextView) findViewById(R.id.textView);
        text2 = (TextView) findViewById(R.id.textView2);


        takePhoto = (Button) findViewById(R.id.takePhoto);
        detect = (Button) findViewById(R.id.detect);
        load = (Button) findViewById(R.id.LoadBtn);

        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(i,20);


            }
        });


        //autocam
        // Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //startActivityForResult(i,20);
        load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, RQS_LOADIMAGE);

            }
        });

        detect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myBitmap == null) {
                    Toast.makeText(MainActivity.this, "Load some PICK please",
                            Toast.LENGTH_LONG).show();
                } else {

                    detectFace();
                    text1.setText("Blue dots are eyes");

                }
            }
        });




    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RQS_LOADIMAGE
                && resultCode == RESULT_OK
                ) {

            if (myBitmap != null) {
                myBitmap.recycle();
            }

            try {
                InputStream inputStream = getContentResolver().openInputStream(data.getData());
                myBitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();
                myImage.setImageBitmap(myBitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(requestCode == 20 && resultCode == RESULT_OK && data !=null){
            myBitmap= (Bitmap) data.getExtras().get("data");
            myImage.setImageBitmap(myBitmap);
        }
        super.onActivityResult(requestCode, resultCode, data);


    }



    private void detectFace() {
        Paint myRectPaint = new Paint();
        myRectPaint.setStrokeWidth(5);
        myRectPaint.setColor(Color.GREEN);
        myRectPaint.setStyle(Paint.Style.STROKE);

        Paint eyePaint = new Paint();
        eyePaint.setStrokeWidth(2);
        eyePaint.setColor(Color.BLUE);
        eyePaint.setStyle(Paint.Style.STROKE);


        Paint landmarksPaint = new Paint();
        landmarksPaint.setStrokeWidth(1);
        landmarksPaint.setColor(Color.RED);
        landmarksPaint.setStyle(Paint.Style.STROKE);

        Paint smilingPaint = new Paint();
        smilingPaint.setStrokeWidth(4);
        smilingPaint.setColor(Color.YELLOW);
        smilingPaint.setStyle(Paint.Style.STROKE);


        boolean areYouSmiling = false;
        float smilingProbability=0;


        Bitmap tempBitmap = Bitmap.createBitmap(myBitmap.getWidth(), myBitmap.getHeight(), Bitmap.Config.RGB_565);
        Canvas tempCanvas = new Canvas(tempBitmap);

        double viewWidth = tempCanvas.getWidth();
        double viewHeight = tempCanvas.getHeight();
        double imageWidth = myBitmap.getWidth();
        double imageHeight = myBitmap.getHeight();
        double scale = Math.min(viewWidth / imageWidth, viewHeight / imageHeight);
        Rect destBounds = new Rect(0, 0, (int)(imageWidth * scale), (int)(imageHeight * scale));
        tempCanvas.drawBitmap(myBitmap, null, destBounds, null);


        FaceDetector faceDetector =

                new FaceDetector.Builder(getApplicationContext())
                        .setTrackingEnabled(false)
                        .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                        .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                        .build();


        Frame frame = new Frame.Builder().setBitmap(myBitmap).build();
        SparseArray<Face> faces = faceDetector.detect(frame);



        for (int i=0; i<faces.size();i++){
            Face thisFace=faces.valueAt(i);
            float x1 = (float) (thisFace.getPosition().x*scale);
            float y1 = (float) (thisFace.getPosition().y*scale);
            float x2 = (float) scale*(x1 + thisFace.getWidth());
            float y2 = (float) scale*(y1 + thisFace.getHeight());

            tempCanvas.drawRoundRect(new RectF(x1, y1, x2, y2), 2, 2, myRectPaint);


            for( int j = 0; j < faces.size(); j++ ) {
                com.google.android.gms.vision.face.Face face = faces.valueAt(j);

                for ( Landmark landmark : face.getLandmarks() ) {
                    int cx = (int) ( landmark.getPosition().x * scale );
                    int cy = (int) ( landmark.getPosition().y * scale );
                    int skin = landmark.getType();
                    if((skin == 4) || (skin == 10)){
                        tempCanvas.drawCircle( cx, cy, 2, eyePaint );
                    } else
                        tempCanvas.drawCircle( cx, cy, 2, landmarksPaint );}
            }

            final float smilingAcceptProbability = 0.3f;
            smilingProbability = thisFace.getIsSmilingProbability();
            if(smilingProbability>smilingAcceptProbability){
                tempCanvas.drawOval(new RectF(x1,y1,x2,y2),smilingPaint);
                areYouSmiling=true;

            }
        }
        Log.d("faces", String.valueOf(faces.size()));






        myImage.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));
        if(areYouSmiling){
                Toast.makeText(MainActivity.this,
                    "Oh god.. you look so cute.. ..",
                    Toast.LENGTH_LONG).show();
            text2.setText("Your smiling probability: " + smilingProbability  );
        }else{
            Toast.makeText(MainActivity.this,
                    "sometimes you need to smile",
                    Toast.LENGTH_LONG).show();
            text2.setText("You are not smiling, smiling probability: " + smilingProbability  );

        }




    }


}






