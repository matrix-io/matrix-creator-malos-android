package admobilize.matrix.malosclient;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import org.zeromq.ZMQ;

import matrix_malos.Driver.GpioParams.Builder;

import static matrix_malos.Driver.*;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private ZMQ.Context config_context;
    private ZMQ.Socket config_socket;
    private ZMQ.Socket sub_socket;
    private Drawable mOffBackground;
    private Drawable mOnBackground;

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

//        mOffBackground = getDrawable(R.drawable.toggle_button_off_holo_dark);
//		mOnBackground = getDrawable(R.drawable.toggle_button_on_holo_dark);
    }

    public void sendOuputValue(int pin,int value){
        Builder gpioParams = GpioParams.newBuilder();
        gpioParams.setPin(pin);
        gpioParams.setModeValue(GpioParams.EnumMode.OUTPUT_VALUE);
        gpioParams.setValue(value);
        new ZeroMQSend(DriverConfig.newBuilder().setGpio(gpioParams)).execute();
    }

    public class ZeroMQConnect extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void...voids) {
            config_context = ZMQ.context(1);
            config_socket = config_context.socket(ZMQ.PUSH);
            config_socket.connect(Config.MALOS_GPIO_CONFIG);
            return null;
        }
    }

    public class ZeroMQServer implements Runnable {
//        private final Handler uiThreadHandler;
//
//        public ZeroMQServer(Handler uiThreadHandler) {
//            this.uiThreadHandler = uiThreadHandler;
//        }

        @Override
        public void run() {

            ZMQ.Context context = ZMQ.context(1);
            sub_socket = context.socket(ZMQ.SUB);
            sub_socket.connect(Config.MALOS_GPIO_SUB);
            sub_socket.subscribe("".getBytes());

            while(!Thread.currentThread().isInterrupted()) {

                // Read envelope with address
                String address = sub_socket.recvStr ();
                // Read message contents
                String contents = sub_socket.recvStr ();
                byte[] msg = sub_socket.recv(0);
                Log.i(TAG,"ZeroMQSubscription: "+address + " : " + contents +" "+msg);
            }
            sub_socket.close();
            context.close();
        }
    }


    public class ZeroMQSend extends AsyncTask<Void, Void, Void>{
        private DriverConfig.Builder config;

        public ZeroMQSend(DriverConfig.Builder config) {
            this.config=config;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            config_socket.send(config.build().toByteArray());
            Log.d(TAG,"ZeroMQMessageTask doInBackground result");
            return null;
        }
    }

    @Override
    protected void onResume() {
        new ZeroMQConnect().execute();
         new Thread(new ZeroMQServer()).start();
        super.onResume();
    }

    @Override
    protected void onStop() {
        config_socket.close();
        sub_socket.close();
        config_context.term();
        super.onStop();
    }
}
