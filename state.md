---
title: State Flow
layout: default
---

{% include sequence.md %}

# OAuth2 State

The [OAuth2 spec](http://tools.ietf.org/html/draft-ietf-oauth-v2)
makes state optional, but recommended, for the Authorization Code
grant type.  It mentions this as a protection against
[cross site request forgery (CSRF)](csrf.html), but is not very
specific about how this should happen.  In particular it is not clear
what role the Authorization Server has in CSRF protection.

> Sequence diagram, state-flow-sunny-day:

    {{ sequence }}
    {% include state-flow-sunny-day.txt %}
    {{ endsequence }}

