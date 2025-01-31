package com.example.strzelnica;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;

import android.util.Log;

import com.google.android.material.slider.Slider;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import static xdroid.toaster.Toaster.toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;


public class ActivityCamera extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    Slider slider;
    JavaCameraView javaCameraView;
    Mat mat1, mat2;
    Scalar scalarLow, scalarHigh;
    Mat src;
    MediaPlayer player;

    FileOutputStream fileOutputStream = null;
    int points = 0;
    int counter = 0;

    StringBuffer datax;

    BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(ActivityCamera.this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status)
            {
                case BaseLoaderCallback.SUCCESS:
                {
                    javaCameraView.enableView();
                    break;
                }
                default:
                {
                    super.onManagerConnected(status);
                    break;
                }
            }
            super.onManagerConnected(status);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        datax = new StringBuffer("");
        try {
            FileInputStream fIn = openFileInput ( "jezyk" ) ;
            InputStreamReader isr = new InputStreamReader ( fIn ) ;
            BufferedReader buffreader = new BufferedReader ( isr ) ;

            String readString = buffreader.readLine ( ) ;
            while ( readString != null ) {
                datax.append(readString);
                readString = buffreader.readLine ( ) ;
            }
            isr.close ( ) ;
        } catch ( IOException ioe ) {
            ioe.printStackTrace ( ) ;
        }

        OpenCVLoader.initDebug();

        File dir = getFilesDir();
        File file = new File(dir, "punkty");
        file.delete();
        try {
            fileOutputStream = openFileOutput("punkty", MODE_APPEND);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        javaCameraView = (JavaCameraView) findViewById(R.id.javaCameraView);

        javaCameraView.setCameraIndex(0);

        scalarLow = new Scalar(136,87,111);
        scalarHigh = new Scalar(180,255,255);

        javaCameraView.setCvCameraViewListener(ActivityCamera.this);
        javaCameraView.enableView();

    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        src = new Mat(height, width, CvType.CV_16UC4);

        mat1 = new Mat(height, width, CvType.CV_16UC4);
        mat2 = new Mat(height, width, CvType.CV_16UC4);
    }

    @Override
    public void onCameraViewStopped() {
        src.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        src = inputFrame.rgba();
        int w = src.width();
        int h = src.height();
        int w_rect = w * 3 / 4; // or 640
        int h_rect = h * 3 / 4; // or 480

        int scale = w / 774;

        Imgproc.cvtColor(inputFrame.rgba(), mat1, Imgproc.COLOR_BGR2HSV);

        Core.inRange(mat1, scalarLow, scalarHigh, mat2);
        Core.MinMaxLocResult mmG = Core.minMaxLoc(mat2);

        Point point = mmG.maxLoc;

        if (point.x > 100.0 && point.x < 100 + h) {
            if (counter < 10) {
                Imgproc.circle(src, mmG.maxLoc, 25, new Scalar(0, 0, 255), 5, Imgproc.LINE_AA);
                try {

                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                play();
                counter++;
                setText();


                //10 punktow
                if ((point.y + 100 > 292.00 * scale && point.y + 100 < 486.00 * scale) && (point.x > 292.00 * scale && point.x < 486.00 * scale)) {
                    points += 10;

                }
                //8 punktów
                else if ((point.y + 100 > 196.00 * scale && point.y + 100 < 581.00 * scale) && (point.x > 197.00 * scale && point.x < 582.00 * scale)) {
                    points += 8;
                }
                //4 punkty
                else if ((point.y + 100 > 102.00 * scale && point.y + 100 < 677.00 * scale) && (point.x > 431.00 * scale && point.x < 676.00 * scale)) {
                    points += 4;
                }
                //2 punkty
                else if ((point.y + 100 > 5.00 * scale && point.y + 100 < 774.00 * scale) && (point.x > 6.00 * scale && point.x < 774.00 * scale)) {
                    points += 2;
                }
            }
            if (counter == 10) {
                try {
                    fileOutputStream.write(Integer.toString(points).getBytes());
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                startActivity(new Intent(ActivityCamera.this, ActivitySummary.class));
            }


        }
        slider = findViewById(R.id.slider);
        int brightness = (int) slider.getValue();

        src.convertTo(src, -1, 1, brightness);
        Imgproc.rectangle(src, new Point(100, 0), new Point(((h + h) / 2) + 100, (h + h) / 2), new Scalar(255, 0, 0), 5);

        return src;
    }


    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

        super.onPointerCaptureChanged(hasCapture);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (javaCameraView != null)
        {
            javaCameraView.disableView();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (javaCameraView != null)
        {
            javaCameraView.disableView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!OpenCVLoader.initDebug())
        {
            Log.e("OpenCV", "Unable to load OpenCV!");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, baseLoaderCallback);
        }

        else
        {
            Log.d("OpenCV", "OpenCV loaded Successfully!");
            baseLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS);
        }
    }

    public void play()
    {
        if(player == null)
        {
            player = MediaPlayer.create(this, R.raw.sample);
        }
        player.start();
    }
    public void setText()
    {
        if(datax.toString().equals("Polski"))
        {
            toast("Pozostała ilość strzałów: " +Integer.toString(10-counter));
        }
        else
        {
            toast("Shots left: " +Integer.toString(10-counter));
        }

    }
}