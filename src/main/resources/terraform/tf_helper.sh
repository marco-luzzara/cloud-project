#!/bin/sh

# template: https://sharats.me/posts/shell-script-best-practices/

set -o errexit
set -o nounset
if [ "${TRACE-0}" = "1" ]; then
    set -o xtrace
fi

cd "$(dirname "$0")"

helpFunction()
{
   echo ""
   echo "Usage: $0 -e [production|localstack] -o [plan|apply]"
   printf "\t-e The environment where the resources should be created\n"
   printf "\t-o The operation to terraform executes\n"
   exit 1
}

main() {
    while getopts ":e:o:" opt
    do
        case "$opt" in
            e ) AWS_ENV="$OPTARG" ;;
            o ) TERRAFORM_OP="$OPTARG" ;;
            ? ) helpFunction ;;
        esac
    done

    # remove the already processed argument so that $@ now contains the remained arguments
    #   shift 2

    VAR_FILE_OPTION="$(find . -name "${AWS_ENV}*.tfvars" | awk '{ print "-var-file="$0 }' | tr '\n' ' ')"
    terraform "$TERRAFORM_OP" $([ "$TERRAFORM_OP" = "apply" ] && echo "-auto-approve") $VAR_FILE_OPTION
}

main "$@"

