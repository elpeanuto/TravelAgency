package edu.elpeanuto.tms.controller.authentification;

import edu.elpeanuto.tms.model.enums.UserStatus;
import edu.elpeanuto.tms.servies.alert.AlertType;
import edu.elpeanuto.tms.servies.alert.SetAlertToRequest;
import edu.elpeanuto.tms.servies.dao.UserDAO;
import edu.elpeanuto.tms.servies.PasswordHashing;
import edu.elpeanuto.tms.model.User;
import edu.elpeanuto.tms.servies.exception.DAOException;
import org.slf4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Client registration controller.
 */
@WebServlet("/registration")
public class RegistrationServlet extends HttpServlet {
    private Logger logger;
    private UserDAO userDAO;

    @Override
    public void init(ServletConfig config) {
        ServletContext sc = config.getServletContext();

        userDAO = (UserDAO) sc.getAttribute("userDAO");
        logger = (Logger) sc.getAttribute("logger");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("view/authentication/registration.jsp").include(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String userName = req.getParameter("name");
        String userEmail = req.getParameter("email");
        String userPhoneNumber = req.getParameter("contact");
        String userPassword = req.getParameter("password");
        String userPasswordRepetition = req.getParameter("re-password");
        String status;

        if (userEmail.equals("admin@gmail.com"))
            status = UserStatus.Leader.name();
        else
            status = UserStatus.Client.name();

        userPassword = PasswordHashing.hashPassword(userPassword);
        userPasswordRepetition = PasswordHashing.hashPassword(userPasswordRepetition);

        User user = new User(null, userName, userPassword, userEmail, userPhoneNumber, UserStatus.valueOf(status));

        try {
            if (userDAO.isEmailOccupied(userEmail)) {
                SetAlertToRequest.setCustomAlert(req, "Error", "Account with this email is already exist.", AlertType.ERROR);
                resp.sendRedirect("registration");
            } else if (!userPassword.equals(userPasswordRepetition)) {
                SetAlertToRequest.setCustomAlert(req, "Error", "Passwords do not match.", AlertType.ERROR);
                resp.sendRedirect("registration");
            } else if (userDAO.save(user)) {
                SetAlertToRequest.setCustomAlert(req, "Success", "Account registered successfully.", AlertType.SUCCESS);
                resp.sendRedirect("login");
            }
        } catch (DAOException e) {
            logger.error(e.getMessage());
            SetAlertToRequest.setErrorAlert(req);

            resp.sendRedirect("login");
        }
    }
}
