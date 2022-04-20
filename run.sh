#! /usr/bin/env bash

JAVA_HOME="/Library/Java/JavaVirtualMachines/openjdk-17.jdk/Contents/Home" ./mvnw clean install && docker compose build && docker compose up