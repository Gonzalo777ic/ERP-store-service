package com.valeerp.store.complaint;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/store/complaints")
public class ComplaintController {

    private final ComplaintBookService book;

    public ComplaintController(ComplaintBookService book) {
        this.book = book;
    }

    @GetMapping("/catalog")
    public ComplaintCatalogView catalog() {
        return book.catalog();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ComplaintSheetView file(@AuthenticationPrincipal Jwt jwt, @RequestBody ComplaintCommands.File command) {
        return book.file(command, jwt == null ? null : jwt.getSubject());
    }

    @GetMapping
    public List<ComplaintSheetView> list() {
        return book.list();
    }

    @GetMapping("/{id}")
    public ComplaintSheetView get(@PathVariable Long id) {
        return book.get(id);
    }

    @PostMapping("/{id}/" + ComplaintAction.ATTENTION_PATH)
    public ComplaintSheetView startAttention(@PathVariable Long id) {
        return book.startAttention(id);
    }

    @PostMapping("/{id}/" + ComplaintAction.RESOLUTION_PATH)
    public ComplaintSheetView attend(@PathVariable Long id, @RequestBody ComplaintCommands.Response command) {
        return book.attend(id, command);
    }

    @PostMapping("/{id}/" + ComplaintAction.CANCELLATION_PATH)
    public ComplaintSheetView cancel(@PathVariable Long id, @RequestBody ComplaintCommands.Cancellation command) {
        return book.cancel(id, command);
    }
}
