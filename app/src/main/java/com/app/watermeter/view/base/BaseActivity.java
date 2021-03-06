package com.app.watermeter.view.base;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.okhttputils.eventbus.InterceptCodeEvent;
import com.app.watermeter.R;
import com.app.watermeter.common.ComApplication;
import com.app.watermeter.common.CommonParams;
import com.app.watermeter.eventBus.DefaultEvent;
import com.app.watermeter.manager.UserManager;
import com.app.watermeter.utils.DateUtils;
import com.app.watermeter.utils.DialogUtils;
import com.app.watermeter.utils.PreferencesUtils;
import com.app.watermeter.view.activity.LoginActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.weyye.hipermission.HiPermission;
import me.weyye.hipermission.PermissionCallback;
import me.weyye.hipermission.PermissionItem;

public abstract class BaseActivity extends AppCompatActivity implements DrawerLayout.DrawerListener {

    @BindView(R.id.drawerLayout)
    DrawerLayout drawerLayout;
    @BindView(R.id.tvTitleBar)
    View tvTitleBar;
    @BindView(R.id.rlBaseTitleLayout)
    RelativeLayout rlBaseTitleLayout;
    @BindView(R.id.ivGoBack)
    protected ImageView ivGoBack;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivNext)
    ImageView ivNext;
    @BindView(R.id.tvNext)
    TextView tvNext;
    @BindView(R.id.llCenterView)
    LinearLayout llCenterView;
    @BindView(R.id.llEmptyPage)
    LinearLayout llEmptyPage;
    @BindView(R.id.rlAddSelectContain)
    RelativeLayout rlAddSelectContain;
    private boolean needTranslucent = true;
    private static final int INVALID_VAL = -1;
    private int mTitleColor = 0;

    private AlertDialog alertDialog;


    /**
     * 外部布局页面统一从这里传进来
     *
     * @return
     */
    protected abstract int getCenterView();

    /**
     * 初始化标题栏的内容，设置标题栏标题，icon，返回事件，等等，在各自子类实现
     */
    protected abstract void initHeader();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(getVew());


        // 全部绑定ButterKnife
        ButterKnife.bind(this);
        // 全局注册EventBus ，，EventBus貌似不能重复注册，这里判断一下
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        ComApplication.getApp().addActivity(this);
        checkPermission();
        updateBaseData();
        checkInstallPermission();
    }
    protected  void checkInstallPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //先获取是否有安装未知来源应用的权限
            boolean    haveInstallPermission = getPackageManager().canRequestPackageInstalls();
            if (!haveInstallPermission) {//没有权限
                DialogUtils.showPermissionDialog(this, new View.OnClickListener() {
                    @TargetApi(Build.VERSION_CODES.O)
                    @Override
                    public void onClick(View v) {
                        startInstallPermissionSettingActivity();
                    }
                });
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startInstallPermissionSettingActivity() {
        Uri packageURI = Uri.parse("package:" + getPackageName());
        //注意这个是8.0新API
        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageURI);
        startActivityForResult(intent, 10086);
    }

    /**
     * 安卓6.0动态检查权限
     *
     * @param
     */
    private void checkPermission() {
        List<PermissionItem> permissionItems = initPermissionList();
        if (permissionItems == null || permissionItems.size() == 0) {
            return;
        }
        HiPermission.create(this)
                .title("权限申请")
                .permissions(permissionItems)
                .msg("权限申请")
                .animStyle(R.style.PermissionAnimScale)
                .style(R.style.PermissionDefaultBlueStyle)
                .checkMutiPermission(new PermissionCallback() {
                    @Override
                    public void onClose() {
                        Log.d("xyc", "onClose: 1");
                    }

                    @Override
                    public void onFinish() {
                    }

                    @Override
                    public void onDeny(String permission, int position) {
                        Log.d("xyc", "onDeny:1 ");
                    }

                    @Override
                    public void onGuarantee(String permission, int position) {
                        Log.d("xyc", "onGuarantee:1 ");
                    }
                });
    }

    /**
     * 初始化需要申请的权限
     *
     * @return
     */
    private List<PermissionItem> initPermissionList() {
        List<PermissionItem> permissionItems = new ArrayList<>();
        permissionItems.add(new PermissionItem(Manifest.permission.WRITE_EXTERNAL_STORAGE, getResources().getString(R.string.permission_storage), R.drawable.permission_ic_storage));
        permissionItems.add(new PermissionItem(Manifest.permission.CAMERA,  getResources().getString(R.string.permission_camera), R.drawable.permission_ic_camera));

        return permissionItems;
    }

    boolean isShow = false;
    private long preSystemTime = 0;
    private long curTime = 0;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoginOutEvent(InterceptCodeEvent event) {
        isShow = true;
        Log.d("xyc", "onLoginOutEvent: event.getCode()=" + event.getCode());
        if (event.getCode() == 401) {
            if (preSystemTime == 0) {
                preSystemTime = DateUtils.getSystemTime();
            } else {
                curTime = DateUtils.getSystemTime();
                long time = curTime - preSystemTime;

                if (time > 0) {
                    preSystemTime = 0;
                    return;
                }
            }
            loginOutOnLine(this);
        }

    }
    public void loginOutOnLine(Context appContext) {
        UserManager.getInstance().loginOut();
        PreferencesUtils.putString(CommonParams.USER_TOKEN, null);
        appContext.startActivity(LoginActivity.makeIntent(appContext, true));
    }

    /**
     * 全局处理一些判断的时候可以用
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDefaultEventBus(DefaultEvent event) {

    }



    /**
     * 从子类获取中间部分页面后，初始化整个界面
     *
     * @return
     */
    private View getVew() {
        View mContainView = LayoutInflater.from(this).inflate(R.layout.activity_base, null);
        LayoutInflater inflater = getLayoutInflater();
        int centerView = getCenterView();
        if (centerView!= 0) {
            View mAddView = inflater.inflate(centerView, null);
            LinearLayout llCenterView = mContainView.findViewById(R.id.llCenterView);
            llCenterView.addView(mAddView, new ViewGroup.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
        }
        return mContainView;
    }

    /**
     * 设置一些，初始化一些基类的事件，初始化等等
     */
    private void updateBaseData() {
        initHeader();
        translucentStatus(this, mTitleColor);
        drawerLayout.addDrawerListener(this);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        ivGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     * 设置状态栏的颜色
     *
     * @param activity
     * @param statusColor
     */
    protected void translucentStatus(Activity activity, int statusColor) {
        if (!getIsNeedTranslucent()) {
            return;
        }
        int setColor;
        if (statusColor == 0) {
            setColor = activity.getResources().getColor(R.color.main_blue_color);
        } else {
            setColor = activity.getResources().getColor(statusColor);
        }
        //当前手机版本为5.0及以上
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (statusColor != INVALID_VAL) {
                activity.getWindow().setStatusBarColor(setColor);
            }
            return;
        }
    }

    /**
     * 这是设置全局的状态栏颜色
     * 从子类传进状态栏的颜色，记得在子类重写的initHeader()方法里面设置，否则会失效。
     *
     * @param color
     */
    protected void setHeaderTitleBarColor(int color) {
        mTitleColor = color;
    }

    /**
     * 侧滑栏监听事件
     *
     * @param drawerView
     * @param slideOffset
     */
    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {

    }

    /**
     * 侧滑栏监听事件
     *
     * @param drawerView
     */
    @Override
    public void onDrawerOpened(View drawerView) {

    }

    /**
     * 侧滑栏监听事件
     *
     * @param drawerView
     */
    @Override
    public void onDrawerClosed(View drawerView) {

    }

    /**
     * 侧滑栏监听事件
     *
     * @param newState
     */
    @Override
    public void onDrawerStateChanged(int newState) {

    }

    public boolean getIsNeedTranslucent() {
        return needTranslucent;
    }

    /**
     * 设置是否需要透明化状态栏，默认true，可以自己设置状态栏颜色，，
     *
     * @param needTranslucent
     */
    public void setIsNeedTranslucent(boolean needTranslucent) {
        this.needTranslucent = needTranslucent;
    }


    @OnClick({R.id.ivNext, R.id.tvNext, R.id.llEmptyPage})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivNext:
                break;
            case R.id.tvNext:
                break;
            case R.id.llEmptyPage:
                break;
        }
    }

    /**
     * 设置左上角返回按钮,默认显示
     *
     * @param visibility
     */
    protected void setLeftIconVisibility(int visibility) {
        if (ivGoBack == null) {
            return;
        }
        ivGoBack.setVisibility(visibility);
    }

    /**
     * 设置右边文字按钮状态，默认隐藏
     *
     * @param visibility
     */
    protected void setRightTextVisibility(int visibility) {
        if (tvNext == null) {
            return;
        }
        tvNext.setVisibility(visibility);
    }

    /**
     * 设置右边图标按钮状态 ，默认隐藏
     *
     * @param visibility
     */
    protected void setRightIconVisibility(int visibility) {
        if (ivNext == null) {
            return;
        }
        ivNext.setVisibility(View.VISIBLE);
    }

    /**
     * 设置右边图标按钮的图标
     *
     * @param resId
     */
    protected void setRightIcon(int resId) {
        if (ivNext != null) {
            ivNext.setImageResource(resId);
        }
    }

    /**
     * 设置右边按钮文字
     *
     * @param rightText
     */
    protected void setHeader_RightText(String rightText) {
        if (tvNext == null) {
            return;
        }
        tvNext.setVisibility(View.VISIBLE);
        tvNext.setText(rightText);
    }

    /**
     * 设置标题栏标题
     *
     * @param title
     */
    protected void setHeaderTitle(String title) {
        if (tvTitle == null || title == null) {
            return;
        }
        tvTitle.setText(title);
    }

    /**
     * 设置右边图标按钮的监听事件
     *
     * @param listener
     */
    protected void setHeader_rightIconListener(View.OnClickListener listener) {
        if (ivNext != null) {
            ivNext.setOnClickListener(listener);
        }
    }


    /**
     * 设置右边文字按钮的点击事件监听
     *
     * @param listener
     */
    protected void setHeader_RightTextClickListener(View.OnClickListener listener) {
        if (tvNext != null) {
            tvNext.setOnClickListener(listener);
        }

    }

    protected void setHeaderVisibility(int visibility) {
        if (rlBaseTitleLayout == null) {
            return;
        }
        rlBaseTitleLayout.setVisibility(visibility);
    }
    public void showLoadingDialog() {
        alertDialog = new AlertDialog.Builder(BaseActivity.this).create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable());
        alertDialog.setCancelable(false);
        alertDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_SEARCH || keyCode == KeyEvent.KEYCODE_BACK)
                    return true;
                return false;
            }
        });
        alertDialog.show();
        alertDialog.setContentView(R.layout.loading_alert);
        alertDialog.setCanceledOnTouchOutside(false);
    }
    public void dismissLoadingDialog() {
        if (null != alertDialog && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
    }


    /**
     * 解除EventBus注册
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }
}
