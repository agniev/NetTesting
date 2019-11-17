package agniev.nettesting;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.*;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;

import static android.telephony.TelephonyManager.*;
import static java.lang.Math.pow;
import static java.lang.Math.round;

public class Allocator {
    static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
    static final String TAG = "myLogs";

    static int[] colours;
    static Bitmap myBitmap;
    static Bitmap myBitmapRsrp;
    static Bitmap myBitmapNetworkClass;
    static Bitmap myBitmapLast;
    static float part;
    static float r;
    static int radius;
    static int tableRadius;
    static int coloumns;
    static int rows;
    static float pxSize;

    private int networkType;
    private CellInfo cellInfo;
    private int measuredX;
    private int measuredY;
    static LinkedList<Allocator> cellMeasuredValues;
    static int[][] cellInterpolatedRsrp;
    static int[][] cellInterpolatedNetworkClass;


    private static final int NETWORK_CLASS_UNKNOWN = 0;
    private static final int NETWORK_CLASS_2_G = 2;
    private static final int NETWORK_CLASS_3_G = 3;
    private static final int NETWORK_CLASS_4_G = 4;
    private static final int NETWORK_TYPE_LTE_CA = 19;

    Allocator(int networkType, CellInfo cellInfo){
        this.networkType = networkType;
        this.cellInfo = cellInfo;
    }

    public int getMeasuredX() {
        return measuredX;
    }

    public void setMeasuredX(int measuredX) {
        this.measuredX = measuredX;
    }

    public int getMeasuredY() {
        return measuredY;
    }

    public void setMeasuredY(int measuredY) {
        this.measuredY = measuredY;
    }

    public int getNetworkType() {
        return networkType;
    }

    public CellInfo getCellInfo() {
        return cellInfo;
    }

    static void allocateParams(Activity activity){
        colours = allocateColours();
        myBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.first_floor);
        myBitmapRsrp = myBitmap.copy(Bitmap.Config.RGB_565, true);
        myBitmapNetworkClass = myBitmap.copy(Bitmap.Config.RGB_565, true);

        part = 647f/29.22f/800f; //длина объекта в пикселах/длина объекта в метрах/ширина изображения в пикселах
        r = 0.75f; //радиус апроксимации в метрах
        radius = round(myBitmap.getWidth()*r*part); //радиус апроксимации в пикселах объекта Bitmap
        coloumns = 1000; //число столбцов в сетке
        pxSize = myBitmap.getWidth()/(float)coloumns; //число пикселов в ячейке сетки
        Log.d(TAG, "getMyBitmapRsrpWidth() "+ 1000*pxSize);
        rows = round(myBitmap.getHeight()/pxSize); //число строк в сетке
        Log.d(TAG, "rows "+ rows);
        Log.d(TAG, "Heigth "+ rows*pxSize);
        cellMeasuredValues = new LinkedList<>();
        cellInterpolatedRsrp = new int[coloumns][rows];
        cellInterpolatedNetworkClass = new int[coloumns][rows];
        tableRadius = round(radius/pxSize);
    }

    static int[] allocateColours(){
        int[] colours = new int[511];
        int count = 0xff0000;
        for (int i = 0; i <= 255; i++){
            colours[i] = count;
            count += 0x100;
        }
        count = 0xffff00;
        for (int i = 256; i < 511; i++){
            count -= 0x10000;
            colours[i] = count;
        }
        return colours;
    }

    static Allocator getRegisteredCellInfo(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
        } else {
            List<CellInfo> CellInfo_list;
            TelephonyManager telephonyManager;
            telephonyManager = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);
            CellInfo_list = telephonyManager.getAllCellInfo();
            int networkType = telephonyManager.getNetworkType();
            for (int i = 0; i < CellInfo_list.size(); i++) {
                if (CellInfo_list.get(i).isRegistered()) {
                    Allocator myCell = new Allocator(networkType, CellInfo_list.get(i));
                    return myCell;
                }
            }
        }
        return null;
    }

    static void addMeasuredPoint(Activity activity, int tableX, int tableY){
        Allocator measuredPoint = getRegisteredCellInfo(activity);
        measuredPoint.setMeasuredX(tableX);
        measuredPoint.setMeasuredY(tableY);
        cellMeasuredValues.add(measuredPoint);
    }

    public void setInterpolatedRsrp(){
        for (int i = this.getMeasuredX() - tableRadius; i <= this.getMeasuredX() + tableRadius; i++) {
            for (int j = this.getMeasuredY() - tableRadius; j <= this.getMeasuredY() + tableRadius; j++) {
                if(i >= 0 && i < coloumns && j >= 0 && j < rows){
                    if (pow(i - this.getMeasuredX(), 2) + pow(j - this.getMeasuredY(), 2) <= pow(tableRadius, 2)) {
                        if(cellInterpolatedRsrp[i][j] == 0){
                            cellInterpolatedRsrp[i][j] = this.getRsrpSignalStrength();
                        } else {
                            cellInterpolatedRsrp[i][j] = (cellInterpolatedRsrp[i][j]
                                    + this.getRsrpSignalStrength())/2;
                        }
                    }
                }
            }
        }
    }

    public void setInterpolatedNetworkClass(){
        for (int i = this.getMeasuredX() - tableRadius; i <= this.getMeasuredX() + tableRadius; i++) {
            for (int j = this.getMeasuredY() - tableRadius; j <= this.getMeasuredY() + tableRadius; j++) {
                if(i >= 0 && i < coloumns && j >= 0 && j < rows){
                    if (pow(i - this.getMeasuredX(), 2) + pow(j - this.getMeasuredY(), 2) <= pow(tableRadius, 2)) {
                        cellInterpolatedNetworkClass[i][j] = getNetworkClass(this.getNetworkType());
                    }
                }
            }
        }
    }

    public void toSeeLastMeasured(int[][] array, Bitmap bitmap){
        for (int i = this.getMeasuredX() - tableRadius; i <= this.getMeasuredX() + tableRadius; i++) {
            for (int j = this.getMeasuredY() - tableRadius; j <= this.getMeasuredY() + tableRadius; j++) {
                if(i >= 0 && i < coloumns-1 && j >= 0 && j < rows-1){
                    if (pow(i - this.getMeasuredX(), 2) + pow(j - this.getMeasuredY(), 2) <= pow(tableRadius, 2)) {
                        if(array[i][j] != 0){
                            if(array[i][j] < 0){
                                toFillColours(colours[510*(array[i][j]+140)/97], round(i*pxSize), round(j*pxSize), bitmap);
                            }else{
                                switch (array[i][j]){
                                    case(2):
                                        toFillColours(0x00FFFF, round(i*pxSize), round(j*pxSize), bitmap);
                                        break;
                                    case(3):
                                        toFillColours(0x4682B4, round(i*pxSize), round(j*pxSize), bitmap);
                                        break;
                                    case(4):
                                        toFillColours(0x0000CD, round(i*pxSize), round(j*pxSize), bitmap);
                                        break;
                                    default:
                                        toFillColours(0xEE82EE, round(i*pxSize), round(j*pxSize), bitmap);
                                        break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    static void toFillColours(int colour, int startX, int startY, Bitmap bitmap){
        for (int i = startX; i < startX + pxSize; i++) {
            for (int j = startY; j < startY + pxSize; j++) {
                bitmap.setPixel(i, j, colour);
            }
        }
    }

    public int getRsrpSignalStrength(){
        switch (this.getNetworkType()){
            case (NETWORK_TYPE_LTE):
                return ((CellInfoLte) this.getCellInfo()).getCellSignalStrength().getDbm();
            case (NETWORK_TYPE_HSPA):
            case (NETWORK_TYPE_UMTS):
                return ((CellInfoWcdma) this.getCellInfo()).getCellSignalStrength().getDbm();
            case (NETWORK_TYPE_EDGE):
            case (NETWORK_TYPE_GSM):
                return ((CellInfoGsm) this.getCellInfo()).getCellSignalStrength().getDbm();
            default:
                return 0;
        }
    }

    public static int getNetworkClass(int networkType) {
        switch (networkType) {
            case NETWORK_TYPE_GPRS:
            case NETWORK_TYPE_GSM:
            case NETWORK_TYPE_EDGE:
            case NETWORK_TYPE_CDMA:
            case NETWORK_TYPE_1xRTT:
            case NETWORK_TYPE_IDEN:
                return NETWORK_CLASS_2_G;
            case NETWORK_TYPE_UMTS:
            case NETWORK_TYPE_EVDO_0:
            case NETWORK_TYPE_EVDO_A:
            case NETWORK_TYPE_HSDPA:
            case NETWORK_TYPE_HSUPA:
            case NETWORK_TYPE_HSPA:
            case NETWORK_TYPE_EVDO_B:
            case NETWORK_TYPE_EHRPD:
            case NETWORK_TYPE_HSPAP:
            case NETWORK_TYPE_TD_SCDMA:
                return NETWORK_CLASS_3_G;
            case NETWORK_TYPE_LTE:
            case NETWORK_TYPE_IWLAN:
            case NETWORK_TYPE_LTE_CA:
                return NETWORK_CLASS_4_G;
            default:
                return NETWORK_CLASS_UNKNOWN;
        }
    }
}
