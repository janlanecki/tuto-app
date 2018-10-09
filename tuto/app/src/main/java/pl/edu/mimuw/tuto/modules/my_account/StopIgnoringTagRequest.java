package pl.edu.mimuw.tuto.modules.my_account;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;

import org.json.JSONObject;

import java.util.HashMap;

import pl.edu.mimuw.tuto.common.data.request.ActionRequest;

public class StopIgnoringTagRequest extends ActionRequest {
  private final static String URL = "https://.../unblock.php";

  public static StopIgnoringTagRequest newRequest(Listener<String> listener,
                                                  ErrorListener errorListener,
                                                  String userName,
                                                  int tagId) {
    HashMap<String, String> params = new HashMap<>();
    params.put("user", userName);
    params.put("tag", "" + tagId);

    return new StopIgnoringTagRequest(new JSONObject(params), listener, errorListener);
  }

  private StopIgnoringTagRequest(JSONObject postParams, Listener<String> listener, ErrorListener errorListener) {
    super(URL, postParams, listener, errorListener);
  }


  @Override
  protected String getRequestTag() {
    return "IgnoreTagRequest";
  }
}
