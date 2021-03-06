package com.app.watermeter.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.TextView;

import com.alibaba.sdk.android.push.CloudPushService;
import com.alibaba.sdk.android.push.CommonCallback;
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory;
import com.app.watermeter.R;
import com.app.watermeter.apkUpdate.AppUpdateManager;
import com.app.watermeter.common.ChangeLanguageHelper;
import com.app.watermeter.eventBus.ApkInfoEvent;
import com.app.watermeter.eventBus.LanguageChangedEvent;
import com.app.watermeter.model.VersionData;
import com.app.watermeter.utils.DialogUtils;
import com.app.watermeter.utils.ProgressUtils;
import com.app.watermeter.utils.ToastUtil;
import com.app.watermeter.utils.UIUtils;
import com.app.watermeter.view.adapter.FragmentAdapter;
import com.app.watermeter.view.base.BaseActivity;
import com.app.watermeter.view.fragment.HomeFragment;
import com.app.watermeter.view.fragment.BindingFragment;
import com.app.watermeter.view.fragment.MineFragment;
import com.app.watermeter.view.fragment.ScanFragment;
import com.app.watermeter.view.views.NoScrollViewPager;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;


/**
 * @author admin
 */
public class MainActivity extends BaseActivity {
    @BindView(R.id.viewPager)
    NoScrollViewPager viewPager;
    @BindView(R.id.tvHomeTab)
    TextView tvHomeTab;
    @BindView(R.id.tvMeterTab)
    TextView tvMeterTab;
    @BindView(R.id.tvMineTab)
    TextView tvMineTab;
    @BindView(R.id.tvScanTab)
    TextView tvScanTab;
    private List<Fragment> fragmentList;
    private FragmentAdapter adapter;
    private CloudPushService mPushService;

    Context mContext;

    @Override
    protected int getCenterView() {
        return R.layout.activity_main;
    }

    @Override
    protected void initHeader() {
        setHeaderTitle(getString(R.string.main_home));
        setLeftIconVisibility(View.GONE);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(LanguageChangedEvent event) {
        if (mContext instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) mContext;
            mainActivity.finish();

            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ChangeLanguageHelper.init(this);
        mContext = MainActivity.this;
        initData();

    }

    public static Intent makeIntent(Context context) {
        return new Intent(context, MainActivity.class);
    }

    private void initData() {
        fragmentList = new ArrayList<>();
        fragmentList.add(new HomeFragment());
        fragmentList.add(new BindingFragment());
        fragmentList.add(new ScanFragment());
        fragmentList.add(new MineFragment());
        adapter = new FragmentAdapter(fragmentList, getSupportFragmentManager());
        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(adapter);
        tvHomeTab.setSelected(true);
        AppUpdateManager.getInstance().getApkVersionInfo(true);

    }

    @OnClick({R.id.tvHomeTab, R.id.tvMeterTab, R.id.tvMineTab, R.id.tvScanTab})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvHomeTab:
                viewPager.setCurrentItem(0);
                tvHomeTab.setSelected(true);
                tvMeterTab.setSelected(false);
                tvScanTab.setSelected(false);
                tvMineTab.setSelected(false);
                setHeaderTitle(getString(R.string.main_home));

                break;
            case R.id.tvMeterTab:
                viewPager.setCurrentItem(1);
                tvHomeTab.setSelected(false);
                tvMeterTab.setSelected(true);
                tvScanTab.setSelected(false);
                tvMineTab.setSelected(false);
                setHeaderTitle(getString(R.string.binding));
                break;
            case R.id.tvScanTab:
                viewPager.setCurrentItem(2);
                tvHomeTab.setSelected(false);
                tvMeterTab.setSelected(false);
                tvMineTab.setSelected(false);
                tvScanTab.setSelected(true);
                setHeaderTitle(getString(R.string.scan_scan));
                break;
            case R.id.tvMineTab:
                viewPager.setCurrentItem(3);
                tvHomeTab.setSelected(false);
                tvMeterTab.setSelected(false);
                tvScanTab.setSelected(false);
                tvMineTab.setSelected(true);
                setHeaderTitle(getString(R.string.mine));
                break;
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onApkInfoEvent(ApkInfoEvent event) {
        ProgressUtils.getIntance().dismissProgress();
        final VersionData apkInfoModel = event.getApkInfoModel();
        boolean selfCheck = event.isSelfCheck();
        if (!selfCheck) {
            return;
        }
        if (apkInfoModel == null) {
            ToastUtil.showShort(UIUtils.getValueString(R.string.request_data_error));
            return;
        }
        boolean needUpdateApk = AppUpdateManager.getInstance().needUpdateApk(apkInfoModel);
        if (needUpdateApk) {
            DialogUtils.showApkUpdateDialog(this, apkInfoModel, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AppUpdateManager.getInstance().updateNewApk(MainActivity.this, apkInfoModel);
                }
            });
        }
    }
}
