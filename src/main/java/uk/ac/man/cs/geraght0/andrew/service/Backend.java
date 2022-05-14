package uk.ac.man.cs.geraght0.andrew.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.Streams;
import java.util.Comparator;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import uk.ac.man.cs.geraght0.andrew.config.Config;
import uk.ac.man.cs.geraght0.andrew.models.config.FolderConfig;

@Slf4j
@Service
@RequiredArgsConstructor
public class Backend {

  private final Config config;
  private final FolderConfig folderConfig;
  private final WebClient webClient;


  public Optional<String> checkForNewerVersion() {
    if (!NumberUtils.isParsable(config.getVersion())) {
      log.info("The version is currently \"{}\" which is likely not a release. Skipping check for newer release version", config.getVersion());
    } else {
      final double thisVersion = Double.parseDouble(config.getVersion());
      try {
        ArrayNode json = webClient.get()
                                  .uri("https://api.github.com/repos/tomgeraghty3/andrew-tools/tags")
                                  .retrieve()
                                  .bodyToMono(ArrayNode.class)
                                  .block();

        Optional<JsonNode> max = Streams.stream(json.elements())
                                        .max(Comparator.comparingDouble(node -> Double.parseDouble(node.get("name")
                                                                                                       .asText())));
        if (max.isPresent()) {
          final String version = max.get()
                                    .get("name")
                                    .asText();
          double latestVersion = Double.parseDouble(version);
          String url = String.format("%s%s", "https://github.com/tomgeraghty3/andrew-tools/releases/tag/", version);
          if (latestVersion > thisVersion) {
            return Optional.of(url);
          }
        }
      } catch (Exception e) {
        log.error("Unable to determine if there was a new version. Continuing as if there isn't", e);
      }
    }

    return Optional.empty();
  }
}