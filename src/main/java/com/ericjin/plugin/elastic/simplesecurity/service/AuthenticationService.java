package com.ericjin.plugin.elastic.simplesecurity.service;

import com.ericjin.plugin.elastic.simplesecurity.util.SimpleSecurityConstants;
import org.elasticsearch.ElasticsearchSecurityException;
import org.elasticsearch.common.component.AbstractComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.transport.TransportRequest;

import java.nio.charset.StandardCharsets;
import java.util.Base64;


/**
 * A service class to enforce request authentication on Elastic node.
 *
 * Created by eric jin on 1/19/17.
 */
public class AuthenticationService  extends AbstractComponent {

    private static final String BASIC_AUTH_HEADER = "Authorization";

    @Inject
    public AuthenticationService(Settings settings) {
        super(settings);
    }

    /**
     * Authenticate Rest Client
     * @param request
     */
    public void authenticate(RestRequest request)  {

        //check auth enable flag
        if (!settings.getAsBoolean(SimpleSecurityConstants.SIMPLESECURITY_AUTH_ENABLE, false)) {
            return;
        }

        //no auth header
        String headerValue = request.header(BASIC_AUTH_HEADER);
        if (headerValue == null || headerValue.isEmpty()) {
            throw authenticationError("missing authentication token for REST request [{}]",
                    RestStatus.UNAUTHORIZED, new Object[] { request.uri() });
        }

        //auth header value wrong format
        if (!headerValue.startsWith("Basic ") || headerValue.length() == "Basic ".length()) {
            throw authenticationError("invalid basic authentication header value for REST request", null);
        }

        byte[] decodedValue;
        try {
            decodedValue = Base64.getDecoder().decode(headerValue.substring("Basic ".length()));
        } catch (IllegalArgumentException ex) {
            throw authenticationError("invalid basic authentication header encoding for REST request", ex);
        }

        String headerValStr = new String(decodedValue, StandardCharsets.UTF_8);
        String [] vals = headerValStr.split(":");
        if (vals == null || vals.length != 2 || vals[0] == null || vals[0].isEmpty()
                || vals[1] == null || vals[1].isEmpty()) {
            throw authenticationError("invalid basic authentication header value for REST request", null);
        }
        String user = vals[0];
        String password = vals[1];

        String confUser = settings.get(SimpleSecurityConstants.SIMPLESECURITY_AUTH_USER);
        String confPassword = settings.get(SimpleSecurityConstants.SIMPLESECURITY_AUTH_PASSWORD);
        if (confUser == null || confUser.isEmpty() || confPassword == null || confPassword.isEmpty()) {
            logger.warn("no username or password configuration in node for REST request", null);
            throw authenticationError("no username or password configuration in node for REST request", null);
        }

        if (!confUser.equals(user)) {
            throw authenticationError("unable to authenticate user [{}] for REST request [{}]", new Object[] { user, request.uri() });
        }

        if (!confPassword.equals(password)) {
            throw authenticationError("wrong password for user [{}] for REST request [{}]", new Object[] { user, request.uri() });
        }

    }

    /**
     * Authenticate Java Client
     * @param request
     */
    public void authenticate(TransportRequest request)  {
        String headerVal = request.getHeader(SimpleSecurityConstants.JAVA_CLIENT_HEADER);
        if (headerVal == null || headerVal.isEmpty()) {
            throw authenticationError("missing username and password header for Java client", null);
        }
        String [] vals = headerVal.split(":");
        if (vals == null || vals.length != 2 || vals[0] == null || vals[0].isEmpty()
                || vals[1] == null || vals[1].isEmpty()) {
            throw authenticationError("invalid authentication header value for Java client", null);
        }
        String reqUser = vals[0];
        String reqPassword = vals[1];

        String confUser = settings.get(SimpleSecurityConstants.SIMPLESECURITY_JAVA_USER);
        String confPassword = settings.get(SimpleSecurityConstants.SIMPLESECURITY_JAVA_PASSWORD);
        if (confUser == null || confUser.isEmpty() || confPassword == null || confPassword.isEmpty()) {
            logger.warn("no username or password configuration in node for Java client", null);
            throw authenticationError("no username or password configuration in node for Java client", null);
        }

        if (!confUser.equals(reqUser)) {
            throw authenticationError("unable to authenticate user [{}] for Java client [{}]", new Object[] {reqUser});
        }

        if (!confPassword.equals(reqPassword)) {
            throw authenticationError("wrong password for user [{}] for Java client [{}]", new Object[] {reqUser});
        }

    }

    private ElasticsearchSecurityException authenticationError(String msg, Object... args) {
        ElasticsearchSecurityException e = new ElasticsearchSecurityException(msg, RestStatus.UNAUTHORIZED, args);
        e.addHeader("WWW-Authenticate", new String[] { "Basic realm=\"simpleSecurity\" charset=\"UTF-8\"" });
        return e;
    }
}
