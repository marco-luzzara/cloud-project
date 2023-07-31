#!/usr/bin/env bash

return_with() {
    printf "\n%s\n" "$1"
}

get_retval_from() {
    echo "$1" | tail -1
}