/*
 * Copyright (c) 2018 Redlink GmbH.
 */
package io.redlink.utils.test.testcontainers;

import io.redlink.utils.PathUtils;
import io.redlink.utils.ResourceLoaderUtils;
import org.junit.Assert;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.Description;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.FailureDetectingExternalResource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;

public class SolrContainer extends FailureDetectingExternalResource {

    private static final String DEFAULT_IMAGE = "solr";
    private static final String DEFAULT_TAG = "7.6.0";

    private static final Integer SOLR_PORT = 8983;

    private final TemporaryFolder temporaryFolder;
    private final GenericContainer container;
    private final String coreName;
    private final String confDir;

    private SolrContainer(String image, String coreName, String confDir, File workingDir) {
        this.coreName = coreName;
        this.confDir = confDir;

        Assert.assertTrue("workingDir does not exist", workingDir.exists());
        Assert.assertTrue("workingDir not a directory", workingDir.isDirectory());

        container = new GenericContainer(image);
        temporaryFolder = new TemporaryFolder(workingDir);
    }

    @Override
    protected void starting(Description description) {
        super.starting(description);

        try {
            before();
        } catch (Exception t) {
            Assert.fail(t.getMessage());
        }
    }

    protected void before() throws IOException {
        temporaryFolder.create();

        final File mountableConf = temporaryFolder.newFolder(coreName);

        final Path source = ResourceLoaderUtils.getResourceAsPath(confDir, getClass());
        PathUtils.copyRecursive(source, mountableConf.toPath());

        container.addExposedPort(SOLR_PORT);
        container.withFileSystemBind(mountableConf.getAbsolutePath(), "/core-conf", BindMode.READ_ONLY);
        container.withCommand("solr-precreate", coreName, "/core-conf");
        container.waitingFor(
                Wait.forLogMessage(".*SolrCore \\Q[" + coreName + "]\\E Registered new searcher.*\n", 1)
                        .withStartupTimeout(Duration.ofSeconds(15))
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
        return String.format("http://%s:%d/solr", container.getContainerIpAddress(), container.getMappedPort(SOLR_PORT));
    }

    public String getCoreUrl() {
        return String.format("%s/%s", getSolrUrl(), this.coreName);
    }

    public static SolrContainer create(String confDir) {
        return create("collection", confDir);
    }

    public static SolrContainer create(String collectionName, String confDir) {
        return create(DEFAULT_IMAGE + ":" + DEFAULT_TAG, collectionName, confDir);
    }

    public static SolrContainer create(String image, String collectionName, String confDir) {
        return create(image, collectionName, confDir, new File("."));
    }

    public static SolrContainer create(String image, String collectionName, String confDir, File workingDir) {
        return new SolrContainer(image, collectionName, confDir, workingDir);
    }

}
