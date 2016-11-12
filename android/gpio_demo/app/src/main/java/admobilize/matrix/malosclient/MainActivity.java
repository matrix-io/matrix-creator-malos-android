package admobilize.matrix.malosclient;

import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import static android.widget.CompoundButton.OnCheckedChangeListener;
import com.google.protobuf.InvalidProtocolBufferException;
import java.text.DecimalFormat;
import matrix_malos.Driver.GpioParams.Builder;

import static admobilize.matrix.malosclient.MalosDevice.OnSubscriptionCallBack;
import static matrix_malos.Driver.DriverConfig;
import static matrix_malos.Driver.EverloopImage;
import static matrix_malos.Driver.GpioParams;
import static matrix_malos.Driver.Humidity;
import static matrix_malos.Driver.HumidityParams;
import static matrix_malos.Driver.LedValue;
import static matrix_malos.Driver.UV;

public class MainActivity extends BaseActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final boolean DEBUG = Config.DEBUG;
    private static final boolean VERBOSE = Config.VERBOSE;

    private MalosDevice gpio;
    private MalosDevice humidity;
    private MalosDevice uv;
    private MalosDevice everloop;

    private int red, green, blue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gpio = new MalosDevice(MalosTarget.GPIO);
        humidity = new MalosDevice(MalosTarget.HUMIDITY);
        uv = new MalosDevice(MalosTarget.UV);
        everloop = new MalosDevice(MalosTarget.EVERLOOP);

        instanceUI();

        ColorLEDController ledController = new ColorLEDController(this, 1, getResources(),true);
        ledController.attachToView((ViewGroup) findViewById(R.id.leds1));
        outputButton.setOnCheckedChangeListener(onCheckedGpioToggleButton);

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
                        if(VERBOSE)Log.d(TAG,"onGpioInputCallBack receive value: "+gpioParams.getValue());
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
                        if(VERBOSE)Log.d(TAG,"onHumidityDataCallBack humidity: "+humidity.getHumidity());
                        if(VERBOSE)Log.d(TAG,"onHumidityDataCallBack temperature: "+humidity.getTemperature());
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
                        if(VERBOSE)Log.d(TAG,"onUVData OmsRisk: "+uv.getOmsRisk());
                        if(VERBOSE)Log.d(TAG,"onUVData UvIndex: "+uv.getUvIndex());
                        String uv_index = new DecimalFormat("##.##").format(uv.getUvIndex());
                        uv_value.setText(uv_index);
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

    public void configEverLoop(int value, int color){
        if(VERBOSE)Log.d(TAG,"configEverLoop: "+value+ ","+color);
        if(color==0)red=value/Config.LED_INTENSITY_DIVISOR;
        if(color==1)green=value/Config.LED_INTENSITY_DIVISOR;
        if(color==2)blue=value/Config.LED_INTENSITY_DIVISOR;
        EverloopImage.Builder image = EverloopImage.newBuilder();
        for(int i=0;i<35;i++){
            LedValue.Builder ledValue = LedValue.newBuilder();
            ledValue.setRed(red);
            ledValue.setGreen(green);
            ledValue.setBlue(blue);
            image.addLed(ledValue);
        }
        everloop.config(DriverConfig.newBuilder().setImage(image));
    }

    public void requestHumidityData(){
        humidity.push("");
    }

    public void requestUVData(){
        uv.push("");
    }

    public void configHumiditySensor(){
        DriverConfig.Builder config = humidity.getBasicConfig();
        HumidityParams.Builder params = HumidityParams.newBuilder();
        params.setCurrentTemperature(23.0f);
        params.setDoCalibration(true);
        config.setHumidity(params);
        humidity.config(config);
    }


    @Override
    void startDrivers() {
        humidity.start();
        uv.start();
        gpio.start();
        everloop.start();
        configHumiditySensor();
        uv.subscribe(onUVDataCallBack);
        gpio.subscribe(onGpioInputCallBack);
        humidity.subscribe(onHumidityDataCallBack);
    }

    @Override
    void stopDrivers() {
        mSlowTimer.cancel();
        mFastTimer.cancel();
        gpio.unsubscribe();
        humidity.unsubscribe();
        uv.unsubscribe();
        gpio.stop();
        humidity.stop();
        uv.stop();
        everloop.stop();
    }

    @Override
    void fastUpdateDevices() {
        requestGpioInputValue(1);
    }

    @Override
    void slowUpdateDevices() {
        requestHumidityData();
        requestUVData();
    }
}
