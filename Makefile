SHELL := /bin/bash -e -u -o pipefail

TERRAFORM_CONTAINER_NAME ?= terraform_for_localstack
TERRAFORM_VOLUME_DIR ?= terraform
LOCALSTACK_CONTAINER_NAME ?= localstackmain
LOCALSTACK_VOLUME_DIR ?= localstack_persistence
LOCALSTACK_AUTH_TOKEN ?= $(shell cat cloud/infrastructure/src/testFixtures/resources/localstack/auth_token.secret)
LOCALSTACK_PORT ?= 4566
LOCALSTACK_PERSISTENCE ?= 1
LOCALSTACK_NETWORK_NAME ?= localstack_network
export TERRAFORM_CONTAINER_NAME
export TERRAFORM_VOLUME_DIR
export LOCALSTACK_CONTAINER_NAME
export LOCALSTACK_VOLUME_DIR
export LOCALSTACK_AUTH_TOKEN
export LOCALSTACK_PORT
export LOCALSTACK_PERSISTENCE
export LOCALSTACK_NETWORK_NAME

.PHONY: build start stop restart tf_apply destroy follow_lambda_logs get_rest_api_id

start: stop build
	docker-compose up -d
	trap '$(MAKE) stop' ERR && $(MAKE) tf_apply

build:
	gradle buildHotReloadFolder

stop:
	./scripts/stop.sh

restart: stop start

tf_apply: build
	./scripts/tf_apply.sh

destroy: stop
	chown -R 1000 ./volumes
	rm -rf ./volumes

follow_lambda_logs:
	./scripts/follow_lambda_logs.sh

get_rest_api_id:
	docker exec "${LOCALSTACK_CONTAINER_NAME}" awslocal apigateway get-rest-apis --output text --query "items[0].id"