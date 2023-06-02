package com.walmart.thor.endgame.orderingestion.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.walmart.thor.endgame.orderingestion.repository.PickTicketIDRepository;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PickTicketIDGeneratorTest {

  @Mock private PickTicketIDRepository pickTicketIDRepository;

  @InjectMocks
  private PickTicketIDGenerator pickTicketIDGenerator = Mockito.spy(new PickTicketIDGenerator());

  @Test
  void testIDGenerationSuccessful_001() {
    final long nextId = 10000;
    final String fcId = "9610";
    final String expectedPtId = fcId + nextId;
    when(pickTicketIDRepository.getNextPickTicketID()).thenReturn(nextId);
    assertEquals(
        expectedPtId, pickTicketIDGenerator.generateNextPickTicketID(fcId, Optional.empty()));
  }

  @Test
  void testIDGenerationSuccessful_002() {
    final long nextId = 10000;
    final String fcId = "9610";
    final String suffix = "A";
    final String expectedPtId = fcId + nextId + suffix;
    when(pickTicketIDRepository.getNextPickTicketID()).thenReturn(nextId);
    assertEquals(
        expectedPtId, pickTicketIDGenerator.generateNextPickTicketID(fcId, Optional.of(suffix)));
  }

  @Test
  void testIDGenerationSuccessful_003() {
    final long nextId = 10000;
    final String fcId = "9610";
    var basePtIDString = String.join("", List.of(fcId, String.valueOf(nextId)));
    when(pickTicketIDRepository.getNextPickTicketID()).thenReturn(nextId);
    List<String> ids = pickTicketIDGenerator.getNextPickTicketIDs(fcId, 2);
    String ptIDPattern = fcId + nextId + "[A-Z]*";
    Pattern pattern = Pattern.compile(ptIDPattern);
    assertTrue(ids.stream().map(pattern::matcher).allMatch(Matcher::matches));
    assertTrue(ids.stream().anyMatch(id -> id.contains("A")));
    assertTrue(ids.stream().anyMatch(id -> id.contains("B")));
    assertTrue(ids.stream().allMatch(id -> id.contains(basePtIDString)));
    assertEquals(2, ids.size());
  }

  @Test
  void testIDGenerationSuccessful_004() {
    final long nextId = 10000;
    final String fcId = "9610";
    when(pickTicketIDRepository.getNextPickTicketID()).thenReturn(nextId);
    List<String> ids = pickTicketIDGenerator.getNextPickTicketIDs(fcId, 728);
    String ptIDPattern = fcId + nextId + "[A-Z]*";
    Pattern pattern = Pattern.compile(ptIDPattern);
    assertEquals(728, ids.size());
    assertTrue(ids.stream().map(pattern::matcher).allMatch(Matcher::matches));
    assertTrue(ids.stream().anyMatch(id -> id.contains("AAA")));
  }

  @Test
  void testIDGenerationSuccessful_005() {
    final long nextId = 10000;
    final String fcId = "9610";
    var basePtIDString = String.join("", List.of(fcId, String.valueOf(nextId)));
    when(pickTicketIDRepository.getNextPickTicketID()).thenReturn(nextId);
    List<String> ids = pickTicketIDGenerator.getNextPickTicketIDs(fcId, 28);
    String ptIDPattern = fcId + nextId + "[A-Z]*";
    Pattern pattern = Pattern.compile(ptIDPattern);
    assertTrue(ids.stream().map(pattern::matcher).allMatch(Matcher::matches));
    assertTrue(ids.stream().anyMatch(id -> id.contains("A")));
    assertTrue(ids.stream().anyMatch(id -> id.contains("B")));
    assertTrue(ids.stream().anyMatch(id -> id.contains("AB")));
    assertTrue(ids.stream().allMatch(id -> id.contains(basePtIDString)));
    assertEquals(28, ids.size());
  }
}
