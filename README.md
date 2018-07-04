# MASAI

[//]: # (Common expressions:)
[//]: # (* MASAI - ARI - Manufacturing & Retail Sector)

## Table of contents

* [Introduction](#section_introduction)
* [Description](#section_description)
* [How to Start Up the System](#section_start)
* [How to Use](#section_usage)

<a name='section_introduction'/>
## Introduction

This document contains MASAI's documentation, including the following parts:

* Description of MASAI
* Documentation about how to start up MASAI
* Documentation about how to use MASAI

<a name='section_description'/>
## Description

MASAI is a middleware component to support continuous data collection from IoT based resources.
In a typical IT infrastructure, it is usually located at gateway level, between data producers
or resources and data consumers. One of its main features is that it can be deployed in low power
devices, like a Raspberry PI.
MASAI is composed by a set of components that tackle three different issues:
communication, management of the devices and data handling.

* Protocol Adaptation is used for enabling the communication between both IoT systems and
FIWARE Orion Context Broker (OCB), granting interoperability and adaptation between different
protocols and the matching between data providers and consumers and providers.

*	For managing the different types of IoT devices, a component called Device Management is present.
This module encloses generic information about the devices and also addresses their security and connectivity.

*	Supporting data handling functionalities, MASAI is also granted with a sub component
that ensures that the data obtained in the IoT world is pre-filtered before being passed
to OCB, reducing the flow or the quantity of inaccurate data.

<a name='section_start'/>
## How to start up the system

Necessary software prerequisites:

[Node.js](https://nodejs.org/), [npm](https://www.npmjs.com/), [Java8](http://www.oracle.com/technetwork/java/javase/downloads/index-jsp-138363.html), [Maven](https://maven.apache.org/) and a [MQTT](http://mqtt.org/) broker running.

Clone the repository

    $ sudo git clone https://github.com/ARI-MR/MASAI.git masai

Prepare iotagent-node-lib as a dependency of iotagent-mqtt

    $ cd masai/iotagent-node-lib
    $ sudo npm install

Prepare iotagent-mqtt

    $ cd masai/iotagent-mqtt
    $ sudo npm install

Copy iotagent-node-lib into node_modules folder of iotagent-mqtt

    $ cd masai/
    $ sudo cp -r iotagent-node-lib/ iotagent-mqtt/node_modules/

Before starting the iotagent-mqtt, check config.json file, where it has to be set where the MQTT broker is
running and in which port will the agent be running:

    config.mqtt = {
      host: 'localhost',
      port: 1883,
      defaultKey: 'ATOS',
      thinkingThingsPlugin: true
    }
    server: {
      port: 4041
    }

Start iotagent-mqtt

    $ cd masai/iotagent-mqtt
    $ bin/iotagentMqtt.js

Once the agent is started, it can be started the cepheus engine

    $ cd masai/fiware-cepheus
    $ sudo mvn clean install

Start cepheus-broker

    $ cd masai/fiware-cepheus/
    $ sudo mvn spring-boot:run

Start cepheus-cep

    $ cd masai/fiware-cepheus
    $ sudo mvn spring-boot:run

<a name='section_usage'/>
## How to use the system

Once the system has been started, it is necessary to configure it.
In the demo folder it can be found an example to provide the necessary information:

    $ cd masai/demo

Three files can be found at the previous directory:
*	agent.json --> Definition of Devices including its attributes into MASAI's IoT agent.
*	cepheus.json --> Definition of Managed entities (input and ouput), including its attributes and
its data handling rules into MASAI's Cepheus.
*	configure.sh --> Executable file to configure the system using the files described above.

To configure the system using the providing configuration, execute:

    $ cd masai/demo
    $ ./configure.sh

MASAI supports the MQTT protocol to receive data from devices. When finished configuration, MASAI
responds with the MQTT topics created, which should follow the next structure:

*	For single attribute measurement: /ATOS/${deviceName}/attributes/+
*	For multiple attributes measurements: /ATOS/${deviceName}/attributes

Sending a single measurement:

    Topic: /ATOS/device1/attributes/temperature
    Data to topic:
    $ mosquitto_pub -h ${host} -t "/ATOS/device1/attributes/temperature" -m "19"
    host: represents the host where the MQTT broker is running, typically localhost.

Sending multiple measurements:

    Topic: /ATOS/device1/attributes
    Data to topic:
    $ mosquitto_pub -h ${host} -t /ATOS/device1/attributes -m '{"temperature":"31","humidity":"27"}'
    host: represents the host where the MQTT broker is running, typically localhost.
