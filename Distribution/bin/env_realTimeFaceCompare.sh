#!/bin/bash
set -x

#cd `dirname $0`
#:pwd
INSTALL_HOME=/opt/RealTimeFaceCompare

#cluster home
export CLUSTER_HOME=${INSTALL_HOME}/cluster
export PATH=$PATH:$CLUSTER_HOME/bin

#common home
export COMMON_HOME=${INSTALL_HOME}/common
export PATH=$PATH:$COMMON_HOME/bin

#ftp home
export FTP_HOME=${INSTALL_HOME}/ftp
export PATH=$PATH:$FTP_HOME/bin

#service home
export SERVICE_HOME=${INSTALL_HOME}/service
export PATH=$PATH:$SERVICE_HOME/bin


set +x
