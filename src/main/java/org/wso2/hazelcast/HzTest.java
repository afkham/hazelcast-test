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

/**
 * TODO: class level comment
 */
public class HzTest {

    public static final String DOMAIN = "wso2.hztest.domain";
    public static final String HZ_MAP = "wso2.hztest.map";

    public static void main(String[] args) {
        String localMemberHost = args[0];
        String localMemberPort = args[1];
        String remoteMember = args[2];

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

        final IMap<String, TestSerializable> map = hzInstance.getMap(HZ_MAP);

        new Thread() {

            String[] keys = {"a", "b", "c", "d", "e", "f"};
            int keyIndex = 0;

            @Override
            public void run() {
                while (true) {
                    if (keyIndex >= keys.length) {
                        keyIndex = 0;
                    }
                    long start = System.currentTimeMillis();
                    System.out.println(keys[keyIndex] + "=" + map.get(keys[keyIndex]));
                    System.out.println("Hz map get took " + (System.currentTimeMillis() - start) + "ms");

                    start = System.currentTimeMillis();

                    AnotherTestSerializable anotherTestSerializable = new AnotherTestSerializable(keys[keyIndex], System.currentTimeMillis());
                    map.put(keys[keyIndex],
                            new TestSerializable(anotherTestSerializable,keys[keyIndex], System.currentTimeMillis()));
                    System.out.println("Hz map put took " + (System.currentTimeMillis() - start) + "ms");

                    keyIndex++;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();

    }
}
