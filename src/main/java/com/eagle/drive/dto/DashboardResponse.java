package com.eagle.drive.dto;

import com.eagle.drive.model.Ride;

import java.util.List;

public class DashboardResponse {

    private UserProfileDTO profile;
    private List<Ride> rides;

    public DashboardResponse() {
    }

    public DashboardResponse(UserProfileDTO profile, List<Ride> rides) {
        this.profile = profile;
        this.rides = rides;
    }

    public UserProfileDTO getProfile() {
        return profile;
    }

    public void setProfile(UserProfileDTO profile) {
        this.profile = profile;
    }

    public List<Ride> getRides() {
        return rides;
    }

    public void setRides(List<Ride> rides) {
        this.rides = rides;
    }
}
