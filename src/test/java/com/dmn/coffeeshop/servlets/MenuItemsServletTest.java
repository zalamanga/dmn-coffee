package com.dmn.coffeeshop.servlets;

import com.dmn.coffeeshop.model.MenuItem;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MenuItemsServletTest {
    private HttpServletRequest request;
    private HttpServletResponse response;
    private RequestDispatcher dispatcher;
    private MenuItemsServlet.ConnectionSource connectionSource;
    private MenuItemsServlet servlet;

    @BeforeEach
    void setUp() {
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        dispatcher = mock(RequestDispatcher.class);
        connectionSource = mock(MenuItemsServlet.ConnectionSource.class);
        servlet = new MenuItemsServlet(connectionSource);
        when(request.getContextPath()).thenReturn("/dmn-coffee");
        when(request.getRequestDispatcher("/WEB-INF/views/menu.jsp")).thenReturn(dispatcher);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "0", "-1", "1000", "1.5", "1e2", "+1", "table", "999999999999999999"})
    void invalidTableRedirectsBeforeOpeningDatabase(String value) throws Exception {
        when(request.getParameter("tableNumber")).thenReturn(value);

        servlet.doGet(request, response);

        verify(response).sendRedirect("/dmn-coffee/index.html?invalidTable=1");
        verify(connectionSource, never()).open();
        verify(request, never()).getRequestDispatcher(anyString());
    }

    @Test
    void groupsRowsInFirstSeenCategoryOrderAndKeepsDecimalPrices() throws Exception {
        when(request.getParameter("tableNumber")).thenReturn(" 007 ");
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet result = mock(ResultSet.class);
        when(connectionSource.open()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(result);
        when(result.next()).thenReturn(true, true, true, false);
        when(result.getInt("id")).thenReturn(1, 2, 3);
        when(result.getString("name")).thenReturn("Coffee A", "Snack B", "Coffee C");
        when(result.getString("category")).thenReturn("Coffees", "Snacks", "Coffees");
        when(result.getBigDecimal("price")).thenReturn(
                new BigDecimal("12500.25"), new BigDecimal("8000.00"), new BigDecimal("16000.50"));
        when(result.getString("image_path")).thenReturn("images/coffee.jpg", "images/nachos.jpg", "images/latte.jpg");

        servlet.doGet(request, response);

        Map<?, ?> groups = capturedGroups();
        assertInstanceOf(LinkedHashMap.class, groups);
        assertEquals(List.of("Coffees", "Snacks"), List.copyOf(groups.keySet()));
        List<?> coffees = (List<?>) groups.get("Coffees");
        assertEquals(2, coffees.size());
        MenuItem first = (MenuItem) coffees.get(0);
        MenuItem second = (MenuItem) coffees.get(1);
        assertEquals(1, first.getId());
        assertEquals("Coffee A", first.getName());
        assertEquals("images/coffee.jpg", first.getImagePath());
        assertEquals(new BigDecimal("12500.25"), first.getPrice());
        assertEquals(3, second.getId());
        assertEquals(new BigDecimal("16000.50"), second.getPrice());
        verify(request).setAttribute("tableNumber", 7);
        verify(request).setAttribute("menuError", false);
        verify(connection).prepareStatement(
                "SELECT id, name, category, price, image_path FROM menu_items ORDER BY id");
        verify(result).close();
        verify(statement).close();
        verify(connection).close();
        verify(dispatcher).forward(request, response);
    }

    @ParameterizedTest
    @ValueSource(strings = {"1", "999"})
    void validBoundaryTablesAllowAnEmptyMenuWithoutAnError(String value) throws Exception {
        when(request.getParameter("tableNumber")).thenReturn(value);
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet result = mock(ResultSet.class);
        when(connectionSource.open()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(result);
        when(result.next()).thenReturn(false);

        servlet.doGet(request, response);

        assertTrue(capturedGroups().isEmpty());
        verify(request).setAttribute("tableNumber", Integer.parseInt(value));
        verify(request).setAttribute("menuError", false);
        verify(response, never()).setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        verify(dispatcher).forward(request, response);
        verify(result).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void connectionFailureShowsUnavailableMenuWithoutWritingExceptionDetails() throws Exception {
        when(request.getParameter("tableNumber")).thenReturn("12");
        when(connectionSource.open()).thenThrow(new SQLException("synthetic connection failure"));

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        verify(request).setAttribute("tableNumber", 12);
        verify(request).setAttribute("menuError", true);
        assertTrue(capturedGroups().isEmpty());
        verify(dispatcher).forward(request, response);
        verify(response, never()).getWriter();
    }

    @Test
    void queryFailureClosesOpenedResourcesAndShowsUnavailableMenu() throws Exception {
        when(request.getParameter("tableNumber")).thenReturn("3");
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        when(connectionSource.open()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenThrow(new SQLException("synthetic query failure"));

        servlet.doGet(request, response);

        verify(statement).close();
        verify(connection).close();
        verify(response).setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        verify(request).setAttribute("menuError", true);
        assertTrue(capturedGroups().isEmpty());
        verify(dispatcher).forward(request, response);
    }

    private Map<?, ?> capturedGroups() {
        ArgumentCaptor<Object> value = ArgumentCaptor.forClass(Object.class);
        verify(request).setAttribute(eq("menuGroups"), value.capture());
        return assertInstanceOf(Map.class, value.getValue());
    }
}
