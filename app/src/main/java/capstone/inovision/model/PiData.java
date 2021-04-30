package capstone.inovision.model;

public class PiData {

    private boolean isArmed,isDoorOpen,isSpeakerAnnouncementOn,IsPiAndCloudSynced,isPiModified,isCloudModified;


    public PiData() {
    }


    public boolean isArmed() {
        return isArmed;
    }

    public void setArmed(boolean armed) {
        isArmed = armed;
    }

    public boolean isDoorOpen() {
        return isDoorOpen;
    }

    public void setDoorOpen(boolean doorOpen) {
        isDoorOpen = doorOpen;
    }

    public boolean isSpeakerAnnouncementOn() {
        return isSpeakerAnnouncementOn;
    }

    public void setSpeakerAnnouncementOn(boolean speakerAnnouncementOn) {
        isSpeakerAnnouncementOn = speakerAnnouncementOn;
    }

    public boolean isPiAndCloudSynced() {
        return IsPiAndCloudSynced;
    }

    public void setPiAndCloudSynced(boolean piAndCloudSynced) {
        IsPiAndCloudSynced = piAndCloudSynced;
    }

    public boolean isPiModified() {
        return isPiModified;
    }

    public void setPiModified(boolean piModified) {
        isPiModified = piModified;
    }

    public boolean isCloudModified() {
        return isCloudModified;
    }

    public void setCloudModified(boolean cloudModified) {
        isCloudModified = cloudModified;
    }
}
