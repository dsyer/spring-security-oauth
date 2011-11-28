---
title: CSRF
layout: default
---

{% include sequence.md %}

# Cross Site Request Forgery and OAuth2

## Attacks on the Authorization Server

Some example CSRF attempts on the Authorization Server follow.  The bad guy is trying to obtain an access token by tricking the user into clicking on a link to the Authorization Server at a time when the user is already authenticated.  All of the examples rely on the fact that the Authorization Server will send redirects to any URI you ask it to, unless a specific redirect has been pre-registered for the client in question.  With registered redirects the attacks are not going to work, but that might be too restrictive for some systems (e.g. Facebook doesn't bother with it but Google does).

To try out the attacks you need an Authorization Server running locally, and then you can just click on the links and see what secrets are revealed in the redirect.  There is a simple script in the redirect that captures the browser window location and mirrors it to demonstrate how the bad guy could strip the information he needs and send it somewhere else.  The examples use the `sparklr2` application from [Spring Security OAuth](http://github.com/SpringSource/spring-security-oauth).

### Implicit Grant Attacks

Implicit Grant attacks are arguably the nastiest because the bad guy gets a token without having to do any more work.  He does, however, need to know the client secret, as well as a valid client id. So this attack succeeds:

> Implicit Grant: client without secret [click here](http://localhost:8080/sparklr2/oauth/authorize?response_type=token&client_id=my-trusted-client&redirect_uri=http://localhost:4000/attack.html&scope=read)

But this attack fails:

> Implicit Grant: client with secret: [click here](http://localhost:8080/sparklr2/oauth/authorize?response_type=token&client_id=my-trusted-client-with-secret&redirect_uri=http://localhost:4000/attack.html&scope=read)

### Authorization Code Attacks

Here the basic Authorization Code grant flow to remind us of the
details before we look at the attacks on it:

> Sequence diagram, auth-code-flow:

    {{ sequence }}
    {% include auth-code-flow.txt %}
    {{ endsequence }}

Authorization code attacks allow the bad guy to steal an authorization code which can then be exchanged for a token.  The bad guy can get a an authorization code independent of client secrets, to use it he will need the secret.  So both of these links send us back a valid code, but one of them is protected by a client secret:

> Authorization Code Grant: client without secret [click here](http://localhost:8080/sparklr2/oauth/authorize?response_type=code&client_id=my-trusted-client&redirect_uri=http://localhost:4000/attack.html&scope=read)

> Implicit Grant: client with secret: [click here](http://localhost:8080/sparklr2/oauth/authorize?response_type=code&client_id=my-trusted-client-with-secret&redirect_uri=http://localhost:4000/attack.html&scope=read)

> Sequence diagram, auth-code-csrf:

    {{ sequence }}
    {% include auth-code-csrf.txt %}
    {{ endsequence }}

## Attacks on the Client

Client apps are also open to CSRF attacks, not to steal access tokens, but to change state on the client or (more likely) a Resource Server that the client uses to manage its state.  The provider systems in this case can't prevent the attacks, but they can help the client to implement its own protection.  The principal machanism for this is the state parameter that is generated and managed by the client, and passed through intact by the Authorization Server.

Here is a sequence diagram of the full Authorization Code grant flow with a state parameter.  The Client implements CSRF protection by checking that the state exists in the user's session when he comes back to get the access token.  The state parameter in this design is a key to a session attribute in the authenticated user's session with the Client application.

    {{ sequence }}
    {% include state-flow-sunny-day.txt %}
    {{ endsequence }}

If the key for the state is guessable then a CSRF attack in this example could still succeed, but as long as the session is cleaned up by the client after the token is granted the attack would have to take place during an existing legitimate grant with the same key.

That would be unlucky (and unlikely), but the OAuth2 spec still recommends that clients use unguessable (e.g. with a random component) state.  The spec does not assume that Clients themselves are stateful, which makes that recommendation easier to understand: if the state parameter is not just a key in a session, but carries the whole state itself encoded in some opaque way, then it is more important to make it unguessable.
