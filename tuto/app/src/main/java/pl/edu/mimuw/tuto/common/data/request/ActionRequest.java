package pl.edu.mimuw.tuto.common.data.request;

import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public abstract class ActionRequest extends JsonRequest<String> {
  protected abstract String getRequestTag();

  private final Listener<String> listener;

  /**
   * Constructor used to create a request that contains POST parameters.
   *
   * @param postParams    JSON object with parameters.
   * @param listener      Listener called when request succeeds.
   * @param errorListener Listener called when request fails.
   */
  public ActionRequest(String URL, JSONObject postParams, Listener<String> listener, Response.ErrorListener errorListener) {
    super(Request.Method.POST, URL, (postParams == null) ? null : postParams.toString(), listener, errorListener);
    this.listener = listener;
  }

  @Override
  protected void deliverResponse(String response) {
    listener.onResponse(response);
  }

  @Override
  protected Response<String> parseNetworkResponse(NetworkResponse response) {
    try {
      Log.d(getRequestTag(), new String(response.data));
      String json = new String(
          response.data,
          HttpHeaderParser.parseCharset(response.headers));
      JSONObject object = new JSONObject(json);
      String result = object.getString("result");
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
