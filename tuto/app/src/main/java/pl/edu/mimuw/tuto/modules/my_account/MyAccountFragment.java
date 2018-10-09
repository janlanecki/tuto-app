package pl.edu.mimuw.tuto.modules.my_account;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pl.edu.mimuw.tuto.MainActivity;
import pl.edu.mimuw.tuto.R;
import pl.edu.mimuw.tuto.common.Optional;
import pl.edu.mimuw.tuto.common.data.DataProvider;
import pl.edu.mimuw.tuto.common.object.tag.Tag;
import pl.edu.mimuw.tuto.framework.views.TagListView;

public class MyAccountFragment extends Fragment implements TagListView.OnTagLongClickedListener,
    View.OnClickListener {
  public final static String TAG = "MyAccountFragment";
  private final static String IGNORED_TAGS_KEY = TAG + "_ignoredTags";

  private TextView mEmailView;
  private TagListView mIgnoredTagsView;
  private ProgressBar mProgressBar;

  private Optional<List<Tag>> ignoredTags = Optional.absent();
  private boolean isRequestPending = false;

  public static MyAccountFragment getInstance(FragmentManager fragmentManager) {
    MyAccountFragment fragment = (MyAccountFragment) fragmentManager.findFragmentByTag(TAG);

    if (fragment == null) {
      fragment = new MyAccountFragment();
      Log.d(TAG, "New instance was created.");
    }
    return fragment;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    View rootView = inflater.inflate(R.layout.my_account_fragment, container, false);

    mEmailView = rootView.findViewById(R.id.my_account_email);

    mIgnoredTagsView = rootView.findViewById(R.id.my_account_ignored_tags);
    mIgnoredTagsView.setOnTagLongClickedListener(this);

    mProgressBar = rootView.findViewById(R.id.my_account_progress_bar);

    rootView.findViewById(R.id.menu_button).setOnClickListener(this);

    return rootView;
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    if (ignoredTags.isPresent()) {
      outState.putParcelableArrayList(IGNORED_TAGS_KEY, new ArrayList<>(ignoredTags.get()));
    }
    Log.d(TAG, "onSaveInstanceState(" + outState + ")");
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    ignoredTags = Optional.absent();
    if (savedInstanceState != null) {
      if (savedInstanceState.containsKey(IGNORED_TAGS_KEY)) {
        ignoredTags = Optional.of(savedInstanceState.getParcelableArrayList(IGNORED_TAGS_KEY));
      }
    }

    MainActivity activity = (MainActivity) getActivity();
    mEmailView.setText(activity.getUsersEmail());

    isRequestPending = false;
    if (!ignoredTags.isPresent()) {
      sendTagListRequest();
    } else {
      mIgnoredTagsView.clearTags();
      mIgnoredTagsView.addTags(ignoredTags.get());
    }

    updateProgressBar();
  }

  @Override
  public void onStop() {
    super.onStop();
    // Cancel all request created by this fragment before it is destroyed.
    RequestQueue mRequestQueue =
        DataProvider.getInstance(getActivity().getApplicationContext()).getRequestQueue();
    if (mRequestQueue != null) {
      mRequestQueue.cancelAll(TAG);
    }
  }

  @Override
  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.menu_button:
        Activity activity = getActivity();
        if (activity instanceof MainActivity) {
          ((MainActivity) activity).toggleDrawer();
        }
        break;
    }
  }

  @Override
  public void onTagLongClicked(Tag tag) {
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    builder.setMessage(getString(R.string.unblock_tag_question, tag.getName()))
        .setPositiveButton(R.string.yes, (DialogInterface dialog, int id) -> {
          sendStopIgnoringTagRequest(tag.getId());
        })
        .setNegativeButton(R.string.no, (DialogInterface dialog, int id) -> {
          // User cancelled the dialog
        });
    builder.create().show();
  }

  private void onTagListRequestFinished(List<Tag> tags) {

    ignoredTags = Optional.of(new ArrayList<>(tags));
    mIgnoredTagsView.clearTags();
    mIgnoredTagsView.addTags(tags);

    isRequestPending = false;
    updateProgressBar();
  }

  private void updateProgressBar() {
    Log.d(TAG,
        "ignoredTags: " + ignoredTags.isPresent() +
        ", isRequestPending: " + isRequestPending);
    if (ignoredTags.isAbsent() || isRequestPending) {
      mProgressBar.setVisibility(View.VISIBLE);
    } else {
      mProgressBar.setVisibility(View.GONE);
    }
  }

  private void sendTagListRequest() {
    synchronized (this) {
      if (isRequestPending) {
        Log.d(TAG, "Did not send request because one is currently pending.");
        return;
      }

      HashMap<String, String> params = new HashMap<>();
      params.put("user", ((MainActivity) getActivity()).getUsersEmail());

      TagListRequest request =
          new TagListRequest(
              new JSONObject(params),
              (List<Tag> response) -> {
                onTagListRequestFinished(response);
              },
              (VolleyError error) -> {
                onTagListRequestFinished(new ArrayList<>());
                Log.d(TAG, "Response error: " + error.getMessage());
                Toast.makeText(getContext(), "Error.", Toast.LENGTH_SHORT).show();
              });

      request.setTag(getTag());

      // Add the request to the RequestQueue.
      DataProvider.getInstance(getActivity().getApplicationContext())
          .addToRequestQueue(request);

      isRequestPending = true;
      updateProgressBar();

      Log.d(TAG, "TagListRequest was sent.");
    }
  }

  private void onStopIgnoringTagRequestFinished(String response) {
    Toast.makeText(getContext(), response, Toast.LENGTH_SHORT).show();
    isRequestPending = false;
    sendTagListRequest();
  }

  private void sendStopIgnoringTagRequest(int tagId) {
    synchronized (this) {
      if (isRequestPending) {
        Log.d(TAG, "Did not send request because one is currently pending.");
        return;
      }

      StopIgnoringTagRequest request =
          StopIgnoringTagRequest.newRequest(
              (String response) -> {
                onStopIgnoringTagRequestFinished(response);
              },
              (VolleyError error) -> {
                onStopIgnoringTagRequestFinished("Error");
                Log.d(TAG, "Response error: " + error.getMessage());
//                Toast.makeText(getContext(), "Error.", Toast.LENGTH_SHORT).show();
              },
              ((MainActivity) getActivity()).getUsersEmail(),
              tagId);

      request.setTag(getTag());

      // Add the request to the RequestQueue.
      DataProvider.getInstance(getActivity().getApplicationContext())
          .addToRequestQueue(request);

      isRequestPending = true;
      updateProgressBar();

      Log.d(TAG, "StopIgnoringTagRequest was sent.");
    }
  }
}
