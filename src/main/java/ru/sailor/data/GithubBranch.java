package ru.sailor.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubBranch {

    String name;
    GithubCommit commit;

    @JsonProperty("message")
    String errorMessage;

}
