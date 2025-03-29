package com.example.crud.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

public class RuvdsDTO {

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BalanceResponse {
        private Double amount;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ServerResponse {
        @JsonProperty("virtual_server_id")
        private Integer serverId;

        @JsonProperty("user_comment")
        private String userComment;

        @JsonProperty("paid_till")
        private String paidTill;

        @JsonProperty("network_v4")
        private List<NetworkV4> networkV4;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NetworkV4 {
        @JsonProperty("ip_address")
        private String ipAddress;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ServersListResponse {
        private List<ServerResponse> servers;
    }
}