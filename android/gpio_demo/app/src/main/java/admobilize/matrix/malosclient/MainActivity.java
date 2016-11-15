package admobilize.matrix.malosclient;

import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import com.google.protobuf.InvalidProtocolBufferException;
import com.iamhabib.easy_preference.EasyPreference;

import java.text.DecimalFormat;

import admobilize.matrix.malosclient.malos.MalosDevice;
import admobilize.matrix.malosclient.malos.MalosDrive;
import admobilize.matrix.malosclient.malos.MalosTarget;
import admobilize.matrix.malosclient.network.Discovery;
import admobilize.matrix.malosclient.ui.InfoFragment;
import admobilize.matrix.malosclient.utils.Storage;
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

    private int red, green, blue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initLoader();

        MalosDevice matrix = EasyPreference.with(this).getObject(Storage.CURRENT_DEVICE,MalosDevice.class);
        if(matrix==null){
            startDiscovery();
        }
        else {
            deviceIp=matrix.getIpAddress();
            currentDevice=matrix;
            setTargetConfig(true);
            initDevices();
        }

    }

    @Override
    public void startDiscovery() {
        if(DEBUG)Log.i(TAG,"startDiscovery..");
        showLoader(R.string.msg_find_device);
        if(isTargetConfig())stopDrivers();
        deviceIp="";
        setTargetConfig(false);
        onConfigDevice=true;
        new Discovery(this, onDiscoveryMatrix).searchDevices();
    }

    private Discovery.OnDiscoveryMatrixDevice onDiscoveryMatrix = new Discovery.OnDiscoveryMatrixDevice() {
        @Override
        public void onFoundedMatrixDevice(final MalosDevice device) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dismissLoader();
                    if(DEBUG)Log.i(TAG,"[ Matrix Creator Device Found!]");
                    String host=device.getIpAddress();
                    EasyPreference.with(MainActivity.this).addObject(Storage.CURRENT_DEVICE,device).save();
                    deviceIp=host;
                    currentDevice=device;
                    setTargetConfig(true);
                    showLoader(R.string.msg_enable_sensors);
                    initDevices();
                    startDrivers();
                }
            });

        }

        @Override
        public void onDiscoveryError(String msgError) {
            // TODO: show snack or dialog
        }
    };


    private void initDevices() {
        gpio = new MalosDrive(MalosTarget.GPIO, deviceIp);
        humidity = new MalosDrive(MalosTarget.HUMIDITY, deviceIp);
        uv = new MalosDrive(MalosTarget.UV, deviceIp);
        everloop = new MalosDrive(MalosTarget.EVERLOOP, deviceIp);

        instanceUI();
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
                        if(onConfigDevice&&isTargetConfig()){ // Only for new device found
                            dismissLoader();
                            onConfigDevice =false;
                        }
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
        initTimers();
    }

    @Override
    void stopDrivers() {
        stopTimers();
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

    @Override
    void showDeviceInfo() {
        if(isTargetConfig()) {
            InfoFragment infoFragment = InfoFragment.newInstance(currentDevice.getDeviceInfo());
            showDialog(infoFragment,InfoFragment.TAG);
        }
    }


}
