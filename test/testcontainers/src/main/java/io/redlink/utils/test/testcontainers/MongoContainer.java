/*
 * Copyright (c) 2018 Redlink GmbH.
 */
package io.redlink.utils.test.testcontainers;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.wait.strategy.WaitStrategy;
import org.testcontainers.shaded.org.apache.commons.lang.StringUtils;

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
        return "mongodb://" + getContainerIpAddress() + ":" + getMappedPort(MONGO_PORT)
                + StringUtils.defaultString(prependIfNotBlank(this.databaseName, "/"));
    }

    private static String prependIfNotBlank(String value, String prepend) {
        if (StringUtils.isNotBlank(value)) {
            return prepend + value;
        } else {
            return value;
        }

    }
}
