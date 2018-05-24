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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 *
 * @author asenf
 */
public class LargeScaleTest implements Runnable {

    private void error(String message) {
        System.err.println(message);
        System.exit(1);
    }    
    
    private final String indexPath, server;
    private final int numThreads, numRequests;
    
    public LargeScaleTest(int nThreads,
                          int nRequests,
                          String indexPath,
                          String server) {
        
        this.numThreads = nThreads;
        this.numRequests = nRequests;
        this.indexPath = indexPath;
        this.server = server;
    }
    
    /*
     * Execute Test
     */
    @Override
    public void run() {

        // Prepare Sequences
        System.out.println("Randomly selecting " + this.numRequests + " sequences from " + this.indexPath);
        byte[][] querySequences = new byte[this.numRequests][100];
        
        try {
            // Get Sequences from Index
            RandomAccessFile indexFile = new RandomAccessFile(indexPath, "r");
            
            // Get file Size
            long size = indexFile.length() - 100; // Subtract query sequence length
            
            // Randomly select query sequences out of the index file
            Random r = new Random();
            for (int i=0; i<this.numRequests; i++) {
                long pos = Math.abs(r.nextLong()) % size;
                indexFile.seek(pos);
                indexFile.read(querySequences[i]);
            }
            
            // Close file
            indexFile.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(LargeScaleTest.class.getName()).log(Level.SEVERE, null, ex);
            return;
        } catch (IOException ex) {
            Logger.getLogger(LargeScaleTest.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        // Preparation done
        System.out.println("Preparations done. Running tests!");
        
        try {
            // Run the tests
            testServer(querySequences);
        } catch (Exception ex) {
            Logger.getLogger(LargeScaleTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /*
     * Testing Server
     */
    private void testServer(byte[][] querySequences) throws Exception {
        
        OkHttpClient client = SSLUtilities.getUnsafeOkHttpClient();
        
        ArrayList<Future<?>> results = new ArrayList<>();
        ArrayList<TestWorkerLST> workers = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        
        long totalVolume = 0;
        for (int i=0; i<numRequests; i++) {
            String querySequence = new String(querySequences[i]);
            String queryUrl = this.server + "/v1/proc?seq=" + querySequence;
            
            TestWorkerLST worker = new TestWorkerLST(i, client, queryUrl);
            workers.add(worker);
            results.add(executor.submit(worker));
        }
        
        long dt = System.currentTimeMillis();
        System.out.println("Wait for Completion");
        boolean wait = true;
        while (wait) {
            try {Thread.sleep(250);} catch (InterruptedException ex) {}
            wait = false;
            for (int j=0; j<results.size(); j++) {
                if (!results.get(j).isDone())
                    wait = true;
            }
        }
        dt = System.currentTimeMillis() - dt;
        
        // Done
        System.out.println("Done");
    }
    
}
