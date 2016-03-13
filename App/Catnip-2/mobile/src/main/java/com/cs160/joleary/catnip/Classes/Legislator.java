package com.cs160.joleary.catnip.Classes;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
/**
 * Created by namhyun on 3/9/16.
 */
public class Legislator implements Parcelable {
    public String id;
    public String firstName;
    public String lastName;
    public String birthday;
    public String chamber;
    public String website;
    public String email;
    public String party;
    public String title;
    public String twitterID;
    public String termEndDate;
    public String zipCodeString="";
    public String state;
    public String county;

    public Legislator() {

    }

    public static Legislator legislatorFromJson(JsonObject json) {
        Legislator legislator = new Legislator();
        legislator.id =         getStringFromJSON(json, "bioguide_id");
        legislator.firstName =  getStringFromJSON(json, "first_name");
        legislator.lastName =   getStringFromJSON(json, "last_name");
        legislator.birthday =   getStringFromJSON(json, "birthday");
        legislator.chamber =    getStringFromJSON(json, "chamber");
        legislator.website =    getStringFromJSON(json, "website");
        legislator.email =      getStringFromJSON(json, "oc_email");
        legislator.party =      getStringFromJSON(json, "party");
        legislator.title =      getStringFromJSON(json, "title");
        legislator.twitterID =  getStringFromJSON(json, "twitter_id");
        legislator.termEndDate = getStringFromJSON(json, "term_end");
        return legislator;
    }

    public String getFullTitleName() {
        return title+". "+firstName+" "+lastName+" ("+party+")";
    }

    private static String getStringFromJSON(JsonObject json, String key) {
        JsonElement jsonElement = json.get(key);
        if (jsonElement.isJsonNull()) {
            return null;
        }
        return jsonElement.getAsString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // write your object's data to the passed-in Parcel
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(id);
        out.writeString(firstName);
        out.writeString(lastName);
        out.writeString(birthday);
        out.writeString(chamber);
        out.writeString(website);
        out.writeString(email);
        out.writeString(party);
        out.writeString(title);
        out.writeString(twitterID);
        out.writeString(termEndDate);
        out.writeString(zipCodeString);
        out.writeString(state);
        out.writeString(county);
    }

    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<Legislator> CREATOR = new Parcelable.Creator<Legislator>() {
        public Legislator createFromParcel(Parcel in) {
            return new Legislator(in);
        }

        public Legislator[] newArray(int size) {
            return new Legislator[size];
        }
    };

    // example constructor that takes a Parcel and gives you an object populated with it's values
    private Legislator(Parcel in) {
        id = in.readString();
        firstName = in.readString();
        lastName = in.readString();
        birthday = in.readString();
        chamber = in.readString();
        website = in.readString() ;
        email= in.readString();
        party= in.readString();
        title= in.readString();
        twitterID= in.readString();
        termEndDate=in.readString();
        zipCodeString=in.readString();
        state=in.readString();
        county=in.readString();
    }
}