package dev.byblos.webapi.security;

import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

/**
 * A {@link ClientRegistrationRepository} that has no registered provider.
 */
public enum EmptyClientRegistrationRepository implements ClientRegistrationRepository {
    INSTANCE;

    @Override
    public ClientRegistration findByRegistrationId(String registrationId) {
        return null;
    }
}
