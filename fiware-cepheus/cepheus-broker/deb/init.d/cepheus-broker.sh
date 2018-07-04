#!/bin/sh

### BEGIN INIT INFO
# Provides: Cepheus-Broker daemon
# Required-Start: $local_fs $network
# Required-Stop: $local_fs $remote_fs
# Default-Start: 2 3 4 5
# Default-Stop: 0 1 6
# Short-Description: Gateway-level NGSI broker from the FIWARE-Cepheus project
# Description: Gateway-level NGSI broker from the FIWARE-Cepheus project.
### END INIT INFO

SERVICE_NAME=Cepheus-Broker
PATH_TO_JAR=/usr/lib/cepheus/cepheus-broker.jar
PID_PATH_NAME=/tmp/cepheus-broker-pid

CONFIG=/etc/cepheus/broker.properties
LOG=/var/log/cepheus/broker.log
DATA=/var/cepheus/cepheus-broker.db
PORT=8081

ARGS="--spring.config.location=$CONFIG --spring.datasource.url=jdbc:sqlite:$DATA --logging.mode=file --logging.file=$LOG --port=$PORT"

case $1 in
    start)
        echo "Starting $SERVICE_NAME ..."
        if [ ! -f $PID_PATH_NAME ]; then
            nohup java -jar $PATH_TO_JAR $ARGS 2>> /dev/null >> /dev/null &
            echo $! > $PID_PATH_NAME
            echo "$SERVICE_NAME started on port $PORT"
        else
            echo "$SERVICE_NAME is already running on port $PORT"
        fi
    ;;
    stop)
        if [ -f $PID_PATH_NAME ]; then
            PID=$(cat $PID_PATH_NAME);
            echo "$SERVICE_NAME stoping ..."
            kill $PID;
            echo "$SERVICE_NAME stopped ..."
            rm $PID_PATH_NAME
        else
            echo "$SERVICE_NAME is not running ..."
        fi
    ;;
    restart)
        if [ -f $PID_PATH_NAME ]; then
            PID=$(cat $PID_PATH_NAME);
            echo "$SERVICE_NAME stopping ...";
            kill $PID;
            echo "$SERVICE_NAME stopped ...";
            rm $PID_PATH_NAME
            echo "$SERVICE_NAME starting ..."
            nohup java -jar $PATH_TO_JAR $ARGS 2>> /dev/null >> /dev/null &
            echo $! > $PID_PATH_NAME
            echo "$SERVICE_NAME started on port $PORT"
        else
            echo "$SERVICE_NAME is not running ..."
        fi
    ;;
    status)
        if [ -f $PID_PATH_NAME ]; then
            PID=$(cat $PID_PATH_NAME);
            echo "$SERVICE_NAME is running";
        else
            echo "$SERVICE_NAME is not running ..."
        fi
    ;;
esac
