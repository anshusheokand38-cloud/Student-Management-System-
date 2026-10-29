package com.example;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import org.hibernate.Session;
import org.hibernate.Transaction;

@WebServlet("/students")
public class StudentServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

       String action = request.getParameter("action");

try {
    Class.forName("com.mysql.cj.jdbc.Driver");

    if ("add".equals(action)) {
                String name = request.getParameter("name");
                String course = request.getParameter("course");

                try (Connection con = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/aip_studentdb",
                        "root", "");
                     PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO students(name, course) VALUES (?, ?)")) {

                    ps.setString(1, name);
                    ps.setString(2, course);
                    ps.executeUpdate();
                }

            } else if ("update".equals(action)) {

                int id = Integer.parseInt(request.getParameter("id"));

                try (Session session =
                         HibernateUtil.getSessionFactory().openSession()) {

                    Transaction tx = session.beginTransaction();
                    Student s = session.get(Student.class, id);

                    if (s != null) {
                        s.setName(request.getParameter("name"));
                        s.setCourse(request.getParameter("course"));
                    }

                    tx.commit();
                }

            } else if ("delete".equals(action)) {

                int id = Integer.parseInt(request.getParameter("id"));

                try (Session session =
                         HibernateUtil.getSessionFactory().openSession()) {

                    Transaction tx = session.beginTransaction();
                    Student s = session.get(Student.class, id);

                    if (s != null) {
                        session.remove(s);
                    }

                    tx.commit();
                }
            }

            response.sendRedirect(request.getContextPath() + "/students");

        } catch (Exception e) {
            throw new ServletException("Database operation failed.", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        try (PrintWriter out = response.getWriter();
             Session session =
                 HibernateUtil.getSessionFactory().openSession()) {

            var students = session
                .createQuery("FROM Student ORDER BY id", Student.class)
                .list();

            out.println("<html><body>");
            out.println("<h2>Student Records</h2>");
            out.println("<a href='" + request.getContextPath()
                        + "/'>Add New Student</a><br><br>");

            out.println("<table border='1' cellpadding='8'>");
            out.println("<tr><th>ID</th><th>Name</th>"
                        + "<th>Course</th><th>Actions</th></tr>");

            for (Student s : students) {

                out.println("<tr><td>" + s.getId() + "</td>");
                out.println("<td>" + s.getName() + "</td>");
                out.println("<td>" + s.getCourse() + "</td><td>");

                out.println("<form action='"
                    + request.getContextPath()
                    + "/students' method='post'>");

                out.println("<input type='hidden' name='action' value='update'>");
                out.println("<input type='hidden' name='id' value='"
                    + s.getId() + "'>");
                out.println("<input name='name' value='"
                    + s.getName() + "' required>");
                out.println("<input name='course' value='"
                    + s.getCourse() + "' required>");
                out.println("<button type='submit'>Update</button>");
                out.println("</form>");

                out.println("<form action='"
                    + request.getContextPath()
                    + "/students' method='post'>");

                out.println("<input type='hidden' name='action' value='delete'>");
                out.println("<input type='hidden' name='id' value='"
                    + s.getId() + "'>");
                out.println("<button type='submit'>Delete</button>");
                out.println("</form>");

                out.println("</td></tr>");
            }

            out.println("</table></body></html>");

        } catch (Exception e) {
            throw new ServletException("Unable to display student records.", e);
        }
    }
}
