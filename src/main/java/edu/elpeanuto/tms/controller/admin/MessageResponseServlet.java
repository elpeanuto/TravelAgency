package edu.elpeanuto.tms.controller.admin;

import edu.elpeanuto.tms.servies.alert.AlertType;
import edu.elpeanuto.tms.servies.alert.SetAlertToRequest;
import edu.elpeanuto.tms.servies.dao.MessagesDAO;
import edu.elpeanuto.tms.servies.dao.UserDAO;
import edu.elpeanuto.tms.servies.dto.MessageDTO;
import edu.elpeanuto.tms.servies.dto.UserDTO;
import edu.elpeanuto.tms.servies.exception.DAOException;
import edu.elpeanuto.tms.servies.exception.FailToUpdateDBException;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Message response controller.
 */
@WebServlet("/messageResponse")
public class MessageResponseServlet extends HttpServlet {
    private Logger logger;
    private MessagesDAO messagesDAO;
    private UserDAO userDAO;

    @Override
    public void init(ServletConfig config) {
        ServletContext sc = config.getServletContext();

        messagesDAO = (MessagesDAO) sc.getAttribute("messagesDAO");
        userDAO = (UserDAO) sc.getAttribute("userDAO");
        logger = (Logger) sc.getAttribute("logger");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long messageId = Long.valueOf(req.getParameter("id"));

        try {
            req.setAttribute("admin", userDAO.get(((UserDTO) req.getSession().getAttribute("user")).getId()).orElseThrow(NoEntityException::new));
            req.setAttribute("message", messagesDAO.get(messageId).orElseThrow(NoEntityException::new));
        } catch (DAOException e) {
            logger.error(e.getMessage());
            SetAlertToRequest.setCustomAlert(req, "Error", e.getMessage(), AlertType.ERROR);
        } catch (NoEntityException e) {
            logger.warn(e.getMessage());
            SetAlertToRequest.setCustomAlert(req, "Error", e.getMessage(), AlertType.WARNING);
        }

        req.getRequestDispatcher("view/admin/messageResponse.jsp").include(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long messageId = Long.valueOf(req.getParameter("id"));
        Long adminId = Long.valueOf(req.getParameter("adminId"));
        String response = req.getParameter("response");

        try {
            Long answerMessageId = messagesDAO.get(messageId).orElseThrow(NoEntityException::new).getMessageAnswerId();
            if(!messagesDAO.adminUpdate(new MessageDTO(answerMessageId, adminId, response,
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))))
                throw new FailToUpdateDBException();
        } catch (DAOException e) {
            logger.error(e.getMessage());
            SetAlertToRequest.setCustomAlert(req, "Error", e.getMessage(), AlertType.ERROR);

            resp.sendRedirect("adminHome");
            return;
        } catch (NoEntityException e) {
            logger.warn(e.getMessage());
            SetAlertToRequest.setCustomAlert(req, "Warning", e.getMessage(), AlertType.WARNING);
        } catch (FailToUpdateDBException e) {
            throw new RuntimeException(e);
        }

        resp.sendRedirect("allMessages?page=1");
    }
}
