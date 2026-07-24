package de.haidox.normandiereise;

public final class Place {
    public final String category;
    public final String name;
    public final String destination;
    public final String note;
    public final String badge;

    public Place(String category, String name, String destination, String note, String badge) {
        this.category = category;
        this.name = name;
        this.destination = destination;
        this.note = note;
        this.badge = badge;
    }
}
