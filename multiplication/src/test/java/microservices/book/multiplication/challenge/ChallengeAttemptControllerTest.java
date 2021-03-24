package microservices.book.multiplication.challenge;

import microservices.book.multiplication.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ExtendWith(SpringExtension.class)
@AutoConfigureJsonTesters
@WebMvcTest(ChallengeAttemptController.class)
class ChallengeAttemptControllerTest {

    @MockBean
    private ChallengeService challengeService;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JacksonTester<ChallengeAttemptDTO> jsonRequestAttempt;

    @Autowired
    private JacksonTester<ChallengeAttempt> jsonResultAttempt;

    @Autowired
    private JacksonTester<List<ChallengeAttempt>> jsonResultAttemptList;

    @Test
    void postValidResult() throws Exception {
        // given
        User user = new User(1L, "John");
        ChallengeAttemptDTO attemptDTO = new ChallengeAttemptDTO(50, 70, "John", 3500);
        ChallengeAttempt expectedResponse = new ChallengeAttempt(5L, user, 50, 70, 3500, true);
        given(challengeService.verifyAttempt(attemptDTO)).willReturn(expectedResponse);

        // when
        MockHttpServletResponse response = mvc.perform(
                post("/attempts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequestAttempt.write(attemptDTO).getJson()))
                .andReturn().getResponse();

        // then
        then(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        then(response.getContentAsString()).isEqualTo(jsonResultAttempt.write(expectedResponse).getJson());
    }

    @Test
    void postInvalidResult() throws Exception {
        // given an attempt with invalid input data
        ChallengeAttemptDTO attemptDTO = new ChallengeAttemptDTO(2000, -70, "john", 1);

        // when
        MockHttpServletResponse response = mvc.perform(
                post("/attempts").contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequestAttempt.write(attemptDTO).getJson()))
                .andReturn().getResponse();

        // then
        then(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void ShouldGetLastAttemptsForUser() throws Exception {
        // when
        User user = new User(1L, "john_doe");
        List<ChallengeAttempt> expectedAttempts = List.of(
                new ChallengeAttempt(1L, user, 11, 11, 132, false),
                new ChallengeAttempt(2L, user, 11, 11, 121, true)
        );
        when(challengeService.getStatsForUser("john_doe")).thenReturn(expectedAttempts);

        // when
        MockHttpServletResponse response = mvc.perform(
                get("/attempts").param("alias", "john_doe"))
                .andReturn()
                .getResponse();

        // then
        assertAll(
                () -> then(response.getStatus()).isEqualTo(HttpStatus.OK.value()),
                () -> then(response.getContentAsString()).isEqualTo(jsonResultAttemptList.write(expectedAttempts).getJson())
        );
    }
}
