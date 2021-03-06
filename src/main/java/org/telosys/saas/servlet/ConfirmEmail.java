package org.telosys.saas.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telosys.tools.users.User;
import org.telosys.tools.users.UsersManager;
import org.telosys.tools.users.crypto.PasswordEncoder;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * Servlet to validate the email address of a new user
 */
@WebServlet("/confirmEmail/*")
public class ConfirmEmail extends HttpServlet {

    protected static final Logger logger = LoggerFactory.getLogger(ConfirmEmail.class);

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        logger.info("ConfirmEmail doGet");
        // Remove last error message
        request.getSession().removeAttribute("success");
        request.getSession().removeAttribute("error");

        // Parse the request Url to find the token
        String urlRequest = request.getRequestURL().toString();
        String[] parseUrlRequest = urlRequest.split("/");
        String token = parseUrlRequest[parseUrlRequest.length-1];
        Memory memory = Memory.getMemory();
        UsersManager usersManager = UsersManager.getInstance();
        // Find the user associate to the token
        User user = memory.findUserByToken(token);
        if(user == null){
            logger.info("Bad confirmation link");
            request.getSession().setAttribute("error", "Bad confirmation link");
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        logger.info("Save user");
        usersManager.saveUser(user, user.getEncryptedPassword());
        response.sendRedirect(request.getContextPath() + "/login");
    }
}
