package admobilize.matrix.malosclient.ui;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import admobilize.matrix.malosclient.Config;
import admobilize.matrix.malosclient.R;


/**
 * Created by Antonio Vanegas @hpsaturn on 11/13/16.
 */

public class InfoFragment extends DialogFragment {

    private static final boolean DEBUG = Config.DEBUG;
    public static final String TAG = InfoFragment.class.getSimpleName();

    public static InfoFragment newInstance(String info){
        InfoFragment fragment = new InfoFragment();
        Bundle args = new Bundle();
        args.putString(TAG,info);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int theme= R.style.BaseTheme_Dialog;
        int style = DialogFragment.STYLE_NORMAL;
        setStyle(style, theme);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dialog_info, container, false);

        TextView tvInfo = (TextView) v.findViewById(R.id.tv_infofragment_info);

        Bundle args = getArguments();
        String info = args.getString(TAG, "");
        tvInfo.setText(info);

        return v;

    }

}
