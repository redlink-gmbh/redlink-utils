/*
 * Copyright 2020 Redlink GmbH
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

public class ZookeeperContainer extends GenericContainer<ZookeeperContainer> {

    public static final String DEFAULT_IMAGE = "zookeeper";
    public static final String DEFAULT_TAG = "3.5";

    public static final int CONNECT_PORT = 2181;
    public static final int ADMIN_PORT = 8080;

    private static final String[] FOUR_LETTER_COMMANDS = {
            "srvr", "stat", "wchc", "dump", "crst", "srst", "envi",
            "conf", "telnet close", "wchs", "wchp", "dirs", "cons",
            "mntr", "isro", "ruok", "gtmk", "stmk"
    };

    public ZookeeperContainer() {
        this(DEFAULT_IMAGE + ":" + DEFAULT_TAG);
    }

    public ZookeeperContainer(String dockerImageName) {
        super(dockerImageName);

        waitStrategy = Wait.forLogMessage(".*binding to port .*:" + CONNECT_PORT + "\n", 1);
        addExposedPorts(CONNECT_PORT, ADMIN_PORT);
        addEnv("ZOO_4LW_COMMANDS_WHITELIST", String.join(",", FOUR_LETTER_COMMANDS));
    }

    public String getZkConnect() {
        return String.format("%s:%d", getContainerIpAddress(), getMappedPort(CONNECT_PORT));
    }

    public String getAdminUrl() {
        return String.format("http://%s:%d/commands", getContainerIpAddress(), getMappedPort(ADMIN_PORT));
    }


}
