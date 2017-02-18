package com.ericjin.plugin.elastic.simplesecurity.service;

import com.ericjin.plugin.elastic.simplesecurity.util.SimpleSecurityConstants;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.transport.*;
import org.elasticsearch.threadpool.ThreadPool;

/**
 * Add security plugin support for TransportClient
 * Created by eric jin on 2/16/17.
 */
public class SimpleSecurityClientTransportService extends TransportService {

    private final ESLogger logger = Loggers.getLogger(this.getClass());

    @Inject
    public SimpleSecurityClientTransportService(Settings settings, Transport transport, ThreadPool threadPool) {
        super(settings, transport, threadPool);
    }

    /**
     * Add java client auth header to request
     * @param node
     * @param action
     * @param request
     * @param options
     * @param handler
     * @param <T>
     */
    @Override
    public <T extends TransportResponse> void sendRequest(DiscoveryNode node, String action, TransportRequest request, TransportRequestOptions options, TransportResponseHandler<T> handler) {

        String user = settings.get(SimpleSecurityConstants.JAVA_CLIENT_HEADER);
        if (user!= null && !user.isEmpty()) {
            request.putHeader(SimpleSecurityConstants.JAVA_CLIENT_HEADER, user);
        }

        super.sendRequest(node, action, request, options, handler);
    }

}
