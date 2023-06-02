package com.shopinzone.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class CommonUtils {

  public String readJson(String src) throws IOException {
    return IOUtils.resourceToString(src, StandardCharsets.UTF_8);
  }
}
