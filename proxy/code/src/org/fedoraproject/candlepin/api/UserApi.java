/**
 * Copyright (c) 2008 Red Hat, Inc.
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package org.fedoraproject.candlepin.api;

import org.fedoraproject.candlepin.model.ObjectFactory;
import org.fedoraproject.candlepin.model.User;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;


/**
 * REST api gateway for the User object.
 */
@Path("/user")
public class UserApi extends BaseApi {
    @GET @Path("/{login}")
    @Produces(MediaType.APPLICATION_JSON)
    public User get(@PathParam("login") String login) {
        return (User) ObjectFactory.get().lookupByFieldName(User.class, "login", login);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class getApiClass() {
        return User.class;
    }
}
