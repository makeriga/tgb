#!/bin/bash
docker run --rm -it --mount type=bind,source="$(pwd)",target=/app --volume gradle_cache:/home/gradle/.gradle --workdir /app gradle:7.5.1-jdk18 gradle clean jar
