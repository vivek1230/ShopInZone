package com.walmart.thor.endgame.orderingestion.repository;

import static com.walmart.thor.endgame.orderingestion.common.constants.Constants.INVENTORY_PROJECTION_SCHEMA;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class PickTicketIDRepository {
  @Value("${application.data.pickTicketIDProvider}")
  private String pickTicketIDSeqName;

  @PersistenceContext private EntityManager entityManager;

  public Long getNextPickTicketID() {
    final String fetchIdQuery =
        String.format(
            "SELECT NEXT VALUE FOR %s.%s", INVENTORY_PROJECTION_SCHEMA, pickTicketIDSeqName);
    return Long.parseLong(
        entityManager.createNativeQuery(fetchIdQuery).getSingleResult().toString());
  }
}
