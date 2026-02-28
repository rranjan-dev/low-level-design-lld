package com.lld.elevatorsystem.models;

public class Person {
    private final String personId;
    private final String name;

    public Person(String personId, String name) {
        this.personId = personId;
        this.name = name;
    }

    public String getPersonId() {
        return personId;
    }

    public String getName() {
        return name;
    }

    /**
     * Example output: "Alice [P1]"
     */
    @Override
    public String toString() {
        return name + " [" + personId + "]";
    }
}
