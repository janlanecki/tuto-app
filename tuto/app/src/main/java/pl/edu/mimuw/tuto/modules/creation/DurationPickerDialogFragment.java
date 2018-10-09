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

public class DurationPickerDialogFragment extends DialogFragment {
  public static String TAG = "DurationPickerDialogFragment";

  private NumberPicker mDurationPicker;
  private EditText mDurationText;

  public void setmDurationText(EditText mDurationText) {
    this.mDurationText = mDurationText;
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    LayoutInflater inflater = getActivity().getLayoutInflater();
    View rootView = inflater.inflate(R.layout.number_picker_dialog_fragment, null);
    String durationText = mDurationText.getText().toString();

    mDurationPicker = rootView.findViewById(R.id.numberPicker);
    mDurationPicker.setDisplayedValues(createValues(5, 300, 5));
    mDurationPicker.setMaxValue(300 / 5 - 1);
    mDurationPicker.setMinValue(0);
    mDurationPicker.setValue(durationText.isEmpty() ? 90 / 5 - 1 : Integer.parseInt(durationText) / 5 - 1);

    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);

    builder.setView(rootView.getRootView())
        .setTitle(R.string.duration_title)
        .setPositiveButton(R.string.set, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            mDurationText.setText(Integer.toString((mDurationPicker.getValue() + 1) * 5));
          }
        })
        .setNegativeButton(R.string.cancel, (dialog, id) -> DurationPickerDialogFragment.this.getDialog().cancel());

    return builder.create();
  }

  private String[] createValues(int begin, int end, int jump) {
    String[] values = new String[(end - begin) / jump + 1];

    for (int i = 0; i < values.length; ++i) {
      values[i] = Integer.toString(begin + i * jump);
    }

    return values;
  }
}
