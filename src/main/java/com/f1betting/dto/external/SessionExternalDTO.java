package com.f1betting.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for session data from OpenF1 API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SessionExternalDTO {

    @JsonProperty("session_key")
    private Integer sessionKey;

    @JsonProperty("session_name")
    private String sessionName;

    @JsonProperty("date_start")
    private String dateStart;

    @JsonProperty("date_end")
    private String dateEnd;

    @JsonProperty("year")
    private Integer year;

    @JsonProperty("country_name")
    private String countryName;

    @JsonProperty("country_code")
    private String countryCode;

    @JsonProperty("circuit_short_name")
    private String circuitShortName;

    @JsonProperty("location")
    private String location;

    @JsonProperty("session_type")
    private String sessionType;
}
