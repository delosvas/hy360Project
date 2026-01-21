package model;

/*
 * Department
 * 
 * Model class
 */
public class Department {
    private int id; // Unique Identifier
    private String name;

    /*
     * Constructor for creating a new department object
     */
    public Department(int id, String name) {
        this.id = id;
        this.name = name;
    }

    // Following setters and Getters for every field
    
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
