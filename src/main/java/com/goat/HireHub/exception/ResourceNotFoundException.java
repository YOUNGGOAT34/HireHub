package com.goat.HireHub.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " not found with id " + id);
    }

    public ResourceNotFoundException(String resource) {
        super(resource + " not found with id ");
    }
}