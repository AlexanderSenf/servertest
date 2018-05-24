/*
 * Copyright 2017 ELIXIR EBI
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

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author asenf
 */
public class ServerTest {
    private static final int VERSION_MAJOR = 0;
    private static final int VERSION_MINOR = 1;
    
    private static void error(String message) {
        System.err.println(message);
        System.exit(1);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws NoSuchAlgorithmException, KeyManagementException, InterruptedException {

        try {
            int threads = Integer.parseInt(args[0]);
            int requests = Integer.parseInt(args[1]);
            String indexPath = args[2];
            String server = "http://localhost:9221";
            if (args.length>3)
                server = args[3];
            RLT_Test(threads, requests, indexPath, server); // files, threads, chunks
        } catch (Exception ex) {
            Logger.getLogger(ServerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /*
     * Random Load Testing
     */
    
    private static void RLT_Test(int nThreads, int nRequests, String indexPath, String server) {
        
        LargeScaleTest lst = new LargeScaleTest(
                          nThreads,
                          nRequests,
                          indexPath,
                          server);
        
        // Run
        lst.run();
    }
}
