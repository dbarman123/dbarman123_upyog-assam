package org.egov.edcr.feature;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.egov.common.entity.edcr.Block;
import org.egov.common.entity.edcr.DARamp;
import org.egov.common.entity.edcr.DARoom;
import org.egov.common.entity.edcr.Floor;
import org.egov.common.entity.edcr.Measurement;
import org.egov.common.entity.edcr.Ramp;
import org.egov.common.entity.edcr.RampLanding;
import org.egov.common.entity.edcr.TypicalFloor;
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
public class RampServiceExtract extends FeatureExtract {

    @Autowired
    private LayerNames layerNames;
    private static final Logger LOG = LogManager.getLogger(RampServiceExtract.class);
    @Override
    public PlanDetail extract(PlanDetail pl) {
        if (pl != null && !pl.getBlocks().isEmpty())
            for (Block block : pl.getBlocks()) {
                String rampLayerNameRegex = String.format(layerNames.getLayerName("LAYER_NAME_DA_RAMP"), block.getNumber())
                        + "_+\\d";
                List<String> rampLayerNames = Util.getLayerNamesLike(pl.getDoc(), rampLayerNameRegex);
                for (String rampLayerName : rampLayerNames) {
                    List<DXFLWPolyline> polyLines = Util.getPolyLinesByLayer(pl.getDoc(), rampLayerName);
                    String[] layerArray = rampLayerName.split("_", 5);
                    BigDecimal slope = extractSlope(pl, rampLayerName);

                    List<Measurement> convertedPolyLines = polyLines.stream()
                            .map(polyLine -> new MeasurementDetail(polyLine, true)).collect(Collectors.toList());

                    List<BigDecimal> daRampWidth = Util.getListOfDimensionByColourCode(pl, rampLayerName,
        					DxfFileConstants.INDEX_COLOR_TWO);
                    
 
                    if (!polyLines.isEmpty() && polyLines != null && !layerArray[4].isEmpty()
                            && layerArray[4] != null) {
                        DARamp daRamp = new DARamp();
                        daRamp.setNumber(Integer.valueOf(layerArray[4]));
                        daRamp.setMeasurements(convertedPolyLines);
                        daRamp.setPresentInDxf(true);
                        daRamp.setSlope(slope);
                        block.addDARamps(daRamp);
                    	String landingNamePattern = String.format(layerNames.getLayerName("LAYER_NAME_DA_RAMP_LANDING"),
    							block.getNumber(), "+\\d");

                    	if(daRampWidth != null) {
                        	daRamp.setDaRampWidth(daRampWidth);
                        }
    					addRampLanding(pl, landingNamePattern, daRamp);
                        
                    }

                }
                if (block.getBuilding() != null && !block.getBuilding().getFloors().isEmpty()) {
                    outside: for (Floor floor : block.getBuilding().getFloors()) {
                        if (!block.getTypicalFloor().isEmpty())
                            for (TypicalFloor tp : block.getTypicalFloor())
                                if (tp.getRepetitiveFloorNos().contains(floor.getNumber()))
                                    for (Floor allFloors : block.getBuilding().getFloors())
                                        if (allFloors.getNumber().equals(tp.getModelFloorNo()))
                                            if (!allFloors.getDaRooms().isEmpty()) {
                                                floor.setDaRooms(allFloors.getDaRooms());
                                                continue outside;
                                            }
                        String daRoomLayerName = String.format(layerNames.getLayerName("LAYER_NAME_DA_ROOM"), block.getNumber(),
                                floor.getNumber());
                        List<DXFLWPolyline> polyLinesByLayer = Util.getPolyLinesByLayer(pl.getDoc(), daRoomLayerName);
                        if (!polyLinesByLayer.isEmpty() && polyLinesByLayer != null)
                            for (DXFLWPolyline polyline : polyLinesByLayer) {
                                DARoom daRoom = new DARoom();
                                daRoom.setPresentInDxf(true);
                                floor.addDaRoom(daRoom);
                            }
                    }
                    outside: for (Floor floor : block.getBuilding().getFloors()) {
                        if (!block.getTypicalFloor().isEmpty())
                            for (TypicalFloor tp : block.getTypicalFloor())
                                if (tp.getRepetitiveFloorNos().contains(floor.getNumber()))
                                    for (Floor allFloors : block.getBuilding().getFloors())
                                        if (allFloors.getNumber().equals(tp.getModelFloorNo()))
                                            if (!allFloors.getRamps().isEmpty()) {
                                                floor.setRamps(allFloors.getRamps());
                                                continue outside;
                                            }
                        String rampRegex = String.format(layerNames.getLayerName("LAYER_NAME_RAMP"), block.getNumber(),
                                floor.getNumber()) + "_+\\d";
                        List<String> rampLayer = Util.getLayerNamesLike(pl.getDoc(), rampRegex);
                        if (!rampLayer.isEmpty())
                            for (String rmpLayer : rampLayer) {
                                List<DXFLWPolyline> polylines = Util.getPolyLinesByLayer(pl.getDoc(), rmpLayer);
                                String[] splitLayer = rmpLayer.split("_", 6);
                                if (splitLayer[5] != null && !splitLayer[5].isEmpty() && !polylines.isEmpty()) {
                                    Ramp ramp = new Ramp();
                                    ramp.setNumber(Integer.valueOf(splitLayer[5]));
                                    boolean isClosed = polylines.stream()
                                            .allMatch(dxflwPolyline -> dxflwPolyline.isClosed());
                                    ramp.setRampClosed(isClosed);
                                    List<Measurement> rampPolyLine = polylines.stream()
                                            .map(dxflwPolyline -> new MeasurementDetail(dxflwPolyline, true))
                                            .collect(Collectors.toList());
                                    ramp.setRamps(rampPolyLine);
                                    String floorHeight = Util.getMtextByLayerName(pl.getDoc(), rmpLayer, "FLR_HT_M");

                                    if (!isBlank(floorHeight)) {
                                        if (floorHeight.contains("="))
                                            floorHeight = floorHeight.split("=")[1] != null
                                                    ? floorHeight.split("=")[1].replaceAll("[^\\d.]", "")
                                                    : "";
                                        else
                                            floorHeight = floorHeight.replaceAll("[^\\d.]", "");

                                        if (!isBlank(floorHeight)) {
                                            BigDecimal height = BigDecimal.valueOf(Double.parseDouble(floorHeight));
                                            ramp.setFloorHeight(height);
                                        }
                                        BigDecimal minEntranceHeight = extractMinEntranceHeight(pl, rmpLayer);
                                        ramp.setMinEntranceHeight(minEntranceHeight);
                                       
                                        floor.addRamps(ramp);
                                    }
                                }
                            }
                    }
                }
            }
        return pl;
    }

    /**
     * Extracts the slope of a ramp from the given DXF layer text.
     * Expected format in DXF layer: "SLOPE=1 in 12"
     * 
     * @param pl The PlanDetail containing the DXF document.
     * @param rampLayerName The DXF layer name where the slope is defined.
     * @return The slope as BigDecimal (dividend/divisor). Returns BigDecimal.ZERO if not found or invalid.
     */
    /**
     * Extracts the ramp slope from DXF layer text.
     * The DXF text is expected in the format: "FLR_HT_M=<height>".
     * According to the rule, slope = run / rise, where max slope = 1 in 12.
     *
     * @param pl The PlanDetail containing the DXF document
     * @param rampLayerName The DXF layer name
     * @return Slope as BigDecimal (run/rise). Returns 0 if not found.
     */
    private BigDecimal extractSlope(PlanDetail pl, String rampLayerName) {
        String text = Util.getMtextByLayerName(pl.getDoc(), rampLayerName);
        BigDecimal slope = BigDecimal.ZERO;

        if (text == null || text.isEmpty()) {
            LOG.debug("No text found in layer: {}", rampLayerName);
            return slope;
        }

        if (!text.contains("=")) {
            LOG.debug("Text in layer '{}' does not contain '=': {}", rampLayerName, text);
            return slope;
        }

        String[] parts = text.split("=", 2);
        if (parts.length != 2) {
            LOG.debug("Cannot split text by '=' in layer '{}': {}", rampLayerName, text);
            return slope;
        }

        String key = parts[0].trim();
        String value = parts[1].trim();

        if (!"FLR_HT_M".equalsIgnoreCase(key)) {
            LOG.debug("Layer '{}' does not contain ramp height info. Found key: {}", rampLayerName, key);
            return slope;
        }

        try {
            BigDecimal rise = new BigDecimal(value);
            if (rise.compareTo(BigDecimal.ZERO) > 0) {
                slope = BigDecimal.valueOf(12).divide(rise, 2, RoundingMode.HALF_UP); // run/rise
                LOG.debug("Extracted ramp slope from layer '{}': FLR_HT_M={} -> slope={}", rampLayerName, rise, slope);
            }
        } catch (NumberFormatException ex) {
            LOG.debug("Failed to parse ramp height in layer '{}': {}", rampLayerName, value, ex);
        }

        return slope;
    }


	private BigDecimal extractMinEntranceHeight(PlanDetail pl, String rampLayerName) {
	    String text = Util.getMtextByLayerName(pl.getDoc(), rampLayerName, "ENT_HT");
	    BigDecimal entranceHeight = BigDecimal.ZERO;
	    if (text != null && !text.isEmpty()) {
	        if (text.contains("=")) {
	            text = text.split("=")[1] != null ? text.split("=")[1].replaceAll("[^\\d.]", "") : "";
	        } else {
	            text = text.replaceAll("[^\\d.]", "");
	        }

	        if (!isBlank(text)) {
	            entranceHeight = new BigDecimal(text);
	        }
	    }
	    return entranceHeight;
	}
	
	/**
	 * Adds ramp landings to the given {@link DARamp} by extracting and processing
	 * layers from the DXF document within the given {@link PlanDetail}.
	 * <p>
	 * This method identifies all layers matching the specified landing name pattern,
	 * processes polylines on these layers to create {@link RampLanding} objects,
	 * and assigns these landings to the provided {@link DARamp} instance.
	 * </p>
	 *
	 * @param pl The {@link PlanDetail} containing the DXF document and related data.
	 * @param landingNamePattern The pattern used to identify landing layers within the DXF document.
	 * @param daRamp The {@link DARamp} instance to which the extracted ramp landings will be assigned.
	 */
	
	private void addRampLanding(PlanDetail pl, String landingNamePattern, DARamp daRamp) {
		DXFDocument doc = pl.getDoc();
	    List<String> landingLayerNames = Util.getLayerNamesLike(doc, landingNamePattern);
		List<RampLanding> landings = new ArrayList<>();

		for (String landingLayer : landingLayerNames) {

			RampLanding rampLanding = new RampLanding();

			String[] landingNo = landingLayer.split("_");

			rampLanding.setNumber(landingNo[7]);

			List<DXFLWPolyline> landingPolyLines = Util.getPolyLinesByLayer(doc, landingLayer);

			boolean isClosed = landingPolyLines.stream().allMatch(dxflwPolyline -> dxflwPolyline.isClosed());

			rampLanding.setLandingClosed(isClosed);

			List<Measurement> landingPolyLinesMeasurement = landingPolyLines.stream()
					.map(flightPolyLine -> new MeasurementDetail(flightPolyLine, true)).collect(Collectors.toList());

			rampLanding.setLandings(landingPolyLinesMeasurement);

			// set length of flight
			List<BigDecimal> landingLengths = Util.getListOfDimensionByColourCode(pl, landingLayer,
					DxfFileConstants.STAIR_FLIGHT_LENGTH_COLOR);

			rampLanding.setLengths(landingLengths);

			// set width of flight
			List<BigDecimal> landingWidths = Util.getListOfDimensionByColourCode(pl, landingLayer,
					DxfFileConstants.STAIR_FLIGHT_WIDTH_COLOR);

			rampLanding.setWidths(landingWidths);

			landings.add(rampLanding);
		}

		daRamp.setLandings(landings);
	}



    @Override
    public PlanDetail validate(PlanDetail pl) {
        return pl;
    }
}
