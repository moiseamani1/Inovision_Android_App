package capstone.inovision.model;

import android.net.Uri;



public class Photo {

    private String path;
    private Uri uri;

    public Photo(){}



    public Photo(Uri uri) {
        this.uri = uri;
    }


    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }
}
