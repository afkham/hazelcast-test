/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.hazelcast;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * TODO: class level comment
 */
public class HzTestServlet extends HttpServlet {
//    private static final Log log = LogFactory.getLog(MainServlet.class);

    @Override
    public void init(ServletConfig config) throws ServletException {
        HzTest.init();
        ThreadFactory threadFactory = new ThreadFactory() {
            @Override
            public Thread newThread(Runnable runnable) {
                Thread th = new Thread(runnable);
                th.setName("CacheExpirySchedulerThread");
                return th;
            }
        };
        ScheduledExecutorService cacheExpiryScheduler =
                Executors.newScheduledThreadPool(10, threadFactory);
        cacheExpiryScheduler.scheduleWithFixedDelay(new CleanupTask(), 30, 30, TimeUnit.SECONDS);
    }
}
