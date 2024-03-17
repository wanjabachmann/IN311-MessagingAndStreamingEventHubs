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

# Event Hub Installation

Reference: https://learn.microsoft.com/en-us/azure/event-hubs/event-hubs-quickstart-powershell

```PowerShell
cd ./blog-backend/mvnw quarkus:add-extension -Dextensions="org.apache.camel.quarkus:camel-quarkus-azure-eventhubs"
```

```PowerShell
$rgName = "rg-blogbackend-prod-switzerlandnorth-001"
$region = "switzerlandnorth"
$namespaceName = "ns-aeh"

# New-AzResourceGroup parameters
$rgParams = @{
    Name     = $rgName
    Location = $region
}

# Create the Azure Resource Group
New-AzResourceGroup @rgParams

# Create an Event Hubs namespace.
az eventhubs namespace create --name $namespaceName --resource-group $rgName -l $region --sku Standard

# collect the connection string
az eventhubs namespace authorization-rule keys list --resource-group $rgName --namespace-name $namespaceName --name RootManageSharedAccessKey --query primaryConnectionString

```

https://learn.microsoft.com/en-us/azure/event-hubs/event-hubs-quickstart-cli

https://quarkus.io/guides/kafka#azure-event-hub

# Azure Functions

##

## Deploy Quarkus Project to Azure Functions

https://quarkus.io/guides/azure-functions

Add Azure function extension:

```PowerShell
cd .\text-validator\
.\mvnw.cmd quarkus:add-extension -Dextensions="io.quarkus:quarkus-azure-functions" -f ".\text-validator\pom.xml"
```

Login to Azure:

```PowerShell
az login
```

Deploy the Quarkus Project:

```PowerShell
./mvnw quarkus:deploy
```
