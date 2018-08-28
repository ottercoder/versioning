package ru.ottercoder.versioning.sample;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.ottercoder.versioning.annotation.ApiVersion;

@RestController
@ApiVersion
public class SampleController {

    @GetMapping("/info")
    @ApiVersion(till = 3)
    public String getSample() {
        return "Hello Sample";
    }

    @GetMapping("/info")
    @ApiVersion(since = 4)
    public String getSample2() {
        return "Hello Sample 2";
    }

    @GetMapping("/sample")
    public String getSample3() {
        return "Hello Sample 2";
    }
}
