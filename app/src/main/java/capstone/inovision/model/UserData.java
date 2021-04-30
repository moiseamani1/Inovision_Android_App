package capstone.inovision.model;

public class UserData {

    private String PiId,phone;
    private boolean isTextMessageReceiveOn;

    public UserData() {
    }

    public String getPiId() {
        return PiId;
    }

    public void setPiId(String piId) {
        PiId = piId;
    }


    public boolean isTextMessageReceiveOn() {
        return isTextMessageReceiveOn;
    }

    public void setTextMessageReceiveOn(boolean textMessageReceiveOn) {
        isTextMessageReceiveOn = textMessageReceiveOn;
    }


    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
