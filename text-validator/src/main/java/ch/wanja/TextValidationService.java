package ch.wanja;

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
    public record ValidationRequest(long id, String content) {
    }

    public record ValidationResponse(long id, boolean valid) {
    }

    @FunctionName("validate-text")
    public void validateTextMessages(
            @KafkaTrigger(name = "kafkaTrigger", username = "$ConnectionString", password = "EventHubConnectionString", topic = "blogs", brokerList = "%BrokerList%", consumerGroup = "$Default", authenticationMode = BrokerAuthenticationMode.PLAIN, protocol = BrokerProtocol.SASLSSL) String kafkaEventData,
            @KafkaOutput(name = "kafkaOutput", username = "$ConnectionString", password = "EventHubConnectionString", topic = "validated-blogs", brokerList = "%BrokerList%", authenticationMode = BrokerAuthenticationMode.PLAIN, protocol = BrokerProtocol.SASLSSL) OutputBinding<ValidationResponse> output,
            final ExecutionContext context) {

        System.out.println("======================================");
        System.out.println("Example: ---- >" + kafkaEventData.toString());
        System.out.println("======================================");

        // Extract the content value from the JSON string
        int startIndex = kafkaEventData.indexOf("\"content\":\"") + 13; // Move to the start of the content value
        int endIndex = kafkaEventData.indexOf("\"", startIndex); // Find the closing quotation mark

        String content = kafkaEventData.substring(startIndex, endIndex);

        boolean valid = !content.contains("0");
        System.out.println("Text-Validation: " + content + " -> " + valid);

        ValidationResponse response = new ValidationResponse(1L, valid); // Assuming an ID of 1

        output.setValue(response);
    }
}
