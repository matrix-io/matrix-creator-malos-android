package admobilize.matrix.malosclient;

import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.protobuf.InvalidProtocolBufferException;

import java.util.Timer;
import java.util.TimerTask;

import matrix_malos.Driver.GpioParams.Builder;

import static admobilize.matrix.malosclient.MalosDevice.*;
import static android.widget.CompoundButton.*;
import static matrix_malos.Driver.*;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final boolean DEBUG = Config.DEBUG;

    private static Timer timer;
    private Drawable mOffBackground;
    private Drawable mOnBackground;
    private ToggleButton outputButton;
    private ImageButton inputButton;

    private MalosDevice gpio;
    private MalosDevice humidity;
    private MalosDevice uv;
    private Drawable mOffImage;
    private Drawable mOnImage;
    private TextView uv_value;
    private TextView uv_risk;
    private TextView temp_value;
    private TextView humi_value;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gpio = new MalosDevice(MalosTarget.GPIO);
        humidity = new MalosDevice(MalosTarget.HUMIDITY);
        uv = new MalosDevice(MalosTarget.UV);

        outputButton = (ToggleButton) findViewById(R.id.tb_main_ouput);
        inputButton = (ImageButton) findViewById(R.id.ib_main_input);
        outputButton.setOnCheckedChangeListener(onCheckedGpioToggleButton);

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

    private OnCheckedChangeListener onCheckedGpioToggleButton = new OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                if(DEBUG)Log.i(TAG,"outputButton on");
                outputButton.setBackgroundDrawable(mOnBackground);
                configGpioOuputValue(0,1);
            } else {
                if(DEBUG)Log.i(TAG,"outputButton off");
                outputButton.setBackgroundDrawable(mOffBackground);
                configGpioOuputValue(0,0);
            }
        }
    };


    private OnSubscriptionCallBack onGpioInputCallBack = new OnSubscriptionCallBack() {
        @Override
        public void onReceiveData(final byte[] data) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        GpioParams gpioParams = GpioParams.parseFrom(data);
                        if(DEBUG)Log.d(TAG,"onGpioInputCallBack receive value: "+gpioParams.getValue());
                        if(gpioParams.getValue()==0)inputButton.setImageDrawable(mOffImage);
                        else inputButton.setImageDrawable(mOnImage);
                    } catch (InvalidProtocolBufferException e) {
                        e.printStackTrace();
                    }

                }
            });
        }
    };

    private OnSubscriptionCallBack onHumidityDataCallBack = new OnSubscriptionCallBack() {
        @Override
        public void onReceiveData(final byte[] data) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Humidity humidity = Humidity.parseFrom(data);
                        if(DEBUG)Log.d(TAG,"onHumidityDataCallBack humidity: "+humidity.getHumidity());
                        if(DEBUG)Log.d(TAG,"onHumidityDataCallBack temperature: "+humidity.getTemperature());
                        temp_value.setText(""+((int)(humidity.getTemperature()*10))/10.0f+"º");
                        humi_value.setText(""+((int)(humidity.getHumidity()*10))/10.0f);
                    } catch (InvalidProtocolBufferException e) {
                        e.printStackTrace();
                    }
                }
            });

        }
    };

    private OnSubscriptionCallBack onUVDataCallBack = new OnSubscriptionCallBack() {
        @Override
        public void onReceiveData(final byte[] data) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        UV uv = UV.parseFrom(data);
                        if(DEBUG)Log.d(TAG,"onUVData OmsRisk: "+uv.getOmsRisk());
                        if(DEBUG)Log.d(TAG,"onUVData UvIndex: "+uv.getUvIndex());
                        uv_value.setText(""+Integer.parseInt(""+(int)(uv.getUvIndex()*1000)));
                        uv_risk.setText(uv.getOmsRisk());
                    } catch (InvalidProtocolBufferException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    public void configGpioOuputValue(int pin, int value){
        Builder gpioParams = GpioParams.newBuilder();
        gpioParams.setPin(pin);
        gpioParams.setModeValue(GpioParams.EnumMode.OUTPUT_VALUE);
        gpioParams.setValue(value);
        gpio.config(DriverConfig.newBuilder().setGpio(gpioParams));
    }

    public void requestGpioInputValue(int pin){
        Builder gpioParams = GpioParams.newBuilder();
        gpioParams.setPin(pin);
        gpioParams.setModeValue(GpioParams.EnumMode.INPUT_VALUE);
        gpio.config(DriverConfig.newBuilder().setGpio(gpioParams));
    }

    public void requestHumidityData(){
        humidity.push("");
    }

    public void requestUVData(){
        uv.push("");
    }

    @Override
    protected void onResume() {
        gpio.bindConfig();
        gpio.subscribe(onGpioInputCallBack);
        humidity.subscribe(onHumidityDataCallBack);
        uv.subscribe(onUVDataCallBack);
        timer = new Timer();
        startTimer();
        super.onResume();
    }

    @Override
    protected void onStop() {
        timer.cancel();
        gpio.unbindConfig();
        gpio.unsubscribe();
        humidity.unsubscribe();
        uv.unsubscribe();
        super.onStop();
    }

    public void startTimer() {
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                requestGpioInputValue(1);
                requestHumidityData();
                requestUVData();
            }
        }, 0, 1000);
    }


}
