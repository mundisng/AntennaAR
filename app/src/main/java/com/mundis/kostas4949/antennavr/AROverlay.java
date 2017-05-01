package com.mundis.kostas4949.antennavr;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
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
    private Location currentLocation;
    private List<ARCoord> arPoints;


    public AROverlay(Context context) {
        super(context);

        this.context = context;

        //Demo points
        System.out.println("We got in AROverlay");
        arPoints = new ArrayList<ARCoord>() {{
            // add(new ARCoord("Sun Wheel", 16.0404856, 108.2262447, 0));
            // add(new ARCoord("Linh Ung Pagoda", 16.1072989, 108.2343984, 0));
            add(new ARCoord("testing",35.188726,25.718366,30));
        }};
    }

    public void updateRotatedProjectionMatrix(float[] rotatedProjectionMatrix) {
        this.rotatedProjectionMatrix = rotatedProjectionMatrix;
        this.invalidate();
    }

    public void updateCurrentLocation(Location currentLocation){
        this.currentLocation = currentLocation;
        //System.out.println("Got location as:"+currentLocation.getLatitude()+" "+currentLocation.getLongitude());
        this.invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        System.out.println("We got in here!");
        //  if (currentLocation == null) {
        //System.out.println("Current location is null?");
        //     return;
        // }
        //System.out.println("Starting drawing!");
        final int radius = 70;
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        // paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        // paint.setTextSize(60);

       /* for (int i = 0; i < arPoints.size(); i ++) {
            System.out.println("Drawing "+arPoints.size()+" points.");
            float[] currentLocationInECEF = LocationConverter.WSG84toECEF(currentLocation);
            System.out.println("Location In ECEF: x: "+currentLocationInECEF[0]+" y: "+currentLocationInECEF[1]+" z: "+currentLocationInECEF[2]);
            float[] pointInECEF = LocationConverter.WSG84toECEF(arPoints.get(i).getLocation());
            System.out.println("Location of Point in ECEF: x: "+pointInECEF[0]+" y: "+pointInECEF[1]+" z: "+pointInECEF[2]);
            float[] pointInENU = LocationConverter.ECEFtoENU(currentLocation, currentLocationInECEF, pointInECEF);
            System.out.println("Position in ENU: East: "+pointInENU[0]+" North: "+pointInENU[1]+"Up: "+pointInENU[2]);
            float[] cameraCoordinateVector = new float[4];
            Matrix.multiplyMV(cameraCoordinateVector, 0, rotatedProjectionMatrix, 0, pointInENU, 0);
*/
        // cameraCoordinateVector[2] is z, that always less than 0 to display on right position
        // if z > 0, the point will display on the opposite
        //if (cameraCoordinateVector[2] < 0) {
        //    System.out.println("Did we get in here?");
        // float x  = (0.5f + cameraCoordinateVector[0]/cameraCoordinateVector[3]) * canvas.getWidth();
        // float y = (0.5f - cameraCoordinateVector[1]/cameraCoordinateVector[3]) * canvas.getHeight();
        // System.out.println("DRAWING: X: "+x+" Y: "+y);
        canvas.drawCircle(300, 300, radius, paint);
        // canvas.drawText(arPoints.get(i).getName(), x - (30 * arPoints.get(i).getName().length() / 2), y - 80, paint);
        // }
        //}
    }
}