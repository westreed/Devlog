package io.blog.devlog.global.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
@Getter
@Component
@RequiredArgsConstructor
public class ErrorResponse {
    private String timestamp;
    private Integer status;
    private String error;
    private String path;

    @JsonIgnore
    private final ObjectMapper objectMapper;

    protected String toJsonString() {
        try {
            log.info("ErrorResponse toJsonString() 호출");
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            log.error("ErrorResponse toJsonString() 오류 발생!");
            return null;
        }
    }

    public void setResponse(HttpServletResponse response, Integer status, String error, String path) throws IOException {
        this.timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").format(new Date());
        this.status = status;
        this.error = error;
        this.path = path;
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON.toString());
        response.getWriter().write(this.toJsonString());
    }
}
