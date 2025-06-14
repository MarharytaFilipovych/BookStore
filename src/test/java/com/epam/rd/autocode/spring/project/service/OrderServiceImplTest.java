package com.epam.rd.autocode.spring.project.service;

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
import com.epam.rd.autocode.spring.project.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static com.epam.rd.autocode.spring.project.testdata.BookData.*;
import static com.epam.rd.autocode.spring.project.testdata.EmployeeData.*;
import static com.epam.rd.autocode.spring.project.testdata.ClientData.*;
import static com.epam.rd.autocode.spring.project.testdata.BookItemData.*;
import static com.epam.rd.autocode.spring.project.testdata.OrderData.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private BookItemMapper bookItemMapper;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order order;
    private OrderDTO testOrderDTO;
    private Employee employee;
    private Client client;
    private Book book;
    private BookItemDTO bookItemDTO;
    private List<Order> orders;
    private List<OrderDTO> orderDTOs;

    @BeforeEach
    void setUp() {
        employee = getEmployeeEntity();
        client = getClientEntity();
        book = getBookEntity();
        order = getOrderEntity();
        bookItemDTO = getBookItemDTO();
        testOrderDTO = getOrderDTO();
        orders = getOrderEntities();
        orderDTOs = getOrderDTOs();
    }

    @Test
    void getAllOrders_ShouldReturnPageOfOrderDTOs() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orderPage = new PageImpl<>(orders, pageable, orders.size());
        when(orderRepository.findAllByClientNotNullAndEmployeeNotNull(pageable))
                .thenReturn(orderPage);
        for (int i = 0; i < orders.size(); i++) {
            when(orderMapper.toDto(orders.get(i))).thenReturn(orderDTOs.get(i));
        }

        // Act
        Page<OrderDTO> result = orderService.getAllOrders(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(orders.size(), result.getTotalElements());
        assertEquals(orderDTOs, result.getContent());
        verify(orderRepository).findAllByClientNotNullAndEmployeeNotNull(pageable);
        verify(orderMapper, times(orders.size())).toDto(any(Order.class));
    }

    @Test
    void getAllOrders_WithSortByOrderDateAsc_ShouldPassCorrectSortToRepository() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10, Sort.by("orderDate").ascending());
        Page<Order> orderPage = new PageImpl<>(orders, pageable, orders.size());
        when(orderRepository.findAllByClientNotNullAndEmployeeNotNull(any(Pageable.class)))
                .thenReturn(orderPage);
        for (int i = 0; i < orders.size(); i++) {
            when(orderMapper.toDto(orders.get(i))).thenReturn(orderDTOs.get(i));
        }

        // Act
        Page<OrderDTO> result = orderService.getAllOrders(pageable);

        // Assert
        assertNotNull(result);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(orderRepository).findAllByClientNotNullAndEmployeeNotNull(pageableCaptor.capture());

        Pageable capturedPageable = pageableCaptor.getValue();
        assertNotNull(capturedPageable.getSort());
        assertTrue(capturedPageable.getSort().isSorted());

        Sort.Order orderDateOrder = capturedPageable.getSort().getOrderFor("orderDate");
        assertNotNull(orderDateOrder);
        assertEquals(Sort.Direction.ASC, orderDateOrder.getDirection());
        assertEquals("orderDate", orderDateOrder.getProperty());
    }

    @Test
    void getAllOrders_WithSortByPriceDesc_ShouldPassCorrectSortToRepository() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10, Sort.by("price").descending());
        Page<Order> orderPage = new PageImpl<>(orders, pageable, orders.size());

        when(orderRepository.findAllByClientNotNullAndEmployeeNotNull(any(Pageable.class)))
                .thenReturn(orderPage);
        for (int i = 0; i < orders.size(); i++) {
            when(orderMapper.toDto(orders.get(i))).thenReturn(orderDTOs.get(i));
        }

        // Act
        orderService.getAllOrders(pageable);

        // Assert
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(orderRepository).findAllByClientNotNullAndEmployeeNotNull(pageableCaptor.capture());

        Pageable capturedPageable = pageableCaptor.getValue();
        Sort.Order priceOrder = capturedPageable.getSort().getOrderFor("price");
        assertNotNull(priceOrder);
        assertEquals(Sort.Direction.DESC, priceOrder.getDirection());
        assertEquals("price", priceOrder.getProperty());
    }

    @Test
    void getAllOrders_WithMultipleSort_ShouldPassCorrectSortToRepository() {
        // Arrange - Sort by orderDate ASC, then by price DESC
        Sort multiSort = Sort.by("orderDate").ascending().and(Sort.by("price").descending());
        Pageable pageable = PageRequest.of(0, 10, multiSort);
        Page<Order> orderPage = new PageImpl<>(orders, pageable, orders.size());

        when(orderRepository.findAllByClientNotNullAndEmployeeNotNull(any(Pageable.class)))
                .thenReturn(orderPage);
        for (int i = 0; i < orders.size(); i++) {
            when(orderMapper.toDto(orders.get(i))).thenReturn(orderDTOs.get(i));
        }

        // Act
        orderService.getAllOrders(pageable);

        // Assert
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(orderRepository).findAllByClientNotNullAndEmployeeNotNull(pageableCaptor.capture());

        Pageable capturedPageable = pageableCaptor.getValue();
        Sort capturedSort = capturedPageable.getSort();

        List<Sort.Order> sortOrders = capturedSort.toList();
        assertEquals(2, sortOrders.size());

        // First sort: orderDate ASC
        Sort.Order orderDateOrder = sortOrders.get(0);
        assertEquals("orderDate", orderDateOrder.getProperty());
        assertEquals(Sort.Direction.ASC, orderDateOrder.getDirection());

        // Second sort: price DESC
        Sort.Order priceOrder = sortOrders.get(1);
        assertEquals("price", priceOrder.getProperty());
        assertEquals(Sort.Direction.DESC, priceOrder.getDirection());
    }

    @Test
    void getOrdersByClient_ShouldReturnListOfOrderDTOs() {
        // Arrange
        String clientEmail = client.getEmail();
        List<Order> clientOrders = List.of(order);

        when(orderRepository.findAllByClient_Email(clientEmail)).thenReturn(clientOrders);
        when(orderMapper.toDto(order)).thenReturn(testOrderDTO);

        // Act
        List<OrderDTO> result = orderService.getOrdersByClient(clientEmail);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testOrderDTO, result.get(0));
        verify(orderRepository).findAllByClient_Email(clientEmail);
        verify(orderMapper).toDto(order);
    }

    @Test
    void getOrdersByEmployee_ShouldReturnListOfOrderDTOs() {
        // Arrange
        String employeeEmail = employee.getEmail();
        List<Order> employeeOrders = List.of(order);

        when(orderRepository.findAllByEmployee_Email(employeeEmail)).thenReturn(employeeOrders);
        when(orderMapper.toDto(order)).thenReturn(testOrderDTO);

        // Act
        List<OrderDTO> result = orderService.getOrdersByEmployee(employeeEmail);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testOrderDTO, result.get(0));
        verify(orderRepository).findAllByEmployee_Email(employeeEmail);
        verify(orderMapper).toDto(order);
    }

    @Test
    void addOrder_WithValidData_ShouldReturnOrderDTO_Fixed() {
        // Given
        Order mappedOrder = new Order();
        mappedOrder.setOrderDate(testOrderDTO.getOrderDate());
        mappedOrder.setPrice(testOrderDTO.getPrice());

        when(orderMapper.toEntity(testOrderDTO)).thenReturn(mappedOrder);
        when(employeeRepository.getByEmail(testOrderDTO.getEmployeeEmail()))
                .thenReturn(Optional.of(employee));
        when(clientRepository.getByEmail(testOrderDTO.getClientEmail()))
                .thenReturn(Optional.of(client));
        for (BookItemDTO bookItemDTO : testOrderDTO.getBookItems()) {
            when(bookRepository.findByName(bookItemDTO.getBookName()))
                    .thenReturn(Optional.of(book));
            when(bookItemMapper.toEntity(bookItemDTO)).thenReturn(new BookItem());
        }
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toDto(order)).thenReturn(testOrderDTO);

        // When
        OrderDTO result = orderService.addOrder(testOrderDTO);

        // Then
        assertNotNull(result);
        assertEquals(testOrderDTO, result);
    }

    @Test
    void addOrder_WithNullClientEmail_ShouldThrowOrderMustContainClientException() {
        // Arrange
        testOrderDTO.setClientEmail(null);

        // Act & Assert
        OrderMustContainClientException exception = assertThrows(OrderMustContainClientException.class,
                () -> orderService.addOrder(testOrderDTO));
        assertTrue(exception.getMessage().contains("The client's email within order cannot be null!"));
        verify(orderMapper, never()).toEntity(any());
        verify(employeeRepository, never()).getByEmail(testOrderDTO.getEmployeeEmail());
        verify(clientRepository, never()).getByEmail(anyString());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void addOrder_WithNonExistentEmployee_ShouldThrowNotFoundException() {
        // Arrange
        when(orderMapper.toEntity(testOrderDTO)).thenReturn(new Order());
        when(employeeRepository.getByEmail(testOrderDTO.getEmployeeEmail()))
                .thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> orderService.addOrder(testOrderDTO));
        assertTrue(exception.getMessage().contains("Employee with email " + testOrderDTO.getEmployeeEmail()));
        verify(employeeRepository).getByEmail(testOrderDTO.getEmployeeEmail());
        verify(clientRepository, never()).getByEmail(anyString());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void addOrder_WithNonExistentClient_ShouldThrowNotFoundException() {
        // Arrange
        when(orderMapper.toEntity(testOrderDTO)).thenReturn(new Order());
        when(employeeRepository.getByEmail(testOrderDTO.getEmployeeEmail()))
                .thenReturn(Optional.of(employee));
        when(clientRepository.getByEmail(testOrderDTO.getClientEmail()))
                .thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> orderService.addOrder(testOrderDTO));
        assertTrue(exception.getMessage().contains("Client with email " + testOrderDTO.getClientEmail()));
        verify(employeeRepository).getByEmail(testOrderDTO.getEmployeeEmail());
        verify(clientRepository).getByEmail(testOrderDTO.getClientEmail());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void addOrder_WithNonExistentBook_ShouldThrowNotFoundException() {
        // Arrange
        Order mappedOrder = new Order();
        when(orderMapper.toEntity(testOrderDTO)).thenReturn(mappedOrder);
        when(employeeRepository.getByEmail(testOrderDTO.getEmployeeEmail()))
                .thenReturn(Optional.of(employee));
        when(clientRepository.getByEmail(testOrderDTO.getClientEmail()))
                .thenReturn(Optional.of(client));
        when(bookRepository.findByName(bookItemDTO.getBookName()))
                .thenReturn(Optional.empty());
        when(bookItemMapper.toEntity(bookItemDTO)).thenReturn(new BookItem());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> orderService.addOrder(testOrderDTO));
        assertTrue(exception.getMessage().contains("Book with name " + bookItemDTO.getBookName()));
        verify(bookRepository).findByName(bookItemDTO.getBookName());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void confirmOrder_WithValidData_ShouldUpdateOrderWithEmployee() {
        // Arrange
        when(orderRepository.findDistinctByClient_EmailAndOrderDate(
                testOrderDTO.getClientEmail(), testOrderDTO.getOrderDate()))
                .thenReturn(order);
        when(employeeRepository.getByEmail(testOrderDTO.getEmployeeEmail()))
                .thenReturn(Optional.of(employee));
        when(orderRepository.save(order)).thenReturn(order);

        // Act
        orderService.confirmOrder(testOrderDTO);

        // Assert
        verify(orderRepository).findDistinctByClient_EmailAndOrderDate(
                testOrderDTO.getClientEmail(), testOrderDTO.getOrderDate());
        verify(employeeRepository).getByEmail(testOrderDTO.getEmployeeEmail());
        verify(orderRepository).save(order);
        assertEquals(employee, order.getEmployee());
    }

    @Test
    void confirmOrder_WithNonExistentEmployee_ShouldThrowNotFoundException() {
        // Arrange
        when(orderRepository.findDistinctByClient_EmailAndOrderDate(
                testOrderDTO.getClientEmail(), testOrderDTO.getOrderDate()))
                .thenReturn(order);
        when(employeeRepository.getByEmail(testOrderDTO.getEmployeeEmail()))
                .thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> orderService.confirmOrder(testOrderDTO));
        assertTrue(exception.getMessage().contains("Employee with email " + testOrderDTO.getEmployeeEmail()));
        verify(orderRepository).findDistinctByClient_EmailAndOrderDate(
                testOrderDTO.getClientEmail(), testOrderDTO.getOrderDate());
        verify(employeeRepository).getByEmail(testOrderDTO.getEmployeeEmail());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void addOrder_WithMultipleBookItems_ShouldMapAllBookItems() {
        // Arrange
        List<BookItemDTO> bookItemDTOs = getBookItemDTOs();
        List<Book> books = getBookEntities();

        testOrderDTO.setBookItems(bookItemDTOs);

        when(orderMapper.toEntity(testOrderDTO)).thenReturn(order);
        when(employeeRepository.getByEmail(testOrderDTO.getEmployeeEmail()))
                .thenReturn(Optional.of(employee));
        when(clientRepository.getByEmail(testOrderDTO.getClientEmail()))
                .thenReturn(Optional.of(client));
        for (int i = 0; i < bookItemDTOs.size(); i++) {
            when(bookRepository.findByName(bookItemDTOs.get(i).getBookName()))
                    .thenReturn(Optional.of(books.get(i)));
        }

        when(bookItemMapper.toEntity(any(BookItemDTO.class))).thenReturn(new BookItem());
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toDto(order)).thenReturn(testOrderDTO);

        // Act
        OrderDTO result = orderService.addOrder(testOrderDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testOrderDTO, result);
        for (BookItemDTO bookItemDTO : bookItemDTOs) {
            verify(bookRepository).findByName(bookItemDTO.getBookName());
        }
        verify(bookItemMapper, times(bookItemDTOs.size())).toEntity(any(BookItemDTO.class));
        verify(orderRepository).save(any(Order.class));
    }
}