package io.github.lagrant.MeetingCraftsman.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Stack;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.froger.instamaterial.R;
import io.github.froger.instamaterial.ui.activity.MainActivity;
import io.github.froger.instamaterial.ui.activity.PublishActivity;
import io.github.froger.instamaterial.ui.view.LoadingFeedItemView;

public class FeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final String ACTION_LIKE_BUTTON_CLICKED = "action_like_button_button";
    public static final String ACTION_LIKE_IMAGE_CLICKED = "action_like_image_button";

    public static final int VIEW_TYPE_DEFAULT = 1;
    public static final int VIEW_TYPE_LOADER = 2;

    private static Random random = new Random(100);
    private static final List<FeedItem> feedItems = new ArrayList<>();
    private static final Stack<FeedItem> feedItems1 = new Stack<>();

    private static Context context;
    private OnFeedItemClickListener onFeedItemClickListener;

    private boolean showLoadingView = false;
    private static int validFeedItem = 7;
    private static String bottom[] = new String[256];
    private static Bitmap queue[] = new Bitmap[256];

    public FeedAdapter(Context context) {
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_DEFAULT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_feed, parent, false);
            CellFeedViewHolder cellFeedViewHolder = new CellFeedViewHolder(view);
            setupClickableViews(view, cellFeedViewHolder);
            return cellFeedViewHolder;
        } else if (viewType == VIEW_TYPE_LOADER) {
            LoadingFeedItemView view = new LoadingFeedItemView(context);
            view.setLayoutParams(new LinearLayoutCompat.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT)
            );
            return new LoadingCellFeedViewHolder(view);
        }

        return null;
    }

    private void setupClickableViews(final View view, final CellFeedViewHolder cellFeedViewHolder) {
        cellFeedViewHolder.btnComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFeedItemClickListener.onCommentsClick(view, cellFeedViewHolder.getAdapterPosition());
            }
        });
        cellFeedViewHolder.btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFeedItemClickListener.onMoreClick(v, cellFeedViewHolder.getAdapterPosition());
            }
        });
        cellFeedViewHolder.ivFeedCenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = cellFeedViewHolder.getAdapterPosition();
                feedItems.get(adapterPosition).likesCount++;
                notifyItemChanged(adapterPosition, ACTION_LIKE_IMAGE_CLICKED);
                if (context instanceof MainActivity) {
                    ((MainActivity) context).showLikedSnackbar();
                }
            }
        });
        cellFeedViewHolder.btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = cellFeedViewHolder.getAdapterPosition();
                feedItems.get(adapterPosition).likesCount++;
                notifyItemChanged(adapterPosition, ACTION_LIKE_BUTTON_CLICKED);
                if (context instanceof MainActivity) {
                    ((MainActivity) context).showLikedSnackbar();
                }
            }
        });
        cellFeedViewHolder.ivUserProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFeedItemClickListener.onProfileClick(view);
            }
        });
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        ((CellFeedViewHolder) viewHolder).bindView(feedItems.get(position));

        if (getItemViewType(position) == VIEW_TYPE_LOADER) {
            bindLoadingFeedItem((LoadingCellFeedViewHolder) viewHolder);
        }
    }

    private void bindLoadingFeedItem(final LoadingCellFeedViewHolder holder) {
        holder.loadingFeedItemView.setOnLoadingFinishedListener(new LoadingFeedItemView.OnLoadingFinishedListener() {
            @Override
            public void onLoadingFinished() {
                showLoadingView = false;
                notifyItemChanged(0);
            }
        });
        holder.loadingFeedItemView.startLoading();
    }

    @Override
    public int getItemViewType(int position) {
        if (showLoadingView && position == 0) {
            return VIEW_TYPE_LOADER;
        } else {
            return VIEW_TYPE_DEFAULT;
        }
    }

    @Override
    public int getItemCount() {
        return feedItems.size();
    }

    public void updateItems(boolean animated) {
        System.out.println("\nI'm executed again");
        feedItems.clear();
        feedItems.addAll(Arrays.asList(
                new FeedItem(33, false),
                new FeedItem(1, false),
                new FeedItem(223, false),
                new FeedItem(2, false),
                new FeedItem(6, false),
                new FeedItem(8, false),
                new FeedItem(99, false)
        ));

//        feedItems1.clear();
//        feedItems1.addAll(Arrays.asList(
//                new FeedItem(33, false),
//                new FeedItem(1, false),
//                new FeedItem(223, false),
//                new FeedItem(2, false),
//                new FeedItem(6, false),
//                new FeedItem(8, false),
//                new FeedItem(99, false)
//        ));

        if (animated) {
            notifyItemRangeInserted(0, feedItems.size());
        } else {
            notifyDataSetChanged();
        }
    }

    public void setOnFeedItemClickListener(OnFeedItemClickListener onFeedItemClickListener) {
        this.onFeedItemClickListener = onFeedItemClickListener;
    }

    public void showLoadingView() {
        showLoadingView = true;
        notifyItemChanged(0);
    }

    public static class CellFeedViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.ivFeedCenter)
        ImageView ivFeedCenter;
        @BindView(R.id.description)
        TextView description;
//        @BindView(R.id.ivFeedBottom)
//        ImageView ivFeedBottom;
        @BindView(R.id.btnComments)
        ImageButton btnComments;
        @BindView(R.id.btnLike)
        ImageButton btnLike;
        @BindView(R.id.btnMore)
        ImageButton btnMore;
        @BindView(R.id.vBgLike)
        View vBgLike;
        @BindView(R.id.ivLike)
        ImageView ivLike;
        @BindView(R.id.tsLikesCounter)
        TextSwitcher tsLikesCounter;
        @BindView(R.id.ivUserProfile)
        ImageView ivUserProfile;
        @BindView(R.id.vImageRoot)
        FrameLayout vImageRoot;

        FeedItem feedItem;

        public CellFeedViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        private void addToHead(FeedItem f){
            int size = feedItems.size();
            feedItems.add(f);
            for(int i = size-1; i >= 0; i--){
                feedItems.set(i+1,feedItems.get(i));
            }
            feedItems.set(0,f);
        }

        public void bindView(FeedItem feedItem) {
            this.feedItem = feedItem;
            int adapterPosition = getAdapterPosition();
//            ivFeedCenter.setImageResource(adapterPosition % 2 == 0 ? R.drawable.img_feed_center_1 : R.drawable.img_feed_center_2);
            if(validFeedItem == 7 && PublishActivity.btm != null && PublishActivity.btm != queue[0]){
                validFeedItem++;
                queue[0] = PublishActivity.btm;
                bottom[0] = PublishActivity.dcp;
//                feedItems.add(new FeedItem(random.nextInt(400),false));
//                feedItems1.push(new FeedItem(random.nextInt(400),false));
                addToHead(new FeedItem(random.nextInt(400),false));
            }
            else if(PublishActivity.btm != null && PublishActivity.btm != queue[validFeedItem-8]) {
                validFeedItem++;
                queue[validFeedItem-8] = PublishActivity.btm;
                bottom[validFeedItem-8] = PublishActivity.dcp;
//                feedItems.add(new FeedItem(random.nextInt(400),false));
//                feedItems1.push(new FeedItem(random.nextInt(400),false));
                addToHead(new FeedItem(random.nextInt(400),false));
            }

            int relatedPosition = adapterPosition % validFeedItem;

            System.out.println("related position is "+relatedPosition+"\nvalid feed item is "+validFeedItem);
            if (relatedPosition >= validFeedItem - 7 && relatedPosition <= validFeedItem - 1) {
                if(relatedPosition == validFeedItem-7) {
                    ivFeedCenter.setImageResource(R.drawable.img_feed_center_1);
                    description.setText("Lagrant: 强烈安利");
                }
                else if(relatedPosition == validFeedItem-6) {
                    ivFeedCenter.setImageResource(R.drawable.img_feed_center_2);
                    description.setText("Lagrant: 一看就知道一位其貌不扬的大佬");
                }
                else if(relatedPosition == validFeedItem-5) {
                    ivFeedCenter.setImageResource(R.drawable.img_feed_center_3);
                    description.setText("Lagrant: 大概是在做吃的？");
                }
                else if(relatedPosition == validFeedItem-4) {
                    ivFeedCenter.setImageResource(R.drawable.img_feed_center_4);
                    description.setText("Lagrant: 虽然不知道他在干什么，但感觉很厉害");
                }
                else if(relatedPosition == validFeedItem-3) {
                    ivFeedCenter.setImageResource(R.drawable.img_feed_center_5);
                    description.setText("Lagrant: 工人老师傅");
                }
                else if(relatedPosition == validFeedItem-2) {
                    ivFeedCenter.setImageResource(R.drawable.img_feed_center_6);
                    description.setText("Lagrant: 还有外国的手工艺者们");
                }
                else if(relatedPosition == validFeedItem-1) {
                    ivFeedCenter.setImageResource(R.drawable.img_feed_center_7);
                    description.setText("Lagrant: 节目很不错");
                }
            }

                for(int i = validFeedItem - 8; i >= 0; i--){
                    if(PublishActivity.btm != null && i == relatedPosition) {
                        ivFeedCenter.setImageBitmap(queue[validFeedItem - 8 - i]);
                        description.setText(bottom[validFeedItem - 8 - i]);
                    }
            }
//            switch(adapterPosition % validFeedItem) {
//                case 0:
////                    ivFeedCenter.setImageResource(R.drawable.img_feed_center_1);
//
//                    if(PublishActivity.btm != null){
//                        ivFeedCenter.setImageBitmap(PublishActivity.btm);
//                    }
//                    else ivFeedCenter.setImageResource(R.drawable.img_feed_center_1);
//                    break;
//                case 1:
//                    ivFeedCenter.setImageResource(R.drawable.img_feed_center_2);
//                    break;
//                case 2:
//                    ivFeedCenter.setImageResource(R.drawable.img_feed_center_3);
//                    break;
//                case 3:
//                    ivFeedCenter.setImageResource(R.drawable.img_feed_center_4);
//                    break;
//                case 4:
//                    ivFeedCenter.setImageResource(R.drawable.img_feed_center_5);
//                    break;
//                case 5:
//                    ivFeedCenter.setImageResource(R.drawable.img_feed_center_6);
//                    break;
//                case 6:
//                    ivFeedCenter.setImageResource(R.drawable.img_feed_center_7);
//                    break;
//            }
//            ivFeedBottom.setImageResource(adapterPosition % 2 == 0 ? R.drawable.img_feed_bottom_1 : R.drawable.img_feed_bottom_2);
//            switch (adapterPosition % 7) {
//                case 0:
//                    ivFeedBottom.setImageResource(R.drawable.img_feed_bottom_1);
//                    break;
//                case 1:
//                    ivFeedBottom.setImageResource(R.drawable.img_feed_bottom_2);
//                    break;
//                case 2:
//                    ivFeedBottom.setImageResource(R.drawable.img_feed_bottom_3);
//                    break;
//                case 3:
//                    ivFeedBottom.setImageResource(R.drawable.img_feed_bottom_4);
//                    break;
//                case 4:
//                    ivFeedBottom.setImageResource(R.drawable.img_feed_bottom_5);
//                    break;
//                case 5:
//                    ivFeedBottom.setImageResource(R.drawable.img_feed_bottom_6);
//                    break;
//                case 6:
//                    ivFeedBottom.setImageResource(R.drawable.img_feed_bottom_7);
//                    break;
//            }
            btnLike.setImageResource(feedItem.isLiked ? R.drawable.ic_heart_red : R.drawable.ic_heart_outline_grey);
            tsLikesCounter.setCurrentText(vImageRoot.getResources().getQuantityString(
                    R.plurals.likes_count, feedItem.likesCount, feedItem.likesCount
            ));
        }

        public FeedItem getFeedItem() {
            return feedItem;
        }
    }

    public static class LoadingCellFeedViewHolder extends CellFeedViewHolder {

        LoadingFeedItemView loadingFeedItemView;

        public LoadingCellFeedViewHolder(LoadingFeedItemView view) {
            super(view);
            this.loadingFeedItemView = view;
        }

        @Override
        public void bindView(FeedItem feedItem) {
            super.bindView(feedItem);
        }
    }

    public static class FeedItem {
        public int likesCount;
        public boolean isLiked;

        public FeedItem(int likesCount, boolean isLiked) {
            this.likesCount = likesCount;
            this.isLiked = isLiked;
        }
    }

    public interface OnFeedItemClickListener {
        void onCommentsClick(View v, int position);

        void onMoreClick(View v, int position);

        void onProfileClick(View v);
    }
}
