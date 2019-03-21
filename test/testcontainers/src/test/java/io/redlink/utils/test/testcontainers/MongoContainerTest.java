/*
 * Copyright 2019 redlink GmbH
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

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.hamcrest.Matchers;
import org.junit.Test;

import static org.junit.Assert.*;

public class MongoContainerTest {


    @Test
    public void testDefaultDatabase() {
        try (MongoContainer mongoContainer = new MongoContainer()) {
            mongoContainer.start();
            try (MongoClient mongo = MongoClients.create(mongoContainer.getConnectionUrl())) {
                final MongoDatabase testDB = mongo.getDatabase("test");
                final Document ping = testDB.runCommand(new BasicDBObject("ping", "1"));

                assertNotNull("not null", ping);
                assertThat("status", ping.keySet(), Matchers.contains("ok"));
                assertEquals("value", ping.getDouble("ok"), 1, 1e-6);
            }
        }
    }

    @Test
    public void testCustomDatabase() {
        try (MongoContainer mongoContainer = new MongoContainer().withDatabaseName("foo")) {
            mongoContainer.start();
            try (MongoClient mongo = MongoClients.create(mongoContainer.getConnectionUrl())) {
                final MongoDatabase testDB = mongo.getDatabase("foo");
                final Document ping = testDB.runCommand(new BasicDBObject("ping", "1"));

                assertNotNull("not null", ping);
                assertThat("status", ping.keySet(), Matchers.contains("ok"));
                assertEquals("value", ping.getDouble("ok"), 1, 1e-6);
            }
        }
    }
}
