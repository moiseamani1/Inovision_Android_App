package capstone.inovision.common;

import android.content.Context;
import android.util.DisplayMetrics;

import com.google.firebase.auth.FirebaseUser;

import capstone.inovision.Remote.APIService;
import capstone.inovision.Remote.RetrofitClient;
import capstone.inovision.model.PiData;
import capstone.inovision.model.UserData;

public class Common {

    public static FirebaseUser firebaseCurrentUser;
    public static UserData firebaseCurrentUserData;
    public static PiData firebaseCurrentPiData;

    public static int calculateNoOfColumns(Context context, float columnWidthDp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float screenWidthDp = displayMetrics.widthPixels / displayMetrics.density;
        return (int) (screenWidthDp / columnWidthDp + 0.5);// returns number of columns and +0.5 to correct integer rounding
    }

    public static final String BASE_URL="https://fcm.googleapis.com/";


    public static APIService getFCMService(){

        return RetrofitClient.getRetrofitClient(BASE_URL).create(APIService.class);

    }
}
