package admobilize.matrix.malosclient.malos;

import android.os.AsyncTask;
import android.util.Log;


import org.zeromq.ZMQ;

import admobilize.matrix.malosclient.Config;
import one.matrixio.proto.malos.v1.DriverConfig;

/**
 * Created by Antonio Vanegas @hpsaturn on 11/10/16.
 */

public class MalosDrive {

    private static final String TAG = MalosDrive.class.getSimpleName();
    private static final boolean DEBUG = Config.DEBUG;
    private static final boolean VERBOSE = Config.VERBOSE;

    private final MalosTarget target;
    private ZMQ.Context config_context;
    private ZMQ.Socket config_socket;
    private ZMQ.Socket sub_socket;
    private ZMQ.Context push_context;
    private ZMQ.Socket push_socket;
    private ZMQ.Socket req_socket;


    public interface OnSubscriptionCallBack {
        void onReceiveData(String host, byte[] data);
    }

    public MalosDrive(int malosTarget, String host) {
        this.target =new MalosTarget(malosTarget,host);
    }

    public void config (DriverConfig.Builder config){
        new ZeroMQConfig(config).execute();
    }

    public void subscribe(OnSubscriptionCallBack cb){
        new Thread(new ZeroMQSubscription(cb)).start();
    }

    public void request (OnSubscriptionCallBack cb) {
        new Thread(new ZeroMQRequest(cb)).start();
    }

    public void push (String data){
        new ZeroMQPush().execute(data);
    }

    public void stop() {
        new ZeroMQstop().execute();
    }

    public void ping() {
        push("");
    }

    public DriverConfig.Builder getBasicConfig() {
        DriverConfig.Builder driverConfig = DriverConfig.newBuilder();
        driverConfig.setDelayBetweenUpdates(2.0f);
        driverConfig.setTimeoutAfterLastPing(6.0f);
        return driverConfig;
    }

    private class ZeroMQConfig extends AsyncTask<Void, Void, Void>{
        private DriverConfig.Builder config;

        public ZeroMQConfig(DriverConfig.Builder config) {
            this.config=config;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                if(config_context==null||config_socket==null){
                    config_context = ZMQ.context(1);
                    config_socket = config_context.socket(ZMQ.PUSH);
                    config_socket.connect(target.getBaseport());
                }
                if(VERBOSE)Log.i(TAG,"sending configuration to "+target.getBaseport());
                config_socket.send(config.build().toByteArray());

            } catch (Exception e) {
                if(DEBUG)Log.e(TAG,"ZeroMQConfig crash: "+e.getMessage());
                e.printStackTrace();
            }
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
            try {
                ZMQ.Context sub_context = ZMQ.context(1);
                sub_socket = sub_context.socket(ZMQ.SUB);
                sub_socket.connect(target.getSubPort());
                sub_socket.subscribe("".getBytes());
                if(DEBUG)Log.i(TAG,"subscribe with: "+ target.getSubPort());

                while(!Thread.currentThread().isInterrupted()) {
                    try {
                        if(sub_socket!=null){
                            cb.onReceiveData(target.getHost(),sub_socket.recv());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        sub_socket = null;
                    }
                }
                sub_socket.close();
                sub_context.term();
            } catch (Exception e) {
                if(DEBUG)Log.e(TAG,"ZeroMQSubscription crash: "+e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private class ZeroMQRequest implements Runnable {

        private final OnSubscriptionCallBack cb;

        public ZeroMQRequest(OnSubscriptionCallBack cb) {
            this.cb=cb;
        }

        @Override
        public void run() {
            try {
                ZMQ.Context req_context = ZMQ.context(1);
                req_socket = req_context.socket(ZMQ.REQ);
                req_socket.setLinger(0);
                req_socket.setSendTimeOut(0);
                req_socket.connect(target.getBaseport());
                req_socket.send("".getBytes());
                if(DEBUG)Log.d(TAG,"try get config from: "+ target.getBaseport());

                while(!Thread.currentThread().isInterrupted()) {
                    try {
                        if(req_socket!=null){
                            cb.onReceiveData(target.getHost(),req_socket.recv());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        req_socket=null;
                    }
                }
                req_socket.close();
                req_context.term();
            } catch (Exception e) {
                if(DEBUG)Log.e(TAG,"ZeroMQRequest crash: "+e.getMessage());
                e.printStackTrace();
            }
        }
    }


    private class ZeroMQPush extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String...data) {
            try {
                if(push_context==null||push_socket==null) {
                    if(VERBOSE)Log.i(TAG,"creating push connection..");
                    push_context = ZMQ.context(1);
                    push_socket = push_context.socket(ZMQ.PUSH);
                    push_socket.connect(target.getPushPort());
                }
                if(VERBOSE)Log.i(TAG,"push data..");
                if(push_context!=null&&push_socket!=null)push_socket.send(data[0]);
            } catch (Exception e) {
                if(DEBUG)Log.e(TAG,"ZeroMQPush crash: "+e.getMessage());
                e.printStackTrace();
            }
            return null;
        }
    }

    private class ZeroMQstop extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void...voids) {
            try {
                if(DEBUG)Log.d(TAG,"stopping "+ target.getBaseport());
                if(config_socket!=null){
                    config_socket.close();
                    config_socket=null;
                }
                if(config_context!=null){
                    config_context.term();
                    config_context=null;
                }
                if(push_socket!=null){
                    push_socket.close();
                    push_socket=null;
                }
                if(push_context!=null){
                    push_context.term();
                    push_context=null;
                }
                if(DEBUG)Log.d(TAG,"unsubscribe "+ target.getSubPort());
                if(sub_socket!=null){
                    sub_socket.close();
                    sub_socket=null;
                }
                if(req_socket!=null){
                    req_socket.close();
                    req_socket=null;
                }
            } catch (Exception e) {
                if(DEBUG)Log.e(TAG,"ZeroMQstop crash: "+e.getMessage());
                e.printStackTrace();
            }
            return null;
        }
    }



}
