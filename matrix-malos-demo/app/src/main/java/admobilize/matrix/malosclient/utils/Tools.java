package admobilize.matrix.malosclient.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import admobilize.matrix.malosclient.Config;
import admobilize.matrix.malosclient.R;


/**
 * Created by Antonio Vanegas @hpsaturn on 11/12/16.
 */

public class Tools {
    private static final boolean DEBUG = Config.DEBUG;
    public static final String TAG = Tools.class.getSimpleName();


    public static void showToast(Context context, int i) {
        int duration = Toast.LENGTH_LONG;
        Toast.makeText(context, i, duration).show();
    }

    public static void showToast(Context context, CharSequence msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static String getAndroidDeviceId(Context ctx) {
        return Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static void hideKeyboard(Activity act) {
        act.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    public static void setButtonTintBackground(Context ctx, Button mButtonSMS, int tintColor){
        mButtonSMS.getBackground().setColorFilter(ContextCompat.getColor(ctx, tintColor), PorterDuff.Mode.MULTIPLY);
    }

//	VALIDATORS

    public static boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isDocumentValid(CharSequence name) {
        Pattern pattern = Pattern.compile("^[A-Za-z0-9]+");
        Matcher matcher = pattern.matcher(name);
        return matcher.matches();

    }

    public static boolean isNameValid(CharSequence name) {
        Pattern pattern = Pattern.compile("[^0-9()*^|\\.,:;\"&@$~+_-]+");
        Matcher matcher = pattern.matcher(name);
        return matcher.matches();

    }

    public static boolean isPhoneValid(CharSequence phone) {
        return android.util.Patterns.PHONE.matcher(phone).matches();
    }

    public static boolean isAddressValid(CharSequence address) {
        //TODO
        return true;
    }

    public static boolean isValidCarNumber(String carnum) {

        Pattern pattern = Pattern.compile("^([a-zA-Z]{2,3}\\d{3,4})$");
        Matcher matcher = pattern.matcher(carnum);
        return matcher.matches();

    }

    public static int getVersionCode(Context ctx) {
        try {
            return ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionCode;

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static String getVersionName(Context ctx) {
        try {
            return ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionName;

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }


    public static Bitmap getCircleBitmap(Bitmap bitmap) {
        Bitmap crop;
        if (bitmap.getWidth() >= bitmap.getHeight()) {

            crop = Bitmap.createBitmap(
                    bitmap,
                    bitmap.getWidth() / 2 - bitmap.getHeight() / 2,
                    0,
                    bitmap.getHeight(),
                    bitmap.getHeight()
            );

        } else {

            crop = Bitmap.createBitmap(
                    bitmap,
                    0,
                    bitmap.getHeight() / 2 - bitmap.getWidth() / 2,
                    bitmap.getWidth(),
                    bitmap.getWidth()
            );
        }

        final Bitmap output = Bitmap.createBitmap(crop.getWidth(), crop.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);

        final int color = Color.RED;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, crop.getWidth(), crop.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawOval(rectF, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(crop, rect, rect, paint);

        crop.recycle();
        bitmap.recycle();

        return output;

    }

    public static String getGoogleApiKey(Context ctx) {
        try {
            ApplicationInfo ai = ctx.getPackageManager().getApplicationInfo(ctx.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            String myAPIKey = bundle.getString("clientId");
            System.out.println("API KEY : " + myAPIKey);
            return myAPIKey;
        } catch (PackageManager.NameNotFoundException e) {
            if (DEBUG) Log.e(TAG, "Failed to load meta-data, NameNotFound: " + e.getMessage());
            return null;
        } catch (NullPointerException e) {
            if (DEBUG) Log.e(TAG, "Failed to load meta-data, NullPointer: " + e.getMessage());
            return null;
        }
    }
//
//    public static String getAndroidDeviceId(Context ctx){
//        return Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ANDROID_ID);
//    }

    public static void shareText(Context ctx, String text) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        sendIntent.setType("text/plain");
        ctx.startActivity(Intent.createChooser(sendIntent, ctx.getResources().getText(R.string.send_to)));
    }


    public static String getDateFormatTrack(Calendar cal) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE dd-MMM-yyyy hh:mm aa", Locale.getDefault());
        return sdf.format(cal.getTime());
    }


    public static void viewLink(Context ctx, String url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        ctx.startActivity(i);
    }

    public static Bitmap getContactBitmapFromURI(Context ctx, Uri uri) {
        Cursor cursor = ctx.getContentResolver().query(uri, new String[] {ContactsContract.Contacts.Photo.PHOTO}, null, null, null);
        if (cursor == null) {
            if (DEBUG) Log.e(TAG, "Failed getContact cursor");
            return null;
        }
        try {
            if (cursor.moveToFirst()) {
                byte[] data = cursor.getBlob(0);
                if (data != null) {
                    return BitmapFactory.decodeStream(new ByteArrayInputStream(data));
                }else
                if (DEBUG) Log.e(TAG, "Failed getContact data is null");
            } else{
                if (DEBUG) Log.e(TAG, "Failed getContact cursor index");
            }
        } finally {
            cursor.close();
        }

        if (DEBUG) Log.e(TAG, "Failed getContact bitmap");
        return null;

    }

}
