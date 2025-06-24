package com.example.petshopapplication.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.petshopapplication.R;
import com.example.petshopapplication.UpdateFeedbackActivity;
import com.example.petshopapplication.model.FeedBack;
import com.example.petshopapplication.model.User;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class FeedBackListAdapter extends RecyclerView.Adapter<FeedBackListAdapter.FeedbackHolder> {

    List<FeedBack> feedBackItems;
    List<User> userItems;
    User user;
    Context context;
    int role;

    public FeedBackListAdapter(List<FeedBack> feedBackItems, List<User> userItems, User user) {
        this.feedBackItems = feedBackItems;
        this.userItems = userItems;
        this.user = user;
    }

    @NonNull
    @Override
    public FeedbackHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View inflate = LayoutInflater.from(context).inflate(R.layout.view_holder_feedback_item, parent, false);
        return new FeedbackHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedbackHolder holder, int position) {
        role = user.getRoleId();
        FeedBack feedback = feedBackItems.get(position);

        User fbUser;
        if (!userItems.isEmpty()){
            fbUser = getUser(feedback.getUserId());
        } else {
            fbUser = user;
        }

            // Display elements of feedback
            if (fbUser.getAvatar() != null && !fbUser.getAvatar().isEmpty()) {
                holder.imv_feedback_user_avatar.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(fbUser.getAvatar())
                        .placeholder(R.drawable.icon) // Optional placeholder image
                        .into(holder.imv_feedback_user_avatar);
            }
            holder.tv_feedback_user_name.setText(fbUser.getFullName());
            holder.tv_feedback_content.setText(feedback.getContent());
            holder.tv_feedback_created_at.setText(feedback.getCreatedAt().replace("T", " "));
            holder.rtb_feedback_rating.setRating(feedback.getRating());

            // Load image into ImageView using Glide
            if (feedback.getImageUrl() != null && !feedback.getImageUrl().isEmpty()) {
                holder.imv_feedback_image.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(feedback.getImageUrl())
                        .placeholder(R.drawable.icon) // Optional placeholder image
                        .into(holder.imv_feedback_image);
            }

        // Add options to the spinner
        List<String> options = new ArrayList<>();
        options.add("Select Action:");
        if (role != 1) {
            options.add("Ban");
            options.add("Unban");
        } else {
            // Check if the current user matches the feedback user and if feedback is not marked as deleted
            if (feedback.getUserId().equals(user.getId())) {
                    options.add("Edit");
                    options.add("Delete");
            } else {
                // Hide spinner if conditions are not met
                holder.sp_feedback.setVisibility(View.GONE);
            }
        }

        // Set up spinner
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, options);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.sp_feedback.setAdapter(spinnerAdapter);

        // Handle spinner item selection
        holder.sp_feedback.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedAction = (String) parentView.getItemAtPosition(position);
                switch (selectedAction) {
                    case "Edit":
                        updateFeedback(feedback);
                        break;
                    case "Delete":
                    case "Ban":
                        deleteFeedback(feedback);
                        break;
                    case "Unban":
                        unbanFeedback(feedback);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // No action needed
            }
        });

        if (role != 1 && feedback.isDeleted()) {
            holder.itemView.setAlpha(0.5f);
        }
    }

    public User getUser(String userId) {
        for (User userData : userItems) {
            if (userData.getId().equals(userId)) {
                return userData;
            }
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return feedBackItems.size();
    }

    public class FeedbackHolder extends RecyclerView.ViewHolder {

        ImageView imv_feedback_user_avatar, imv_feedback_image;
        TextView tv_feedback_user_name, tv_feedback_content, tv_feedback_created_at;
        RatingBar rtb_feedback_rating;
        Spinner sp_feedback;

        public FeedbackHolder(@NonNull View itemView) {
            super(itemView);
            imv_feedback_user_avatar = itemView.findViewById(R.id.imv_feedback_user_avatar);
            imv_feedback_image = itemView.findViewById(R.id.imv_feedback_image);
            tv_feedback_user_name = itemView.findViewById(R.id.tv_feedback_username);
            tv_feedback_content = itemView.findViewById(R.id.tv_feedback_content);
            tv_feedback_created_at = itemView.findViewById(R.id.tv_feedback_date);
            rtb_feedback_rating = itemView.findViewById(R.id.rtb_feedback_rating);
            sp_feedback = itemView.findViewById(R.id.sp_feedback);
        }
    }

    private void updateFeedback(FeedBack feedback) {
        Intent intent = new Intent(context, UpdateFeedbackActivity.class);
        intent.putExtra("feedback", feedback);  // Pass feedback object to UpdateFeedbackActivity
        context.startActivity(intent);
    }


    private void deleteFeedback(FeedBack feedback) {
        feedback.setDeleted(true);

        // Save the updated feedback in Firebase (with isDeleted = true)
        DatabaseReference feedbackRef = FirebaseDatabase.getInstance().getReference("feedbacks").child(feedback.getId());
        feedbackRef.setValue(feedback).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(context, "Feedback marked as deleted successfully", Toast.LENGTH_SHORT).show();
                ((Activity) context).finish();
            } else {
                Toast.makeText(context, "Failed to delete feedback", Toast.LENGTH_SHORT).show();
            }
        });
        return;
    }

    private void unbanFeedback(FeedBack feedback) {
        feedback.setDeleted(false);

        // Save the updated feedback in Firebase (with isDeleted = true)
        DatabaseReference feedbackRef = FirebaseDatabase.getInstance().getReference("feedbacks").child(feedback.getId());
        feedbackRef.setValue(feedback).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(context, "Feedback marked as deleted successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Failed to delete feedback", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
