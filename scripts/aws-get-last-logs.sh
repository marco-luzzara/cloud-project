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

main() {
    LOG_GROUPS_OUTPUT="$(awslocal logs describe-log-groups --query 'logGroups[*].logGroupName')"
    read -ra LOG_GROUP_NAMES <<< "$LOG_GROUPS_OUTPUT"

    for LOG_GROUP_NAME in "${LOG_GROUP_NAMES[@]}"
    do
        echo "********* Logging group $LOG_GROUP_NAME"
        LOG_STREAMS_OUTPUT="$(awslocal logs describe-log-streams --log-group-name "$LOG_GROUP_NAME" --query 'logStreams[*].logStreamName')"
        read -ra LOG_STREAM_NAMES <<< "$LOG_STREAMS_OUTPUT"

        for LOG_STREAM_NAME in "${LOG_STREAM_NAMES[@]}"
        do
            echo "+++++++++++++++++++++++++ Logs for $LOG_GROUP_NAME -> $LOG_STREAM_NAME"
            awslocal logs get-log-events --log-stream-name "$LOG_STREAM_NAME" --log-group-name "$LOG_GROUP_NAME" --query 'events[*].message'

            # at the end of each test I clear the log streams of each group so that I will not
            # have repeated logs in the following test
            awslocal logs delete-log-stream --log-group-name "$LOG_GROUP_NAME" --log-stream-name "$LOG_STREAM_NAME"
        done
    # sed here is necessary because with text output the logs are (inexplicably) tab-separated
    done | sed 's/\t/\n/g'
}

main "$@"