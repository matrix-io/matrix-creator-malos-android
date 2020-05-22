package admobilize.matrix.malosclient;

import android.app.ProgressDialog;
import android.graphics.drawable.Drawable;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.Timer;
import java.util.TimerTask;

import admobilize.matrix.malosclient.ui.ColorLEDController;
import admobilize.matrix.malosclient.ui.JoystickView;
import admobilize.matrix.malosclient.utils.Tools;

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
    public TextView humi_temp_value;
    public TextView humi_value;
    public TextView press_value;
    public TextView press_temp_value;
    public TextView press_alti_value;
    private JoystickView mJoystickView;
    private ProgressDialog loader;

    private boolean isTargetConfig=false;


    /******************************************************
     * TIMER METHODS FOR DRIVER SENSORS PINGS
     ******************************************************/

    private void pingTimer() {
        mSlowTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                pingDevices();
            }
        }, 0, Config.TIME_INTO_PING);
    }

    public void startPingTimer(){
        mSlowTimer = new Timer();
        pingTimer();
    }

    public void stopPingTimer(){
        mSlowTimer.cancel();
    }
    abstract void pingDevices();

    /********************************************************
     * DISCONNECT DRIVER ON EXIT APP
     ********************************************************/

    @Override
    protected void onDestroy() {
        if(isTargetConfig) {
            if(DEBUG)Log.d(TAG,"onDestroy stopping drivers..");
            stopDrivers();
        }
        super.onDestroy();
    }

    /**********************************************************
     * MAIN ACTIVITIES METHODS
     **********************************************************/

    public boolean isTargetConfig() {
        return isTargetConfig;
    }

    public void setTargetConfig(boolean targetConfig) {
        isTargetConfig = targetConfig;
    }

    abstract void startDrivers();
    abstract void stopDrivers();
    abstract void startDiscovery();
    abstract void showDeviceInfo();
    abstract void showIPInputFragment();

    /*******************************************************************************
     *                        U I   M E T H O D S
     * *****************************************************************************/

    public void instanceUI (){
        outputButton = (ToggleButton) findViewById(R.id.tb_main_ouput);
        inputButton = (ImageButton) findViewById(R.id.ib_main_input);

        uv_value = (TextView)findViewById(R.id.tv_sensor_uv_percent_value);
        uv_risk = (TextView)findViewById(R.id.tv_sensor_uv_detail);
        humi_temp_value = (TextView)findViewById(R.id.tv_humidity_temp_value);
        humi_value = (TextView)findViewById(R.id.tv_humidity_value);
        press_value = (TextView)findViewById(R.id.tv_pressure_value);
        press_alti_value = (TextView)findViewById(R.id.tv_pressure_altitude_value);
        press_temp_value = (TextView)findViewById(R.id.tv_pressure_temp_value);

        mOffBackground = getResources().getDrawable(R.drawable.toggle_button_off_holo_dark);
		mOnBackground = getResources().getDrawable(R.drawable.toggle_button_on_holo_dark);
        int onImageId = R.drawable.indicator_button1_on_noglow;
        int offImageId = R.drawable.indicator_button1_off_noglow;
        mOffImage = getResources().getDrawable(offImageId);
        mOnImage = getResources().getDrawable(onImageId);

        mJoystickView = (JoystickView) findViewById(R.id.imu_view);
        ColorLEDController ledController = new ColorLEDController((MainActivity) this, 1, getResources(),true);
        ledController.attachToView((ViewGroup) findViewById(R.id.leds1));

    }

    public void initLoader(){
        loader = new ProgressDialog(this);
        loader.setIndeterminate(true);
        loader.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        loader.setMessage(getString(R.string.msg_loading));
    }

    public void joystickButtonSwitchStateChanged(boolean buttonState) {
		mJoystickView.setPressed(buttonState);
	}

    public void joystickMoved(int x, int y) {
		mJoystickView.setPosition(x, y);
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        menu.add(Menu.NONE, menu.size(), Menu.NONE,
                "Version "+Tools.getVersionName(this)+
                " rev"+Tools.getVersionCode(this));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_scan_devices) {
            startDiscovery();
            return true;
        }
        if (id == R.id.action_get_device_info) {
            showDeviceInfo();
            return true;
        }
        if (id == R.id.action_show_ip_input) {
            showIPInputFragment();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void showSnackLong(String msg) {
        Snackbar.make(this.getCurrentFocus(), msg, Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }

    public void showSnackLong(int msg) {
        Snackbar.make(this.getCurrentFocus(), msg, Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }

    public void showLoader(int msg) {
        if (loader != null) {
            try {
                if(msg>0)loader.setMessage(getString(msg));
                if(!loader.isShowing())loader.show();
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

    /*******************************************************************************
     *                        F R A G M E N T   T O O L S
     * *****************************************************************************
     *
     * @param fragment fragment to replace on container
     * @param fragmentTag TAG of fragment for post searching
     * @param toStack enable/disable put on popbackstack
     */

    public void showFragment(Fragment fragment, String fragmentTag, boolean toStack) {

        try {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_default, fragment, fragmentTag);
            if (toStack) ft.addToBackStack(fragmentTag);
            ft.commitAllowingStateLoss();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void showFragment(Fragment fragment, String fragmentTag, boolean toStack, int content) {

        try {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(content, fragment, fragmentTag);
            if (toStack) ft.addToBackStack(fragmentTag);
            ft.commitAllowingStateLoss();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void showFragmentFull(Fragment fragment, String fragmentTag, boolean toStack) {

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_default, fragment, fragmentTag);
        if (toStack) ft.addToBackStack(fragmentTag);
        ft.commitAllowingStateLoss();

    }

    public void showDialog(Fragment fragment, String fragmentTag){
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(fragment, fragmentTag);
        ft.show(fragment);
        ft.commitAllowingStateLoss();
    }

    public void popBackStackSecure(String TAG) {
        try {
            if (DEBUG) Log.d(TAG, "popBackStackSecure to: " + TAG);
            getSupportFragmentManager().popBackStack(TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void popBackLastFragment() {
        if (getSupportFragmentManager().getBackStackEntryCount() != 0) {
            if (DEBUG) Log.d(TAG, "onBackPressed popBackStack for:" + getLastFragmentName());
            getSupportFragmentManager().popBackStack();
        }
    }


    public void removeFragment(Fragment fragment) {
        try {
            if (DEBUG) Log.w(TAG, "removing fragment: " + fragment.getClass().getSimpleName());
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.remove(fragment).commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getLastFragmentName() {
        try {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) return "";
            FragmentManager fm = getSupportFragmentManager();
            return fm.getBackStackEntryAt(fm.getBackStackEntryCount() - 1).getName();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public boolean isFragmentInStack(String tag) {
        try {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) return false;
            FragmentManager fm = getSupportFragmentManager();
            Fragment match = fm.findFragmentByTag(tag);
            if (match!=null)return true;
            else return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
