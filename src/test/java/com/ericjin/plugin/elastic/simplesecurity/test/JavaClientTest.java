package com.ericjin.plugin.elastic.simplesecurity.test;

import com.ericjin.plugin.elastic.simplesecurity.SimpleSecurityPlugin;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import java.net.InetAddress;
import java.util.Map;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

/**
 * Created by eric jin on 2/15/17.
 */
public class JavaClientTest  {

    public static void main(String[] args) throws Exception {

        TransportClient client = TransportClient.builder()
                .addPlugin(SimpleSecurityPlugin.class)
                .settings(Settings.builder()
                        .put("javaclient", "java:java")
                        .build())
                .build()
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9898));

        IndexResponse response = client.prepareIndex("test", "account", "123")
                .setSource(jsonBuilder().startObject()
                        .field("name", "Eric Jin")
                        .field("phone", "1234567979")
                        .endObject()
                )
                .get();

        GetResponse resp = client.prepareGet("test", "account", "123").get();
        Map<String, Object> map = resp.getSource();
        for (String key : map.keySet()) {
            Object val = map.get(key);
            System.out.println(key + " = " + val);
        }

        client.close();

    }

}
