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
 *         derived works should carry eGovernments Foundation LOGo on the top right corner.
 *
 *      For the LOGo, please refer http://egovernments.org/html/LOGo/egov_LOGo.png.
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

import static org.egov.edcr.constants.CommonFeatureConstants.LOCATION_PLANS_PROVIDED;
import static org.egov.edcr.constants.CommonFeatureConstants.LOCATION_PLAN_LAYER_NOT_PROVIDED;
import static org.egov.edcr.constants.CommonFeatureConstants.POLYLINE_NOT_DEFINED;
import static org.egov.edcr.constants.CommonKeyConstants.LOCATION_PLAN;
import static org.egov.edcr.constants.EdcrReportConstants.DESC_BHARALU_MORA_BONDAJAN;
import static org.egov.edcr.constants.EdcrReportConstants.DESC_NOTIFIED_WATER_BODIES;
import static org.egov.edcr.constants.EdcrReportConstants.DESC_OTHER_CHANNELS;
import static org.egov.edcr.constants.EdcrReportConstants.DESC_OTHER_LARGE_PONDS_WATER_BODIES;
import static org.egov.edcr.constants.EdcrReportConstants.DESC_OTHER_NOTIFIED_WATER_BODIES;
import static org.egov.edcr.constants.EdcrReportConstants.DESC_RIVER;
import static org.egov.edcr.constants.EdcrReportConstants.FIELD_BHARALU_MORA_BONDAJAN;
import static org.egov.edcr.constants.EdcrReportConstants.FIELD_NOTIFIED_WATER_BODIES;
import static org.egov.edcr.constants.EdcrReportConstants.FIELD_OTHER_CHANNELS;
import static org.egov.edcr.constants.EdcrReportConstants.FIELD_OTHER_LARGE_PONDS_WATER_BODIES;
import static org.egov.edcr.constants.EdcrReportConstants.FIELD_OTHER_NOTIFIED_WATER_BODIES;
import static org.egov.edcr.constants.EdcrReportConstants.FIELD_RIVER;
import static org.egov.edcr.constants.EdcrReportConstants.LOCATION_PLAN_DESCRIPTION;
import static org.egov.edcr.constants.EdcrReportConstants.RULE_4_4_4_I;
import static org.egov.edcr.constants.EdcrReportConstants.RULE_NOTIFIED_WATER_BODIES;
import static org.egov.edcr.service.FeatureUtil.addScrutinyDetailtoPlan;
import static org.egov.edcr.service.FeatureUtil.mapReportDetails;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.egov.common.entity.edcr.Block;
import org.egov.common.entity.edcr.DistanceFromWaterBodiesRequirement;
import org.egov.common.entity.edcr.FeatureEnum;
import org.egov.common.entity.edcr.OccupancyTypeHelper;
import org.egov.common.entity.edcr.Plan;
import org.egov.common.entity.edcr.ReportScrutinyDetail;
import org.egov.common.entity.edcr.Result;
import org.egov.common.entity.edcr.ScrutinyDetail;
import org.egov.edcr.service.MDMSCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LocationPlan extends FeatureProcess {

	private static final Logger LOG = LogManager.getLogger(LocationPlan.class);
	
	@Autowired
	MDMSCacheManager cache;

	@Override
	public Plan validate(Plan pl) {

		return pl;
	}

	@Override
	public Plan process(Plan pl) {

		ScrutinyDetail scrutinyDetail = new ScrutinyDetail();
		scrutinyDetail.setKey(Common_Location_Plan);
		scrutinyDetail.addColumnHeading(1, RULE_NO);
		scrutinyDetail.addColumnHeading(2, DESCRIPTION);
		scrutinyDetail.addColumnHeading(3, PROVIDED);
		scrutinyDetail.addColumnHeading(4, STATUS);

		HashMap<String, String> errors = new HashMap<>();
		ReportScrutinyDetail detail = new ReportScrutinyDetail();
		detail.setRuleNo(RULE_4_4_4_I);
		detail.setDescription(LOCATION_PLAN_DESCRIPTION);

		if (pl.getDrawingPreference().getLocationPlans() == null) {
			errors.put(LOCATION_PLAN, LOCATION_PLAN_LAYER_NOT_PROVIDED);
			pl.addErrors(errors);
		} else if (!pl.getDrawingPreference().getLocationPlans().isEmpty()) {
			detail.setProvided(LOCATION_PLANS_PROVIDED);
			detail.setStatus(Result.Accepted.getResultVal());

			Map<String, String> details = mapReportDetails(detail);
			addScrutinyDetailtoPlan(scrutinyDetail, pl, details);
		} else {
			detail.setProvided(POLYLINE_NOT_DEFINED);
			detail.setStatus(Result.Not_Accepted.getResultVal());

			Map<String, String> details = mapReportDetails(detail);
			addScrutinyDetailtoPlan(scrutinyDetail, pl, details);
		}

		processDistanceFromWaterBodiesAndReport(pl);
		return pl;
	}
	
	 /**
     * Processes the distance from waterbodies in the Plan object,
     * validates them against MDMS rules, and generates a detailed scrutiny report.
     * Entries without provided distances in Plan are excluded from the report.
     *
     * @param pl The Plan object containing distances from various waterbodies
     * @param block The current Block context (optional depending on implementation)
     * @param mostRestrictiveOccupancyType The occupancy type helper used for restrictions
     * @return The Plan object updated with scrutiny details report
     */
	
	public Plan processDistanceFromWaterBodiesAndReport(Plan pl) {
        LOG.info("Starting processDistanceFromWaterBodiesAndReport for Plan");

        ScrutinyDetail scrutinyDetail = new ScrutinyDetail();
        scrutinyDetail.setKey("Common_Distance Water Bodies");
        scrutinyDetail.addColumnHeading(1, RULE_NO);
        scrutinyDetail.addColumnHeading(2, DESCRIPTION);
        scrutinyDetail.addColumnHeading(3, REQUIRED);
        scrutinyDetail.addColumnHeading(4, PROVIDED);      
        scrutinyDetail.addColumnHeading(5, STATUS);

        // Fetch rules applicable for Distance from Water Bodies feature
        List<Object> rules = cache.getFeatureRules(pl, FeatureEnum.DISTANCE_FROM_WATERBODIES.getValue(), false);
        Optional<DistanceFromWaterBodiesRequirement> matchedRule = rules.stream()
            .filter(DistanceFromWaterBodiesRequirement.class::isInstance)
            .map(DistanceFromWaterBodiesRequirement.class::cast)
            .findFirst();

        if (!matchedRule.isPresent()) {
            LOG.warn("No DistanceFromWaterBodiesRequirement rule found for Plan");
            // No rules means unable to validate; could add error or skip
        } else {
            DistanceFromWaterBodiesRequirement rule = matchedRule.get();

            // Only report waterbodies with provided distance values
            if (pl.getRiver() != null) {
                LOG.debug("Reporting river distance: {}", pl.getRiver());
                reportWaterbody(pl, scrutinyDetail, FIELD_RIVER, DESC_RIVER, pl.getRiver(), rule.getRiver(), RULE_NOTIFIED_WATER_BODIES);
            }
            if (pl.getBharaluMoraBondajan() != null) {
                LOG.debug("Reporting Bharalu/Mora/Bondajan distance: {}", pl.getBharaluMoraBondajan());
                reportWaterbody(pl, scrutinyDetail, FIELD_BHARALU_MORA_BONDAJAN, DESC_BHARALU_MORA_BONDAJAN, pl.getBharaluMoraBondajan(), rule.getBharaluMoraBondajan(), RULE_NOTIFIED_WATER_BODIES);
            }
            if (pl.getOtherChannels() != null) {
                LOG.debug("Reporting Other Channels distance: {}", pl.getOtherChannels());
                reportWaterbody(pl, scrutinyDetail, FIELD_OTHER_CHANNELS, DESC_OTHER_CHANNELS, pl.getOtherChannels(), rule.getOtherChannels(), RULE_NOTIFIED_WATER_BODIES);
            }
            if (pl.getNotifiedWaterBodies() != null) {
                LOG.debug("Reporting Notified Water Bodies distance: {}", pl.getNotifiedWaterBodies());
                reportWaterbody(pl, scrutinyDetail, FIELD_NOTIFIED_WATER_BODIES, DESC_NOTIFIED_WATER_BODIES, pl.getNotifiedWaterBodies(), rule.getNotifiedWaterBodies(), RULE_NOTIFIED_WATER_BODIES);
            }
            if (pl.getOtherNotifiedWaterBodies() != null) {
                LOG.debug("Reporting Other Notified Water Bodies distance: {}", pl.getOtherNotifiedWaterBodies());
                reportWaterbody(pl, scrutinyDetail, FIELD_OTHER_NOTIFIED_WATER_BODIES, DESC_OTHER_NOTIFIED_WATER_BODIES, pl.getOtherNotifiedWaterBodies(), rule.getOtherNotifiedWaterBodies(), RULE_NOTIFIED_WATER_BODIES);
            }
            if (pl.getOtherLargePondsOrWaterBody() != null) {
                LOG.debug("Reporting Other Large Ponds/Water Bodies distance: {}", pl.getOtherLargePondsOrWaterBody());
                reportWaterbody(pl, scrutinyDetail, FIELD_OTHER_LARGE_PONDS_WATER_BODIES, DESC_OTHER_LARGE_PONDS_WATER_BODIES, pl.getOtherLargePondsOrWaterBody(), rule.getOtherLargePondsWaterBodies(), RULE_NOTIFIED_WATER_BODIES);
            }
        }

        pl.getReportOutput().getScrutinyDetails().add(scrutinyDetail);

        LOG.info("Completed processDistanceFromWaterBodiesAndReport for Plan");

        return pl;
    }
	
	 /**
     * Adds a single waterbody distance scrutiny record to the report,
     * if the provided value is not null.
     *
     * @param pl The Plan object being processed
     * @param scrutinyDetail The current ScrutinyDetail summary object
     * @param fieldName The field/internal name of the waterbody distance
     * @param desc Description of the waterbody distance rule
     * @param provided The actual distance value provided in the Plan
     * @param permissible The minimum permissible distance value from MDMS
     * @param ruleNumber The rule number reference for this distance validation
     */

	private void reportWaterbody(Plan pl, ScrutinyDetail scrutinyDetail,
            String fieldName, String desc,
            BigDecimal provided, BigDecimal permissible,
            String ruleNumber) {
			ReportScrutinyDetail detail = new ReportScrutinyDetail();
			detail.setRuleNo(ruleNumber);
			detail.setDescription(desc);
			
			detail.setProvided(provided.toPlainString());
			detail.setRequired(permissible.toPlainString());
			
			String status;
			if (provided != null && (permissible == null || provided.compareTo(permissible) >= 0)) {
			status = Result.Accepted.getResultVal();
			LOG.debug("Validation passed for {}: provided {}, permissible {}", fieldName, provided, permissible);
			} else {
			status = Result.Not_Accepted.getResultVal();
			LOG.warn("Validation failed for {}: provided {}, permissible {}", fieldName, provided, permissible);
			}
			detail.setStatus(status);
			
			Map<String, String> details = mapReportDetails(detail);
			addScrutinyDetailtoPlan(scrutinyDetail, pl, details);
			}
			
			

	@Override
	public Map<String, Date> getAmendments() {
		return new LinkedHashMap<>();
	}

}
