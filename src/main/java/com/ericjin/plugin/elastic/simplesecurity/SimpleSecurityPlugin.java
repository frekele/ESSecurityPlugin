package com.ericjin.plugin.elastic.simplesecurity;

import com.ericjin.plugin.elastic.simplesecurity.module.SimpleSecurityModule;
import com.ericjin.plugin.elastic.simplesecurity.service.SimpleSecurityClientTransportService;
import com.ericjin.plugin.elastic.simplesecurity.service.SimpleSecurityServerTransportService;
import com.google.common.collect.Lists;
import org.elasticsearch.common.inject.Module;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.transport.TransportModule;

import java.util.Collection;

/**
 * SimpleSecurity plugin added basic authentication support for
 * REST and Java client for Elasticsearch
 */
public class SimpleSecurityPlugin extends Plugin {

    private static final ESLogger logger = Loggers.getLogger(SimpleSecurityPlugin.class);
    private final Settings settings;

    public SimpleSecurityPlugin(Settings settings) {
        this.settings = settings;
    }

    @Override
    public String name() {
        return "SimpleSecurityPlugin";
    }

    @Override
    public String description() {
        return "This is a security plugin for Elasticsearch.";
    }

    @Override
    public Collection<Module> nodeModules() {
        final Collection<Module> modules = Lists.newArrayList();
        modules.add(new SimpleSecurityModule(settings));
        return modules;
    }

    /**
     * Register extended tranport service for server and client side
     * @param module
     */
    public void onModule(TransportModule module) {
        if (!"node".equals(settings.get("client.type"))) {
            module.setTransportService(SimpleSecurityClientTransportService.class, "simpleSecurity");
        } else {
            module.setTransportService(SimpleSecurityServerTransportService.class, "simpleSecurity");
        }
    }
}
