package com.app.watermeter.manager;

import android.util.Log;

import com.app.okhttputils.callback.GenericsCallback;
import com.app.okhttputils.request.JsonGenericsSerializator;
import com.app.watermeter.common.CommonUrl;
import com.app.watermeter.model.ComResponseModel;
import com.app.watermeter.model.UserInfoModel;
import com.app.watermeter.model.UserInfoParam;
import com.app.watermeter.okhttp.DataManager;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Response;

/**
 * Create by Admin on 2018/8/24
 */
public class UserManager {
    public static UserManager instance = null;
    public static DataManager dataInstance = null;

    private UserManager() {

    }

    public static UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        if (dataInstance == null) {
            dataInstance = DataManager.getInstance();
        }
        return instance;
    }

    /**
     * 登录
     * @param contact
     * @param passwd
     */
    public void login(String contact, String passwd) {
        JSONObject params = new JSONObject();
        try {
            params.put("contact", contact);
            params.put("passwd", passwd);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        dataInstance.sendPostRequestData(CommonUrl.LOGIN, params)
                .execute(new GenericsCallback<UserInfoModel>(new JsonGenericsSerializator()) {
                    @Override
                    public void onError(Response response, Call call, Exception e, int id) {
                        String message = e.getMessage();
                      /*  String errorMsg = JsonUtils.getErrorMsg(response);
                        EventBus.getDefault().post(new ErrorResponseEvent(errorMsg, CommonPageState.login_page));*/
                        Log.d("xyc", "onError: message="+message);
                    }

                    @Override
                    public void onResponse(UserInfoModel response, int id) {
                        // EventBus.getDefault().post(new LoginEvent(response));
                        Log.d("xyc", "onResponse: response="+response);
                    }
                });
    }

    /**
     * 发送短信验证码
     * @param contact
     * @param type
     */
    public void sendSmsToCheck(String contact ,String type){
        JSONObject params = new JSONObject();
        try {
            params.put("contact", contact);
            params.put("type",type);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        dataInstance.sendPostRequestData(CommonUrl.SEND_SMS, params)
                .execute(new GenericsCallback<ComResponseModel>(new JsonGenericsSerializator()) {
                    @Override
                    public void onError(Response response, Call call, Exception e, int id) {
                        String message = e.getMessage();
                      /*  String errorMsg = JsonUtils.getErrorMsg(response);
                        EventBus.getDefault().post(new ErrorResponseEvent(errorMsg, CommonPageState.login_page));*/
                        Log.d("xyc", "onError: message="+message);
                    }

                    @Override
                    public void onResponse(ComResponseModel response, int id) {
                        // EventBus.getDefault().post(new LoginEvent(response));
                        Log.d("xyc", "onResponse: response="+response);
                    }
                });
    }

    /**
     * 校验短信验证码
     * @param contact
     * @param type
     * @param vcode
     */
    public void checkSmsCode(String contact,String type,String vcode){
        JSONObject params = new JSONObject();
        try {
            params.put("contact", contact);
            params.put("type",type);
            params.put("vcode",vcode);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        dataInstance.sendPostRequestData(CommonUrl.CHECK_SMS_CODE, params)
                .execute(new GenericsCallback<ComResponseModel>(new JsonGenericsSerializator()) {
                    @Override
                    public void onError(Response response, Call call, Exception e, int id) {
                        String message = e.getMessage();
                      /*  String errorMsg = JsonUtils.getErrorMsg(response);
                        EventBus.getDefault().post(new ErrorResponseEvent(errorMsg, CommonPageState.login_page));*/
                        Log.d("xyc", "onError: message="+message);
                    }

                    @Override
                    public void onResponse(ComResponseModel response, int id) {
                        // EventBus.getDefault().post(new LoginEvent(response));
                        Log.d("xyc", "onResponse: response="+response);
                    }
                });
    }
  public void register(UserInfoParam userInfoParam){
      JSONObject params = new JSONObject();
      try {
          params.put("contact", userInfoParam.getContact());
          params.put("passwd",userInfoParam.getPassword());
          params.put("passwd_confirmation",userInfoParam.getConfirmPsw());
          params.put("email",userInfoParam.getEmail());
          params.put("real_name",userInfoParam.getRealName());
      } catch (JSONException e) {
          e.printStackTrace();
      }
      dataInstance.sendPostRequestData(CommonUrl.REGISTER, params)
              .execute(new GenericsCallback<ComResponseModel>(new JsonGenericsSerializator()) {
                  @Override
                  public void onError(Response response, Call call, Exception e, int id) {
                      String message = e.getMessage();
                      /*  String errorMsg = JsonUtils.getErrorMsg(response);
                        EventBus.getDefault().post(new ErrorResponseEvent(errorMsg, CommonPageState.login_page));*/
                      Log.d("xyc", "onError: message="+message);
                  }

                  @Override
                  public void onResponse(ComResponseModel response, int id) {
                      // EventBus.getDefault().post(new LoginEvent(response));
                      Log.d("xyc", "onResponse: response="+response);
                  }
              });
  }

    public void testIt(){
        dataInstance.sendGetRequestData(CommonUrl.TEST, null)
                .execute(new GenericsCallback<ComResponseModel>(new JsonGenericsSerializator()) {
                    @Override
                    public void onError(Response response, Call call, Exception e, int id) {
                        String message = e.getMessage();
                      /*  String errorMsg = JsonUtils.getErrorMsg(response);
                        EventBus.getDefault().post(new ErrorResponseEvent(errorMsg, CommonPageState.login_page));*/

                    }

                    @Override
                    public void onResponse(ComResponseModel response, int id) {
                        // EventBus.getDefault().post(new LoginEvent(response));
                        Log.d("xyc", "onResponse: response="+response);
                    }
                });
    }
}