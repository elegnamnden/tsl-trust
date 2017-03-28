/*
 * Copyright 2012 Swedish Agency for Economic and Regional Growth - Tillväxtverket 
 *  		 
 * Licensed under the EUPL, Version 1.1 or ñ as soon they will be approved by the 
 * European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence. 
 * You may obtain a copy of the Licence at:
 *
 * http://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed 
 * under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations 
 * under the Licence.
 */
package se.tillvaxtverket.tsltrust.weblogic.hibernate;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;

/**
 * Hibernate utility class for the log database
 */
public class LogHibernateUtil extends HibernateUtil{


    @Override
    public SessionFactory getSessionFactory() throws HibernateException{
        if (logSessionFactory==null){
            throw new HibernateException("Null session factory");
        }
        return logSessionFactory;
    }

    @Override
    public void shutdown() {
        try {
            getSessionFactory().close();
        } catch (HibernateException ex) {
            LOG.warning(ex.getMessage());
        }
    }
}