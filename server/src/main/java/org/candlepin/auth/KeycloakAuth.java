/*
 *  Copyright (c) 2009 - ${YEAR} Red Hat, Inc.
 *
 *  This software is licensed to you under the GNU General Public License,
 *  version 2 (GPLv2). There is NO WARRANTY for this software, express or
 *  implied, including the implied warranties of MERCHANTABILITY or FITNESS
 *  FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 *  along with this software; if not, see
 *  http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 *  Red Hat trademarks are not licensed under GPLv2. No permission is
 *  granted to use or replicate Red Hat trademarks that are incorporated
 *  in this software or its documentation.
 */

package org.candlepin.auth;

import com.google.inject.Inject;
import org.apache.commons.codec.binary.Base64;
import org.candlepin.auth.permissions.PermissionFactory;
import org.candlepin.common.exceptions.CandlepinException;
import org.candlepin.common.exceptions.NotAuthorizedException;
import org.candlepin.common.exceptions.ServiceUnavailableException;
import org.candlepin.common.resteasy.auth.AuthUtil;
import org.candlepin.service.UserServiceAdapter;
import org.jboss.resteasy.spi.HttpRequest;
import org.keycloak.KeycloakSecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.keycloak.KeycloakPrincipal;
import org.xnap.commons.i18n.I18n;

import javax.inject.Provider;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

public class KeycloakAuth extends UserAuth {

    private static Logger log = LoggerFactory.getLogger(BasicAuth.class);

    @Context
    private SecurityContext securityContext;

    @Inject
    public KeycloakAuth(UserServiceAdapter userServiceAdapter, Provider<I18n> i18nProvider, PermissionFactory permissionFactory) {
        super(userServiceAdapter, i18nProvider, permissionFactory);
    }

    @Override
    public Principal getPrincipal(HttpRequest httpRequest) {
        try {
            String auth = AuthUtil.getHeader(httpRequest, "Authorization");
            System.out.println(auth);
            //= httpRequest.getAttribute(KeycloakSecurityContext.class.getName());
            String username = securityContext.getUserPrincipal().getName();
            System.out.println(username+"hey this is username");
            String password = null;
            if (securityContext.getUserPrincipal() instanceof KeycloakPrincipal) {
                KeycloakPrincipal<KeycloakSecurityContext> kp = (KeycloakPrincipal<KeycloakSecurityContext>)  securityContext.getUserPrincipal();
                // Login Name
                username = kp.getKeycloakSecurityContext().getIdToken().getPreferredUsername();
                System.out.println("this is sc username"+username);
               // password = kp.getKeycloakSecurityContext().getToken()
            }
            //i18r below!!

            if (auth != null && auth.toUpperCase().startsWith("BASIC ")) {
                String userpassEncoded = auth.substring(6);
                String[] userpass = new String(Base64
                        .decodeBase64(userpassEncoded)).split(":", 2);
                username = userpass[0];
                password = null;
                if (userpass.length > 1) {
                    password = userpass[1];
                }

                if (log.isDebugEnabled()) {
                    Integer length = (password == null) ? 0 : password.length();
                    log.debug("check for: {} - password of length {}", username, length);
                }

                if (userServiceAdapter.validateUser(username, password)) {
                    Principal principal = createPrincipal(username);
                    log.debug("principal created for user '{}'", username);
                    return principal;
                }
                else {
                    throw new NotAuthorizedException(i18nProvider.get().tr("Invalid Credentials"));
                }
            }


        }
        catch (CandlepinException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error getting principal " + e);
            }
            throw e;
        }
        catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Error getting principal " + e);
            }
            throw new ServiceUnavailableException(i18nProvider.get().tr("Error contacting user service"));
        }
        return null;



    }
}
