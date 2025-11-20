package com.huntercodexs.integration.simulation.api;

import com.huntercodexs.integration.simulation.dto.UserRequestSimulation;
import com.huntercodexs.integration.openfeign.config.FeignClientConfig;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name="invalid-api", url="http://localhost:8085/api/users", configuration = FeignClientConfig.class)
public interface InvalidApiClientSimulation {

    @RequestMapping(method = RequestMethod.POST, value = "/create")
    Void create(@Valid @RequestBody UserRequestSimulation userRequestMOck);

}
