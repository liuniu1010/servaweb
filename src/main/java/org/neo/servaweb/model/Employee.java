package org.neo.servaweb.model;

public class Employee {
    private int id;
    private String name;
    private String department;

    public Employee(int inputId, String inputName, String inputDepartment) {
        id = inputId;
        name = inputName;
        department = inputDepartment;
    }

    public int getId() {
        return id;
    }

    public void setId(int inputId) {
        id = inputId;
    }

    public String getName() {
        return name;
    }

    public void setName(String inputName) {
        name = inputName;
    }
    
    public String getDepartment() {
        return department;
    }

    public void setDepartment(String inputDepartment) {
        department = inputDepartment;
    }
}
