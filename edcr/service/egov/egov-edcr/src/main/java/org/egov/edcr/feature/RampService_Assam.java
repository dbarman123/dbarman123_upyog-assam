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
 *         derived works should carry eGovernments Foundation LOGGERo on the top right corner.
 *
 *      For the LOGGERo, please refer http://egovernments.org/html/LOGGERo/egov_LOGGERo.png.
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

import static org.egov.edcr.constants.CommonFeatureConstants.DA_RAMP;
import static org.egov.edcr.constants.CommonFeatureConstants.DA_RAMP_DEFINED;
import static org.egov.edcr.constants.CommonFeatureConstants.DA_RAMP_LANDING;
import static org.egov.edcr.constants.CommonFeatureConstants.DA_RAMP_MAX_SLOPE;
import static org.egov.edcr.constants.CommonFeatureConstants.DA_RAMP_SLOPE;
import static org.egov.edcr.constants.CommonFeatureConstants.DA_ROOM;
import static org.egov.edcr.constants.CommonFeatureConstants.EMPTY_STRING;
import static org.egov.edcr.constants.CommonFeatureConstants.FLOOR;
import static org.egov.edcr.constants.CommonFeatureConstants.LESS_THAN_SLOPE;
import static org.egov.edcr.constants.CommonFeatureConstants.MIN_NUMBER_DA_ROOMS;
import static org.egov.edcr.constants.CommonFeatureConstants.RAMP_MAX_SLOPE;
import static org.egov.edcr.constants.CommonFeatureConstants.RAMP_MIN_WIDTH;
import static org.egov.edcr.constants.CommonFeatureConstants.RAMP_LENGTH_WIDTH;
import static org.egov.edcr.constants.CommonFeatureConstants.RAMP_POLYLINE_ERROR;
import static org.egov.edcr.constants.CommonFeatureConstants.UNDERSCORE;
import static org.egov.edcr.constants.CommonKeyConstants.BLOCK;
import static org.egov.edcr.constants.CommonKeyConstants.DA_RAMP_NUMBER;
import static org.egov.edcr.constants.CommonKeyConstants.SLOPE_STRING;
import static org.egov.edcr.constants.DxfFileConstants.A_R;
import static org.egov.edcr.constants.EdcrReportConstants.SUBRULE_50_C_4_B;
import static org.egov.edcr.constants.EdcrReportConstants.SUBRULE_50_C_4_B_SLOPE_DESCRIPTION;
import static org.egov.edcr.constants.EdcrReportConstants.SUBRULE_50_C_4_B_SLOPE_MAN_DESC;
import static org.egov.edcr.service.FeatureUtil.addScrutinyDetailtoPlan;
import static org.egov.edcr.service.FeatureUtil.mapReportDetails;
import static org.egov.edcr.constants.EdcrReportConstants.RULE_RAMP_LENGTH;
import static org.egov.edcr.constants.EdcrReportConstants.DESC_RAMP_WIDTH;
import static org.egov.edcr.constants.EdcrReportConstants.RULE_RAMP_WIDTH;
import static org.egov.edcr.constants.EdcrReportConstants.PERMISSIBLE_WIDTH;
import static org.egov.edcr.constants.EdcrReportConstants.NOT_DEFINED;
import static org.egov.edcr.constants.EdcrReportConstants.DESC_RAMP_LENGTH;
import static org.egov.edcr.constants.EdcrReportConstants.PERMISSIBLE_LENGTH;
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
import org.egov.common.entity.edcr.Block;
import org.egov.common.entity.edcr.DARamp;
import org.egov.common.entity.edcr.FeatureEnum;
import org.egov.common.entity.edcr.Floor;
import org.egov.common.entity.edcr.Measurement;
import org.egov.common.entity.edcr.OccupancyType;
import org.egov.common.entity.edcr.OccupancyTypeHelper;
import org.egov.common.entity.edcr.Plan;
import org.egov.common.entity.edcr.Ramp;
import org.egov.common.entity.edcr.RampLanding;
import org.egov.common.entity.edcr.RampServiceRequirement;
import org.egov.common.entity.edcr.ReportScrutinyDetail;
import org.egov.common.entity.edcr.Result;
import org.egov.common.entity.edcr.ScrutinyDetail;
import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.service.MDMSCacheManager;
import org.egov.edcr.utility.DcrConstants;
import org.egov.edcr.utility.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import static org.egov.edcr.constants.CommonFeatureConstants.RAMP_HT;

@Service
public class RampService_Assam extends RampService {
	private static final Logger LOGGER = LogManager.getLogger(RampService_Assam.class);

    @Autowired
	MDMSCacheManager cache;
 
    /**
     * Validates the given Plan object for ramp and DA ramp requirements.
     *
     * @param pl the Plan object to validate
     * @return the validated Plan object with errors added if any
     */
    @Override
    public Plan validate(Plan pl) {
        validateRampMeasurements(pl);
        validateDARamps(pl);
        return pl;
    }

    /**
     * Validates ramp measurements for each floor of every block in the plan.
     *
     * @param pl the Plan object containing blocks and floors to validate ramps
     */
    private void validateRampMeasurements(Plan pl) {
        for (Block block : pl.getBlocks()) {
            if (block.getBuilding() != null && !block.getBuilding().getFloors().isEmpty()) {
                for (Floor floor : block.getBuilding().getFloors()) {
                    List<Ramp> ramps = floor.getRamps();
                    if (ramps != null && !ramps.isEmpty()) {
                        for (Ramp ramp : ramps) {
                            List<Measurement> rampPolyLines = ramp.getRamps();
                            if (rampPolyLines != null && !rampPolyLines.isEmpty()) {
                                validateDimensions(pl, block.getNumber(), floor.getNumber(), ramp.getNumber().toString(), rampPolyLines);
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Validates the presence and slope of DA (Differently Abled) ramps in each block of the plan.
     *
     * @param pl the Plan object containing blocks and occupancy information
     */

    private void validateDARamps(Plan pl) {
        HashMap<String, String> errors = new HashMap<>();
        OccupancyTypeHelper mostRestrictiveOccupancyType = pl.getVirtualBuilding().getMostRestrictiveFarHelper();

        if (pl != null && !pl.getBlocks().isEmpty()) {
            for (Block block : pl.getBlocks()) {
                if (shouldValidateDARamps(pl, block, mostRestrictiveOccupancyType)) {
                    if (!block.getDARamps().isEmpty()) {
                        validateSlopeForDARamps(pl, block, errors);
                    } else {
                        addMissingDARampError(pl, block, errors);
                        break;
                    }
                }
            }
        }
    }
    
    /**
     * Determines if the DA ramp validation should be performed for a given block based on occupancy and plot conditions.
     *
     * @param pl the Plan object
     * @param block the Block to be checked
     * @param mostRestrictiveOccupancyType the most restrictive occupancy type in the plan
     * @return true if validation should be performed; false otherwise
     */

    private boolean shouldValidateDARamps(Plan pl, Block block, OccupancyTypeHelper mostRestrictiveOccupancyType) {
        return pl.getPlot() != null
                && !Util.checkExemptionConditionForSmallPlotAtBlkLevel(pl.getPlot(), block)
                && mostRestrictiveOccupancyType != null
                && mostRestrictiveOccupancyType.getSubtype() != null
                && !A_R.equalsIgnoreCase(mostRestrictiveOccupancyType.getSubtype().getCode());
    }

    /**
     * Validates the slope values for DA ramps in a block and adds errors to the plan if not defined.
     *
     * @param pl the Plan object to add errors to
     * @param block the Block containing DA ramps
     * @param errors map of errors to be added
     */
    private void validateSlopeForDARamps(Plan pl, Block block, Map<String, String> errors) {
        boolean isSlopeDefined = false;
        for (DARamp daRamp : block.getDARamps()) {
            if (daRamp != null && daRamp.getSlope() != null && daRamp.getSlope().compareTo(BigDecimal.valueOf(0)) > 0) {
                isSlopeDefined = true;
            }

    		OccupancyTypeHelper mostRestrictiveOccupancyType = block.getBuilding().getMostRestrictiveFarHelper();
            ScrutinyDetail scrutinyDetail = createScrutinyDetail(DA_RAMP_LANDING, block.getNumber(), false);
        	List<RampLanding> landings = daRamp.getLandings();
			if (!landings.isEmpty()) {
				validateLanding(pl, block, scrutinyDetail, mostRestrictiveOccupancyType,
						 daRamp, landings);
			} 
        }
        if (!isSlopeDefined) {
            errors.put(String.format(DcrConstants.RAMP_SLOPE, EMPTY_STRING, block.getNumber()),
                    edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED,
                            new String[]{String.format(DcrConstants.RAMP_SLOPE, EMPTY_STRING, block.getNumber())},
                            LocaleContextHolder.getLocale()));
            pl.addErrors(errors);
        }
    }

    /**
     * Validates the width of ramp landings against required minimum width
     * based on the building plan, block, and occupancy type. It processes
     * each RampLanding in the provided list, checks if their widths meet
     * the minimum requirements, and sets report output details accordingly.
     * Detailed debug logs are recorded throughout the process.
     *
     * @param plan the building plan containing overall project details
     * @param block the specific block of the building under scrutiny
     * @param scrutinyDetailLanding the scrutiny detail object for reporting validation results
     * @param mostRestrictiveOccupancyType the occupancy type helper which defines restrictions to consider
     * @param daRamp the DARamp object related to the ramp under validation
     * @param landings the list of RampLanding objects to validate
     */
    
    private void validateLanding(Plan plan, Block block, ScrutinyDetail scrutinyDetailLanding,
            OccupancyTypeHelper mostRestrictiveOccupancyType, 
            DARamp daRamp, List<RampLanding> landings) {

        LOGGER.debug("Starting validateLanding with {} landings", landings.size());
        
        for (RampLanding landing : landings) {
            List<BigDecimal> widths = landing.getWidths();
            if (!widths.isEmpty()) {
                BigDecimal landingWidth = widths.stream().reduce(BigDecimal::min).get();
                LOGGER.debug("Minimum width from landing widths: {}", landingWidth);

                BigDecimal minWidth = BigDecimal.ZERO;
                boolean valid = false;

                minWidth = Util.roundOffTwoDecimal(landingWidth);
                BigDecimal minimumWidth = getRequiredWidth(plan, block, mostRestrictiveOccupancyType);
                LOGGER.debug("Required minimum width: {}, Rounded landing width: {}", minimumWidth, minWidth);

                if (minWidth.compareTo(minimumWidth) >= 0) {
                    valid = true;
                    LOGGER.debug("Landing width is valid");
                } else {
                    LOGGER.debug("Landing width is NOT valid");
                }

                Map<String, String> mapOfRampNumberAndSlopeValues = new HashMap<>();

                if (valid) {
                    setReportOutputDetails(plan, SUBRULE_50_C_4_B,
                        String.format(SUBRULE_50_C_4_B_SLOPE_DESCRIPTION,
                            mapOfRampNumberAndSlopeValues.get(DA_RAMP_NUMBER)),
                        minimumWidth.toString(), minWidth.toString(),
                        Result.Accepted.getResultVal(), scrutinyDetailLanding);
                    LOGGER.debug("Report output set as Accepted");
                } else {
                    setReportOutputDetails(plan, SUBRULE_50_C_4_B,
                        String.format(SUBRULE_50_C_4_B_SLOPE_DESCRIPTION, EMPTY_STRING),
                        minimumWidth.toString(), minWidth.toString(),
                        Result.Not_Accepted.getResultVal(), scrutinyDetailLanding);
                    LOGGER.debug("Report output set as Not Accepted");
                }

            } else {
                LOGGER.warn("No widths found for a landing; skipping validation");
            }
        }
    }
    /**
     * Retrieves the required minimum ramp width based on the given building plan,
     * block, and the most restrictive occupancy type. It looks up applicable
     * ramp service rules from the cache and returns the corresponding width.
     * If no matching rule is found, it returns zero.
     *
     * @param pl the building plan for which width requirement is sought
     * @param block the block of the building considered in validation
     * @param mostRestrictiveOccupancyType the occupancy type helper indicating restrictions
     * @return the required ramp width as a BigDecimal; zero if no rule applies
     */
    private BigDecimal getRequiredWidth(Plan pl, Block block, OccupancyTypeHelper mostRestrictiveOccupancyType) {
        LOGGER.debug("Getting required width for plan: {}, block: {}, occupancyType: {}",
                pl, block, mostRestrictiveOccupancyType);

        BigDecimal value = BigDecimal.ZERO;

        List<Object> rules = cache.getFeatureRules(pl, FeatureEnum.RAMP_SERVICE.getValue(), false);
        Optional<RampServiceRequirement> matchedRule = rules.stream()
                .filter(RampServiceRequirement.class::isInstance)
                .map(RampServiceRequirement.class::cast)
                .findFirst();

        if (matchedRule.isPresent()) {
            RampServiceRequirement rule = matchedRule.get();
            value = rule.getRampServiceWidth();
            LOGGER.debug("Found matching ramp service rule with width: {}", value);
        } else {
            value = BigDecimal.ZERO;
            LOGGER.warn("No matching ramp service rule found; returning width zero");
        }

        return value;
    }


    /**
     * Adds an error to the Plan indicating the absence of a required DA ramp in the block.
     *
     * @param pl the Plan object to add errors to
     * @param block the Block missing the DA ramp
     * @param errors map of errors to be added
     */
    private void addMissingDARampError(Plan pl, Block block, Map<String, String> errors) {
        errors.put(String.format(DA_RAMP, block.getNumber()),
                edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED,
                        new String[]{String.format(DA_RAMP, block.getNumber())},
                        LocaleContextHolder.getLocale()));
        pl.addErrors(errors);
    }

    /**
     * Checks if a given occupancy type is one for which ramps are required.
     *
     * @param occupancyType the OccupancyType to check
     * @return true if the occupancy type requires ramps; false otherwise
     */

    private boolean getOccupanciesForRamp(OccupancyType occupancyType) {
        return occupancyType.equals(OccupancyType.OCCUPANCY_A2) ||
                occupancyType.equals(OccupancyType.OCCUPANCY_A3) || occupancyType.equals(OccupancyType.OCCUPANCY_A4) ||
                occupancyType.equals(OccupancyType.OCCUPANCY_B1) || occupancyType.equals(OccupancyType.OCCUPANCY_B2) ||
                occupancyType.equals(OccupancyType.OCCUPANCY_B3) ||
                occupancyType.equals(OccupancyType.OCCUPANCY_C) || occupancyType.equals(OccupancyType.OCCUPANCY_C1) ||
                occupancyType.equals(OccupancyType.OCCUPANCY_C2) || occupancyType.equals(OccupancyType.OCCUPANCY_C3) ||
                occupancyType.equals(OccupancyType.OCCUPANCY_D) || occupancyType.equals(OccupancyType.OCCUPANCY_D1) ||
                occupancyType.equals(OccupancyType.OCCUPANCY_D2) || occupancyType.equals(OccupancyType.OCCUPANCY_E) ||
                occupancyType.equals(OccupancyType.OCCUPANCY_F) || occupancyType.equals(OccupancyType.OCCUPANCY_F1) ||
                occupancyType.equals(OccupancyType.OCCUPANCY_F2) || occupancyType.equals(OccupancyType.OCCUPANCY_F3) ||
                occupancyType.equals(OccupancyType.OCCUPANCY_F4);
    }

    
    /**
     * Processes the Plan object for compliance with ramp and DA ramp rules,
     * performing validations and adding scrutiny details.
     *
     * @param pl the Plan object to process
     * @return the processed Plan with scrutiny details
     */
    @Override
    public Plan process(Plan pl) {
        BigDecimal rampServiceValueOne = BigDecimal.ZERO;
        BigDecimal rampServiceExpectedSlopeOne = BigDecimal.ZERO;
        BigDecimal rampServiceDivideExpectedSlope = BigDecimal.ZERO;
        BigDecimal rampServiceSlopValue = BigDecimal.ZERO;
        BigDecimal rampServiceBuildingHeight = BigDecimal.ZERO;
        BigDecimal rampServiceTotalLength = BigDecimal.ZERO;
        BigDecimal rampServiceExpectedSlopeCompare = BigDecimal.ZERO;
        BigDecimal rampServiceExpectedSlopeTwo = BigDecimal.ZERO;
        BigDecimal rampServiceExpectedSlopeCompareTrue = BigDecimal.ZERO;
        BigDecimal rampServiceExpectedSlopeCompareFalse = BigDecimal.ZERO;
        BigDecimal rampServiceMinHeightEntrance = BigDecimal.ZERO;


        validate(pl);

        if (pl != null && !pl.getBlocks().isEmpty()) {
            for (Block block : pl.getBlocks()) {
                ScrutinyDetail scrutinyDetail = createScrutinyDetail(DA_RAMP_DEFINED, block.getNumber(), false);
                ScrutinyDetail scrutinyDetail1 = createScrutinyDetail(DA_RAMP_SLOPE, block.getNumber(), false);
                ScrutinyDetail scrutinyDetail2 = createScrutinyDetail(DA_RAMP_MAX_SLOPE, block.getNumber(), false);
                ScrutinyDetail scrutinyDetail3 = createScrutinyDetail(DA_ROOM, block.getNumber(), false);
                ScrutinyDetail scrutinyDetail4 = createScrutinyDetail(RAMP_MIN_WIDTH, block.getNumber(), true);
                ScrutinyDetail scrutinyDetail5 = createScrutinyDetail(RAMP_MAX_SLOPE, block.getNumber(), true);
                ScrutinyDetail scrutinyDetail7 = createScrutinyDetail(RAMP_LENGTH_WIDTH, block.getNumber(), false);
                ScrutinyDetail scrutinyDetail6 = createScrutinyDetail(RAMP_HT, block.getNumber(), false);


                List<Object> rules = cache.getFeatureRules(pl, FeatureEnum.RAMP_SERVICE.getValue(), false);
                Optional<RampServiceRequirement> matchedRule = rules.stream()
                    .filter(RampServiceRequirement.class::isInstance)
                    .map(RampServiceRequirement.class::cast)
                    .findFirst();

                if (matchedRule.isPresent()) {
                	RampServiceRequirement rule = matchedRule.get();
                    rampServiceValueOne = rule.getRampServiceValueOne();
                    rampServiceExpectedSlopeOne = rule.getRampServiceExpectedSlopeOne();
                    rampServiceDivideExpectedSlope = rule.getRampServiceDivideExpectedSlope();
                    rampServiceSlopValue = rule.getRampServiceSlopValue();
                    rampServiceBuildingHeight = rule.getRampServiceBuildingHeight();
                    rampServiceTotalLength = rule.getRampServiceTotalLength();
                    rampServiceExpectedSlopeTwo = rule.getRampServiceExpectedSlopeTwo();
                    rampServiceExpectedSlopeCompare = rule.getRampServiceExpectedSlopeCompare();
                    rampServiceExpectedSlopeCompareTrue = rule.getRampServiceExpectedSlopeCompareTrue();
                    rampServiceExpectedSlopeCompareFalse = rule.getRampServiceExpectedSlopeCompareFalse();
                    rampServiceMinHeightEntrance = rule.getRampServiceMinHeightEntrance();
                }

                processRampSlopeValidation(pl, block, rampServiceValueOne, rampServiceExpectedSlopeOne,
                        rampServiceDivideExpectedSlope, rampServiceSlopValue, scrutinyDetail1, scrutinyDetail2);

                processDARoomValidation(pl, block, rampServiceBuildingHeight, scrutinyDetail3);
                
                validateMinHeightEntrance(pl, block, rampServiceMinHeightEntrance, scrutinyDetail6);
                
                validateDARampDimensions(pl, block, scrutinyDetail7);

            }
        }

        return pl;
    }
    
    /**
     * Creates a ScrutinyDetail object with given parameters.
     *
     * @param keySuffix suffix to be used in the scrutiny detail key
     * @param blockNumber the number of the block being validated
     * @param hasFloorColumn whether floor column should be added
     * @return the constructed ScrutinyDetail object
     */

    private ScrutinyDetail createScrutinyDetail(String keySuffix, String blockNumber, boolean hasFloorColumn) {
        ScrutinyDetail scrutinyDetail = new ScrutinyDetail();
        scrutinyDetail.addColumnHeading(1, RULE_NO);
        scrutinyDetail.addColumnHeading(2, DESCRIPTION);
        int columnIndex = 3;
        if (hasFloorColumn) {
            scrutinyDetail.addColumnHeading(columnIndex++, FLOOR);
        }
        scrutinyDetail.addColumnHeading(columnIndex++, REQUIRED);
        scrutinyDetail.addColumnHeading(columnIndex++, PROVIDED);
        scrutinyDetail.addColumnHeading(columnIndex, STATUS);
        scrutinyDetail.setKey(BLOCK + blockNumber + UNDERSCORE + keySuffix);
        if (keySuffix.equals(DA_ROOM)) {
            scrutinyDetail.setSubHeading(MIN_NUMBER_DA_ROOMS);
        }
        return scrutinyDetail;
    }
    
    /**
     * Performs validation of DA ramp slope for the given block using specified slope values and adds scrutiny details.
     *
     * @param pl the Plan object
     * @param block the Block containing the DA ramps
     * @param rampServiceValueOne base value to check if slope is defined
     * @param rampServiceExpectedSlopeOne numerator of expected slope calculation
     * @param rampServiceDivideExpectedSlope denominator of expected slope calculation
     * @param rampServiceSlopValue minimum allowed slope value
     * @param scrutinyDetail1 scrutiny detail for slope definition
     * @param scrutinyDetail2 scrutiny detail for slope compliance
     */

    private void processRampSlopeValidation(Plan pl, Block block, BigDecimal rampServiceValueOne,
            BigDecimal rampServiceExpectedSlopeOne, BigDecimal rampServiceDivideExpectedSlope,
            BigDecimal rampServiceSlopValue, ScrutinyDetail scrutinyDetail1, ScrutinyDetail scrutinyDetail2) {

        OccupancyTypeHelper mostRestrictiveOccupancyType = pl.getVirtualBuilding().getMostRestrictiveFarHelper();

        if (pl.getPlot() != null
                && !Util.checkExemptionConditionForSmallPlotAtBlkLevel(pl.getPlot(), block)
                && mostRestrictiveOccupancyType != null
                && mostRestrictiveOccupancyType.getSubtype() != null
                && !A_R.equalsIgnoreCase(mostRestrictiveOccupancyType.getSubtype().getCode())) {

            if (!block.getDARamps().isEmpty()) {
                boolean isSlopeDefined = isSlopeDefined(block, rampServiceValueOne);

                setReportOutputDetails(pl, SUBRULE_50_C_4_B, SUBRULE_50_C_4_B_SLOPE_MAN_DESC, EMPTY_STRING,
                        isSlopeDefined ? DcrConstants.OBJECTDEFINED_DESC : DcrConstants.OBJECTNOTDEFINED_DESC,
                        isSlopeDefined ? Result.Accepted.getResultVal() : Result.Not_Accepted.getResultVal(),
                        scrutinyDetail1);

                if (isSlopeDefined) {
                    validateRampSlopes(pl, block, rampServiceExpectedSlopeOne, rampServiceDivideExpectedSlope,
                            rampServiceSlopValue, scrutinyDetail2);
                }
            }
        }
    }

    /**
     * Checks if any DA ramp in the block has a slope defined above a specified threshold.
     *
     * @param block the Block to check
     * @param rampServiceValueOne minimum slope value to consider as defined
     * @return true if a valid slope is defined; false otherwise
     */
    private boolean isSlopeDefined(Block block, BigDecimal rampServiceValueOne) {
        for (DARamp daRamp : block.getDARamps()) {
            if (daRamp != null && daRamp.getSlope() != null
                    && daRamp.getSlope().compareTo(rampServiceValueOne) > 0) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Validates that DA ramp slopes in the block fall within the allowed maximum slope.
     *
     * @param pl the Plan object
     * @param block the Block containing DA ramps
     * @param expectedSlopeOne numerator for slope validation
     * @param divideExpectedSlope denominator for slope calculation
     * @param rampServiceSlopValue maximum allowed slope
     * @param scrutinyDetail2 scrutiny detail to report results
     */

    /**
     * Validates ramp slopes against expected values and updates the scrutiny report.
     *
     * @param pl                  The plan
     * @param block               The block containing ramps
     * @param expectedSlopeOne    The numerator for expected slope calculation
     * @param divideExpectedSlope The denominator for expected slope calculation
     * @param rampServiceSlope    The minimum slope value to consider
     * @param scrutinyDetail2     The scrutiny detail object to update
     */
    /**
     * Validates ramp slopes against expected values and updates the scrutiny report.
     *
     * @param pl                  The plan
     * @param block               The block containing ramps
     * @param expectedSlopeOne    The numerator for expected slope calculation
     * @param divideExpectedSlope The denominator for expected slope calculation
     * @param rampServiceSlope    The minimum slope value to consider
     * @param scrutinyDetail2     The scrutiny detail object to update
     */
    private void validateRampSlopes(Plan pl, Block block, BigDecimal expectedSlopeOne,
            BigDecimal divideExpectedSlope, BigDecimal rampServiceSlope,
            ScrutinyDetail scrutinyDetail2) {

        boolean valid = false;
        Map<String, String> mapOfRampNumberAndSlopeValues = new HashMap<>();
        // Convert expected slope to fraction form (rise/run)
        BigDecimal expectedSlope = expectedSlopeOne.divide(divideExpectedSlope, 2, RoundingMode.HALF_UP); // run/rise
        BigDecimal expectedSlopeFraction = BigDecimal.ONE.divide(expectedSlope, 2, RoundingMode.HALF_UP); // fraction for comparison

        BigDecimal lastSlopeFraction = BigDecimal.ZERO;

        for (DARamp daRamp : block.getDARamps()) {
            BigDecimal slope = daRamp.getSlope(); // run/rise
            if (slope != null) {
                BigDecimal slopeFraction = BigDecimal.ONE.divide(slope, 2, RoundingMode.HALF_UP); // convert to fraction
                lastSlopeFraction = slopeFraction;

                if (slopeFraction.compareTo(expectedSlopeFraction) <= 0) {
                    valid = true;
                    mapOfRampNumberAndSlopeValues.put(DA_RAMP_NUMBER, daRamp.getNumber().toString());
                    mapOfRampNumberAndSlopeValues.put(SLOPE_STRING, slopeFraction.toString());
                    break;
                }
            }
        }

        String providedRampNumber = mapOfRampNumberAndSlopeValues.getOrDefault(DA_RAMP_NUMBER, EMPTY_STRING);
        String providedSlope = mapOfRampNumberAndSlopeValues.getOrDefault(SLOPE_STRING, lastSlopeFraction.toString());

        setReportOutputDetails(pl, SUBRULE_50_C_4_B,
                String.format(SUBRULE_50_C_4_B_SLOPE_DESCRIPTION, providedRampNumber),
                expectedSlope.toString(), // report expected in fraction
                providedSlope,
                valid ? Result.Accepted.getResultVal() : Result.Not_Accepted.getResultVal(),
                scrutinyDetail2);
    }


    
    private void validateMinHeightEntrance(Plan pl, Block block, BigDecimal requiredMinHeight, ScrutinyDetail scrutinyDetail) {
        for (Floor floor : block.getBuilding().getFloors()) {
            List<Ramp> ramps = floor.getRamps();
            if (ramps != null) {
                for (Ramp ramp : ramps) {
                    BigDecimal providedHeight = ramp.getMinEntranceHeight();
                    String status = (providedHeight != null && providedHeight.compareTo(requiredMinHeight) >= 0)
                            ? Result.Accepted.getResultVal()
                            : Result.Not_Accepted.getResultVal();

                    String provided = (providedHeight != null) ? providedHeight.toString() : "Not Defined";
                    
                    setReportOutputDetails(
                            pl, "RULE_30", "Minimum Entrance Height for Ramp " + ramp.getNumber(),
                            requiredMinHeight.toString(), provided, status, scrutinyDetail);
                }
            }
        }
    }
    
  
    /**
     * Validates the dimensions of DA (Disabled Access) Ramps for a given block.
     * It checks both ramp length and width against the permissible limits defined in MDMS rules.
     *
     * @param pl              The plan object containing building details.
     * @param block           The block containing DA ramps.
     * @param scrutinyDetail  The scrutiny detail object for recording validation results.
     */
    private void validateDARampDimensions(Plan pl, Block block, ScrutinyDetail scrutinyDetail) {
        LOGGER.info("Validating DA Ramp dimensions for Block: {}", block.getNumber());

        for (DARamp daRamp : block.getDARamps()) {
            LOGGER.info("Processing DA Ramp: {}", daRamp);

            if (daRamp.getMeasurements() != null && !daRamp.getMeasurements().isEmpty()) {
                for (Measurement m : daRamp.getMeasurements()) {
                    BigDecimal length = m.getLength();
                    List<BigDecimal> widthList = daRamp.getDaRampWidth();

                   
                    BigDecimal minWidth = (widthList != null && !widthList.isEmpty())
                            ? widthList.stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO)
                            : null;

                    String providedLength = (length != null)
                            ? String.format("%.2f", length)
                            : NOT_DEFINED;
                    String providedWidth = (minWidth != null)
                            ? String.format("%.2f", minWidth)
                            : NOT_DEFINED;

                    LOGGER.info("Ramp Measurement → Length: {}, Min Width: {}", providedLength, providedWidth);

                    
                    BigDecimal rampServiceMaxLength = BigDecimal.valueOf(9);
                    BigDecimal rampServiceMinWidth = BigDecimal.valueOf(1.5);

                  
                    List<Object> rules = cache.getFeatureRules(pl, FeatureEnum.RAMP_SERVICE.getValue(), false);
                    Optional<RampServiceRequirement> matchedRule = rules.stream()
                            .filter(RampServiceRequirement.class::isInstance)
                            .map(RampServiceRequirement.class::cast)
                            .findFirst();

                    if (matchedRule.isPresent()) {
                        RampServiceRequirement rule = matchedRule.get();
                        if (rule.getRampServiceMaxLength() != null || rule.getRampServiceMinWidth() != null) {
                            rampServiceMaxLength = rule.getRampServiceMaxLength();
                            rampServiceMinWidth = rule.getRampServiceMinWidth();
                            LOGGER.info("Matched rule → MaxLength: {}, MinWidth: {}",
                                    rampServiceMaxLength, rampServiceMinWidth);
                        }
                    }

                    //  Length validation
                    if (length == null || length.compareTo(rampServiceMaxLength) > 0) {
                        LOGGER.info("Ramp length validation failed. Required ≤ {}, Provided: {}",
                                rampServiceMaxLength, providedLength);
                        setReportOutputDetails(pl,
                                RULE_RAMP_LENGTH,
                                DESC_RAMP_LENGTH,
                                rampServiceMaxLength.toString(),
                                providedLength,
                                Result.Not_Accepted.getResultVal(),
                                scrutinyDetail);
                    } else {
                        LOGGER.info("Ramp length validation passed. Required ≤ {}, Provided: {}",
                                rampServiceMaxLength, providedLength);
                        setReportOutputDetails(pl,
                                RULE_RAMP_LENGTH,
                                DESC_RAMP_LENGTH,
                                rampServiceMaxLength.toString(),
                                providedLength,
                                Result.Accepted.getResultVal(),
                                scrutinyDetail);
                    }

                    //  Width validation
                    if (minWidth == null || minWidth.compareTo(rampServiceMinWidth) < 0) {
                        LOGGER.info("Ramp width validation failed. Required ≥ {}, Provided: {}",
                                rampServiceMinWidth, providedWidth);
                        setReportOutputDetails(pl,
                                RULE_RAMP_WIDTH,
                                DESC_RAMP_WIDTH,
                                rampServiceMinWidth.toString(),
                                providedWidth,
                                Result.Not_Accepted.getResultVal(),
                                scrutinyDetail);
                    } else {
                        LOGGER.info("Ramp width validation passed. Required ≥ {}, Provided: {}",
                                rampServiceMinWidth, providedWidth);
                        setReportOutputDetails(pl,
                                RULE_RAMP_WIDTH,
                                DESC_RAMP_WIDTH,
                                rampServiceMinWidth.toString(),
                                providedWidth,
                                Result.Accepted.getResultVal(),
                                scrutinyDetail);
                    }
                }
            } else {
            	LOGGER.warn("No measurements found for DA Ramp in Block: {}", block.getNumber());
            }
        }
    }



    private void processDARoomValidation(Plan pl, Block block, BigDecimal rampServiceBuildingHeight, ScrutinyDetail scrutinyDetail3) {
        
    }

    
    private void setReportOutputDetails(Plan pl, String ruleNo, String ruleDesc, String expected, String actual, String status,
            ScrutinyDetail scrutinyDetail) {
        ReportScrutinyDetail detail = new ReportScrutinyDetail();
        detail.setRuleNo(ruleNo);
        detail.setDescription(ruleDesc);
        detail.setRequired(expected);
        detail.setProvided(actual);
        detail.setStatus(status);

        Map<String, String> details = mapReportDetails(detail);
        addScrutinyDetailtoPlan(scrutinyDetail, pl, details);
    }

    private void setReportOutputDetailsFloorWise(Plan pl, String ruleNo, String floor, String expected, String actual,
            String status, ScrutinyDetail scrutinyDetail) {
        ReportScrutinyDetail detail = new ReportScrutinyDetail();
        detail.setRuleNo(ruleNo);
        detail.setFloorNo(floor);
        detail.setRequired(expected);
        detail.setProvided(actual);
        detail.setStatus(status);

        Map<String, String> details = mapReportDetails(detail);
        addScrutinyDetailtoPlan(scrutinyDetail, pl, details);
    }

    private void setReportOutputDetailsFloorWiseWithDescription(Plan pl, String ruleNo, String ruleDesc, String floor,
            String expected, String actual, String status, ScrutinyDetail scrutinyDetail) {
        ReportScrutinyDetail detail = new ReportScrutinyDetail();
        detail.setRuleNo(ruleNo);
        detail.setDescription(ruleDesc);
        detail.setFloorNo(floor);
        detail.setRequired(expected);
        detail.setProvided(actual);
        detail.setStatus(status);

        Map<String, String> details = mapReportDetails(detail);
        addScrutinyDetailtoPlan(scrutinyDetail, pl, details);
    }

  
    private void validateDimensions(Plan plan, String blockNo, int floorNo, String rampNo,
            List<Measurement> rampPolylines) {
        int count = 0;
        for (Measurement m : rampPolylines) {
            if (m.getInvalidReason() != null && m.getInvalidReason().length() > 0) {
                count++;
            }
        }
        if (count > 0) {
            plan.addError(String.format(DxfFileConstants.LAYER_RAMP_WITH_NO, blockNo, floorNo, rampNo),
                    count + RAMP_POLYLINE_ERROR
                            + String.format(DxfFileConstants.LAYER_RAMP_WITH_NO, blockNo, floorNo, rampNo));

        }
    }

    @Override
    public Map<String, Date> getAmendments() {
        return new LinkedHashMap<>();
    }
}
