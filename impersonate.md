---
title: Impersonate
layout: default
---

{% include sequence.md %}

## Client Impersonation

A Bad Client can impersonate a Good Client to obtain an access token,
but only if a) there is no pre-registered redirect and b) he knows the
good client's secret.

> Sequence diagram, bad-client:

    {{ sequence }}
    {% include bad-client.txt %}
    {{ endsequence }}

