package agniev.nettesting;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import static agniev.nettesting.Allocator.*;
import static agniev.nettesting.GraphicsView.*;

public class MainActivity extends Activity implements View.OnClickListener {

    Button btnMeasurement, btnTypeOfMeasure, btnPlan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "//////////////////////////NEW_APP_TEST///////////////////////");
        allocateParams(this);

        setContentView(R.layout.activity_main);

        btnMeasurement = (Button) findViewById(R.id.btnMeasurement);
        btnMeasurement.setOnClickListener(this);
        btnTypeOfMeasure = (Button) findViewById(R.id.btnTypeOfMeasure);
        btnTypeOfMeasure.setOnClickListener(this);
        btnPlan = (Button) findViewById(R.id.btnPlan);
        btnPlan.setOnClickListener(this);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getRegisteredCellInfo(MainActivity.this);
                }
                return;
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnMeasurement:
                inMeasure = !inMeasure;
                if (inMeasure){
                    ((Button)findViewById(R.id.btnMeasurement)).setText("Измерение");
                } else {
                    ((Button)findViewById(R.id.btnMeasurement)).setText("Позиция");
                }
                break;
            case R.id.btnTypeOfMeasure:
                if (inPlan){
                    graphicsBitmap = myBitmapLast;
                    inPlan = false;
                    ((Button)findViewById(R.id.btnPlan)).setText("Сигнал");
                } else {
                    if (graphicsBitmap == myBitmapRsrp) {
                        graphicsBitmap = myBitmapNetworkClass;
                        ((Button) findViewById(R.id.btnTypeOfMeasure)).setText("Тип сети");
                    } else {
                        graphicsBitmap = myBitmapRsrp;
                        ((Button) findViewById(R.id.btnTypeOfMeasure)).setText("Уровень сигнала");
                    }
                }
                findViewById(R.id.toSeePlan).invalidate();
                break;
            case R.id.btnPlan:
                if(graphicsBitmap == myBitmap){
                    graphicsBitmap = myBitmapLast;
                    inPlan = false;
                    ((Button)findViewById(R.id.btnPlan)).setText("Сигнал");
                } else {
                    myBitmapLast = graphicsBitmap;
                    graphicsBitmap = myBitmap;
                    inPlan = true;
                    ((Button)findViewById(R.id.btnPlan)).setText("План");
                }
                findViewById(R.id.toSeePlan).invalidate();
                break;
            default:
                break;
        }
    }

}
