/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Endpoints Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloEndpoints
*/

package com.elementary.backend;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.users.User;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * An endpoint class we are exposing
 */
@Api(
        name = "reminderApi",
        version = "v1",
        description = "API for accessing Reminder application.",
        namespace = @ApiNamespace(
                ownerDomain = "backend.myapplication.Nazar.example.com",
                ownerName = "backend.myapplication.Nazar.example.com",
                packagePath = ""
        )
)
public class ReminderApi {

    @ApiMethod(name = "sendNotification", path = "notification", httpMethod = ApiMethod.HttpMethod.POST)
    public ResultMessage sendNotification(User user, NotificationForm form)
            throws IOException, UnauthorizedException {
        if (user == null || user.getEmail() == null || !user.getEmail().equalsIgnoreCase("n.suhovich@gmail.com")) {
            throw new UnauthorizedException("Invalid user");
        }
        HttpClient client = new DefaultHttpClient();
        HttpPost httpRequest = new HttpPost("https://fcm.googleapis.com/fcm/send");
        httpRequest.setHeader("Content-Type", "application/json");
        httpRequest.setHeader("Authorization", "key=AAAAO_3Auzc:APA91bFps2C-ZzxcV9FrPgQ8Ws6RMal73E2OsSrSYq48V_IJHqy0Rjm9UGazFCVCzZ6zDAREP6K5WKuqWTc-MFFY4w0j3OKjZM0pKWmj1gR9oSrPYz52IT_laE0QS4jTWhE15MEhR-DL");
        HttpEntity entity = new ByteArrayEntity(new NotificationBuilder().getMessengerEntity(form));
        httpRequest.setEntity(entity);
        HttpResponse response = client.execute(httpRequest);
        String result = EntityUtils.toString(response.getEntity());
        return new ResultMessage(result);
    }

    @ApiMethod(name = "sendFreeNotification", path = "notification_free", httpMethod = ApiMethod.HttpMethod.POST)
    public ResultMessage sendFreeNotification(User user, NotificationForm form)
            throws IOException, UnauthorizedException {
        if (user == null || user.getEmail() == null || !user.getEmail().equalsIgnoreCase("n.suhovich@gmail.com")) {
            throw new UnauthorizedException("Invalid user");
        }
        HttpClient client = new DefaultHttpClient();
        HttpPost httpRequest = new HttpPost("https://fcm.googleapis.com/fcm/send");
        httpRequest.setHeader("Content-Type", "application/json");
        httpRequest.setHeader("Authorization", "key=AAAANMhxe9Q:APA91bFFtTlH-1Y5KuVbNcoqUX-Gi1M0lg8KC_42ZtdAiWNG0o0aqO4AhBjAt3wHnO3fd8L1ggw2idvoMOJaI3iiICSab_YpyTTA2OLE_NZo-0sSCPeeHR3qVjNas26jHC4WzKTNOPiH");
        HttpEntity entity = new ByteArrayEntity(new NotificationBuilder().getMessengerEntity(form));
        httpRequest.setEntity(entity);
        HttpResponse response2 = client.execute(httpRequest);
        String result2 = EntityUtils.toString(response2.getEntity());
        return new ResultMessage(result2);
    }

}
