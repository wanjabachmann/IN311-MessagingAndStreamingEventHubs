# Blog Validation

This project simulates a quarkus blog backend and a Quarkus validation service. <br>
It processes the blog content and determines whether the content is valid or not. <br>
The project uses Azure Event Hub as streaming plattform. <br>
The Validation Service is running as an Azure Function. <br>

Below a short overview:

```mermaid
graph LR
    subgraph "User"
        UA((🤓 User))
    end
    subgraph "Quarkus Blog Backend"
        subgraph "API"
            A((HTTP API))
        end
        subgraph "Business Logic"
            B{Quarkus Application}
        end
        subgraph "Data"
            C{{MySQL Database}}
        end
    end
    subgraph "Event Hub"
        D((☁️ Azure Event Hub))
    end
    subgraph "Azure Function"
        E[☁️ Azure Function for Blog Validation]
    end

    UA <-->| 1. HTTP Request | A
    A <-->| 2. Receive Request | B
    B -->| 4. Send Blog content to Event Hub | D
    D -->| 5. Blog Ready to Process | E
    E -->| 6. Validate & Add Metadata | B
    B <-->| 3. Add Blog to DB | C
    B <-->| 7. Update Blog on the DB | C

```

The aim of the project is to get a feeling for the use of Azure Eventhub and Azure Functions.

# Azure Event Hub Installation

Add the Quarkus Extension to the blog backend:

```PowerShell
cd ./blog-backend/mvnw quarkus:add-extension -Dextensions="org.apache.camel.quarkus:camel-quarkus-azure-eventhubs"
```

Create the Azure Event Hub Ressources

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
az eventhubs namespace create `
    --name $namespaceName --resource-group $rgName -l $region --sku Standard

# collect the connection string
az eventhubs namespace authorization-rule keys list `
    --resource-group $rgName --namespace-name $namespaceName
    --name RootManageSharedAccessKey --query primaryConnectionString

```

Docs:

- [https://learn.microsoft.com/en-us/azure/event-hubs/event-hubs-quickstart-cli](https://learn.microsoft.com/en-us/azure/event-hubs/event-hubs-quickstart-cli)<br>
- [https://quarkus.io/guides/kafka#azure-event-hub](https://quarkus.io/guides/kafka#azure-event-hub)

# Azure Functions

Prerequisites are listed here: [https://quarkus.io/guides/azure-functions#prerequisites](https://quarkus.io/guides/azure-functions#prerequisites)

fyi: In case you want to create a Quarkus project with Azure function extension:

```PowerShell
quarkus create app --extension=quarkus-azure-functions
```

> ⚠️ I don't recommend adding the extension to an existing Quarkus project. Because at least the local.settings.json is missing.

[https://quarkus.io/guides/azure-functions](https://quarkus.io/guides/azure-functions)

# ⚒️ Docker Container Environment

## 🏃🏻‍♂️Run the prod environment:

➕ Create two .env files with the following parameters: (used by the docker compose file)<br>
`blog-backend/.env`

```JSON
PASSWORD=Endpoint=sb://xxx.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=xxxx
KAFKA_BOOTSTRAP_SERVER=xxx.servicebus.windows.net:9093
```

`text-validator/.env`

```JSON
PASSWORD="Endpoint=sb://xxxx.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=xxxxx"
AZURE_TENANT_ID=xxx-xxx-xxx-xxx-xxx
AZURE_SUBSCRIPTION_ID=xxx-xxx-xxx-xxx-xxx

```

💨 Start the environment:

```Shell
# Or without -d to see the requests/repsonses in the console
docker-compose up -d
```

🤚🏻 Stop the environment:

```Shell
docker-compose down
```

The container images are available here:
https://github.com/wanjabachmann?tab=packages

# 🧪 Local Testing

## 💨 Start the project

Run the "Azure" Function locally: <br>
A cool option is to test the whole thing locally using the Azure Core Tools. Make sure, that you have the needed local.settings.json in place. (Check: Azure Functions)

```PowerShell
cd .\text-validator\
.\mvnw clean install && .\mvnw quarkus:run`
```

Start Blog Backend locally:

```PowerShell
cd .\blog-backend\
.\mvnw compile quarkus:dev
```

## 🧑🏻‍💻 Start the project in dev mode

🔗 Access to Swagger UI in development:<br>
[http://localhost:8080/q/swagger-ui/#/](http://localhost:8080/q/swagger-ui/#/)

🔗 Access to Kafka Topics in development:<br>
[http://localhost:8080/q/dev-ui/io.quarkus.quarkus-kafka-client/topics](http://localhost:8080/q/dev-ui/io.quarkus.quarkus-kafka-client/topics)

# 🚀 Deploy the Quarkus Azure function

Set the variables for the BrokerList & the EventHubConnectionString
![Azure Variables](doc\FunctionVariables.png)

You can then update the local.settings.json file with the following command:

```PowerShell
# Login to Azure:
az login
# Fetch local.settings.json variables with Azure App settings variables
func azure functionapp fetch-app-settings app-quarkus-text-validator
```

The local.settings.json should now look like this:

```JSON
{
  "IsEncrypted": false,
  "Values": {
    "AzureWebJobsStorage": "DefaultEndpointsProtocol=https;AccountName=xxxx;AccountKey=xxxx/xxxx==;EndpointSuffix=core.windows.net",
    "FUNCTIONS_WORKER_RUNTIME": "java",
    "BrokerList": "xxxx.servicebus.windows.net:9093",
    "EventHubConnectionString": "Endpoint=sb://xxxx.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=xxxx",
    "inboundTopic": "blogs",
    "outboundTopic": "validated-blogs",
    "FUNCTIONS_EXTENSION_VERSION": "~4",
    "AzureWebJobsDashboard": "DefaultEndpointsProtocol=https;AccountName=xxxx;AccountKey=xxxx/xxxx==;EndpointSuffix=core.windows.net",
    "APPINSIGHTS_INSTRUMENTATIONKEY": "xxxx",
    "WEBSITE_RUN_FROM_PACKAGE": "https://xxxx.blob.core.windows.net/java-functions-run-from-packages/subscriptions-xxxx-resourceGroups-rg-azf-quarkus-002-providers-Microsoft.Web-sites-app-quarkus-text-validator-azure-xxxx.zip?sig=b3C1rp%2B7i3ddgTjbQxtLjwYQSx35o0MfLhSKR29a69o%3D&st=2024-03-22T13%3A35%3A11Z&se=2034-03-22T13%3A40%3A11Z&sv=2019-02-02&sp=r&sr=b"
  },
  "ConnectionStrings": {}
}
```

Kudos to [@langeneggerma] for the help with the variable troubleshooting. 🙂

Deploy the Quarkus Project as an Azure Function:

```PowerShell
# Login to Azure:
az login
cd .\text-validator\
./mvnw clean package quarkus:deploy
```

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

# 🧪 Testing

🔗 Access to Swagger UI in production:<br>
[http://localhost:8080/q/swagger-ui/](http://localhost:8080/q/swagger-ui/)

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
