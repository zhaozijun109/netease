#!/bin/bash
export WEB_HOME=$(cd $(dirname $0)/..; pwd)
export CLASSPATH=${WEB_HOME}/conf:${WEB_HOME}/lib/*:${CLASSPATH}
if [ -n ${JAVA_HOME} ]; then
  export JAVA_HOME=/home/hadoop/java-current
fi
NDI_OPTS="-Xmx4G -Xms4G ${NDI_OPTS}"
NDI_OPTS="-Dlog.dir=${WEB_HOME}/logs ${NDI_OPTS}"
NDI_OPTS="-Dcom.sun.management.jmxremote.port=8999 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false ${NDI_OPTS}"
DEBUG_MODE="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
nohup ${JAVA_HOME}/bin/java ${DEBUG_MODE} ${NDI_OPTS} com.netease.bdms.ndi.service.web.DataIntegrationWeb > nohup.out 2>&1 &
echo $! > ${WEB_HOME}/pidfile