# Security

By default, Byblos is accessible without any form of authentication or authorization.
While this is useful for quickly testing it, it will not be suitable for most production deployments.

## Enabling security

Security is enabled by defining the following [configuration parameters](configuration.md):

| Key                                     | Type | Description                                                     |
|-----------------------------------------|------|-----------------------------------------------------------------|
| `byblos.webapi.security.enabled`        | boolean | Whether to require authentication to access protected resources |
| `byblos.webapi.security.provider`       | string | Which OAuth provider to use                                     |

The available providers are described in the next sections.
Each provider comes with its own additional settings.

## GitHub

To use GitHub as an authentication provider, use the following configuration values:

```
byblos.webapi.security {
  enabled = true
  provider = github
}
```

You will then need to define the following parameters to further configure GitHub:

| Key                                | Type | Description                                                         |
|------------------------------------|------|---------------------------------------------------------------------|
| `byblos.webapi.security.client-id` | string | Client ID for the OAuth flow                                        |
| `byblos.webapi.security.client-secret` | string | Client secret for the OAuth flow                                    |
| `byblos.webapi.security.github-org` | string | A GitHub organization that users must be a member of. **Optional.** |

Please refer to [GitHub's documentation](https://docs.github.com/en/apps/oauth-apps/building-oauth-apps/creating-an-oauth-app) to learn how to obtain your client ID and secret.
The authorization callback URL will look like `https://byblos.fly.dev/login/oauth2/code/github`.

By default, any GitHub user is allowed to access Byblos.
Make sure to use the `github-org` parameter if you wish to restrict access to only members of a given org.

## Google

To use Google as an authentication provider, use the following configuration values:

```
byblos.webapi.security {
  enabled = true
  provider = google
}
```

You will then need to define the following parameters to further configure Google:

| Key                                | Type | Description                                                      |
|------------------------------------|------|------------------------------------------------------------------|
| `byblos.webapi.security.client-id` | string | Client ID for the OAuth flow                                     |
| `byblos.webapi.security.client-secret` | string | Client secret for the OAuth flow                             |

Please refer to [Google's documentation](https://developers.google.com/identity/gsi/web/guides/get-google-api-clientid) to learn how to obtain your client ID and secret.
The authorized redirect URI will look like `https://byblos.fly.dev/login/oauth2/code/google`.

## Okta

To use Google as an authentication provider, use the following configuration values:

```
byblos.webapi.security {
  enabled = true
  provider = okta
}
```

You will then need to define the following parameters to further configure Okta:

| Key                                | Type | Description                                                      |
|------------------------------------|------|------------------------------------------------------------------|
| `byblos.webapi.security.client-id` | string | Client ID for the OAuth flow                                     |
| `byblos.webapi.security.client-secret` | string | Client secret for the OAuth flow                             |
| `byblos.webapi.security.okta-subdomain` | string | Okta subdomain |

Please refer to [Okta's documentation](https://help.okta.com/en-us/Content/Topics/Apps/Apps_App_Integration_Wizard_OIDC.htm) to learn how to obtain your client ID and secret.
The redirect URI will look like `https://byblos.fly.dev/login/oauth2/code/okta`.