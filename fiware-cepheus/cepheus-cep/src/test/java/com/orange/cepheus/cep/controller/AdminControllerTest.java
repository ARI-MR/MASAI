/*
 * Copyright (C) 2015 Orange
 *
 * This software is distributed under the terms and conditions of the 'GNU GENERAL PUBLIC LICENSE
 * Version 2' license which can be found in the file 'LICENSE.txt' in this package distribution or
 * at 'http://www.gnu.org/licenses/gpl-2.0-standalone.html'.
 */

package com.orange.cepheus.cep.controller;

import com.orange.cepheus.cep.Application;
import com.orange.cepheus.cep.ComplexEventProcessor;
import com.orange.cepheus.cep.EventMapper;
import com.orange.cepheus.cep.exception.ConfigurationException;
import com.orange.cepheus.cep.exception.PersistenceException;
import com.orange.cepheus.cep.model.Configuration;
import com.orange.cepheus.cep.model.Statement;
import com.orange.cepheus.cep.persistence.Persistence;
import com.orange.cepheus.cep.tenant.TenantFilter;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.web.context.WebApplicationContext;

import java.util.LinkedList;
import java.util.List;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.Assert.assertEquals;
import static com.orange.cepheus.cep.Util.*;


/**
 * Test the Admin controller
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {Application.class})
@WebAppConfiguration
public class AdminControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private MappingJackson2HttpMessageConverter mapping;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Mock
    private ComplexEventProcessor complexEventProcessor;

    @Mock
    private Persistence persistence;

    @Mock
    private EventMapper eventMapper;

    @Autowired
    @InjectMocks
    AdminController adminController;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @After
    public void resetMocks() {
        reset(complexEventProcessor);
        reset(persistence);
        reset(eventMapper);
    }

    @Test
    public void checkConfigurationNotFound() throws Exception {
        when(complexEventProcessor.getConfiguration()).thenReturn(null);

        mockMvc.perform(get("/v1/admin/config")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void postConfOK() throws Exception {
        Configuration configuration = getBasicConf();

        mockMvc.perform(post("/v1/admin/config").content(json(mapping, configuration)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        ArgumentCaptor<Configuration> configurationArg = ArgumentCaptor.forClass(Configuration.class);
        verify(complexEventProcessor).setConfiguration(configurationArg.capture());

        Configuration capturedConfiguration = configurationArg.getValue();
        assertEquals(1, capturedConfiguration.getEventTypeIns().size());
        assertEquals("S.*", capturedConfiguration.getEventTypeIns().get(0).getId());
        assertEquals(1, capturedConfiguration.getEventTypeOuts().size());
        assertEquals("OUT1", capturedConfiguration.getEventTypeOuts().get(0).getId());

        verify(persistence).saveConfiguration(eq(TenantFilter.DEFAULT_TENANTID), eq(capturedConfiguration));
    }

    @Test
    public void getConfiguration() throws Exception {
        Configuration configuration = getBasicConf();
        when(complexEventProcessor.getConfiguration()).thenReturn(configuration);

        mockMvc.perform(get("/v1/admin/config")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.in[0].id").value(configuration.getEventTypeIns().get(0).getId()))
                .andExpect(jsonPath("$.out[0].id").value(configuration.getEventTypeOuts().get(0).getId()))
                .andExpect(jsonPath("$.statements[0]").value(configuration.getStatements().get(0)));
    }

    @Test
    public void deleteConfiguration() throws Exception {

        mockMvc.perform(delete("/v1/admin/config")).andExpect(status().isOk());

        verify(complexEventProcessor).reset();
        verify(persistence).deleteConfiguration(eq(TenantFilter.DEFAULT_TENANTID));
    }

    @Test
    public void checkNoStatement() throws Exception {
        List<Statement> noStatement = new LinkedList<>();
        when(complexEventProcessor.getStatements()).thenReturn(noStatement);

        mockMvc.perform(get("/v1/admin/statements")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getStatements() throws Exception {
        List<Statement> statements = new LinkedList<>();
        Statement statement1 = new Statement("statement1", "insert into TempSensorAvg select \"OUT1\" as id, avg(temp) as avgTemp, temp_unit as avgTemp_unit from TempSensor.win:time(2 seconds) where TempSensor.id=\"S1\"");
        statements.add(statement1);
        Statement statement2 = new Statement("statement2", "ON FenceCross fc MERGE TrackerState ts WHERE fc.id = ts.id WHEN NOT MATCHED THEN INSERT SELECT id, time, location, inside WHEN MATCHED AND fc.inside != ts.inside THEN UPDATE SET inside = fc.inside");
        statements.add(statement2);
        when(complexEventProcessor.getStatements()).thenReturn(statements);

        mockMvc.perform(get("/v1/admin/statements")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andExpect(jsonPath("$.[0].name").value("statement1"))
                .andExpect(jsonPath("$.[1].name").value("statement2"));
    }

    @Test
    public void configurationErrorHandling() throws Exception {
        Configuration configuration = getBasicConf();

        doThrow(new ConfigurationException("ERROR", new Exception("DETAIL ERROR"))).when(complexEventProcessor).setConfiguration(any(Configuration.class));

        mockMvc.perform(post("/v1/admin/config").content(json(mapping, configuration)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.reasonPhrase").value("ERROR"))
                .andExpect(jsonPath("$.details").value("DETAIL ERROR"));
    }

    @Test
    public void eventMapperErrorHandling() throws Exception {
        Configuration configuration = getBasicConf();

        doThrow(new ConfigurationException("ERROR", new Exception("DETAIL ERROR"))).when(eventMapper).setConfiguration(any(Configuration.class));

        mockMvc.perform(post("/v1/admin/config").content(json(mapping, configuration)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.reasonPhrase").value("ERROR"))
                .andExpect(jsonPath("$.details").value("DETAIL ERROR"));
    }

    @Test
    public void persistenceErrorHandling() throws Exception {

        doThrow(new PersistenceException("ERROR")).when(persistence).saveConfiguration(any(), any(Configuration.class));

        Configuration configuration = getBasicConf();

        mockMvc.perform(post("/v1/admin/config").content(json(mapping, configuration)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("500"))
                .andExpect(jsonPath("$.reasonPhrase").value("ERROR"));
    }

}
