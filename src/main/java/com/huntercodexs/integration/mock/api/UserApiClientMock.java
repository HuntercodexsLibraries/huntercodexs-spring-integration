package com.huntercodexs.integration.mock.api;

import com.huntercodexs.integration.mock.dto.UserRequestMock;
import com.huntercodexs.integration.openfeign.config.FeignClientConfig;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name="user-manager", url="http://localhost:8080/api/users", configuration = FeignClientConfig.class)
public interface UserApiClientMock {

    @RequestMapping(method = RequestMethod.POST, value = "/create")
    Void create(@Valid @RequestBody UserRequestMock userRequestMOck);

}
