package pl.edu.mimuw.tuto.modules.wall;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import pl.edu.mimuw.tuto.R;
import pl.edu.mimuw.tuto.common.Optional;
import pl.edu.mimuw.tuto.common.object.session.Session;
import pl.edu.mimuw.tuto.common.object.tag.Tag;
import pl.edu.mimuw.tuto.framework.views.TagListView;

/**
 * Adapter for the WallFragment list's views.
 */
public class WallListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
    implements TagListView.OnTagLongClickedListener, TagListView.OnTagClickedListener {

  private final int VIEW_ITEM = 1;
  private final int VIEW_PROG = 0;

  private final Optional<List<Session>> mValues;

  // The minimum amount of items to have below your current scroll position before loading more.
  private int visibleThreshold = 2;
  private int lastVisibleItem, totalItemCount;
  private boolean loading;
  private boolean canStartLoading;
  private OnLoadMoreListener onLoadMoreListener;
  private OnItemClickListener onItemClickListener;
  private OnEditClickListener onEditClickListener;

  private TagListView.OnTagLongClickedListener onTagLongClickedListener;
  private TagListView.OnTagClickedListener onTagClickedListener;

  WallListAdapter(Optional<List<Session>> mValues, RecyclerView parentView) {
    this.mValues = mValues;

    if (parentView.getLayoutManager() instanceof LinearLayoutManager) {

      final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) parentView.getLayoutManager();
      parentView.addOnScrollListener(new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
          super.onScrolled(recyclerView, dx, dy);

          totalItemCount = linearLayoutManager.getItemCount();
          lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
          if (!loading) {
            if (canStartLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
              // End has been reached - do something
              if (onLoadMoreListener != null) {
                onLoadMoreListener.onLoadMore();
              }
              canStartLoading = false;
            } else if (totalItemCount > (lastVisibleItem + visibleThreshold)) {
              canStartLoading = true;
            }
          }
        }
      });
    }
  }

  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    RecyclerView.ViewHolder holder;
    if (viewType == VIEW_ITEM) {
      View v = LayoutInflater.from(parent.getContext())
          .inflate(R.layout.wall_list_adapter, parent, false);

      holder = new ViewHolder(v);
    } else {
      View v = LayoutInflater.from(parent.getContext())
          .inflate(R.layout.wall_list_loader_item, parent, false);

      holder = new ProgressViewHolder(v);
    }
    return holder;
  }

  @Override
  public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
    if (holder instanceof ProgressViewHolder) {
      // This is the loader - do nothing.
      return;
    }
    final ViewHolder lHolder = (ViewHolder) holder;
    final Session session = getValues().get(position);

    lHolder.mWrapper.setOnClickListener((View view) -> {
      onItemClicked(position);
    });
    lHolder.mHeader.setText(session.getLabel());
    lHolder.mDate.setText(session.getDate() + ", " + session.getTime());
    lHolder.mPlace.setText(session.getPlace());
    lHolder.mAuthor.setText(session.getAuthor());

    if (session.getOwn().equals("true")) {
      lHolder.mOwn.setOnClickListener(view -> onEditClick(position));
      lHolder.mOwn.setVisibility(View.VISIBLE);
    } else {
      lHolder.mOwn.setVisibility(View.GONE);
    }

    lHolder.mTags.clearTags();
    lHolder.mSupertags.clearTags();
    lHolder.mSupertags.addTags(session.getSuperTags());

    if (session.getTags().isEmpty()) {
      lHolder.mTags.setVisibility(View.GONE);
    } else {
      lHolder.mTags.addTags(session.getTags());
      lHolder.mTags.setOnTagLongClickedListener(this);
      lHolder.mTags.setOnTagClickedListener(this);
      lHolder.mTags.setVisibility(View.VISIBLE);
    }
  }

  @Override
  public void onTagClicked(Tag tag) {
    if (onTagClickedListener != null) {
      onTagClickedListener.onTagClicked(tag);
    }
  }

  @Override
  public void onTagLongClicked(Tag tag) {
    if (onTagLongClickedListener != null) {
      onTagLongClickedListener.onTagLongClicked(tag);
    }
  }

  private void onItemClicked(int position) {
    if (onItemClickListener != null) {
      onItemClickListener.onItemClick(position);
    }
  }

  private void onEditClick(int position) {
    if (onEditClickListener != null) {
      onEditClickListener.onEditClick(position);
    }
  }

  @Override
  public int getItemCount() {
    if (loading) {
      return getValues().size() + 1;
    } else {
      return getValues().size();
    }

  }

  @Override
  public int getItemViewType(int position) {
    return (position < getValues().size()) ? VIEW_ITEM : VIEW_PROG;
  }

  public void startLoading() {
    loading = true;
    // Add the progress-bar item.
    notifyItemInserted(getValues().size());
  }

  public void finishLoading() {
    loading = false;
    // Remove the progress-bar item.
    notifyItemRemoved(getValues().size());
  }

  class ViewHolder extends RecyclerView.ViewHolder {
    final View mWrapper;
    final SupertagsView mSupertags;
    final TextView mHeader;
    final TextView mDate;
    final TextView mPlace;
    final TextView mAuthor;
    final ImageButton mOwn;
    final TagListView mTags;

    ViewHolder(View view) {
      super(view);
      mWrapper = view.findViewById(R.id.wrapper);
      mHeader = view.findViewById(R.id.header);
      mDate = view.findViewById(R.id.date);
      mPlace = view.findViewById(R.id.place);
      mAuthor = view.findViewById(R.id.author);
      mOwn = view.findViewById(R.id.editButton);
      mTags = view.findViewById(R.id.wall_adapter_tags);
      mSupertags = view.findViewById(R.id.supertags_wrapper);
    }
  }

  public static class ProgressViewHolder extends RecyclerView.ViewHolder {
    final ProgressBar progressBar;

    ProgressViewHolder(View v) {
      super(v);
      progressBar = v.findViewById(R.id.progress_bar);
    }
  }

  public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
    this.onLoadMoreListener = onLoadMoreListener;
  }

  public interface OnLoadMoreListener {
    void onLoadMore();
  }

  public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
    this.onItemClickListener = onItemClickListener;
  }

  public void setOnTagLongClickedListener(TagListView.OnTagLongClickedListener onTagLongClickedListener) {
    this.onTagLongClickedListener = onTagLongClickedListener;
  }

  public void setOnTagClickedListener(TagListView.OnTagClickedListener onTagClickedListener) {
    this.onTagClickedListener = onTagClickedListener;
  }

  public void setOnEditClickListener(OnEditClickListener onEditClickListener) {
    this.onEditClickListener = onEditClickListener;
  }

  private List<Session> getValues() {
    if (mValues.isPresent()) {
      return mValues.get();
    } else {
      return new ArrayList<>();
    }
  }

  public interface OnItemClickListener {
    void onItemClick(int position);
  }

  public interface OnEditClickListener {
    void onEditClick(int position);
  }
}
