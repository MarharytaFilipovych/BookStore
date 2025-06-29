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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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

    @Mock private OrderRepository orderRepository;
    @Mock private OrderMapper orderMapper;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private ClientRepository clientRepository;
    @Mock private BookItemMapper bookItemMapper;
    @Mock private BookRepository bookRepository;
    @Mock private SortMappingService sortMappingService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order order;
    private OrderDTO testOrderDTO;
    private Employee employee;
    private Client client;
    private BookItemDTO bookItemDTO;
    private List<Order> orders;
    private List<OrderDTO> orderDTOs;
    private List<Book> books;
    private List<BookItemDTO> bookItemDTOs;

    @BeforeEach
    void setUp() {
        employee = getEmployeeEntity();
        client = getClientEntity();
        order = getOrderEntity();
        bookItemDTO = getBookItemDTO();
        testOrderDTO = getOrderDTO();
        orders = getOrderEntities();
        orderDTOs = getOrderDTOs();
        books = getBookEntities();
        bookItemDTOs = getBookItemDTOs();
    }

    private void mockPageableOrderOperations(Pageable pageable, Pageable mappedPageable,
                                             List<Order> orderList, List<OrderDTO> orderDTOList) {
        Page<Order> orderPage = new PageImpl<>(orderList, mappedPageable, orderList.size());
        when(sortMappingService.applyMappings(pageable, "order")).thenReturn(mappedPageable);
        when(orderRepository.findAll(mappedPageable)).thenReturn(orderPage);
        for (int i = 0; i < orderList.size(); i++) {
            when(orderMapper.toDto(orderList.get(i))).thenReturn(orderDTOList.get(i));
        }
    }

    private void mockEmptyPageableRepository(Pageable pageable, Pageable mappedPageable) {
        Page<Order> emptyPage = new PageImpl<>(List.of(), mappedPageable, 0);
        when(sortMappingService.applyMappings(pageable, "order")).thenReturn(mappedPageable);
        when(orderRepository.findAll(mappedPageable)).thenReturn(emptyPage);
    }

    private void mockClientOrderOperations(String clientEmail, Pageable pageable, Pageable mappedPageable,
                                           List<Order> clientOrders, List<OrderDTO> expectedDTOs) {

        if (pageable != null) {
            when(sortMappingService.applyMappings(pageable, "order")).thenReturn(mappedPageable);
            Page<Order> orderPage = new PageImpl<>(clientOrders, mappedPageable, clientOrders.size());
            when(orderRepository.findAllByClient_Email(clientEmail, mappedPageable)).thenReturn(orderPage);
        } else {
            when(orderRepository.findAllByClient_Email(clientEmail)).thenReturn(clientOrders);
        }
        for (int i = 0; i < clientOrders.size(); i++) {
            when(orderMapper.toDto(clientOrders.get(i))).thenReturn(expectedDTOs.get(i));
        }
    }

    private void mockEmployeeOrderOperations(String employeeEmail, Pageable pageable, Pageable mappedPageable,
                                             List<Order> employeeOrders, List<OrderDTO> expectedDTOs) {
        if (pageable != null) {
            when(sortMappingService.applyMappings(pageable, "order")).thenReturn(mappedPageable);
            Page<Order> orderPage = new PageImpl<>(employeeOrders, mappedPageable, employeeOrders.size());
            when(orderRepository.findAllByEmployee_Email(employeeEmail, mappedPageable)).thenReturn(orderPage);
        } else {
            when(orderRepository.findAllByEmployee_Email(employeeEmail)).thenReturn(employeeOrders);
        }
        for (int i = 0; i < employeeOrders.size(); i++) {
            when(orderMapper.toDto(employeeOrders.get(i))).thenReturn(expectedDTOs.get(i));
        }
    }

    private void mockSuccessfulOrderCreation(OrderDTO dto, Order mappedOrder, Order savedOrder) {
        when(orderMapper.toEntity(dto)).thenReturn(mappedOrder);
        when(clientRepository.getByEmail(dto.getClientEmail())).thenReturn(Optional.of(client));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(orderMapper.toDto(savedOrder)).thenReturn(dto);
    }

    private void mockBookItemOperations(List<BookItemDTO> itemDTOs, List<Book> booksToReturn) {
        for (int i = 0; i < itemDTOs.size() && i < booksToReturn.size();  i++) {
            when(bookRepository.findByName(itemDTOs.get(i).getBookName()))
                    .thenReturn(Optional.of(booksToReturn.get(i)));
            when(bookItemMapper.toEntity(itemDTOs.get(i))).thenReturn(new BookItem());
        }
    }

    private void mockOrderConfirmation(OrderDTO dto, Order orderToConfirm) {
        when(orderRepository.findDistinctByClient_EmailAndOrderDate(dto.getClientEmail(), dto.getOrderDate()))
                .thenReturn(orderToConfirm);
        when(employeeRepository.getByEmail(dto.getEmployeeEmail())).thenReturn(Optional.of(employee));
        when(orderRepository.save(orderToConfirm)).thenReturn(orderToConfirm);
    }

    private void mockEmptyOrderResults(String email, boolean isClient) {
        if (isClient) {
            when(orderRepository.findAllByClient_Email(email)).thenReturn(List.of());
        } else {
            when(orderRepository.findAllByEmployee_Email(email)).thenReturn(List.of());
        }
    }

    private void verifyPageableOperations(Pageable originalPageable, Pageable mappedPageable) {
        verify(sortMappingService).applyMappings(originalPageable, "order");
        verify(orderRepository).findAll(mappedPageable);
    }

    private void verifyPagedResults(Page<OrderDTO> result, List<OrderDTO> expectedContent, int expectedTotalElements) {
        assertNotNull(result);
        assertEquals(expectedTotalElements, result.getTotalElements());
        assertEquals(expectedContent, result.getContent());
    }

    private void verifyListResults(List<OrderDTO> result, List<OrderDTO> expectedContent) {
        assertNotNull(result);
        assertEquals(expectedContent.size(), result.size());
        assertEquals(expectedContent, result);
    }

    private void verifyEmptyResults(Page<OrderDTO> result) {
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    private void verifyEmptyListResults(List<OrderDTO> result) {
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    private void verifyOrderCreationOperations(OrderDTO dto) {
        verify(orderMapper).toEntity(dto);
        verify(orderRepository).save(any(Order.class));
        verify(orderMapper).toDto(any(Order.class));
    }

    private void verifyOrderConfirmationOperations(OrderDTO dto, Order orderToConfirm) {
        verify(orderRepository).findDistinctByClient_EmailAndOrderDate(dto.getClientEmail(), dto.getOrderDate());
        verify(employeeRepository).getByEmail(dto.getEmployeeEmail());
        verify(orderRepository).save(orderToConfirm);
        assertEquals(employee, orderToConfirm.getEmployee());
    }

    private void verifyClientOrderOperations(String clientEmail, Pageable pageable) {
        if (pageable != null) {
            verify(sortMappingService).applyMappings(pageable, "order");
            verify(orderRepository).findAllByClient_Email(eq(clientEmail), any(Pageable.class));
        } else verify(orderRepository).findAllByClient_Email(clientEmail);
    }

    private void verifyEmployeeOrderOperations(String employeeEmail, Pageable pageable) {
        if (pageable != null) {
            verify(sortMappingService).applyMappings(pageable, "order");
            verify(orderRepository).findAllByEmployee_Email(eq(employeeEmail), any(Pageable.class));
        } else verify(orderRepository).findAllByEmployee_Email(employeeEmail);
    }

    private void verifyBookItemMappingOperations(List<BookItemDTO> itemDTOs) {
        for (BookItemDTO itemDTO : itemDTOs) {
            verify(bookRepository).findByName(itemDTO.getBookName());
        }
        verify(bookItemMapper, times(itemDTOs.size())).toEntity(any(BookItemDTO.class));
    }

    private void verifyNotFoundScenario(Class<? extends Exception> exceptionType, String expectedMessageFragment,
                                        Runnable serviceCall) {
        Exception exception = assertThrows(exceptionType, serviceCall::run);

        assertTrue(exception.getMessage().contains(expectedMessageFragment));
    }

    private void testSortingScenario(Sort sort) {
        Pageable pageable = PageRequest.of(0, 10, sort);
        Pageable mappedPageable = PageRequest.of(0, 10, sort);
        mockPageableOrderOperations(pageable, mappedPageable, orders, orderDTOs);

        // Act
        orderService.getAllOrders(pageable);

        // Assert
        verifyPageableOperations(pageable, mappedPageable);
    }

    @Test
    void getAllOrders_WithValidPageable_ShouldReturnPageOfOrderDTOs() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Pageable mappedPageable = PageRequest.of(0, 10, Sort.by("orderDate"));
        mockPageableOrderOperations(pageable, mappedPageable, orders, orderDTOs);

        // Act
        Page<OrderDTO> result = orderService.getAllOrders(pageable);

        // Assert
        verifyPagedResults(result, orderDTOs, orders.size());
        verifyPageableOperations(pageable, mappedPageable);
        verify(orderMapper, times(orders.size())).toDto(any(Order.class));
    }

    @Test
    void getAllOrders_WithSortByOrderDateAsc_ShouldPassCorrectSortToRepository() {
        testSortingScenario(Sort.by("orderDate").ascending());
    }

    @Test
    void getAllOrders_WithSortByPriceDesc_ShouldPassCorrectSortToRepository() {
        testSortingScenario(Sort.by("price").descending());
    }

    @Test
    void getAllOrders_WithMultipleSort_ShouldPassCorrectSortToRepository() {
        Sort multiSort = Sort.by("orderDate").ascending().and(Sort.by("price").descending());
        testSortingScenario(multiSort);
    }

    @Test
    void getAllOrders_WithEmptyRepository_ShouldReturnEmptyPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Pageable mappedPageable = PageRequest.of(0, 10, Sort.by("orderDate"));
        mockEmptyPageableRepository(pageable, mappedPageable);

        // Act
        Page<OrderDTO> result = orderService.getAllOrders(pageable);

        // Assert
        verifyEmptyResults(result);
        verify(orderMapper, never()).toDto(any());
    }

    @Test
    void getOrdersByClient_WithoutPageable_ShouldReturnListOfOrderDTOs() {
        // Arrange
        String clientEmail = client.getEmail();
        List<Order> clientOrders = List.of(order);
        mockClientOrderOperations(clientEmail, null, null, clientOrders, List.of(testOrderDTO));

        // Act
        List<OrderDTO> result = orderService.getOrdersByClient(clientEmail);

        // Assert
        verifyListResults(result, List.of(testOrderDTO));
        verifyClientOrderOperations(clientEmail, null);
        verify(orderMapper).toDto(order);
    }

    @Test
    void getOrdersByClient_WithPageable_ShouldReturnPageOfOrderDTOs() {
        // Arrange
        String clientEmail = client.getEmail();
        Pageable pageable = PageRequest.of(0, 10);
        Pageable mappedPageable = PageRequest.of(0, 10, Sort.by("orderDate"));
        List<Order> clientOrders = List.of(order);
        mockClientOrderOperations(clientEmail, pageable, mappedPageable, clientOrders, List.of(testOrderDTO));

        // Act
        Page<OrderDTO> result = orderService.getOrdersByClient(clientEmail, pageable);

        // Assert
        verifyPagedResults(result, List.of(testOrderDTO), 1);
        verifyClientOrderOperations(clientEmail, pageable);
        verify(orderMapper).toDto(order);
    }

    @ParameterizedTest
    @ValueSource(strings = {"nonexistent@example.com", "missing@client.com", "invalid@test.com"})
    void getOrdersByClient_WithEmptyResults_ShouldReturnEmptyList(String clientEmail) {
        // Arrange
        mockEmptyOrderResults(clientEmail, true);

        // Act
        List<OrderDTO> result = orderService.getOrdersByClient(clientEmail);

        // Assert
        verifyEmptyListResults(result);
        verify(orderRepository).findAllByClient_Email(clientEmail);
        verify(orderMapper, never()).toDto(any());
    }

    @Test
    void getOrdersByEmployee_WithoutPageable_ShouldReturnListOfOrderDTOs() {
        // Arrange
        String employeeEmail = employee.getEmail();
        List<Order> employeeOrders = List.of(order);
        mockEmployeeOrderOperations(employeeEmail, null, null, employeeOrders, List.of(testOrderDTO));

        // Act
        List<OrderDTO> result = orderService.getOrdersByEmployee(employeeEmail);

        // Assert
        verifyListResults(result, List.of(testOrderDTO));
        verifyEmployeeOrderOperations(employeeEmail, null);
        verify(orderMapper).toDto(order);
    }

    @Test
    void getOrdersByEmployee_WithPageable_ShouldReturnPageOfOrderDTOs() {
        // Arrange
        String employeeEmail = employee.getEmail();
        Pageable pageable = PageRequest.of(0, 10);
        Pageable mappedPageable = PageRequest.of(0, 10, Sort.by("orderDate"));
        List<Order> employeeOrders = List.of(order);
        mockEmployeeOrderOperations(employeeEmail, pageable, mappedPageable, employeeOrders, List.of(testOrderDTO));

        // Act
        Page<OrderDTO> result = orderService.getOrdersByEmployee(employeeEmail, pageable);

        // Assert
        verifyPagedResults(result, List.of(testOrderDTO), 1);
        verifyEmployeeOrderOperations(employeeEmail, pageable);
        verify(orderMapper).toDto(order);
    }

    @ParameterizedTest
    @ValueSource(strings = {"nonexistent@example.com", "missing@employee.com", "invalid@test.com"})
    void getOrdersByEmployee_WithEmptyResults_ShouldReturnEmptyList(String employeeEmail) {
        // Arrange
        mockEmptyOrderResults(employeeEmail, false);

        // Act
        List<OrderDTO> result = orderService.getOrdersByEmployee(employeeEmail);

        // Assert
        verifyEmptyListResults(result);
        verify(orderRepository).findAllByEmployee_Email(employeeEmail);
        verify(orderMapper, never()).toDto(any());
    }

    @Test
    void addOrder_WithValidData_ShouldReturnOrderDTO() {
        // Arrange
        Order mappedOrder = new Order();
        mappedOrder.setOrderDate(testOrderDTO.getOrderDate());
        mappedOrder.setPrice(testOrderDTO.getPrice());
        mockSuccessfulOrderCreation(testOrderDTO, mappedOrder, order);
        mockBookItemOperations(testOrderDTO.getBookItems(), books);

        // Act
        OrderDTO result = orderService.addOrder(testOrderDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testOrderDTO, result);
        verifyOrderCreationOperations(testOrderDTO);
    }

    @Test
    void addOrder_WithNullClientEmail_ShouldThrowOrderMustContainClientException() {
        // Arrange
        testOrderDTO.setClientEmail(null);

        // Act & Assert
        verifyNotFoundScenario(OrderMustContainClientException.class,
                "The client's email within order cannot be null!",
                () -> orderService.addOrder(testOrderDTO));

        verify(orderMapper, never()).toEntity(any());
        verify(employeeRepository, never()).getByEmail(any());
        verify(clientRepository, never()).getByEmail(any());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void addOrder_WithNonExistentClient_ShouldThrowNotFoundException() {
        // Arrange
        when(orderMapper.toEntity(testOrderDTO)).thenReturn(new Order());

        // Act & Assert
        verifyNotFoundScenario(NotFoundException.class,
                "Client with email " + testOrderDTO.getClientEmail(),
                () -> orderService.addOrder(testOrderDTO));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void addOrder_WithNonExistentBook_ShouldThrowNotFoundException() {
        // Arrange
        Order mappedOrder = new Order();
        when(orderMapper.toEntity(testOrderDTO)).thenReturn(mappedOrder);
        when(clientRepository.getByEmail(client.getEmail())).thenReturn(Optional.of(client));
        when(bookRepository.findByName(bookItemDTO.getBookName())).thenReturn(Optional.empty());
        when(bookItemMapper.toEntity(bookItemDTO)).thenReturn(new BookItem());

        // Act & Assert
        verifyNotFoundScenario(NotFoundException.class,
                "Book with name " + bookItemDTO.getBookName(),
                () -> orderService.addOrder(testOrderDTO));

        verify(bookRepository).findByName(bookItemDTO.getBookName());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"empty", "null"})
    void addOrder_WithEmptyOrNullBookItems_ShouldStillCreateOrder(String bookItemsType) {
        // Arrange
        if ("empty".equals(bookItemsType)) {
            testOrderDTO.setBookItems(new ArrayList<>());
        } else {
            testOrderDTO.setBookItems(null);
        }

        Order mappedOrder = new Order();
        mappedOrder.setOrderDate(testOrderDTO.getOrderDate());
        mappedOrder.setPrice(testOrderDTO.getPrice());
        mockSuccessfulOrderCreation(testOrderDTO, mappedOrder, order);

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
    void addOrder_WithMultipleBookItems_ShouldMapAllBookItems() {
        // Arrange
        testOrderDTO.setBookItems(bookItemDTOs);
        mockSuccessfulOrderCreation(testOrderDTO, order, order);
        mockBookItemOperations(bookItemDTOs, books);

        // Act
        OrderDTO result = orderService.addOrder(testOrderDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testOrderDTO, result);
        verifyBookItemMappingOperations(bookItemDTOs);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void confirmOrder_WithValidData_ShouldUpdateOrderWithEmployee() {
        // Arrange
        mockOrderConfirmation(testOrderDTO, order);

        // Act
        orderService.confirmOrder(testOrderDTO);

        // Assert
        verifyOrderConfirmationOperations(testOrderDTO, order);
    }

    @Test
    void confirmOrder_WithNonExistentEmployee_ShouldThrowNotFoundException() {
        // Arrange
        when(orderRepository.findDistinctByClient_EmailAndOrderDate(
                testOrderDTO.getClientEmail(), testOrderDTO.getOrderDate())).thenReturn(order);
        when(employeeRepository.getByEmail(testOrderDTO.getEmployeeEmail())).thenReturn(Optional.empty());

        // Act & Assert
        verifyNotFoundScenario(NotFoundException.class,
                "Employee with email " + testOrderDTO.getEmployeeEmail(),
                () -> orderService.confirmOrder(testOrderDTO));

        verify(orderRepository).findDistinctByClient_EmailAndOrderDate(
                testOrderDTO.getClientEmail(), testOrderDTO.getOrderDate());
        verify(employeeRepository).getByEmail(testOrderDTO.getEmployeeEmail());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void confirmOrder_WithNullOrder_ShouldThrowNullPointerException() {
        // Arrange
        when(orderRepository.findDistinctByClient_EmailAndOrderDate(
                testOrderDTO.getClientEmail(), testOrderDTO.getOrderDate())).thenReturn(null);

        // Act & Assert
        assertThrows(NotFoundException.class, () -> orderService.confirmOrder(testOrderDTO));

        verify(orderRepository).findDistinctByClient_EmailAndOrderDate(
                testOrderDTO.getClientEmail(), testOrderDTO.getOrderDate());
        verify(employeeRepository).getByEmail(any());
        verify(orderRepository, never()).save(any(Order.class));
    }
}