# TP 7 — Cache de second niveau, problème N+1 et optimisation des requêtes Hibernate

## 1. Configuration du cache : persistence.xml et ehcache.xml

![Configuration du cache](screenshots/01-configuration-cache.png)

## 2. Entités Auteur et Livre : @Cache et @NamedEntityGraph

![Entités](screenshots/02-entites-cache.png)

## 3. Service de mesure : N+1, JOIN FETCH, Entity Graph, cache

![Service de performance](screenshots/03-service-requetes.png)

## 4. Exécution — problème N+1 : 11 requêtes SQL sans cache

![Exécution sans cache](screenshots/04-execution-sans-cache.png)

## 5. Exécution — cache de second niveau : 0 requête SQL en lecture 2 et 3

![Exécution avec cache](screenshots/05-execution-avec-cache.png)
