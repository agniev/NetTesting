package agniev.nettesting;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import static agniev.nettesting.Allocator.*;
import static java.lang.Math.*;

public class GraphicsView extends View {
    static boolean inMeasure = false;
    static boolean inPlan = false;
    float zeroX = 0;
    float zeroY = 0;
    float touchX = 0;
    float touchY = 0;
    float touchX2 = 0;
    float touchY2 = 0;
    static float scale = 1.0f;
    float newScale = 1.0f;
    float setScale = 1.0f;
    boolean inSecondTouch = false;
    boolean inTouch = false;
    static Bitmap graphicsBitmap = myBitmapRsrp;


    public GraphicsView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    public GraphicsView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public GraphicsView(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.scale(setScale, setScale);
        canvas.drawBitmap(graphicsBitmap, zeroX, zeroY, null);
        super.onDraw(canvas);
    }

    public boolean onTouchEvent(MotionEvent event) {
        if(!inMeasure){
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    inTouch = true;
                    touchX = event.getX(0);
                    touchY = event.getY(0);
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    inSecondTouch = true;
                    touchX2 = event.getX(1);
                    touchY2 = event.getY(1);
                    break;
                case MotionEvent.ACTION_MOVE:
                    if(!inSecondTouch && inTouch) {
                        zeroX += event.getX(0) - touchX;
                        zeroY += event.getY(0) - touchY;
                        touchX = event.getX(0);
                        touchY = event.getY(0);
                        invalidate();
                    } else if(inSecondTouch && inTouch){
                        newScale = (float) (sqrt(pow(event.getX(0)-event.getX(1),2)+pow(event.getY(0)-event.getY(1),2))
                                /sqrt(pow(touchX-touchX2,2)+pow(touchY-touchY2,2)));
                        setScale = scale*newScale;
                        invalidate();
                    }
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    scale = setScale;
                    inSecondTouch = false;
                    inTouch = false;
                    break;
                default:
                    break;
            }
        } else {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_UP:
                    addMeasuredPoint((Activity) findViewById(R.id.toSeePlan).getContext(),
                            tableCoordinate(zeroX, event.getX(0)), tableCoordinate(zeroY, event.getY(0)));
                    cellMeasuredValues.getLast().setInterpolatedNetworkClass();
                    cellMeasuredValues.getLast().toSeeLastMeasured(cellInterpolatedNetworkClass, myBitmapNetworkClass);
                    cellMeasuredValues.getLast().setInterpolatedRsrp();
                    cellMeasuredValues.getLast().toSeeLastMeasured(cellInterpolatedRsrp, myBitmapRsrp);
                    invalidate();
                    break;
                default:
                    break;
            }
        }
        return true;
    }

    static int tableCoordinate(float zero, float z){
        int tableZ = round((-zero * scale + z)/scale/pxSize);
        return tableZ;
    }
}
