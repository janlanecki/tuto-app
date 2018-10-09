package pl.edu.mimuw.tuto.notifications;

import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;

public class NotificationsRequest extends JsonRequest<NotificationsResponse> {
  private final static String URL = "https://.../check.php";

  private final Listener<NotificationsResponse> listener;

  /**
   * Constructor used to create a request that contains POST parameters.
   *
   * @param postParams    JSON object with parameters.
   * @param listener      Listener called when request succeeds.
   * @param errorListener Listener called when request fails.
   */
  public NotificationsRequest(JSONObject postParams, Listener<NotificationsResponse> listener, ErrorListener errorListener) {
    super(Method.POST, URL, (postParams == null) ? null : postParams.toString(), listener, errorListener);
    this.listener = listener;
  }

  @Override
  protected void deliverResponse(NotificationsResponse response) {
    listener.onResponse(response);
  }

  @Override
  protected Response<NotificationsResponse> parseNetworkResponse(NetworkResponse response) {
    try {
      Log.d("NotificationsResponse", new String(response.data));
      String json = new String(
          response.data,
          HttpHeaderParser.parseCharset(response.headers));
      JSONObject object = new JSONObject(json);

      long serverTime = object.getLong("server_time");
      JSONArray notifications = object.getJSONArray("notifications");

      ArrayList<Notification> list =
          new ArrayList<>(Arrays.asList(Notification.fromJSONArray(notifications)));

      NotificationsResponse result = new NotificationsResponse(serverTime, list);
      return Response.success(
          result,
          HttpHeaderParser.parseCacheHeaders(response));
    } catch (UnsupportedEncodingException e) {
      return Response.error(new ParseError(e));
    } catch (JSONException e) {
      return Response.error(new ParseError(e));
    }
  }

}
