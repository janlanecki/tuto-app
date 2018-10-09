package pl.edu.mimuw.tuto.modules.tag_picker;

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
import java.util.List;

import pl.edu.mimuw.tuto.common.object.tag.Tag;

public class TagListRequest extends JsonRequest<List<TagWithParent>> {
  private final static String URL = "https://.../tag_tips_dev.php";

  private final Listener<List<TagWithParent>> listener;

  /**
   * Constructor used to create a request that contains POST parameters.
   *
   * @param postParams    JSON object with parameters.
   * @param listener      Listener called when request succeeds.
   * @param errorListener Listener called when request fails.
   */
  public TagListRequest(JSONObject postParams, Listener<List<TagWithParent>> listener, ErrorListener errorListener) {
    super(Method.POST, URL, (postParams == null) ? null : postParams.toString(), listener, errorListener);
    this.listener = listener;
  }

  @Override
  protected void deliverResponse(List<TagWithParent> response) {
    listener.onResponse(response);
  }

  @Override
  protected Response<List<TagWithParent>> parseNetworkResponse(NetworkResponse response) {
    try {
      Log.d("TagListResponse", new String(response.data));
      String json = new String(
          response.data,
          HttpHeaderParser.parseCharset(response.headers));
      JSONArray jsonArray = new JSONArray(json);
      List<TagWithParent> tags = new ArrayList<>(Arrays.asList(TagWithParent.fromJSONArray(jsonArray)));
      return Response.success(
          tags,
          HttpHeaderParser.parseCacheHeaders(response));
    } catch (UnsupportedEncodingException e) {
      return Response.error(new ParseError(e));
    } catch (JSONException e) {
      return Response.error(new ParseError(e));
    }
  }

}
