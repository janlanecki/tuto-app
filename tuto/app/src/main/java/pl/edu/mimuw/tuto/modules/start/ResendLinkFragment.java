package pl.edu.mimuw.tuto.modules.start;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.NetworkError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.HashMap;

import pl.edu.mimuw.tuto.MainActivity;
import pl.edu.mimuw.tuto.R;
import pl.edu.mimuw.tuto.common.data.DataProvider;

public class ResendLinkFragment extends Fragment implements View.OnClickListener {
  public final static String TAG = "resend_link";

  private static final String URL = "https://.../resend_link.php";

  private static final String EMAIL_KEY = TAG + "_email";

  private ProgressBar mProgressBar;
  private EditText mEmailField;
  private Button mButton;

  public static ResendLinkFragment getInstance(FragmentManager fragmentManager) {
    ResendLinkFragment fragment = (ResendLinkFragment) fragmentManager.findFragmentByTag(ResendLinkFragment.TAG);

    if (fragment == null) {
      fragment = new ResendLinkFragment();
      Log.d(TAG, "New instance was created.");
    }
    return fragment;
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    outState.putString(EMAIL_KEY, mEmailField.getText().toString());
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.resend_link_fragment, container, false);

    mProgressBar = rootView.findViewById(R.id.progressBar);

    mEmailField = rootView.findViewById(R.id.emailField);

    mButton = rootView.findViewById(R.id.button);
    mButton.setOnClickListener(this);

    return rootView;
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    if (savedInstanceState != null) {
      if (savedInstanceState.containsKey(EMAIL_KEY)) {
        mEmailField.setText(savedInstanceState.getString(EMAIL_KEY));
      }
    }

    updateViews(false);
  }

  @Override
  public void onStop() {
    super.onStop();
    // Cancel all requests created by this fragment before it is destroyed.
    RequestQueue mRequestQueue =
        DataProvider.getInstance(getActivity().getApplicationContext()).getRequestQueue();
    if (mRequestQueue != null) {
      mRequestQueue.cancelAll(TAG);
    }
  }

  @Override
  public void onClick(View v) {
    if (mEmailField.getText().toString().isEmpty()) {
      Toast.makeText(getContext(), "Wprowadź adres email", Toast.LENGTH_SHORT).show();
    } else {
      sendResendRequest();
    }
  }

  private void updateViews(boolean loading) {
    mProgressBar.setVisibility(loading ? View.VISIBLE : View.GONE);

    mButton.setEnabled(!loading);
    mEmailField.setEnabled(!loading);
  }

  private void handleResend(String result) {
    switch (result) {
      case "OK":
        Toast.makeText(getContext(), "Link wysłany!", Toast.LENGTH_SHORT).show();
        break;
      case "error bad email":
        Toast.makeText(getContext(), "Konto z podanym adresem email nie oczekuje na aktywację", Toast.LENGTH_LONG).show();
        break;
      default: // "error"
        Toast.makeText(getContext(), "Wystąpił błąd, spróbuj ponownie", Toast.LENGTH_SHORT).show();
        break;
    }
  }

  private void sendResendRequest() {
    synchronized (this) {
      updateViews(true);

      String email = mEmailField.getText().toString();

      HashMap<String, String> params = new HashMap<>();
      params.put(EMAIL_KEY, email);

      JSONObject json = new JSONObject(params);

      JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
          (Request.Method.POST, URL, json, (JSONObject response) -> {
            try {
              handleResend(response.get("result").toString());
            } catch (org.json.JSONException e) {
              Log.d(TAG, "Response reading error: " + e.getMessage());
              Toast.makeText(getContext(), "Wystąpił błąd, spróbuj ponownie", Toast.LENGTH_SHORT).show();
            }

            updateViews(false);
          }, (VolleyError error) -> {
            Log.d(TAG, "Response volley error: " + error.getMessage());
            if (error instanceof NetworkError) {
              Toast.makeText(getContext(), "Brak połączenia z siecią", Toast.LENGTH_SHORT).show();
            } else {
              Toast.makeText(getContext(), "Wystąpił błąd, spróbuj ponownie", Toast.LENGTH_SHORT).show();
            }

            updateViews(false);
          });

      jsonObjectRequest.setTag(TAG);

      DataProvider.getInstance(getContext()).addToRequestQueue(jsonObjectRequest);
    }
  }
}
