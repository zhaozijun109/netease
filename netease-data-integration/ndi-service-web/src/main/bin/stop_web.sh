#!/bin/bash
export WEB_HOME=$(cd $(dirname $0)/..; pwd)
PID=$(cat ${WEB_HOME}/pidfile)
kill -9 ${PID}