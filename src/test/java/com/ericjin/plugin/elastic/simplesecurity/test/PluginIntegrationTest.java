package com.ericjin.plugin.elastic.simplesecurity.test;

import com.ericjin.plugin.elastic.simplesecurity.util.SimpleSecurityConstants;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ericjin.plugin.elastic.simplesecurity.SimpleSecurityPlugin;
import org.elasticsearch.action.admin.cluster.node.info.NodeInfo;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.plugins.PluginInfo;
import org.elasticsearch.test.ESIntegTestCase;
import org.elasticsearch.test.rest.client.http.HttpRequestBuilder;
import org.elasticsearch.test.rest.client.http.HttpResponse;
import org.junit.Test;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.elasticsearch.rest.RestStatus.OK;
import static org.elasticsearch.rest.RestStatus.UNAUTHORIZED;
import static org.elasticsearch.test.ESIntegTestCase.Scope.TEST;

/**
 * Created by eric jin on 1/26/17.
 */
@ESIntegTestCase.ClusterScope(scope=TEST, numDataNodes=3)
public class PluginIntegrationTest extends ESIntegTestCase {

    protected static final String AUTH_HEADER = "Basic YWRtaW46YWRtaW4=";
    @Override
    protected Settings nodeSettings(int nodeOrdinal) {

        return Settings.builder().put(super.nodeSettings(nodeOrdinal))
                .put("http.enabled", true)
                .put(SimpleSecurityConstants.SIMPLESECURITY_AUTH_ENABLE, true)
                .put(SimpleSecurityConstants.SIMPLESECURITY_AUTH_USER, "admin")
                .put(SimpleSecurityConstants.SIMPLESECURITY_AUTH_PASSWORD, "admin")
                .build();
    }

    @Override
    protected Collection<Class<? extends Plugin>> nodePlugins() {
        return pluginList(SimpleSecurityPlugin.class);
    }

    @Test
    public void pluginIsLoaded() throws Exception {
        System.out.println("************ Start pluginIsLoaded test ***********");

        NodesInfoResponse response = client().admin().cluster().prepareNodesInfo().setPlugins(true).get();
        System.out.println("Nodes number : " + response.getNodes().length);
        for (NodeInfo nodeInfo : response.getNodes()) {
            boolean pluginFound = false;
            for (PluginInfo pluginInfo : nodeInfo.getPlugins().getPluginInfos()) {
                if (pluginInfo.getName().equals("SimpleSecurityPlugin")) {
                    pluginFound = true;
                    break;
                }
            }
            assertTrue(pluginFound == true);
        }
    }

    @Test
    public void restClientFail() throws Exception {
        System.out.println("************ Start restClientFail test ***********");

        HttpRequestBuilder builder = httpClient();
        assertNotNull(builder);
        HttpResponse response = builder.method("GET").path("/_cat/indices?v").execute();
        System.out.println(response.toString());
        assertEquals(response.getStatusCode(), UNAUTHORIZED.getStatus());
    }

    @Test
    public void restClientPass() throws Exception {
        System.out.println("************ Start restClientPass test ***********");

        HttpRequestBuilder builder = httpClient();
        assertNotNull(builder);
        HttpResponse response = builder.method("GET").path("/_cat/indices?v")
                .addHeader("Authorization", AUTH_HEADER).execute();
        System.out.println(response.toString());
        assertEquals(response.getStatusCode(), OK.getStatus());
    }

    @Test
    public void crud() throws Exception {

        System.out.println("************ Start CRUD test ***********");
        Map<String, Object> map = new HashMap<>();
        map.put("name", "Eric Jin");
        map.put("phone", "1234567878");
        index("bank", "account", "123", map);
        HttpRequestBuilder builder = httpClient();
        assertNotNull(builder);
        HttpResponse response = builder.method("GET").path("/bank/account/123").execute();
        System.out.println(response.toString());
        assertEquals(response.getStatusCode(), UNAUTHORIZED.getStatus());

        final HttpResponse response2 = builder.method("GET").path("/bank/account/123")
                .addHeader("Authorization", AUTH_HEADER).execute();
        System.out.println(response2.getBody());
        assertEquals(response2.getStatusCode(), OK.getStatus());

        AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    Map<String, Object> jsonMap = mapper.readValue(response2.getBody(), new TypeReference<Map<String, Object>>() {
                    });
                    assertEquals(((Map<String, Object>) jsonMap.get("_source")).get("name"), "Eric Jin");
                }catch (Exception ex) {

                }
                return null;
            }
        });
    }
}
