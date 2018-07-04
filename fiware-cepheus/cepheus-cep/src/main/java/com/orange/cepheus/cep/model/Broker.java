/*
 * Copyright (C) 2015 Orange
 *
 * This software is distributed under the terms and conditions of the 'GNU GENERAL PUBLIC LICENSE
 * Version 2' license which can be found in the file 'LICENSE.txt' in this package distribution or
 * at 'http://www.gnu.org/licenses/gpl-2.0-standalone.html'.
 */

package com.orange.cepheus.cep.model;

/**
 * A broker is a NGSI Context Manager that can handle updates of ContentElements.
 *
 * If the mustRegister property is FALSE, the broker will be notified with /updateContext requests on each event.
 * If the mustRegister property is TRUE, the broker will receive an initial /registerContext with the CEP as providing application
 * (the broker will then be able to call /queryContext)
 */
public class Broker {

    /**
     * Url to the broker
     */
    private String url;

    /**
     * Fiware specific service name (optional)
     */
    private String serviceName;

    /**
     * Fiware specific service path (optional)
     */
    private String servicePath;

    /**
     * OAuth token for secured brokers
     */    
    private String authToken;
    
    /**
     * Rabbit MQ URL
     */
    private String rabbitMQHost;
    
    /**
     * Rabbit MQ Username
     */
    private String rabbitMQUsername;
    
    /**
     * Rabbit MQ Password
     */
    private String rabbitMQPassword;
    
    /**
     * Rabbit MQ Queue Name
     */
    private String rabbitMQQueueName;
    
    /**
     * Rabbit MQ Exchange
     */
    private String rabbitMQExchange;
    

    public Broker() {
    }

    public Broker(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServicePath() {
        return servicePath;
    }

    public void setServicePath(String servicePath) {
        this.servicePath = servicePath;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

	public String getRabbitMQHost() {
		return rabbitMQHost;
	}

	public void setRabbitMQHost(String rabbitMQHost) {
		this.rabbitMQHost = rabbitMQHost;
	}
	
	public String getRabbitMQUsername() {
		return rabbitMQUsername;
	}

	public void setRabbitMQUsername(String rabbitMQUsername) {
		this.rabbitMQUsername = rabbitMQUsername;
	}

	public String getRabbitMQPassword() {
		return rabbitMQPassword;
	}

	public void setRabbitMQPassword(String rabbitMQPassword) {
		this.rabbitMQPassword = rabbitMQPassword;
	}

	public String getRabbitMQQueueName() {
		return rabbitMQQueueName;
	}

	public void setRabbitMQQueueName(String rabbitMQQueueName) {
		this.rabbitMQQueueName = rabbitMQQueueName;
	}

	public String getRabbitMQExchange() {
		return rabbitMQExchange;
	}

	public void setRabbitMQExchange(String rabbitMQExchange) {
		this.rabbitMQExchange = rabbitMQExchange;
	}
    
}
