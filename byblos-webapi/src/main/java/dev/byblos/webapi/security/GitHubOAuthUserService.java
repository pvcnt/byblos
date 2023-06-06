package dev.byblos.webapi.security;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.reactive.function.client.WebClient;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient;

public final class GitHubOAuthUserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final WebClient rest;
    private final String allowedOrg;
    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

    public GitHubOAuthUserService(WebClient rest, @Nullable String allowedOrg) {
        this.rest = requireNonNull(rest);
        this.allowedOrg = allowedOrg;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        var user = delegate.loadUser(request);
        var client = new OAuth2AuthorizedClient(request.getClientRegistration(), user.getName(), request.getAccessToken());
        if (null != allowedOrg) {
            String url = user.getAttribute("organizations_url");
            List<Map<String, Object>> orgs = rest.get()
                    .uri(url)
                    .attributes(oauth2AuthorizedClient(client))
                    .retrieve()
                    .bodyToMono(List.class)
                    .block();
            if (null != orgs && orgs.stream().noneMatch(org -> allowedOrg.equals(org.get("login")))) {
                throw new OAuth2AuthenticationException(new OAuth2Error("invalid_token", "Not a member of the GitHub org: " + allowedOrg, ""));
            }
        }
        return new GitHubOAuth2User(user);
    }
}
