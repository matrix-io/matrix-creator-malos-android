package admobilize.matrix.malosclient;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.CompoundButton;
import com.crashlytics.android.Crashlytics;
import com.google.protobuf.InvalidProtocolBufferException;
import com.iamhabib.easy_preference.EasyPreference;

import io.fabric.sdk.android.Fabric;
import java.text.DecimalFormat;

import admobilize.matrix.malosclient.malos.MalosDevice;
import admobilize.matrix.malosclient.malos.MalosDrive;
import admobilize.matrix.malosclient.malos.MalosTarget;
import admobilize.matrix.malosclient.network.Discovery;
import admobilize.matrix.malosclient.ui.IPTargetInputFragment;
import admobilize.matrix.malosclient.ui.InfoFragment;
import admobilize.matrix.malosclient.utils.Storage;
import matrix_malos.Driver;
import matrix_malos.Driver.GpioParams.Builder;

import static admobilize.matrix.malosclient.malos.MalosDrive.OnSubscriptionCallBack;
import static android.widget.CompoundButton.OnCheckedChangeListener;
import static matrix_malos.Driver.DriverConfig;
import static matrix_malos.Driver.EverloopImage;
import static matrix_malos.Driver.GpioParams;
import static matrix_malos.Driver.Humidity;
import static matrix_malos.Driver.HumidityParams;
import static matrix_malos.Driver.LedValue;
import static matrix_malos.Driver.UV;

/**
 * Created by Antonio Vanegas @hpsaturn on 11/12/16.
 */

public class MainActivity extends BaseActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final boolean DEBUG = Config.DEBUG;
    private static final boolean VERBOSE = Config.VERBOSE;

    private MalosDevice currentDevice;
    private String deviceIp;
    private boolean onConfigDevice;

    private MalosDrive gpio;
    private MalosDrive humidity;
    private MalosDrive uv;
    private MalosDrive everloop;
    private MalosDrive imu;
    private MalosDrive pressure;

    private int red, green, blue;
    private Handler handler = new Handler();
    private MalosDrive deviceInfo;
    private Ringtone r;
    private boolean previousSetGpio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);
        initLoader();

        MalosDevice matrix = EasyPreference.with(this).getObject(Storage.CURRENT_DEVICE,MalosDevice.class);
        if(matrix==null){
            startDiscovery();
        }
        else {
            deviceInfo = new MalosDrive(MalosTarget.DEVICEINFO, matrix.getIpAddress());
            deviceInfo.request(onMatrixDetection);
        }

        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        r = RingtoneManager.getRingtone(getApplicationContext(), notification);
    }

    /******************************************************************
     * MALOS SENSORS CALLBACKS
     ******************************************************************/

    private OnCheckedChangeListener onCheckedGpioToggleButton = new OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                if(DEBUG)Log.i(TAG,"outputButton on");
                outputButton.setBackgroundDrawable(mOnBackground);
                configGpioOuputValue(15,1);
            } else {
                if(DEBUG)Log.i(TAG,"outputButton off");
                outputButton.setBackgroundDrawable(mOffBackground);
                configGpioOuputValue(15,0);
            }
        }
    };

    private OnSubscriptionCallBack onGpioInputCallBack = new OnSubscriptionCallBack() {
        @Override
        public void onReceiveData(String host, final byte[] data) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        GpioParams gpioParams = GpioParams.parseFrom(data);
                        if(VERBOSE)Log.d(TAG,"Gpio PINs vector: "+Integer.toBinaryString(gpioParams.getValues()));
                        int gpio_data = gpioParams.getValues();
                        int pin_output_mask = 0x1 << Config.GPIO_DEMO_OUTPUT;
                        int pin_input_mask  = 0x1 << Config.GPIO_DEMO_INPUT;

                        if(((gpio_data & pin_input_mask) >> Config.GPIO_DEMO_INPUT)==1){
                            inputButton.setImageDrawable(mOnImage);
                            previousSetGpio=true;
                            r.stop();
                        }
                        else {
                            inputButton.setImageDrawable(mOffImage);
                            if(previousSetGpio)r.play();
                        }
                        // output button state (for refresh multiple devices):
                        if(((gpio_data & pin_output_mask) >> Config.GPIO_DEMO_OUTPUT)==1)outputButton.setChecked(true);
                        else outputButton.setChecked(false);
                    } catch (InvalidProtocolBufferException e) {
                        e.printStackTrace();
                    }

                }
            });
        }
    };

    private OnSubscriptionCallBack onHumidityDataCallBack = new OnSubscriptionCallBack() {
        @Override
        public void onReceiveData(String host, final byte[] data) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if(onConfigDevice&&isTargetConfig()){ // Only for new device found
                            dismissLoader();
                            onConfigDevice =false;
                        }
                        Humidity humidity = Humidity.parseFrom(data);
                        if(VERBOSE)Log.d(TAG,"onHumidityDataCallBack humidity: "+humidity.getHumidity());
                        if(VERBOSE)Log.d(TAG,"onHumidityDataCallBack temperature: "+humidity.getTemperature());
                        humi_temp_value.setText(""+((int)(humidity.getTemperatureRaw()*10))/10.0f+"º");
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
        public void onReceiveData(String host, final byte[] data) {
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

    private OnSubscriptionCallBack onIMUDataCallBack = new OnSubscriptionCallBack() {
        @Override
        public void onReceiveData(String host, final byte[] data) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Driver.Imu imudata = imudata = Driver.Imu.parseFrom(data);
                        if(VERBOSE)Log.d(TAG,"Yaw => "+imudata.getYaw());
                        if(VERBOSE)Log.d(TAG,"Roll => "+imudata.getRoll());
                        if(VERBOSE)Log.d(TAG,"Pitch => "+imudata.getPitch());
                        joystickMoved((int)imudata.getPitch(),(int)imudata.getRoll());
                    } catch (InvalidProtocolBufferException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private OnSubscriptionCallBack onPressureDataCallBack = new OnSubscriptionCallBack() {
        @Override
        public void onReceiveData(String host, final byte[] data) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Driver.Pressure pressureData = null;
                    try {
                        pressureData = Driver.Pressure.parseFrom(data);
                        if(VERBOSE)Log.d(TAG,"onPressure Pressure: "+pressureData.getPressure());
                        if(VERBOSE)Log.d(TAG,"onPressure Altitude: "+pressureData.getAltitude());
                        if(VERBOSE)Log.d(TAG,"onPressure Temperature: "+pressureData.getTemperature());
                        String press_format = new DecimalFormat("##.##").format(pressureData.getPressure()/100);
                        String alti_format  = new DecimalFormat("##.#").format(pressureData.getAltitude());
                        String temp_format  = new DecimalFormat("##.##").format(pressureData.getTemperature());
                        press_value.setText(press_format);
                        press_alti_value.setText(alti_format+"m");
                        press_temp_value.setText(temp_format+"º");
                    } catch (InvalidProtocolBufferException e) {
                        e.printStackTrace();
                    }

                }
            });
        }
    };

    /******************************************************************
     * CONFIG MALOS SENSORS
     ******************************************************************/

    public void configGpioOuputValue(int pin, int value){
        Builder gpioParams = GpioParams.newBuilder();
        gpioParams.setPin(pin);
        gpioParams.setModeValue(GpioParams.EnumMode.OUTPUT_VALUE);
        gpioParams.setValue(value);
        gpio.config(DriverConfig.newBuilder().setGpio(gpioParams));
    }

    public void configGpioInputValue(int pin){
        DriverConfig.Builder config = gpio.getBasicConfig();
        Builder gpioParams = GpioParams.newBuilder();
        gpioParams.setPin(pin);
        gpioParams.setModeValue(GpioParams.EnumMode.INPUT_VALUE);
        config.setDelayBetweenUpdates(0.100f);
        gpio.config(config.setGpio(gpioParams));
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

    public void configHumiditySensor(){
        DriverConfig.Builder config = humidity.getBasicConfig();
        HumidityParams.Builder params = HumidityParams.newBuilder();
        params.setCurrentTemperature(23.0f);
        config.setHumidity(params);
        config.setDelayBetweenUpdates(0.500f);
        humidity.config(config);
    }

    public void configIMUSensor(){
        DriverConfig.Builder config = imu.getBasicConfig();
        config.setTimeoutAfterLastPing(7.0f);
        config.setDelayBetweenUpdates(0.100f);
        imu.config(config);
    }

    /***********************************************************
     * PING TO SENSORS
     ***********************************************************/

    @Override
    void pingDevices() {
        if(VERBOSE)Log.i(TAG,"pingDevices..");
        humidity.ping();
        uv.ping();
        imu.ping();
        gpio.ping();
        pressure.ping();
    }


    /***********************************************************
     * DISCOVERY METHODS
     ***********************************************************/

    private Discovery.OnDiscoveryMatrixDevice onDiscoveryMatrix = new Discovery.OnDiscoveryMatrixDevice() {
        @Override
        public void onFoundedMatrixDevice(final MalosDevice device) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(DEBUG)Log.i(TAG,"[ Matrix Creator Device Found!]");
                    setNewIpTarget(device);
                }
            });
        }
        @Override
        public void onDiscoveryError(String msgError) {
            // TODO: show snack or dialog
            if(DEBUG)Log.e(TAG,"onDiscoveryError: "+msgError);
        }
    };

    @Override
    public void startDiscovery() {
        if(DEBUG)Log.i(TAG,"startDiscovery..");
        showLoader(R.string.msg_find_device);
        new Discovery(this, onDiscoveryMatrix).searchDevices();
    }

    public void setNewIpTarget(final MalosDevice device) {
        if(isTargetConfig())stopDrivers();
        disableCurrentConfig();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dismissLoader();
                if (DEBUG) Log.i(TAG, "Matrix device detected!");
                String host=device.getIpAddress();
                EasyPreference.with(MainActivity.this).addObject(Storage.CURRENT_DEVICE,device).save();
                deviceIp=host;
                currentDevice=device;
                setTargetConfig(true);
                showLoader(R.string.msg_enable_sensors);
                initDevices();
                startDrivers();
            }
        }, 3000);
    }

    private MalosDrive.OnSubscriptionCallBack onMatrixDetection = new MalosDrive.OnSubscriptionCallBack() {
        @Override
        public void onReceiveData(final String host, final byte[] data) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MalosDevice matrixDevice = new MalosDevice(host, data);
                    deviceInfo.stop();
                    deviceIp=matrixDevice.getIpAddress();
                    currentDevice=matrixDevice;
                    setTargetConfig(true);
                    initDevices();
                }
            });
        }
    };

    private void initDevices() {
        if(DEBUG)Log.i(TAG,"initDevices..");
        gpio = new MalosDrive(MalosTarget.GPIO, deviceIp);
        humidity = new MalosDrive(MalosTarget.HUMIDITY, deviceIp);
        uv = new MalosDrive(MalosTarget.UV, deviceIp);
        everloop = new MalosDrive(MalosTarget.EVERLOOP, deviceIp);
        imu = new MalosDrive(MalosTarget.IMU, deviceIp);
        pressure = new MalosDrive(MalosTarget.PRESSURE, deviceIp);
        startDrivers();
        instanceUI();
        outputButton.setOnCheckedChangeListener(onCheckedGpioToggleButton);
    }

    @Override
    void startDrivers() {
        if(DEBUG)Log.i(TAG,"startDrivers..");

        configHumiditySensor();
        configIMUSensor();
        configGpioInputValue(1);

        uv.subscribe(onUVDataCallBack);
        gpio.subscribe(onGpioInputCallBack);
        humidity.subscribe(onHumidityDataCallBack);
        imu.subscribe(onIMUDataCallBack);
        pressure.subscribe(onPressureDataCallBack);

        startPingTimer();
    }

    @Override
    public void stopDrivers() {
        if(DEBUG)Log.i(TAG,"stopDrivers..");
        stopPingTimer();
        gpio.stop();
        humidity.stop();
        uv.stop();
        everloop.stop();
        imu.stop();
    }

    public void disableCurrentConfig(){
        deviceIp="";
        setTargetConfig(false);
        onConfigDevice=true;
    }



    /***********************************************************
     * FRAGMENTS METHODS
     ***********************************************************/

    @Override
    void showDeviceInfo() {
        if(isTargetConfig()) {
            InfoFragment infoFragment = InfoFragment.newInstance(currentDevice.getDeviceInfo());
            showDialog(infoFragment,InfoFragment.TAG);
        }
    }

    @Override
    void showIPInputFragment() {
        String ipAndroid = Discovery.getWifiIpAddress(this);
        IPTargetInputFragment ipInputFragment = IPTargetInputFragment.newInstance(ipAndroid);
        showDialog(ipInputFragment,IPTargetInputFragment.TAG);
    }

}
