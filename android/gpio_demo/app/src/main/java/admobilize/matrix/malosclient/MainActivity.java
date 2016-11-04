package admobilize.matrix.malosclient;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import org.zeromq.ZMQ;
import matrix_malos.Driver;
import matrix_malos.Driver.GpioParams.Builder;

import static matrix_malos.Driver.*;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private ZMQ.Context zmqcontext;
    private ZMQ.Socket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ToggleButton toggle = (ToggleButton) findViewById(R.id.toggleButton);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.i(TAG,"toggle on");
                    sendOuputValue(0,1);
                } else {
                    Log.i(TAG,"toggle off");
                    sendOuputValue(0,0);
                }
            }
        });
    }

    public void sendOuputValue(int pin,int value){
        Builder gpioParams = GpioParams.newBuilder();
        gpioParams.setPin(pin);
        gpioParams.setModeValue(GpioParams.EnumMode.OUTPUT_VALUE);
        gpioParams.setValue(value);
        new ZeroMQSend(DriverConfig.newBuilder().setGpio(gpioParams)).execute();
    }

    public class ZeroMQContext extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void...voids) {
            zmqcontext = ZMQ.context(1);
            socket = zmqcontext.socket(ZMQ.PUSH);
            socket.connect("tcp://10.0.0.167:20049");
            return null;
        }
    }

    public class ZeroMQSend extends AsyncTask<Void, Void, Void>{
        private DriverConfig.Builder config;

        public ZeroMQSend(DriverConfig.Builder config) {
            this.config=config;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            socket.send(config.build().toByteArray());
            Log.d(TAG,"ZeroMQMessageTask doInBackground result");
            return null;
        }
    }

    @Override
    protected void onResume() {
        new ZeroMQContext().execute();
        super.onResume();
    }

    @Override
    protected void onStop() {
        socket.close();
        zmqcontext.term();
        super.onStop();
    }
}
