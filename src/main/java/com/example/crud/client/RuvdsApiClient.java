package com.example.crud.client;

import com.example.crud.dto.RuvdsDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "ruvds-api", url = "https://api.ruvds.com/v2")
public interface RuvdsApiClient {

    @GetMapping("/balance")
    RuvdsDTO.BalanceResponse getBalance(@RequestHeader("Authorization") String authToken);

    @GetMapping("/servers?get_paid_till=true&get_network=true")
    RuvdsDTO.ServersListResponse getServers(@RequestHeader("Authorization") String authToken);
}