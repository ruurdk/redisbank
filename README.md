# About Redisbank

This application uses *Redis Stack* combining Redis core data structures, Streams, RediSearch and TimeSeries to build a
Java/Spring Boot/Lettuce application that shows a searchable transaction overview with realtime updates
as well as a personal finance management overview with realtime balance and biggest spenders updates. UI in Bootstrap/CSS/Vue.

Features in *Redisbank*:

- Redis Streams for the realtime transactions
- RedisJSON for storing transactions
- Sorted Sets for the 'biggest spenders'
- Redis TimeSeries for the balance over time
- RediSearch for searching transactions
- Redis hashes for http session storage via Spring Session

*Important note*
When using this forked repository and the `basic`(https://github.com/alexvasseur/redisbank/tree/basic) branch  
- The application is using the Spring framework in a very basic way only for the web/REST API and for running as standalone executable jar
- The codebase is using plain Lettuce API for Redis.
- The data model for BankTransaction is JSON with RedisJSON (instead of basic Redis Hashes)
(You may want to check the more complex version of the application in the upstream if you are familiar with Spring Data and Srping Data Repository)

# Architecture
<img src="architecture.png"/>

# Getting Started

## Prerequisites

1. JDK 17 or higher (https://openjdk.java.net/install/index.html)
2. Docker Desktop (https://www.docker.com/products/docker-desktop), or Colima with a docker/k8s/containerd runtime

## Running locally

1. Checkout the project
2. `docker-compose.sh up`
3. Navigate to RedisBank at [localhost:8080](http://localhost:8080) and login with user `lars` and password `larsje`
4. Navigate to *Redis Insight* at [localhost:8001](http://localhost:8001)
5. Stop and clean with `docker-compose down -v --rmi local --remove-orphans`

## Building and running your own

You can also build and run your own locally with sensible defaults:
1. Run *Redis Stack* with *Redis Insight* embedded in a container
```
docker run --name redis-stack --rm -p 6379:6379 -p 8001:8001 redis/redis-stack:latest
```
2. Build and run the Redisbank Java application with the provided maven build
```
./mvnw spring-boot:run
```
3. Access *Redisbank* and *Redis Insight* as instructed above

## Building and Running in Gitpod

Gitpod can spin up a fully featured developer friendly environment with both Visual Studio and Redisbank running for you.
[![Open in Gitpod](https://gitpod.io/button/open-in-gitpod.svg)](https://gitpod.io/#https://github.com/alexvasseur/redisbank/tree/basic)

# Interesting code to explore

- [BankTransaction](https://github.com/alexvasseur/redisbank/blob/basic/src/main/java/com/redislabs/demos/redisbank/transactions/BankTransaction.java) is a plain POJO
- Stored in Redis using RedisJSON Lettuce [redis.jsonSet](https://github.com/alexvasseur/redisbank/blob/442905b1c47bf045a12f288d4af932740e5a0b51/src/main/java/com/redislabs/demos/redisbank/transactions/BankTransactionForwarder.java#L65)  
- With an index in RediSearch [BankTransactionGenerator](https://github.com/alexvasseur/redisbank/blob/442905b1c47bf045a12f288d4af932740e5a0b51/src/main/java/com/redislabs/demos/redisbank/transactions/BankTransactionGenerator.java#L87)
- and with RediSearch query thru a REST/JSON api [TransactionOverviewController](https://github.com/alexvasseur/redisbank/blob/442905b1c47bf045a12f288d4af932740e5a0b51/src/main/java/com/redislabs/demos/redisbank/transactions/TransactionOverviewController.java#L99)

and also
- biggest spenders categories using Redis sorted set [redis.incrementScore](https://github.com/alexvasseur/redisbank/blob/442905b1c47bf045a12f288d4af932740e5a0b51/src/main/java/com/redislabs/demos/redisbank/transactions/BankTransactionGenerator.java#L162)
- accessed using a Redis sorted set range [redis.rangeByScoreWithScores](https://github.com/alexvasseur/redisbank/blob/442905b1c47bf045a12f288d4af932740e5a0b51/src/main/java/com/redislabs/demos/redisbank/transactions/TransactionOverviewController.java#L81)

## Known limitations

1. Thread safety. Data is currently generated off of a single stream of transactions, which means it's the same for all users. Not a problem with the current iteration because it's single user, but beware when expanding this to multi-user.
2. Hardcoded values. Code uses hardcoded values throughout the code, these need to be replaced with proper variables.
