#!/usr/bin/env bash

# template: https://sharats.me/posts/shell-script-best-practices/

set -o errexit
set -o nounset
set -o pipefail
if [[ "${TRACE-0}" == "1" ]]; then
    set -o xtrace
fi

if [[ "${1-}" =~ ^-*h(elp)?$ ]]; then
    echo "Usage: ./$(basename "$0")"
    exit
fi

cd "$(dirname "$0")"

print_step_message() {
    echo "*************** $1 ..."
}

print_done() {
    echo "*************** Done"
}

main() {
    # create hot-reload folder for lambda hot-reloading
    ( cd .. && gradle buildHotReloadFolder )

    TERRAFORM_CONTAINER_NAME="terraform_for_localstack"
    TERRAFORM_VOLUME_DIR="terraform"
    LOCALSTACK_CONTAINER_NAME="localstackmain"
    LOCALSTACK_VOLUME_DIR="localstack_persistence"
    LOCALSTACK_API_KEY="$(cat ../src/test/resources/localstack/apikey.secret)"
    LOCALSTACK_PORT=4566
    PERSISTENCE=1
    export TERRAFORM_CONTAINER_NAME
    export TERRAFORM_VOLUME_DIR
    export LOCALSTACK_CONTAINER_NAME
    export LOCALSTACK_VOLUME_DIR
    export LOCALSTACK_API_KEY
    export LOCALSTACK_PORT
    export PERSISTENCE
    docker-compose up -d


    TEMPDIR=$(mktemp -d)
    echo "Created tempdir: $TEMPDIR"
    trap 'rm -rf $TEMPDIR && ./stop_local.sh' ERR
    # copy files inside terraform
    # create tar to preserve the tree structure
    print_step_message "Copying tf files on terraform"
    # ./**/*.tf does not match with root level tf files (for some reason)
    (cd ../src/main/resources/terraform && tar -czf "$TEMPDIR/tf_tree.tar.gz" ./*.tf ./**/*.tf)
    (cd ../src/test/resources/terraform && tar -czf "$TEMPDIR/tfvars_tree.tar.gz" ./*.tfvars)
    docker cp "$TEMPDIR/tf_tree.tar.gz" "$TERRAFORM_CONTAINER_NAME:/app/tf_tree.tar.gz"
    docker cp "$TEMPDIR/tfvars_tree.tar.gz" "$TERRAFORM_CONTAINER_NAME:/app/tfvars_tree.tar.gz"
    # TODO: printf does not work for variable replacement in
    cat ../src/test/resources/terraform/provider_override.tf.template | sed -e "s/%1\$s/accesskey/g" -e "s/%2\$s/secretkey/g" -e "s/%3\$s/http:\/\/$LOCALSTACK_CONTAINER_NAME:$LOCALSTACK_PORT/g" > "$TEMPDIR/provider_override.tf"
    docker cp "$TEMPDIR/provider_override.tf" "$TERRAFORM_CONTAINER_NAME:/app/provider_override.tf"

    docker exec "$TERRAFORM_CONTAINER_NAME" sh -c "tar -xzf tf_tree.tar.gz && rm tf_tree.tar.gz"
    docker exec "$TERRAFORM_CONTAINER_NAME" sh -c "tar -xzf tfvars_tree.tar.gz && rm tfvars_tree.tar.gz"
    print_done

    # I don't need to copy the zip distribution in the terraform and localstack
    # containers because lambda hot-reloading is enabled

    # terraform apply
    print_step_message "Terraform applying"
    docker exec "$TERRAFORM_CONTAINER_NAME" terraform init
    docker exec "$TERRAFORM_CONTAINER_NAME" terraform apply \
        -auto-approve \
        -var="webapp_lambda_dist_bucket=hot-reload" \
        -var="webapp_lambda_dist_bucket_key=$(pwd)/../build/hot-reload"
    print_done

    print_step_message "Cleanup"
    rm -rf "$TEMPDIR"
    print_done
}

main "$@"