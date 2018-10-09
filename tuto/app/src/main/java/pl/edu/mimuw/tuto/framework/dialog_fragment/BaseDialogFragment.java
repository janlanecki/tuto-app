package pl.edu.mimuw.tuto.framework.dialog_fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.android.volley.RequestQueue;

import pl.edu.mimuw.tuto.R;
import pl.edu.mimuw.tuto.common.data.DataProvider;

public abstract class BaseDialogFragment extends DialogFragment {
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
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    Dialog dialog = new Dialog(getActivity(), R.style.AppTheme);
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    return dialog;
  }

  /**
   * Bugfix:
   * https://stackoverflow.com/questions/12433397/android-dialogfragment-disappears-after-orientation-change
   */
  @Override
  public void onDestroyView() {
    if (getDialog() != null && getRetainInstance()) {
      getDialog().setDismissMessage(null);
    }
    super.onDestroyView();
  }

  /**
   * Allow the dialog to be dismissed when the outside of the is touched.
   *
   * https://stackoverflow.com/questions/8404140/how-to-dismiss-a-dialogfragment-when-pressing-outside-the-dialog
   */
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    getDialog().setCanceledOnTouchOutside(true);
    return null;
  }

  @Override
  public void onStop() {
    super.onStop();
    // Cancel all request created by this fragment before it is destroyed.
    RequestQueue mRequestQueue =
        DataProvider.getInstance(getActivity().getApplicationContext()).getRequestQueue();
    if (mRequestQueue != null) {
      mRequestQueue.cancelAll(this.getTag());
    }
  }
}
