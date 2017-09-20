package utils;

import com.google.android.gms.ads.NativeExpressAdView;

import java.io.Serializable;

/**
 * Created by Aisha on 8/31/2017.
 */

public class HealthFact implements Serializable{


    private String mHealthFactTitle;
    private String mHealthFactFull;
    private String mHealthFactTag;
    private String mHealthFactDate;
    private String mHealthFactID;

    private int mHealthFactLikes ,objectType;
    boolean pushNotification;

    transient NativeExpressAdView nativeExpressAdView;

    public String getmHealthImageAddress() {
        return mHealthImageAddress;
    }

    public void setmHealthImageAddress(String mHealthImageAddress) {
        this.mHealthImageAddress = mHealthImageAddress;
    }

    private String mHealthImageAddress;

    public String getmHealthFactTitle() {
        return mHealthFactTitle;
    }

    public void setmHealthFactTitle(String mHealthFactTitle) {
        this.mHealthFactTitle = mHealthFactTitle;
    }

    public String getmHealthFactFull() {
        return mHealthFactFull;
    }

    public void setmHealthFactFull(String mHealthFactFull) {
        this.mHealthFactFull = mHealthFactFull;
    }

    public String getmHealthFactTag() {
        return mHealthFactTag;
    }

    public void setmHealthFactTag(String mHealthFactTag) {
        this.mHealthFactTag = mHealthFactTag;
    }

    public String getmHealthFactDate() {
        return mHealthFactDate;
    }

    public void setmHealthFactDate(String mHealthFactDate) {
        this.mHealthFactDate = mHealthFactDate;
    }

    public int getmHealthFactLikes() {
        return mHealthFactLikes;
    }

    public void setmHealthFactLikes(int mHealthFactLikes) {
        this.mHealthFactLikes = mHealthFactLikes;
    }

    public String getmHealthFactID() {
        return mHealthFactID;
    }

    public void setmHealthFactID(String mHealthFactID) {
        this.mHealthFactID = mHealthFactID;
    }

    public boolean isPushNotification() {
        return pushNotification;
    }

    public void setPushNotification(boolean pushNotification) {
        this.pushNotification = pushNotification;
    }

    public int getObjectType() {
        return objectType;
    }

    public void setObjectType(int objectType) {
        this.objectType = objectType;
    }

    public NativeExpressAdView getNativeExpressAdView() {
        return nativeExpressAdView;
    }

    public void setNativeExpressAdView(NativeExpressAdView nativeExpressAdView) {
        this.nativeExpressAdView = nativeExpressAdView;
    }
}
