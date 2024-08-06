package com.user;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Random;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.algorithm.AES;
import com.connection.DatabaseConnection;

/**
 * Servlet implementation class UserLogin
 */
@WebServlet("/UserLogin")
public class UserLogin extends HttpServlet {
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			final String secretKey = "%@ajdhdklddf";
			String uname = request.getParameter("uname");
			String pass = request.getParameter("upass");
			String encryptPass= AES.encrypt(pass, secretKey);
			String vercode = request.getParameter("vercode");
			String captchaDB = null;
			HttpSession hs = request.getSession();
			String tokens = UUID.randomUUID().toString();
			Random random = new Random();
			int newRandomCaptcha = random.nextInt(9000) + 10000;
			Connection con = DatabaseConnection.getConnection();
			Statement st = con.createStatement();
			ResultSet captchResultSet = DatabaseConnection.getResultFromSqlQuery("select * from tblcaptcha");
			if (captchResultSet.next()) {
				captchaDB = captchResultSet.getString(1);
			}
			if (captchaDB.equals(vercode)) {
				ResultSet resultset = st.executeQuery("select * from tbluser where uname='" + uname + "' AND upass='" + encryptPass + "'");
				if (resultset.next()) {
					hs.setAttribute("uname",resultset.getString("uname"));
					response.sendRedirect("user-dashboard.jsp?_tokens='" + tokens + "'");
				} else {
					String message = "You have enter wrong credentials";
					hs.setAttribute("credential", message);
					response.sendRedirect("user-login.jsp");
					int update = DatabaseConnection.insertUpdateFromSqlQuery("update tblcaptcha set captcha='"+ newRandomCaptcha + "'");
				}
			} else {
				String message = "You have enter invalid verification code";
				hs.setAttribute("verificationCode", message);
				response.sendRedirect("user-dashboard.jsp");
				int update = DatabaseConnection.insertUpdateFromSqlQuery("update tblcaptcha set captcha='"+ newRandomCaptcha + "'");
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

}
