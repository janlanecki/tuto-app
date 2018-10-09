package pl.edu.mimuw.tuto.modules.tag_picker;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pl.edu.mimuw.tuto.MainActivity;
import pl.edu.mimuw.tuto.R;
import pl.edu.mimuw.tuto.common.Optional;
import pl.edu.mimuw.tuto.common.data.DataProvider;
import pl.edu.mimuw.tuto.common.object.tag.Tag;
import pl.edu.mimuw.tuto.framework.dialog_fragment.BaseDialogFragment;
import pl.edu.mimuw.tuto.framework.views.TagListView;

public class TagPickerDialogFragment extends BaseDialogFragment implements View.OnClickListener {
  public final static String TAG = "TagPickerDialogFragment";
  private final static String INITIAL_TAGS_KEY = TAG + "_initialTags";
  private final static String TARGET_FRAGMENT_TAG = TAG + "_target";
  private final static String TAG_PICKER_VIEW_TAG = TAG + "_tagPicker";
  private final static String TAG_TIPS_VIEW_TAG = TAG + "_tagList";
  private final static String IS_REQUEST_PENDING_TAG = TAG + "_request";

  private EditText mTagInput;
  private View mProgressBar;

  private Optional<String> pendingRequest;
  private boolean requestAttemptHappened = false;

  private TagListView mTagTipsView;
  private TagListView mTagPickerView;

  public static TagPickerDialogFragment newInstance(List<Tag> initialTags, String targetTag) {
    TagPickerDialogFragment fragment = new TagPickerDialogFragment();

    Bundle bundle = new Bundle();
    bundle.putParcelableArrayList(INITIAL_TAGS_KEY, new ArrayList<>(initialTags));
    bundle.putString(TARGET_FRAGMENT_TAG, targetTag);
    fragment.setArguments(bundle);

    // Reuse this fragment instance during configuration changes(e.g. rotating the screen).
    fragment.setRetainInstance(true);
    Log.d(TAG, "New instance was created.");
    return fragment;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);

    getDialog().getWindow().setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);

    View rootView = inflater.inflate(R.layout.tag_picker_dialog_fragment, container, false);

    rootView.findViewById(R.id.accept_button).setOnClickListener(this);
    rootView.findViewById(R.id.cancel_button).setOnClickListener(this);

    mTagInput = rootView.findViewById(R.id.tag_input);

    mTagInput.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

      }

      @Override
      public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

      }

      @Override
      public void afterTextChanged(Editable editable) {
        maybeSendRequest(editable.toString());
      }
    });

    mTagTipsView = rootView.findViewById(R.id.tag_list);
    mTagTipsView.setOnTagClickedListener((Tag tag) -> {
      mTagTipsView.removeTag(tag);
      if (tag instanceof TagWithParent) {
        List<Tag> tags = mTagPickerView.getPickedTags();
        for (Tag t : tags) {
          if (t.getId() == ((TagWithParent)tag).getParent()) {
            mTagPickerView.removeTag(t);
          }
        }
      }
      mTagPickerView.addTag(tag);
      maybeSendRequest();
    });
    mTagTipsView.setEmptyListIndicatorText(getString(R.string.no_tags_to_show));

    mTagPickerView = rootView.findViewById(R.id.tag_picker);
    mTagPickerView.setOnTagClickedListener((Tag tag) -> {
      mTagPickerView.removeTag(tag);
      maybeSendRequest();
    });
    mTagPickerView.setEmptyListIndicatorText(getString(R.string.no_picked_tags));

    mProgressBar = rootView.findViewById(R.id.progress_bar);
    mProgressBar.setVisibility(View.GONE);

    return rootView;
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putParcelable(TAG_PICKER_VIEW_TAG, mTagPickerView.onSaveInstanceState());
    outState.putParcelable(TAG_TIPS_VIEW_TAG, mTagTipsView.onSaveInstanceState());
    outState.putBoolean(IS_REQUEST_PENDING_TAG, pendingRequest.isPresent());
    Log.d(TAG, "onSaveInstanceState(" + outState + ")");
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    if (savedInstanceState == null) {
      Bundle bundle = getArguments();
      if (bundle.containsKey(INITIAL_TAGS_KEY)) {
        List<Tag> tags = bundle.getParcelableArrayList(INITIAL_TAGS_KEY);
        mTagPickerView.addTags(tags);
      }
    } else {
      mTagPickerView.onRestoreInstanceState(savedInstanceState.getParcelable(TAG_PICKER_VIEW_TAG));
      mTagTipsView.onRestoreInstanceState(savedInstanceState.getParcelable(TAG_TIPS_VIEW_TAG));
    }

    if ((savedInstanceState == null || savedInstanceState.getBoolean(IS_REQUEST_PENDING_TAG))) {
      // Send initial request
      pendingRequest = Optional.absent();
      maybeSendRequest();
    }
  }

  @Override
  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.cancel_button:
        dismiss();
        break;
      case R.id.accept_button:
        List<Tag> pickedTags = mTagPickerView.getPickedTags();
        Bundle args = getArguments();
        if (args.containsKey(TARGET_FRAGMENT_TAG)) {
          String target = args.getString(TARGET_FRAGMENT_TAG);
          Fragment f = getFragmentManager().findFragmentByTag(target);
          if (f != null && f instanceof TagPickerUser) {
            ((TagPickerUser) f).onTagsPicked(pickedTags);
          }
          dismiss();
        } else {
          Log.d(TAG, "Error: no target fragment.");
          Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
        }
        break;
    }
  }

  private void showProgressBar() {
    mProgressBar.setVisibility(View.VISIBLE);
  }

  private void hideProgressBar() {
    mProgressBar.setVisibility(View.GONE);
  }

  private void onRequestFinished(List<TagWithParent> tags) {
    boolean inputChanged = (pendingRequest.isPresent()
        && !pendingRequest.get().equals(mTagInput.getText().toString()));

    pendingRequest = Optional.absent();

    mTagTipsView.clearTags();
    for (Tag tag : tags) {
      if (!mTagPickerView.containsTag(tag)) {
        mTagTipsView.addTag(tag);
      }
    }

    if (inputChanged || requestAttemptHappened) {
      maybeSendRequest(mTagInput.getText().toString());
      showProgressBar();
    } else {
      hideProgressBar();
    }
  }

  private void maybeSendRequest() {
    maybeSendRequest(mTagInput.getText().toString());
  }

  private void maybeSendRequest(String currentInput) {
    synchronized (this) {
      Log.d(TAG, "Request attempt: currentInput: " +
          (currentInput == null ? "null" : "\"" + currentInput + "\""));
      if (pendingRequest.isPresent()
          || currentInput == null) {
        requestAttemptHappened = true;
        Log.d(TAG, "Request was not sent");
        return;
      }
      requestAttemptHappened = false;

      HashMap<String, String> params = new HashMap<>();
      params.put("user", ((MainActivity) getActivity()).getUsersEmail());
      params.put("input", currentInput);
      JSONArray tags = new JSONArray();
      for (Tag tag : mTagPickerView.getPickedTags()) {
        tags.put(tag.getId());
      }
      params.put("picked", tags.toString());

      TagListRequest request =
          new TagListRequest(
              new JSONObject(params),
              (List<TagWithParent> response) -> {
                onRequestFinished(response);
              },
              (VolleyError error) -> {
                onRequestFinished(new ArrayList<>());
                Log.d(TAG, "Response error: " + error.getMessage());
                Toast.makeText(getContext(), "Error.", Toast.LENGTH_SHORT).show();
              });

      request.setTag(getTag());

      pendingRequest = Optional.of(currentInput);
      showProgressBar();

      // Add the request to the RequestQueue.
      DataProvider.getInstance(getActivity().getApplicationContext())
          .addToRequestQueue(request);

      Log.d(TAG, "TagListRequest was sent.");
    }
  }

  public interface TagPickerUser {
    void onTagsPicked(List<Tag> tags);
  }
}
