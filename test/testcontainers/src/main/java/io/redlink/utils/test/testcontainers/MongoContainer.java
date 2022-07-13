/*
 * Copyright (c) 2018-2022 Redlink GmbH.
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

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.wait.strategy.WaitStrategy;
import org.testcontainers.shaded.org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.concurrent.Future;

public class MongoContainer extends GenericContainer<MongoContainer> {

    private static final String DEFAULT_IMAGE = "mongo";
    private static final String DEFAULT_TAG = "3.6";

    private static final Integer MONGO_PORT = 27017;

    private String databaseName = null;

    public MongoContainer() {
        this(DEFAULT_IMAGE + ":" + DEFAULT_TAG);
    }

    public MongoContainer(String dockerImageName) {
        super(dockerImageName);
    }

    public MongoContainer(Future<String> image) {
        super(image);
    }

    @Override
    protected void configure() {
        super.configure();

        addExposedPort(MONGO_PORT);
    }

    public MongoContainer withDatabaseName(final String databaseName) {
        this.databaseName = databaseName;
        return self();
    }

    @Override
    protected WaitStrategy getWaitStrategy() {
        return Wait.forLogMessage(".*waiting for connections on port.*\n", 1);
    }

    public String getConnectionUrl() {
        return "mongodb://" + getHost() + ":" + getMappedPort(MONGO_PORT)
                + StringUtils.defaultString(prependIfNotBlank(this.databaseName, "/"));
    }

    private static String prependIfNotBlank(String value, String prepend) {
        if (StringUtils.isNotBlank(value)) {
            return prepend + value;
        } else {
            return value;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MongoContainer)) return false;
        if (!super.equals(o)) return false;
        MongoContainer that = (MongoContainer) o;
        return Objects.equals(databaseName, that.databaseName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), databaseName);
    }
}
