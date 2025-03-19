# Hexagonship

This is a pet project of mine and I use it mainly to try out different concepts.
For the moment the following topics are covered with that project:

* Hexagon Architecture
* Domain Driven Design Approach
* Transactional Outbox with Kafka-Connect/Debezium

There is (always) more to com in the future, course. Therfore this project will grow and grow.

## Impressions
tbd

## How to start it and try it out

There are two ways to startup the project without the need of any development environment.
For starting up the project there are two ways to do that:
1. without the kafka connect environment
2. with the kafka connect environment

### Requirements
The minimum requirement to get the project started is Docker and Docker Compose.

If you want to extend the project on your own, you need:
* Java 17 or higher
* NVM (Node version manager)
* Angular


### Start without Kafka-Connect

In the project, there exists a docker-compose file that solely starts up the application only.
The commmand you need for starting it up is:

```bash
docker compose -f docker-compose.yml -d
```

### Start with together with Kafka-Connect

If you want to start the application together with the whole kafka environment there exist two other docker-compose files for that.
Make sure you start them in the right order, which is:

```bash
docker compose -f docker-compose-kafka.yml -d
docker comopse -f docker-compose-app.yml -d
```

With that all is up and running.

When the application is started up, you can visit it directly under "localhost"

### Ship - Terminal

If you decide to run the application together with the kafka environment, there is a 
little application that consumes outboxed messages from the application.
You can find it under 

* ship-terminal/src/main/kotlin *

## Adjust the Infrastructure for full Experience

### Database Data

Unfortunately there is no Flyway or other DB migraiton tool in action yet. So, when you start the project for the
very first time, you need to add the datarecords manually by executing all *.sql files in

* ./driven-adapter/src/main/resources/sql *

### Cat Images

The images for the cats are also needed to be uploaded manually. The MinIO container startsup together with the application
and is available on *port 9001*.

Username and password are *hexagonminio*

### Add Kafka-Connect configuration files

To get the kafka-messages flowing, you need to upload the outbox configuration file to the Kafka-Connect instance.
This can be done using the helper-script files.

Everything is inside the kafka-connect directory and you can upload the outbox file with:

```bash
kafka-connect/connect-helpers/upload-connector kafka-connect/connectors/ship-outobx-connector.json
```

### Monitor through Kafka UI

You can monitor toipcs, kafka messages, and health state of your kafka connectors using the also running Kafka-UI.
You can visit that page under *port 8005*


