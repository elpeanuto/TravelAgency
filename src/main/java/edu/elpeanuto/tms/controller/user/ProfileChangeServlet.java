package edu.elpeanuto.tms.controller.user;

import edu.elpeanuto.tms.model.User;
import edu.elpeanuto.tms.model.enums.Gender;
import edu.elpeanuto.tms.model.enums.UserStatus;
import edu.elpeanuto.tms.servies.alert.SetAlertToRequest;
import edu.elpeanuto.tms.servies.dao.UserDAO;
import edu.elpeanuto.tms.servies.dto.UserDTO;
import edu.elpeanuto.tms.servies.exception.DAOException;
import edu.elpeanuto.tms.servies.exception.NoEntityException;
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
 * Clients change profile controller.
 */
@WebServlet("/profileChange")
public class ProfileChangeServlet extends HttpServlet {
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
        UserDTO userDto = (UserDTO) req.getSession().getAttribute("user");

        if (userDto == null) {
            resp.sendRedirect("login");
            return;
        }

        try {
            req.setAttribute("user", userDAO.get(userDto.getId()).orElseThrow(NoEntityException::new));
        } catch (DAOException e) {
            logger.error(e.getMessage());
            SetAlertToRequest.setErrorAlert(req);

            resp.sendRedirect("allProduct?page=1");
            return;
        } catch (NoEntityException e) {
            logger.warn(e.getMessage());
            SetAlertToRequest.setErrorAlert(req);
        }

        req.getRequestDispatcher("view/user/profileChange.jsp").include(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            userDAO.update(getUserFromRequest(req));
        } catch (DAOException e) {
            logger.error(e.getMessage());
            SetAlertToRequest.setErrorAlert(req);

            resp.sendRedirect("allProduct?page=1");
            return;
        }

        resp.sendRedirect("allProduct?page=1");
    }

    private User getUserFromRequest(HttpServletRequest req){
        UserDTO userDto = (UserDTO) req.getSession().getAttribute("user");

        String name = req.getParameter("name");
        String phoneNumber = req.getParameter("phoneNumber");
        String realName = req.getParameter("realName");
        String realSurname = req.getParameter("realSurname");
        String dateOfBirth = req.getParameter("dateOfBirth");
        String gender = req.getParameter("gender");
        String citizenship = req.getParameter("citizenship");
        String passportSerial = req.getParameter("passportSerial");
        String passportNumber = req.getParameter("passportNumber");
        String passportValidDate = req.getParameter("passportValidDate");
        UserStatus status = userDto.getStatus();

        return new User(userDto.getId(), userDto.getId(), name, null, null, phoneNumber, status,
                realName, realSurname, Gender.valueOf(gender), dateOfBirth, citizenship, passportSerial, passportNumber, passportValidDate);
    }
}
