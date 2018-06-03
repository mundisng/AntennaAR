package com.mundis.kostas4949.antennavr;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.opengl.Matrix;
import android.view.View;



import java.util.ArrayList;
import java.util.List;

/**
 * Created by kostas4949 on 24/4/2017.
 */

public class AROverlay extends View {
    Context context;
    private float[] rotatedProjectionMatrix = new float[16];
    private Bitmap my_bitmap;

    public AROverlay(Context context/*,double my_radius,int antenum*/) {
        super(context);

        this.context = context;
        Resources res = getResources();
        my_bitmap = BitmapFactory.decodeResource(res, R.mipmap.ic_launcher_roundantenna);
        System.out.println("We got in AROverlay");
    }

    public void openDB(){
      //  databaseAccess = DatabaseAccess.getInstance(this.context);  //kane comment oles aytes tis grammes ama sou kollaei to kinhto
       // System.out.println("Opening database!");
       // databaseAccess.open();
    }

    public void closeDB(){
       // databaseAccess.close();
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
            if(App.current_location==null){
                return;
            }
            else{
                current_location=App.current_location;
            }
        }
        List<ARCoord> arPoints;
        synchronized(App.my_antennas_flag){
            if(App.my_antennas==null || App.my_antennas.isEmpty()){
                return;
            }
            else{
                arPoints=App.my_antennas;
            }
        }
        final int radius = 30;
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
         paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
         paint.setTextSize(60);

        for (int i = 0; i < arPoints.size(); i ++) {
            System.out.println("Drawing "+arPoints.size()+" points.");
            float[] currentLocationInECEF = LocationConverter.WGS84toECEF(current_location);
           // System.out.println("Location In ECEF: x: "+currentLocationInECEF[0]+" y: "+currentLocationInECEF[1]+" z: "+currentLocationInECEF[2]);
            float[] pointInECEF = LocationConverter.WGS84toECEF(arPoints.get(i).getLocation());
           // System.out.println("Location of Point in ECEF: x: "+pointInECEF[0]+" y: "+pointInECEF[1]+" z: "+pointInECEF[2]);
            float[] pointInENU = LocationConverter.ECEFtoENU(current_location, currentLocationInECEF, pointInECEF);
           // System.out.println("Position in ENU: East: "+pointInENU[0]+" North: "+pointInENU[1]+"Up: "+pointInENU[2]);
            float[] cameraCoordinateVector = new float[4];
            Matrix.multiplyMV(cameraCoordinateVector, 0, rotatedProjectionMatrix, 0, pointInENU, 0);

        // cameraCoordinateVector[2] is z, that always less than 0 to display on right position
        // if z > 0, the point will display on the opposite
        if (cameraCoordinateVector[2] < 0) {
           // System.out.println("Did we get in here?");
         float x  = (0.5f + cameraCoordinateVector[0]/cameraCoordinateVector[3]) * canvas.getWidth();
         float y = (0.5f - cameraCoordinateVector[1]/cameraCoordinateVector[3]) * canvas.getHeight();
        // System.out.println("DRAWING: X: "+x+" Y: "+y);
        //canvas.drawCircle(x, y, radius, paint);
            canvas.drawBitmap(my_bitmap,x,y,paint);
         canvas.drawText(arPoints.get(i).getName(), x - (30 * arPoints.get(i).getName().length() / 2), y - 80, paint);
         }
        }

    }
}