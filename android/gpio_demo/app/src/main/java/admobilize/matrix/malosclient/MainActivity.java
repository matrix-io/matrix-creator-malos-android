package admobilize.matrix.malosclient;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.google.protobuf.InvalidProtocolBufferException;
import matrix_malos.Driver.GpioParams.Builder;

import static admobilize.matrix.malosclient.MalosDevice.*;
import static android.widget.CompoundButton.*;
import static matrix_malos.Driver.*;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final boolean DEBUG = Config.DEBUG;
    private MalosDevice gpio;
    private MalosDevice humidity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gpio = new MalosDevice(MalosTarget.GPIO);
        humidity = new MalosDevice(MalosTarget.HUMIDITY);

        ToggleButton toggle = (ToggleButton) findViewById(R.id.toggleButton);
        toggle.setOnCheckedChangeListener(onCheckedGpioToggleButton);

//      mOffBackground = getDrawable(R.drawable.toggle_button_off_holo_dark);
//		mOnBackground = getDrawable(R.drawable.toggle_button_on_holo_dark);
    }

    private OnCheckedChangeListener onCheckedGpioToggleButton = new OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                if(DEBUG)Log.i(TAG,"toggle on");
                configGpioOuputValue(0,1);
            } else {
                if(DEBUG)Log.i(TAG,"toggle off");
                configGpioOuputValue(0,0);
            }
        }
    };


    private OnSubscriptionCallBack onGpioInputCallBack = new OnSubscriptionCallBack() {
        @Override
        public void onReceiveData(byte[] data) {
            try {
                GpioParams gpioParams = GpioParams.parseFrom(data);
                if(DEBUG)Log.d(TAG,"onGpioInputCallBack receive value: "+gpioParams.getValue());
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }
    };

    private OnSubscriptionCallBack onHumidityDataCallBack = new OnSubscriptionCallBack() {
        @Override
        public void onReceiveData(byte[] data) {

            try {
                Humidity humidity = Humidity.parseFrom(data);
                if(DEBUG)Log.d(TAG,"onHumidityDataCallBack value: "+humidity.getHumidity());
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
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

    @Override
    protected void onResume() {
        gpio.connect();
        gpio.subscription(onGpioInputCallBack);
        humidity.subscription(onHumidityDataCallBack);
        super.onResume();
    }

    @Override
    protected void onStop() {
        gpio.disconnect();
        super.onStop();
    }
}
