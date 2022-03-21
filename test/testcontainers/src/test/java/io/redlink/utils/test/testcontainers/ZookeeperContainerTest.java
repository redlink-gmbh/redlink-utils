/*
 * Copyright (c) 2020-2022 Redlink GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.redlink.utils.test.testcontainers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.shaded.org.apache.commons.lang.StringUtils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public class ZookeeperContainerTest {

    private static final Logger LOG = LoggerFactory.getLogger(ZookeeperContainerTest.class);

    @Test
    public void testRUOK() throws IOException {
        try (ZookeeperContainer zkContainer = new ZookeeperContainer()) {
            zkContainer.start();

            try (Socket s = new Socket(zkContainer.getContainerIpAddress(), zkContainer.getMappedPort(ZookeeperContainer.CONNECT_PORT))) {
                s.setKeepAlive(true);

                try (OutputStream os = s.getOutputStream()) {
                    os.write("ruok\n".getBytes());

                    try (BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()))) {
                        assertThat("RUOK", br.readLine(), Matchers.is("imok"));
                    }
                }
            }
        }
    }

    @Test
    public void testZkConnect() throws IOException, InterruptedException {
        try (ZookeeperContainer zkContainer = new ZookeeperContainer()) {
            zkContainer.start();

            final CountDownLatch latch = new CountDownLatch(1);
            final Watcher watcher = watchedEvent -> {
                LOG.info("zkEvent: {}", watchedEvent);
                if (watchedEvent.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    latch.countDown();
                }
            };

            try (ZooKeeper zooKeeper = new ZooKeeper(zkContainer.getZkConnect(), 5000, watcher)) {
                assertTrue("Connect", latch.await(5, TimeUnit.SECONDS));
                assertThat("State: Connected", zooKeeper.getState(), Matchers.is(ZooKeeper.States.CONNECTED));
            }

        }
    }

    @Test
    public void testAdminRUOK() throws IOException {
        try (ZookeeperContainer zkContainer = new ZookeeperContainer()) {
            zkContainer.start();

            final JsonNode json = new ObjectMapper().readTree(new URL(zkContainer.getAdminUrl() + "/ruok"));

            assertThat("command", json.get("command"), isTextNode("ruok"));
            assertThat("error", json.get("error"), isNullNode());
        }
    }

    private static Matcher<JsonNode> isNullNode() {
        return new TypeSafeDiagnosingMatcher<>() {
            @Override
            protected boolean matchesSafely(JsonNode item, Description mismatchDescription) {
                mismatchDescription.appendText(" node of type ")
                        .appendValue(item.getNodeType())
                        .appendText(" containing ")
                        .appendValue(item.textValue());

                return item.getNodeType() == JsonNodeType.NULL;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(" node of type ")
                        .appendValue(JsonNodeType.NULL);

            }
        };
    }

    private static Matcher<JsonNode> isTextNode(String text) {
        return new TypeSafeDiagnosingMatcher<>(JsonNode.class) {
            @Override
            public void describeTo(Description description) {
                description.appendText(" node of type ")
                        .appendValue(JsonNodeType.STRING)
                        .appendText(" containing ")
                        .appendValue(text);
            }

            @Override
            protected boolean matchesSafely(JsonNode item, Description mismatchDescription) {
                mismatchDescription.appendText(" node of type ")
                        .appendValue(item.getNodeType())
                        .appendText(" containing ")
                        .appendValue(item.textValue());

                return item.getNodeType() == JsonNodeType.STRING &&
                        StringUtils.equals(item.asText(), text);
            }
        };
    }


}