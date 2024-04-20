package com.backend.admin_server.access_requests.service;

import com.backend.admin_server.access_requests.dto.AccessRequestDTO;
import com.backend.admin_server.access_requests.enums.ApprovalStatusEnums;
import com.backend.admin_server.access_requests.model.AccessRequestModel;
import com.backend.admin_server.access_requests.repository.AccessRequestRepository;
import com.backend.admin_server.user_data.model.UserModel;
import com.backend.admin_server.user_data.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

@Service
public class AccessRequestValidationService {
    private final RestTemplate restTemplate;
    private final AccessRequestRepository accessRequestRepository;
    private final String externalApiUrl;
    private final UserRepository userRepository;
    private static final Logger LOGGER = Logger.getLogger(AccessRequestValidationService.class.getName());


    @Autowired
    public AccessRequestValidationService(RestTemplate restTemplate,
                                          AccessRequestRepository accessRequestRepository,
                                          @Value("${external.api.url}") String externalApiUrl,
                                          UserRepository userRepository) {
        this.restTemplate = restTemplate;
        this.accessRequestRepository = accessRequestRepository;
        this.externalApiUrl = externalApiUrl;
        this.userRepository = userRepository;
    }

    public AccessRequestDTO processAccessRequest(AccessRequestDTO requestDTO) {
        try {
            LOGGER.info("Processing access request for user: " + requestDTO.getUserId());

            String userBase64Image = retrieveUserImage(requestDTO.getUserId());
            LOGGER.info("Retrieved user image for verification");

            LOGGER.info("Sending for external verification");
            boolean verificationResult = sendForExternalVerification(requestDTO.getBase64Image(), userBase64Image);
            ApprovalStatusEnums status = mapVerificationResultToStatus(verificationResult);

            AccessRequestModel model = createRequestModel(requestDTO, status);
            AccessRequestModel savedModel = accessRequestRepository.save(model);

            requestDTO.setTtl(savedModel.getTtl());
            requestDTO.setRequestId(savedModel.getRequestId());
            requestDTO.setState(savedModel.getState());
            requestDTO.setDate(savedModel.getDate());
            requestDTO.setApprovalStatus(savedModel.getApprovalStatus());
            return requestDTO;

        } catch (Exception e) {
            LOGGER.severe("Exception in processing access request: " + e.getMessage());
            throw new RuntimeException("Failed to process access request", e);
        }
    }

    private String retrieveUserImage(Integer userId) {
        UserModel user = userRepository.findByUserId(userId);
        LOGGER.info("Retrieved image for user ID " + userId + ": " + (user != null ? "Image found" : "No image found"));
        return user != null ? user.getUserImage() : null;
    }

    private AccessRequestModel createRequestModel(AccessRequestDTO dto, ApprovalStatusEnums status) {
        AccessRequestModel model = new AccessRequestModel();
        model.setUserId(dto.getUserId());
        model.setBase64Image(dto.getBase64Image());

        String dateString = ZonedDateTime.now().format(DateTimeFormatter.ISO_DATE);
        model.setDate(dateString);

        model.setApprovalStatus(status);
        return model;
    }

    private boolean sendForExternalVerification(String clientBase64Image, String userBase64Image) {
        try {
            LOGGER.info("Client Base64 Image: " + (clientBase64Image != null ? "Present" : "Null"));
            LOGGER.info("User Base64 Image: " + (userBase64Image != null ? "Present" : "Null"));
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String jsonBody = String.format("{\"captured\": \"%s\", \"reference\": \"%s\"}",
                    clientBase64Image, userBase64Image);

            HttpEntity<String> request = new HttpEntity<>(jsonBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(externalApiUrl, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                String responseBody = response.getBody();
                if (responseBody != null) {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode root = mapper.readTree(responseBody);
                    JsonNode data = root.get("data");
                    if (data != null) {
                        return data.get("verified").asBoolean();
                    }
                }
            }
            return false;
        } catch (Exception e) {
            LOGGER.severe("Exception while sending request for external verification: " + e.getMessage());
            return false;
        }
    }

    private ApprovalStatusEnums mapVerificationResultToStatus(boolean result) {
        return result ? ApprovalStatusEnums.APPROVED : ApprovalStatusEnums.DENIED;
    }
}
