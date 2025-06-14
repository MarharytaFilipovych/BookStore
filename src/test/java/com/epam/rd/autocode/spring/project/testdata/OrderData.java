package com.epam.rd.autocode.spring.project.testdata;

import com.epam.rd.autocode.spring.project.dto.BookItemDTO;
import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.model.BookItem;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.model.Order;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import static com.epam.rd.autocode.spring.project.testdata.ClientData.*;
import static com.epam.rd.autocode.spring.project.testdata.EmployeeData.*;
import static com.epam.rd.autocode.spring.project.testdata.BookItemData.*;

public class OrderData {
    public static List<Order> getOrderEntities() {
        List<Employee> employees = getEmployeeEntities();
        List<Client> clients = getClientEnities();
        List<BookItem> bookItems = getBookItemEntities();

        return new ArrayList<>(List.of(
                new Order(1L, employees.get(0), clients.get(0),
                        LocalDateTime.of(2025, 6, 10, 14, 30), new BigDecimal("47.97"),
                        List.of(bookItems.get(0), bookItems.get(1))),
                new Order(2L, employees.get(1), clients.get(1),
                        LocalDateTime.of(2025, 6, 11, 9, 15), new BigDecimal("89.96"),
                        List.of(bookItems.get(2))),
                new Order(3L, employees.get(2), clients.get(2),
                        LocalDateTime.of(2025, 6, 12, 16, 45), new BigDecimal("31.98"),
                        new ArrayList<>())
        ));
    }

    public static Order getOrderEntity(){
        return getOrderEntities().get(0);
    }

    public static List<OrderDTO> getOrderDTOs() {
        List<BookItemDTO> bookItemDTOs = getBookItemDTOs();

        return new ArrayList<>(List.of(
                getOrderDTO(),
                new OrderDTO(EMPLOYEE_EMAIL_2, CLIENT_EMAIL_2,
                        LocalDateTime.of(2025, 6, 11, 9, 15), new BigDecimal("89.96"),
                        List.of(bookItemDTOs.get(2))),
                new OrderDTO(EMPLOYEE_EMAIL_3, CLIENT_EMAIL_3,
                        LocalDateTime.of(2025, 6, 12, 16, 45), new BigDecimal("31.98"),
                        new ArrayList<>())
        ));
    }

    public static OrderDTO getOrderDTO(){
        List<BookItemDTO> bookItemDTOs = getBookItemDTOs();
        return new OrderDTO(EMPLOYEE_EMAIL_1, CLIENT_EMAIL_1,
                LocalDateTime.of(2025, 6, 10, 14, 30), new BigDecimal("47.97"),
                List.of(bookItemDTOs.get(0), bookItemDTOs.get(1)));
    }
}