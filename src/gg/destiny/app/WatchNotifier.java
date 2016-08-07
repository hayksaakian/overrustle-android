package gg.destiny.app;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Hayk on 4/21/2015.
 */
public class WatchNotifier implements
        DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener  {
    public static final String TAG = "Push OverRustle";
    public static final String UPDATE_DATA_PATH = "/set_live";

    GoogleApiClient mGoogleApiClient;
    String notification_data;

    ResultCallback resultCallback;

    public WatchNotifier(String raw_notification_data, Context context) {
        notification_data = raw_notification_data;
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        Log.d(TAG, "Going to start connection.");

//        ConnectionResult connectionResult =
                mGoogleApiClient.connect();
                //blockingConnect(30, TimeUnit.SECONDS);
        Log.d(TAG, "Queued to start connection.");
//        Log.d(TAG, "done connecting, got "+connectionResult.toString()+". Going to start connection.");

        resultCallback = new ResultCallback() {
            @Override
            public void onResult(Result sendMessageResult) {
                if (!sendMessageResult.getStatus().isSuccess()) {
                    Log.e(TAG, "Failed to send message with status code: "
                            + sendMessageResult.getStatus().getStatusCode());
                }else{
                    Log.d(TAG, "Sent! message with status code: "
                            + sendMessageResult.getStatus().getStatusCode());
                }
            }
        };
    }

    private void sendLiveStatusMessage(String nodeId, String message) {
        Log.d(TAG, "Sending "+String.valueOf(message.getBytes().length)+ " bytes: "+message);
        Wearable.MessageApi.sendMessage(
                mGoogleApiClient, nodeId, UPDATE_DATA_PATH, message.getBytes())
                .setResultCallback(resultCallback);
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(TAG, "onConnected: " + connectionHint);

        Log.d(TAG, "sending via DataApi");

        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(UPDATE_DATA_PATH);
        putDataMapReq.getDataMap().putString("raw_notification_data", notification_data);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);

        pendingResult.setResultCallback(resultCallback);

//        SendDataViaMessage();
    }

    public void SendDataViaMessage(){
        Log.d(TAG, "starting to get nodes");

        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient)
        .setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                List<Node> nodes = getConnectedNodesResult.getNodes();
                List<String> results = new ArrayList<String>();
                for (Node node : nodes) {
                    Log.d("Node Found ", node.getId() + ": " + node.getDisplayName());
                    results.add(node.getId());
                }
                Log.d(TAG, "done getting nodes, got " + String.valueOf(nodes.size()) + ". Going to send data.");

                // Now you can use the Data Layer API

                if (nodes.size() == 0) {
                    Log.d(TAG, "Not Enough Nodes! where do I send it?");
                    return;
                } else if (nodes.size() == 1) {
                    try {
                        Log.d("Sending Live Value:", notification_data);
                        sendLiveStatusMessage(results.get(0), notification_data);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    int i = 0;
                    Log.d(TAG, "TOO MANY NODES, where do I send it? all of them?");
                    return;
                }
            }
        });
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(TAG, "onConnectionSuspended: " + cause);

    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.d(TAG, "onConnectionFailed: " + result);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d(TAG, "onDataChanged called on handset, not sure why since the watch is not supposed to send data");
    }
}
