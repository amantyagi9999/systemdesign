package com.demo.api;

import com.demo.dto.DefaultApiResponse;
import com.demo.dto.SearchDto;
import com.demo.dto.SpamDto;
import com.demo.service.ContactUploadService;
import com.demo.service.SearchService;
import com.demo.service.SpamReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/caller/")
@RequiredArgsConstructor
@Slf4j
public class CallerIdController {

    private final SearchService searchService;
    private final SpamReportService spamReportService;
    private final ContactUploadService contactUploadService;

    @GetMapping("search/{phoneNumber}")
    public ResponseEntity<DefaultApiResponse> searchPhoneNumber(@PathVariable String phoneNumber){
        SearchDto  searchDto = searchService.searchPhoneNumber(phoneNumber);
        return ResponseEntity.ok(DefaultApiResponse.builder()
                .message("Number Searched success")
                .data(searchDto)
                .code(HttpStatus.OK.value()).build());
    }

    @PostMapping("spam")
    public ResponseEntity<DefaultApiResponse> searchPhoneNumber(@RequestBody SpamDto spamDto){
        SearchDto  searchDto = searchService.searchPhoneNumber(phoneNumber);
        return ResponseEntity.ok(DefaultApiResponse.builder()
                .message("Number Searched success")
                .data(searchDto)
                .code(HttpStatus.OK.value()).build());
    }


}
