package com.epam.rd.autocode.spring.project.testdata;

import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeUpdateDTO;
import com.epam.rd.autocode.spring.project.model.Employee;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EmployeeData {
    public static final String EMPLOYEE_EMAIL_1 = "mike.stevens@bookstore.com";
    public static final String EMPLOYEE_EMAIL_2 = "lisa.rodriguez@bookstore.com";
    public static final String EMPLOYEE_EMAIL_3 = "anna.thompson@bookstore.com";

    public static List<Employee> getEmployeeEntities() {
        return new ArrayList<>(List.of(
                getEmployeeEntity(),
                new Employee(2L, EMPLOYEE_EMAIL_2, "password123", "Lisa Rodriguez",
                        "+380971578786", LocalDate.of(1990, 7, 22)),
                new Employee(3L, EMPLOYEE_EMAIL_3, "password123", "Anna Thompson",
                        "+380971518786", LocalDate.of(1988, 12, 3))
        ));
    }

    public static Employee getEmployeeEntity(){
        return new Employee(1L, EMPLOYEE_EMAIL_1, "password123", "Mike Stevens",
                "+380971567786", LocalDate.of(1985, 3, 15));
    }

    public static List<EmployeeDTO> getEmployeeDTOs() {
        return new ArrayList<>(List.of(
                getEmployeeDTO(),
                new EmployeeDTO(EMPLOYEE_EMAIL_2, "password123", "Lisa Rodriguez",
                        "+380971578786", LocalDate.of(1990, 7, 22)),
                new EmployeeDTO(EMPLOYEE_EMAIL_3, "password123", "Anna Thompson",
                        "+380971518786", LocalDate.of(1988, 12, 3))
        ));
    }

    public static EmployeeDTO getEmployeeDTO() {
        return new EmployeeDTO(EMPLOYEE_EMAIL_1, "password123", "Mike Stevens",
                "+380971518786", LocalDate.of(1985, 3, 15));
    }

    public static EmployeeUpdateDTO getEmployeeUpdateDTO() {
        return new EmployeeUpdateDTO("Mike Stevens", "+380971518786", LocalDate.of(1985, 3, 15));
    }
}
