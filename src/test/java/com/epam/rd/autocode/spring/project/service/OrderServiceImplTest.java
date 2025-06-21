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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.ArrayList;
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

    @Mock
    private SortMappingService sortMappingService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order order;
    private OrderDTO testOrderDTO;
    private Employee employee;
    private Client client;
    private Book book;
    private BookItem bookItem;
    private BookItemDTO bookItemDTO;
    private List<Order> orders;
    private List<OrderDTO> orderDTOs;

    @BeforeEach
    void setUp() {
        employee = getEmployeeEntity();
        client = getClientEntity();
        book = getBookEntity();
        bookItem = getBookItemEntity();
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
        Pageable mappedPageable = PageRequest.of(0, 10, Sort.by("orderDate"));
        Page<Order> orderPage = new PageImpl<>(orders, mappedPageable, orders.size());

        when(sortMappingService.applyMappings(pageable, "order")).thenReturn(mappedPageable);
        when(orderRepository.findAllByClientNotNullAndEmployeeNotNull(mappedPageable))
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
        verify(sortMappingService).applyMappings(pageable, "order");
        verify(orderRepository).findAllByClientNotNullAndEmployeeNotNull(mappedPageable);
        verify(orderMapper, times(orders.size())).toDto(any(Order.class));
    }

    @Test
    void getAllOrders_WithSortByOrderDateAsc_ShouldPassCorrectSortToRepository() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10, Sort.by("orderDate").ascending());
        Pageable mappedPageable = PageRequest.of(0, 10, Sort.by("orderDate").ascending());
        Page<Order> orderPage = new PageImpl<>(orders, mappedPageable, orders.size());

        when(sortMappingService.applyMappings(pageable, "order")).thenReturn(mappedPageable);
        when(orderRepository.findAllByClientNotNullAndEmployeeNotNull(mappedPageable))
                .thenReturn(orderPage);
        for (int i = 0; i < orders.size(); i++) {
            when(orderMapper.toDto(orders.get(i))).thenReturn(orderDTOs.get(i));
        }

        // Act
        Page<OrderDTO> result = orderService.getAllOrders(pageable);

        // Assert
        assertNotNull(result);
        verify(sortMappingService).applyMappings(pageable, "order");
        verify(orderRepository).findAllByClientNotNullAndEmployeeNotNull(mappedPageable);
    }

    @Test
    void getAllOrders_WithSortByPriceDesc_ShouldPassCorrectSortToRepository() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10, Sort.by("price").descending());
        Pageable mappedPageable = PageRequest.of(0, 10, Sort.by("price").descending());
        Page<Order> orderPage = new PageImpl<>(orders, mappedPageable, orders.size());

        when(sortMappingService.applyMappings(pageable, "order")).thenReturn(mappedPageable);
        when(orderRepository.findAllByClientNotNullAndEmployeeNotNull(mappedPageable))
                .thenReturn(orderPage);
        for (int i = 0; i < orders.size(); i++) {
            when(orderMapper.toDto(orders.get(i))).thenReturn(orderDTOs.get(i));
        }

        // Act
        orderService.getAllOrders(pageable);

        // Assert
        verify(sortMappingService).applyMappings(pageable, "order");
        verify(orderRepository).findAllByClientNotNullAndEmployeeNotNull(mappedPageable);
    }

    @Test
    void getAllOrders_WithMultipleSort_ShouldPassCorrectSortToRepository() {
        // Arrange
        Sort multiSort = Sort.by("orderDate").ascending().and(Sort.by("price").descending());
        Pageable pageable = PageRequest.of(0, 10, multiSort);
        Pageable mappedPageable = PageRequest.of(0, 10, multiSort);
        Page<Order> orderPage = new PageImpl<>(orders, mappedPageable, orders.size());

        when(sortMappingService.applyMappings(pageable, "order")).thenReturn(mappedPageable);
        when(orderRepository.findAllByClientNotNullAndEmployeeNotNull(mappedPageable))
                .thenReturn(orderPage);
        for (int i = 0; i < orders.size(); i++) {
            when(orderMapper.toDto(orders.get(i))).thenReturn(orderDTOs.get(i));
        }

        // Act
        orderService.getAllOrders(pageable);

        // Assert
        verify(sortMappingService).applyMappings(pageable, "order");
        verify(orderRepository).findAllByClientNotNullAndEmployeeNotNull(mappedPageable);
    }

    @Test
    void getOrdersByClient_WithoutPageable_ShouldReturnListOfOrderDTOs() {
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
    void getOrdersByClient_WithPageable_ShouldReturnPageOfOrderDTOs() {
        // Arrange
        String clientEmail = client.getEmail();
        Pageable pageable = PageRequest.of(0, 10);
        Pageable mappedPageable = PageRequest.of(0, 10, Sort.by("orderDate"));
        List<Order> clientOrders = List.of(order);
        Page<Order> orderPage = new PageImpl<>(clientOrders, mappedPageable, 1);

        when(sortMappingService.applyMappings(pageable, "order")).thenReturn(mappedPageable);
        when(orderRepository.findAllByClient_Email(clientEmail, mappedPageable)).thenReturn(orderPage);
        when(orderMapper.toDto(order)).thenReturn(testOrderDTO);

        // Act
        Page<OrderDTO> result = orderService.getOrdersByClient(clientEmail, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testOrderDTO, result.getContent().get(0));
        verify(sortMappingService).applyMappings(pageable, "order");
        verify(orderRepository).findAllByClient_Email(clientEmail, mappedPageable);
        verify(orderMapper).toDto(order);
    }

    @Test
    void getOrdersByEmployee_WithoutPageable_ShouldReturnListOfOrderDTOs() {
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
    void getOrdersByEmployee_WithPageable_ShouldReturnPageOfOrderDTOs() {
        // Arrange
        String employeeEmail = employee.getEmail();
        Pageable pageable = PageRequest.of(0, 10);
        Pageable mappedPageable = PageRequest.of(0, 10, Sort.by("orderDate"));
        List<Order> employeeOrders = List.of(order);
        Page<Order> orderPage = new PageImpl<>(employeeOrders, mappedPageable, 1);

        when(sortMappingService.applyMappings(pageable, "order")).thenReturn(mappedPageable);
        when(orderRepository.findAllByEmployee_Email(employeeEmail, mappedPageable)).thenReturn(orderPage);
        when(orderMapper.toDto(order)).thenReturn(testOrderDTO);

        // Act
        Page<OrderDTO> result = orderService.getOrdersByEmployee(employeeEmail, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testOrderDTO, result.getContent().get(0));
        verify(sortMappingService).applyMappings(pageable, "order");
        verify(orderRepository).findAllByEmployee_Email(employeeEmail, mappedPageable);
        verify(orderMapper).toDto(order);
    }

    @Test
    void addOrder_WithValidData_ShouldReturnOrderDTO() {
        // Arrange
        Order mappedOrder = new Order();
        mappedOrder.setOrderDate(testOrderDTO.getOrderDate());
        mappedOrder.setPrice(testOrderDTO.getPrice());

        when(orderMapper.toEntity(testOrderDTO)).thenReturn(mappedOrder);
        when(employeeRepository.getByEmail(testOrderDTO.getEmployeeEmail()))
                .thenReturn(Optional.of(employee));
        when(clientRepository.getByEmail(testOrderDTO.getClientEmail()))
                .thenReturn(Optional.of(client));

        // Mock book items
        for (BookItemDTO itemDTO : testOrderDTO.getBookItems()) {
            when(bookRepository.findByName(itemDTO.getBookName()))
                    .thenReturn(Optional.of(book));
            when(bookItemMapper.toEntity(itemDTO)).thenReturn(bookItem);
        }

        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toDto(order)).thenReturn(testOrderDTO);

        // Act
        OrderDTO result = orderService.addOrder(testOrderDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testOrderDTO, result);
        verify(orderMapper).toEntity(testOrderDTO);
        verify(employeeRepository).getByEmail(testOrderDTO.getEmployeeEmail());
        verify(clientRepository).getByEmail(testOrderDTO.getClientEmail());
        verify(orderRepository).save(any(Order.class));
        verify(orderMapper).toDto(order);
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
        verify(employeeRepository, never()).getByEmail(any());
        verify(clientRepository, never()).getByEmail(any());
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
        verify(clientRepository, never()).getByEmail(any());
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
    void addOrder_WithEmptyBookItems_ShouldStillCreateOrder() {
        // Arrange
        testOrderDTO.setBookItems(new ArrayList<>());
        Order mappedOrder = new Order();
        mappedOrder.setOrderDate(testOrderDTO.getOrderDate());
        mappedOrder.setPrice(testOrderDTO.getPrice());

        when(orderMapper.toEntity(testOrderDTO)).thenReturn(mappedOrder);
        when(employeeRepository.getByEmail(testOrderDTO.getEmployeeEmail()))
                .thenReturn(Optional.of(employee));
        when(clientRepository.getByEmail(testOrderDTO.getClientEmail()))
                .thenReturn(Optional.of(client));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toDto(order)).thenReturn(testOrderDTO);

        // Act
        OrderDTO result = orderService.addOrder(testOrderDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testOrderDTO, result);
        verify(bookRepository, never()).findByName(any());
        verify(bookItemMapper, never()).toEntity(any());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void addOrder_WithNullBookItems_ShouldStillCreateOrder() {
        // Arrange
        testOrderDTO.setBookItems(null);
        Order mappedOrder = new Order();
        mappedOrder.setOrderDate(testOrderDTO.getOrderDate());
        mappedOrder.setPrice(testOrderDTO.getPrice());

        when(orderMapper.toEntity(testOrderDTO)).thenReturn(mappedOrder);
        when(employeeRepository.getByEmail(testOrderDTO.getEmployeeEmail()))
                .thenReturn(Optional.of(employee));
        when(clientRepository.getByEmail(testOrderDTO.getClientEmail()))
                .thenReturn(Optional.of(client));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toDto(order)).thenReturn(testOrderDTO);

        // Act
        OrderDTO result = orderService.addOrder(testOrderDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testOrderDTO, result);
        verify(bookRepository, never()).findByName(any());
        verify(bookItemMapper, never()).toEntity(any());
        verify(orderRepository).save(any(Order.class));
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
    void confirmOrder_WithNullOrder_ShouldThrowNullPointerException() {
        // Arrange
        when(orderRepository.findDistinctByClient_EmailAndOrderDate(
                testOrderDTO.getClientEmail(), testOrderDTO.getOrderDate()))
                .thenReturn(null);

        // Act & Assert
        assertThrows(NotFoundException.class,
                () -> orderService.confirmOrder(testOrderDTO));
        verify(orderRepository).findDistinctByClient_EmailAndOrderDate(
                testOrderDTO.getClientEmail(), testOrderDTO.getOrderDate());
        verify(employeeRepository).getByEmail(any());
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
            when(bookItemMapper.toEntity(bookItemDTOs.get(i))).thenReturn(new BookItem());
        }

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

    @Test
    void getAllOrders_WithEmptyRepository_ShouldReturnEmptyPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Pageable mappedPageable = PageRequest.of(0, 10, Sort.by("orderDate"));
        Page<Order> emptyPage = new PageImpl<>(List.of(), mappedPageable, 0);

        when(sortMappingService.applyMappings(pageable, "order")).thenReturn(mappedPageable);
        when(orderRepository.findAllByClientNotNullAndEmployeeNotNull(mappedPageable))
                .thenReturn(emptyPage);

        // Act
        Page<OrderDTO> result = orderService.getAllOrders(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
        verify(orderMapper, never()).toDto(any());
    }

    @Test
    void getOrdersByClient_WithEmptyResults_ShouldReturnEmptyList() {
        // Arrange
        String clientEmail = "nonexistent@example.com";
        when(orderRepository.findAllByClient_Email(clientEmail)).thenReturn(List.of());

        // Act
        List<OrderDTO> result = orderService.getOrdersByClient(clientEmail);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(orderRepository).findAllByClient_Email(clientEmail);
        verify(orderMapper, never()).toDto(any());
    }

    @Test
    void getOrdersByEmployee_WithEmptyResults_ShouldReturnEmptyList() {
        // Arrange
        String employeeEmail = "nonexistent@example.com";
        when(orderRepository.findAllByEmployee_Email(employeeEmail)).thenReturn(List.of());

        // Act
        List<OrderDTO> result = orderService.getOrdersByEmployee(employeeEmail);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(orderRepository).findAllByEmployee_Email(employeeEmail);
        verify(orderMapper, never()).toDto(any());
    }
}