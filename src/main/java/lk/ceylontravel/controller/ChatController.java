package lk.ceylontravel.controller;

import java.security.Principal;
import java.util.Map;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lk.ceylontravel.model.Access;
import lk.ceylontravel.service.ChatService;
import lk.ceylontravel.service.Events;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Function: Tourist and Guide Chat Management
 * Member Name: Premasiriwardhana I. H. N. C.
 * Student ID: IT25103630
 * Role: Scrum Master
 * Course: SE2030 - Software Engineering (SLIIT)
 */
@RestController
public class ChatController {
    private final ChatService chatService;
    private final Access access;
    private final Events events;

    public ChatController(ChatService chatService, Access access, Events events) {
        this.chatService = chatService;
        this.access = access;
        this.events = events;
    }

    public record Start(
            @Positive long guide_id,
            @NotBlank @Size(max = 2000) String body
    ) {}

    public record Message(
            @NotBlank @Size(max = 2000) String body
    ) {}

    @GetMapping(value = "/api/events", produces = "text/event-stream")
    public SseEmitter events(Principal p) {
        return events.subscribe(access.id(p));
    }

    @GetMapping("/api/conversations")
    public Object rooms(Principal p) {
        return chatService.getUserConversations(access.id(p));
    }

    @PostMapping("/api/conversations")
    public Object start(Principal p, @Valid @RequestBody Start r) {
        long touristId = access.require(p, "TOURIST");
        long conversationId = chatService.startConversation(touristId, r.guide_id(), r.body());
        return Map.of("id", conversationId);
    }

    @GetMapping("/api/conversations/{id}/messages")
    public Object messages(Principal p, @PathVariable long id) {
        return chatService.getMessages(id, access.id(p));
    }

    @PostMapping("/api/conversations/{id}/messages")
    public Object send(Principal p, @PathVariable long id, @Valid @RequestBody Message r) {
        long messageId = chatService.sendMessage(id, access.id(p), r.body());
        return Map.of("id", messageId);
    }

    @PutMapping("/api/messages/{id}")
    public void edit(Principal p, @PathVariable long id, @Valid @RequestBody Message r) {
        chatService.editMessage(id, access.id(p), r.body());
    }

    @DeleteMapping("/api/messages/{id}")
    public void delete(Principal p, @PathVariable long id) {
        chatService.deleteMessage(id, access.id(p));
    }
}