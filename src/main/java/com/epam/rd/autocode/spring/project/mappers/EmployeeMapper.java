package com.epam.rd.autocode.spring.project.mappers;

import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeUpdateDTO;
import com.epam.rd.autocode.spring.project.model.Employee;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class EmployeeMapper {
    private final ModelMapper mapper;

    public EmployeeMapper(ModelMapper mapper) {
        this.mapper = mapper;
        mapper.createTypeMap(EmployeeDTO.class, Employee.class).addMappings(m ->
                m.skip(Employee::setId)
        );
        mapper.createTypeMap(EmployeeUpdateDTO.class, Employee.class)
                .addMappings(mapping -> {
                    mapping.skip(Employee::setId);
                    mapping.skip(Employee::setPassword);
                    mapping.skip(Employee::setEmail);
                });
    }

    public EmployeeDTO toDto(Employee employee){
        return mapper.map(employee, EmployeeDTO.class);
    }

    public Employee toEntity(EmployeeDTO dto){
        return mapper.map(dto, Employee.class);
    }

    public Employee toEntity(EmployeeUpdateDTO dto){
        return mapper.map(dto, Employee.class);
    }
}
