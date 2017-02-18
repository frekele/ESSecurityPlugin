package com.ericjin.plugin.elastic.simplesecurity.module;

import com.ericjin.plugin.elastic.simplesecurity.rest.SimpleSecurityRestFilter;
import com.ericjin.plugin.elastic.simplesecurity.service.AuthenticationService;
import org.elasticsearch.common.inject.AbstractModule;
import org.elasticsearch.common.settings.Settings;

/**
 * Node module to register RestFilter and AuthenicationService
 */
public class SimpleSecurityModule extends AbstractModule {

    protected final Settings settings;

    public SimpleSecurityModule(Settings settings) {
        this.settings = settings;
    }

    @Override
    protected void configure() {
        bind(SimpleSecurityRestFilter.class).asEagerSingleton();
        bind(AuthenticationService.class).asEagerSingleton();
    }
}
