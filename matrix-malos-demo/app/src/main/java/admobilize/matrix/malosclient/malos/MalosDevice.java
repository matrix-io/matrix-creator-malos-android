package admobilize.matrix.malosclient.malos;

import android.util.Log;

import com.google.protobuf.InvalidProtocolBufferException;

import java.util.Iterator;
import java.util.List;

import admobilize.matrix.malosclient.Config;
import one.matrixio.proto.malos.v1.DriverInfo;
import one.matrixio.proto.malos.v1.MalosDriverInfo;

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
            MalosDriverInfo driverInfo = MalosDriverInfo.parseFrom(data);
            info=info+"\n\n=== Features: ===\n";
            info=info+"\n[Port]\t[Delay]\t[Ping]\t[Timeout]\t[DriverName]\n";
            List<DriverInfo> features = driverInfo.getInfoList();
            Iterator<DriverInfo> it = features.iterator();
            while (it.hasNext()) {
                DriverInfo driver = it.next();
                String feature="" +
                        "["+driver.getBasePort()+"]\t"+
                        "["+driver.getDelayBetweenUpdates()+"]\t"+
                        "["+driver.getNeedsPings()+"]\t"+
                        "["+driver.getTimeoutAfterLastPing()+"]\t"+
                        driver.getDriverName();
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
