package com.walmart.thor.endgame.orderingestion.dto;

import com.walmart.thor.endgame.orderingestion.common.dao.CustomerPurchaseOrderDAO;
import com.walmart.thor.endgame.orderingestion.common.dao.GlobalGtinInventory;
import java.util.List;
import java.util.Map;

import com.walmart.thor.endgame.orderingestion.common.dto.domain.ChangeReason;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Builder(toBuilder = true)
@Getter
@Setter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class InventoryResult {

  Boolean isAvailable;
  Boolean alreadyUpdated;
  List<GlobalGtinInventory> gtinInventory;
  Map<ItemKey, Integer> requestedInventoryQty;
  Map<ItemKey, ChangeReason> itemStatus;
  List<CustomerPurchaseOrderDAO.PickTicketDetails> pickTicketDetails;


}
