package org.iam.compoment.sync.yunxiazi;

import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

public class HttpUtil {

	/**
	 * PSOT请求方法
	 * @param url
	 * @param param
	 * @param headers
	 * @return
	 */
	@SuppressWarnings({ "resource" })
	public static String POST(String url, Object param, Map<String,String> headers) {
		HttpPost httpPost = null;
		String result = null;
		try {
			HttpClient client = new SSLClient();
			httpPost = new HttpPost(url);
			if (param != null) {
				StringEntity entity = new StringEntity(param.toString(), "utf-8");
				httpPost.setEntity(entity);
				for (Map.Entry<String,String> entry : headers.entrySet()) {
					httpPost.addHeader(entry.getKey(), entry.getValue());
				}
			}
			HttpResponse response = client.execute(httpPost);
			if (response != null) {
				HttpEntity resEntity = response.getEntity();
				if (resEntity != null) {
					result = EntityUtils.toString(resEntity, "utf-8");
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return result;
	}
}
