/*
 * Copyright (C) 2015 Orange
 *
 * This software is distributed under the terms and conditions of the 'GNU GENERAL PUBLIC LICENSE
 * Version 2' license which can be found in the file 'LICENSE.txt' in this package distribution or
 * at 'http://www.gnu.org/licenses/gpl-2.0-standalone.html'.
 */

package com.orange.cepheus.cep;

import com.orange.cepheus.cep.exception.ConfigurationException;
import com.orange.cepheus.cep.exception.PersistenceException;
import com.orange.cepheus.cep.model.Configuration;
import com.orange.cepheus.cep.persistence.Persistence;
import com.orange.cepheus.cep.tenant.TenantFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * This Bean is created to load a persisted Configuration
 * into the Complex Event Processor on startup.
 * Except when multi-tenant is enabled (in this case, see the TenantInit bean).
 */
@Component
@Profile("!multi-tenant")
public class Init {

    private static Logger logger = LoggerFactory.getLogger(Init.class);

    @Autowired
    protected ApplicationContext applicationContext;

    @Autowired
    protected Persistence persistence;

    @Autowired
    protected ComplexEventProcessor complexEventProcessor;

    @Autowired
    protected SubscriptionManager subscriptionManager;

    @Autowired
    protected EventMapper eventMapper;

    @PostConstruct
    protected void loadConfigurationOnStartup() {
        // Try restoring the persisted configuration if any
        try {
            if (persistence.configurationExists(TenantFilter.DEFAULT_TENANTID)) {
                Configuration configuration = persistence.loadConfiguration(TenantFilter.DEFAULT_TENANTID);
                eventMapper.setConfiguration(configuration);
                complexEventProcessor.setConfiguration(configuration);
                subscriptionManager.setConfiguration(configuration);
            }
        } catch (PersistenceException | ConfigurationException e) {
            logger.error("Failed to load or apply persisted configuration", e);
        }
    }
}
