package com.ericjin.plugin.elastic.simplesecurity.rest;

import com.ericjin.plugin.elastic.simplesecurity.service.AuthenticationService;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.rest.*;

/**
 * Overwrite RestFilter to enable Http basic authentication.
 *
 * Created by eric jin on 1/18/17.
 */
public class SimpleSecurityRestFilter extends RestFilter {

    private static final ESLogger logger = Loggers.getLogger(SimpleSecurityRestFilter.class);
    private final AuthenticationService authService;

    @Inject
    public SimpleSecurityRestFilter(AuthenticationService service, RestController controller) {
        this.authService = service;
        controller.registerFilter(this);
    }

    @Override
    public int order() {
        return Integer.MIN_VALUE;
    }

    @Override
    public void process(RestRequest request, RestChannel channel,
                        RestFilterChain filterChain) throws Exception {

        this.authService.authenticate(request);

        filterChain.continueProcessing(request, channel);
    }
}
