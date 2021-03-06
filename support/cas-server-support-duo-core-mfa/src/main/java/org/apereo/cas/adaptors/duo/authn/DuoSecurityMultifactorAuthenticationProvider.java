package org.apereo.cas.adaptors.duo.authn;

import org.apereo.cas.authentication.MultifactorAuthenticationProvider;

/**
 * This is {@link DuoSecurityMultifactorAuthenticationProvider}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public interface DuoSecurityMultifactorAuthenticationProvider extends MultifactorAuthenticationProvider {

    /**
     * Gets duo authentication service.
     *
     * @return the duo authentication service
     */
    DuoSecurityAuthenticationService getDuoAuthenticationService();

    /**
     * Link to the registration portal where new users
     * will be redirected to sign up for duo.
     *
     * @return the url.
     */
    String getRegistrationUrl();
}
