package com.dangdangsalon.domain.chat.controller;

import com.dangdangsalon.domain.chat.service.MessageReplayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/replay")
public class MessageReplayController {

    private final MessageReplayService replayService;

    @PostMapping("/{id}")
    public ResponseEntity<?> replayMessage(@PathVariable String id) {
        replayService.replayById(id);
        return ResponseEntity.ok("Replay 요청 id = " + id);
    }
}
