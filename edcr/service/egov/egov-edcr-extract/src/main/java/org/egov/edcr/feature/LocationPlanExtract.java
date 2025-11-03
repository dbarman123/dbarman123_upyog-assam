package org.egov.edcr.feature;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.entity.blackbox.MeasurementDetail;
import org.egov.edcr.entity.blackbox.PlanDetail;
import org.egov.edcr.service.LayerNames;
import org.egov.edcr.utility.Util;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFLWPolyline;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LocationPlanExtract extends FeatureExtract {

    private static final Logger LOGGER = LogManager.getLogger(LocationPlanExtract.class);
    @Autowired
    private LayerNames layerNames;
    
    @Autowired
	private Util util;
    private String digitsRegex = "[^\\d.]";

    @Override
    public PlanDetail extract(PlanDetail pl) {
    	

        DXFDocument doc = pl.getDoc();

        List<DXFLWPolyline> locationPlanPolyLines = Util.getPolyLinesByLayer(doc,
                layerNames.getLayerName("LAYER_NAME_LOCATION_PLAN"));

        if (!locationPlanPolyLines.isEmpty()) {
            pl.getDrawingPreference().setLocationPlans(new ArrayList<>());
            for (DXFLWPolyline locationPlanPolyLine : locationPlanPolyLines)
                pl.getDrawingPreference().getLocationPlans().add(new MeasurementDetail(locationPlanPolyLine, true));
        }
        extractPlanInfo(pl);
        return pl;
    }

    /**
     * Extracts location plan-related waterbody information from a given plan document
     * and updates the corresponding {@link PlanDetail} object.
     * <p>
     * This method fetches different waterbody properties from the plan,
     * removes digits (via {@code digitsRegex}), and converts the cleaned value
     * into a {@link BigDecimal} representation using {@link #getNumericValue(String, PlanDetail, String)}.
     * </p>
     *
     * @param pl the {@link PlanDetail} object containing plan metadata and to be updated
     */
    private void extractPlanInfo(PlanDetail pl) {
       
        Map<String, String> planProperties = util.getFormatedLocationPlanProperties(pl.getDoc());

        try {
            // Extract river details
            String river = planProperties.get(DxfFileConstants.RIVER);
            if (StringUtils.isNotBlank(river)) {
                LOGGER.debug("Processing river property: {}", river);
                river = river.replaceAll(digitsRegex, "");
                BigDecimal riverValue = getNumericValue(river, pl, DxfFileConstants.RIVER);
                pl.setRiver(riverValue);
                LOGGER.info("Set river value to {}", riverValue);
            }

            // Extract Bharalu Mora Bondajan
            String bharaluMoraBondajan = planProperties.get(DxfFileConstants.BHARALU_MORA_BONDAJAN);
            if (StringUtils.isNotBlank(bharaluMoraBondajan)) {
                LOGGER.debug("Processing Bharalu Mora Bondajan property: {}", bharaluMoraBondajan);
                bharaluMoraBondajan = bharaluMoraBondajan.replaceAll(digitsRegex, "");
                BigDecimal value = getNumericValue(bharaluMoraBondajan, pl, DxfFileConstants.BHARALU_MORA_BONDAJAN);
                pl.setBharaluMoraBondajan(value);
                LOGGER.info("Set Bharalu Mora Bondajan value to {}", value);
            }

            // Extract Other Channels
            String otherChannels = planProperties.get(DxfFileConstants.OTHER_CHANNELS);
            if (StringUtils.isNotBlank(otherChannels)) {
                LOGGER.debug("Processing Other Channels property: {}", otherChannels);
                otherChannels = otherChannels.replaceAll(digitsRegex, "");
                BigDecimal value = getNumericValue(otherChannels, pl, DxfFileConstants.OTHER_CHANNELS);
                pl.setOtherChannels(value);
                LOGGER.info("Set Other Channels value to {}", value);
            }

            // Extract Minor Drains
            String minorDrains = planProperties.get(DxfFileConstants.MINOR_DRAINS);
            if (StringUtils.isNotBlank(minorDrains)) {
                LOGGER.debug("Processing Minor Drains property: {}", minorDrains);
                minorDrains = minorDrains.replaceAll(digitsRegex, "");
                BigDecimal value = getNumericValue(minorDrains, pl, DxfFileConstants.MINOR_DRAINS);
                pl.setMinorDrains(value);
                LOGGER.info("Set Minor Drains value to {}", value);
            }

            // Extract Notified Waterbodies
            String notifiedWaterbodies = planProperties.get(DxfFileConstants.NOTIFIED_WATERBODIES);
            if (StringUtils.isNotBlank(notifiedWaterbodies)) {
                LOGGER.debug("Processing Notified Waterbodies property: {}", notifiedWaterbodies);
                notifiedWaterbodies = notifiedWaterbodies.replaceAll(digitsRegex, "");
                BigDecimal value = getNumericValue(notifiedWaterbodies, pl, DxfFileConstants.NOTIFIED_WATERBODIES);
                pl.setNotifiedWaterBodies(value);
                LOGGER.info("Set Notified Waterbodies value to {}", value);
            }

            // Extract Other Notified Waterbodies
            String otherNotifiedWaterbodies = planProperties.get(DxfFileConstants.OTHER_NOTIFIED_WATERBODIES);
            if (StringUtils.isNotBlank(otherNotifiedWaterbodies)) {
                LOGGER.debug("Processing Other Notified Waterbodies property: {}", otherNotifiedWaterbodies);
                otherNotifiedWaterbodies = otherNotifiedWaterbodies.replaceAll(digitsRegex, "");
                BigDecimal value = getNumericValue(otherNotifiedWaterbodies, pl, DxfFileConstants.OTHER_NOTIFIED_WATERBODIES);
                pl.setOtherNotifiedWaterBodies(value);
                LOGGER.info("Set Other Notified Waterbodies value to {}", value);
            }

            // Extract Other Large Ponds/Waterbodies
            String otherLargePondsWaterbodies = planProperties.get(DxfFileConstants.OTHER_LARGE_PONDS_WATERBODIES);
            if (StringUtils.isNotBlank(otherLargePondsWaterbodies)) {
                LOGGER.debug("Processing Other Large Ponds Waterbodies property: {}", otherLargePondsWaterbodies);
                otherLargePondsWaterbodies = otherLargePondsWaterbodies.replaceAll(digitsRegex, "");
                BigDecimal value = getNumericValue(otherLargePondsWaterbodies, pl, DxfFileConstants.OTHER_LARGE_PONDS_WATERBODIES);
                pl.setOtherLargePondsOrWaterBody(value);
                LOGGER.info("Set Other Large Ponds Waterbodies value to {}", value);
            }

            // Extract Small Ponds
            String smallPonds = planProperties.get(DxfFileConstants.SMALL_PONDS);
            if (StringUtils.isNotBlank(smallPonds)) {
                LOGGER.debug("Processing Small Ponds property: {}", smallPonds);
                smallPonds = smallPonds.replaceAll(digitsRegex, "");
                BigDecimal value = getNumericValue(smallPonds, pl, DxfFileConstants.SMALL_PONDS);
                pl.setSmallPonds(value);
                LOGGER.info("Set Small Ponds value to {}", value);
            }

        } catch (Exception e) {
            LOGGER.error("Error occurred while extracting plan info for Location Plan", e);
        }
    }
    
    @Override
    public PlanDetail validate(PlanDetail pl) {

        return pl;
    }

}
