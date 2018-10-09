package pl.edu.mimuw.tuto.modules.wall;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

import pl.edu.mimuw.tuto.R;
import pl.edu.mimuw.tuto.common.object.tag.Tag;

import static pl.edu.mimuw.tuto.framework.views.TagView.calculateColor;

public class SupertagView extends FrameLayout {

  TextView mTextView;

  public SupertagView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init();
  }

  public SupertagView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public SupertagView(Context context) {
    super(context);
    init();
  }

  public SupertagView(Context context, Tag tag) {
    super(context);
    init(tag);
  }

  private void init() {
    inflate(getContext(), R.layout.supertag_view, this);
    mTextView = (TextView) getChildAt(0);
  }

  private void init(Tag tag) {
    init();
    setShownTag(tag);
  }

  public void setShownTag(Tag tag) {
    mTextView.setText(tag.getName());
    setBackgroundColor(calculateColor(tag));
  }
}