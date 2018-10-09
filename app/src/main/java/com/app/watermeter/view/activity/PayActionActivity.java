package com.app.watermeter.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.app.watermeter.R;
import com.app.watermeter.common.CommonParams;
import com.app.watermeter.common.CommonUrl;
import com.app.watermeter.eventBus.GetChartReadListEvent;
import com.app.watermeter.eventBus.GetPayResultEvent;
import com.app.watermeter.eventBus.GetPerPayEvent;
import com.app.watermeter.manager.MeterManager;
import com.app.watermeter.utils.DataUtils;
import com.app.watermeter.utils.DateUtils;
import com.app.watermeter.utils.PreferencesUtils;
import com.app.watermeter.view.base.BaseActivity;
import com.app.watermeter.view.base.WebViewActivity;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author admin
 */
public class PayActionActivity extends BaseActivity {


    @BindView(R.id.etMoney)
    EditText etMoney;
    @BindView(R.id.llPayStart)
    LinearLayout llPayStart;
    @BindView(R.id.tvMoney)
    TextView tvMoney;
    @BindView(R.id.llPayEnd)
    LinearLayout llPayEnd;
    @BindView(R.id.tvPayAction)
    TextView tvPayAction;
    private Context mContext;
    private int meterId;

    @Override
    protected int getCenterView() {
        return R.layout.activity_pay_action;
    }

    public static Intent makeIntent(Context context, int meterId) {
        Intent intent = new Intent(context, PayActionActivity.class);
        intent.putExtra(CommonParams.METER_ID, meterId);
        return intent;
    }


    @Override
    protected void initHeader() {
        setHeaderTitle(getString(R.string.payment_money_title));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = PayActionActivity.this;
        getIntentData();
    }

    /**
     * 获取类型
     */
    private void getIntentData() {
        Intent intent = getIntent();
        meterId = intent.getIntExtra(CommonParams.METER_ID, 0);
    }

    @OnClick({R.id.tvPayAction})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tvPayAction:
//                llPayStart.setVisibility(View.GONE);
//                llPayEnd.setVisibility(View.VISIBLE);
//                tvPayAction.setText(getString(R.string.completed));


                String money = etMoney.getText().toString().trim();
                if (TextUtils.isEmpty(money)) {

                } else {
                    long payMoney = Long.valueOf(money);
                    MeterManager.getInstance().saveMoney(meterId, payMoney, CommonParams.USD);
                }
                break;
        }
    }

    /**
     * 接口返回--充值
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GetPerPayEvent event) {
        if (event.getModelInfo() != null) {
            String tradeId = event.getModelInfo().getTrade_id() + "";
            String orderNo = event.getModelInfo().getOut_trade_no();
            PreferencesUtils.putString("orderNo",orderNo);

            String callTime = DateUtils.getGMT7PayTime();

            String security = DataUtils.getRandomStr() + DataUtils.MD5(CommonParams.PARTNER + CommonParams.SECURITY_KEY + tradeId + callTime) + DataUtils.getRandomStr();

            String html = "<!DOCTYPE><html><head><title>ACE API</title><meta charset=\"UTF-8\"/></head><body><form action=\"https://uat-api.asiaweiluy.com/gateway.php?method=ace.trade.pay\" method=\"post\" id=\"awl_post\" target=\"_self\"><input type=\"hidden\" name=\"partner\" value=\"" + CommonParams.PARTNER + "\"/><input type=\"hidden\" name=\"trade_id\" value=\"" + tradeId + "\"/><input type=\"hidden\" name=\"call_time\" value=\"" + callTime + "\"/><input type=\"hidden\" name=\"security\" value=\"" + security.toLowerCase() + "\"/></form></body><script>window.onload=function(){document.getElementById(\"awl_post\").submit();}</script></html>\n";
            startActivityForResult(WebViewActivity.makeIntent(mContext, html),CommonParams.PAY_RESULT);
//            MeterManager.getInstance().paymentAction(event.getModelInfo().getTrade_id(), callTime, security.toLowerCase());
        }
    }

    /**
     * 接口返回--支付
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GetPayResultEvent event) {

        if (!TextUtils.isEmpty(event.getHtmlStr())) {
            String html = event.getHtmlStr();
            String htmlStr = html.replace("./web/", CommonUrl.BASE_PAY_URL);

            mContext.startActivity(WebViewActivity.makeIntent(mContext, htmlStr));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == CommonParams.PAY_RESULT) {
            setResult(CommonParams.PAY_RESULT);
            finish();
        }
    }
}
