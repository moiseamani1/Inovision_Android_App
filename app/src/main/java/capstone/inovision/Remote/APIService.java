package capstone.inovision.Remote;

import capstone.inovision.model.MyResponse;
import capstone.inovision.model.Sender;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers({

            "Content-Type:application/json",
            "Authorization:key=AAAAcb08gPg:APA91bE6NxZDeq-vNXpw36GYNzNMwTxgZiWz7u7BiiLceJ3FnkIg8qQAfbh_-03vzrttMbD1NEFREUKZmkCgQ_Di3i1SrvXDXg8481U85xDlzAET1keys2xCtBMn7P1B6gdJhYDeu7_n"
    })
    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}

