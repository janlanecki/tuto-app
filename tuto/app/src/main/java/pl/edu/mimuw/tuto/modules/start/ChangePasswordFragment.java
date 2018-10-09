package pl.edu.mimuw.tuto.modules.start;

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

import pl.edu.mimuw.tuto.R;
import pl.edu.mimuw.tuto.common.data.DataProvider;

import static pl.edu.mimuw.tuto.common.StringUtil.getHash;

public class ChangePasswordFragment extends Fragment implements View.OnClickListener {
  public final static String TAG = "change_password";

  private final static String SEND_CODE_URL = "https://.../send_reset_code.php";
  private final static String CHANGE_PASSWORD_URL = "https://.../change_password.php";
  private final static String EMAIL_KEY = TAG + "_email";
  private final static String CODE_KEY = TAG + "_code";
  private final static String PASSWORD_KEY = TAG + "_password";
  private final static String REPEAT_PASSWORD_KEY = TAG + "_repeat_password";

  // must be the same as in send_reset_code and change_password scripts
  private final static String HASHING_SALT = "...";

  private ProgressBar mProgressBar;
  private EditText mEmailField;
  private EditText mCodeField;
  private EditText mPasswordField;
  private EditText mRepeatPasswordField;
  private Button mSendCodeButton;
  private Button mChangePasswordButton;

  public static ChangePasswordFragment getInstance(FragmentManager fragmentManager) {
    ChangePasswordFragment fragment =
        (ChangePasswordFragment) fragmentManager.findFragmentByTag(ChangePasswordFragment.TAG);

    if (fragment == null) {
      fragment = new ChangePasswordFragment();
      Log.d(TAG, "New instance was created.");
    }
    return fragment;
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    outState.putString(EMAIL_KEY, mEmailField.getText().toString());
    outState.putString(CODE_KEY, mCodeField.getText().toString());
    outState.putString(PASSWORD_KEY, mPasswordField.getText().toString());
    outState.putString(REPEAT_PASSWORD_KEY, mRepeatPasswordField.getText().toString());
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.change_password_fragment, container, false);

    mProgressBar = rootView.findViewById(R.id.progressBar);

    mEmailField = rootView.findViewById(R.id.emailField);
    mCodeField = rootView.findViewById(R.id.codeField);
    mPasswordField = rootView.findViewById(R.id.passwordField);
    mRepeatPasswordField = rootView.findViewById(R.id.repeatPasswordField);

    mSendCodeButton = rootView.findViewById(R.id.sendCodeButton);
    mSendCodeButton.setOnClickListener(this);
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
      if (savedInstanceState.containsKey(CODE_KEY)) {
        mCodeField.setText(savedInstanceState.getString(CODE_KEY));
      }
      if (savedInstanceState.containsKey(PASSWORD_KEY)) {
        mPasswordField.setText(savedInstanceState.getString(PASSWORD_KEY));
      }
      if (savedInstanceState.containsKey(REPEAT_PASSWORD_KEY))
        mRepeatPasswordField.setText(savedInstanceState.getString(REPEAT_PASSWORD_KEY));
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
  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.sendCodeButton:
        handleSendCodeClick();
        break;
      case R.id.changePasswordButton:
        handleChangePasswordClick();
        break;
    }
  }

  private void handleSendCodeClick() {
    if (!mEmailField.getText().toString().isEmpty()) {
      sendSendCodeRequest();
    } else {
      Toast.makeText(getContext(), "Wprowadź adres email", Toast.LENGTH_SHORT).show();
    }
  }

  private void handleSendCode(String result) {
    switch (result) {
      case "OK":
        Toast.makeText(getContext(), "Kod wysłany!", Toast.LENGTH_SHORT).show();
        break;
      case "error bad email":
        Toast.makeText(getContext(), "Błędny adres email", Toast.LENGTH_SHORT).show();
        break;
      default: // "error"
        Toast.makeText(getContext(), "Wystąpił błąd spróbuj ponownie", Toast.LENGTH_SHORT).show();
        break;
    }
  }

  private void handleChangePasswordClick() {
    String password = mPasswordField.getText().toString();
    String repeatedPassword = mRepeatPasswordField.getText().toString();
    boolean accepted = false;

    if (password.isEmpty()) {
      Toast.makeText(getContext(), "Hasło nie może być puste.", Toast.LENGTH_SHORT).show();
    } else if (password.length() < 8) {
      Toast.makeText(getContext(), "Hasło musi mieć co najmniej 8 znaków.", Toast.LENGTH_SHORT).show();
    } else if (!password.matches(".*[0-9].*") || !password.matches(".*[A-ZĄĆĘŁŃÓŚŹŻ].*")) {
      Toast.makeText(getContext(), "Hasło musi zawierać wielką literę i cyfrę.", Toast.LENGTH_SHORT).show();
    } else if (!password.equals(repeatedPassword)) {
      Toast.makeText(getContext(), "Hasła się różnią.", Toast.LENGTH_SHORT).show();
    } else {
      accepted = true;
    }

    if (accepted) {
      sendChangePasswordRequest();
    }
  }

  private void handleChangePassword(String result) {
    switch (result) {
      case "OK":
        Toast.makeText(getContext(), "Hasło zmienione!", Toast.LENGTH_SHORT).show();
        getFragmentManager().popBackStackImmediate();
        break;
      case "error bad email":
        Toast.makeText(getContext(), "Błędny adres email", Toast.LENGTH_SHORT).show();
        break;
      case "error bad code":
        Toast.makeText(getContext(), "Błędny kod", Toast.LENGTH_SHORT).show();
        break;
      default:  // result.equals("error")
        Toast.makeText(getContext(), "Wystąpił błąd, spróbuj ponownie", Toast.LENGTH_SHORT).show();
        break;
    }
  }

  private void updateViews(boolean loading) {
    mProgressBar.setVisibility(loading ? View.VISIBLE : View.GONE);

    mSendCodeButton.setEnabled(!loading);
    mChangePasswordButton.setEnabled(!loading);
    mEmailField.setEnabled(!loading);
    mCodeField.setEnabled(!loading);
    mPasswordField.setEnabled(!loading);
    mRepeatPasswordField.setEnabled(!loading);
  }

  private void sendSendCodeRequest() {
    synchronized (this) {
      updateViews(true);

      String email = mEmailField.getText().toString();

      HashMap<String, String> params = new HashMap<>();
      params.put(EMAIL_KEY, email);

      JSONObject json = new JSONObject(params);

      JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
          (Request.Method.POST, SEND_CODE_URL, json, (JSONObject response) -> {
            try {
              handleSendCode(response.get("result").toString());
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

  private void sendChangePasswordRequest() {
    synchronized (this) {
      updateViews(true);

      String email = mEmailField.getText().toString();
      String code = mCodeField.getText().toString();
      String password = mPasswordField.getText().toString();

      HashMap<String, String> params = new HashMap<>();
      params.put(EMAIL_KEY, email);
      try {
        password = getHash(password);
        code = getHash(HASHING_SALT.concat(code));
      } catch (NoSuchAlgorithmException e) {
        Log.d(TAG, "Hashing error: " + e.getMessage());
      }
      params.put(PASSWORD_KEY, password);
      params.put(CODE_KEY, code);

      JSONObject json = new JSONObject(params);

      JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
          (Request.Method.POST, CHANGE_PASSWORD_URL, json, (JSONObject response) -> {
            try {
              handleChangePassword(response.get("result").toString());
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
