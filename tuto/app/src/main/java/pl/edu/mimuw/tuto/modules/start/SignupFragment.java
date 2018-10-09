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
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.regex.Pattern;

import pl.edu.mimuw.tuto.MainActivity;
import pl.edu.mimuw.tuto.R;
import pl.edu.mimuw.tuto.common.data.DataProvider;

import pl.edu.mimuw.tuto.common.StringUtil;

public class SignupFragment extends Fragment implements View.OnClickListener {
  public final static String TAG = "signup";

  private final static String SIGNUP_URL = "https://.../signup.php";
  private final static String RESET_URL = "https://.../reset.php";

  // RFC822 compliant regular expression describing email address
  private final static Pattern EMAIL_PATTERN = Pattern.compile("(?:(?:\\r\\n)?[ \\t])*(?:(?:(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*|(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)*\\<(?:(?:\\r\\n)?[ \\t])*(?:@(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*(?:,@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*)*:(?:(?:\\r\\n)?[ \\t])*)?(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*\\>(?:(?:\\r\\n)?[ \\t])*)|(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)*:(?:(?:\\r\\n)?[ \\t])*(?:(?:(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*|(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)*\\<(?:(?:\\r\\n)?[ \\t])*(?:@(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*(?:,@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*)*:(?:(?:\\r\\n)?[ \\t])*)?(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*\\>(?:(?:\\r\\n)?[ \\t])*)(?:,\\s*(?:(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*|(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)*\\<(?:(?:\\r\\n)?[ \\t])*(?:@(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*(?:,@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*)*:(?:(?:\\r\\n)?[ \\t])*)?(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*\\>(?:(?:\\r\\n)?[ \\t])*))*)?;\\s*)");

  private final static String ACTIVATION_CODE_KEY = TAG + "_code";
  private final static String NAME_KEY = TAG + "_name";
  private final static String SURNAME_KEY = TAG + "_surname";
  private final static String EMAIL_KEY = TAG + "_email";
  private final static String PASSWORD_KEY = TAG + "_password";
  private final static String REPEAT_PASSWORD_KEY = TAG + "_repeat_password";
  private final static String NAME_TIP_KEY = TAG + "_name_tip";
  private final static String SURNAME_TIP_KEY = TAG + "_surname_tip";
  private final static String EMAIL_TIP_KEY = TAG + "_email_tip";
  private final static String PASSWORD_TIP_KEY = TAG + "_password_tip";
  private final static String REPEAT_PASSWORD_TIP_KEY = TAG + "_repeat_password_tip";
  
  private ProgressBar mProgressBar;
  private EditText mNameField;
  private EditText mSurnameField;
  private EditText mEmailField;
  private EditText mPasswordField;
  private EditText mRepeatPasswordField;
  private TextView mNameTip;
  private TextView mSurnameTip;
  private TextView mEmailTip;
  private TextView mPasswordTip;
  private TextView mRepeatPasswordTip;
  private Button mSignupButton;
  private Button mResendLinkButton;

  public static SignupFragment getInstance(FragmentManager fragmentManager) {
    SignupFragment fragment = (SignupFragment) fragmentManager.findFragmentByTag(SignupFragment.TAG);

    if (fragment == null) {
      fragment = new SignupFragment();
      Log.d(TAG, "New instance was created.");
    }
    return fragment;
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    outState.putString(NAME_KEY, mNameField.getText().toString());
    outState.putString(SURNAME_KEY, mSurnameField.getText().toString());
    outState.putString(EMAIL_KEY, mEmailField.getText().toString());
    outState.putString(PASSWORD_KEY, mPasswordField.getText().toString());
    outState.putString(REPEAT_PASSWORD_KEY, mRepeatPasswordField.getText().toString());

    if (mNameTip.getVisibility() == View.VISIBLE)
      outState.putString(NAME_TIP_KEY, mNameTip.getText().toString());
    if (mSurnameTip.getVisibility() == View.VISIBLE)
      outState.putString(SURNAME_TIP_KEY, mSurnameTip.getText().toString());
    if (mEmailTip.getVisibility() == View.VISIBLE)
      outState.putString(EMAIL_TIP_KEY, mEmailTip.getText().toString());
    if (mPasswordTip.getVisibility() == View.VISIBLE)
      outState.putString(PASSWORD_TIP_KEY, mPasswordTip.getText().toString());
    if (mRepeatPasswordTip.getVisibility() == View.VISIBLE)
      outState.putString(REPEAT_PASSWORD_TIP_KEY, mRepeatPasswordTip.getText().toString());
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.signup_fragment, container, false);

    mProgressBar = rootView.findViewById(R.id.progressBar);
    mProgressBar.setVisibility(View.GONE);

    mNameField = rootView.findViewById(R.id.nameField);
    mSurnameField = rootView.findViewById(R.id.surnameField);
    mEmailField = rootView.findViewById(R.id.emailField);
    mPasswordField = rootView.findViewById(R.id.passwordField);
    mRepeatPasswordField = rootView.findViewById(R.id.repeatPasswordField);
    mSignupButton = rootView.findViewById(R.id.signupButton);
    mSignupButton.setOnClickListener(this);
    mResendLinkButton = rootView.findViewById(R.id.resendLinkButton);
    mResendLinkButton.setOnClickListener(this);

    mNameTip = rootView.findViewById(R.id.nameTipText);
    mNameTip.setVisibility(View.GONE);
    mSurnameTip = rootView.findViewById(R.id.surnameTipText);
    mSurnameTip.setVisibility(View.GONE);
    mEmailTip = rootView.findViewById(R.id.emailTipText);
    mEmailTip.setVisibility(View.GONE);
    mPasswordTip = rootView.findViewById(R.id.passwordTipText);
    mPasswordTip.setVisibility(View.GONE);
    mRepeatPasswordTip = rootView.findViewById(R.id.repeatPasswordTipText);
    mRepeatPasswordTip.setVisibility(View.GONE);

    return rootView;
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    if (savedInstanceState != null) {
      if (savedInstanceState.containsKey(NAME_KEY)) {
        mNameField.setText(savedInstanceState.getString(NAME_KEY));
      }
      if (savedInstanceState.containsKey(SURNAME_KEY)) {
        mSurnameField.setText(savedInstanceState.getString(SURNAME_KEY));
      }
      if (savedInstanceState.containsKey(EMAIL_KEY)) {
        mEmailField.setText(savedInstanceState.getString(EMAIL_KEY));
      }
      if (savedInstanceState.containsKey(PASSWORD_KEY)) {
        mPasswordField.setText(savedInstanceState.getString(PASSWORD_KEY));
      }
      if (savedInstanceState.containsKey(REPEAT_PASSWORD_KEY)) {
        mRepeatPasswordField.setText(savedInstanceState.getString(REPEAT_PASSWORD_KEY));
      }

      if (!savedInstanceState.containsKey(NAME_TIP_KEY)) {
        mNameTip.setVisibility(View.GONE);
      }
      else {
        mNameTip.setText(savedInstanceState.getString(NAME_TIP_KEY));
        mNameTip.setVisibility(View.VISIBLE);
      }

      if (!savedInstanceState.containsKey(SURNAME_TIP_KEY)) {
        mSurnameTip.setVisibility(View.GONE);
      }
      else {
        mSurnameTip.setText(savedInstanceState.getString(SURNAME_TIP_KEY));
        mSurnameTip.setVisibility(View.VISIBLE);
      }

      if (!savedInstanceState.containsKey(EMAIL_TIP_KEY)) {
        mEmailTip.setVisibility(View.GONE);
      }
      else {
        mEmailTip.setText(savedInstanceState.getString(EMAIL_TIP_KEY));
        mEmailTip.setVisibility(View.VISIBLE);
      }

      if (!savedInstanceState.containsKey(PASSWORD_TIP_KEY)) {
        mPasswordTip.setVisibility(View.GONE);
      }
      else {
        mPasswordTip.setText(savedInstanceState.getString(PASSWORD_TIP_KEY));
        mPasswordTip.setVisibility(View.VISIBLE);
      }

      if (!savedInstanceState.containsKey(REPEAT_PASSWORD_TIP_KEY)) {
        mRepeatPasswordTip.setVisibility(View.GONE);
      }
      else {
        mRepeatPasswordTip.setText(savedInstanceState.getString(REPEAT_PASSWORD_TIP_KEY));
        mRepeatPasswordTip.setVisibility(View.VISIBLE);
      }
    }
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

  private boolean correctName() {
    String name = mNameField.getText().toString();

    if (name.isEmpty()) {
      mNameTip.setText("To pole jest obowiązkowe.");
    } else if (!name.matches("^((?![0-9_])[\\w])+((?![0-9_])[\\w\\- ])*$")) {
      mNameTip.setText("Imię może się składać jedynie z liter, myślników i odstępów oraz musi " +
          "zaczynać się od litery.");
    } else {
      mNameField.setText(StringUtil.toNameCase(name));
      return true;
    }

    mNameTip.setVisibility(View.VISIBLE);
    return false;
  }

  private boolean correctSurname() {
    String surname = mSurnameField.getText().toString();
    if (surname.isEmpty()) {
      mSurnameTip.setText("To pole jest obowiązkowe.");
    } else if (!surname.matches("^((?![0-9_])[\\w])+((?![0-9_])[\\w\\- ])*$")) {
      mSurnameTip.setText("Nazwisko może się składać jedynie z liter, myślników i odstępów oraz musi " +
          "zaczynać się od litery.");
    } else {
      mSurnameField.setText(StringUtil.toNameCase(surname));
      return true;
    }

    mSurnameTip.setVisibility(View.VISIBLE);
    return false;
  }

  private boolean correctEmail() {
    String email = mEmailField.getText().toString();
    if (email.isEmpty()) {
      mEmailTip.setText("To pole jest obowiązkowe.");
    } else if (!EMAIL_PATTERN.matcher(email).matches() || !email.matches("^([a-z]{2}[0-9]{6}@students.mimuw.edu.pl)|(.{1,64}@mimuw\\.edu\\.pl)$")) {
      mEmailTip.setText("E-mail nieprawidłowy.");
    } else {
      return true;
    }

    mEmailTip.setVisibility(View.VISIBLE);
    return false;
  }

  private boolean correctPassword() {
    String password = mPasswordField.getText().toString();
    String repeatedPassword = mRepeatPasswordField.getText().toString();

    if (password.isEmpty()) {
      mPasswordTip.setText("To pole jest obowiązkowe.");
      mPasswordTip.setVisibility(View.VISIBLE);
    } else if (password.length() < 8) {
      mPasswordTip.setText("Hasło ma mniej niż 8 znaków.");
      mPasswordTip.setVisibility(View.VISIBLE);
    } else if (!password.matches(".*[0-9].*") || !password.matches(".*[A-ZĄĆĘŁŃÓŚŹŻ].*")) {
      mPasswordTip.setText("Hasło nie spełnia poniższych wymagań.");
      mPasswordTip.setVisibility(View.VISIBLE);
    } else if (!password.equals(repeatedPassword)) {
      mRepeatPasswordTip.setText("Hasła się różnią.");
      mRepeatPasswordTip.setVisibility(View.VISIBLE);
    } else {
      return true;
    }

    return false;
  }

  private void handleSignup() {
    updateViews(true);
    setTipsInvisible();

    boolean everythingCorrect = true;
    everythingCorrect &= correctName();
    everythingCorrect &= correctSurname();
    everythingCorrect &= correctEmail();
    everythingCorrect &= correctPassword();

    if (everythingCorrect) {
      sendSignupRequest();
    } else {
      updateViews(false);
    }
  }

  private void handleResendLink() {
    Activity activity = getActivity();
    if (activity instanceof MainActivity) {
      ((MainActivity) activity).showResendLinkFragment();
    } else {
      // Should not happen.
      throw new RuntimeException("LoginFragment.getActivity() is not an instance of MainActivity.");
    }
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.signupButton:
        handleSignup();
        break;

      case R.id.resendLinkButton:
        handleResendLink();
        break;
    }
  }

  private void handleSignup(String result) {
    switch (result) {
      case "OK":
        Toast.makeText(getContext(), "Link aktywacyjny został wysłany na Twój adres email!", Toast.LENGTH_LONG).show();
        getFragmentManager().popBackStackImmediate();
        break;
      case "error already inactive":
        mEmailTip.setText("Konto z podanym adresem email już oczekuje na aktywację.");
        mEmailTip.setVisibility(View.VISIBLE);
        break;
      case "error email taken":
        mEmailTip.setText("Konto z podanym adresem email już istnieje.");
        mEmailTip.setVisibility(View.VISIBLE);
        break;
      default: // "error"
        Toast.makeText(getContext(), "Wystąpił błąd, spróbuj ponownie", Toast.LENGTH_SHORT).show();
        break;
    }
  }

  private void sendSignupRequest() {
    synchronized (this) {
      String name = mNameField.getText().toString();
      String surname = mSurnameField.getText().toString();
      String email = mEmailField.getText().toString();
      String password = mPasswordField.getText().toString();
      String activationCode = "";

      HashMap<String, String> params = new HashMap<>();
      params.put(NAME_KEY, name);
      params.put(SURNAME_KEY, surname);
      params.put(EMAIL_KEY, email);

      try {
        password = StringUtil.getHash(password);
        //activationCode = StringUtil.generateCode(ACTIVATION_CODE_LENGTH);
      } catch (NoSuchAlgorithmException e) {
        Log.d(TAG, "No such algorithm exception: " + e.getMessage());
        Toast.makeText(getContext(), "Operacja niewspierana przez twoje urządzenie", Toast.LENGTH_LONG).show();
        return;
      }
      params.put(PASSWORD_KEY, password);
      params.put(ACTIVATION_CODE_KEY, activationCode);
      
      JSONObject json = new JSONObject(params);

      JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
          (Request.Method.POST, SIGNUP_URL, json, (JSONObject response) -> {
            try {
              handleSignup(response.get("result").toString());
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

      updateViews(true);
      DataProvider.getInstance(getContext()).addToRequestQueue(jsonObjectRequest);
    }
  }

  private void updateViews(boolean loading) {
    mProgressBar.setVisibility(loading ? View.VISIBLE : View.GONE);

    mSignupButton.setEnabled(!loading);
    mResendLinkButton.setEnabled(!loading);
    mNameField.setEnabled(!loading);
    mSurnameField.setEnabled(!loading);
    mEmailField.setEnabled(!loading);
    mPasswordField.setEnabled(!loading);
    mRepeatPasswordField.setEnabled(!loading);
  }

  private void setTipsInvisible() {
    mNameTip.setVisibility(View.GONE);
    mSurnameTip.setVisibility(View.GONE);
    mEmailTip.setVisibility(View.GONE);
    mPasswordTip.setVisibility(View.GONE);
    mRepeatPasswordTip.setVisibility(View.GONE);
  }
}
