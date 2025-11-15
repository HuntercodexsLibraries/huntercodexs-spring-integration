package com.huntercodexs;

import com.huntercodexs.openfeign.config.FeignClientConfig;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name="user-manager", url="http://localhost:8080/api/users", configuration = FeignClientConfig.class)
public interface UserApiClient {

    @RequestMapping(method = RequestMethod.POST, value = "/create")
    Void create(@Valid @RequestBody UserRequest userRequest);

}
