/*
 * Copyright (c) 2022 Redlink GmbH.
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

import org.junit.Assert;
import org.junit.Assume;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.testcontainers.DockerClientFactory;

public final class DockerAvailableRule implements TestRule {

    private static final String DEFAULT_MESSAGE = "Could not find a valid Docker environment.";

    private final boolean fail;
    private final String message;

    public DockerAvailableRule(boolean fail, String message) {
        this.fail = fail;
        this.message = message;
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                checkDockerAvailable(fail, message);

                base.evaluate();
            }
        };
    }

    private static void checkDockerAvailable(boolean fail, String message) {
        if (fail) {
            Assert.assertTrue(message, DockerClientFactory.instance().isDockerAvailable());
        } else {
            Assume.assumeTrue(message, DockerClientFactory.instance().isDockerAvailable());
        }
    }

    public static void assumeDockerAvailable() {
        assumeDockerAvailable(DEFAULT_MESSAGE);
    }

    public static void assumeDockerAvailable(String message) {
        checkDockerAvailable(false, message);
    }

    public static void assertDockerAvailable() {
        assertDockerAvailable(DEFAULT_MESSAGE);
    }

    public static void assertDockerAvailable(String message) {
        checkDockerAvailable(true, message);
    }

    public static DockerAvailableRule skipOnDockerMissing() {
        return skipOnDockerMissing(DEFAULT_MESSAGE);
    }

    public static DockerAvailableRule skipOnDockerMissing(String message) {
        return new DockerAvailableRule(false, message);
    }

    public static DockerAvailableRule failOnDockerMissing() {
        return failOnDockerMissing(DEFAULT_MESSAGE);
    }

    public static DockerAvailableRule failOnDockerMissing(String message) {
        return new DockerAvailableRule(true, message);
    }

}
