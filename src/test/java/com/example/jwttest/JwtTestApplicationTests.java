package com.example.jwttest;

import com.example.jwttest.auth.JwtTokenUtil;
import com.example.jwttest.auth.LoginRequest;
import com.example.jwttest.auth.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class JwtTestApplicationTests {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	final String testEmail = "testEmail4@example.com";
	final String testName = "testName4";
	final String testPassword = "testPassword4";

	@Test
	void registerAndLoginFlow() throws Exception {
		RegisterRequest registerRequest = new RegisterRequest(testEmail, testName, testPassword);
		mockMvc.perform(post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(registerRequest)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value(testEmail))
				.andExpect(jsonPath("$.role").value("CUSTOMER"));

		LoginRequest loginRequest = new LoginRequest(testEmail, testPassword);
		MvcResult result = mockMvc.perform(post("/api/auth/login")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(loginRequest)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").isString())
				.andReturn();

		String jwt = JsonPath.read(result.getResponse().getContentAsString(), "$.token");
		mockMvc.perform(get("/api/user/me")
				.contentType(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value(testEmail))
				.andExpect(jsonPath("$.name").value(testName))
				.andExpect(jsonPath("$.role").value("CUSTOMER"));
	}
}
