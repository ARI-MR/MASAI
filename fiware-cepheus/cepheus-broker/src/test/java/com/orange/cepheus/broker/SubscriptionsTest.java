/*
 * Copyright (C) 2015 Orange
 *
 * This software is distributed under the terms and conditions of the 'GNU GENERAL PUBLIC LICENSE
 * Version 2' license which can be found in the file 'LICENSE.txt' in this package distribution or
 * at 'http://www.gnu.org/licenses/gpl-2.0-standalone.html'.
 */
package com.orange.cepheus.broker;

import com.orange.cepheus.broker.exception.SubscriptionException;
import com.orange.cepheus.broker.exception.SubscriptionPersistenceException;
import com.orange.cepheus.broker.model.Subscription;
import com.orange.cepheus.broker.persistence.SubscriptionsRepository;
import com.orange.ngsi.model.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.orange.cepheus.broker.Util.createSubscribeContext;
import static com.orange.cepheus.broker.Util.createSubscribeContextTemperature;
import static org.junit.Assert.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.any;

/**
 * Tests for Subscriptions management
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@TestPropertySource(locations="classpath:test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class SubscriptionsTest {

    @Rule
    public ExpectedException thrown= ExpectedException.none();

    @Autowired
    public Subscriptions subscriptions;

    @Autowired
    SubscriptionsRepository subscriptionsRepository;

    @Test
    public void addSubscriptionTest() throws URISyntaxException, SubscriptionException, SubscriptionPersistenceException {
        SubscribeContext subscribeContext = createSubscribeContextTemperature();
        String subscriptionId = subscriptions.addSubscription(subscribeContext);

        Assert.notNull(subscriptionId);
        Assert.hasLength(subscriptionId);
        Assert.notNull(subscriptions.getSubscription(subscriptionId));
        Map<String, Subscription> subscriptions = subscriptionsRepository.getAllSubscriptions();
        Assert.isTrue(subscriptions.size()==1);
        Assert.notNull(subscriptions.get(subscriptionId).getExpirationDate());
    }

    @Test
    public void addSubscriptionWithNegativeDurationTest() throws SubscriptionException, URISyntaxException, SubscriptionPersistenceException {
        thrown.expect(SubscriptionException.class);
        thrown.expectMessage("negative duration is not allowed");
        SubscribeContext subscribeContext = createSubscribeContextTemperature();
        subscribeContext.setDuration("-PT10S");
        subscriptions.addSubscription(subscribeContext);
        Assert.isTrue(subscriptionsRepository.getAllSubscriptions().size()==0);
    }

    @Test
    public void addSubscriptionWithBadDurationTest() throws SubscriptionException, URISyntaxException, SubscriptionPersistenceException {
        thrown.expect(SubscriptionException.class);
        thrown.expectMessage("bad duration: PIPO");
        SubscribeContext subscribeContext = createSubscribeContextTemperature();
        subscribeContext.setDuration("PIPO");
        subscriptions.addSubscription(subscribeContext);
        Assert.isTrue(subscriptionsRepository.getAllSubscriptions().size()==0);
    }

    @Test
    public void addSubscriptionWithZeroDurationTest() throws SubscriptionException, URISyntaxException, SubscriptionPersistenceException {
        SubscribeContext subscribeContext = createSubscribeContextTemperature();
        subscribeContext.setDuration("PT0S");
        String subscriptionId = subscriptions.addSubscription(subscribeContext);
        Assert.notNull(subscriptionId);
        Assert.hasLength(subscriptionId);
        Subscription subscription = subscriptions.getSubscription(subscriptionId);
        Assert.notNull(subscription);
        Assert.notNull(subscription.getExpirationDate());
        Assert.notNull(subscription.getSubscriptionId());
        assertEquals(subscriptionId, subscription.getSubscriptionId());
        Calendar c = (Calendar) Calendar.getInstance().clone();
        c.add(Calendar.MONTH, 1);
        c.add(Calendar.HOUR, 24);
        assertFalse(subscription.getExpirationDate().isAfter(c.toInstant()));
        c.add(Calendar.HOUR, -48);
        assertFalse(subscription.getExpirationDate().isBefore(c.toInstant()));
        Assert.isTrue(subscriptionsRepository.getAllSubscriptions().size()==1);
    }

    @Test
    public void addSubscriptionWithBadPatternTest() throws SubscriptionException, URISyntaxException, SubscriptionPersistenceException {
        thrown.expect(SubscriptionException.class);
        thrown.expectMessage("bad pattern");
        SubscribeContext subscribeContext = createSubscribeContextTemperature();
        subscribeContext.getEntityIdList().get(0).setId("]|,\\((");
        subscribeContext.getEntityIdList().get(0).setIsPattern(true);

        subscriptions.addSubscription(subscribeContext);
        Assert.isTrue(subscriptionsRepository.getAllSubscriptions().size()==0);
    }

    @Test
    public void deleteExistSubscriptions() throws URISyntaxException, SubscriptionException, SubscriptionPersistenceException {
        SubscribeContext subscribeContext = createSubscribeContextTemperature();
        String subscriptionId = subscriptions.addSubscription(subscribeContext);
        Assert.isTrue(subscriptionsRepository.getAllSubscriptions().size()==1);
        UnsubscribeContext unsubscribeContext = new UnsubscribeContext(subscriptionId);
        assertTrue(subscriptions.deleteSubscription(unsubscribeContext));
        Assert.isTrue(subscriptionsRepository.getAllSubscriptions().size()==0);
    }

    @Test
    public void deleteNotExistSubscriptions() throws URISyntaxException, SubscriptionException, SubscriptionPersistenceException {
        UnsubscribeContext unsubscribeContext = new UnsubscribeContext("12345");
        assertFalse(subscriptions.deleteSubscription(unsubscribeContext));
    }

    @Test
    public void purgeExpiredSubscriptionsTest() throws URISyntaxException, SubscriptionException, InterruptedException, SubscriptionPersistenceException {
        SubscribeContext subscribeContext = createSubscribeContextTemperature();
        subscribeContext.setDuration("PT1S"); // 1s only
        String subscriptionId = subscriptions.addSubscription(subscribeContext);
        Assert.isTrue(subscriptionsRepository.getAllSubscriptions().size()==1);

        Thread.sleep(1500);

        subscriptions.purgeExpiredSubscriptions();
        assertNull(subscriptions.getSubscription(subscriptionId));
        Assert.isTrue(subscriptionsRepository.getAllSubscriptions().size()==0);
    }

    @Test
    public void testFindEntityId() throws Exception {
        // Insert 3 subscriptions
        for (String n : new String[]{"A", "B", "C"}) {
            subscriptions.addSubscription(createSubscribeContext(n, "string", false, "http://" + n, "temp"));
        }
        // Find B
        EntityId searchedEntityId = new EntityId("B", "string", false);
        Iterator<Subscription> it = subscriptions.findSubscriptions(searchedEntityId, null);
        assertTrue(it.hasNext());
        assertEquals("http://B", it.next().getSubscribeContext().getReference().toString());
        assertFalse(it.hasNext());
    }

    @Test
    public void testFindEntityIds() throws Exception {
        // Insert 3 subscriptions
        for (String n : new String[]{"A", "B", "C"}) {
            subscriptions.addSubscription(createSubscribeContext(n, "string", false, "http://" + n, "temp"));
        }
        // Insert 3 more
        for (String n : new String[]{"A", "B", "C"}) {
            subscriptions.addSubscription(createSubscribeContext(n, "string", false, "http://" + n + "2", "temp"));
        }

        // Find the two B
        EntityId searchedEntityId = new EntityId("B", "string", false);
        List<String> results = new LinkedList<>();
        subscriptions.findSubscriptions(searchedEntityId, null).forEachRemaining(subscription -> results.add(subscription.getSubscribeContext().getReference().toString()));
        Collections.sort(results);
        assertEquals(2, results.size());
        assertEquals("http://B", results.get(0));
        assertEquals("http://B2", results.get(1));
    }

    @Test
    public void testFindEntityIdPattern() throws Exception {
        // Insert 3 localRegistrations
        for (String n : new String[]{"A", "B", "C"}) {
            subscriptions.addSubscription(createSubscribeContext(n, "string", false, "http://" + n, "temp"));
        }

        // Find A and B
        EntityId searchedEntityId = new EntityId("A|B", "string", true);
        List<String> results = new LinkedList<>();
        subscriptions.findSubscriptions(searchedEntityId, null).forEachRemaining(subscription -> results.add(subscription.getSubscribeContext().getReference().toString()));
        Collections.sort(results);
        assertEquals(2, results.size());
        assertEquals("http://A", results.get(0));
        assertEquals("http://B", results.get(1));
    }

    @Test
    public void testFindEntityIdPattern2() throws Exception {
        // Insert 3 subscriptions
        subscriptions.addSubscription(createSubscribeContext("A|B", "string", true, "http://" + "AB", "temp"));
        for (String n : new String[]{"C", "D"}) {
            subscriptions.addSubscription(createSubscribeContext(n, "string", false, "http://" + n, "temp"));
        }

        // Find A and B
        EntityId searchedEntityId = new EntityId("A", "string", false);
        List<String> results = new LinkedList<>();
        subscriptions.findSubscriptions(searchedEntityId, null).forEachRemaining(subscription -> results.add(subscription.getSubscribeContext().getReference().toString()));
        Collections.sort(results);
        assertEquals(1, results.size());
        assertEquals("http://AB", results.get(0));
    }

    @Test
    public void testPartialAttributesMatch() throws Exception {
        SubscribeContext subscribeContext = createSubscribeContext("A", "string", false, "http://A" , "");
        subscribeContext.setAttributeList(Arrays.asList("temp", "humidity"));
        subscriptions.addSubscription(subscribeContext);

        // Find http://A matching temp
        Iterator<Subscription> it = subscriptions.findSubscriptions(new EntityId("A", "string", false), new HashSet<>(Arrays.asList("temp")));
        assertTrue(it.hasNext());
        assertEquals("http://A", it.next().getSubscribeContext().getReference().toString());
        assertFalse(it.hasNext());

        // Find http://A matching humidity and temp
        it = subscriptions.findSubscriptions(new EntityId("A", "string", false), new HashSet<>(Arrays.asList("humidity", "temp")));
        assertTrue(it.hasNext());
        assertEquals("http://A", it.next().getSubscribeContext().getReference().toString());
        assertFalse(it.hasNext());

        // Find http://A matching humidity
        it = subscriptions.findSubscriptions(new EntityId("A", "string", false), new HashSet<>(Arrays.asList("humidity", "pressure")));
        assertTrue(it.hasNext());
        assertEquals("http://A", it.next().getSubscribeContext().getReference().toString());
        assertFalse(it.hasNext());
    }

    @Test
    public void testFindEntityIdNoMatch() throws Exception {
        // Insert 3 subscriptions
        for (String n : new String[]{"A", "B", "C"}) {
            subscriptions.addSubscription(createSubscribeContext(n, "string", false, "http://" + n, "temp"));
        }

        EntityId searchedEntityId = new EntityId("D", "string", false);
        Iterator<Subscription> it = subscriptions.findSubscriptions(searchedEntityId, null);
        assertFalse(it.hasNext());

        searchedEntityId = new EntityId("B", "wrongtype", false);
        it = subscriptions.findSubscriptions(searchedEntityId, null);
        assertFalse(it.hasNext());
    }

    @Test
    public void testFindEntityIdExpired() throws Exception {
        // Insert 3 localRegistrations with short expiration
        for (String n : new String[]{"A", "B", "C"}) {
            SubscribeContext subscribeContext = createSubscribeContext(n, "string", false, "http://" + n, "temp");
            subscribeContext.setDuration("PT1S");
            subscriptions.addSubscription(subscribeContext);
        }

        // Wait for expiration
        Thread.sleep(1500);

        EntityId searchedEntityId = new EntityId("A", "string", false);
        Iterator<Subscription> it = subscriptions.findSubscriptions(searchedEntityId, null);
        assertFalse(it.hasNext());

        searchedEntityId = new EntityId("B", "string", false);
        it = subscriptions.findSubscriptions(searchedEntityId, null);
        assertFalse(it.hasNext());

        searchedEntityId = new EntityId("C", "string", false);
        it = subscriptions.findSubscriptions(searchedEntityId, null);
        assertFalse(it.hasNext());
    }
}
