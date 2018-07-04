/*
 * Copyright 2014 Telefonica Investigación y Desarrollo, S.A.U
 *
 * This file is part of fiware-iotagent-lib
 *
 * fiware-iotagent-lib is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * fiware-iotagent-lib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with fiware-iotagent-lib.
 * If not, seehttp://www.gnu.org/licenses/.
 *
 * For those usages not covered by the GNU Affero General Public License
 * please contact with::[contacto@tid.es]
 */
'use strict';

var iotAgentLib = require('../../'),
    should = require('should'),
    _ = require('underscore'),
    iotAgentConfig = {
        contextBroker: {
            host: '192.168.1.1',
            port: '1026'
        },
        server: {
            port: 4041
        },
        types: {
            'Light': {
                commands: [],
                type: 'Light',
                lazy: [
                    {
                        name: 'temperature',
                        type: 'centigrades'
                    }
                ],
                active: [
                    {
                        name: 'pressure',
                        type: 'Hgmm'
                    }
                ]
            }
        },
        providerUrl: 'http://smartGondor.com',
        deviceRegistrationDuration: 'P1M',
        throttling: 'PT5S'
    },
    iotAgentConfigNoUrl = _.clone(iotAgentConfig),
    iotAgentConfigNoTypes = _.clone(iotAgentConfig);

describe('Startup tests', function() {
    describe('When the IoT Agent is started without a "providerUrl" config parameter', function() {
        beforeEach(function() {
            delete iotAgentConfigNoUrl.providerUrl;
        });

        it('should not start and raise a MISSING_CONFIG_PARAMS error', function(done) {
            iotAgentLib.activate(iotAgentConfigNoUrl, function(error) {
                should.exist(error);
                should.exist(error.name);
                error.name.should.equal('MISSING_CONFIG_PARAMS');
                done();
            });
        });
    });
    describe('When the IoT Agent is started without a "types" attribute', function() {
        beforeEach(function() {
            delete iotAgentConfigNoTypes.types;
        });

        it('should not start and raise a MISSING_CONFIG_PARAMS error', function(done) {
            iotAgentLib.activate(iotAgentConfigNoTypes, function(error) {
                should.exist(error);
                should.exist(error.name);
                error.name.should.equal('MISSING_CONFIG_PARAMS');
                done();
            });
        });
    });
});
