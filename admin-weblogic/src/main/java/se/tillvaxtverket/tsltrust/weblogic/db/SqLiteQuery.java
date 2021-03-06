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
package se.tillvaxtverket.tsltrust.weblogic.db;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.*;
import se.tillvaxtverket.tsltrust.common.utils.core.SqLiteConnectionPool;

/**
 * Generic abstract class for queries to an SQLite database
 */
public abstract class SqLiteQuery<E extends Object> {

    private static final Logger LOG = Logger.getLogger(SqLiteAction.class.getName());
    private String userid, password;
    private String url;
    private static final long maxWaitTime = 10000;

    /**
     * Constructor
     * @param url Database URL
     * @param userid Database user id
     * @param password Database password
     */
    public SqLiteQuery(String url, String userid, String password) {
        this.userid = userid;
        this.password = password;
        this.url = url;
    }

    /**
     * Execute a database query
     * @param query The query string
     * @param defaultResult The default return value in case the query fails to produce result
     * @return Returns a result object for the database query
     */
    public E dbQuery(String query, E defaultResult) {
        E result = defaultResult;
        Connection con = null;
        Statement stmt;
        boolean complete = false;
        long initTime = System.currentTimeMillis();
        while (!complete && System.currentTimeMillis() < initTime + maxWaitTime) {
            try {
                con = SqLiteConnectionPool.getConnection(url,userid, password);

                if (con != null) {
                    // check the connection
                    if (con.getWarnings() != null) {
                        con.clearWarnings();
                        con.close();
                        con = SqLiteConnectionPool.getConnection(url, userid, password);
                    }

                    stmt = con.createStatement();
                    ResultSet rs = stmt.executeQuery(query);
                    result = parseResultSet(rs);
                    rs.close();
                    con.close();
                } else {
                    result = defaultResult;
                    LOG.warning("Null SQLite connection. Returning default value");
                }
                complete = true;
            } catch (Exception ex) {
                result = defaultResult;
//                LOG.warning(ex.getMessage());
                try {
                    if (con != null) {
                        con.close();
                    }
                } catch (SQLException ex1) {
                }
                try {
                    Thread.sleep(25);
                } catch (InterruptedException ex1) {
                    LOG.log(Level.SEVERE, null, ex1);
                }
            }
        }
        return result;
    }

    /**
     * Abstract method for parsing the database result set into a result object
     * @param rs The result set obtained from the database query
     * @param defaultResult The default result value set by the dbQuery method
     * @return Result object obtained from parsing the result set
     */
    abstract E parseResultSet(ResultSet rs) throws SQLException;
}
