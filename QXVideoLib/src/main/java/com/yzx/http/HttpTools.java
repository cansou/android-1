package com.yzx.http;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;

import org.json.JSONException;
import org.json.JSONObject;


import android.content.Context;

/*import com.yzx.listenerInterface.MessageListener;
import com.yzx.listenerInterface.UploadProgressListener;*/
import com.yzx.preference.UserData;
import com.yzx.tools.Util;
import com.yzxtcp.tools.CustomLog;

/**
 * 
 * @author xiaozhenhua
 *
 */
public class HttpTools {
	
	// Http回调
	public interface HttpCallbackListener {
		// code 0代表成功，1代表url非法，2代表失败
		void onFinish(byte[] response, int code);
		void onError(Exception e, int code);
	}
	
	public static void sendHttpRequest(final String address, final HttpCallbackListener listener) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				HttpURLConnection connection = null;
				ByteArrayOutputStream baos = null;
				InputStream is = null;
				try {
					URL url = new URL(address);
					connection = (HttpURLConnection) url.openConnection();
					connection.setConnectTimeout(8000);
					connection.setReadTimeout(8000);
					connection.connect();
					is = connection.getInputStream();
					baos = new ByteArrayOutputStream();
					byte[] buffer = new byte[1024];
					int length = -1;
					while ((length = is.read(buffer)) != -1) {
						baos.write(buffer, 0, length);
					}
					baos.flush();
					if(listener != null) {
						listener.onFinish(baos.toByteArray(),0);
					}
				} catch (Exception e) {
					e.printStackTrace(); 
					if(listener != null) {
						listener.onError(e,2);
					}
					CustomLog.e("load previewImgUrl exception:" + e.getMessage());
				} finally {
					if(is != null) {
						try {
							is.close();
						} catch (IOException e) {
							e.printStackTrace();  
						}
					}
					if(baos != null) {
						try {
							baos.close();
						} catch (IOException e) {
							e.printStackTrace();  
						}
					}
					if(connection != null) {
						connection.disconnect();
					}
				}
			}
		}).start();
	}

	private static TrustManager[] trustManager = new YzxTrustManager[]{new YzxTrustManager()};
	
	/**
	 * @Description 不需要验证服务器证书
	 * @date 2016-12-8 下午12:12:03 
	 * @author xhb  
	 * @return void    返回类型
	 */
	private static void trustAllHosts() {
		// Create a trust manager that does not validate certificate chains
		// Android 采用X509的证书信息机制
		// Install the all-trusting trust manager
		try {
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, trustManager, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			HttpsURLConnection.setDefaultHostnameVerifier(DO_NOT_VERIFY); // 不进行主机名确认
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
		@Override
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	};
	
	
	/**
	 * 验证账号(登录)
	 * @param amsAddress:登录地址
	 * @param mainaccount：主账号(开发者账号)
	 * @param mainaccountpwd:主账号密码(开发者账号密码)
	 * @param voipaccount:子账号(用户账号)
	 * @param voipaccountpwd:子账号密码(用户密码)
	 * @param imei:设备IMEI
	 * @param keys:协议扩展key(如开发者验证账号反回成功的协议中多了一个key,侧开发者将该key加入到kyes列表中,协议解析时会从反回成功的协议中解析出该key)
	 * @author: xiaozhenhua
	 * @throws JSONException 
	 * @throws IOException 
	 * @data:2014-4-10 上午10:35:45
	 */
	public static JSONObject loginToAms(StringBuffer sbf) throws IOException, JSONException{
		return doGet(sbf.toString(),null);
	}
	
	public static JSONObject getCsAddress(StringBuffer csmAddress) throws IOException, JSONException{
		return doGet(csmAddress.toString(), null);
	}
	
	/**
	 * get提交
	 * 
	 * @author: xiaozhenhua
	 * @throws JSONException 
	 * @throws IOException 
	 * @data:2014-4-11 上午9:55:19
	 */
	public static JSONObject doGetMethod(String buffer ,String ac) throws IOException, JSONException{
		return doGet(buffer,ac);
	}
	
	/**
	 * port提交
	 * @param mContext
	 * @param uri
	 * @param body
	 * @return
	 * @author: xiaozhenhua
	 * @data:2014-10-17 下午5:14:29
	 */
	public static JSONObject doPostMethod(Context mContext, String uri, String body) {
		return httpConnectionPostJson(uri, body);
		//return doPostMethod(mContext, uri, body, NetWorkTools.isWifi(mContext), false, true);
	}
	
	public static JSONObject httpConnectionPostJson(String strUrl, String body) {
		StringBuffer result = new StringBuffer();
		URLConnection connection = null;
		BufferedReader reader = null;
		JSONObject jsonOuter = null;
		try {
			URL url = new URL(strUrl);
			connection = url.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setUseCaches(false);
			connection.setRequestProperty("Content-Type","text/xml;charset=UTF-8");
			connection.setConnectTimeout(50000);
			connection.setReadTimeout(50000);
			connection.connect();
			OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
			out.write(new String(body.getBytes("UTF-8")));
			out.flush();
			out.close();

			reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
			
			String line = "";
			while ((line = reader.readLine()) != null) {
				result.append(line);
				result.append("\r\n");
			}
		} catch (Exception e) {
			result.append("");
			e.printStackTrace();
		}finally{
			if(reader != null){
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		try {
			if (result != null){
				jsonOuter = new JSONObject(result.toString());
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonOuter;
	}
	
	
	private static JSONObject doPostMethod(Context mContext, String uri, String body, boolean isWifi, boolean proxy, boolean isFirstHost) {
		InputStream is = null;
		HttpURLConnection httpconn = null;
		JSONObject jsonOuter = null;
		String jsonstr = null;
		int httpcode = -1;
		URL url = null;
		String proxyHost = android.net.Proxy.getDefaultHost();
		try {
			url = new URL(uri);
			if (!isWifi && proxyHost != null) {// 如果是wap方式，要加网关
				java.net.Proxy p = new java.net.Proxy(java.net.Proxy.Type.HTTP,new InetSocketAddress(android.net.Proxy.getDefaultHost(),android.net.Proxy.getDefaultPort()));
				if (proxy){
					httpconn = (HttpURLConnection) url.openConnection(p);
				}else{
					httpconn = (HttpURLConnection) url.openConnection();
				}
			} else {
				httpconn = (HttpURLConnection) url.openConnection();
			}
			httpconn.setRequestMethod("POST");
			httpconn.setDoOutput(true);
			httpconn.setDoInput(true);
			httpconn.setUseCaches(false);
			httpconn.setRequestProperty("Accept-Charset", "utf-8");
			httpconn.setRequestProperty("Connection", "close");
			httpconn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
			httpconn.setRequestProperty("SecurityFlag", Util.setSecurityFlag());
			// 设置cookie
			/*if(UserData.getAc() != null && UserData.getAc().length() > 0){
				httpconn.setRequestProperty("ac", UserData.getAc());
			}*/
			if (UserData.getImSsid(mContext) != null && UserData.getImSsid(mContext).length() > 0) {
				httpconn.setRequestProperty("ac", UserData.getImSsid(mContext));
			}

			OutputStream outputStream = httpconn.getOutputStream();
			outputStream.write(body.getBytes("utf-8"));
			outputStream.flush();
			outputStream.close();
			httpcode = httpconn.getResponseCode();
			if (httpcode == 200) {
				is = httpconn.getInputStream();
				jsonstr = convertStreamToString(is);
			}
		} catch (IOException e) {
			e.printStackTrace();
			if(!proxy && proxyHost != null && !isWifi){
				return doPostMethod(mContext, uri, body, isWifi, true, isFirstHost);
			}
		} finally {
			try {
				if (is != null)
					is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				if (httpconn != null) {
					httpconn.disconnect();
					httpconn = null;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try {
			if (jsonstr != null){
				jsonOuter = new JSONObject(jsonstr);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonOuter;
	}
	
	
	private static JSONObject doGet(String buffer,String ac) throws IOException, JSONException{
		InputStream is = null;
		String jsonStr = null;
		JSONObject jsonOuter = null;
		String cookie = "";
		
		URL url = new URL(buffer);
		HttpURLConnection httpconn;
		if(url.getProtocol().toLowerCase().equals("https")){
			trustAllHosts();
			httpconn = (HttpsURLConnection) url.openConnection();  
//			((HttpsURLConnection) httpconn).setHostnameVerifier(DO_NOT_VERIFY);// 不进行主机名确认
			//CustomLog.v("LOGIN_HTTPS ... ");
		}else{
			httpconn = (HttpURLConnection) url.openConnection();
			//CustomLog.v("LOGIN_HTTP ... ");
		}
		httpconn.setConnectTimeout(10000);
		httpconn.setReadTimeout(10000);
		httpconn.setRequestMethod("GET");// 设置请求类型为
		httpconn.setDoInput(true);
		httpconn.setRequestProperty("Accept-Charset", "utf-8");
		httpconn.setRequestProperty("Connection", "close");
		httpconn.setRequestProperty("SecurityFlag", Util.setSecurityFlag());
		if(ac != null && ac.length() > 0){
			httpconn.setRequestProperty("ac", ac);
		}

		int httpcode = httpconn.getResponseCode();
		if (httpcode == 200) {
			cookie = httpconn.getHeaderField("set-cookie");
			is = httpconn.getInputStream();
			jsonStr = convertStreamToString(is);
		}
		
		if (is != null) {
			is.close();
			is = null;
		}
		if (httpconn != null) {
			httpconn.disconnect();
			httpconn = null;
		}
		if (jsonStr != null){
			jsonOuter = new JSONObject(jsonStr);
			if(cookie != null && cookie.length() > 0){
				jsonOuter.put("cookie", cookie);
			}
		}
		return jsonOuter;
	}
	
	private static void getCookie(HttpURLConnection http) {
		String cookieVal = null;
		String key = null;
		for (int i = 1; (key = http.getHeaderFieldKey(i)) != null; i++) {
			if (key.equalsIgnoreCase("set-cookie")) {
				cookieVal = http.getHeaderField(i);
				cookieVal = cookieVal.substring(0, cookieVal.indexOf(";"));
			}
		}
	}

	/**
	 * 将http的二进止流转换成JSON字符串
	 * @param is
	 * @return
	 * @author: xiaozhenhua
	 * @throws IOException 
	 * @data:2014-4-9 下午4:13:06
	 */
	private static synchronized String convertStreamToString(InputStream is) throws IOException {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length = -1;
		while ((length = is.read(buffer)) != -1) {
			stream.write(buffer, 0, length);
		}
		stream.flush();
		stream.close();
		is.close();
		return stream.toString();
	}
}
