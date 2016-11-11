package admobilize.matrix.malosclient;

import android.os.AsyncTask;
import android.util.Log;

import org.zeromq.ZMQ;

import matrix_malos.Driver;

import static matrix_malos.Driver.*;

/**
 * Created by Antonio Vanegas @hpsaturn on 11/10/16.
 */

public class MalosDevice {

    private static final String TAG = MalosDevice.class.getSimpleName();
    private static final boolean DEBUG = Config.DEBUG;

    private final MalosTarget driver;
    private ZMQ.Context config_context;
    private ZMQ.Socket config_socket;
    private ZMQ.Socket sub_socket;

    public interface OnSubscriptionCallBack {
        void onReceiveData(byte[] data);
    }

    public MalosDevice(int malosTarget) {
        this.driver=new MalosTarget(malosTarget);
    }

    public void connect (){
        new ZeroMQConnect().execute(driver.getBaseport());
    }

    public void config (DriverConfig.Builder config){
        new ZeroMQConfig(config).execute();
    }

    public void subscription (OnSubscriptionCallBack cb){
         new Thread(new ZeroMQSubscription(cb)).start();
//        new ZeroMQSub(cb).execute();
    }

    public void push (String data){
        new ZeroMQPush().execute(data);
    }

    public void disconnect() {
        new ZeroMQDisonnect().execute();
    }

    // TODO: unify Connect and Config ??
    private class ZeroMQConnect extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String...port) {
            config_context = ZMQ.context(1);
            config_socket = config_context.socket(ZMQ.PUSH);
            config_socket.connect(port[0]);
            if(DEBUG)Log.d(TAG,"connecting with: "+port[0]);
            return null;
        }
    }

    private class ZeroMQConfig extends AsyncTask<Void, Void, Void>{
        private DriverConfig.Builder config;

        public ZeroMQConfig(DriverConfig.Builder config) {
            this.config=config;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            config_socket.send(config.build().toByteArray());
            return null;
        }
    }

    private class ZeroMQSubscription implements Runnable {

        private final OnSubscriptionCallBack cb;

        public ZeroMQSubscription(OnSubscriptionCallBack cb) {
            this.cb=cb;
        }

        @Override
        public void run() {
            ZMQ.Context sub_context = ZMQ.context(1);
            sub_socket = sub_context.socket(ZMQ.SUB);
            sub_socket.connect(driver.getSubPort());
            sub_socket.subscribe("".getBytes());

            while(!Thread.currentThread().isInterrupted()) {
                try {
                    cb.onReceiveData(sub_socket.recv());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            sub_socket.close();
            sub_context.close();
        }
    }

    private class ZeroMQDisonnect extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void...voids) {
            if(DEBUG)Log.d(TAG,"disconnecting..");
            config_socket.close();
            config_context.term();
            if(sub_socket!=null)sub_socket.close();
            return null;
        }
    }


    private class ZeroMQPush extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String...data) {
            ZMQ.Context push_context = ZMQ.context(1);
            ZMQ.Socket push_socket = push_context.socket(ZMQ.PUSH);
            push_socket.connect(driver.getPushPort());
            push_socket.send(data[0]);
            push_socket.close();
            push_context.term();
            return null;
        }
    }


}
