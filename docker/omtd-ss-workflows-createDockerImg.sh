#!/bin/bash

docker build -t "maxxkia/omtd-ss-xmi-ner-docker" -f "./omtd-ss-xmi-ner.dockerfile" ..
docker build -t "maxxkia/omtd-ss-pdf-xmi-docker" -f "./omtd-ss-pdf-xmi.dockerfile" ..
