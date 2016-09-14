package twosun.sec.com.memoprime;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.pen.Spen;
import com.samsung.android.sdk.pen.SpenSettingPenInfo;
import com.samsung.android.sdk.pen.document.SpenNoteDoc;
import com.samsung.android.sdk.pen.document.SpenPageDoc;
import com.samsung.android.sdk.pen.engine.SpenSurfaceView;
import com.samsung.android.sdk.pen.settingui.SpenSettingPenLayout;

import java.io.IOException;

public class ScreenOffMemo extends AppCompatActivity {

    private View decorView;
    private int uiOption;

    private Context mContext;
    private SpenNoteDoc mSpenNoteDoc;
    private SpenPageDoc mSpenPageDoc;
    private SpenSurfaceView mSpenSurfaceView;

    private ImageButton penBtn;
    private ImageButton eraseBtn;
    private ImageButton excuteBtn;
    private ImageButton pinBtn;
    private Button cancelBtn;
    private Button saveBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Full Screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_screen_off_memo);

        // Hiding actionbar
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        penBtn = (ImageButton) findViewById(R.id.penButton);
        eraseBtn = (ImageButton) findViewById(R.id.eraseButton);
        excuteBtn = (ImageButton) findViewById(R.id.excuteButton);
        pinBtn = (ImageButton) findViewById(R.id.pinButton);
        cancelBtn = (Button) findViewById(R.id.cancelButton);
        saveBtn = (Button) findViewById(R.id.saveButton);

        mContext = this;

        boolean isSpenFeatureEnabled = false;
        Spen spenPackage = new Spen();

        try {
            spenPackage.initialize(this);
            isSpenFeatureEnabled = spenPackage.isFeatureEnabled(Spen.DEVICE_PEN);
        } catch (SsdkUnsupportedException e) {
            if (processUnsupportedException(e) == true) {
                return;
            }
        } catch (Exception e1) {
            Toast.makeText(mContext, "Cannot initialize Pen.", Toast.LENGTH_SHORT).show();
            e1.printStackTrace();
            finish();
        }
        //Create Spen View
        RelativeLayout spenViewLayout = (RelativeLayout) findViewById(R.id.spenViewLayout);
        mSpenSurfaceView = new SpenSurfaceView(mContext);
        Toast.makeText(mContext, "Create new SpenView.", Toast.LENGTH_SHORT).show();
        if (mSpenSurfaceView == null) {
            Toast.makeText(mContext, "Cannot create new SpenView.", Toast.LENGTH_SHORT).show();
            finish();
        }
        spenViewLayout.addView(mSpenSurfaceView);
//        Get the dimensions of the screen.
        Display display = getWindowManager().getDefaultDisplay();
        Rect rect = new Rect();
        display.getRectSize(rect);

        try {
            mSpenNoteDoc = new SpenNoteDoc(mContext, rect.width(), rect.height());
        } catch (IOException e) {
            Toast.makeText(mContext, "Cannot create new NoteDoc.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            finish();
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
        // After adding a page to NoteDoc, get an instance and set it as a member variable.
        mSpenPageDoc = mSpenNoteDoc.appendPage();
        mSpenPageDoc.setBackgroundColor(0xFF000000);
        mSpenPageDoc.clearHistory();

        // Set PageDoc to View.
        mSpenSurfaceView.setPageDoc(mSpenPageDoc, true);
        initPenSettingInfo();
        mSpenSurfaceView.setToolTypeAction(SpenSurfaceView.TOOL_FINGER, SpenSurfaceView.ACTION_NONE);
        if (isSpenFeatureEnabled == false) {
            mSpenSurfaceView.setToolTypeAction(SpenSurfaceView.TOOL_FINGER, SpenSurfaceView.ACTION_STROKE);
            Toast.makeText(mContext, "Device does not support Spen. \n You can draw stroke by finger.", Toast.LENGTH_SHORT).show();
        }

        penBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSpenSurfaceView.setToolTypeAction(SpenSurfaceView.TOOL_SPEN, SpenSurfaceView.ACTION_STROKE);
            }
        });
        eraseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSpenSurfaceView.setToolTypeAction(SpenSurfaceView.TOOL_SPEN, SpenSurfaceView.ACTION_ERASER);
            }
        });
    }

    private void initPenSettingInfo() {
        SpenSettingPenInfo penInfo = new SpenSettingPenInfo();
        penInfo.color = Color.WHITE;
        penInfo.size = 5;
        mSpenSurfaceView.setPenSettingInfo(penInfo);
    }

    private boolean processUnsupportedException(SsdkUnsupportedException e) {
        e.printStackTrace();
        int errorType = e.getType();
        Log.d("An Error Occurred", "Error");
        // The device is not a Samsung device or it is a Samsung device that does not support S pen.
        if (errorType == SsdkUnsupportedException.VENDOR_NOT_SUPPORTED || errorType == SsdkUnsupportedException.DEVICE_NOT_SUPPORTED) {
            Toast.makeText(mContext, "This device does not support Spen.", Toast.LENGTH_SHORT).show();
            finish();
        } else if (errorType == SsdkUnsupportedException.LIBRARY_NOT_INSTALLED) { // SpenSdk 4.1 apk is not installed on the device.
            showAlertDialog("You need to install an additional package to use this application You will be taken to the installation screen. Restart this application after the software has been installed", true);
        } else if (errorType == SsdkUnsupportedException.LIBRARY_UPDATE_IS_REQUIRED) { // The Pen library or SpenSdk 4.1 apk requires to be updated.
            showAlertDialog("You need to update the installed Pen library or package to use this application. You will be taken to the installation screen. Restart this application after the software has been updated", true);
        } else if (errorType == SsdkUnsupportedException.LIBRARY_UPDATE_IS_RECOMMENDED) { // It is recommended that the Pen library or SpenSdk 4.1 apk is updated to // thelatest version as possible
            showAlertDialog("We recommend that you update the installed Pen library or package before using this application. You will be taken to the installation screen. Restart this application after the software has been updated.", false);
            return false;
        }
        return true;
    }

    private void showAlertDialog(String msg, final boolean closeActivity) {
        AlertDialog.Builder dlg = new AlertDialog.Builder(mContext);
        dlg.setIcon(getResources().getDrawable(android.R.drawable.ic_dialog_alert));
        dlg.setTitle("Upgrade Notification").setMessage(msg)
                .setPositiveButton(android.R.string.yes,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Go to the market website and install/update SpenSdk3 4.1 apk.
                                Uri uri = Uri.parse("market://details?id=" + Spen.getSpenPackageName());
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                dialog.dismiss();
                                finish();
                            }
                        }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(
                    DialogInterface dialog, int which) {
                if (closeActivity == true) { // Terminate the activity if the user does not wish to install and closes the dialog.
                    finish();
                }
                dialog.dismiss();
            }
        }).show();
        dlg = null;
    }

    //    public static final Drawable getDrawable(Context context, int id) {
//        final int version = Build.VERSION.SDK_INT;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            return resources.getDrawable(id, context.getTheme());
//        } else {
//            return resources.getDrawable(id);
//        }
//        if (version >= 21) {
//            return ContextCompat.getDrawable(context, id);
//        } else {
//            return context.getResources().getDrawable(id);
//        }
//    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSpenSurfaceView != null) {
            mSpenSurfaceView.close();
            mSpenSurfaceView = null;
        }
        if (mSpenNoteDoc != null) {
            try {
                mSpenNoteDoc.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mSpenNoteDoc = null;
        }
    }
}