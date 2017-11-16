#!/bin/bash

docker build -t "omtd-ss-xmi-ner-docker" -f "./omtd-ss-xmi-ner.dockerfile" ..
docker build -t "omtd-ss-pdf-xmi-docker" -f "./omtd-ss-pdf-xmi.dockerfile" ..
