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
 * please contact with::daniel.moranjimenez@telefonica.com
 */
'use strict';

var async = require('async'),
    restUtils = require('./restUtils'),
    statsRegistry = require('./../stats/statsRegistry'),
    deviceService = require('./../devices/deviceService'),
    logger = require('logops'),
    errors = require('../../errors'),
    _ = require('underscore'),
    context = {
        op: 'IoTAgentNGSI.DeviceProvisioning'
    },
    apply = async.apply,
    provisioningHandler,
    updateDeviceTemplate = require('../../templates/updateDevice.json'),
    createDeviceTemplate = require('../../templates/createDevice.json'),
    mandatoryHeaders = [
        'fiware-service',
        'fiware-servicepath'
    ],
    provisioningAPITranslation = {
        /* jshint camelcase:false */

        name: 'id',
        service: 'service',
        service_path: 'subservice',
        entity_name: 'name',
        entity_type: 'type',
        timezone: 'timezone',
        attributes: 'active',
        commands: 'lazy',
        internal_attributes: 'internalAttributes'
    };

/**
 * Express middleware to handle incoming device provisioning requests. Every request is validated and handled to the
 * NGSI Service for the registration.
 */
function handleProvision(req, res, next) {

    function Topic(topic_single_attr, topic_mult_attrs, topic_command_req, topic_command_resp) {
        this.topic_single_attr = topic_single_attr;
        this.topic_mult_attrs = topic_mult_attrs;
        this.topic_command_req = topic_command_req;
        this.topic_command_resp = topic_command_resp;
    }

    function handleProvisioningFinish(error, results) {
        if (error) {
            logger.debug(context, 'Device provisioning failed due to the following error: ', error.message);
            next(error);
        } else {
            logger.debug(context, 'Device provisioning request succeeded');
	    /*var topic1="/" + req.headers['fiware-service'] + "/" + req.body.devices[0].device_id + "/attributes/+";*/
	    /*var topic2="/" + req.headers['fiware-service'] + "/" + req.body.devices[0].device_id + "/attributes";*/
	    /*var topic3="/" + req.headers['fiware-service'] + "/" + req.body.devices[0].device_id + "/configuration/commands";*/
	    /*res.status(201).json({"topic1_single_attr":topic1,"topic2_attrs":topic2,"topic3_conf_comm":topic3});*/
            var arrayList = new Array();
	    for (var i in req.body.devices){
	    	 var response = new Topic("/ATOS/" + req.body.devices[i].device_id + "/attributes/+",
					  "/ATOS/" + req.body.devices[i].device_id + "/attributes",
					  "/ATOS/command/request",
					  "/ATOS/"+ req.body.devices[i].device_id +"/command/response");
		 arrayList.push(response);
	    }
	    res.status(201).json(arrayList);
        }
    }

    function applyProvisioningHandler(device, callback) {
        if (provisioningHandler) {
            provisioningHandler(device, callback);
        } else {
            callback(null, device);
        }
    }

    function registerDevice(service, subservice, body, callback) {
        /*jshint sub:true */
        deviceService.register({
                id: body['device_id'],
                type: body['entity_type'],
                name: body['entity_name'],
                service: service,
                subservice: subservice,
                active: body['attributes'],
                staticAttributes: body['static_attributes'],
                lazy: body['lazy'],
                commands: body['commands'],
                timezone: body['timezone'],
                internalAttributes: body['internal_attributes'],
                protocol: body['protocol'],
                internalId: null
            },
            callback);
    }

    function provisionSingleDevice(device, callback) {
        async.waterfall([
            apply(statsRegistry.add, 'deviceCreationRequests', 1),
            apply(restUtils.checkMandatoryQueryParams,
                ['device_id', 'entity_type'], device),
            apply(registerDevice, req.headers['fiware-service'], req.headers['fiware-servicepath']),
            applyProvisioningHandler
        ], callback);
    }

    function extractDevices() {
        return req.body.devices;
    }

    logger.debug('Handling device provisioning request.');

    async.map(extractDevices(), provisionSingleDevice, handleProvisioningFinish);
}

/**
 * Translate an attribute from the internal representaiton format to the one required by the Provisioning API.
 *
 * @param {Object} attribute                        Attribute in internal representation format.
 * @return {{object_id: *, name: *, type: *}}      Attribute in Device Provisioning API format.
 */
function attributeToProvisioningAPIFormat(attribute) {
    return {
        object_id: attribute.object_id,
        name: attribute.name,
        type: attribute.type
    };
}

/**
 * Translate between the inner model format to the external Device Provisioning API one.
 *
 * @param {Object} device           Device object coming from the registry.
 * @return {Object}                 Device object translated to Device Provisioning API format.
 */
function toProvisioningAPIFormat(device) {
    /* jshint camelcase:false */
    return {
        device_id: device.id,
        service: device.service,
        service_path: device.subservice,
        entity_name: device.name,
        entity_type: device.type,
        timezone: device.timezone,
        attributes: device.active.map(attributeToProvisioningAPIFormat),
        lazy: device.lazy.map(attributeToProvisioningAPIFormat),
        static_attributes: device.staticAttributes,
        internal_attributes: device.internalAttributes,
        protocol: device.protocol
    };
}

/**
 * Translate between the inner model format to the external Device Provisioning API one.
 *
 * @param {Object} device           Device object coming from the registry.
 * @return {Object}                 Device object translated to Device Provisioning API format, including commands information.
 */
function toProvisioningAPIFormatWithCommands(device) {
    /* jshint camelcase:false */
    return {
        device_id: device.id,
        service: device.service,
        service_path: device.subservice,
        entity_name: device.name,
        entity_type: device.type,
        timezone: device.timezone,
        attributes: device.active.map(attributeToProvisioningAPIFormat),
        lazy: device.lazy.map(attributeToProvisioningAPIFormat),
        static_attributes: device.staticAttributes,
        internal_attributes: device.internalAttributes,
        protocol: device.protocol,
        commands: device.commands
    };
}

/**
 * Express middleware that retrieves the complete set of provisioned devices (in JSON format).
 */
function handleListDevices(req, res, next) {
    deviceService.listDevices(
        req.headers['fiware-service'],
        req.headers['fiware-servicepath'],
        req.query.limit,
        req.query.offset,
        function handleListDevices(error, deviceList) {
            if (error) {
                next(error);
            } else {
                var response = deviceList;
                response.devices = deviceList.devices.map(toProvisioningAPIFormat);

                res.status(200).json(response);
            }
        });
}

/**
 * This middleware gets de device specified in the deviceId parameter of the URL from the registry and returns it in
 * JSON format.
 */
function handleGetDevice(req, res, next) {
    deviceService.getDevice(req.params.deviceId, function(error, device) {
        if (error) {
            next(error);
        } else if (device) {
            /*logger.debug("Device content ->");*/
            /*logger.debug(JSON.stringify(device));*/
	    /*res.status(200).json(toProvisioningAPIFormat(device));*/
            res.status(200).json(toProvisioningAPIFormatWithCommands(device));
	 } else {
            next(new errors.DeviceNotFound(req.params.deviceId));
        }
    });
}

/**
 * This middleware handles the removal of a particular device specified with the deviceId.
 */
function handleRemoveDevice(req, res, next) {
    statsRegistry.add('deviceRemovalRequests', 1, function() {
        deviceService.unregister(req.params.deviceId, function(error) {
            if (error) {
                next(error);
            } else {
                res.status(204).send();
            }
        });
    });
}

/**
 * This middleware handles updates in the provisioning devices. The only attribute
 */
function handleUpdateDevice(req, res, next) {
    if (req.body.name) {
        next(new errors.BadRequest('Can\'t change the ID of a preprovisioned device'));
    } else {
        deviceService.getDevice(req.params.deviceId, function(error, device) {
            if (error) {
                next(error);
            } else if (device) {
                var pairs = _.pairs(req.body);

                for (var i in pairs[0]) {
                    device[provisioningAPITranslation[pairs[i][0]]] = pairs[i][1];
                }

                deviceService.updateRegister(device, function handleDeviceUpdate(error) {
                    if (error) {
                        next(error);
                    } else {
                        res.status(200).json(device);
                    }
                });
            } else {
                next(new errors.DeviceNotFound(req.params.deviceId));
            }
        });
    }
}

/**
 * Load the routes related to device provisioning in the Express App.
 *
 * @param {Object} router      Express request router object.
 */
function loadContextRoutes(router) {
    router.post('/iot/devices',
        restUtils.checkRequestAttributes('headers', mandatoryHeaders),
        restUtils.checkBody(createDeviceTemplate),
        handleProvision);

    router.get('/iot/devices',
        restUtils.checkRequestAttributes('headers', mandatoryHeaders), handleListDevices);

    router.get('/iot/devices/:deviceId',
        restUtils.checkRequestAttributes('headers', mandatoryHeaders), handleGetDevice);

    router.put('/iot/devices/:deviceId',
        restUtils.checkRequestAttributes('headers', mandatoryHeaders),
        restUtils.checkBody(updateDeviceTemplate),
        handleUpdateDevice);

    router.delete('/iot/devices/:deviceId',
        restUtils.checkRequestAttributes('headers', mandatoryHeaders), handleRemoveDevice);
}

function setProvisioningHandler(newHandler) {
    provisioningHandler = newHandler;
}

exports.loadContextRoutes = loadContextRoutes;
exports.setProvisioningHandler = setProvisioningHandler;
