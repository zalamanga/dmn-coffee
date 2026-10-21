<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="id_ID"/>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Menu | DMN Coffeeshop</title>
    <link rel="icon" href="data:,">
    <link rel="stylesheet" href="css/style.css">
    <script src="js/common.js" defer></script>
    <script src="js/main_menu.js" defer></script>
</head>
<body data-table-number="<c:out value='${tableNumber}'/>">
    <a class="skip-link" href="#menu-content">Skip to menu</a>
    <header><h1>DMN Coffeeshop</h1><p class="table-number">Table <c:out value="${tableNumber}"/></p></header>
    <nav aria-label="Menu categories">
        <ul>
            <c:forEach var="group" items="${menuGroups}" varStatus="groupStatus">
                <c:set var="anchor" value="${group.key eq 'Snacks' ? 'snacks' : group.key eq 'Main Dishes' ? 'main-dishes' : group.key eq 'Coffees' ? 'coffees' : group.key eq 'Non-Coffee Drinks' ? 'non-coffees' : group.key eq 'Teas' ? 'teas' : group.key eq 'Milk-Based Drinks' ? 'milk-base' : 'other'}"/>
                <c:if test="${anchor eq 'other'}"><c:set var="anchor" value="category-${groupStatus.index}"/></c:if>
                <li><a href="#${anchor}"><c:out value="${group.key}"/></a></li>
            </c:forEach>
            <li><a href="cart.html">Cart (<span id="cart-count">0</span>)</a></li>
            <li><a href="index.html">Change table</a></li>
        </ul>
    </nav>
    <main id="menu-content">
        <p class="demo-note">Sample menu and prices for this demo. Your cart stays on this browser.</p>
        <p id="storage-error" role="alert" hidden></p>
        <p id="feedback" role="status" aria-live="polite"></p>
        <c:choose>
            <c:when test="${menuError}">
                <section role="alert"><h2>The menu could not be loaded</h2><p>Please try again or contact the person running this demo.</p><a class="button" href="MenuItemsServlet?tableNumber=${tableNumber}">Retry loading menu</a></section>
            </c:when>
            <c:otherwise>
                <c:if test="${empty menuGroups}"><section><h2>No menu items yet</h2><p>The menu has not been added. Please check again later.</p><a class="button" href="MenuItemsServlet?tableNumber=${tableNumber}">Reload menu</a></section></c:if>
                <c:forEach var="group" items="${menuGroups}" varStatus="groupStatus">
                    <c:set var="anchor" value="${group.key eq 'Snacks' ? 'snacks' : group.key eq 'Main Dishes' ? 'main-dishes' : group.key eq 'Coffees' ? 'coffees' : group.key eq 'Non-Coffee Drinks' ? 'non-coffees' : group.key eq 'Teas' ? 'teas' : group.key eq 'Milk-Based Drinks' ? 'milk-base' : 'other'}"/>
                    <c:if test="${anchor eq 'other'}"><c:set var="anchor" value="category-${groupStatus.index}"/></c:if>
                    <section id="${anchor}">
                        <h2><c:out value="${group.key}"/></h2>
                        <ul class="menu-list">
                            <c:forEach var="item" items="${group.value}">
                                <li class="menu-item">
                                    <img src="<c:out value='${item.imagePath}'/>" alt="<c:out value='${item.name}'/>" width="100" height="100" loading="lazy">
                                    <div class="item-details"><h3><c:out value="${item.name}"/></h3><p>Rp <fmt:formatNumber value="${item.price}" pattern="#,##0.##"/></p></div>
                                    <button type="button" data-add-item data-id="${item.id}" data-name="<c:out value='${item.name}'/>" data-price="${item.price}">Add to cart<span class="sr-only">: <c:out value="${item.name}"/></span></button>
                                </li>
                            </c:forEach>
                        </ul>
                    </section>
                </c:forEach>
            </c:otherwise>
        </c:choose>
    </main>
    <footer><p>&copy; 2024 Nugi Nugroho. DMN Coffeeshop project.</p></footer>
</body>
</html>
