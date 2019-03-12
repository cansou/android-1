package com.yzx.tools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.yzx.api.UCSCall;
import com.yzx.http.HttpTools;
import com.yzx.http.net.SharedPreferencesUtils;
import com.yzx.listenerInterface.ReportListener;
import com.yzxtcp.tools.CustomLog;

import android.content.Context;

public class ErrorCodeReportTools {
	
	private static final long time = (23 * 60 * 60 * 1000);
	//private static final long time = ( 60 * 60 * 1000);
	
	private static final String REPORT_ERROR_CODE = "REPORT_ERROR_CODE";
	public static boolean isReportErrorCode(Context mContext){
		return (time +  mContext.getSharedPreferences(DefinitionAction.PREFERENCE_NAME, 0).getLong(REPORT_ERROR_CODE, 0)) < System.currentTimeMillis();
	}
	
	public static void saveReportErrorCode(Context mContext){
		mContext.getSharedPreferences(DefinitionAction.PREFERENCE_NAME, 0).edit().putLong(REPORT_ERROR_CODE, System.currentTimeMillis()).commit();
	}
	
	
	
	
	/**
	 * 上报错误码
	 * @param mContext
	 * @param reportDevicesListener
	 * @author: xiaozhenhua
	 * @data:2014-10-20 下午7:30:09
	 */
	public static void reportErrorCode(final Context mContext,final ReportListener reportDevicesListener){
		new Thread(new Runnable() {
			@Override
			public void run() {
				String url = DefinitionAction.REPORT_URL+"/ulog/log?event=errorCode";
				JSONArray jsonArray = getErrorCode(mContext);
				CustomLog.v("REPORT_ERRORCODE_JSON:"+jsonArray.toString());
				if(jsonArray != null && jsonArray.length() > 0){
					JSONObject resultJson = HttpTools.doPostMethod(mContext, url, jsonArray.toString());
					if(resultJson != null && resultJson.has("code")){
						CustomLog.v("REPORT_ERRORCODE_RESPONSE_JSON:"+resultJson);
						try {
							reportDevicesListener.onReportResult(resultJson.getInt("code"), resultJson.has("result")?resultJson.getString("result"):"");
						} catch (JSONException e) {
							reportDevicesListener.onReportResult(-2, e.toString());
							e.printStackTrace();
						}
					}else{
						reportDevicesListener.onReportResult(-1, "errorCode response is null");
					}
				}else{
					reportDevicesListener.onReportResult(-3, "report errorCode json is null ");
				}
			}
		}).start();
	}
	
	
	/**
	 * 收集错误码
	 * @param mContext
	 * @param clientNumber：子账号
	 * @param ifName:函数名称
	 * @param errorCode:错误码
	 * @param errorMsg：错误消息
	 * @author: xiaozhenhua
	 * @data:2014-10-20 下午5:09:00
	 */
	private static void collectionErrorCode(Context mContext,String clientNumber,String ifName,int errorCode,String errorMsg){
		if(SharedPreferencesUtils.isLogReportEnable(mContext)
				&& clientNumber != null && clientNumber.length() > 0 ){
			JSONObject errorJson = new JSONObject();
			try {
				errorJson.put("clientNumber", clientNumber);
				errorJson.put("ifType", 1);
				if(ifName != null && ifName.length() > 0){
					errorJson.put("ifName", ifName);
				}else{
					getFunctionName(errorCode, errorJson, errorMsg);
				}
				errorJson.put("errorCode", errorCode);
				errorJson.put("errorMsg", errorMsg);
				errorJson.put("logDate", DateUtils.getDate());
				saveErrorCode(mContext,errorJson);
				CustomLog.v("CONNECTION_ERROR_CODE:"+errorJson.toString());
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	
	private static void getFunctionName(int errorCode,JSONObject errorJson,String errorMsg){
		try{
			switch(errorCode){
			case UCSCall.CALL_VOIP_ERROR:
				if(errorMsg.startsWith("media")){
					errorJson.put("ifName", "onDialFailed");
				}else{
					errorJson.put("ifName", "onHangUp");
				}
				break;
			case UCSCall.CALL_VOIP_NOT_ENOUGH_BALANCE:
				if(errorMsg.startsWith("sorry")){
					errorJson.put("ifName", "onDialFailed");
				}else{
					errorJson.put("ifName", "onHangUp");
				}
				break;
			case 300010:
				if(errorMsg.startsWith("session")){
					errorJson.put("ifName", "onDialFailed");
				}else{
					errorJson.put("ifName", "onConnectionFailed");
				}
				break;
			case 300202:
			case 300203:
			case 300204:
			case 300205:
			case 300206:
			case 300003:
			case 300004:
			case 300001:
			case 300002:
			case 300005:
			case UCSCall.CALL_NUMBER_IS_EMPTY:
			case 300007:
			case 300008:
			case 300009:
			case 300011:
			case 300012:
			case 300014:
			case 300015:
			case 300016:
			case 300017:
			case 300505:
			case 300207:
			case 300501:
			case 300502:
			case 300503:
			case 300504:
			case 300506:
			case 300507:
			case 300508:
				errorJson.put("ifName", "onConnectionFailed");
				break;
			case 300013:
				errorJson.put("ifName", "sendUcsMessage");
				break;
			case 300230:
				errorJson.put("ifName", "onReceiveUcsMessage");
				break;
			case UCSCall.CALL_VOIP_CALLYOURSELF:
			case UCSCall.CALL_VOIP_NOT_ANSWER:
			case UCSCall.CALL_VOIP_NETWORK_TIMEOUT:
			case UCSCall.HUNGUP_MYSELF:
			case UCSCall.HUNGUP_OTHER:
			case UCSCall.HUNGUP_OTHER_REASON:
			case UCSCall.HUNGUP_MYSELF_REFUSAL:
			case UCSCall.CALL_VOIP_REFUSAL:
				errorJson.put("ifName", "onHangUp");
				break;
			case 300019:
			case UCSCall.CALL_VOIP_BUSY:
			case UCSCall.CALL_VOIP_NUMBER_OFFLINE:
			case UCSCall.CALL_VOIP_CALLID_NOT_EXIST:
			case UCSCall.CALL_VOIP_USERID_NOT_EXIST:
			case UCSCall.CALL_VOIP_NUMBER_WRONG:
			case UCSCall.CALL_VOIP_ACCOUNT_FROZEN:
			case UCSCall.CALL_VOIP_REJECT_ACCOUNT_FROZEN:
			case UCSCall.CALL_VOIP_ACCOUNT_EXPIRED:
			case UCSCall.CALL_VOIP_TRYING_183:
			case 300233:
			case 300234:
			case 300235:
			case 300236:
			case 300237:
			case 300238:
			case 300239:
			case 300240:
			case 300241:
			case 300242:
			case 300243:
			case UCSCall.CALL_VIDEO_DOES_NOT_SUPPORT:
			case UCSCall.CALL_FAIL_BLACKLIST:
				errorJson.put("ifName", "onDialFailed");
				break;
			default:
				errorJson.put("ifName", "");
				break;
			}
		}catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 保存错误码
	 * @param mContext
	 * @param errorJson
	 * @author: xiaozhenhua
	 * @data:2014-10-20 下午7:30:22
	 */
	private static void saveErrorCode(Context mContext,JSONObject errorJson){
		mContext.getSharedPreferences(DefinitionAction.PREFERENCE_NAME, 0).edit().putString("YZX_ERROR_CODE_LIST", getErrorCode(mContext).put(errorJson).toString()).commit();
	}
	
	/**
	 * 获取错误码列表
	 * @param mContext
	 * @return
	 * @author: xiaozhenhua
	 * @data:2014-10-20 下午7:30:33
	 */
	private static JSONArray getErrorCode(Context mContext){
		String json = mContext.getSharedPreferences(DefinitionAction.PREFERENCE_NAME, 0).getString("YZX_ERROR_CODE_LIST", "");
		JSONArray jsonArray = null;
		if(json != null && json.length() > 0){
			try {
				jsonArray = new JSONArray(json);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}else{
			jsonArray = new JSONArray();
		}
		return  jsonArray;
	}
	
	/**
	 * 清除以经上报过的errorCode
	 * @param mContext
	 * @author: xiaozhenhua
	 * @data:2014-10-20 下午7:17:43
	 */
	public static void cleanErrorCode(Context mContext){
		mContext.getSharedPreferences(DefinitionAction.PREFERENCE_NAME, 0).edit().putString("YZX_ERROR_CODE_LIST", "").commit();
	}
	
}
