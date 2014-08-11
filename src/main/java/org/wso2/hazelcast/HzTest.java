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

import com.hazelcast.config.*;
import com.hazelcast.core.*;

import java.util.Map;

/**
 * TODO: class level comment
 */
public class HzTest {

    private static final String DOMAIN = "wso2.hztest.domain";
    private static final String HZ_MAP = "wso2.hztest.map";
    private static IMap<String, TestSerializable> map;

    public static void init() {
        String localMemberHost = System.getProperty("localMemberHost", "127.0.0.1");
        String localMemberPort = System.getProperty("localMemberPort", "4000");
        String remoteMember = System.getProperty("remoteMember", "127.0.0.1:4000");

        Config config = new Config();
        GroupConfig groupConfig = config.getGroupConfig();
        groupConfig.setName(DOMAIN);

        NetworkConfig nwConfig = config.getNetworkConfig();
        nwConfig.setPublicAddress(localMemberHost);
        nwConfig.setPort(Integer.parseInt(localMemberPort));

        JoinConfig join = nwConfig.getJoin();
        join.getMulticastConfig().setEnabled(false);
        TcpIpConfig tcpIpConfig = join.getTcpIpConfig();
        tcpIpConfig.setEnabled(true);
        tcpIpConfig.addMember(remoteMember);

        HazelcastInstance hzInstance = Hazelcast.newHazelcastInstance(config);
        hzInstance.getCluster().addMembershipListener(new MembershipListener() {
            @Override
            public void memberAdded(MembershipEvent membershipEvent) {
                Member member = membershipEvent.getMember();
                System.out.println("Member joined [" + member.getUuid() + "]: " + member.getInetSocketAddress().toString());
            }

            @Override
            public void memberRemoved(MembershipEvent membershipEvent) {
                Member member = membershipEvent.getMember();
                System.out.println("Member left [" + member.getUuid() + "]: " + member.getInetSocketAddress().toString());
            }
        });
        map = hzInstance.getMap(HZ_MAP);
    }

    public static void cleanup() {
        long start = System.currentTimeMillis();
        System.out.println("Started map cleanup...");
        for (Map.Entry<String, TestSerializable> entry : map.entrySet()) {
            map.removeAsync(entry.getKey());
        }
        System.out.println("Cleanup completed in " + (System.currentTimeMillis() - start) + "ms");
    }

    public static void putItem(String key) {
        long start = System.currentTimeMillis();
        AnotherTestSerializable anotherTestSerializable = new AnotherTestSerializable(key, System.currentTimeMillis());
        map.set(key,
                new TestSerializable(anotherTestSerializable, key, System.currentTimeMillis()));
        System.out.println("Hz map put took " + (System.currentTimeMillis() - start) + "ms");
    }

    public static void getItem(String key) {
        long start = System.currentTimeMillis();
        System.out.println(key + "=" + map.get(key));
        System.out.println("Hz map get took " + (System.currentTimeMillis() - start) + "ms");
    }
}
