package com.walmart.thor.endgame.orderingestion.service;

import com.walmart.thor.endgame.orderingestion.repository.PickTicketIDRepository;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PickTicketIDGenerator {

  @Autowired private PickTicketIDRepository pickTicketIDRepository;

  /**
   * @param fcId The <code>FcID</code> to prepend to the newly generated <code>PickTicket</code>
   *     <b>ID</b>
   * @param suffix An optional suffix if needed to be appended to the ID.
   * @return newly generated <code>PickTicket</code> <b>ID</b>
   * @apiNote Takes a <code>FcID</code> and prepends it to the newly generated <code>PickTicket
   * </code> <b>ID</b>
   */
  public String generateNextPickTicketID(String fcId, Optional<String> suffix) {
    var newId = pickTicketIDRepository.getNextPickTicketID();
    return String.join("", fcId, String.valueOf(newId), suffix.orElse(""));
  }

  /**
   * @implNote This method takes a limit (count) of how many versions of the Pick-ticket ID to be
   *     generated and iteratively generates the next suffix. The suffix generation begins from just
   *     "A" and proceeds in lexicographically increasing order by following a base-26 addition. For
   *     example: if the limit is 2 and the baseID is "96102222" then the versions will be:
   *     96102222A, 96102222B.
   */
  private List<String> getValues(final String baseID, final int limit) {
    final List<Integer> suffixList = new ArrayList<>();
    final List<String> ptIDs = new LinkedList<>();
    int base = 65; // Decimal ASCII value for 'A'.
    for (int i = 0; i < limit; i++) {
      // for each perform a base-26 addition (increment by 1).
      // The 26 alphabets A-Z are to be mapped onto 0-25, ex: 0=A, 1=B,.. 25=Z
      // Note 1: just as 'A'+1='B', similarly 'Z'+1 = 'AA', 'ZZ'+1='AAA', 'AZZ'+1='BAA' and so on.
      // Note 2: we will process the list from left (beginning) to right (end) as opposed to right
      // to left (as done in arithmetic addition)
      // for the sake of ease and later on reverse the result.
      if (suffixList.isEmpty()) {
        suffixList.add(0); // we want to start with 'A'.
      } else {
        int idx = 0;
        while (idx < suffixList.size() && suffixList.get(idx) == 25) {
          // for all the places which are having 'Z' have to be incremented & converted to 'A' and
          // carry-forward the +1 to the next position.
          suffixList.set(idx, 0);
          idx++;
        }
        if (idx == suffixList.size()) {
          // this means all the places held 'Z' and now there's a carry and hence we need to assign
          // a new 'A'. 'ZZ'+1 => 'AAA'
          suffixList.add(0);
        } else {
          // this means the idx position has a value less than 'Z' which can be incremented.
          // 'ZA'+1 => 'ZB'
          suffixList.set(idx, suffixList.get(idx) + 1);
        }
      }
      StringBuilder idBuilder = new StringBuilder();
      Function<Integer, String> toAlphabet = intLabel -> String.valueOf((char) (intLabel + base));
      suffixList.stream()
          .map(toAlphabet)
          .forEach(idBuilder::append); // map each number [0-25] to A-Z.
      var suffix = idBuilder.reverse().toString(); // reverse the string to get the actual result.
      ptIDs.add(String.join("", baseID, suffix));
    }
    return ptIDs;
  }

  /**
   * @param fcId The <code>FcID</code> to prepend to the newly generated <code>PickTicket</code>
   * @param count How many versions of the pick-ticket required.
   * @return A list of pick-ticket IDs suffixed with alphabets in lexicographically increasing
   *     order.
   */
  public List<String> getNextPickTicketIDs(final String fcId, final int count) {
    var baseId = generateNextPickTicketID(fcId, Optional.empty());
    return getValues(baseId, count);
  }
}
