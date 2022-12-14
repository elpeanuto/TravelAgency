<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="${sessionScope.locale}" />
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<fmt:bundle basename="viewBundle">

    <head>
        <title>
            <fmt:message key="orders" />
        </title>
        <link rel="shortcut icon" href="${pageContext.request.contextPath}/images/travel.png" type="image/png">
        <style>
            <%@include file="/css/home-style.css"%>
        </style>
        <script src="https://unpkg.com/sweetalert/dist/sweetalert.min.js"></script>


    </head>

    <body>
    <%@ include file="header.jspf" %>
    <div style="color: black; text-align: center; font-size: 20px;">
        <fmt:message key="yourOrders" /><i class='fab fa-elementor'></i>
    </div>

    <table>
        <thead>
        <tr>
            <th scope="col">
                <fmt:message key="name" />
            </th>
            <th scope="col">
                <fmt:message key="country" />
            </th>
            <th scope="col">
                <fmt:message key="city" />
            </th>
            <th scope="col">
                <fmt:message key="price" />
            </th>
            <th>
                <fmt:message key="status" />
            </th>
            <th>
                <fmt:message key="view" />
            </th>
            <th scope="col">
                <fmt:message key="cancel" />
            </th>
        </tr>
        </thead>
        <tbody>

        <c:forEach var="order" items="${requestScope.orderList}">
            <tr>
                <td>${order.productName}</td>
                <td>${order.productCountry}</td>
                <td>${order.productCity}</td>
                <td>${order.totalPrice}</td>
                <td>${order.orderStatus}</td>
                <td>
                    <a href="bill?id=${order.orderId}">
                        <fmt:message key="view" />
                    </a>
                </td>
                <td>
                    <a href="cancelOrder?id=${order.orderId}">
                        <fmt:message key="cancel" />
                    </a>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
    <br>
    <br>
    <br>

    <c:if test="${requestScope.positionList != null}">
        <div class="pagination"">
            <c:forEach var="position" items="${requestScope.positionList}">
                <a href="myOrders?page=${position}">${position}</a>
            </c:forEach>
        </div>
    </c:if>

    <jsp:include page="footer.jsp" />
    <script>
        if ("${sessionScope.alertFlag}" === "true") {
            swal("${sessionScope.alertHeader}", "${sessionScope.alertBody}", "${sessionScope.alertType}")
        }
    </script>
    </body>
</fmt:bundle>

</html>