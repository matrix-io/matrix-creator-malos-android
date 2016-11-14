package admobilize.matrix.malosclient.malos;

import android.util.Log;

import com.google.protobuf.InvalidProtocolBufferException;

import java.util.Iterator;
import java.util.List;

import admobilize.matrix.malosclient.Config;
import matrix_malos.Driver;

/**
 * Created by Antonio Vanegas @hpsaturn on 11/13/16.
 */

public class MalosDevice {

    private static final String TAG = MalosDevice.class.getSimpleName();
    private static final boolean DEBUG = Config.DEBUG;
    private static final boolean VERBOSE = Config.VERBOSE;

    private String ipAddress;

    private String deviceInfo;

    public MalosDevice(String ipAddress, byte[] data) {
            this.ipAddress = ipAddress;
            this.deviceInfo = generateDeviceInfo(data);
    }

    private String generateDeviceInfo(byte[] data){
        String info="\n[ipAddress] "+ipAddress;
        try {
            Driver.MalosDriverInfo driverInfo = Driver.MalosDriverInfo.parseFrom(data);
            info=info+"\n[Features]";
            List<Driver.DriverInfo> features = driverInfo.getInfoList();
            Iterator<Driver.DriverInfo> it = features.iterator();
            while (it.hasNext()) {
                Driver.DriverInfo driver = it.next();
                String feature="==> matrix feature: [" + driver.getBasePort() + "] " + driver.getDriverName();
                if(DEBUG) Log.d(TAG,feature);
                info=info+"\n"+feature;
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        return info;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public String getIpAddress() {
        return ipAddress;
    }

}
