package pl.edu.mimuw.tuto.framework.views;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import pl.edu.mimuw.tuto.common.object.tag.Tag;

public class TagListView extends FrameLayout {

  private List<Tag> mTagList;
  private FlowLayout mFlowLayout;
  private TextView mEmptyListIndicator;

  private OnTagClickedListener mListener = null;
  private OnTagLongClickedListener mLongClickedListener = null;

  public TagListView(Context context, AttributeSet attrs) {
    super(context, attrs);

    /**
     * Somehow this view is not saved properly - onSaveInstanceState is called, but
     * onRestoreInstanceState is not called with bundle we create. Please save this view manually
     * in the parent until we fix this problem.
     * Example: see TagPickerDialogFragment.onSaveInstanceState()
     *              TagPickerDialogFragment.onActivityCreated()
     *
     * Automatic saving is disabled(see two lines below).
     */
//    setSaveEnabled(false);
    setSaveFromParentEnabled(false);

    if (mTagList == null) {
      mTagList = new ArrayList<>();
    }
    mFlowLayout = new FlowLayout(context, attrs);
    mEmptyListIndicator = new TextView(context);
    mEmptyListIndicator.setGravity(Gravity.CENTER);
    mEmptyListIndicator.setText("-");

    FrameLayout.LayoutParams lp = generateDefaultLayoutParams();
    lp.gravity = Gravity.CENTER;
    addView(mEmptyListIndicator, lp);

    addView(mFlowLayout);
  }

  public void clearTags() {
    mTagList.clear();
    refresh();
  }

  public void addTag(Tag tag) {
    if (maybeAddTag(tag)) {
      refresh();
    }
  }

  public void addTags(List<Tag> tags) {
    for (Tag tag : tags) {
      maybeAddTag(tag);
    }
    refresh();
  }

  public void removeTag(Tag tag) {
    for (int i = mTagList.size() - 1; i >= 0; --i) {
      if (mTagList.get(i).equals(tag)) {
        mTagList.remove(i);
      }
    }
    refresh();
  }

  public void setEmptyListIndicatorText(String text) {
    mEmptyListIndicator.setText(text);
  }

  public boolean containsTag(Tag tag) {
    for (Tag t : mTagList) {
      if (t.equals(tag)) {
        return true;
      }
    }
    return false;
  }

  private boolean maybeAddTag(Tag tag) {
    if (containsTag(tag)) {
      return false;
    }
    mTagList.add(tag);
    return true;
  }

  public List<Tag> getPickedTags() {
    return new ArrayList<>(mTagList);
  }

  private void onItemClicked(int position) {
    if (position >= 0 && position < mTagList.size()) {
      if (mListener != null) {
        mListener.onTagClicked(mTagList.get(position));
      }
    }
  }

  private boolean onItemLongClicked(int position) {
    if (position >= 0 && position < mTagList.size()) {
      if (mLongClickedListener != null) {
        mLongClickedListener.onTagLongClicked(mTagList.get(position));
        return true;
      }
    }
    return false;
  }

  private void refresh() {
    while (mFlowLayout.getChildCount() > mTagList.size()) {
      mFlowLayout.removeViewAt(mFlowLayout.getChildCount() - 1);
    }

    TagView tagView;
    for (int i = 0; i < mTagList.size(); ++i) {
      Tag tag = mTagList.get(i);
      if (i >= mFlowLayout.getChildCount()) {
        tagView = new TagView(getContext(), tag);
        mFlowLayout.addView(tagView);
      } else {
        tagView = (TagView) mFlowLayout.getChildAt(i);
        tagView.setShownTag(tag);
      }
      final int position = i;
      tagView.setOnClickListener((View v) -> onItemClicked(position));
      tagView.setOnLongClickListener((View v) -> onItemLongClicked(position));
    }

    mEmptyListIndicator.setVisibility(mFlowLayout.getChildCount() == 0
        ? View.VISIBLE
        : View.INVISIBLE);

    requestLayout();
  }

  public interface OnTagClickedListener {
    void onTagClicked(Tag tag);
  }

  public interface OnTagLongClickedListener {
    void onTagLongClicked(Tag tag);
  }

  public void setOnTagClickedListener(OnTagClickedListener listener) {
    this.mListener = listener;
  }

  public void setOnTagLongClickedListener(OnTagLongClickedListener listener) {
    this.mLongClickedListener = listener;
  }

  @Override
  public Parcelable onSaveInstanceState() {
    Bundle bundle = new Bundle();
    bundle.putParcelable("superState", super.onSaveInstanceState());
    bundle.putParcelableArrayList("tags", new ArrayList<>(mTagList));
    Log.d("TagListView",
        "Save id: " + getId() + "," +
            "isSaveEnabled: " + isSaveEnabled() + ", " +
            "isSaveFromParentEnabled: " + isSaveFromParentEnabled() + ", " +
            " instance: " + bundle.toString());
    return bundle;
  }

  @Override
  public void onRestoreInstanceState(Parcelable state) {
    Log.d("TagListView", "Restore id: " + getId() + ", instance: " + state.toString());
    if (state instanceof Bundle) {
      Bundle bundle = (Bundle) state;
      addTags(bundle.getParcelableArrayList("tags"));
      state = bundle.getParcelable("superState");
    }
    super.onRestoreInstanceState(state);
  }

}
