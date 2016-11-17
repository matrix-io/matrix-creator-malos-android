package admobilize.matrix.malosclient;

import android.app.ProgressDialog;
import android.graphics.drawable.Drawable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
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

    private JoystickView mJoystickView;

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

        mJoystickView = (JoystickView) findViewById(R.id.joystickView);
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

    @Override
    protected void onResume() {
        if(isTargetConfig) {
            startDrivers();
        }
        super.onResume();
    }

    @Override
    protected void onStop() {
        if(isTargetConfig) {
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


	public void joystickButtonSwitchStateChanged(boolean buttonState) {
		mJoystickView.setPressed(buttonState);
	}

    public void joystickMoved(int x, int y) {
		mJoystickView.setPosition(x, y);
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
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

    abstract void startDiscovery();
    abstract void showDeviceInfo();
    abstract void showIPInputFragment();

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

    public void showSnackLong(String msg) {
        Snackbar.make(this.getCurrentFocus(), msg, Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }

    public void showSnackLong(int msg) {
        Snackbar.make(this.getCurrentFocus(), msg, Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }




}
