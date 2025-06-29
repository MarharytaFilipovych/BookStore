package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.BookItemDTO;
import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.exception.OrderMustContainClientException;
import com.epam.rd.autocode.spring.project.mappers.BookItemMapper;
import com.epam.rd.autocode.spring.project.mappers.OrderMapper;
import com.epam.rd.autocode.spring.project.model.*;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.repo.EmployeeRepository;
import com.epam.rd.autocode.spring.project.repo.OrderRepository;
import com.epam.rd.autocode.spring.project.service.OrderService;
import com.epam.rd.autocode.spring.project.service.SortMappingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final EmployeeRepository employeeRepository;
    private final ClientRepository clientRepository;
    private final BookItemMapper bookItemMapper;
    private final BookRepository bookRepository;
    private final SortMappingService sortMappingService;

    public OrderServiceImpl(OrderRepository orderRepository, OrderMapper orderMapper, EmployeeRepository employeeRepository, ClientRepository clientRepository, BookItemMapper bookItemMapper, BookRepository bookRepository, SortMappingService sortMappingService) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.employeeRepository = employeeRepository;
        this.clientRepository = clientRepository;
        this.bookItemMapper = bookItemMapper;
        this.bookRepository = bookRepository;
        this.sortMappingService = sortMappingService;
    }

    @Override
    public Page<OrderDTO> getAllOrders(Pageable pageable) {
        Pageable mappedPageable = sortMappingService.applyMappings(pageable, "order");
        return orderRepository.findAll(mappedPageable).map(orderMapper::toDto);
    }

    @Override
    public List<OrderDTO> getOrdersByClient(String clientEmail) {
        return orderRepository.findAllByClient_Email(clientEmail).stream().map(orderMapper::toDto).toList();
    }

    @Override
    public Page<OrderDTO> getOrdersByClient(String clientEmail, Pageable pageable) {
        Pageable mappedPageable = sortMappingService.applyMappings(pageable, "order");
        return orderRepository.findAllByClient_Email(clientEmail, mappedPageable).map(orderMapper::toDto);
    }

    @Override
    public List<OrderDTO> getOrdersByEmployee(String employeeEmail) {
        return orderRepository.findAllByEmployee_Email(employeeEmail).stream().map(orderMapper::toDto).toList();
    }

    @Override
    public Page<OrderDTO> getOrdersByEmployee(String employeeEmail, Pageable pageable) {
        Pageable mappedPageable = sortMappingService.applyMappings(pageable, "order");
        return orderRepository.findAllByEmployee_Email(employeeEmail, mappedPageable).map(orderMapper::toDto);
    }

    @Override
    public OrderDTO addOrder(OrderDTO dto) {
        if(dto.getClientEmail() == null)throw new OrderMustContainClientException();
        Order order = orderMapper.toEntity(dto);
        Client client = clientRepository.getByEmail(dto.getClientEmail())
                .orElseThrow(() -> new NotFoundException("Client with email " + dto.getClientEmail()));
        order.setEmployee(null);
        order.setClient(client);

        if(dto.getBookItems() != null && !dto.getBookItems().isEmpty()){
            List<BookItem> bookItems = mapBookItems(dto.getBookItems(), order);
            order.setBookItems(bookItems);
        }
        return orderMapper.toDto(orderRepository.save(order));
    }

    @Override
    public void confirmOrder(OrderDTO dto) {
        Order order = orderRepository.findDistinctByClient_EmailAndOrderDate(dto.getClientEmail(), dto.getOrderDate());
        Employee employee = employeeRepository.getByEmail(dto.getEmployeeEmail())
                .orElseThrow(()-> new NotFoundException("Employee with email " + dto.getEmployeeEmail()));
        order.setEmployee(employee);
        orderRepository.save(order);
    }

    private List<BookItem> mapBookItems(List<BookItemDTO> bookItemDTOs, Order order) {
        return bookItemDTOs.stream()
                .map(dto -> {
                    BookItem bookItem = bookItemMapper.toEntity(dto);
                    Book book = bookRepository.findByName(dto.getBookName())
                            .orElseThrow(() -> new NotFoundException("Book with name " + dto.getBookName()));
                    bookItem.setBook(book);
                    bookItem.setOrder(order);
                    return bookItem;
                })
                .toList();
    }
}
