package admobilize.matrix.malosclient;

import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Antonio Vanegas @hpsaturn on 11/12/16.
 */

public abstract class BaseActivity extends AppCompatActivity {

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
        startDrivers();
        mSlowTimer = new Timer();
        mFastTimer = new Timer();
        slowTimer();
        fastTimer();
        super.onResume();
    }

    @Override
    protected void onStop() {
        mSlowTimer.cancel();
        mFastTimer.cancel();
        stopDrivers();
        super.onStop();
    }

}
