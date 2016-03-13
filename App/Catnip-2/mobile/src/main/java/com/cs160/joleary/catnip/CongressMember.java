package com.cs160.joleary.catnip;

import com.google.gson.JsonObject;

/**
 * Created by namhyun on 3/2/16.
 */
public class CongressMember {
    String name;
    String party;
    String email;
    String homepageURL;
    String termEndDate;
    String tweet;
    String tweetTimeStamp;

    public CongressMember(String name, String party, String email, String homepageURL, String termEndDate) {
        this.name = name;
        this.party = party;
        this.email = email;
        this.homepageURL = homepageURL;
        this.termEndDate = termEndDate;
        this.tweet = "I love America";
        this.tweetTimeStamp = "4h";
    }

    public static CongressMember congressMemberFromJSON(JsonObject jsonObject) {
        return null;
    }
}
