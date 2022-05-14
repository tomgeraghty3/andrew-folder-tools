package uk.ac.man.cs.geraght0.andrew.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.Streams;
import java.util.Comparator;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.ac.man.cs.geraght0.andrew.config.Config;

@Slf4j
@Service
@RequiredArgsConstructor
public class Backend {

  private final Config config;
  private final RestTemplate rest;
  private final BuildProperties buildProperties;

  public Optional<String> checkForNewerVersion() {
    if (config.isSkipVersionUpdateCheck()) {
      log.info("Skipping version check as skipVersionUpdateCheck is true");
      return Optional.empty();
    }

    String ver = buildProperties.getVersion();
    if (!NumberUtils.isParsable(ver)) {
      log.info("The version is currently \"{}\" which is likely not a release. Skipping check for newer release version", ver);
    } else {
      final double thisVersion = Double.parseDouble(ver);
      try {
        String url = "https://api.github.com/repos/tomgeraghty3/andrew-folder-tools/tags";
        log.info("Looking for a newer version (than {}) of the application by calling: {}", ver, url);
        ArrayNode json = rest.getForObject(url, ArrayNode.class);
        if (json != null) {
          Optional<JsonNode> max = Streams.stream(json.elements())
                                          .max(Comparator.comparingDouble(node -> Double.parseDouble(node.get("name")
                                                                                                         .asText())));
          if (!max.isPresent()) {
            log.info("There are no version in GitHib or the GET failed");
          } else {
            final String version = max.get()
                                      .get("name")
                                      .asText();
            double latestVersion = Double.parseDouble(version);
            log.info("The latest in GitHub is: {}", latestVersion);
            url = String.format("%s%s", "https://github.com/tomgeraghty3/andrew-folder-tools/releases/tag/", version);
            if (latestVersion > thisVersion) {
              return Optional.of(url);
            }
          }
        }
      } catch (Exception e) {
        log.error("Unable to determine if there was a new version. Continuing as if there isn't", e);
      }
    }

    return Optional.empty();
  }
}