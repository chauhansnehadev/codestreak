package com.codestreak.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class JsonUtil {

    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public static ObjectMapper getMapper() {
        return mapper;
    }

    public static void sendJson(HttpServletResponse response, int statusCode, Object data) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(statusCode);
        PrintWriter out = response.getWriter();
        mapper.writeValue(out, data);
        out.flush();
    }

    public static void sendSuccess(HttpServletResponse response, String message, Object data) throws IOException {
        Map<String, Object> map = new HashMap<>();
        map.put("success", true);
        map.put("message", message);
        if (data != null) {
            map.put("data", data);
        }
        sendJson(response, HttpServletResponse.SC_OK, map);
    }

    public static void sendCreated(HttpServletResponse response, String message, Object data) throws IOException {
        Map<String, Object> map = new HashMap<>();
        map.put("success", true);
        map.put("message", message);
        if (data != null) {
            map.put("data", data);
        }
        sendJson(response, HttpServletResponse.SC_CREATED, map);
    }

    public static void sendError(HttpServletResponse response, int statusCode, String message) throws IOException {
        Map<String, Object> map = new HashMap<>();
        map.put("success", false);
        map.put("error", message);
        sendJson(response, statusCode, map);
    }
}
