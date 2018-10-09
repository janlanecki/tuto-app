package pl.edu.mimuw.tuto.modules.wall;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import pl.edu.mimuw.tuto.common.object.tag.Tag;
import pl.edu.mimuw.tuto.framework.views.FlowLayout;
import pl.edu.mimuw.tuto.framework.views.TagView;

public class SupertagsView extends LinearLayout {

  private List<Tag> mTagList;

  private OnTagLongClickedListener mLongClickedListener = null;

  public SupertagsView(Context context, AttributeSet attrs) {
    super(context, attrs);

    if (mTagList == null) {
      mTagList = new ArrayList<>();
    }

    LayoutParams lp = generateDefaultLayoutParams();
    lp.gravity = Gravity.CENTER;
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
    while (getChildCount() > mTagList.size()) {
      removeViewAt(getChildCount() - 1);
    }

    SupertagView tagView;
    for (int i = 0; i < mTagList.size(); ++i) {
      Tag tag = mTagList.get(i);
      if (i >= getChildCount()) {
        tagView = new SupertagView(getContext(), tag);
        LinearLayout.LayoutParams lp = generateDefaultLayoutParams();
        lp.width = LayoutParams.MATCH_PARENT;
        lp.height = 0;
        lp.weight = 1;

        addView(tagView, lp);
      } else {
        tagView = (SupertagView) getChildAt(i);
        tagView.setShownTag(tag);
      }
      final int position = i;
      tagView.setOnLongClickListener((View v) -> onItemLongClicked(position));
    }

    setVisibility(getChildCount() > 0
        ? View.VISIBLE
        : View.GONE);

    requestLayout();
  }

  public interface OnTagLongClickedListener {
    void onTagLongClicked(Tag tag);
  }

  public void setOnTagLongClickedListener(OnTagLongClickedListener listener) {
    this.mLongClickedListener = listener;
  }

}
