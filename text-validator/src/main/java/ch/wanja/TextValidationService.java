package ch.wanja;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.BrokerAuthenticationMode;
import com.microsoft.azure.functions.BrokerProtocol;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.OutputBinding;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.KafkaOutput;
import com.microsoft.azure.functions.annotation.KafkaTrigger;

/**
 * Azure Functions with HTTP Trigger integrated with Quarkus
 */
public class TextValidationService {

    public record ValidationResponse(long id, boolean valid) {
    }

    @FunctionName("validate-text")
    public void validateTextMessages(
            @KafkaTrigger(name = "kafkaTrigger", username = "$ConnectionString", password = "EventHubConnectionString", topic = "blogs", brokerList = "%BrokerList%", consumerGroup = "$Default", authenticationMode = BrokerAuthenticationMode.PLAIN, protocol = BrokerProtocol.SASLSSL) Map<String, Object> kafkaEventData,
            @KafkaOutput(name = "kafkaOutput", username = "$ConnectionString", password = "EventHubConnectionString", topic = "validated-blogs", brokerList = "%BrokerList%", authenticationMode = BrokerAuthenticationMode.PLAIN, protocol = BrokerProtocol.SASLSSL) OutputBinding<ValidationResponse> output,
            final ExecutionContext context) {

        // Parse the JSON string from parameter
        String valueString = (String) kafkaEventData.get("Value");
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> valueObject;
        try {
            valueObject = mapper.readValue(valueString, Map.class);
        } catch (Exception e) {
            System.err.println("Error parsing JSON: " + e.getMessage());
            return;
        }

        System.out.println("======================================");
        System.out.println("Data: " + valueObject.toString());
        System.out.println("======================================");

        // Extract content and handle potential missing id
        String content = (String) valueObject.get("content");
        Long id = ((Integer) valueObject.get("id")).longValue();

        boolean valid = !content.contains("0");

        System.out.println("======================================");
        System.out.println("Text-Validation: id " + id + " -> content " + content + " -> valid " + valid);
        System.out.println("======================================");

        ValidationResponse response = new ValidationResponse(id, valid);

        output.setValue(response);
    }
}
