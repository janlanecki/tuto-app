package pl.edu.mimuw.tuto.modules.edit;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.NetworkError;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import pl.edu.mimuw.tuto.R;
import pl.edu.mimuw.tuto.common.data.DataProvider;
import pl.edu.mimuw.tuto.modules.creation.CreationDialogFragment;

public class EditDialogFragment extends CreationDialogFragment {
  private final static String editURL = "https://.../edit.php";
  private final static String detailsURL = "https://.../details.php?id=";

  private int id;
  private String title;
  private String date;
  private String time;
  private String duration;
  private String limit;
  private String place;
  private String description;

  public static EditDialogFragment getInstance(FragmentManager fragmentManager) {
    EditDialogFragment fragment =
        (EditDialogFragment) fragmentManager.findFragmentByTag(EditDialogFragment.TAG);

    if (fragment == null) {
      fragment = new EditDialogFragment();

      // Reuse this fragment instance during configuration changes(e.g. rotating the screen).
      fragment.setRetainInstance(true);
      Log.d(TAG, "New instance was created.");
    }

    return fragment;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = super.onCreateView(inflater, container, savedInstanceState);

    mCreateButton.setText(R.string.edit_button_confirmation);

    mTagButton.setOnClickListener(view -> {}); // TODO: temporarily disabled

    id = this.getArguments().getInt("id");
    getDetails();

    return rootView;
  }

  private void getDetails() {
    String url = detailsURL + id;

    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
        (Request.Method.GET, url, null, (JSONObject response) -> {
          try {
            title = response.getString("label");
            date = response.getString("date");
            time = response.getString("time");
            duration = response.getString("duration");
            limit = response.getString("limit");
            place = response.getString("place");
            description = response.getString("description");

            try {
              SimpleDateFormat simpleDateFormat = new SimpleDateFormat("d-M-yyyy");
              Date dateToConvert = simpleDateFormat.parse(date);
              simpleDateFormat.applyPattern("EEEE, d MMMM yyyy");
              date = simpleDateFormat.format(dateToConvert);
            } catch (ParseException e) {
              Log.e("sss", "Date fail...");
            }

            mTitleText.setText(title);
            mDateText.setText(date);
            mTimeText.setText(time);
            mDurationText.setText(duration);
            mPeopleLimitText.setText(limit);
            mPlaceText.setText(place);
            mDescriptionText.setText(description);
          } catch (org.json.JSONException e) {
            Log.d(TAG, "Response reading error: " + e.getMessage());
            Toast.makeText(getContext(), "Wystąpił błąd, spróbuj ponownie", Toast.LENGTH_SHORT).show();
          }
        }, (VolleyError error) -> {
          Log.d(TAG, "Response volley error: " + error.getMessage());
          if (error instanceof NetworkError) {
            Toast.makeText(getContext(), "Brak połączenia z siecią", Toast.LENGTH_SHORT).show();
          } else {
            Toast.makeText(getContext(), "Wystąpił błąd, spróbuj ponownie", Toast.LENGTH_SHORT).show();
          }
        });

    jsonObjectRequest.setTag(TAG);

    DataProvider.getInstance(getActivity().getApplicationContext())
        .addToRequestQueue(jsonObjectRequest);
  }

  @Override
  public void onClick(View view) {
    HashMap<String, String> jsonPrototype = new HashMap<>();

    if (!boxesExtract(jsonPrototype)) {
      Toast.makeText(getContext(), "Uzupełnij niektóre pola.", Toast.LENGTH_SHORT).show();
      return;
    }

    jsonPrototype.put("id", Integer.toString(id));

    if (jsonPrototype.get("title").equals(title)) {
      jsonPrototype.remove("title");
    }

    if (jsonPrototype.get("due_date").equals(date)) {
      jsonPrototype.remove("due_date");
    }

    if (jsonPrototype.get("due_time").equals(time)) {
      jsonPrototype.remove("due_time");
    }

    if (jsonPrototype.get("duration").equals(duration)) {
      jsonPrototype.remove("duration");
    }

    if (jsonPrototype.get("people_limit").equals(limit)) {
      jsonPrototype.remove("people_limit");
    }

    if (jsonPrototype.get("place").equals(place)) {
      jsonPrototype.remove("place");
    }

    if (jsonPrototype.get("description").equals(description)) {
      jsonPrototype.remove("description");
    }

    JSONObject jsonObject = new JSONObject(jsonPrototype);

    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
        (Request.Method.POST, editURL, jsonObject, (JSONObject response) -> {
          try {
            Log.e("QQQ", response.getString("comment"));
            if (response.getInt("result_code") == 0) {
              dismiss();
              Toast.makeText(getContext(), "Edytowano sesję.", Toast.LENGTH_SHORT).show();
            }
          } catch (org.json.JSONException e) {
            Log.d(TAG, "Response reading error: " + e.getMessage());
            Toast.makeText(getContext(), "Wystąpił błąd, spróbuj ponownie", Toast.LENGTH_SHORT).show();
          }
        }, (VolleyError error) -> {
          Log.d(TAG, "Response volley error: " + error.getMessage());
          if (error instanceof NetworkError) {
            Toast.makeText(getContext(), "Brak połączenia z siecią", Toast.LENGTH_SHORT).show();
          } else {
            Toast.makeText(getContext(), "Wystąpił błąd, spróbuj ponownie", Toast.LENGTH_SHORT).show();
          }
        });

    jsonObjectRequest.setTag(TAG);

    DataProvider.getInstance(getContext()).addToRequestQueue(jsonObjectRequest);
  }
}
