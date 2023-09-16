package it.unimi.cloudproject.ui.lambda.model;

import java.util.Map;

public record InvocationWrapper<T> (T body, Map<String, String> headers) {}
