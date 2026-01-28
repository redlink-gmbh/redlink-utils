/*
 * Copyright (c) 2021-2022 Redlink GmbH.
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
import java.util.Date;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.Http2SolrClient;
import org.apache.solr.client.solrj.request.CoreAdminRequest;
import org.apache.solr.client.solrj.response.CoreAdminResponse;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.SolrPingResponse;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.Description;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.notNullValue;

public class SolrContainerTest {

    @Rule
    public final Timeout globalTimeout = Timeout.seconds(60);

    @Test
    public void testSolrContainerDefaultCore() throws IOException {
        Description description = Description.createTestDescription(SolrContainerTest.class, "default-core");

        final SolrContainer solrContainer = SolrContainer.create("/solr7");
        try {
            solrContainer.starting(description);
            final String coreName = solrContainer.getCoreName();

            validate(solrContainer, coreName, true);
        } finally {
            solrContainer.finished(description);
        }
    }

    @Test
    public void testSolrContainerCustomCore() throws IOException {
        Description description =
        Description.createTestDescription(SolrContainerTest.class, "custom-core");

        final String coreName = "fancy";
        final SolrContainer solrContainer = SolrContainer.create(coreName, "/solr7");
        try {
            solrContainer.starting(description);

            MatcherAssert.assertThat("core-name", solrContainer.getCoreName(), is(coreName));

            validate(solrContainer, coreName, true);
        } finally {
            solrContainer.finished(description);
        }
    }

    @Test
    public void testSolr9ContainerCustomCore() throws IOException {
        Description description =
        Description.createTestDescription(SolrContainerTest.class, "custom-core");

        final String coreName = "solr9";
        final SolrContainer solrContainer = SolrContainer.create("solr:9", coreName, "/solr9");
        try {
            solrContainer.starting(description);

            MatcherAssert.assertThat("core-name", solrContainer.getCoreName(), is(coreName));

            validate(solrContainer, coreName, false);
        } finally {
            solrContainer.finished(description);
        }
    }

    private void validate(SolrContainer solrContainer, String coreName, boolean useHttp1) throws IOException {
        try (Http2SolrClient solrClient = new Http2SolrClient.Builder(solrContainer.getSolrUrl()).useHttp1_1(useHttp1).build()) {

            final CoreAdminResponse adminResponse = CoreAdminRequest.getStatus(coreName, solrClient);

            MatcherAssert.assertThat("request", adminResponse, notNullValue());
            MatcherAssert.assertThat("server-status", adminResponse.getStatus(), is(0));
            MatcherAssert.assertThat("start-time", adminResponse.getStartTime(coreName), lessThan(new Date()));
        } catch (SolrServerException e) {
            Assert.fail(e.getMessage());
        }

        try (Http2SolrClient solrClient = new Http2SolrClient.Builder(solrContainer.getCoreUrl()).useHttp1_1(useHttp1).build()) {
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
