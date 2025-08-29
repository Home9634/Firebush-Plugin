package me.home4.firebush.firebush.classes;

public class Task {
    private String name;
    private String description;
    private int session;

    private Integer id;

    public Task(String name, String description, int session, Integer id) {
        this.name = name;
        this.description = description;
        this.session = session;
        this.id = id;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getTask() {
        return description;
    }

    public int getSession() {
        return session;
    }

    public int getId() {
        return id;
    }
}
