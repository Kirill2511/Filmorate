package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldGetAllReviews() throws Exception {
        mockMvc.perform(get("/reviews"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void shouldGetReviewsByFilmId() throws Exception {
        mockMvc.perform(get("/reviews")
                        .param("filmId", "1")
                        .param("count", "5"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void shouldDeleteReview() throws Exception {
        mockMvc.perform(delete("/reviews/1"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAddLikeToReview() throws Exception {
        mockMvc.perform(put("/reviews/2/like/1"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAddDislikeToReview() throws Exception {
        mockMvc.perform(put("/reviews/2/dislike/1"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRemoveLikeFromReview() throws Exception {
        mockMvc.perform(delete("/reviews/2/like/1"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRemoveDislikeFromReview() throws Exception {
        mockMvc.perform(delete("/reviews/2/dislike/1"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn404ForNonExistentReview() throws Exception {
        mockMvc.perform(get("/reviews/999"))
                .andExpect(status().isNotFound());
    }
}
