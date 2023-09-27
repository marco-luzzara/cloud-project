package it.unimi.cloudproject.apigw.message.model;

import java.util.Map;

public record InvocationWrapper<T> (T body, Map<String, String> headers) {}
