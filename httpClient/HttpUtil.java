import org.apache.http.*;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.net.ssl.SSLContext;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * create by 散入风中
 * time by 2018.4.16
 */
public class HttpUtil {

        private static Logger logger = LoggerFactory.getLogger(HttpUtil.class);

        /**
         * get请求
         * @return
         */
        public static String doGet(String url) {
            try {
                CloseableHttpClient client = createSSLClientDefault();
                //发送get请求
                HttpGet request = new HttpGet(url);
                HttpResponse response = client.execute(request);

                /**请求发送成功，并得到响应**/
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    /**读取服务器返回过来的json字符串数据**/
                    String strResult = EntityUtils.toString(response.getEntity());
                    return strResult;
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * post请求(用于key-value格式的参数)
         * @param url
         * @param params
         * @return
         */
        public static String doPost(String url, Map params){
            BufferedReader in = null;
            try {
                CloseableHttpClient client = createSSLClientDefault();
                HttpPost request = new HttpPost();
                request.setURI(new URI(url));
                //设置参数
                List<NameValuePair> nvps = new ArrayList<NameValuePair>();
                for (Iterator iter = params.keySet().iterator(); iter.hasNext();) {
                    String name = (String) iter.next();
                    String value = String.valueOf(params.get(name));
                    nvps.add(new BasicNameValuePair(name, value));
                    //System.out.println(name +"-"+value);
                }
                request.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
                HttpResponse response = client.execute(request);
                int code = response.getStatusLine().getStatusCode();
                if(code == 200){	//请求成功
                    in = new BufferedReader(new InputStreamReader(response.getEntity()
                            .getContent(),"utf-8"));
                    StringBuffer sb = new StringBuffer("");
                    String line = "";
                    String NL = System.getProperty("line.separator");
                    while ((line = in.readLine()) != null) {
                        sb.append(line + NL);
                    }

                    in.close();
                    return sb.toString();
                }
                else{	//
                    System.out.println("状态码：" + code);
                    return null;
                }
            }
            catch(Exception e){
                e.printStackTrace();
                return null;
            }
        }

        /**
         * post请求（用于请求json格式的参数）
         * @param url
         * @param params
         * @return
         */
        public static String doPost(String url, String params) throws Exception {
            CloseableHttpClient httpclient = createSSLClientDefault();
            HttpPost httpPost = new HttpPost(url);// 创建httpPost
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-Type", "application/json");
            String charSet = "UTF-8";
            StringEntity entity = new StringEntity(params, charSet);
            httpPost.setEntity(entity);
            CloseableHttpResponse response = null;
            try {
                response = httpclient.execute(httpPost);
                StatusLine status = response.getStatusLine();
                int state = status.getStatusCode();
                if (state == HttpStatus.SC_OK) {
                    HttpEntity responseEntity = response.getEntity();
                    String jsonString = EntityUtils.toString(responseEntity);
                    return jsonString;
                }
                else{
                    logger.error("请求返回:"+state+"("+url+")");
                }
            }
            finally {
                if (response != null) {
                    try {
                        response.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    httpclient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
    }

    /**
     * 略过安全检查，信任所有SSL证书
     * create by 散入风中
     * time by 2018/9/10
     * @return CloseableHttpClient
     */
    public static CloseableHttpClient createSSLClientDefault(){
        try {
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null,
                    //忽略条件，信任所有证书
                    (X509Certificate[] chain, String authType) -> {
                        return true;
            }).build();
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext);
            return HttpClients.custom().setSSLSocketFactory(sslsf).build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  HttpClients.createDefault();
    }

}
