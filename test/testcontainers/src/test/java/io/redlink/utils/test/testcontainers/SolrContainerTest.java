/*
 * Copyright 2019 redlink GmbH
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

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.CoreAdminRequest;
import org.apache.solr.client.solrj.response.CoreAdminResponse;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.SolrPingResponse;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.Description;

import java.io.IOException;
import java.util.Date;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.notNullValue;

public class SolrContainerTest {

    @Test
    public void testSolrContainerDefaultCore() throws IOException {
        Description description = Description.createTestDescription(SolrContainerTest.class, "default-core");

        final SolrContainer solrContainer = SolrContainer.create("/solr/core-conf");
        try {
            solrContainer.starting(description);
            final String coreName = solrContainer.getCoreName();

            validate(solrContainer, coreName);
        } finally {
            solrContainer.finished(description);
        }
    }

    @Test
    public void testSolrContainerCustomCore() throws IOException {
        Description description =
        Description.createTestDescription(SolrContainerTest.class, "custom-core");

        final String coreName = "fancy";
        final SolrContainer solrContainer = SolrContainer.create(coreName, "/solr/core-conf");
        try {
            solrContainer.starting(description);

            Assert.assertThat("core-name", coreName, is(solrContainer.getCoreName()));

            validate(solrContainer, coreName);
        } finally {
            solrContainer.finished(description);
        }
    }

    private void validate(SolrContainer solrContainer, String coreName) throws IOException {
        try (HttpSolrClient solrClient = new HttpSolrClient.Builder(solrContainer.getSolrUrl()).build()) {

            final CoreAdminResponse adminResponse = CoreAdminRequest.getStatus(coreName, solrClient);

            Assert.assertThat("request", adminResponse, notNullValue());
            Assert.assertThat("server-status", adminResponse.getStatus(), is(0));
            Assert.assertThat("start-time", adminResponse.getStartTime(coreName), lessThan(new Date()));
        } catch (SolrServerException e) {
            Assert.fail(e.getMessage());
        }

        try (HttpSolrClient solrClient = new HttpSolrClient.Builder(solrContainer.getCoreUrl()).build()) {
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
