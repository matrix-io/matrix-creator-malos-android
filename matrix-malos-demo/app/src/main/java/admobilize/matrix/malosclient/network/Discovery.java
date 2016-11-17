package admobilize.matrix.malosclient.network;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import admobilize.matrix.malosclient.Config;
import admobilize.matrix.malosclient.R;
import admobilize.matrix.malosclient.malos.MalosDevice;
import admobilize.matrix.malosclient.malos.MalosDrive;
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
    private List<MalosDrive> driverRegister = new ArrayList<>();
    final Handler handler = new Handler();
    private boolean founded;

    public interface OnDiscoveryMatrixDevice{
        void onFoundedMatrixDevice(MalosDevice device);
        void onDiscoveryError(String msgError);
    }

    public Discovery(Context ctx, OnDiscoveryMatrixDevice cb) {
        this.ctx = ctx;
        this.callback=cb;
    }

    private MalosDrive.OnSubscriptionCallBack onMatrixDetection = new MalosDrive.OnSubscriptionCallBack() {
        @Override
        public void onReceiveData(String host, byte[] data) {
            if(!founded){
                founded=true;
                callback.onFoundedMatrixDevice(new MalosDevice(host, data));
            }
        }
    };

    public void searchDevices(){
        new FindDevices().execute();
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
                            if (VERBOSE) Log.d(TAG, "found possible host: "+pingAddr.getHostAddress());
                            // request MALOS Device Info for possible compatible host
                            MalosDrive drive = new MalosDrive(MalosTarget.DEVICEINFO,pingAddr.getHostAddress());
                            drive.request(onMatrixDetection);
                            driverRegister.add(drive);
                        }
                    }
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (DEBUG) Log.i(TAG, "stopping all detection threads..");
                            Iterator<MalosDrive> it = driverRegister.iterator();
                            while(it.hasNext())it.next().unsubscribe();
                        }
                    }, 3000);
                }else{
                    if(DEBUG)Log.e(TAG,"Android not have connection!");
                    callback.onDiscoveryError(ctx.getString(R.string.error_wifi_lan_ip));
                }
            } catch (UnknownHostException ex) {
                if(DEBUG)Log.e(TAG,"UnknownHostException: "+ex.getMessage());
            } catch (IOException ex) {
                if(DEBUG)Log.e(TAG,"IOException: "+ex.getMessage());
            }
            return null;
        }

    }

    public static String getWifiIpAddress(Context context) {
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
        }
        return ipAddressString;
    }

}
