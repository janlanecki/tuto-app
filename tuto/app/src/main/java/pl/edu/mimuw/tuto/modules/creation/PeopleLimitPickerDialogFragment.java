package pl.edu.mimuw.tuto.modules.creation;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;

import pl.edu.mimuw.tuto.R;

public class PeopleLimitPickerDialogFragment extends DialogFragment {
  public static String TAG = "PeopleLimitPickerDialogFragment";

  private NumberPicker mPeopleLimitPicker;
  private EditText mPeopleLimitText;

  public void setmPeopleLimitText(EditText mPeopleLimitText) {
    this.mPeopleLimitText = mPeopleLimitText;
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    LayoutInflater inflater = getActivity().getLayoutInflater();
    View rootView = inflater.inflate(R.layout.number_picker_dialog_fragment, null);
    String peopleLimitText = mPeopleLimitText.getText().toString();

    mPeopleLimitPicker = rootView.findViewById(R.id.numberPicker);
    mPeopleLimitPicker.setMaxValue(255);
    mPeopleLimitPicker.setMinValue(2);
    mPeopleLimitPicker.setValue(peopleLimitText.isEmpty() ? 15 : Integer.parseInt(peopleLimitText));

    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);

    builder.setView(rootView.getRootView())
        .setTitle(R.string.people_limit_title)
        .setPositiveButton(R.string.set, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            mPeopleLimitText.setText(Integer.toString(mPeopleLimitPicker.getValue()));
          }
        })
        .setNegativeButton(R.string.cancel, (dialog, id) -> PeopleLimitPickerDialogFragment.this.getDialog().cancel());

    return builder.create();
  }
}
