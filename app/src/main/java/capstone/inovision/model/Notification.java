package capstone.inovision.model;

public class Notification {
    public String body,title,type;
    public String date,time,timeZone;


    public Notification(){}
    public Notification(String body, String title,String type,String date,String time,String timeZone) {
        this.body = body;
        this.title = title;
        this.type=type;
        this.date=date;
        this.time=time;
        this.timeZone=timeZone;
    }


    public Notification(String body, String title){
        this.body = body;
        this.title = title;
    }


    public Notification(String body, String title, String type) {
        this.body = body;
        this.title = title;
        this.type=type;
    }


    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }
}
