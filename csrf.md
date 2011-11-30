---
title: CSRF
layout: default
---

{% include sequence.md %}

# Cross Site Request Forgery and OAuth2

In this short article we look at Cross Site Request Forgery in the context of [OAuth2](http://en.wikipedia.org/wiki/OAuth#OAuth_2.0), looking at possible attacks and how they can be countered when OAuth2 is being used to protect web resources.  

OAuth2 is a protocol enabling a Client application, often a web application, to act on behalf of a User, but with the User's permission.  The actions a Client is allowed to perform are carried out on a Resource Server (another web application or web service), and the User approves the actions by telling an Authorization Server that he trusts the Client to do what it is asking.  Common examples of Authorization Servers on the internet are [Facebook](http://developers.facebook.com/) and [Google](http://code.google.com/apis/accounts/docs/OAuth2.html), both of which also provide Resource Servers (the Graph API in the case of Facebook and the Google APIs in the case of Google).

A [Cross Site Request Forgery](http://en.wikipedia.org/wiki/Cross-site_request_forgery) (CSRF or "sea surf") attack involves a bad guy tricking a user into clicking on a link that changes some state on the target system.  If the user is already authenticated with the target system he might not even notice the attack since the browser will send authentication headers or cookies automatically.

A system that uses OAuth2 to protect resources and delegate permissions is vulnerable to all "normal" CSRF attacks anyway - users authenticate and probably state can be changed.  Here we concentrate on attacks that are specific to the OAuth2 protocol, and in that context the bad guy is going to be trying to get hold of an access token, which would then enable him to do anything the user could do (within the scope of the token).

Some of the defences to CSRF we discuss below come from careful implementation of the OAuth2 Client application, and therefore can be used by any client as long as the authorization server implements the [OAuth2 specification](http://tools.ietf.org/html/draft-ietf-oauth-v2).  Some of the defences are in the OAuth2 Authorization Server itself, so can only be implemented by developers of the server components.  Some are mandated or strongly suggested by the specification and some only come from using the protocol carefully.

Some example CSRF attempts are given below as inline links.  It is not dangerous to click them - it's only a demo and there is no data that can be corrupted or identities that can be revealed.  You can log into the Authorization Server with the credentials it suggests on the login screen (marissa/koala).  The attacks only succeed because the demo system is implemented badly.  You should hope that a real OAuth2 implementer would be more careful.  Probably this is not a vain hope in relation to the Authorization Server, but you can't so certain about the Clients, not that there is anything special about this type of application, but clients are more numerous and have more varied provenances.

## Attacks on the Authorization Server

The bad guy is trying to obtain an access token by tricking the user into clicking on a link to the Authorization Server at a time when the user is already authenticated.  The examples rely on the fact that the Authorization Server might send redirects to any URI you ask it to.  This is allowed by the spec unless a specific redirect has been pre-registered for the client in question.  With registered redirects the attacks are not going to work, but that might be too restrictive for some systems, e.g. Facebook doesn't bother with it but Google does.  Actually Facebook restricts the redirect URLs to be "owned" by a pre-registered application (i.e. start with the same host, path, etc.) which is pretty sane, but not part of the spec.

To try out the attacks you need an Authorization Server, and then you can just click on the links and see what secrets are revealed in the redirect.  There is a simple script in the redirect that captures the browser window location and mirrors it to demonstrate how the bad guy could strip the information he needs and send it somewhere else.  So the attack succeeds if you see some secret information, like an access token in your browser.  That's just a demo.  A real bad guy would take the access token and use it to do evil things on your behalf (or on mariss'a behalf in the demo). The server should be running at [http://oademo.cloudfoundry.com](http://oademo.cloudfoundry.com) - if it's not then the implementation is the `sparklr2` application from [Spring Security OAuth](http://github.com/SpringSource/spring-security-oauth) and you can run it locally if you change the links in the examples.

### Implicit Grant Attacks

Implicit Grant attacks are arguably the nastiest because the bad guy gets a token without having to do any more work.  He does, however, need to know the client secret, as well as a valid client id. 

> So this attack succeeds.  Implicit Grant: client without secret [click here](http://oademo.cloudfoundry.com/oauth/authorize?response_type=token&client_id=my-trusted-client&redirect_uri=http://dsyer.github.com/spring-security-oauth/attack.html&scope=read)

> But this attack fails.  Implicit Grant: client with secret: [click here](http://oademo.cloudfoundry.com/oauth/authorize?response_type=token&client_id=my-trusted-client-with-secret&redirect_uri=http://dsyer.github.com/spring-security-oauth/attack.html&scope=read)

### Authorization Code Attacks

Here the basic Authorization Code grant flow to remind us of the
details before we look at the attacks on it:

> Sequence diagram, auth-code-flow:

    {{ sequence }}
    {% include auth-code-flow.txt %}
    {{ endsequence }}

Authorization code attacks allow the bad guy to steal an authorization code which can then be exchanged for a token.  The bad guy can get an authorization code independent of client secrets, but to use it he will need the secret.  So both of these links send us back a valid code, but one of them is protected by a client secret and can't be used to impersonate a user without the secret:

> Authorization Code Grant: client without secret [click here](http://oademo.cloudfoundry.com/oauth/authorize?response_type=code&client_id=my-trusted-client&redirect_uri=http://dsyer.github.com/spring-security-oauth/attack.html&scope=read)

> Authorization Code Grant: client with secret: [click here](http://oademo.cloudfoundry.com/oauth/authorize?response_type=code&client_id=my-trusted-client-with-secret&redirect_uri=http://dsyer.github.com/spring-security-oauth/attack.html&scope=read)

> Sequence diagram, auth-code-csrf:

    {{ sequence }}
    {% include auth-code-csrf.txt %}
    {{ endsequence }}

## Attacks on the Client

Client apps are also open to CSRF attacks, not to steal access tokens, but to change state on the client or (more likely) a Resource Server that the client uses to manage its state.  The provider systems in this case can't prevent the attacks, but they can help the client to implement its own protection.  The principal mechanism for this is the state parameter that is generated and managed by the client, and passed through intact by the Authorization Server.

Here is a sequence diagram of the full Authorization Code grant flow with a state parameter.  The Client implements CSRF protection by checking that the state exists in the user's session when he comes back to get the access token.  The state parameter in this design is a key to a session attribute in the authenticated user's session with the Client application.

    {{ sequence }}
    {% include state-flow-sunny-day.txt %}
    {{ endsequence }}

If the key for the state is guessable then a CSRF attack in this example could still succeed, but as long as the session is cleaned up by the client after the token is granted the attack would have to take place during an existing legitimate grant with the same key.

That would be unlucky (and unlikely), but the OAuth2 spec still recommends that Clients use unguessable (e.g. with a random component) state.  The spec does not assume that Clients themselves are stateful, which makes that recommendation easier to understand: if the state parameter is not just a key in a session, but carries the whole state itself encoded in some opaque way, then it is more important to make it unguessable.

## Defence Measures

Here is a summary of the possible defence measures that can be used against the attacks above.  All of them can be implemented in a system built with [Spring Security](http://git.springsource.org/spring-security/spring-security) and [Spring Security OAuth](http://github.com/SpringSource/spring-security-oauth) but not many are enforced automatically - as per usual with developer frameworks, the developer has to choose to use the features provided.

### For the Client
* Use a client secret
* Use a registered redirect, either fixed or variable if the authorization server allows it (see below)
* Send a random state parameter value to the authorization endpoint and store something in the user's session with the same key
* Only send requests to the authorization endpoint with a state parameter
* Check the user's session for the state when an authorization code request arrives
* If the client is stateless (no session) encode something about the user in the state key itself and compare that instead

### For the Authorization Server
* Always use SSL so that secrets and tokens cannot be sniffed by casual observers
* Require clients to register with a secret, and maybe also provide stronger secret management features
* Do not expose client password grants to dynamically registered clients
* Require client authentication via headers, not via form parameters.  This makes it quite hard to trick a user into a CSRF link since they would need a script, and probably same-origin policies would prevent the attack in a regular browser.
* Fixed redirects: only redirect to a fixed URL registered by the client, or
* Variable redirects: allow user redirects, but require clients to register a website address and force all redirects to be hosted there

## Conclusions

We've taken a look at some CSRF attacks on an OAuth2 system and some measures that can be taken to defend against them.  The general conclusion is that there are plenty of opportunities to defeat such attacks, some of which come from the specification and come which do not.  As with any security vulnerability, whether or not a system is well defended against CSRF depends on the details of the implementation as well as the quality of passwords and secrets.  Even a system which meets the specification can be attacked, but there are some measures that can be taken by careful implementations to make those attacks unlikely to succeed.
