package admobilize.matrix.malosclient.network;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;

import org.zeromq.ZMQ;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.nio.ByteOrder;

import admobilize.matrix.malosclient.Config;
import admobilize.matrix.malosclient.R;
import admobilize.matrix.malosclient.malos.MalosDevice;
import admobilize.matrix.malosclient.malos.MalosTarget;

import static android.content.Context.WIFI_SERVICE;

/**
 * Created by Antonio Vanegas @hpsaturn on 11/12/16.
 */

public class Discovery {

    private static final String TAG = Discovery.class.getSimpleName();
    private static final boolean DEBUG = Config.DEBUG;
    private static final boolean VERBOSE = Config.VERBOSE;

    private final OnDiscoveryMatrixDevice callback;

    private Context ctx;

    public Discovery(Context ctx, OnDiscoveryMatrixDevice cb) {
        this.ctx = ctx;
        this.callback=cb;
    }

    public void searchDevices(){
        new FindDevices().execute();
    }

    public interface OnDiscoveryMatrixDevice{
        void onFoundedMatrixDevice(MalosDevice device);
        void onDiscoveryError(String msgError);
    }

    private class FindDevices extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                String ipWifi= getWifiIpAddress(ctx);
                if(ipWifi!=null) {
                    if(DEBUG)Log.i(TAG,"current android ip is: "+ipWifi);
                    InetAddress inetAddress = InetAddress.getByName(ipWifi);
                    NetworkInterface iFace = NetworkInterface.getByInetAddress(inetAddress);

                    for (int i = 0; i <= 255; i++) {
                        // build the next IP address
                        ipWifi = ipWifi.substring(0, ipWifi.lastIndexOf('.') + 1) + i;
                        InetAddress pingAddr = InetAddress.getByName(ipWifi);

                        // 50ms Timeout for the "ping"
                        if (pingAddr.isReachable(iFace, 200, 50)) {
                            if (VERBOSE) Log.d(TAG, "found host: "+pingAddr.getHostAddress());
                            // request MALOS Device Info for possible compatible host
                            MalosTarget target = new MalosTarget(MalosTarget.DEVICEINFO, pingAddr.getHostAddress());
                            new Thread(new ZeroMQRequest(target)).start();
                        }
                    }
                }else{
                    if(DEBUG)Log.e(TAG,"Android not have connection!");
                }
            } catch (UnknownHostException ex) {
            } catch (IOException ex) {
            }

            return null;
        }
    }

    private class ZeroMQRequest implements Runnable {

        private final MalosTarget target;

        public ZeroMQRequest(MalosTarget target) {
            this.target =target;
        }

        @Override
        public void run() {
            ZMQ.Context sub_context = ZMQ.context(1);
            ZMQ.Socket sub_socket = sub_context.socket(ZMQ.REQ);
            sub_socket.connect(target.getBaseport());
            sub_socket.setLinger(0);
            sub_socket.send("".getBytes());
            if(DEBUG)Log.d(TAG,"try get config from: "+ target.getBaseport());

            while(!Thread.currentThread().isInterrupted()) {
                try {
                    if(sub_socket!=null){
                        callback.onFoundedMatrixDevice(new MalosDevice(target.getHost(),sub_socket.recv()));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    sub_socket=null;
                }
            }
            sub_socket.close();
            sub_context.close();
        }
    }

    private String getWifiIpAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();

        // Convert little-endian to big-endianif needed
        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ipAddress = Integer.reverseBytes(ipAddress);
        }

        byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();

        String ipAddressString;
        try {
            ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();
        } catch (UnknownHostException ex) {
            if(DEBUG)Log.e("WIFIIP", "Unable to get host address.");
            ipAddressString = null;
            callback.onDiscoveryError(ctx.getString(R.string.error_wifi_lan_ip));
        }

        return ipAddressString;
    }

}
