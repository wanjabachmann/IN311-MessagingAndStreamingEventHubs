# Blog Validation

This project quickly simulates a quarkus blog backend and a quarkus validation service.
It processes the blog content and determines whether it is valid or not.
The project uses Kafka as streaming plattform.

# ⚒️ Docker Container Environment

## 🏃🏻‍♂️Run the prod environment:

💨 Start the environment:

```Shell
docker-compose up -d
```

🤚🏻 Stop the environment:

```Shell
docker-compose down
```

🔗 Access to Swagger UI in production:<br>
[http://localhost:8080/q/swagger-ui/](http://localhost:8080/q/swagger-ui/)

The container images are available here:
https://github.com/wanjabachmann?tab=packages

## 🧑🏻‍💻 Start the project in dev mode

```PowerShell
cd .\blog-backend\
.\mvnw compile quarkus:dev
```

```PowerShell
cd .\text-validator\
.\mvnw compile quarkus:dev
```

🔗 Access to Swagger UI in development:<br>
[http://localhost:8080/q/swagger-ui/#/](http://localhost:8080/q/swagger-ui/#/)

🔗 Access to Kafka Topics in development:<br>
[http://localhost:8080/q/dev-ui/io.quarkus.quarkus-kafka-client/topics](http://localhost:8080/q/dev-ui/io.quarkus.quarkus-kafka-client/topics)

## Example Valid Post

### POST → content contains a 0

SwaggerUi:

```JSON
{
    "title": "Title of the Blog",
    "content": "Is this blog valid? 0"
}
```

httpie:

```JSON
http -v POST :8080/blogs title="Title of the Blog" content="Is this blog valid? 0"
```

### GET Request

SwaggerUi:

```JSON
{
    "content": "Is this blog valid? 0",
    "id": 1,
    "title": "Title of the Blog",
    "valid": false,
    "validationDate": "2024-03-01"
}
```

## Example Not Valid

### POST → content **doesn't** contain a 0

SwaggerUi:

```JSON
{
    "title": "Title",
    "content": "Is this blog valid?"
}
```

httpie:

```JSON
http -v POST :8080/blogs title="Title of the Blog" content="Is this blog valid?"
```

### Get Request

SwaggerUi:

```JSON
{
    "content": "Is this blog valid?",
    "id": 2,
    "title": "Title",
    "valid": true,
    "validationDate": "2024-02-27"
}
```

httpie:

```JSON
http -v GET :8080/blogs
```

```PowerShell
./mvnw quarkus:add-extension -Dextensions="org.apache.camel.quarkus:camel-quarkus-azure-eventhubs"
```
