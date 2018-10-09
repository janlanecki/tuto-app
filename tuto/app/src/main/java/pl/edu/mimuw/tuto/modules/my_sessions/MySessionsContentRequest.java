package pl.edu.mimuw.tuto.modules.my_sessions;

import android.annotation.SuppressLint;
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

import pl.edu.mimuw.tuto.common.object.session.Session;

public class MySessionsContentRequest extends JsonRequest<ArrayList<Session>> {

  private final Listener<ArrayList<Session>> listener;

  /**
   * Constructor used to create a request that contains POST parameters.
   *
   * @param postParams    JSON object with parameters.
   * @param listener      Listener called when request succeeds.
   * @param errorListener Listener called when request fails.
   */
  public MySessionsContentRequest(JSONObject postParams, Listener<ArrayList<Session>> listener,
                                  ErrorListener errorListener, String URL) {
    super(Method.POST, URL, (postParams == null) ? null : postParams.toString(), listener, errorListener);
    this.listener = listener;
  }

  @Override
  protected void deliverResponse(ArrayList<Session> response) {
    listener.onResponse(response);
  }

  @SuppressLint("LongLogTag")
  @Override
  protected Response<ArrayList<Session>> parseNetworkResponse(NetworkResponse response) {
    try {
      Log.d("MySessionsContentResponse", new String(response.data));
      String json = new String(
          response.data,
          HttpHeaderParser.parseCharset(response.headers));
      JSONArray jsonArray = new JSONArray(json);
      ArrayList<Session> sessions = new ArrayList<>(Arrays.asList(Session.fromJSONArray(jsonArray)));
      return Response.success(
          sessions,
          HttpHeaderParser.parseCacheHeaders(response));
    } catch (UnsupportedEncodingException e) {
      return Response.error(new ParseError(e));
    } catch (JSONException e) {
      return Response.error(new ParseError(e));
    }
  }

}
