package pl.edu.mimuw.tuto.modules.details;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import pl.edu.mimuw.tuto.MainActivity;
import pl.edu.mimuw.tuto.R;
import pl.edu.mimuw.tuto.common.data.DataProvider;
import pl.edu.mimuw.tuto.common.object.session.Session;
import pl.edu.mimuw.tuto.framework.views.TagListView;
import pl.edu.mimuw.tuto.modules.my_sessions.MySessionsFragment;
import pl.edu.mimuw.tuto.modules.wall.IgnoreTagRequest;
import pl.edu.mimuw.tuto.modules.wall.WallFragment;

public class DetailsDialogFragment extends DialogFragment implements View.OnClickListener {

  public final static String TAG = "DetailsDialogFragment";

  private static int sessionId = -1;
  private final static String URL = "https://.../details.php?id=";
  private final static String joinURL = "https://.../participant.php";
  private final static String disjoinURL = "https://.../disjoin.php";
  private final static String checkJoinURL = "https://.../present.php";
  private final static String participantsURL = "https://.../participantList.php?id=";
  private static String mode = "";

  private Button mJoinButton;
  private Button mDisjoinButton;
  private static String[] participantsItems;
  private ListView participantsListView;
  private static String author;
  private static ArrayAdapter<String> participantsListViewAdapter;

  private static boolean joined = false;
  private static boolean sessionsAuthor = false;

  public static DetailsDialogFragment getInstance(FragmentManager fragmentManager) {
    DetailsDialogFragment fragment =
        (DetailsDialogFragment) fragmentManager.findFragmentByTag(DetailsDialogFragment.TAG);

    if (fragment == null) {
      fragment = new DetailsDialogFragment();

      // Reuse this fragment instance during configuration changes(e.g. rotating the screen).
      fragment.setRetainInstance(true);
      Log.d(TAG, "New instance was created.");
    }
    return fragment;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);

    getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
    View rootView = inflater.inflate(R.layout.details_dialog_fragment, container, false);
    mJoinButton = rootView.findViewById(R.id.joinButton);
    mJoinButton.setOnClickListener(this);

    sessionId = this.getArguments().getInt("id");
    mode = this.getArguments().getString("mode");
    getDetails(sessionId);

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
    joined = false; //data was kept in memory even after closing dialog
    sessionsAuthor = false;
    if (dialog != null && getRetainInstance()) {
      dialog.setDismissMessage(null);
    }
    super.onDestroyView();
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

  private void getDetails(int sessionId) {
    String url = URL + sessionId;

    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
        (Request.Method.GET, url, null, this::onRequestFinished, error -> {
        });

    // Add the request to the RequestQueue.
    DataProvider.getInstance(getActivity().getApplicationContext())
        .addToRequestQueue(jsonObjectRequest);
  }

  private void onRequestFinished(JSONObject response) {
    try {
      String title = response.getString("label");
      author = response.getString("author");
      String date = response.getString("date");
      String time = response.getString("time");
      String duration = response.getString("duration");
      String limit = response.getString("limit");
      String place = response.getString("place");
      String description = response.getString("description");
      String email = response.getString("email");

      if (email.compareTo(((MainActivity)getActivity()).getUsersEmail()) == 0) {
        sessionsAuthor = true;
      }

      TextView titleTV = (TextView) getDialog().findViewById(R.id.dialogTitle);
      TextView datetimeTV = (TextView) getDialog().findViewById(R.id.date);
      TextView durationTV = (TextView) getDialog().findViewById(R.id.duration);
      TextView limitTV = (TextView) getDialog().findViewById(R.id.limit);
      TextView placeTV = (TextView) getDialog().findViewById(R.id.place);
      TextView descriptionTV = (TextView) getDialog().findViewById(R.id.description);

      titleTV.setText(title);
      datetimeTV.setText("Kiedy: " + date + ", " + time);
      durationTV.setText("Jak długo: " + duration + " min");
      limitTV.setText("Ile osób: " + limit);
      placeTV.setText("Gdzie: " + place);
      descriptionTV.setText("Opis: " + description);

      if (mode.compareTo(MySessionsFragment.ARCHIVE_SESSIONS) != 0 & !sessionsAuthor) {
        mJoinButton.setVisibility(View.VISIBLE);
        checkJoin(sessionId);
      }
      getParticipantsArray();

    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

  private void checkJoin(int sessionId) {
    String userEmail = ((MainActivity)getActivity()).getUsersEmail();
    String url = checkJoinURL + "?id=" + sessionId + "&email=" + userEmail;

    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
        (Request.Method.GET, url, null, this::onCheckJoinRequestFinished, error -> {
        });

    // Add the request to the RequestQueue.
    DataProvider.getInstance(getActivity().getApplicationContext())
        .addToRequestQueue(jsonObjectRequest);
  }

  private void onCheckJoinRequestFinished(JSONObject response) {
    try {
      String result = response.getString("result");
      if (result.compareTo("true") == 0) { //user already joined session
        joined = true;
        if (mode.compareTo(MySessionsFragment.MY_SESSIONS) != 0) { //authors cannot leave their sessions
          mJoinButton.setText(R.string.disjoin_session);
        }
      } else {
          mJoinButton.setText(R.string.join_session);
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void onClick(View v) {
    String sessionIdString = sessionId + "";

    if (!joined) {
      String url = joinURL + "?id=" + sessionIdString + "&email=" + ((MainActivity) getActivity()).getUsersEmail();

      JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
          (Request.Method.GET, url, null, this::onJoinRequestFinished, error -> {
          });

      // Add the request to the RequestQueue.
      DataProvider.getInstance(getActivity().getApplicationContext())
          .addToRequestQueue(jsonObjectRequest);
    } else {
      String url = disjoinURL + "?id=" + sessionIdString + "&email=" + ((MainActivity) getActivity()).getUsersEmail();

      AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
      builder.setMessage(R.string.disjoin_question)
          .setPositiveButton(R.string.yes, (DialogInterface dialog, int id) -> {
            sendDisjoinRequest(url);
          })
          .setNegativeButton(R.string.no, (DialogInterface dialog, int id) -> {
            // User cancelled the dialog
          });
      builder.create().show();
    }
  }

  private void onJoinRequestFinished(JSONObject response) {
    try {
      String result = response.getString("result");
      if (result.compareTo("success") == 0) {
        Toast.makeText(getContext(), R.string.join_success, Toast.LENGTH_SHORT).show();
        joined = true;
        mJoinButton.setText(R.string.disjoin_session);
        getParticipantsArray();
      } else {
        Toast.makeText(getContext(), R.string.join_fail, Toast.LENGTH_SHORT).show();
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

  private void sendDisjoinRequest(String url) {
    synchronized (this) {
      JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
          (Request.Method.GET, url, null, this::onDisjoinRequestFinished, error -> {
          });

      // Add the request to the RequestQueue.
      DataProvider.getInstance(getActivity().getApplicationContext())
          .addToRequestQueue(jsonObjectRequest);

      Log.d(TAG, "DisjoinSessionRequest was sent.");
    }
  }

  private void onDisjoinRequestFinished(JSONObject response) {
    try {
      String result = response.getString("result");
      if (result.compareTo("success") == 0) {
        Toast.makeText(getContext(),  R.string.disjoin_success, Toast.LENGTH_SHORT).show();
        joined = false;
        mJoinButton.setText(R.string.join_session);
        getParticipantsArray();
      } else {
        Toast.makeText(getContext(), R.string.disjoin_fail, Toast.LENGTH_SHORT).show();
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

  private void getParticipantsArray() {
    String url = participantsURL + sessionId;

    JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
        (Request.Method.GET, url, null, this::onGetParticipantsRequestFinished, error -> {
        });

    // Add the request to the RequestQueue.
    DataProvider.getInstance(getActivity().getApplicationContext())
        .addToRequestQueue(jsonArrayRequest);
  }

  private void onGetParticipantsRequestFinished(JSONArray response) {
    try {
      participantsItems = new String[response.length()];
      //authors need to be participant
      if (response.length() == 0) {
        participantsItems = new String[1];
        participantsItems[0] = author + " (autor)";
      } else {
        for (int i = 0; i < response.length(); ++i) {
          participantsItems[i] = response.getJSONObject(i).getString("participant");
          if (participantsItems[i].equals(author)) {
            String participant0 = participantsItems[0];
            participantsItems[0] = participantsItems[i] + " (autor)";
            if (i != 0) {
              participantsItems[i] = participant0;
            }
          }
        }
      }
      participantsListView = (ListView) getView().findViewById(R.id.participantsListView);
      participantsListViewAdapter = new ArrayAdapter<String>(getActivity(),
          R.layout.participant_list_item, participantsItems);
      participantsListView.setAdapter(participantsListViewAdapter);
      justifyListViewHeightBasedOnChildren(participantsListView);
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

  /* Makes list inside scrollview not scrollable */
  public static void justifyListViewHeightBasedOnChildren (ListView listView) {
    ListAdapter adapter = listView.getAdapter();

    if (adapter == null) {
      return;
    }
    ViewGroup vg = listView;
    int totalHeight = 0;
    int itemSize = 0;
    for (int i = 0; i < adapter.getCount(); i++) {
      View listItem = adapter.getView(i, null, vg);
      listItem.measure(0, 0);
      totalHeight += listItem.getMeasuredHeight();
      itemSize = listItem.getMeasuredHeight();
    }
    //keeps additional space for user to join so that the dialog doesn't keep resizing
    if (!joined & !sessionsAuthor && mode.compareTo(MySessionsFragment.ARCHIVE_SESSIONS) != 0) {
      totalHeight += itemSize;
    }

    ViewGroup.LayoutParams par = listView.getLayoutParams();
    par.height = totalHeight + (listView.getDividerHeight() * (adapter.getCount() - 1));
    listView.setLayoutParams(par);
    listView.requestLayout();
  }

}