---
title: CSRF
layout: default
---

{% include sequence.md %}

Some Example CSRF attempts follow.  All of them rely on the fact that the authorization server will send redirects to any URI you ask it to.  With registered redirects they are not going to work.

## Implicit Grant Attacks

Implicit Grant attacks are the nastiest because the bad guy gets a token without having to do any more work.  He does, however, need to know the client secret, as well as a valid client id. So this attack succeeds:

> Implicit Grant: client without secret [click here](http://localhost:8080/sparklr2/oauth/authorize?response_type=token&state=mystateid&client_id=my-trusted-client&redirect_uri=http://localhost:4000/attack.html&scope=read)

But this attack fails:

> Implicit Grant: client with secret: [click here](http://localhost:8080/sparklr2/oauth/authorize?response_type=token&state=mystateid&client_id=my-trusted-client-with-secret&redirect_uri=http://localhost:4000/attack.html&scope=read)

## Authorization Code Attacks

Here the basic Authorization Code grant flow to remind us of the
details before we look at the attacks on it:

> Sequence diagram, auth-code-flow:

    {{ sequence }}
    {% include auth-code-flow.txt %}
    {{ endsequence }}

Authorization code attacks allow the bad guy to steal an authorization code which can then be exchanged for a token.  The bad guy can get a an authorization code independent of client secrets, to use it he will need the secret.  So both of these links send us back a valid code, but one of them is protected by a client secret:

> Authorization Code Grant: client without secret [click here](http://localhost:8080/sparklr2/oauth/authorize?response_type=code&state=mystateid&client_id=my-trusted-client&redirect_uri=http://localhost:4000/attack.html&scope=read)

> Implicit Grant: client with secret: [click here](http://localhost:8080/sparklr2/oauth/authorize?response_type=code&state=mystateid&client_id=my-trusted-client-with-secret&redirect_uri=http://localhost:4000/attack.html&scope=read)

> Sequence diagram, auth-code-csrf:

    {{ sequence }}
    {% include auth-code-csrf.txt %}
    {{ endsequence }}
