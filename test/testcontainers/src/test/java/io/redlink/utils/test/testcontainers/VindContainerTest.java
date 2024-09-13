/*
 * Copyright (c) 2019-2022 Redlink GmbH.
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
import java.util.Date;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpJdkSolrClient;
import org.apache.solr.client.solrj.request.CoreAdminRequest;
import org.apache.solr.client.solrj.response.CoreAdminResponse;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.SolrPingResponse;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.Description;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.notNullValue;

public class VindContainerTest {

    @Test
    public void testDefaultVindConfiguration() throws IOException {
        Description description =
                Description.createTestDescription(VindContainerTest.class, "vind");

        final VindContainer vindContainer = VindContainer.create();
        try {
            vindContainer.starting(description);

            MatcherAssert.assertThat("core-name", vindContainer.getCoreNames(), Matchers.contains(VindContainer.VIND_CORE_NAME));

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

            MatcherAssert.assertThat("core-count", vindContainer.getCoreNames(), Matchers.iterableWithSize(coreNames.length + 1));
            MatcherAssert.assertThat("core-names", vindContainer.getCoreNames(), Matchers.hasItem(VindContainer.VIND_CORE_NAME));
            MatcherAssert.assertThat("core-names", vindContainer.getCoreNames(), Matchers.hasItems(coreNames));

            for (String coreName : vindContainer.getCoreNames()) {
                validate(vindContainer, coreName);
            }

        } finally {
            vindContainer.finished(description);
        }
    }


    private void validate(VindContainer vindContainer, String coreName) throws IOException {
        try (SolrClient solrClient = new HttpJdkSolrClient.Builder(vindContainer.getSolrUrl()).build()) {

            final CoreAdminResponse adminResponse = CoreAdminRequest.getStatus(coreName, solrClient);

            MatcherAssert.assertThat("request", adminResponse, notNullValue());
            MatcherAssert.assertThat("server-status", adminResponse.getStatus(), is(0));
            MatcherAssert.assertThat("start-time", adminResponse.getStartTime(coreName), lessThan(new Date()));
        } catch (SolrServerException e) {
            Assert.fail(e.getMessage());
        }

        try (SolrClient solrClient = new HttpJdkSolrClient.Builder(vindContainer.getCoreUrl(coreName)).build()) {
            final SolrPingResponse ping = solrClient.ping();
            MatcherAssert.assertThat("ping", ping.getStatus(), is(0));

            final QueryResponse response = solrClient.query(
                    new SolrQuery("*:*")
                            .setStart(0)
                            .setRows(0)
            );

            MatcherAssert.assertThat("header", response.getHeader(), notNullValue());
            MatcherAssert.assertThat("results", response.getResults(), notNullValue());
            MatcherAssert.assertThat("result count", response.getResults().getNumFound(), equalTo(0L));
        } catch (SolrServerException e) {
            Assert.fail(e.getMessage());
        }
    }


}