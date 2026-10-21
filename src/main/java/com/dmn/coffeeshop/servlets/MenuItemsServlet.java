package com.dmn.coffeeshop.servlets;

import DB.DBConnection;
import com.dmn.coffeeshop.model.MenuItem;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/MenuItemsServlet")
public class MenuItemsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(MenuItemsServlet.class.getName());
    private static final String MENU_QUERY =
            "SELECT id, name, category, price, image_path FROM menu_items ORDER BY id";
    private final ConnectionSource connectionSource;

    public MenuItemsServlet() {
        this(DBConnection::getConnection);
    }

    MenuItemsServlet(ConnectionSource connectionSource) {
        this.connectionSource = Objects.requireNonNull(connectionSource);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Integer tableNumber = parseTableNumber(request.getParameter("tableNumber"));
        if (tableNumber == null) {
            response.sendRedirect(request.getContextPath() + "/index.html?invalidTable=1");
            return;
        }

        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        request.setAttribute("tableNumber", tableNumber);

        Map<String, List<MenuItem>> menuGroups = new LinkedHashMap<>();
        boolean menuError = false;
        try {
            menuGroups = loadMenuGroups();
        } catch (SQLException exception) {
            LOGGER.log(Level.WARNING, "Unable to load the menu.", exception);
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            menuError = true;
        }
        request.setAttribute("menuGroups", menuGroups);
        request.setAttribute("menuError", menuError);
        request.getRequestDispatcher("/WEB-INF/views/menu.jsp").forward(request, response);
    }

    private static Integer parseTableNumber(String value) {
        if (value == null || !value.trim().matches("[0-9]+")) {
            return null;
        }
        try {
            int tableNumber = Integer.parseInt(value.trim());
            return tableNumber >= 1 && tableNumber <= 999 ? tableNumber : null;
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private Map<String, List<MenuItem>> loadMenuGroups() throws SQLException {
        Map<String, List<MenuItem>> groups = new LinkedHashMap<>();
        try (Connection connection = connectionSource.open();
             PreparedStatement statement = connection.prepareStatement(MENU_QUERY);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                MenuItem item = new MenuItem(
                        result.getInt("id"),
                        result.getString("name"),
                        result.getString("category"),
                        result.getBigDecimal("price"),
                        result.getString("image_path"));
                groups.computeIfAbsent(item.getCategory(), category -> new ArrayList<>()).add(item);
            }
        }
        return groups;
    }

    @FunctionalInterface
    interface ConnectionSource {
        Connection open() throws SQLException;
    }
}
