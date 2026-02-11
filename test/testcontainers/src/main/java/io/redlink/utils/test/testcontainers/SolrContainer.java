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

import io.redlink.utils.PathUtils;
import io.redlink.utils.ResourceLoaderUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import org.junit.Assert;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

public class SolrContainer extends TestContainerRule {
    
    private static final Logger LOG = LoggerFactory.getLogger(SolrContainer.class);

    private static final String DEFAULT_IMAGE = "solr";
    private static final String DEFAULT_TAG = "7.7.1";

    private static final Integer SOLR_PORT = 8983;

    private final TemporaryFolder temporaryFolder;
    private final GenericContainer<?> container;
    private final String coreName;
    private final String confDir;
    private final Duration startupTimeout;

    protected SolrContainer(String image, String coreName, String confDir, File workingDir, Duration startupTimeout) {
        this.coreName = coreName;
        this.confDir = confDir;

        Assert.assertTrue("workingDir does not exist", workingDir.exists());
        Assert.assertTrue("workingDir not a directory", workingDir.isDirectory());

        container = new GenericContainer<>(image);
        temporaryFolder = new TemporaryFolder(workingDir);
        this.startupTimeout = startupTimeout == null ? Duration.ofSeconds(15) : startupTimeout;
    }

    @Override
    protected void starting(Description description) {
        super.starting(description);

        try {
            before();
        } catch (Exception t) {
            if(LOG.isDebugEnabled()) {
                LOG.error("Failed to initialize SolrContainer(coreName: {} | confDir: {} | tmpFolder: {})",
                        coreName, confDir, temporaryFolder, t);
            } else {
                LOG.error("Failed to initialize SolrContainer(coreName: {} | confDir: {} | tmpFolder: {} | {} - {})",
                        coreName, confDir, temporaryFolder, t.getClass().getSimpleName(), t.getMessage());
            }
            Assert.fail("Failed to initialize (" + t.getClass().getSimpleName() + " - " + t.getMessage() + ")");
        }
    }

    protected void before() throws IOException {
        temporaryFolder.create();

        final File mountableConf = temporaryFolder.newFolder(coreName);

        final Path source = ResourceLoaderUtils.getResourceAsPath(confDir, getClass());
        PathUtils.copyRecursive(source, mountableConf.toPath());

        container.addExposedPort(SOLR_PORT);
        container.withFileSystemBind(mountableConf.getAbsolutePath(), "/core-conf", BindMode.READ_ONLY);
        container.withTmpFs(Map.of(
                "/tmp", "rw",
                "/var/solr", "rw,uid=8983,gid=8983",
                "/opt/solr/server/solr/mycores", "rw,uid=8983,gid=8983"
        ));
        container.withCommand("solr-precreate", coreName, "/core-conf");
        container.waitingFor(
                Wait.forHttp("/solr/" + coreName + "/admin/ping")
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

        temporaryFolder.delete();
    }

    public String getCoreName() {
        return coreName;
    }

    public String getSolrUrl() {
        return String.format("http://%s:%d/solr", container.getHost(), container.getMappedPort(SOLR_PORT));
    }

    public String getCoreUrl() {
        return String.format("%s/%s", getSolrUrl(), this.coreName);
    }

    @SuppressWarnings("java:S1452")
    public GenericContainer<?> getContainer() {
        return container;
    }

    public static SolrContainer create(String confDir) {
        return create("collection", confDir);
    }

    public static SolrContainer create(String confDir, Duration startupTimeout) {
        return create("collection", confDir, startupTimeout);
    }

    public static SolrContainer create(String collectionName, String confDir) {
        return create(DEFAULT_IMAGE + ":" + DEFAULT_TAG, collectionName, confDir);
    }
    public static SolrContainer create(String collectionName, String confDir, Duration startupTimeout) {
        return create(DEFAULT_IMAGE + ":" + DEFAULT_TAG, collectionName, confDir, startupTimeout);
    }

    public static SolrContainer create(String image, String collectionName, String confDir) {
        return create(image, collectionName, confDir, new File("."));
    }
    public static SolrContainer create(String image, String collectionName, String confDir, Duration startupTimeout) {
        return create(image, collectionName, confDir, new File("."), startupTimeout);
    }

    public static SolrContainer create(String image, String collectionName, String confDir, File workingDir) {
        return create(image, collectionName, confDir, workingDir, null);
    }
    
    public static SolrContainer create(String image, String collectionName, String confDir, File workingDir, Duration startupTimeout) {
        return new SolrContainer(image, collectionName, confDir, workingDir, startupTimeout);
    }

}
