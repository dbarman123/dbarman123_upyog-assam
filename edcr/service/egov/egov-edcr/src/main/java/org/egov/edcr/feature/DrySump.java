package org.egov.edcr.feature;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.egov.common.entity.edcr.Plan;
import org.egov.common.entity.edcr.Result;
import org.egov.common.entity.edcr.ScrutinyDetail;
import org.springframework.stereotype.Service;

@Service
public class DrySump extends FeatureProcess {
    private static final Logger LOG = LogManager.getLogger(DrySump.class);

    private static final String RULE_DS = "10.4"; 
    private static final String RULE_DS_DESCRIPTION = "Sump ";
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

  

    @Override
    public Plan validate(Plan pl) {
        return pl;
    }

    /**
     * Adds scrutiny report output details for Dry Sump in the given plan.
     *
     * <p>
     * This method checks if the provided {@link Plan} contains a utility
     * section with one or more Dry Sump objects. If Dry Sumps are present,
     * it logs their capacity and appends this information to the scrutiny
     * report output. The report entry includes the sub-rule, its description,
     * and the Dry Sump capacity for verification.
     * </p>
     *
     * <p>
     * If no Dry Sump objects are found in the plan, no report entry is added.
     * </p>
     *
     * @param pl          the {@link Plan} object containing utility details
     * @param subRule     the sub-rule number associated with the Dry Sump requirement
     * @param subRuleDesc the description of the sub-rule for reporting
     */
    
    @Override
    public Plan process(Plan pl) {
        LOG.info("Starting Dry Sump feature process...");
        HashMap<String, String> errors = new HashMap<>();

        scrutinyDetail = new ScrutinyDetail();
        scrutinyDetail.addColumnHeading(1, RULE_NO);
        scrutinyDetail.addColumnHeading(2, DESCRIPTION);
        scrutinyDetail.addColumnHeading(4, PROVIDED);
        scrutinyDetail.addColumnHeading(5, STATUS);
        scrutinyDetail.setKey("Common_Sump");

        String subRule = RULE_DS;
        String subRuleDesc = RULE_DS_DESCRIPTION;

        addReportOutput(pl, subRule, subRuleDesc);

        LOG.info("Completed Dry Sump feature process.");
        return pl;
    }

    /**
     * Adds scrutiny report output details for Dry Sump in the given plan.
     * <p>
     * This method checks if the plan contains a utility with one or more
     * Dry Sump objects. If present, it logs the capacity of the Dry Sump
     * and records it into the scrutiny report output. The report output
     * includes the provided Dry Sump capacity for verification purposes.
     * </p>
     *
     * @param pl          The {@link Plan} object containing utility details.
     * @param subRule     The sub-rule number associated with the Dry Sump requirement.
     * @param subRuleDesc The description of the sub-rule for reporting.
     */
    
    private void addReportOutput(Plan pl, String subRule, String subRuleDesc) {
        LOG.info("Preparing scrutiny report output for Dry Sump...");

        if (pl.getUtility() != null 
                && pl.getUtility().getDrySumps() != null 
                && !pl.getUtility().getDrySumps().isEmpty()) {

            int count = 1;
            for (org.egov.common.entity.edcr.DrySump ds : pl.getUtility().getDrySumps()) {
                BigDecimal area = ds.getArea();
                LOG.info("Dry Sump {} area = {}", count, area);

                String providedDetails = area != null
                        ? "Sump " + count + " - Area: " + area + " sq.m"
                        : "Sump " + count + " - Area not provided";

                setReportOutputDetails(
                    pl,
                    subRule,
                    subRuleDesc,
                    null,
                    providedDetails,
                    Result.Verify.getResultVal()
                );

                count++;
            }
        } else {
            LOG.info("No Dry Sump objects found in the utility section.");
        }
    }


    private void setReportOutputDetails(Plan pl, String ruleNo, String ruleDesc, String expected, String actual,
            String status) {
        LOG.info("Adding scrutiny detail: rule={}, desc={}, provided={}, status={}",
                 ruleNo, ruleDesc, actual, status);

        Map<String, String> details = new HashMap<>();
        details.put(RULE_NO, ruleNo);
        details.put(DESCRIPTION, ruleDesc);
        details.put(PROVIDED, actual);
        details.put(STATUS, status);
        scrutinyDetail.getDetail().add(details);
        pl.getReportOutput().getScrutinyDetails().add(scrutinyDetail);
    }

    @Override
    public Map<String, Date> getAmendments() {
        return new LinkedHashMap<>();
    }
}
