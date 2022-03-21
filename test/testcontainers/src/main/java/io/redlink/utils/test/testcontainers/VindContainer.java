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

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.FailureDetectingExternalResource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

public class VindContainer extends FailureDetectingExternalResource {

    private static final Logger LOG = LoggerFactory.getLogger(VindContainer.class);

    public static final String DEFAULT_IMAGE = "redlinkgmbh/vind-solr-server";
    public static final String DEFAULT_TAG = "latest";
    public static final String VIND_CORE_NAME = "vind";

    public static final Integer SOLR_PORT = 8983;

    private final GenericContainer<?> container;
    private final Duration startupTimeout;

    private final Set<String> coreNames;

    protected VindContainer(String image, Collection<String> coreNames, Duration startupTimeout) {
        container = new GenericContainer<>(image);
        this.coreNames = new HashSet<>(coreNames);
        this.startupTimeout = startupTimeout == null ? Duration.ofSeconds(15) : startupTimeout;
    }

    @Override
    protected void starting(Description description) {
        super.starting(description);

        try {
            before();
        } catch (Exception t) {
            if(LOG.isDebugEnabled()) {
                LOG.error("Failed to initialize VindContainer", t);
            } else {
                LOG.error("Failed to initialize VindContainer({} - {})", t.getClass().getSimpleName(), t.getMessage());
            }
            Assert.fail("Failed to initialize (" + t.getClass().getSimpleName() + " - " + t.getMessage() + ")");
        }
    }

    protected void before() {
        container.addExposedPort(SOLR_PORT);
        container.addEnv("CORES", String.join(",", coreNames));

        final String coreNamesRegex = coreNames.stream()
                .map(Pattern::quote)
                .collect(Collectors.joining("|", "(", ")"));
        container.waitingFor(
                Wait.forLogMessage(".*SolrCore \\Q[\\E" + coreNamesRegex + "\\Q]\\E(?: )+Registered new searcher.*\n", coreNames.size())
                        .withStartupTimeout(startupTimeout)
        );

        container.start();
    }

    @Override
    protected void finished(Description description) {
        super.finished(description);

        after();
    }

    protected void after() {
        container.stop();
    }

    public Set<String> getCoreNames() {
        return Collections.unmodifiableSet(coreNames);
    }

    public String getSolrUrl() {
        return String.format("http://%s:%d/solr", container.getContainerIpAddress(), container.getMappedPort(SOLR_PORT));
    }

    public String getCoreUrl(String coreName) {
        return String.format("%s/%s", getSolrUrl(), coreName);
    }

    public String getVindUrl() {
        return getCoreUrl(VIND_CORE_NAME);
    }

    public static VindContainer create() {
        return create(null, Collections.emptySet());
    }

    public static VindContainer create(Duration startupTimeout) {
        return create(startupTimeout, Collections.emptySet());
    }

    public static VindContainer create(Collection<String> collectionNames) {
        return create(DEFAULT_IMAGE + ":" + DEFAULT_TAG, null, collectionNames);
    }
    public static VindContainer create(Duration startupTimeout, Collection<String> collectionNames) {
        return create(DEFAULT_IMAGE + ":" + DEFAULT_TAG, startupTimeout, collectionNames);
    }

    public static VindContainer create(String image, Duration startupTimeout) {
        return create(image, startupTimeout, Collections.emptySet());
    }

    public static VindContainer create(String image, String collectionName) {
        return create(image, null, collectionName);
    }

    public static VindContainer create(String image, Duration startupTimeout, String collectionName) {
        return create(image, startupTimeout, Collections.singleton(collectionName));
    }

    public static VindContainer create(String image, Duration startupTimeout, Collection<String> collectionNames) {
        final Set<String> collections = new HashSet<>();
        collections.add(VIND_CORE_NAME);
        collections.addAll(collectionNames);

        return new VindContainer(image, collections, startupTimeout);
    }

}
