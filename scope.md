---
title: Scopes
layout: default
---

{% include sequence.md %}

# OAuth2 Scope

The OAuth2 spec includes a `scope` parameter as part of the token granting request (actually it is a set of scope values).  The spec leaves the business content of the scope up to the participants in the protocol - i.e. the scope values are completely arbitrary and can in principle be chosen by any Resource Server using the tokens.  Clients of the Resource Server can ask for a scope to get a token, but the Authorization Server itself doesn't attach any meaning to it - it just passes the value through to the Resource Server.

> Sequence diagram, scopes flow:

    {{ sequence }}
    {% include scopes-flow.txt %}
    {{ endsequence }}

There might be some policies in the Authorization Server about what scope values are valid (the spec doesn't prescribe any but mentions that they might be sensible).  A scope can be used to restrict the audience of the token (in the sense of where it can be used, not by whom): a Resource Server can reject any tokens with scope values that it doesn't handle.  If the system wants to limit the audience of a token, by explicitly only allowing it to be used on specific Resource Servers, all it has to do is arrange the valid scope values and distribute them among Resource Servers accordingly.  A scope value can be shared between multiple physical Resource Servers.

## Existing Examples of Scope Values

Existing scope values in the wild seem to fall into two categories.

1. (The majority) short, lightweight strings, usually obviously placing a limit on the values of the data accessible.  Descriptive but not detailed.  E.g. "profile".  Could also be like a role name or a permission: "read" or "write".

2. (The minority) full URL which tends to be a valid resource on the Resource Server itself

### OpenID Connect

The [OpenID Connect spec](http://openid.net/specs/openid-connect-messages-1_0.html) reserves a few scope values for Resource Servers that happen to provide identity data.  They are in the short string camp: "openid", "email", "profile", "address", "phone".

### Facebook

[Facebook](http://developers.facebook.com/docs/authentication/)) also uses short scope values (and calls them ["permissions"](http://developers.facebook.com/docs/reference/api/permissions/)).  Examples: "email", "read\_stream", "publish\_stream".

### Google

[Google](http://code.google.com/apis/accounts/docs/OAuth2.html) is the odd one out - it uses URLs.  It is quite hard to discover a list of valid scopes for Google APIs, but all the ones we have seen are URLs. Some examples: `https://www.googleapis.com/auth/userinfo.email`, `https://www.googleapis.com/auth/userinfo.profile`.  They are valid resource URLs but they don't have any interesting content.

## Using URLs as Scope Values

Following Google, one idea (outside the spec) for adding explicit meaning to scopes is to use a URL that can be used to identify the Resource Server it is targeted at.  This would essentially remove the option to share scope values between Resource Servers.  It could also be used for message discovery:

> Sequence diagram, scopes and optional message discovery:

    {{ sequence }}
    {% include scopes-flow.txt %}
    {{ endsequence }}

## Resource IDs

The Spring Security implementation of the Authorization Server has a couple of extra scope-related features:

1. There is an optional step in client registration, where a client declares which scopes it will ask for, or alternatively where the Authorization Server can limit the scopes it can ask for. The Authorization Server can then check that token requests contain a valid scope (i.e. one of the set provided on registration).  N.B. this feature is shown in the sequence diagrams above even though it is not explicitly defined in the specification.

2. The Resource Servers can each have a unique ID (e.g. a URI). And aother optional part of a client registration is to provide a set of allowed resource ids for the client in question.  The Authorization Server binds the allowed resource ids to the token and then provides the information via the `/check_token` endpoint, so that a Resource Server can check that its own ID is on the allowed list for the token before serving a resource.

Resource IDs have some of the character of a scope, except that the clients themselves don't need to know about them - it is information exchanged between the Authorization and Resource Servers.  It can therefore change independently of scope values and independent of the clients.

> Sequence diagram, resource ids:

    {{ sequence }}
    {% include resource-ids-flow.txt %}
    {{ endsequence }}
