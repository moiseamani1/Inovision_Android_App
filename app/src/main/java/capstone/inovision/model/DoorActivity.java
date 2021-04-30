package capstone.inovision.model;

public class DoorActivity {

    String contactId,dateTime,name,timeZone,location;

    public DoorActivity(){

    }

    public DoorActivity(String contactId, String dateTime, String name, String timeZone, String location) {
        this.contactId = contactId;
        this.dateTime = dateTime;
        this.name = name;
        this.timeZone = timeZone;
        this.location = location;
    }

    public String getContactId() {
        return contactId;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
