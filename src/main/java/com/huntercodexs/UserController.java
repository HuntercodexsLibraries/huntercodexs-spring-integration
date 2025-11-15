package com.huntercodexs;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserApiClient userApiClient;
    private final InvalidApiClient invalidApiClient;

    @GetMapping("/test/{type}")
    public void test(@PathVariable(value = "type", required = false) String type) {
        System.out.println("test start");

        UserRequest userRequest = new UserRequest();
        userRequest.setName("Username Test");
        userRequest.setEmail("username@email.com");

        if (type != null && type.equals("exception")) {
            System.out.println("Test type: Exception using email=null");
            userRequest.setEmail(null);
        }

        try {
            System.out.println("Starting integration request");
            System.out.println(userRequest);

            Void result;
            if (type != null && type.equals("invalid-api")) {
                result = invalidApiClient.create(userRequest);
            } else {
                result = userApiClient.create(userRequest);
            }

            System.out.println("The result is: "+result);
            System.out.println("fim");

        } catch (Exception ex) {
            System.out.println("<<< Exception >>>");
            System.out.println(ex.getMessage());
        }
    }

    @PostMapping("/create")
    public void create(@Valid @RequestBody UserRequest request) {
        System.out.println("create start");
        System.out.println(request);

        if (request.getEmail() == null) {
            throw new RuntimeException("Email is mandatory");
        }

        System.out.println("fim");
    }

}
