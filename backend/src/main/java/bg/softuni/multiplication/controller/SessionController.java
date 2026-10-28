package bg.softuni.multiplication.controller;

import bg.softuni.multiplication.dto.SessionDtos.AnswerRequest;
import bg.softuni.multiplication.dto.SessionDtos.CreateSessionRequest;
import bg.softuni.multiplication.dto.SessionDtos.ResultResponse;
import bg.softuni.multiplication.dto.SessionDtos.SessionResponse;
import bg.softuni.multiplication.dto.SessionDtos.SubmitRequest;
import bg.softuni.multiplication.service.SessionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {
    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping
    public SessionResponse create(@Valid @RequestBody CreateSessionRequest request) {
        return sessionService.create(request);
    }

    @PostMapping("/{id}/submit")
    public ResultResponse submit(@PathVariable long id, @Valid @RequestBody SubmitRequest request) {
        return sessionService.submit(id, request.answers());
    }
}