/*
 * Copyright 2017 Swedish E-identification Board (E-legitimationsnämnden)
 *  		 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.tillvaxtverket.tsltrust.webservice;

import com.google.gson.Gson;
import se.tillvaxtverket.tsltrust.common.utils.core.Base64Coder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Cookie;

/**
 * This class provides test identities for the user.jsp page, that are used only when the web service is run in devmode.
 * <p>
 * The selected test identity is stored in a cookie as a replacement for a real authentication mechanism
 * that is being enforced in production mode. Devmode is set through a parameter in the Web.xml file.
 */
public class TestIdentities extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(TestIdentities.class.getName());
    private static Gson gson = new Gson();
    String[][] testIdentities = new String[][]{
        new String[]{
            "Lee Falk",
            "https://idp.svelegtest.se/idp",
            "198504051569",
            "personalIdentityNumber"
        },
        new String[]{
            "Kit Walker",
            "https://idp.svelegtest.se/idp",
            "196501021010",
            "personalIdentityNumber"
        },
        new String[]{
            "Fantomen",
            "https://idp.svelegtest.se/idp",
            "196501021111",
            "personalIdentityNumber"
        },
        new String[]{
            "Diana Palmer",
            "https://idp.svelegtest.se/idp",
            "196501022222",
            "personalIdentityNumber"
        },
        new String[]{
            "Guran Bandar",
            "https://idp.svelegtest.se/idp",
            "196501023333",
            "personalIdentityNumber"
        },
        new String[]{
            "Rex King",
            "https://idp.svelegtest.se/idp",
            "196501024444",
            "personalIdentityNumber"
        },
        new String[]{
            "Lamanda Luaga",
            "https://idp.svelegtest.se/idp",
            "196501025555",
            "personalIdentityNumber"
        }
    };

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        String action = request.getParameter("action");
        if (action == null) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            response.getWriter().write("");
            return;
        }

        if (action.equals("userlist")) {
            String json = getUserInfo();
            response.getWriter().write(json);
            return;
        }

        if (action.equals("cookie")) {
            String json = getCookie(request, response);
            response.getWriter().write(json);
            return;
        }

        if (action.equals("resetCookie")) {
            Cookie[] cookies = request.getCookies();
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("testID")) {
                    cookie.setMaxAge(0);
                    response.addCookie(cookie);
                }
            }
            response.getWriter().write("[]");
            return;
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    private String getUserInfo() {
        List<UserParm> userList = new ArrayList<UserParm>();
        for (String[] userData : testIdentities) {
            UserParm user = new UserParm();
            user.name = userData[0];
            user.idp = userData[1];
            user.id = userData[2];
            user.attribute = userData[3];
            userList.add(user);
        }
        return gson.toJson(userList);
    }

    private String getCookie(HttpServletRequest request, HttpServletResponse response) {
        String name = request.getParameter("name");
        String idp = request.getParameter("idp");
        String attr = request.getParameter("attr");
        String id = request.getParameter("id");
        if (id == null || idp == null || attr == null || id == null) {
            return "";
        }
        Cookie cookie = new Cookie("testID", "");
        StringBuilder b = new StringBuilder();
        b.append("name:").append(b64(name)).append(";");
        b.append("idp:").append(b64(idp)).append(";");
        b.append("attr:").append(b64(attr)).append(";");
        b.append("id:").append(b64(id));
        cookie.setValue(b64(b.toString()));
        response.addCookie(cookie);
        return gson.toJson(new UserName(name));
    }

    private String b64(String toB64) {
        return Base64Coder.encodeString(toB64);
    }

    class UserParm {

        String name;
        String idp;
        String attribute;
        String id;

        public UserParm() {
        }
    }

    class UserName {

        String user;

        public UserName(String user) {
            this.user = user;
        }
    }
}
