package pl.edu.mimuw.tuto.framework.views;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import pl.edu.mimuw.tuto.R;

public class LoadingRecyclerView extends LinearLayout {
  private ProgressBar mProgressBar;
  private RecyclerView mRecyclerView;

  public LoadingRecyclerView(Context context) {
    super(context);
    init(context);
  }

  public LoadingRecyclerView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  public LoadingRecyclerView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init(context);
  }

  public void setIsLoaded(boolean isLoaded) {
    if (mProgressBar == null || mRecyclerView == null) {
      throw new RuntimeException("LoadingRecyclerView.setIsLoaded() : view is null.");
    }
    if (isLoaded) {
      mProgressBar.setVisibility(View.GONE);
      mRecyclerView.setVisibility(View.VISIBLE);
    } else {
      mProgressBar.setVisibility(View.VISIBLE);
      mRecyclerView.setVisibility(View.GONE);
    }
  }

  public void setAdapter(RecyclerView.Adapter adapter) {
    if (mRecyclerView != null) {
      mRecyclerView.setAdapter(adapter);
    }
  }

  public RecyclerView getRecyclerView() {
    return this.mRecyclerView;
  }

  public RecyclerView.Adapter getAdapter() {
    if (mRecyclerView != null) {
      return mRecyclerView.getAdapter();
    }
    return null;
  }

  private void init(Context context) {
    this.mProgressBar = createProgressBar(context);
    this.mRecyclerView = createRecyclerView(context);

    LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    params.gravity = Gravity.CENTER;

    addView(mRecyclerView);
    addView(mProgressBar, params);

    setIsLoaded(false);
  }

  private RecyclerView createRecyclerView(Context context) {
    RecyclerView recyclerView = new RecyclerView(context);
    recyclerView.setLayoutParams(new RecyclerView.LayoutParams(
        RecyclerView.LayoutParams.MATCH_PARENT,
        RecyclerView.LayoutParams.MATCH_PARENT));
    recyclerView.setId(R.id.loading_recycler_view);
    recyclerView.setLayoutManager(new LinearLayoutManager(context));
    return recyclerView;
  }

  private ProgressBar createProgressBar(Context context) {
    ProgressBar progressBar = new ProgressBar(context);
    progressBar.setLayoutParams(new LayoutParams(
        LayoutParams.MATCH_PARENT,
        LayoutParams.WRAP_CONTENT
    ));

    return progressBar;
  }
}
