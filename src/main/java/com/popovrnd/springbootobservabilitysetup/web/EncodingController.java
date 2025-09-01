package com.popovrnd.springbootobservabilitysetup.web;

import com.popovrnd.springbootobservabilitysetup.service.Codec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/encode")
public class EncodingController {

    private final Codec codec;

    @PostMapping
    public ResponseEntity<String> submit(@RequestBody String string) {
        log.info("Requested encoding long URL = {}", string);
        String encoded = codec.encode(string);
        return ResponseEntity.ok(encoded);
    }
}
