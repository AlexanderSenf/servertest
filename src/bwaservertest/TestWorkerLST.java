/*
 * Copyright 2018 asenf.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package bwaservertest;

import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 *
 * @author asenf
 */
public class TestWorkerLST implements Runnable {

    private OkHttpClient client = null;
    
    private int workerId = -1;
    
    private String url = null;
    
    public TestWorkerLST(int workerId, OkHttpClient client,
            String url) {
        this.workerId = workerId;
        this.client = client;
        this.url = url;        
    }

    @Override
    public void run() {
        System.out.println("Worker " + workerId + " started!  " + url);

        // (1) Stream File et MDs
        Request requestRequest = null;        
        requestRequest = new Request.Builder()
                    .url(this.url)
                    .build();
        Response response = null;
        long delta = System.currentTimeMillis();
        try {
            response = client.newCall(requestRequest).execute();
        } catch (IOException ex) {
                System.out.println("An Error has occurred in Thread " + workerId
                    + ": " + ex.toString());
                System.out.println("\t\t" + url);
                response.close();
                return;
        }
        delta = System.currentTimeMillis()-delta;
        
        ResponseBody body = response.body();
        boolean successful = response.isSuccessful();
        
        body.close();
        response.close();

        // Done
        System.out.println("Thread " + workerId + " done in " + delta + " ms.");
    }
    
    public String getUrl() {
        return this.url;
    }
    
    public int getWorkerId() {
        return this.workerId;
    }
    
}
