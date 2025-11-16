package com.huntercodexs.integration.mock.controlller;

import com.huntercodexs.integration.mock.api.InvalidApiClientMock;
import com.huntercodexs.integration.mock.api.UserApiClientMock;
import com.huntercodexs.integration.mock.dto.UserRequestMock;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserControllerMock {

    private final UserApiClientMock userApiClientMock;
    private final InvalidApiClientMock invalidApiClientMock;

    @GetMapping("/test/{type}")
    public void test(@PathVariable(value = "type", required = false) String type) {
        System.out.println("test start");

        UserRequestMock userRequestMOck = new UserRequestMock();
        userRequestMOck.setName("Username Test");
        userRequestMOck.setEmail("username@email.com");

        if (type != null && type.equals("exception")) {
            System.out.println("Test type: Exception using email=null");
            userRequestMOck.setEmail(null);
        }

        try {
            System.out.println("Starting integration request");
            System.out.println(userRequestMOck);

            Void result;
            if (type != null && type.equals("invalid-api")) {
                result = invalidApiClientMock.create(userRequestMOck);
            } else {
                result = userApiClientMock.create(userRequestMOck);
            }

            System.out.println("The result is: "+result);
            System.out.println("fim");

        } catch (Exception ex) {
            System.out.println("<<< Exception >>>");
            System.out.println(ex.getMessage());
        }
    }

    @PostMapping("/create")
    public void create(@Valid @RequestBody UserRequestMock request) {
        System.out.println("create start");
        System.out.println(request);

        if (request.getEmail() == null) {
            throw new RuntimeException("Email is mandatory");
        }

        System.out.println("fim");
    }

}
