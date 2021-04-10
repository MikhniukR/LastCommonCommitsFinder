package ru.sailor.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubCommit {
    @JsonProperty("sha")
    String sha;
    @JsonProperty("parents")
    List<GithubCommit> parents;

    public GithubCommit() {
        parents = Collections.emptyList();
    }

}
