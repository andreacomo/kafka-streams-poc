# Kafka Stream POC

* Debezium (Kafka Connect + Kafka)
* Kowl (`http://localhost:9001/`) and Connect UI (`http://localhost:9002`)
* Spring Boot Application for 
  * create data
  * stream from Kafka

# Configure Debezium for PostgreSQL

Go to [http://localhost:9002](http://localhost:9002) and create a connection with this parameters:

```
name=PostgresCDC
connector.class=io.debezium.connector.postgresql.PostgresConnector
plugin.name=pgoutput
database.hostname=postgres
database.port=5432
database.user=cdc
database.password=cdc_pwd
database.dbname=poc
database.server.name=postgres
publication.name=cdc-publication
publication.autocreate.mode=disabled
```