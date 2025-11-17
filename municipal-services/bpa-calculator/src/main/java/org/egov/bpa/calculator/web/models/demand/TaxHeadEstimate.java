package org.egov.bpa.calculator.web.models.demand;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxHeadEstimate {


    private String taxHeadCode;

    private BigDecimal estimateAmount;

    private Category category;

    private Object additionalDetails;
}
