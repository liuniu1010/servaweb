package org.neo.servaweb;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import java.util.List;
import java.util.ArrayList;

import org.neo.servaweb.model.Employee;

@Path("/employees")
public class EmployeeResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Employee> getEmployees() {
        List<Employee> employees = new ArrayList<>();
        employees.add(new Employee(1, "Tom", "Manager"));
        employees.add(new Employee(2, "Neo", "AI Expert"));
        return employees;
    }
}
