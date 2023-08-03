#!/usr/bin/env bash

docker container rm -f $(docker container ls -a | grep "public.ecr.aws" | cut -f 1 -d ' ')