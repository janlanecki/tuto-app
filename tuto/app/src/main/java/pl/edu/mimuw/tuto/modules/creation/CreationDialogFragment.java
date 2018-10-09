package pl.edu.mimuw.tuto.modules.creation;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkError;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import pl.edu.mimuw.tuto.MainActivity;
import pl.edu.mimuw.tuto.R;
import pl.edu.mimuw.tuto.common.data.DataProvider;
import pl.edu.mimuw.tuto.common.object.tag.Tag;
import pl.edu.mimuw.tuto.framework.views.TagListView;
import pl.edu.mimuw.tuto.modules.tag_picker.TagPickerDialogFragment;

public class CreationDialogFragment extends DialogFragment
    implements View.OnClickListener, TagPickerDialogFragment.TagPickerUser {
  public final static String TAG = "CreationDialogFragment";

  private final static String URL = "https://.../create.php";
  protected Button mCreateButton;
  protected TextView mTagButton;
  protected TagListView mTagList;
  protected EditText mTitleText;
  protected EditText mDateText;
  protected EditText mTimeText;
  protected EditText mDurationText;
  protected EditText mPeopleLimitText;
  protected EditText mPlaceText;
  protected EditText mDescriptionText;

  public static CreationDialogFragment getInstance(FragmentManager fragmentManager) {
    CreationDialogFragment fragment =
        (CreationDialogFragment) fragmentManager.findFragmentByTag(CreationDialogFragment.TAG);

    if (fragment == null) {
      fragment = new CreationDialogFragment();

      // Reuse this fragment instance during configuration changes(e.g. rotating the screen).
      fragment.setRetainInstance(true);
      Log.d(TAG, "New instance was created.");
    }

    return fragment;
  }

  @Override
  public void onStart() {
    super.onStart();
    Dialog dialog = getDialog();
    dialog.getWindow()
        .setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT);
    dialog.getWindow().getAttributes().dimAmount = 0.6f;
    dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    dialog.setCanceledOnTouchOutside(true);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.creation_dialog_fragment, container, false);

    mCreateButton = rootView.findViewById(R.id.createButton);
    mCreateButton.setOnClickListener(this);

    mTagList = rootView.findViewById(R.id.tagList);

    mTagButton = rootView.findViewById(R.id.tagButton);
    mTagButton.setOnClickListener(view -> {
      DialogFragment dialog = TagPickerDialogFragment.newInstance(mTagList.getPickedTags(), CreationDialogFragment.TAG);
      dialog.show(getFragmentManager(), TagPickerDialogFragment.TAG);
    });

    mTitleText = rootView.findViewById(R.id.title_text);

    Calendar calendar = Calendar.getInstance();

    mDateText = rootView.findViewById(R.id.dateText);
    mDateText.setOnFocusChangeListener((view, focused) -> {
      if (focused) {
        showDatePickerDialog(calendar);
      }
    });
    mDateText.setOnClickListener(view -> showDatePickerDialog(calendar));
    mDateText.setInputType(InputType.TYPE_NULL);

    mTimeText = rootView.findViewById(R.id.timeText);
    mTimeText.setOnFocusChangeListener((view, focused) -> {
      if (focused) {
        showTimePickerDialog(calendar);
      }
    });
    mTimeText.setOnClickListener(view -> showTimePickerDialog(calendar));
    mTimeText.setInputType(InputType.TYPE_NULL);

    mDurationText = rootView.findViewById(R.id.durationText);
    mDurationText.setOnFocusChangeListener((view, focused) -> {
      if (focused) {
        DurationPickerDialogFragment durationPickerDialogFragment = new DurationPickerDialogFragment();
        durationPickerDialogFragment.setmDurationText(mDurationText);
        durationPickerDialogFragment.show(getFragmentManager(), DurationPickerDialogFragment.TAG);
      }
    });
    mDurationText.setOnClickListener((view) -> {
      DurationPickerDialogFragment durationPickerDialogFragment = new DurationPickerDialogFragment();
      durationPickerDialogFragment.setmDurationText(mDurationText);
      durationPickerDialogFragment.show(getFragmentManager(), DurationPickerDialogFragment.TAG);
    });
    mDurationText.setInputType(InputType.TYPE_NULL);

    mPeopleLimitText = rootView.findViewById(R.id.peopleLimitText);
    mPeopleLimitText.setOnFocusChangeListener((view, focused) -> {
      if (focused) {
        PeopleLimitPickerDialogFragment peopleLimitPickerDialogFragment = new PeopleLimitPickerDialogFragment();
        peopleLimitPickerDialogFragment.setmPeopleLimitText(mPeopleLimitText);
        peopleLimitPickerDialogFragment.show(getFragmentManager(), PeopleLimitPickerDialogFragment.TAG);
      }
    });
    mPeopleLimitText.setOnClickListener((view) -> {
      PeopleLimitPickerDialogFragment peopleLimitPickerDialogFragment = new PeopleLimitPickerDialogFragment();
      peopleLimitPickerDialogFragment.setmPeopleLimitText(mPeopleLimitText);
      peopleLimitPickerDialogFragment.show(getFragmentManager(), PeopleLimitPickerDialogFragment.TAG);
    });
    mPeopleLimitText.setInputType(InputType.TYPE_NULL);

    mPlaceText = rootView.findViewById(R.id.place_text);
    mDescriptionText = rootView.findViewById(R.id.description_text);

    return rootView;
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {  super.onSaveInstanceState(outState);  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {  super.onActivityCreated(savedInstanceState);  }

  /*Needed because the dialog failed on rotation*/
  @Override
  public void onDestroyView() {
    Dialog dialog = getDialog();
    if (dialog != null && getRetainInstance()) {
      dialog.setDismissMessage(null);
    }
    super.onDestroyView();
  }

  private void showDatePickerDialog(Calendar calendar) {
    final int year = calendar.get(Calendar.YEAR);
    final int month = calendar.get(Calendar.MONTH);
    final int day = calendar.get(Calendar.DAY_OF_MONTH);

    DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
        (subview, pickedYear, pickedMonth, pickedDay) -> {
          calendar.set(pickedYear, pickedMonth, pickedDay);

          DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.FULL);

          mDateText.setText(dateFormat.format(calendar.getTime()));
        }, year, month, day);

    datePickerDialog.getDatePicker().setSpinnersShown(false);
    datePickerDialog.getDatePicker().setCalendarViewShown(true);
    datePickerDialog.show();
  }

  private void showTimePickerDialog(Calendar calendar) {
    final int hour = calendar.get(Calendar.HOUR_OF_DAY);
    final int minute = calendar.get(Calendar.MINUTE);

    TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
        (subview, pickedHour, pickedMinute) -> {
          calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
              calendar.get(Calendar.DAY_OF_MONTH), pickedHour, pickedMinute);

          DateFormat dateFormat = DateFormat.getTimeInstance(DateFormat.SHORT);

          mTimeText.setText(dateFormat.format(calendar.getTime()));
        }, hour, minute, true);


    timePickerDialog.show();
  }

  @Override
  @NonNull
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    Dialog dialog = new Dialog(getContext(), R.style.AppTheme);
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    return dialog;
  }

  @Override
  public void onClick(View view) {
    HashMap<String, String> jsonPrototype = new HashMap<>();

    if (!boxesExtract(jsonPrototype)) {
      Toast.makeText(getContext(), "Uzupełnij niektóre pola.", Toast.LENGTH_SHORT).show();
      return;
    }

    JSONObject jsonObject = new JSONObject(jsonPrototype);

    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
        (Request.Method.POST, URL, jsonObject, (JSONObject response) -> {
          try {
            Log.e("QQQ", response.getString("comment"));
            if (response.getInt("result_code") == 0) {
              dismiss();
              Toast.makeText(getContext(), "Utworzono nową sesję.", Toast.LENGTH_SHORT).show();
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

  protected boolean boxesExtract(HashMap<String, String> jsonPrototype) {
    jsonPrototype.put("user", ((MainActivity) getActivity()).getUsersEmail());

    String next = mTitleText.getText().toString();

    if (next.isEmpty()) {
      return false;
    }
    jsonPrototype.put("title", next);

    next = mDateText.getText().toString();
    if (next.isEmpty()) {
      return false;
    }
    try {
      DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.FULL);
      next = dateFormat.parse(next).toString();
      Log.d("sss", next);
    } catch (ParseException e) {
      return false;
    }
    jsonPrototype.put("due_date", next);

    next = mTimeText.getText().toString();
    if (next.isEmpty()) {
      return false;
    }
    jsonPrototype.put("due_time", next);

    next = mDurationText.getText().toString();
    if (next.isEmpty()) {
      return false;
    }
    jsonPrototype.put("duration", next);

    next = mPeopleLimitText.getText().toString();
    if (next.isEmpty()) {
      return false;
    }
    jsonPrototype.put("people_limit", next);

    next = mPlaceText.getText().toString();
    if (next.isEmpty()) {
      return false;
    }
    jsonPrototype.put("place", next);

    next = mDescriptionText.getText().toString();
    if (next.isEmpty()) {
      return false;
    }
    jsonPrototype.put("description", next);

    List<Integer> idList = new ArrayList<>();

    for (Tag tag : mTagList.getPickedTags()) {
      idList.add(tag.getId());
    }
    jsonPrototype.put("tags", idList.toString());

    return true;
  }

  @Override
  public void onTagsPicked(List<Tag> tags) {
    mTagList.clearTags();
    mTagList.addTags(tags);
  }
}
