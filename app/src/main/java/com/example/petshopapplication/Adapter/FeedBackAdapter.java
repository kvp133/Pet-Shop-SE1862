package com.example.petshopapplication.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.petshopapplication.R;
import com.example.petshopapplication.model.FeedBack;
import com.example.petshopapplication.model.User;

import java.util.List;

public class FeedBackAdapter extends RecyclerView.Adapter<FeedBackAdapter.FeedbaclHolder>{


    List<FeedBack> feedBackItems;
    List<User> userItems;
    Context context;


    public FeedBackAdapter(List<FeedBack> feedBackItems, List<User> userItems) {
        this.feedBackItems = feedBackItems;
        this.userItems = userItems;
    }

    @NonNull
    @Override
    public FeedbaclHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View inflate = LayoutInflater.from(context).inflate(R.layout.view_holder_feedback, parent, false);
        return new FeedbaclHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedbaclHolder holder, int position) {
        FeedBack feedback = feedBackItems.get(position);
        User user = getUser(feedback.getUserId());
        holder.tv_feedback_user_name.setText(user.getFullName());
        holder.tv_feedback_content.setText(feedback.getContent());
        holder.tv_feedback_created_at.setText(feedback.getCreatedAt().replace("T", " "));
        holder.rtb_feedback_rating.setRating(feedback.getRating());

        //Load user avatar
        Glide.with(context)
            .load(user.getAvatar())
            .transform(new CenterCrop(), new RoundedCorners(30))
            .into(holder.imv_feedback_user_avatar);
    }

    public User getUser(String userId) {
        for (User user : userItems) {
            if (user.getId().equals(userId)) {
                return user;
            }
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return feedBackItems.size();
    }

    public class FeedbaclHolder extends RecyclerView.ViewHolder {

        ImageView imv_feedback_user_avatar;
        TextView tv_feedback_user_name, tv_feedback_content, tv_feedback_created_at;
        RatingBar rtb_feedback_rating;

        public FeedbaclHolder(@NonNull View itemView) {
            super(itemView);
            imv_feedback_user_avatar = itemView.findViewById(R.id.imv_feedback_user_avatar);
            tv_feedback_user_name = itemView.findViewById(R.id.tv_feedback_username);
            tv_feedback_content = itemView.findViewById(R.id.tv_feedback_content);
            tv_feedback_created_at = itemView.findViewById(R.id.tv_feedback_date);
            rtb_feedback_rating = itemView.findViewById(R.id.rtb_feedback_rating);
        }
    }
}
