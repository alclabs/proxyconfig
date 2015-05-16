package com.alcshare.proxyconfig.servlets;

import com.alcshare.proxyconfig.Config;
import com.alcshare.proxyconfig.util.Logging;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;

/**
 *
 */
public class ProxyTestServlet extends HttpServlet
{
    public static final String PROTOCOL_PREFIX = "http://";

    private static final String PROP_ERRORMSG = "errormsg";
    private static final String PROP_GOOD = "good";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
       try {
          JSONObject result = new JSONObject();
          resp.setContentType("text/json");

          Config config = Config.load();
          String testURL = config.getTestURL();
          URI testURI;
          try {
              testURI = new URI(PROTOCOL_PREFIX+testURL);
              testProxy(testURI, result);
          } catch (URISyntaxException e)
          {
              result.put(PROP_ERRORMSG, "Test URL '"+testURL+"' is not a valid URL");
              result.put(PROP_GOOD, false);
          }
          result.write(resp.getWriter());
       } catch (JSONException e) {
          throw new IOException(e.getMessage(), e);
       }
    }

    private void testProxy(URI testURI, JSONObject result)
    {
       try {
          int responseCode = 0;
          try
          {
              URL testURL =testURI.toURL();

              HttpURLConnection connection = (HttpURLConnection) testURL.openConnection();
              connection.setRequestMethod("GET");
              connection.setUseCaches(false);
              connection.setInstanceFollowRedirects(true);
              connection.setDoInput(true);
              connection.setDoOutput(false);
              connection.setConnectTimeout(1000 * 15); // 15 second timeout
              connection.connect();
              responseCode = connection.getResponseCode();

              try {
                  if (responseCode == HttpURLConnection.HTTP_OK) {
                      InputStream is = connection.getInputStream();
                      BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                      while(rd.readLine() != null) {
                          // for now, just eat response
                      }
                      rd.close();
                  }
              } catch (IOException e)
              {
                  result.put(PROP_GOOD, false);
                  result.put(PROP_ERRORMSG, "Error communicating with proxy");
                  Logging.println("Error communicating with proxy", e);
              }

          } catch (SocketTimeoutException e) {
              result.put(PROP_GOOD, false);
              result.put(PROP_ERRORMSG, "Timeout while testing URL");
          } catch (ConnectException e) {
              result.put(PROP_GOOD, false);
              result.put(PROP_ERRORMSG, "Can not connect to URL");
          } catch (Throwable e) {
              result.put(PROP_GOOD, false);
              result.put(PROP_ERRORMSG, "Internal error while testing URL");
              Logging.println("Internal error while testing URL: "+testURI, e);
          }
          if (responseCode == HttpURLConnection.HTTP_OK) {
              result.put(PROP_GOOD, true);
          } else {
              result.put(PROP_GOOD, false);
              switch (responseCode) {
                  case 0:
                      break; // already handled in catch clauses above

                  case HttpURLConnection.HTTP_USE_PROXY:
                      result.put(PROP_ERRORMSG, "Proxy required");
                      break;

                  case HttpURLConnection.HTTP_PROXY_AUTH:
                      result.put(PROP_ERRORMSG, "Proxy authentication required.  Specify a username and password.");
                      break;

                  case HttpURLConnection.HTTP_UNAUTHORIZED:
                      result.put(PROP_ERRORMSG, "Error - Unauthorized");
                      break;

                  case HttpURLConnection.HTTP_NOT_FOUND:
                      result.put(PROP_ERRORMSG, "Test URL not found");
                      break;

                  default:
                      result.put(PROP_ERRORMSG, "Result code "+responseCode+" returned from test request.");
              }
          }
       } catch (JSONException e) {
          Logging.println("Internal error while testing URL", e);
       }
    }
}
