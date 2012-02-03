---
title: Scopes
layout: default
---

{% include sequence.md %}

# OAuth2 Scope

The OAuth2 spec includes a `scope` parameter as part of the token granting request (actually it is a set of scope values).  The spec leaves the business content of the scope up to the participants in the protocol - i.e. the scope values are completely arbitrary and can in principle be chosen by any Resource Server using the tokens.  Clients of the Resource Server have to ask for a valid scope to get a token, but the Authorization Server itself attaches no meaning to the scope - it just passes the value through to the Resource Server.

A scope can be used to restrict the audience of the token if a Resource Server would reject any tokens with scope values that it doesn't handle.  If the system wants to limit the audience of a token, by explicitly only allowing it to be used on specific Resource Servers, all it has to do is arrange the valid scope values and distribute them among Resource Servers accordingly.  A scope value can be shared between multiple physical Resource Servers.

One idea (outside the spec) for adding explicit meaning to scopes is to use a URL that can be used for message discovery.  This would essentially remove the option to share scope values between Resource Servers.

> Sequence diagram, scopes:

    {{ sequence }}
    {% include scopes-flow.txt %}
    {{ endsequence }}

The UAA implementation of the Authorization Server has a couple of extra scope-related features (by virtue of being implemented in Spring Security where the features originate).

1. There is an optional step in client registration, where a client declares which scopes it will ask for, or alternatively where the Authorization Server can limit the scopes it can ask for. The Authorization Server can then check that token requests contain a valid scope (i.e. one of the set provided on registration).

2. The Resource Servers can each have a unique ID (e.g. a URI). And aother optional part of a client registration is to provide a set of allowed resource ids for the client in question.  The Authorization Server binds the allowed resource ids to the token and then provides the information via the `/check_token` endpoint, so that a Resource Server can check that its own ID is on the allowed list for the token before serving a resource.

Resource IDs have some of the character of a scope, except that the clients themselves don't need to know about them - it is information exchanged between the Authorization and Resource Servers.

One advantage of using resource ids is that the scope values do

> Sequence diagram, resource ids:

    {{ sequence }}
    {% include resource-ids-flow.txt %}
    {{ endsequence }}
