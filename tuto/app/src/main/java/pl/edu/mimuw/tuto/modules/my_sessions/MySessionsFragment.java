package pl.edu.mimuw.tuto.modules.my_sessions;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.RequestQueue;
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
import pl.edu.mimuw.tuto.common.object.session.Session;
import pl.edu.mimuw.tuto.common.object.tag.Tag;
import pl.edu.mimuw.tuto.framework.views.LoadingRecyclerView;
import pl.edu.mimuw.tuto.framework.views.TagListView;
import pl.edu.mimuw.tuto.modules.details.DetailsDialogFragment;
import pl.edu.mimuw.tuto.modules.tag_picker.TagPickerDialogFragment;
import pl.edu.mimuw.tuto.modules.wall.IgnoreTagRequest;

public class MySessionsFragment extends Fragment
    implements SwipeRefreshLayout.OnRefreshListener, MySessionsListAdapter.OnLoadMoreListener,
    MySessionsListAdapter.OnItemClickListener, View.OnClickListener,
    TagPickerDialogFragment.TagPickerUser,
    TagListView.OnTagLongClickedListener {
  public static String TAG = "MySessionsFragment";
  private final static String SESSION_LIST_KEY = TAG + "_sessionList";
  private final static String PICKED_TAGS_KEY = TAG + "_pickedTags";

  public final static String MY_SESSIONS = "MySessions";
  public final static String SAVED_SESSIONS = "SavedSessions";
  public final static String ARCHIVE_SESSIONS = "ArchiveSessions";

  private final static String mySesionsURL = "https://.../user_sessions.php";
  private final static String archiveSessionsURL = "https://.../user_archive.php";
  private final static String savedSessionsURL = "https://.../participant_sessions.php";

  private MySessionsListAdapter mAdapter;
  private LoadingRecyclerView mRecyclerView;
  private SwipeRefreshLayout mSwipeRefreshLayout;

  private Optional<List<Session>> mSessionList = Optional.absent();
  private List<Tag> pickedTags = new ArrayList<>();

  public static MySessionsFragment getInstance(FragmentManager fragmentManager, String MySessionsType) {
    TAG = MySessionsType;
    MySessionsFragment fragment = (MySessionsFragment) fragmentManager.findFragmentByTag(MySessionsFragment.TAG);

    if (fragment == null) {
      fragment = new MySessionsFragment();
      Log.d(TAG, "New instance was created.");
    }
    return fragment;
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    if (mSessionList.isPresent()) {
      Log.d(TAG, "Saving session list(size: " + mSessionList.get().size() + ")");
      outState.putParcelableArrayList(SESSION_LIST_KEY, new ArrayList<>(mSessionList.get()));
    }
    outState.putParcelableArrayList(PICKED_TAGS_KEY, new ArrayList<>(pickedTags));
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    View rootView = inflater.inflate(R.layout.my_sessions_fragment, container, false);

    this.mRecyclerView = rootView.findViewById(R.id.list);

    this.mSwipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh);
    this.mSwipeRefreshLayout.setOnRefreshListener(this);

    rootView.findViewById(R.id.menu_button).setOnClickListener(this);

    rootView.findViewById(R.id.search_button).setOnClickListener(this);

    return rootView;
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    if (savedInstanceState != null) {
      if (savedInstanceState.containsKey(SESSION_LIST_KEY)) {
        Log.d(TAG, "Restoring session list from saved instance state.");
        mSessionList = Optional.of(savedInstanceState.getParcelableArrayList(SESSION_LIST_KEY));
      }
    }

    boolean sendInitialRequest = false;
    if (mSessionList.isAbsent()) {
      Log.d(TAG, "Session list is null");
      sendInitialRequest = true;
    }
    mAdapter = new MySessionsListAdapter(mSessionList, mRecyclerView.getRecyclerView());
    mAdapter.setOnLoadMoreListener(this);
    mAdapter.setOnItemClickListener(this);
    mAdapter.setOnTagLongClickedListener(this);
    mRecyclerView.setAdapter(mAdapter);

    if (sendInitialRequest) {
      mRecyclerView.setIsLoaded(false);
      sendContentRequest();
    } else {
      mRecyclerView.setIsLoaded(true);
    }
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
      case R.id.search_button:
        FragmentManager fm = getFragmentManager();
        DialogFragment fragment =
            (TagPickerDialogFragment) fm.findFragmentByTag(TagPickerDialogFragment.TAG);
        if (fragment == null) {
          fragment = TagPickerDialogFragment.newInstance(pickedTags, TAG);
          fragment.show(fm, TagPickerDialogFragment.TAG);
        }
        break;
    }
  }

  @Override
  public void onTagLongClicked(Tag tag) {
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    builder.setMessage(getString(R.string.block_tag_question, tag.getName()))
        .setPositiveButton(R.string.yes, (DialogInterface dialog, int id) -> {
          sendIgnoreTagRequest(tag.getId());
        })
        .setNegativeButton(R.string.no, (DialogInterface dialog, int id) -> {
          // User cancelled the dialog
        });
    builder.create().show();
  }

  public void handleLogOut() {
    if (mRecyclerView != null) {
      mRecyclerView.setIsLoaded(false);
    }
    if (mAdapter != null) {
      mSessionList.set(null);
      mAdapter.notifyDataSetChanged();
    }
  }

  /**
   * Moves the sessions from @param list to mSessionList and notifies the mRecyclerView.
   */
  private void addListContent(ArrayList<Session> list) {
    if (mSessionList.isPresent() && mSessionList.get() == list) {
      return;
    }
    if (mSessionList.isAbsent()) {
      mSessionList.set(new ArrayList<>());
    }
    mSessionList.get().addAll(list);
    if (list.size() > 0) {
      mAdapter.notifyDataSetChanged();
    }
  }

  @Override
  public void onItemClick(int position) {
    if (mSessionList.isAbsent() || position < 0 || position >= mSessionList.get().size()) {
      // Should not happen.
      return;
    }
    final Session session = mSessionList.get().get(position);

    // Create and show the dialog.
    FragmentManager fm = getFragmentManager();
    DialogFragment newFragment = DetailsDialogFragment.getInstance(fm);
    Bundle args = new Bundle();
    args.putInt("id", session.getId());
    args.putString("mode", TAG);
    newFragment.setArguments(args);
    newFragment.show(fm, DetailsDialogFragment.TAG);
  }

  @Override
  public void onTagsPicked(List<Tag> tags) {
    pickedTags.clear();
    pickedTags.addAll(tags);

    // If there is an ongoing request - cancel it.
    if (!isRefreshingEnabled()) {
      DataProvider.getInstance(getActivity().getApplicationContext()).getRequestQueue()
          .cancelAll(TAG);
      setRefreshingEnabled(true);
    }

    onRefresh();
  }

  /**
   * This function is called when the list is scrolled to the bottom.
   */
  @Override
  public void onLoadMore() {
    synchronized (this) {
      if (!isRefreshingEnabled()) {
        return;
      }
      mAdapter.startLoading();
      sendContentRequest();
    }
  }

  /**
   * This function is called when SwipeRefreshView triggers the refresh.
   */
  @Override
  public void onRefresh() {
    Log.d(TAG, "Refresh was requested.");

    clearSessionList();

    // Hide the swipe-refresh progress spinner.
    mSwipeRefreshLayout.setRefreshing(false);

    sendContentRequest();
  }

  private void clearSessionList() {
    mRecyclerView.setIsLoaded(false);
    if (mSessionList.isPresent()) {
      mSessionList.set(null);
      mAdapter.notifyDataSetChanged();
    }
  }

  private void onRequestFinished() {
    setRefreshingEnabled(true);
    mRecyclerView.setIsLoaded(true);
    mAdapter.finishLoading();
  }

  private void setRefreshingEnabled(boolean enabled) {
    mSwipeRefreshLayout.setEnabled(enabled);
  }

  private boolean isRefreshingEnabled() {
    return mSwipeRefreshLayout.isEnabled();
  }

  private void sendContentRequest() {
    synchronized (this) {
      if (!isRefreshingEnabled()) {
        Toast.makeText(getContext(), "Can not send a request now.", Toast.LENGTH_SHORT).show();
        return;
      }
      setRefreshingEnabled(false);

      HashMap<String, String> params = new HashMap<>();
      JSONArray tags = new JSONArray();
      for (Tag tag : pickedTags) {
        tags.put(tag.getId());
      }
      if (mSessionList.isPresent() && !mSessionList.get().isEmpty()) {
        // Send id of the last session on the list.
        params.put("last_id", "" + mSessionList.get().get(mSessionList.get().size() - 1).getId());
      }
      params.put("tags", tags.toString());
      params.put("user", ((MainActivity) getActivity()).getUsersEmail());

      JSONObject postParams = new JSONObject(params);

      String URL;
      if (TAG.compareTo(MY_SESSIONS) == 0) {
        URL = mySesionsURL;
      } else if (TAG.compareTo(SAVED_SESSIONS) == 0) {
        URL = savedSessionsURL;
      } else {
        URL = archiveSessionsURL;
      }

      MySessionsContentRequest request =
          new MySessionsContentRequest(
              postParams,
              (ArrayList<Session> response) -> {
                onRequestFinished();
                addListContent(response);
              },
              (VolleyError error) -> {
                onRequestFinished();
                Log.d(TAG, "Response error: " + error.getMessage());
                Toast.makeText(getContext(), "Error.", Toast.LENGTH_SHORT).show();
              },
              URL);

      request.setTag(TAG);

      // Add the request to the RequestQueue.
      DataProvider.getInstance(getActivity().getApplicationContext())
          .addToRequestQueue(request);

      Log.d(TAG, "MySessionsContentRequest was sent.");
    }
  }

  private void onIgnoreTagRequestFinished(String response) {
    Toast.makeText(getContext(), response, Toast.LENGTH_SHORT).show();
    setRefreshingEnabled(true);
    onRefresh();
  }

  private void sendIgnoreTagRequest(int tagId) {
    synchronized (this) {
      if (!isRefreshingEnabled()) {
        Toast.makeText(getContext(), "Can not send a request now.", Toast.LENGTH_SHORT).show();
        return;
      }
      setRefreshingEnabled(false);
      clearSessionList();

      IgnoreTagRequest request =
          IgnoreTagRequest.newRequest(
              (String response) -> {
                onIgnoreTagRequestFinished(response);
              },
              (VolleyError error) -> {
                onIgnoreTagRequestFinished("Error");
                Log.d(TAG, "Response error: " + error.getMessage());
              },
              ((MainActivity) getActivity()).getUsersEmail(),
              tagId);
      request.setTag(TAG);

      // Add the request to the RequestQueue.
      DataProvider.getInstance(getActivity().getApplicationContext())
          .addToRequestQueue(request);

      Log.d(TAG, "IgnoreTagRequest was sent.");
    }
  }

}