package org.egov.edcr.feature;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.egov.common.entity.edcr.*;
import org.egov.edcr.service.MDMSCacheManager;
import org.egov.edcr.utility.DcrConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.egov.edcr.constants.CommonFeatureConstants.*;
import static org.egov.edcr.constants.CommonKeyConstants.BLOCK;
import static org.egov.edcr.constants.EdcrReportConstants.RULE_37_6;
import static org.egov.edcr.service.FeatureUtil.addScrutinyDetailtoPlan;
import static org.egov.edcr.service.FeatureUtil.mapReportDetails;

@Service
public class PlantationGreenStrip_Assam extends FeatureProcess {
    private static final Logger LOG = LogManager.getLogger(PlantationGreenStrip_Assam.class);

    @Autowired
    MDMSCacheManager cache;

    @Override
    public Plan validate(Plan pl) {
        return null;
    }

    @Override
    public Plan process(Plan pl) {
        Optional<PlantationGreenStripRequirement> ruleOpt = getPlantationGreenStripRule(pl);

        // Get min/max % from MDMS or default to 10–15%
        BigDecimal minPercentage = ruleOpt.map(PlantationGreenStripRequirement::getMinPercentage)
                                          .orElse(new BigDecimal("10"));
        BigDecimal maxPercentage = ruleOpt.map(PlantationGreenStripRequirement::getMaxPercentage)
                                          .orElse(new BigDecimal("15"));

        BigDecimal plotArea = pl.getPlot().getArea();

        // Convert percentage to decimal for calculation
        BigDecimal minRequiredArea = plotArea.multiply(minPercentage).divide(BigDecimal.valueOf(100));
        BigDecimal maxRequiredArea = plotArea.multiply(maxPercentage).divide(BigDecimal.valueOf(100));

        for (Block block : pl.getBlocks()) {
            processBlock(pl, block, minRequiredArea, maxRequiredArea);
        }

        return pl;
    }

    private Optional<PlantationGreenStripRequirement> getPlantationGreenStripRule(Plan pl) {
        List<Object> rules = cache.getFeatureRules(pl, FeatureEnum.PLANTATION_GREEN_STRIP.getValue(), false);
        return rules.stream()
                .filter(PlantationGreenStripRequirement.class::isInstance)
                .map(PlantationGreenStripRequirement.class::cast)
                .findFirst();
    }

    private void processBlock(Plan pl, Block block, BigDecimal minRequiredArea, BigDecimal maxRequiredArea) {
        ScrutinyDetail scrutinyDetail = createScrutinyDetailForBlock(block);

        List<BigDecimal> areas = block.getPlantationGreenStripes().stream()
                .map(greenStrip -> greenStrip.getArea())
                .collect(Collectors.toList());

        if (!areas.isEmpty()) {
            BigDecimal totalGreenArea = areas.stream().reduce(BigDecimal.ZERO, BigDecimal::add);

            boolean valid = totalGreenArea.compareTo(minRequiredArea) >= 0
                    && totalGreenArea.compareTo(maxRequiredArea) <= 0;

            buildResult(pl, scrutinyDetail, valid,
                    "Area of Plantation Green Strip (10–15% of Plot Area)",
                    minRequiredArea.setScale(DcrConstants.DECIMALDIGITS_MEASUREMENTS, DcrConstants.ROUNDMODE_MEASUREMENTS)
                            + " - " +
                            maxRequiredArea.setScale(DcrConstants.DECIMALDIGITS_MEASUREMENTS, DcrConstants.ROUNDMODE_MEASUREMENTS),
                    totalGreenArea.setScale(DcrConstants.DECIMALDIGITS_MEASUREMENTS, DcrConstants.ROUNDMODE_MEASUREMENTS)
                            .toString());
        }
    }

    private ScrutinyDetail createScrutinyDetailForBlock(Block block) {
        ScrutinyDetail scrutinyDetail = new ScrutinyDetail();
        scrutinyDetail.addColumnHeading(1, RULE_NO);
        scrutinyDetail.addColumnHeading(2, DESCRIPTION);
        scrutinyDetail.addColumnHeading(3, PERMISSIBLE);
        scrutinyDetail.addColumnHeading(4, PROVIDED);
        scrutinyDetail.addColumnHeading(5, STATUS);
        scrutinyDetail.setKey(BLOCK + block.getNumber() + CONTINUOUS_GREEN_PLANTING_STRIP);
        return scrutinyDetail;
    }

    private void buildResult(Plan pl, ScrutinyDetail scrutinyDetail, boolean valid, String description, String permited,
                             String provided) {
        ReportScrutinyDetail detail = new ReportScrutinyDetail();
        detail.setRuleNo(RULE_37_6);
        detail.setDescription(description);
        detail.setPermissible(permited);
        detail.setProvided(provided);
        detail.setStatus(valid ? Result.Accepted.getResultVal() : Result.Not_Accepted.getResultVal());

        Map<String, String> details = mapReportDetails(detail);
        addScrutinyDetailtoPlan(scrutinyDetail, pl, details);
    }

    @Override
    public Map<String, Date> getAmendments() {
        return new LinkedHashMap<>();
    }
}
