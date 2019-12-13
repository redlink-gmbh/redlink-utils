/*
 * Copyright 2019 Redlink GmbH
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

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.CoreAdminRequest;
import org.apache.solr.client.solrj.response.CoreAdminResponse;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.SolrPingResponse;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.Description;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.*;

public class VindContainerTest {

    @Test
    public void testDefaultVindConfiguration() throws IOException {
        Description description =
                Description.createTestDescription(VindContainerTest.class, "vind");

        final VindContainer vindContainer = VindContainer.create();
        try {
            vindContainer.starting(description);

            Assert.assertThat("core-name", vindContainer.getCoreNames(), Matchers.contains(VindContainer.VIND_CORE_NAME));

            validate(vindContainer, VindContainer.VIND_CORE_NAME);
        } finally {
            vindContainer.finished(description);
        }
    }

    @Test
    public void testMultipleVindCollections() throws IOException {
        Description description =
                Description.createTestDescription(VindContainerTest.class, "multipleVindCollections");

        final String[] coreNames = {"vind1", "vind2", "vind3"};

        final VindContainer vindContainer = VindContainer.create(Arrays.asList(coreNames));
        try {
            vindContainer.starting(description);

            Assert.assertThat("core-count", vindContainer.getCoreNames(), Matchers.iterableWithSize(coreNames.length + 1));
            Assert.assertThat("core-names", vindContainer.getCoreNames(), Matchers.hasItem(VindContainer.VIND_CORE_NAME));
            Assert.assertThat("core-names", vindContainer.getCoreNames(), Matchers.hasItems(coreNames));

            for (String coreName : vindContainer.getCoreNames()) {
                validate(vindContainer, coreName);
            }

        } finally {
            vindContainer.finished(description);
        }
    }


    private void validate(VindContainer vindContainer, String coreName) throws IOException {
        try (HttpSolrClient solrClient = new HttpSolrClient.Builder(vindContainer.getSolrUrl()).build()) {

            final CoreAdminResponse adminResponse = CoreAdminRequest.getStatus(coreName, solrClient);

            Assert.assertThat("request", adminResponse, notNullValue());
            Assert.assertThat("server-status", adminResponse.getStatus(), is(0));
            Assert.assertThat("start-time", adminResponse.getStartTime(coreName), lessThan(new Date()));
        } catch (SolrServerException e) {
            Assert.fail(e.getMessage());
        }

        try (HttpSolrClient solrClient = new HttpSolrClient.Builder(vindContainer.getCoreUrl(coreName)).build()) {
            final SolrPingResponse ping = solrClient.ping();
            Assert.assertThat("ping", ping.getStatus(), is(0));

            final QueryResponse response = solrClient.query(
                    new SolrQuery("*:*")
                            .setStart(0)
                            .setRows(0)
            );

            Assert.assertThat("header", response.getHeader(), notNullValue());
            Assert.assertThat("results", response.getResults(), notNullValue());
            Assert.assertThat("result count", response.getResults().getNumFound(), equalTo(0L));
        } catch (SolrServerException e) {
            Assert.fail(e.getMessage());
        }
    }


}