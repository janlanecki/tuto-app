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

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import pl.edu.mimuw.tuto.MainActivity;
import pl.edu.mimuw.tuto.R;
import pl.edu.mimuw.tuto.common.data.DataProvider;

import static pl.edu.mimuw.tuto.common.StringUtil.getHash;

public class LoginFragment extends Fragment implements View.OnClickListener {
  public final static String TAG = "login";

  private final static String URL = "https://.../login.php";
  private final static String EMAIL_KEY = "email";
  private final static String PASSWORD_KEY = "password";

  private ProgressBar mProgressBar;
  private EditText mEmailField;
  private EditText mPasswordField;
  private Button mLoginButton;
  private Button mSignupButton;
  private Button mChangePasswordButton;

  public static LoginFragment getInstance(FragmentManager fragmentManager) {
    LoginFragment fragment = (LoginFragment) fragmentManager.findFragmentByTag(LoginFragment.TAG);

    if (fragment == null) {
      fragment = new LoginFragment();
      Log.d(TAG, "New instance was created.");
    }
    return fragment;
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    outState.putString(EMAIL_KEY, mEmailField.getText().toString());
    outState.putString(PASSWORD_KEY, mPasswordField.getText().toString());
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.login_fragment, container, false);

    mProgressBar = rootView.findViewById(R.id.progressBar);

    mEmailField = rootView.findViewById(R.id.emailField);
    mPasswordField = rootView.findViewById(R.id.passwordField);

    mLoginButton = rootView.findViewById(R.id.loginButton);
    mLoginButton.setOnClickListener(this);
    mSignupButton = rootView.findViewById(R.id.loginSignupButton);
    mSignupButton.setOnClickListener(this);
    mChangePasswordButton = rootView.findViewById(R.id.changePasswordButton);
    mChangePasswordButton.setOnClickListener(this);

    return rootView;
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    if (savedInstanceState != null) {
      if (savedInstanceState.containsKey(EMAIL_KEY)) {
        mEmailField.setText(savedInstanceState.getString(EMAIL_KEY));
      }
      if (savedInstanceState.containsKey(PASSWORD_KEY)) {
        mPasswordField.setText(savedInstanceState.getString(PASSWORD_KEY));
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
    switch (v.getId()) {
      case R.id.loginButton:
        sendLoginRequest();
        break;

      case R.id.loginSignupButton:
        handleSignup();
        break;

      case R.id.changePasswordButton:
        handleChangePassword();
        break;
    }
  }

  private void updateViews(boolean logging) {
    mProgressBar.setVisibility(logging ? View.VISIBLE : View.GONE);

    mLoginButton.setEnabled(!logging);
    mEmailField.setEnabled(!logging);
    mPasswordField.setEnabled(!logging);
  }

  private void logIn(String email) {
    Activity activity = getActivity();
    if (activity instanceof MainActivity) {
      ((MainActivity) activity).logInAs(email);
    } else {
      // Should not happen.
      throw new RuntimeException("LoginFragment.getActivity() is not an instance of MainActivity.");
    }
  }

  private void handleLogin(String email, String result) {
    switch (result) {
      case "OK":
        logIn(email);
        break;
      case "email":
        Toast.makeText(getContext(), getResources().getString(R.string.invalid_email), Toast.LENGTH_SHORT).show();
        break;
      case "password":
        Toast.makeText(getContext(), getResources().getString(R.string.invalid_password), Toast.LENGTH_LONG).show();
        break;
      default:
        Toast.makeText(getContext(), getResources().getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
        break;
    }
  }

  private void handleSignup() {
    Activity activity = getActivity();
    if (activity instanceof MainActivity) {
      ((MainActivity) activity).showSignupFragment();
    } else {
      // Should not happen.
      throw new RuntimeException("LoginFragment.getActivity() is not an instance of MainActivity.");
    }
  }

  private void handleChangePassword() {
    Activity activity = getActivity();
    if (activity instanceof MainActivity) {
      ((MainActivity) activity).showChangePasswordFragment();
    } else {
      // Should not happen.
      throw new RuntimeException("LoginFragment.getActivity() is not an instance of MainActivity.");
    }
  }

  private void sendLoginRequest() {
    synchronized (this) {
      updateViews(true);

      String email = mEmailField.getText().toString();
      String password = mPasswordField.getText().toString();

      HashMap<String, String> params = new HashMap<>();
      params.put(EMAIL_KEY, email);
      try {
        password = getHash(password);
      } catch (NoSuchAlgorithmException e) {
        Log.d(TAG, "Hashing error: " + e.getMessage());
      }
      params.put(PASSWORD_KEY, password);

      JSONObject json = new JSONObject(params);

      JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
          (Request.Method.POST, URL, json, (JSONObject response) -> {
            try {
              handleLogin(email, response.get("result").toString());
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
