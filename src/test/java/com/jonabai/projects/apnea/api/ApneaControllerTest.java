package com.jonabai.projects.apnea.api;

import com.jonabai.projects.apnea.api.domain.BreathingPause;
import com.jonabai.projects.apnea.api.domain.BreathingPauseType;
import com.jonabai.projects.apnea.services.AudioFileSilenceDetectorService;
import com.jonabai.projects.apnea.services.BreathingPauseClassificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApneaController.class)
@DisplayName("ApneaController Tests")
class ApneaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AudioFileSilenceDetectorService silenceDetectorService;

    @MockitoBean
    private BreathingPauseClassificationService classificationService;

    @Test
    @DisplayName("GET /api/health should return UP status")
    void healthCheckReturnsUp() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("POST /api/analyze with empty file should return bad request")
    void analyzeEmptyFileReturnsBadRequest() throws Exception {
        var emptyFile = new MockMultipartFile(
                "file",
                "test.wav",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                new byte[0]
        );

        mockMvc.perform(multipart("/api/analyze").file(emptyFile))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/analyze with non-WAV file should return bad request")
    void analyzeNonWavFileReturnsBadRequest() throws Exception {
        var nonWavFile = new MockMultipartFile(
                "file",
                "test.mp3",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                "test content".getBytes()
        );

        mockMvc.perform(multipart("/api/analyze").file(nonWavFile))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/analyze with valid WAV file should return analysis results")
    void analyzeValidWavFileReturnsResults() throws Exception {
        var detectedPauses = List.of(
                new BreathingPause("test.wav", 1, 1.0f, 2.0f, BreathingPauseType.NOT_SET),
                new BreathingPause("test.wav", 2, 5.0f, 10.5f, BreathingPauseType.NOT_SET)
        );

        var classifiedPauses = List.of(
                new BreathingPause("test.wav", 1, 1.0f, 2.0f, BreathingPauseType.NORMAL),
                new BreathingPause("test.wav", 2, 5.0f, 10.5f, BreathingPauseType.APNEA)
        );

        when(silenceDetectorService.processFile(anyString())).thenReturn(detectedPauses);
        when(classificationService.classify(any())).thenReturn(classifiedPauses);

        var wavFile = new MockMultipartFile(
                "file",
                "test.wav",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                "RIFF....WAVEfmt ".getBytes()
        );

        mockMvc.perform(multipart("/api/analyze").file(wavFile))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.filename").value("test.wav"))
                .andExpect(jsonPath("$.totalPauses").value(2))
                .andExpect(jsonPath("$.apneaCount").value(1))
                .andExpect(jsonPath("$.normalCount").value(1))
                .andExpect(jsonPath("$.pauses").isArray())
                .andExpect(jsonPath("$.pauses[0].index").value(1))
                .andExpect(jsonPath("$.pauses[0].type").value("NORMAL"))
                .andExpect(jsonPath("$.pauses[1].index").value(2))
                .andExpect(jsonPath("$.pauses[1].type").value("APNEA"));
    }

    @Test
    @DisplayName("POST /api/analyze with no pauses detected should return empty results")
    void analyzeFileWithNoPausesReturnsEmptyResults() throws Exception {
        when(silenceDetectorService.processFile(anyString())).thenReturn(List.of());

        var wavFile = new MockMultipartFile(
                "file",
                "silent.wav",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                "RIFF....WAVEfmt ".getBytes()
        );

        mockMvc.perform(multipart("/api/analyze").file(wavFile))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filename").value("silent.wav"))
                .andExpect(jsonPath("$.totalPauses").value(0))
                .andExpect(jsonPath("$.apneaCount").value(0))
                .andExpect(jsonPath("$.normalCount").value(0))
                .andExpect(jsonPath("$.pauses").isEmpty());
    }
}
