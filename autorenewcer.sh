#!/bin/bash

# Run acme.sh renewal command
$HOME/.acme.sh/acme.sh --cron --home $HOME/.acme.sh/

# Get certificate path
CERT_DIR=$HOME/.acme.sh/neoai.bot_ecc
FULLCHAIN=$CERT_DIR/fullchain.cer

# Check if renewal was successful or needed
if [ $? -ne 0 ]; then
  echo "acme.sh renewal command failed"
  exit 1
fi

# Check if certificate file was modified recently (e.g., within the past day)
if [ $(find $FULLCHAIN -mtime -1) ]; then
  echo "Certificate has been renewed, proceeding with the following steps"

  # Define other paths
  CONTAINER_NAME=servaweb
  TOMCAT_CONF_DIR=/usr/local/tomcat/conf

  # Copy certificate files to Docker container
  docker cp $CERT_DIR/fullchain.cer $CONTAINER_NAME:$TOMCAT_CONF_DIR/
  docker cp $CERT_DIR/neoai.bot.key $CONTAINER_NAME:$TOMCAT_CONF_DIR/
  docker cp $CERT_DIR/ca.cer $CONTAINER_NAME:$TOMCAT_CONF_DIR/

  # Check if copying to Docker container was successful
  if [ $? -ne 0 ]; then
    echo "Failed to copy certificate files to Docker container"
    exit 1
  fi

  # Restart Docker container to apply new certificate
  docker restart $CONTAINER_NAME

  # Check if restart was successful
  if [ $? -ne 0 ]; then
    echo "Failed to restart Docker container"
    exit 1
  fi

  echo "Certificate renewal and application successful"

else
  echo "Certificate not renewed, no further action required"
fi

