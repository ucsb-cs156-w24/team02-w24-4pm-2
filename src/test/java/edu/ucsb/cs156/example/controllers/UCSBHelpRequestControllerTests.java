package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBDate;
import edu.ucsb.cs156.example.entities.UCSBHelpRequest;
import edu.ucsb.cs156.example.repositories.UCSBDateRepository;
import edu.ucsb.cs156.example.repositories.UCSBHelpRequestRepository;

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

@WebMvcTest(controllers = UCSBHelpRequestController.class)
@Import(TestConfig.class)
public class UCSBHelpRequestControllerTests extends ControllerTestCase {

        @MockBean
        UCSBHelpRequestRepository ucsbHelpRequestRepository;

        @MockBean
        UserRepository userRepository;

        // Tests for GET /api/ucsbhelprequest/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/ucsbhelprequest/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/ucsbhelprequest/all"))
                                .andExpect(status().is(200)); // logged
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_ucsbhelprequests() throws Exception {

                // arrange
                LocalDateTime ldt1 = LocalDateTime.parse("2022-04-20T17:35");

                UCSBHelpRequest ucsbHelpRequest1 = UCSBHelpRequest.builder()
                                .requesterEmail("cgaucho@ucsb.edu")
                                .teamId("s22-5pm-3")
                                .tableOrBreakoutRoom("7")
                                .explanation("Need help with Swagger-ui")
                                .solved(false)
                                .requestTime(ldt1)
                                .build();

                LocalDateTime ldt2 = LocalDateTime.parse("2022-03-11T00:00:00");

                UCSBHelpRequest ucsbHelpRequest2 = UCSBHelpRequest.builder()
                                .requesterEmail("ldelplaya@ucsb.edu")
                                .teamId("s22-6pm-3")
                                .tableOrBreakoutRoom("11")
                                .explanation("Dokku problems")
                                .solved(false)
                                .requestTime(ldt2)
                                .build();

                ArrayList<UCSBHelpRequest> expectedHelpRequests = new ArrayList<>();
                expectedHelpRequests.addAll(Arrays.asList(ucsbHelpRequest1, ucsbHelpRequest2));

                when(ucsbHelpRequestRepository.findAll()).thenReturn(expectedHelpRequests);

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsbhelprequest/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(ucsbHelpRequestRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedHelpRequests);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        // Tests for POST /api/ucsbdiningcommons...

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/ucsbhelprequest/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/ucsbhelprequest/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_ucsbhelprequest() throws Exception {
                // arrange

                LocalDateTime ldt1 = LocalDateTime.parse("2022-04-20T17:35");

                UCSBHelpRequest ucsbHelpRequest1 = UCSBHelpRequest.builder()
                                .requesterEmail("cgaucho@ucsb.edu")
                                .teamId("s22-5pm-3")
                                .tableOrBreakoutRoom("7")
                                .explanation("Need help with Swagger-ui")
                                .solved(true)
                                .requestTime(ldt1)
                                .build();

                when(ucsbHelpRequestRepository.save(eq(ucsbHelpRequest1))).thenReturn(ucsbHelpRequest1);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/ucsbhelprequest/post?requesterEmail=cgaucho@ucsb.edu&teamId=s22-5pm-3&tableOrBreakoutRoom=7&explanation=Need help with Swagger-ui&solved=true&requestTime=2022-04-20T17:35")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbHelpRequestRepository, times(1)).save(ucsbHelpRequest1);
                String expectedJson = mapper.writeValueAsString(ucsbHelpRequest1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        // Tests for GET /api/ucsbdates?id=...

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/ucsbhelprequest?id=7"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange
                LocalDateTime ldt1 = LocalDateTime.parse("2022-04-21T14:15");

                UCSBHelpRequest ucsbHelpRequest = UCSBHelpRequest.builder()
                                .requesterEmail("cgaucho@ucsb.edu")
                                .teamId("s22-5pm-3")
                                .tableOrBreakoutRoom("7")
                                .explanation("Need help with Swagger-ui")
                                .solved(false)
                                .requestTime(ldt1)
                                .build();

                when(ucsbHelpRequestRepository.findById(eq(7L))).thenReturn(Optional.of(ucsbHelpRequest));

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsbhelprequest?id=7"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(ucsbHelpRequestRepository, times(1)).findById(eq(7L));
                String expectedJson = mapper.writeValueAsString(ucsbHelpRequest);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(ucsbHelpRequestRepository.findById(eq(7L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsbhelprequest?id=7"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(ucsbHelpRequestRepository, times(1)).findById(eq(7L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("UCSBHelpRequest with id 7 not found", json.get("message"));
        }

        // Tests for DELETE /api/ucsbhelprequest?id=... 

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_ucsbhelprequest() throws Exception {
                // arrange

                LocalDateTime ldt1 = LocalDateTime.parse("2022-04-21T14:15");

                UCSBHelpRequest ucsbHelpRequest1 = UCSBHelpRequest.builder()
                                .requesterEmail("cgaucho@ucsb.edu")
                                .teamId("s22-5pm-3")
                                .tableOrBreakoutRoom("7")
                                .explanation("Need help with Swagger-ui")
                                .solved(false)
                                .requestTime(ldt1)
                                .build();

                when(ucsbHelpRequestRepository.findById(eq(15L))).thenReturn(Optional.of(ucsbHelpRequest1));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/ucsbhelprequest?id=15")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbHelpRequestRepository, times(1)).findById(15L);
                verify(ucsbHelpRequestRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBHelpRequest with id 15 deleted", json.get("message"));
        }
        
        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_ucsbhelprequest_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(ucsbHelpRequestRepository.findById(eq(15L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/ucsbhelprequest?id=15")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(ucsbHelpRequestRepository, times(1)).findById(15L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBHelpRequest with id 15 not found", json.get("message"));
        }

        // Tests for PUT /api/ucsbhelprequest?id=... 

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_ucsbhelprequest() throws Exception {
                // arrange

                LocalDateTime ldt1 = LocalDateTime.parse("2022-04-20T17:35");
                LocalDateTime ldt2 = LocalDateTime.parse("2022-04-20T18:31");

                UCSBHelpRequest ucsbHelpRequestOriginal = UCSBHelpRequest.builder()
                                .requesterEmail("cgaucho@ucsb.edu")
                                .teamId("s22-5pm-3")
                                .tableOrBreakoutRoom("7")
                                .explanation("Need help with Swagger-ui")
                                .solved(false)
                                .requestTime(ldt1)
                                .build();

                UCSBHelpRequest ucsbHelpRequestEdited = UCSBHelpRequest.builder()
                                .requesterEmail("dplaya@ucsb.edu")
                                .teamId("s22-5pm-4")
                                .tableOrBreakoutRoom("8")
                                .explanation("Need help with mutation tests")
                                .solved(true)
                                .requestTime(ldt2)
                                .build();

                String requestBody = mapper.writeValueAsString(ucsbHelpRequestEdited);

                when(ucsbHelpRequestRepository.findById(eq(67L))).thenReturn(Optional.of(ucsbHelpRequestOriginal));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/ucsbhelprequest?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbHelpRequestRepository, times(1)).findById(67L);
                verify(ucsbHelpRequestRepository, times(1)).save(ucsbHelpRequestEdited); // should be saved with correct user
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        
        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_ucsbhelprequest_that_does_not_exist() throws Exception {
                // arrange

                LocalDateTime ldt1 = LocalDateTime.parse("2022-04-20T17:35");

                UCSBHelpRequest ucsbEditedHelpRequest = UCSBHelpRequest.builder()
                                .requesterEmail("cgaucho@ucsb.edu")
                                .teamId("s22-5pm-3")
                                .tableOrBreakoutRoom("7")
                                .explanation("Need help with Swagger-ui")
                                .solved(false)
                                .requestTime(ldt1)
                                .build();

                String requestBody = mapper.writeValueAsString(ucsbEditedHelpRequest);

                when(ucsbHelpRequestRepository.findById(eq(67L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/ucsbhelprequest?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(ucsbHelpRequestRepository, times(1)).findById(67L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBHelpRequest with id 67 not found", json.get("message"));

        }

}
