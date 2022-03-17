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

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Info;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.DockerClientFactory;

public class DockerAvailableRuleTest {

    @Rule
    public DockerAvailableRule dockerAvailable = DockerAvailableRule.skipOnDockerMissing();

    private final DockerClient dockerClient = DockerClientFactory.lazyClient();

    @Test(timeout = 5000)
    public void testDockerInfo() {
        final Info info = dockerClient.infoCmd().exec();
        Assert.assertNotNull("Docker-Info Name", info.getName());
        Assert.assertNotNull("Docker-Info OS-Type", info.getOsType());
        Assert.assertNotNull("Docker-Info Architecture", info.getArchitecture());
    }
}