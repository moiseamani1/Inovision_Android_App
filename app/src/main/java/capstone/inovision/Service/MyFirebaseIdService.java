package capstone.inovision.Service;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import capstone.inovision.common.Common;
import capstone.inovision.model.Token;


public class MyFirebaseIdService extends FirebaseMessagingService {

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        String tokenRefreshed=s;
        Log.d("NEW_TOKEN",s);
        if(Common.firebaseCurrentUser!=null){
        updateTokenToFirebase(s);
        }
    }
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
    }

    private void updateTokenToFirebase(String newToken) {
        FirebaseDatabase db= FirebaseDatabase.getInstance();
        DatabaseReference tokens=db.getReference("Tokens");
        Token token=new Token(newToken,false);
        tokens.child(Common.firebaseCurrentUser.getUid()).setValue(token);
    }
}
