package org.example.profile_canhan.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

@Controller
@CrossOrigin(origins = "*")
public class ChatController {

    // 🔑 API KEY GEMINI
    private static final String GEMINI_API_KEY = "AIzaSyCzmlI_RK2WeT3e98JXUxS3w7pBT8tjuqQ";
    private static final String GEMINI_API =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + GEMINI_API_KEY;

    // 🧠 Tính cách trợ lý còn goị là yêu cầu
    private static final String SYSTEM_INSTRUCTION =
            "Bạn là Trợ lý AI của Chu Đình Bình. Trả lời NGẮN GỌN, súc tích, chỉ nói điều quan trọng. " +
                    "Không nói dài, không lặp lại. Luôn dùng tiếng Việt thân thiện, tự nhiên. Nếu người dùng muốn nói tiếng anh thì trả lời bằng tiếng anh";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ⚡ Khi mở trình duyệt => hiển thị giao diện ChatBot
    @GetMapping("/")
    public String showChatPage() {
        return "cv";
        //<div th:replace="fragments/chatbot :: chatbot"></div> dán vàotrang muốn sử dụng chatbot này
        // => /templates/ChatBot.html
    }

        //<div th:replace="fragments/chatbot :: chatbot"></div> dán vàotrang muốn sử dụng chatbot này
        // => /templates/ChatBot.html
    }
    // ⚡ API xử lý khi người dùng gửi tin nhắn
    @PostMapping("/api/chat")
    @ResponseBody
    public String chat(@RequestBody String userMessage) {
        try {
            // ✅ Thông tin cá nhân mẫu
            String personalInfo = """
                    Họ và tên: Chu Đình Bình
                    Nghề nghiệp: Lập trình viên và chuyên chạy Ads, thiết kế Website.
                    Kinh nghiệm: 1 năm phát triển ứng dụng Java Spring Boot, dự án quán cà phê, khu vui chơi, và tool tự động hóa.
                    Liên Hệ : 0389415404
                    Dịch vụ: Thiết kế web, chạy quảng cáo Facebook, xây chatbot, xây hệ thống quản lý doanh nghiệp.
                    """;


            // ✅ Nội dung prompt gửi đi
            String fullPrompt = String.format("""
                    --- HỒ SƠ CỦA BẠN ---
                    %s

                    --- CÂU HỎI NGƯỜI DÙNG ---
                    %s
                    """, personalInfo, userMessage);

            // ✅ Tạo request gửi Gemini API
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> systemInstructionPart = Map.of("text", SYSTEM_INSTRUCTION);
            Map<String, Object> systemInstructionConfig = Map.of("parts", List.of(systemInstructionPart));

            Map<String, Object> userPart = Map.of("text", fullPrompt);
            Map<String, Object> userContent = Map.of("role", "user", "parts", List.of(userPart));

            Map<String, Object> generationConfig = Map.of("temperature", 0.7);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("systemInstruction", systemInstructionConfig);
            requestBody.put("contents", List.of(userContent));
            requestBody.put("generationConfig", generationConfig);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(GEMINI_API, entity, String.class);

            // ✅ Xử lý phản hồi từ Gemini
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode textNode = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");

            if (textNode.isTextual()) {
                return textNode.asText();
            } else {
                JsonNode errorNode = root.path("error").path("message");
                if (errorNode.isTextual()) {
                    return "❌ Lỗi từ Gemini API: " + errorNode.asText();
                }
                return "⚠️ Không thể đọc phản hồi từ Gemini.";
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "⚠️ Lỗi xử lý Backend: " + e.getMessage();
        }
    }
}
