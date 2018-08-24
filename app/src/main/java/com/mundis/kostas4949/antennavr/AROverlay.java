package com.mundis.kostas4949.antennavr;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.location.Location;
import android.opengl.Matrix;
import android.view.View;
import java.util.List;

/**
 * Created by kostas4949 on 24/4/2017.
 */

public class AROverlay extends View {
    Context context;
    private float[] rotatedProjectionMatrix = new float[16];
    private Bitmap my_bitmap;

    public AROverlay(Context context) {
        super(context);
        this.context = context;
        Resources res = getResources();
        my_bitmap = BitmapFactory.decodeResource(res, R.mipmap.ic_launcher_roundantenna);  //use this icon for cell towers
    }

    public void updateRotatedProjectionMatrix(float[] rotatedProjectionMatrix) {
        this.rotatedProjectionMatrix = rotatedProjectionMatrix;
        this.invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Location current_location;
        synchronized(App.current_location_flag){
            if(App.current_location==null){    //If we don't have a current location don't draw anything.
                return;
            }
            else{
                current_location=App.current_location;
            }
        }
        List<ARCoord> arPoints;
        synchronized(App.my_antennas_flag){
            if(App.my_antennas==null || App.my_antennas.isEmpty()){   //If we don't have any antennas (arpoints) don't draw anything.
                return;
            }
            else{
                arPoints=App.my_antennas;
            }
        }
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);  //Set drawing options
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
         paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
         paint.setTextSize(60);

        for (int i = 0; i < arPoints.size(); i ++) {     //Drawing the arpoints on AR screen
            float[] currentLocationInECEF = LocationConverter.WGS84toECEF(current_location); //Convert our current location to ECEF
            float[] pointInECEF = LocationConverter.WGS84toECEF(arPoints.get(i).getLocation()); //Convert arpoint to ECEF
            float[] pointInENU = LocationConverter.ECEFtoENU(current_location, currentLocationInECEF, pointInECEF); //Get the ENU of arpoint having the phone location as reference point
            float[] cameraCoordinateVector = new float[4];
            Matrix.multiplyMV(cameraCoordinateVector, 0, rotatedProjectionMatrix, 0, pointInENU, 0); //Multiply the ENU of arpoint with the camera rotated projection matrix(based on camera projection and sensors for phone rotation).

        // cameraCoordinateVector[2] is z, that is always less than 0 to display on right position
        // if z > 0, the point will display on the opposite
        if (cameraCoordinateVector[2] < 0) {
         float x  = (0.5f + cameraCoordinateVector[0]/cameraCoordinateVector[3]) * canvas.getWidth(); //Draw the arpoint based on canvas size.
         float y = (0.5f - cameraCoordinateVector[1]/cameraCoordinateVector[3]) * canvas.getHeight();
         canvas.drawBitmap(my_bitmap,x,y,paint);
         canvas.drawText(arPoints.get(i).getName(), x - (30 * arPoints.get(i).getName().length() / 2), y - 80, paint);  //Draw arpoint name
         }
        }

    }
}