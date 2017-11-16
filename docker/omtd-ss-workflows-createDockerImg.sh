#!/bin/bash

Dockerfile="./omtd-ss-xmi-ner.dockerfile"
DockerImg="omtd-ss-xmi-ner-docker"

docker build -t $DockerImg -f $Dockerfile ..


