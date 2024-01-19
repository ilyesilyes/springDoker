package fr.meritis.first.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.meritis.first.domain.HttpResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;

import static java.time.LocalDate.now;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Component
public class CustomAuthentificationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
            HttpResponse httpResponse = HttpResponse.builder()
                    .timeStamp(now().toString())
                    .reason("You need to log in to access the resource")
                    .status(UNAUTHORIZED)
                    .statusCode(UNAUTHORIZED.value())
                    .build();
            response.setContentType(APPLICATION_JSON_VALUE);
            response.setStatus(UNAUTHORIZED.value());
            OutputStream out = response.getOutputStream();
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(out, httpResponse);
            out.flush();
    }
}

