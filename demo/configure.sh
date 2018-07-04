#!/bin/bash

CEP=localhost:8080
IOTAGENT=localhost:4041

# Common functions

function updateConfig() #(url, config)
{
    curl -H 'Content-Type: application/json' \
         -H 'Accept: application/json' \
         -d"$2" -s \
         $1/v1/admin/config
}

echo "Updating CEP configuration"
CONFIG=`cat cepheus.json`
updateConfig $CEP "$CONFIG"


function provisionDevice() #(url, payload)
{
    curl -H 'Content-Type: application/json' \
         -H 'Accept: application/json' \
         -H 'Fiware-Service: Hannover' \
         -H 'Fiware-ServicePath: /Messe' \
         -d"$2" -s \
         $1/iot/devices
}

echo "Provisioning Device"
PAYLOAD=`cat agent.json`
provisionDevice $IOTAGENT "$PAYLOAD"

echo "Done!"
