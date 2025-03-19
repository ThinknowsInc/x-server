package com.thinknows.x_server.model.response;

import java.time.LocalDateTime;
import java.util.List;

public class UserProfileResponse {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private String fullName;
    private String avatar;
    private String bio;
    private LocalDateTime joinDate;
    private int followersCount;
    private int followingCount;
    private List<String> interests;
    private String location;
    private String website;
    private boolean verified;

    public UserProfileResponse() {
    }

    public UserProfileResponse(Long id, String username, String email, String phone, String fullName, String avatar,
                              String bio, LocalDateTime joinDate, int followersCount, int followingCount,
                              List<String> interests, String location, String website, boolean verified) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.fullName = fullName;
        this.avatar = avatar;
        this.bio = bio;
        this.joinDate = joinDate;
        this.followersCount = followersCount;
        this.followingCount = followingCount;
        this.interests = interests;
        this.location = location;
        this.website = website;
        this.verified = verified;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public LocalDateTime getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(LocalDateTime joinDate) {
        this.joinDate = joinDate;
    }

    public int getFollowersCount() {
        return followersCount;
    }

    public void setFollowersCount(int followersCount) {
        this.followersCount = followersCount;
    }

    public int getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(int followingCount) {
        this.followingCount = followingCount;
    }

    public List<String> getInterests() {
        return interests;
    }

    public void setInterests(List<String> interests) {
        this.interests = interests;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }
}
