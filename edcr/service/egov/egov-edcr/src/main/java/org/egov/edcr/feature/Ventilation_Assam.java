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

import static org.egov.edcr.constants.CommonFeatureConstants.AT_FLOOR;
import static org.egov.edcr.constants.CommonFeatureConstants.BATH_VENTILATION;
import static org.egov.edcr.constants.CommonFeatureConstants.COMMON_VENTILATION;
import static org.egov.edcr.constants.CommonFeatureConstants.MINIMUM_PREFIX_1;
import static org.egov.edcr.constants.CommonFeatureConstants.OF_CARPET_AREA;
import static org.egov.edcr.constants.CommonFeatureConstants.TH_OF_FLOOR_AREA;
import static org.egov.edcr.constants.CommonFeatureConstants.VENTILATION_AREA;
import static org.egov.edcr.constants.EdcrReportConstants.AREA_UNIT_SQM;
import static org.egov.edcr.constants.EdcrReportConstants.COMMON_ROOM_VENTILATION_OPENING_DESC_PREFIX;
import static org.egov.edcr.constants.EdcrReportConstants.LAUNDRY_VENTILATION_OPENING_DESC_PREFIX;
import static org.egov.edcr.constants.EdcrReportConstants.LIGHT_VENTILATION_DESCRIPTION;
import static org.egov.edcr.constants.EdcrReportConstants.PARENTHESIS_END;
import static org.egov.edcr.constants.EdcrReportConstants.PARENTHESIS_START;
import static org.egov.edcr.constants.EdcrReportConstants.PERCENTAGE_SUFFIX;
import static org.egov.edcr.constants.EdcrReportConstants.RULE_43;
import static org.egov.edcr.constants.EdcrReportConstants.RULE_VENT_01;
import static org.egov.edcr.service.FeatureUtil.addScrutinyDetailtoPlan;
import static org.egov.edcr.service.FeatureUtil.mapReportDetails;

import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.egov.common.entity.edcr.*;
import org.egov.edcr.service.MDMSCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Ventilation_Assam extends Ventilation {

	private static final Logger LOG = LogManager.getLogger(Ventilation_Assam.class);

	 @Autowired
	 MDMSCacheManager cache;

	/**
	 * Validates the building plan for ventilation requirements.
	 * Currently performs no validation and returns the plan as-is.
	 *
	 * @param pl The building plan to validate
	 * @return The unmodified plan
	 */
	@Override
	public Plan validate(Plan pl) {
		return pl;
	}

	/**
	 * Processes ventilation requirements for all blocks and floors in the building plan.
	 * Extracts ventilation rules from MDMS, creates scrutiny details, and validates
	 * general ventilation requirements for each floor.
	 *
	 * @param pl The building plan to process
	 * @return The processed plan with scrutiny details added
	 */
	@Override
	public Plan process(Plan pl) {
	    for (Block b : pl.getBlocks()) {
	        BigDecimal[] ventilationValues = extractVentilationRules(pl);
	        ScrutinyDetail generalScrutiny = createScrutinyDetail(COMMON_VENTILATION);
	        ScrutinyDetail bathScrutiny = createScrutinyDetail(BATH_VENTILATION);

	        if (b.getBuilding() != null && b.getBuilding().getFloors() != null) {
	            for (Floor f : b.getBuilding().getFloors()) {
                    if(f.getUnits() != null && !f.getUnits().isEmpty())
                        for(FloorUnit floorUnit : f.getUnits()) {
                            processGeneralVentilation(f, floorUnit, ventilationValues[0], generalScrutiny, pl);
                            processLaundryRecreationVentilation(f, floorUnit, bathScrutiny, pl);
                            processCommonRoomVentilation(f, floorUnit, bathScrutiny, pl);
                        }
	            }
	        }
	        
	        pl.getReportOutput().getScrutinyDetails().add(generalScrutiny);
	        pl.getReportOutput().getScrutinyDetails().add(bathScrutiny);
	    }
	    return pl;
	}

	/**
	 * Extracts ventilation requirement values from MDMS cache.
	 * Fetches ventilation ratio values used for calculating minimum required ventilation areas.
	 *
	 * @param pl The building plan containing configuration details
	 * @return Array containing ventilationValueOne and ventilationValueTwo
	 */
	private BigDecimal[] extractVentilationRules(Plan pl) {
	    BigDecimal ventilationValueOne = BigDecimal.ZERO;
	    BigDecimal ventilationValueTwo = BigDecimal.ZERO;
        List<Object> rules = cache.getFeatureRules(pl, FeatureEnum.VENTILATION.getValue(), false);
        Optional<VentilationRequirement> matchedRule = rules.stream()
            .filter(VentilationRequirement.class::isInstance)
            .map(VentilationRequirement.class::cast)
            .findFirst();

	    if (matchedRule.isPresent()) {
	    	VentilationRequirement rule = matchedRule.get();
	        ventilationValueOne = rule.getVentilationValueOne();
	        ventilationValueTwo = rule.getVentilationValueTwo();
	    }

	    return new BigDecimal[]{ventilationValueOne, ventilationValueTwo};
	}

	/**
	 * Creates and initializes a scrutiny detail object for ventilation reporting.
	 * Sets up column headings and key for the specified ventilation type.
	 *
	 * @param key The key identifier for the scrutiny detail (e.g., COMMON_VENTILATION)
	 * @return Configured ScrutinyDetail object with appropriate headings and key
	 */
	private ScrutinyDetail createScrutinyDetail(String key) {
	    ScrutinyDetail detail = new ScrutinyDetail();
	    detail.setKey(key);
	    detail.addColumnHeading(1, RULE_NO);
	    detail.addColumnHeading(2, DESCRIPTION);
	    detail.addColumnHeading(3, REQUIRED);
	    detail.addColumnHeading(4, PROVIDED);
	    detail.addColumnHeading(5, STATUS);
	    return detail;
	}

	/**
	 * Processes general light and ventilation requirements for a specific floor.
	 * Calculates total ventilation area and carpet area, compares against required ratios,
	 * and generates scrutiny details for compliance verification.
	 *
	 * @param floor The floor containing ventilation measurements
	 * @param ventilationRatio The required ventilation ratio (e.g., 1/10th of floor area)
	 * @param scrutinyDetail The scrutiny detail object to add results to
	 * @param pl The building plan for adding scrutiny details to report
	 */
	private void processGeneralVentilation(Floor floor, FloorUnit floorUnit, BigDecimal ventilationRatio,
	                                       ScrutinyDetail scrutinyDetail, Plan pl) {
	    if (floorUnit.getLightAndVentilation() != null &&
	        floorUnit.getLightAndVentilation().getMeasurements() != null &&
	        !floorUnit.getLightAndVentilation().getMeasurements().isEmpty()) {

	        BigDecimal totalVentilationArea = floorUnit.getLightAndVentilation().getMeasurements().stream()
	            .map(Measurement::getArea).reduce(BigDecimal.ZERO, BigDecimal::add);

	        BigDecimal totalCarpetArea = floorUnit.getOccupancies().stream()
	            .map(Occupancy::getCarpetArea).reduce(BigDecimal.ZERO, BigDecimal::add);

	        if (totalVentilationArea.compareTo(BigDecimal.ZERO) > 0) {
	            BigDecimal requiredVentilation = totalCarpetArea.divide(ventilationRatio, 2, BigDecimal.ROUND_HALF_UP);
				ReportScrutinyDetail detail = new ReportScrutinyDetail();
				detail.setRuleNo(RULE_43);
				detail.setDescription(LIGHT_VENTILATION_DESCRIPTION);
				detail.setRequired(MINIMUM_PREFIX_1 + ventilationRatio + TH_OF_FLOOR_AREA);
				detail.setProvided(VENTILATION_AREA + totalVentilationArea + OF_CARPET_AREA + totalCarpetArea + AT_FLOOR + floor.getNumber());
				detail.setStatus(totalVentilationArea.compareTo(requiredVentilation) >= 0 ? Result.Accepted.getResultVal() : Result.Not_Accepted.getResultVal());

				 Map<String, String> details = mapReportDetails(detail);
			     addScrutinyDetailtoPlan(scrutinyDetail, pl, details);
	           
	        }
	    }
	}


	private void processLaundryRecreationVentilation(Floor floor, FloorUnit floorUnit, ScrutinyDetail scrutinyDetail, Plan pl) {
	    MeasurementWithHeight laundryVent = floorUnit.getLaundryOrRecreationalVentilation();

	    // If not provided in DXF, skip adding to report
	    if (laundryVent == null || laundryVent.getMeasurements() == null || laundryVent.getMeasurements().isEmpty()) {
	        return;
	    }

	    BigDecimal ventilationPercent = BigDecimal.TEN;
	    List<Object> rules = cache.getFeatureRules(pl, FeatureEnum.VENTILATION.getValue(), false);

	    Optional<VentilationRequirement> matchedRule = rules.stream()
	        .filter(VentilationRequirement.class::isInstance)
	        .map(VentilationRequirement.class::cast)
	        .findFirst();

	    if (matchedRule.isPresent()) {
	        ventilationPercent = matchedRule.get().getLaundryRecreationPercent();
	    }

	    BigDecimal roomArea = floorUnit.getArea();
	    BigDecimal requiredVentArea = roomArea.multiply(ventilationPercent);
	    BigDecimal providedVentArea = laundryVent.getMeasurements().stream()
	        .map(Measurement::getArea)
	        .reduce(BigDecimal.ZERO, BigDecimal::add);

	    ReportScrutinyDetail detail = new ReportScrutinyDetail();
	    detail.setRuleNo(RULE_VENT_01);
	    detail.setDescription(LAUNDRY_VENTILATION_OPENING_DESC_PREFIX + ventilationPercent + PERCENTAGE_SUFFIX + "Floor Area");
	    detail.setRequired(requiredVentArea + AREA_UNIT_SQM + PARENTHESIS_START + ventilationPercent + PERCENTAGE_SUFFIX + roomArea + PARENTHESIS_END);
	    detail.setProvided(providedVentArea + AREA_UNIT_SQM + AT_FLOOR + floor.getNumber());
	    detail.setStatus(providedVentArea.compareTo(requiredVentArea) >= 0
	        ? Result.Accepted.getResultVal()
	        : Result.Not_Accepted.getResultVal());

	    scrutinyDetail.getDetail().add(mapReportDetails(detail));
	    pl.getReportOutput().getScrutinyDetails().add(scrutinyDetail);
	}

	
	private void processCommonRoomVentilation(Floor floor, FloorUnit floorUnit, ScrutinyDetail scrutinyDetail, Plan pl) {
	    if (floorUnit.getCommonRoom() == null || floorUnit.getCommonRoom().getCommonRoomVentialtion() == null
	            || floorUnit.getCommonRoom().getCommonRoomVentialtion().isEmpty()) {
	        // Skip if not provided in DXF
	        return;
	    }

	    List<Measurement> roomVent = floorUnit.getCommonRoom().getCommonRoomVentialtion();
	    BigDecimal ventilationPercent = BigDecimal.ZERO;

	    List<Object> rules = cache.getFeatureRules(pl, FeatureEnum.VENTILATION.getValue(), false);
	    Optional<VentilationRequirement> matchedRule = rules.stream()
	            .filter(VentilationRequirement.class::isInstance)
	            .map(VentilationRequirement.class::cast)
	            .findFirst();

	    if (matchedRule.isPresent()) {
	        ventilationPercent = matchedRule.get().getCommonRoomPercent();
	    }

	    List<Measurement> commonRoomMeasurements = floorUnit.getCommonRoom().getRooms();
	    BigDecimal roomArea = commonRoomMeasurements.stream()
	            .map(Measurement::getArea)
	            .reduce(BigDecimal.ZERO, BigDecimal::add);

	    BigDecimal requiredVentArea = roomArea.multiply(ventilationPercent);
	    BigDecimal providedVentArea = roomVent.stream()
	            .map(Measurement::getArea)
	            .reduce(BigDecimal.ZERO, BigDecimal::add);

	    ReportScrutinyDetail detail = new ReportScrutinyDetail();
	    detail.setRuleNo(RULE_VENT_01);
	    detail.setDescription(COMMON_ROOM_VENTILATION_OPENING_DESC_PREFIX + ventilationPercent + PERCENTAGE_SUFFIX + "Floor Area");
	    detail.setRequired(requiredVentArea + AREA_UNIT_SQM + PARENTHESIS_START + ventilationPercent + PERCENTAGE_SUFFIX + roomArea + PARENTHESIS_END);
	    detail.setProvided(providedVentArea + AREA_UNIT_SQM + AT_FLOOR + floor.getNumber());
	    detail.setStatus(providedVentArea.compareTo(requiredVentArea) >= 0
	        ? Result.Accepted.getResultVal()
	        : Result.Not_Accepted.getResultVal());

	    scrutinyDetail.getDetail().add(mapReportDetails(detail));
	}

	@Override
	public Map<String, Date> getAmendments() {
		return new LinkedHashMap<>();
	}

}
