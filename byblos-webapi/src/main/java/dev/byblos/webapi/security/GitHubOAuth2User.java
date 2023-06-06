package dev.byblos.webapi.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public final class GitHubOAuth2User implements OAuth2User {
    private final OAuth2User delegate;

    public GitHubOAuth2User(OAuth2User delegate) {
        this.delegate = requireNonNull(delegate);
    }

    @Override
    public Map<String, Object> getAttributes() {
        return delegate.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return delegate.getAuthorities();
    }

    @Override
    public String getName() {
        return delegate.getAttribute("login");
    }
}
