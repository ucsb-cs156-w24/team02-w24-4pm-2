package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.RecommendationRequest;
import edu.ucsb.cs156.example.repositories.RecommendationRequestRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.time.LocalDateTime;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = RecommendationRequestController.class)
@Import(TestConfig.class)
public class RecommendationRequestControllerTests extends ControllerTestCase {

    @MockBean
    RecommendationRequestRepository recommendationRequestRepository;

    @MockBean
    UserRepository userRepository;

    // Tests for GET /api/recommendationrequest/all

    @Test
    public void logged_out_users_cannot_get_all() throws Exception {
        mockMvc.perform(get("/api/recommendationrequest/all"))
                .andExpect(status().is(403)); // logged out users can't get all
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_users_can_get_all() throws Exception {
        mockMvc.perform(get("/api/recommendationrequest/all"))
                .andExpect(status().is(200)); // logged
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_user_can_get_all_recommentationrequest() throws Exception {

        // arrange
        LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
        LocalDateTime ldt2 = LocalDateTime.parse("2024-02-04T01:02:03");

        RecommendationRequest recommendationRequest1 = RecommendationRequest.builder()
                .requesterEmail("requester1@gmail.com")
                .professorEmail("professor1@gmail.com")
                .explanation("explanation for request 1")
                .dateRequested(ldt1)
                .dateNeeded(ldt2)
                .done(false)
                .build();

        LocalDateTime ldt3 = LocalDateTime.parse("2022-01-11T00:00:00");
        LocalDateTime ldt4 = LocalDateTime.parse("2024-02-12T01:02:03");

        RecommendationRequest recommendationRequest2 = RecommendationRequest.builder()
                .requesterEmail("requester2@gmail.com")
                .professorEmail("professor2@gmail.com")
                .explanation("explanation for request 2")
                .dateRequested(ldt3)
                .dateNeeded(ldt4)
                .done(true)
                .build();

        ArrayList<RecommendationRequest> expectedRecommendationRequest = new ArrayList<>();
        expectedRecommendationRequest.addAll(Arrays.asList(recommendationRequest1, recommendationRequest2));

        when(recommendationRequestRepository.findAll()).thenReturn(expectedRecommendationRequest);

        // act
        MvcResult response = mockMvc.perform(get("/api/recommendationrequest/all"))
                .andExpect(status().isOk()).andReturn();

        // assert

        verify(recommendationRequestRepository, times(1)).findAll();
        String expectedJson = mapper.writeValueAsString(expectedRecommendationRequest);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }

    // Tests for POST /api/recommendationrequest/post...

    @Test
    public void logged_out_users_cannot_post() throws Exception {
        mockMvc.perform(post("/api/recommendationrequest/post"))
                .andExpect(status().is(403));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_regular_users_cannot_post() throws Exception {
        mockMvc.perform(post("/api/recommendationrequest/post"))
                .andExpect(status().is(403)); // only admins can post
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void an_admin_user_can_post_a_new_recommendationrequest() throws Exception {
        // arrange

        LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
        LocalDateTime ldt2 = LocalDateTime.parse("2024-02-04T01:02:03");

        RecommendationRequest recommendationRequest1 = RecommendationRequest.builder()
                .requesterEmail("requester1@gmail.com")
                .professorEmail("professor1@gmail.com")
                .explanation("explanation for request 1")
                .dateRequested(ldt1)
                .dateNeeded(ldt2)
                .done(false)
                .build();

        when(recommendationRequestRepository.save(eq(recommendationRequest1))).thenReturn(recommendationRequest1);

        // act
        MvcResult response = mockMvc.perform(
                post("/api/recommendationrequest/post?requesterEmail=requester1@gmail.com&professorEmail=professor1@gmail.com&explanation=explanation for request 1&dateRequested=2022-01-03T00:00:00&dateNeeded=2024-02-04T01:02:03")
                        .with(csrf()))
                .andExpect(status().isOk()).andReturn();

        // assert
        verify(recommendationRequestRepository, times(1)).save(recommendationRequest1);
        String expectedJson = mapper.writeValueAsString(recommendationRequest1);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }

    // Tests for GET /api/recommendationrequest?id=...

    @Test
    public void logged_out_users_cannot_get_by_id() throws Exception {
        mockMvc.perform(get("/api/recommendationrequest?id=7"))
                .andExpect(status().is(403)); // logged out users can't get by id
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

        // arrange
        LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
        LocalDateTime ldt2 = LocalDateTime.parse("2024-02-04T01:02:03");

        RecommendationRequest recommendationRequest = RecommendationRequest.builder()
                .requesterEmail("requester1@gmail.com")
                .professorEmail("professor1@gmail.com")
                .explanation("explanation for request 1")
                .dateRequested(ldt1)
                .dateNeeded(ldt2)
                .done(false)
                .build();

        when(recommendationRequestRepository.findById(eq(7L))).thenReturn(Optional.of(recommendationRequest));

        // act
        MvcResult response = mockMvc.perform(get("/api/recommendationrequest?id=7"))
                .andExpect(status().isOk()).andReturn();

        // assert
        verify(recommendationRequestRepository, times(1)).findById(eq(7L));
        String expectedJson = mapper.writeValueAsString(recommendationRequest);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

        // arrange

        when(recommendationRequestRepository.findById(eq(7L))).thenReturn(Optional.empty());

        // act
        MvcResult response = mockMvc.perform(get("/api/recommendationrequest?id=7"))
                .andExpect(status().isNotFound()).andReturn();

        // assert

        verify(recommendationRequestRepository, times(1)).findById(eq(7L));
        Map<String, Object> json = responseToJson(response);
        assertEquals("EntityNotFoundException", json.get("type"));
        assertEquals("RecommendationRequest with id 7 not found", json.get("message"));
    }

    // Tests for DELETE /api/recommendationrequest?id=...

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_can_delete_a_date() throws Exception {
        // arrange

        LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
        LocalDateTime ldt2 = LocalDateTime.parse("2024-02-04T01:02:03");

        RecommendationRequest recommendationRequest = RecommendationRequest.builder()
                .requesterEmail("requester1@gmail.com")
                .professorEmail("professor1@gmail.com")
                .explanation("explanation for request 1")
                .dateRequested(ldt1)
                .dateNeeded(ldt2)
                .done(false)
                .build(); 

        when(recommendationRequestRepository.findById(eq(15L))).thenReturn(Optional.of(recommendationRequest));

        // act
        MvcResult response = mockMvc.perform(
                delete("/api/recommendationrequest?id=15")
                        .with(csrf()))
                .andExpect(status().isOk()).andReturn();

        // assert
        verify(recommendationRequestRepository, times(1)).findById(15L);
        verify(recommendationRequestRepository, times(1)).delete(any());

        Map<String, Object> json = responseToJson(response);
        assertEquals("RecommendationRequest with id 15 deleted", json.get("message"));
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_tries_to_delete_non_existant_recommendationrequest_and_gets_right_error_message()
            throws Exception {
        // arrange

        when(recommendationRequestRepository.findById(eq(15L))).thenReturn(Optional.empty());

        // act
        MvcResult response = mockMvc.perform(
                delete("/api/recommendationrequest?id=15")
                        .with(csrf()))
                .andExpect(status().isNotFound()).andReturn();

        // assert
        verify(recommendationRequestRepository, times(1)).findById(15L);
        Map<String, Object> json = responseToJson(response);
        assertEquals("RecommendationRequest with id 15 not found", json.get("message"));
    }

    // Tests for PUT /api/recommendationrequest?id=...

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_can_edit_an_existing_recommendationrequest() throws Exception {
        // arrange

        LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
        LocalDateTime ldt2 = LocalDateTime.parse("2024-02-04T01:02:03");
        LocalDateTime ldt3 = LocalDateTime.parse("2025-01-11T00:00:00");
        LocalDateTime ldt4 = LocalDateTime.parse("2026-02-12T01:02:03");

        RecommendationRequest recommendationRequestOrig = RecommendationRequest.builder()
                .requesterEmail("requester1@gmail.com")
                .professorEmail("professor1@gmail.com")
                .explanation("explanation for request 1")
                .dateRequested(ldt1)
                .dateNeeded(ldt2)
                .done(false)
                .build();

        RecommendationRequest recommendationRequestEdited = RecommendationRequest.builder()
                .requesterEmail("requester11@gmail.com")
                .professorEmail("professor11@gmail.com")
                .explanation("explanation for request 11")
                .dateRequested(ldt3)
                .dateNeeded(ldt4)
                .done(true)
                .build();

        String requestBody = mapper.writeValueAsString(recommendationRequestEdited);

        when(recommendationRequestRepository.findById(eq(67L))).thenReturn(Optional.of(recommendationRequestOrig));

        // act
        MvcResult response = mockMvc.perform(
                put("/api/recommendationrequest?id=67")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8")
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isOk()).andReturn();

        // assert
        verify(recommendationRequestRepository, times(1)).findById(67L);
        verify(recommendationRequestRepository, times(1)).save(recommendationRequestEdited); // should be saved with correct user
        String responseString = response.getResponse().getContentAsString();
        assertEquals(requestBody, responseString);
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_can_edit_an_existing_recommendationrequest_2() throws Exception {
        // arrange

        LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
        LocalDateTime ldt2 = LocalDateTime.parse("2024-02-04T01:02:03");
        LocalDateTime ldt3 = LocalDateTime.parse("2025-01-11T00:00:00");
        LocalDateTime ldt4 = LocalDateTime.parse("2026-02-12T01:02:03");

        RecommendationRequest recommendationRequestOrig = RecommendationRequest.builder()
                .requesterEmail("requester1@gmail.com")
                .professorEmail("professor1@gmail.com")
                .explanation("explanation for request 1")
                .dateRequested(ldt1)
                .dateNeeded(ldt2)
                .done(false)
                .build();

        RecommendationRequest recommendationRequestEdited = RecommendationRequest.builder()
                .requesterEmail("requester11@gmail.com")
                .professorEmail("professor11@gmail.com")
                .explanation("explanation for request 11")
                .dateRequested(ldt3)
                .dateNeeded(ldt4)
                .done(false)
                .build();

        String requestBody = mapper.writeValueAsString(recommendationRequestEdited);

        when(recommendationRequestRepository.findById(eq(67L))).thenReturn(Optional.of(recommendationRequestOrig));

        // act
        MvcResult response = mockMvc.perform(
                put("/api/recommendationrequest?id=67")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8")
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isOk()).andReturn();

        // assert
        verify(recommendationRequestRepository, times(1)).findById(67L);
        verify(recommendationRequestRepository, times(1)).save(recommendationRequestEdited); // should be saved with correct user
        String responseString = response.getResponse().getContentAsString();
        assertEquals(requestBody, responseString);
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_cannot_edit_recommendationrequest_that_does_not_exist() throws Exception {
        // arrange

        LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
        LocalDateTime ldt2 = LocalDateTime.parse("2024-02-04T01:02:03");

        RecommendationRequest recommendationRequest = RecommendationRequest.builder()
                .requesterEmail("requester1@gmail.com")
                .professorEmail("professor1@gmail.com")
                .explanation("explanation for request 1")
                .dateRequested(ldt1)
                .dateNeeded(ldt2)
                .done(false)
                .build();

        String requestBody = mapper.writeValueAsString(recommendationRequest);

        when(recommendationRequestRepository.findById(eq(67L))).thenReturn(Optional.empty());

        // act
        MvcResult response = mockMvc.perform(
                put("/api/recommendationrequest?id=67")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8")
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isNotFound()).andReturn();

        // assert
        verify(recommendationRequestRepository, times(1)).findById(67L);
        Map<String, Object> json = responseToJson(response);
        assertEquals("RecommendationRequest with id 67 not found", json.get("message"));

    }
}
