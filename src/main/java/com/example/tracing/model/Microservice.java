package com.example.tracing.model;

import java.util.Objects;

public class Microservice implements Comparable<Microservice> {
    private final String name;

    public Microservice(String name) {
        this.name = Objects.requireNonNull(name);
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Microservice that = (Microservice) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public int compareTo(Microservice o) {
        return name.compareTo(o.getName());
    }

    @Override
    public String toString() {
        return name;
    }
}
