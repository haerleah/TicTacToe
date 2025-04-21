# TicTacToe web - application

## Description

The project is a web application based on Spring MVC (without using Spring Boot).\
The project implements registration and Bearer authorization using Spring Security.\
JSON Web Token (JWT) is used as tokens for Bearer authorization.\
A token rotation mechanism has been implemented.
Each user receives a pair of tokens (refresh token and access token) upon authorization. The access token is reusable, but has a short lifespan (15 minutes), while the refresh token is disposable and has a long lifespan (24 hours).\
Storage of user and game data is carried out via Spring Data JPA + Hibernate.
The PostgreSQL database is used for storage.

## Build

Edit Database name, Database user and Database user password in ```docker-compose.yml``` and ```application.properties```\
Then:\
```$ gradle build```\
```$ docker-compose up --build```


Application will be accessible at ```http://localhost:8080/```\
Database will be accessible at ```localhost:15432```
