package com.ericjin.plugin.elastic.simplesecurity.service;

import java.util.concurrent.Callable;

import com.ericjin.plugin.elastic.simplesecurity.util.SimpleSecurityConstants;
import org.elasticsearch.ElasticsearchSecurityException;
import org.elasticsearch.common.logging.ESLogger;;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.Transport;
import org.elasticsearch.transport.TransportRequest;
import org.elasticsearch.transport.TransportRequestHandler;
import org.elasticsearch.transport.TransportService;
import org.elasticsearch.tasks.Task;
import org.elasticsearch.transport.TransportChannel;
import org.elasticsearch.common.inject.Inject;

/**
 * Overwrite TransportService on node to add authentication for Java Client
 * Not use for now.
 * Created by eric jin on 1/13/17.
 */
public class SimpleSecurityServerTransportService extends TransportService {

    private final AuthenticationService authService;

    @Inject
    public SimpleSecurityServerTransportService(Settings settings, Transport transport, ThreadPool threadPool, AuthenticationService authService) {
        super(settings, transport, threadPool);
        this.authService = authService;

    }

    @Override
    public <Request extends TransportRequest> void registerRequestHandler(String action, Callable<Request> requestFactory, String executor, TransportRequestHandler<Request> handler)
    {
        TransportRequestHandler<Request> custHandler = new CustTransportRequestHandler(action, handler);
        super.registerRequestHandler(action, requestFactory, executor, custHandler);
    }

    @Override
    public <Request extends TransportRequest> void registerRequestHandler(String action, Callable<Request> requestFactory, String executor, boolean forceExecution, boolean canTripCircuitBreaker, TransportRequestHandler<Request> handler)
    {
        TransportRequestHandler<Request> custHandler = new CustTransportRequestHandler(action, handler);
        super.registerRequestHandler(action, requestFactory, executor, forceExecution, canTripCircuitBreaker, custHandler);
    }

    @Override
    public <Request extends TransportRequest> void registerRequestHandler(String action, Class<Request> request, String executor, boolean forceExecution, boolean canTripCircuitBreaker, TransportRequestHandler<Request> handler)
    {
        TransportRequestHandler<Request> custHandler = new CustTransportRequestHandler(action, handler);
        super.registerRequestHandler(action, request, executor, forceExecution, canTripCircuitBreaker, custHandler);
    }

    /**
     * Customized request handler to add security support for Javaclient
     * @param <T>
     */
    private class CustTransportRequestHandler<T extends TransportRequest> extends TransportRequestHandler<T> {
        private final ESLogger logger = Loggers.getLogger(this.getClass());
        private final TransportRequestHandler<T> handler;
        private final String action;

        public CustTransportRequestHandler(String action, TransportRequestHandler<T> handler) {
            super();
            this.action = action;
            this.handler = handler;
        }

        @Override
        public void messageReceived(T request, TransportChannel channel) throws Exception {
            messageReceived(request, channel, null);
        }

        @Override
        public void messageReceived(T request, TransportChannel transportChannel, Task task) throws Exception {

            if ("java".equals(transportChannel.getProfileName())) {

                logger.info("============ Message Received! ==============");
                logger.info("Action: " + this.action);
                logger.info("Task: " + task + " - type: " + task.getType());
                logger.info("Request Headers: " + request.getHeaders());
                logger.info("Request Java Header: " + request.getHeader(SimpleSecurityConstants.JAVA_CLIENT_HEADER));


                logger.info("TransportChannel: " + transportChannel);
                logger.info("TransportChannel Type: " + transportChannel.getChannelType());
                logger.info("TransportChannel ProfileName: " + transportChannel.getProfileName());
                logger.info("============ Message End! ==============");

                boolean javaAuthEnable = settings.getAsBoolean(SimpleSecurityConstants.SIMPLESECURITY_JAVA_ENABLE, false);

                if (javaAuthEnable) {
                    authService.authenticate(request);
                }
            }
            this.handler.messageReceived(request, transportChannel, task);
        }

    }
}
