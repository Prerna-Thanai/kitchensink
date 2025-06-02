package com.kitchensink.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kitchensink.api.TestController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
class ExceptionAdviceControllerTest {

    private MockMvc mockMvc;
    @InjectMocks
    private TestController testController;


    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders
                .standaloneSetup(testController)     // instantiate controller.
                .setControllerAdvice(new ExceptionAdvice(new ObjectMapper()))   // bind with controller advice.
                .build();
    }
    @Test
    void testBaseApplicationException() throws Exception {
        mockMvc.perform(get("/base-exception"))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.message").value("Base error"))
               .andExpect(jsonPath("$.errorType").value("UNKNOWN"))
               .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void testBadCredentialsException() throws Exception {
        mockMvc.perform(get("/bad-credentials"))
               .andExpect(status().isUnauthorized())
               .andExpect(jsonPath("$.message").value("Invalid email id or password"))
               .andExpect(jsonPath("$.errorType").value("INVALID_CREDENTIALS"))
               .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void testUsernameNotFoundException() throws Exception {
        mockMvc.perform(get("/username-not-found"))
               .andExpect(status().isUnauthorized())
               .andExpect(jsonPath("$.message").value("Invalid email id or password"))
               .andExpect(jsonPath("$.errorType").value("INVALID_CREDENTIALS"))
               .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void testUnhandledException() throws Exception {
        mockMvc.perform(get("/uncaught"))
               .andExpect(status().isInternalServerError())
               .andExpect(jsonPath("$.message").value(containsString("Unable to process the request")))
               .andExpect(jsonPath("$.errorType").value("UNKNOWN"))
               .andExpect(jsonPath("$.status").value(500));
    }

    @Test
    void testInvalidMethodException() throws Exception {
        mockMvc.perform(get("/test-login"))
               .andExpect(status().isMethodNotAllowed())
               .andExpect(jsonPath("$.message").value(containsString("Request Type not supported")))
               .andExpect(jsonPath("$.errorType").value("REQUEST_VALIDATION_FAILED"))
               .andExpect(jsonPath("$.status").value(405));
    }

    @Test
    void testBindException() throws Exception {
        mockMvc.perform(post("/test-login")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content("{\"email\":\"testEmail@gmail.com\"}"))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.message").value(containsString("Password should not be empty")))
               .andExpect(jsonPath("$.errorType").value("REQUEST_VALIDATION_FAILED"))
               .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void testRequestBodyMissingException() throws Exception {
        mockMvc.perform(post("/test-login")
                       .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.message").value(containsString("Required request body is missing")))
               .andExpect(jsonPath("$.errorType").value("REQUEST_VALIDATION_FAILED"))
               .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void testHttpMessageNotReadableException() throws Exception {
        mockMvc.perform(post("/test-login")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content("{invalidJson}"))  // Invalid JSON to trigger exception
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.message").value(containsString("Unable to read body")))
               .andExpect(jsonPath("$.errorType").value("REQUEST_VALIDATION_FAILED"))
               .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void testInvalidFieldTypeInRequestException() throws Exception {
        mockMvc.perform(post("/test-login")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content("{\"email\":[\"testEmail@gmail.com\"]}"))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.message").value(containsString("Unable to parse email field")))
               .andExpect(jsonPath("$.errorType").value("REQUEST_VALIDATION_FAILED"))
               .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void testInvalidContentTypeException() throws Exception {
        mockMvc.perform(post("/test-login")
                       .contentType(MediaType.APPLICATION_ATOM_XML)
                       .content("{\"email\":[\"testEmail@gmail.com\"]}"))
               .andExpect(status().isUnsupportedMediaType())
               .andExpect(jsonPath("$.message").value(containsString("Unsupported Media Type")))
               .andExpect(jsonPath("$.errorType").value("REQUEST_VALIDATION_FAILED"))
               .andExpect(jsonPath("$.status").value(415));
    }


    @Test
    void testMissingParamException() throws Exception {
        mockMvc.perform(get("/test-param")
                       .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.message").value(containsString("Required parameter 'param' is not present")))
               .andExpect(jsonPath("$.errorType").value("REQUEST_VALIDATION_FAILED"))
               .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void testMismatchedParamException() throws Exception {
        mockMvc.perform(get("/test-mismatch?param=<boolean>")
                       .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.message").value(containsString("Invalid param value: <boolean>")))
               .andExpect(jsonPath("$.errorType").value("REQUEST_VALIDATION_FAILED"))
               .andExpect(jsonPath("$.status").value(400));
    }
}
