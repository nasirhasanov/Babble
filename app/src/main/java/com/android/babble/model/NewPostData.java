package com.android.babble.model;

public class NewPostData {

    String postText;
    String userName;
    String userId;
    String userPic;
    String countryCode;
    String countryName;
    String fullAddress;
    int    score;
    String   timeStamp;

    public NewPostData(){}

    public void setPostText(String postText) {
        this.postText = postText;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public void setUserPic(String userPic) {
        this.userPic = userPic;
    }
    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }
    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }
    public void setFullAddress(String fullAddress) {
        this.fullAddress = fullAddress;
    }
    public void setScore(int score) {
        this.score = score;
    }
    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getPostText(){
        return postText;
    }
    public String getUserName(){
        return userName;
    }
    public String getUserId(){
        return userId;
    }
    public String getUserPic(){
        return userPic;
    }
    public String getCountryCode(){
        return countryCode;
    }
    public String getCountryName(){
        return countryName;
    }
    public String getFullAddress(){
        return fullAddress;
    }
    public int getScore() {
        return score;
    }
    public String getTimeStamp() {
        return timeStamp;
    }
}
