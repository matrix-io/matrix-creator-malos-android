package admobilize.matrix.malosclient;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.Timer;
import java.util.TimerTask;

import admobilize.matrix.malosclient.ui.ColorLEDController;

/**
 * Created by Antonio Vanegas @hpsaturn on 11/12/16.
 */

public abstract class BaseActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final boolean DEBUG = Config.DEBUG;
    private static final boolean VERBOSE = Config.VERBOSE;

    public static Timer mSlowTimer;
    public static Timer mFastTimer;
    public Drawable mOffBackground;
    public Drawable mOnBackground;
    public ToggleButton outputButton;
    public ImageButton inputButton;
    public Drawable mOffImage;
    public Drawable mOnImage;
    public TextView uv_value;
    public TextView uv_risk;
    public TextView temp_value;
    public TextView humi_value;

    private boolean isTargetConfig=false;
    public ProgressDialog loader;

    public void instanceUI (){
        outputButton = (ToggleButton) findViewById(R.id.tb_main_ouput);
        inputButton = (ImageButton) findViewById(R.id.ib_main_input);

        uv_value = (TextView)findViewById(R.id.tv_sensor_uv_percent_value);
        uv_risk = (TextView)findViewById(R.id.tv_sensor_uv_detail);
        temp_value = (TextView)findViewById(R.id.tv_sensor_temp_value);
        humi_value = (TextView)findViewById(R.id.tv_sensor_humidity_value);

        mOffBackground = getResources().getDrawable(R.drawable.toggle_button_off_holo_dark);
		mOnBackground = getResources().getDrawable(R.drawable.toggle_button_on_holo_dark);
        int onImageId = R.drawable.indicator_button1_on_noglow;
        int offImageId = R.drawable.indicator_button1_off_noglow;
        mOffImage = getResources().getDrawable(offImageId);
        mOnImage = getResources().getDrawable(onImageId);

        ColorLEDController ledController = new ColorLEDController((MainActivity) this, 1, getResources(),true);
        ledController.attachToView((ViewGroup) findViewById(R.id.leds1));

    }

    public void initLoader(){
        loader = new ProgressDialog(this);
        loader.setIndeterminate(true);
        loader.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        loader.setMessage(getString(R.string.msg_loading));
    }

    abstract void startDrivers();

    abstract void stopDrivers();

    public void slowTimer() {
        mSlowTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                slowUpdateDevices();
            }
        }, 0, 7000);
    }

    public void fastTimer() {
        mFastTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                fastUpdateDevices();
            }
        }, 0, 500);
    }

    abstract void fastUpdateDevices();

    abstract void slowUpdateDevices();

    @Override
    protected void onResume() {
        if(isTargetConfig) {
            startDrivers();
            mSlowTimer = new Timer();
            mFastTimer = new Timer();
            slowTimer();
            fastTimer();
        }
        super.onResume();
    }

    @Override
    protected void onStop() {
        if(isTargetConfig) {
            mSlowTimer.cancel();
            mFastTimer.cancel();
            stopDrivers();
        }
        super.onStop();
    }

    public boolean isTargetConfig() {
        return isTargetConfig;
    }

    public void setTargetConfig(boolean targetConfig) {
        isTargetConfig = targetConfig;
    }


    public void showLoader(int msg) {
        if (loader != null) {
            try {
                if(msg>0)loader.setMessage(getString(msg));
                loader.show();
            } catch (Exception e) {
                if (DEBUG) Log.d(TAG, "LOADER Exception:");
                if (DEBUG) e.printStackTrace();
            }
        }
    }

    public void dismissLoader() {
        if(loader!=null){
            try {
                if(loader.isShowing())loader.dismiss();
            } catch (Exception e) {
                if(DEBUG)Log.d(TAG,"LOADER Exception:");
                if(DEBUG)e.printStackTrace();
            }
        }
    }


}
