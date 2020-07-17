package com.android.babble.adapter;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.babble.R;
import com.android.babble.model.NewPostData;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class NewPostsAdapter extends RecyclerView.Adapter<NewPostsAdapter.ViewHolder>{

    private List<NewPostData> newPostData;
    private FirebaseUser user;
    private Context context;


    public NewPostsAdapter(List<NewPostData> postData, Context context) {
        this.newPostData = postData;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.list_new_post,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        NewPostData postData = newPostData.get(position);
        holder.usernameTextView.setText(postData.getUserName());
        holder.postTextView.setText(postData.getPostText());
        holder.timePlaceTextView.setText(postData.getFullAddress());
        holder.scoreTextView.setText(String.valueOf(postData.getScore()));
        downloadProfilePicture(holder.profileImage,postData.getUserPic());

    }

    @Override
    public int getItemCount() {
        return newPostData.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView usernameTextView, postTextView, timePlaceTextView,scoreTextView,replyButton;
        private ImageView upvoteButton, downvoteButton;
        private CircleImageView profileImage;

        ViewHolder(View itemView) {
            super(itemView);

            postTextView = itemView.findViewById(R.id.post_text);
            usernameTextView = itemView.findViewById(R.id.username);
            timePlaceTextView = itemView.findViewById(R.id.post_time_and_place);
            scoreTextView = itemView.findViewById(R.id.score);

            upvoteButton = itemView.findViewById(R.id.ic_upvote);
            downvoteButton = itemView.findViewById(R.id.ic_downvote);

            replyButton = itemView.findViewById(R.id.reply);
            profileImage = itemView.findViewById(R.id.circleView);

            upvoteButton.setOnClickListener(this);
            downvoteButton.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {

            if (v.getId() == upvoteButton.getId()) {

            }

        }

    }

    private CharSequence convertTimestamp(String timestamp){
        return DateUtils.getRelativeTimeSpanString(Long.parseLong(timestamp),System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS);
    }

    private void downloadProfilePicture(CircleImageView profileImage, String profile_pic_path) {
        Glide.with(profileImage.getContext())
                .load(profile_pic_path)
                .placeholder(R.drawable.ic_default_profile_avatar)
                .thumbnail(
                        Glide.with(profileImage.getContext())
                                .load(profile_pic_path)
                                .override(50,50))
                .into(profileImage);
    }
}