package admobilize.matrix.malosclient.ui;

import android.os.Bundle;
import android.os.HandlerThread;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import admobilize.matrix.malosclient.Config;
import admobilize.matrix.malosclient.MainActivity;
import admobilize.matrix.malosclient.R;
import admobilize.matrix.malosclient.malos.MalosDevice;
import admobilize.matrix.malosclient.malos.MalosDrive;
import admobilize.matrix.malosclient.malos.MalosTarget;


/**
 * Created by Antonio Vanegas @hpsaturn on 11/13/16.
 */

public class IPTargetInputFragment extends DialogFragment {

    private static final boolean DEBUG = Config.DEBUG;
    public static final String TAG = IPTargetInputFragment.class.getSimpleName();
    private EditText inputIp;
    private MalosDrive drive;

    public static IPTargetInputFragment newInstance(String ipAndroid) {
        IPTargetInputFragment fragment = new IPTargetInputFragment();
        Bundle args = new Bundle();
        args.putString(TAG, ipAndroid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int theme = R.style.BaseTheme_Dialog;
        int style = DialogFragment.STYLE_NORMAL;
        setStyle(style, theme);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_ipinput, container, false);

        inputIp = (EditText) v.findViewById(R.id.ed_ipinput_input);
        Button btSave = (Button) v.findViewById(R.id.bt_ipinput_confirm);
        Bundle args = getArguments();
        String ipAndroid = args.getString(TAG, "");
        inputIp.setText(ipAndroid);

        btSave.setOnClickListener(onButtonSave);

        return v;

    }


    private View.OnClickListener onButtonSave = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            final String ip = inputIp.getText().toString();
            if (ip.length() > 0) {
                if (DEBUG) Log.i(TAG, "finding device..");
                getMain().showLoader(R.string.msg_find_device);
                drive = new MalosDrive(MalosTarget.DEVICEINFO, ip);
                drive.request(onMatrixDetection);
            }
        }
    };

    private MalosDrive.OnSubscriptionCallBack onMatrixDetection = new MalosDrive.OnSubscriptionCallBack() {
        @Override
        public void onReceiveData(final String host, final byte[] data) {
            MalosDevice matrixDevice = new MalosDevice(host, data);
            getMain().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    drive.stop();
                }
            });
            getMain().setNewIpTarget(matrixDevice);
            dismiss();
        }
    };

    private MainActivity getMain() {
        return ((MainActivity) getActivity());
    }

}
