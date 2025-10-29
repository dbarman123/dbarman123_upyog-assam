/*
 * UPYOG  SmartCity eGovernance suite aims to improve the internal efficiency,transparency,
 * accountability and the service delivery of the government  organizations.
 *
 *  Copyright (C) <2019>  eGovernments Foundation
 *
 *  The updated version of eGov suite of products as by eGovernments Foundation
 *  is available at http://www.egovernments.org
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see http://www.gnu.org/licenses/ or
 *  http://www.gnu.org/licenses/gpl.html .
 *
 *  In addition to the terms of the GPL license to be adhered to in using this
 *  program, the following additional terms are to be complied with:
 *
 *      1) All versions of this program, verbatim or modified must carry this
 *         Legal Notice.
 *      Further, all user interfaces, including but not limited to citizen facing interfaces,
 *         Urban Local Bodies interfaces, dashboards, mobile applications, of the program and any
 *         derived works should carry eGovernments Foundation logo on the top right corner.
 *
 *      For the logo, please refer http://egovernments.org/html/logo/egov_logo.png.
 *      For any further queries on attribution, including queries on brand guidelines,
 *         please contact contact@egovernments.org
 *
 *      2) Any misrepresentation of the origin of the material is prohibited. It
 *         is required that all modified versions of this material be marked in
 *         reasonable ways as different from the original version.
 *
 *      3) This license does not grant any rights to any user of the program
 *         with regards to rights under trademark law for use of the trade names
 *         or trademarks of eGovernments Foundation.
 *
 *  In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
 */

package org.egov.edcr.feature;

import static org.egov.edcr.constants.CommonFeatureConstants.CAPACITY_PREFIX;
import static org.egov.edcr.constants.CommonFeatureConstants.NOT_DEFINED_IN_PLAN;
import static org.egov.edcr.constants.CommonFeatureConstants.OVERHEAD_PREFIX;
import static org.egov.edcr.constants.CommonFeatureConstants.PIPE_PREFIX;
import static org.egov.edcr.constants.CommonFeatureConstants.SETTLING_PREFIX;
import static org.egov.edcr.constants.EdcrReportConstants.RULE_51;
import static org.egov.edcr.constants.EdcrReportConstants.RULE_51_DESCRIPTION;
import static org.egov.edcr.constants.EdcrReportConstants.RWH_DECLARATION_ERROR;
import static org.egov.edcr.service.FeatureUtil.addScrutinyDetailtoPlan;
import static org.egov.edcr.service.FeatureUtil.mapReportDetails;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.egov.common.entity.edcr.FeatureEnum;
import org.egov.common.entity.edcr.MdmsFeatureRule;
import org.egov.common.entity.edcr.OccupancyTypeHelper;
import org.egov.common.entity.edcr.PercolationPit;
import org.egov.common.entity.edcr.Plan;
import org.egov.common.entity.edcr.RainWaterHarvestingRequirement;
import org.egov.common.entity.edcr.ReportScrutinyDetail;
import org.egov.common.entity.edcr.Result;
import org.egov.common.entity.edcr.ScrutinyDetail;
import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.entity.blackbox.PlanDetail;
import org.egov.edcr.service.MDMSCacheManager;
import org.egov.edcr.utility.DcrConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RainWaterHarvesting_Assam extends FeatureProcess {
	private static final Logger LOG = LogManager.getLogger(RainWaterHarvesting_Assam.class);

    @Autowired
   	MDMSCacheManager cache;
    
    @Override
    public Plan validate(Plan pl) {
        return pl;
    }
    
    /**
     * Processes the given {@link Plan} object to evaluate and validate compliance 
     * with rainwater harvesting requirements based on occupancy type and plot area.
     * 
     * <p>
     * This method performs the following:
     * <ul>
     *   <li>Initializes scrutiny detail columns for reporting.</li>
     *   <li>Fetches the most restrictive FAR (Floor Area Ratio) occupancy type for the virtual building.</li>
     *   <li>Retrieves permissible rainwater harvesting values from MDMS feature rules.</li>
     *   <li>Checks if the occupancy type and plot area require rainwater harvesting compliance.</li>
     *   <li>If conditions are met, it adds compliance details to the report output.</li>
     * </ul>
     * </p>
     *
     * @param pl The {@link Plan} object that includes plot and building details.
     * @return The updated {@link Plan} object with any added scrutiny or error details.
     */
@Override
public Plan process(Plan pl) {
    // Initialize a map to store errors
    HashMap<String, String> errors = new HashMap<>();

    // Initialize scrutiny details for the report
    scrutinyDetail = new ScrutinyDetail();
    scrutinyDetail.addColumnHeading(1, RULE_NO); // Column for rule number
    scrutinyDetail.addColumnHeading(2, DESCRIPTION);
    scrutinyDetail.addColumnHeading(3, REQUIRED);// Column for description
    scrutinyDetail.addColumnHeading(4, PROVIDED); // Column for provided values
    scrutinyDetail.addColumnHeading(5, STATUS); // Column for status (Accepted/Not Accepted)
    scrutinyDetail.setKey(Common_Rain_Water_Harvesting); // Key for the scrutiny detail

    // Define rule and description for rainwater harvesting
    String subRule = RULE_51;
    String subRuleDesc = RULE_51_DESCRIPTION;

	// Initialize variables for plot area and permissible values
    BigDecimal plotArea = pl.getPlot() != null ? pl.getPlot().getArea() : BigDecimal.ZERO;
    BigDecimal rainWaterHarvestingPermissibleValue = BigDecimal.ZERO;

    // Get the most restrictive FAR helper for the virtual building
    OccupancyTypeHelper mostRestrictiveFarHelper = pl.getVirtualBuilding() != null
            ? pl.getVirtualBuilding().getMostRestrictiveFarHelper()
            : null;


	List<Object> rules = cache.getFeatureRules(pl, FeatureEnum.RAIN_WATER_HARVESTING.getValue(), false);
    Optional<RainWaterHarvestingRequirement> matchedRule = rules.stream()
        .filter(RainWaterHarvestingRequirement.class::isInstance)
        .map(RainWaterHarvestingRequirement.class::cast)
        .findFirst();

        	if (matchedRule.isPresent()) {
        	    MdmsFeatureRule rule = matchedRule.get();
        	    rainWaterHarvestingPermissibleValue = rule.getPermissible();
        	} 
    // Validate the plot area and occupancy type for rainwater harvesting
    if (mostRestrictiveFarHelper != null && mostRestrictiveFarHelper.getType() != null) {
        if (DxfFileConstants.A.equalsIgnoreCase(mostRestrictiveFarHelper.getType().getCode()) &&
                plotArea.compareTo(rainWaterHarvestingPermissibleValue) >= 0) {
            addOutput(pl, errors, subRule, subRuleDesc); // Add output for compliance
        } else if (DxfFileConstants.F.equalsIgnoreCase(mostRestrictiveFarHelper.getType().getCode())) {
            addOutput(pl, errors, subRule, subRuleDesc); // Add output for compliance
        } else if (DxfFileConstants.G.equalsIgnoreCase(mostRestrictiveFarHelper.getType().getCode())) {
            addOutput(pl, errors, subRule, subRuleDesc); // Add output for compliance
        }
    }

    validatePercolationPit(pl, plotArea);
    return pl; // Return the updated plan object
}

/**
 * Adds output details for rainwater harvesting compliance.
 *
 * @param pl The plan object
 * @param errors The map of errors
 * @param subRule The rule number
 * @param subRuleDesc The rule description
 */
private void addOutput(Plan pl, HashMap<String, String> errors, String subRule, String subRuleDesc) {
    if (pl.getPlanInformation() != null && pl.getPlanInformation().getRwhDeclared() != null) {
        if (pl.getPlanInformation().getRwhDeclared().equalsIgnoreCase(DcrConstants.NO)
                || pl.getPlanInformation().getRwhDeclared().equalsIgnoreCase(DcrConstants.NA)) {
            // Add error if rainwater harvesting is not declared
            errors.put(DxfFileConstants.RWH_DECLARED, RWH_DECLARATION_ERROR);
            pl.addErrors(errors);
            addReportOutput(pl, subRule, subRuleDesc); // Add report output
        } else {
            addReportOutput(pl, subRule, subRuleDesc); // Add report output
        }
    }
}

/**
 * Adds report output details for rainwater harvesting compliance.
 *
 * @param pl The plan object
 * @param subRule The rule number
 * @param subRuleDesc The rule description
 */
private void addReportOutput(Plan pl, String subRule, String subRuleDesc) {
    if (pl.getUtility() != null) {
        if (pl.getUtility().getRainWaterHarvest() != null && !pl.getUtility().getRainWaterHarvest().isEmpty()) {
            // Add report output if rainwater harvesting is defined
            setReportOutputDetails(pl, subRule, subRuleDesc, null,
                    CAPACITY_PREFIX + pl.getUtility().getRainWaterHarvestingTankCapacity(),
                    Result.Verify.getResultVal());
        }
        else {
            // Add report output if rainwater harvesting is not defined
            setReportOutputDetails(pl, subRule, subRuleDesc, null,
                    NOT_DEFINED_IN_PLAN,
                    Result.Not_Accepted.getResultVal());
        }
     // Pipe Diameter
        if (pl.getUtility().getRainWaterHarvest() != null && !pl.getUtility().getRainWaterHarvest().isEmpty()) {
            // Add report output if rainwater harvesting is defined
            setReportOutputDetails(pl, subRule, subRuleDesc, null,
                    PIPE_PREFIX + pl.getUtility().getRwhPipeDia(),
                    Result.Verify.getResultVal());
        }
        if (pl.getUtility().getSettlingTank() != null && !pl.getUtility().getSettlingTank().isEmpty()) {
            // Add report output if settling tank  is defined
            setReportOutputDetails(pl, subRule, subRuleDesc, null,
                    SETTLING_PREFIX + pl.getUtility().getSettlingTank(),
                    Result.Verify.getResultVal());
        }
        if (pl.getUtility().getOverheadTank() != null && !pl.getUtility().getOverheadTank().isEmpty()) {
            // Add report output if settling tank  is defined
            setReportOutputDetails(pl, subRule, subRuleDesc, null,
                    OVERHEAD_PREFIX + pl.getUtility().getOverheadTank(),
                    Result.Verify.getResultVal());
        }
        
    }
}

/**
 * Validates the percolation pit requirements as per building bye-laws.
 * <p>
 * This method checks:
 * <ul>
 *   <li>If the required number of percolation pits are provided based on the plot area.</li>
 *   <li>If each provided pit matches the required dimensions (1.2m x 1.2m x 1.5m).</li>
 * </ul>
 *
 * @param pl       the {@link PlanDetail} object containing plot and utility information
 * @param plotArea the plot area used for calculating required number of pits
 */
private void validatePercolationPit(Plan pl, BigDecimal plotArea) {

    LOG.info("Starting percolation pit validation for plot area: {}", plotArea);

    List<PercolationPit> pits = (pl.getUtility() != null) ? pl.getUtility().getPercolationPits() : null;

    if (pits != null && !pits.isEmpty()) {

        BigDecimal pitRate = new BigDecimal("100");
        int requiredPits = plotArea.divide(pitRate, 0, RoundingMode.UP).intValue();

        BigDecimal requiredVolumePerPit = new BigDecimal("1.2")
                .multiply(new BigDecimal("1.2"))
                .multiply(new BigDecimal("1.5")); // 2.16 m³

        BigDecimal requiredTotalVolume = requiredVolumePerPit.multiply(new BigDecimal(requiredPits));

        int providedPits = pits.size();
        BigDecimal providedTotalVolume = BigDecimal.ZERO;

        // ---- Step 2: Provided calculations ----
        for (PercolationPit pit : pits) {
            BigDecimal totalVolume = calculatePitVolume(pit);
            providedTotalVolume = providedTotalVolume.add(totalVolume);
        }

        providedTotalVolume = providedTotalVolume.setScale(2, RoundingMode.HALF_UP);

        LOG.info("Required pits: {}, Provided pits: {}", requiredPits, providedPits);
        LOG.info("Required total volume: {} m³, Provided total volume: {} m³",
                requiredTotalVolume, providedTotalVolume);

        String volumeCompliance = (providedTotalVolume.compareTo(requiredTotalVolume) >= 0)
                ? Result.Accepted.getResultVal()
                : Result.Not_Accepted.getResultVal();

        // ---- Step 4: Reporting ----
        setReportOutputDetails(pl, RULE_51, "Percolation Pit Count",
                requiredPits + " (1 pit / 100 sq.m)",
                String.valueOf(providedPits),
                providedPits >= requiredPits ? Result.Accepted.getResultVal() : Result.Not_Accepted.getResultVal());

        setReportOutputDetails(pl, RULE_51, "Percolation Pit Volume (m³)",
                String.format("%.2f", requiredTotalVolume),
                String.format("%.2f", providedTotalVolume),
                volumeCompliance);

        // ---- Step 5: Individual pit details ----
        for (int i = 0; i < pits.size(); i++) {
            PercolationPit pit = pits.get(i);
            BigDecimal totalVolume = calculatePitVolume(pit);

            String lengthStr = pit.getPitLength() != null && !pit.getPitLength().isEmpty()
                    ? pit.getPitLength().get(0).setScale(2, RoundingMode.HALF_UP).toPlainString()
                    : "-";
            String widthStr = pit.getPitWidth() != null && !pit.getPitWidth().isEmpty()
                    ? pit.getPitWidth().get(0).setScale(2, RoundingMode.HALF_UP).toPlainString()
                    : "-";
            String heightStr = pit.getPitHeight() != null
                    ? pit.getPitHeight().setScale(2, RoundingMode.HALF_UP).toPlainString()
                    : "-";

            String dimensionStr = lengthStr + "m × " + widthStr + "m × " + heightStr + "m = "
                    + String.format("%.2f", totalVolume) + "m³";

            setReportOutputDetails(pl, RULE_51,
                    "Percolation Pit-" + (i + 1) + " Dimension",
                    "1.20m × 1.20m × 1.50m = 2.16m³",
                    dimensionStr,
                    totalVolume.compareTo(requiredVolumePerPit) >= 0
                            ? Result.Accepted.getResultVal()
                            : Result.Not_Accepted.getResultVal());
        }

    } else {
        LOG.info("No percolation pits found for validation.");
        setReportOutputDetails(pl, RULE_51, "Percolation Pit Requirement",
                "Required as per 1 pit per 100 sq.m plot area (1.2m × 1.2m × 1.5m each)",
                "Not Provided",
                Result.Not_Accepted.getResultVal());
    }

    LOG.info("Completed percolation pit validation.");
}



private BigDecimal calculatePitVolume(PercolationPit pit) {
    BigDecimal totalVolume = BigDecimal.ZERO;
    List<BigDecimal> lengths = pit.getPitLength();
    List<BigDecimal> widths = pit.getPitWidth();
    BigDecimal height = pit.getPitHeight();

    if (lengths != null && widths != null && height != null) {
        int count = Math.min(lengths.size(), widths.size());
        for (int i = 0; i < count; i++) {
            BigDecimal length = lengths.get(i);
            BigDecimal width = widths.get(i);
            if (length != null && width != null) {
                totalVolume = totalVolume.add(length.multiply(width).multiply(height));
            }
        }
    }
    return totalVolume;
}

/**
 * Sets the report output details for scrutiny.
 *
 * @param pl The plan object
 * @param ruleNo The rule number
 * @param ruleDesc The rule description
 * @param expected The expected value
 * @param actual The actual value
 * @param status The validation status
 */
private void setReportOutputDetails(Plan pl, String ruleNo, String ruleDesc, String expected, String actual,
        String status) {
    ReportScrutinyDetail detail = new ReportScrutinyDetail();
    detail.setRuleNo(ruleNo);
    detail.setDescription(ruleDesc);
    detail.setRequired(expected);
    detail.setProvided(actual);
    detail.setStatus(status);

    Map<String, String> details = mapReportDetails(detail);
    addScrutinyDetailtoPlan(scrutinyDetail, pl, details);
}

@Override
public Map<String, Date> getAmendments() {
    return new LinkedHashMap<>(); // Return an empty map for amendments
}
}
