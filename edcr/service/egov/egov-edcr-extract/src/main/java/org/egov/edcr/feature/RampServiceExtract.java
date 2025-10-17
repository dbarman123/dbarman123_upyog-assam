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
                    

                    List<Measurement> convertedPolyLines = polyLines.stream()
                            .map(polyLine -> new MeasurementDetail(polyLine, true)).collect(Collectors.toList());

                    List<BigDecimal> daRampWidth = Util.getListOfDimensionByColourCode(pl, rampLayerName,
        					DxfFileConstants.INDEX_COLOR_TWO);
                    
                    List<BigDecimal> daRampLength = Util.getListOfDimensionByColourCode(pl, rampLayerName,
        					DxfFileConstants.INDEX_COLOR_ONE);
                    BigDecimal slope = BigDecimal.ZERO;
 
                    if (!polyLines.isEmpty() && polyLines != null && !layerArray[4].isEmpty()
                            && layerArray[4] != null) {
                        DARamp daRamp = new DARamp();
                        daRamp.setNumber(Integer.valueOf(layerArray[4]));
                        daRamp.setMeasurements(convertedPolyLines);
                        daRamp.setPresentInDxf(true);
                       
                        if (daRampWidth != null && !daRampWidth.isEmpty()) {
                            daRamp.setDaRampWidth(daRampWidth);
                        }
                        if (daRampLength != null && !daRampLength.isEmpty()) {
                            daRamp.setDaRampLength(daRampLength);
                           slope =  extractDASlope(pl, rampLayerName, daRampLength, daRamp);
                        }
                      
                        daRamp.setSlope(slope);
                    	block.addDARamps(daRamp);
                    	String landingNamePattern = String.format(
                    		    layerNames.getLayerName("LAYER_NAME_DA_RAMP_LANDING"),
                    		    block.getNumber(),
                    		    daRamp.getNumber()
                    		);

                   	
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
                                        
                                        List<BigDecimal> rampWidth = Util.getListOfDimensionByColourCode(pl, rmpLayer,
                            					DxfFileConstants.INDEX_COLOR_TWO);
                                        
                                        List<BigDecimal> rampLength = Util.getListOfDimensionByColourCode(pl, rmpLayer,
                            					DxfFileConstants.INDEX_COLOR_ONE);
                                        
                                        if (rampWidth != null && !rampWidth.isEmpty()) {
                                            ramp.setRampWidth(rampWidth);
                                        }
                                        if (rampLength != null && !rampLength.isEmpty()) {
                                            ramp.setRampLength(rampLength);}
                                        
                                        BigDecimal  slope =  extractSlope(pl, rmpLayer, rampLength, ramp);
                                        ramp.setSlope(slope);
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
     * Extracts and calculates the slope (rise/run) for a DA ramp layer.
     * The rise (height) is read from MText (key = FLR_HT_M),
     * and the run (length) is taken from the dimension list (daRampLength).
     *
     * @param pl              the PlanDetail object containing DXF data
     * @param rampLayerName   the layer name of the ramp
     * @param daRampLength    the list of ramp lengths (in meters)
     * @return the calculated slope as rise/run (BigDecimal)
     */
    private BigDecimal extractDASlope(PlanDetail pl, String rampLayerName, List<BigDecimal> daRampLength, DARamp daRamp) {
        BigDecimal slope = BigDecimal.ZERO;
        String text = Util.getMtextByLayerName(pl.getDoc(), rampLayerName); // MText content (e.g., "FLR_HT_M=0.5")

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
            // Extract ramp height (m)
            BigDecimal height = new BigDecimal(value.replaceAll("[^\\d.]", "")); 
            daRamp.setHeight(height);

            // Sum all lengths from daRampLength list
            BigDecimal totalLength = daRampLength != null 
                    ? daRampLength.stream().reduce(BigDecimal.ZERO, BigDecimal::add) 
                    : BigDecimal.ZERO;

            if (height.compareTo(BigDecimal.ZERO) > 0 && totalLength.compareTo(BigDecimal.ZERO) > 0) {
                // slope = length / height (as per your requirement)
                slope = totalLength.divide(height, 3, RoundingMode.HALF_UP);
                LOG.debug("Extracted ramp slope from layer '{}': height={}m, totalLength={}m -> slope={}", 
                          rampLayerName, height, totalLength, slope);
            } else {
                LOG.debug("Invalid height or totalLength for layer '{}': height={}, totalLength={}", 
                          rampLayerName, height, totalLength);
            }
        } catch (NumberFormatException ex) {
            LOG.debug("Failed to parse ramp height in layer '{}': {}", rampLayerName, value, ex);
        }

        return slope;
    }

    /**
     * Extracts and calculates the slope (rise/run) for a DA ramp layer.
     * The rise (height) is read from MText (key = FLR_HT_M),
     * and the run (length) is taken from the dimension list (daRampLength).
     *
     * @param pl              the PlanDetail object containing DXF data
     * @param rampLayerName   the layer name of the ramp
     * @param daRampLength    the list of ramp lengths (in meters)
     * @return the calculated slope as rise/run (BigDecimal)
     */
    private BigDecimal extractSlope(PlanDetail pl, String rampLayerName, List<BigDecimal> daRampLength, Ramp ramp) {
        BigDecimal slope = BigDecimal.ZERO;
        String text = Util.getMtextByLayerName(pl.getDoc(), rampLayerName); // MText content (e.g., "FLR_HT_M=0.5")

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
            // Extract ramp height (m)
            BigDecimal height = new BigDecimal(value.replaceAll("[^\\d.]", "")); 
            ramp.setHeight(height);

            // Sum all lengths from daRampLength list
            BigDecimal totalLength = daRampLength != null 
                    ? daRampLength.stream().reduce(BigDecimal.ZERO, BigDecimal::add) 
                    : BigDecimal.ZERO;

            if (height.compareTo(BigDecimal.ZERO) > 0 && totalLength.compareTo(BigDecimal.ZERO) > 0) {
                // slope = length / height (as per your requirement)
                slope = totalLength.divide(height, 3, RoundingMode.HALF_UP);
                LOG.debug("Extracted ramp slope from layer '{}': height={}m, totalLength={}m -> slope={}", 
                          rampLayerName, height, totalLength, slope);
            } else {
                LOG.debug("Invalid height or totalLength for layer '{}': height={}, totalLength={}", 
                          rampLayerName, height, totalLength);
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
	        String[] parts = landingLayer.split("_");

	        // Defensive check — ensure proper format
	        if (parts.length >= 6) {
	            // Example: BLK_1_DA_RAMP_1_LANDING
	            // parts[1] = block number
	            // parts[4] = ramp number
	            // parts[5] = "LANDING" (or landing number if present)

	            String blockNo = parts[1];
	            String rampNo  = parts[4];
	            String landingNo = parts.length > 6 ? parts[6] : "1"; // optional number if exists
	            rampLanding.setNumber(landingNo); // safely set landing number
	        } else {
	            LOG.warn("Unexpected landing layer format: " + landingLayer);
	            continue;
	        }

	        List<DXFLWPolyline> landingPolyLines = Util.getPolyLinesByLayer(doc, landingLayer);
	        boolean isClosed = landingPolyLines.stream().allMatch(DXFLWPolyline::isClosed);
	        rampLanding.setLandingClosed(isClosed);

	        List<Measurement> landingPolyLinesMeasurement = landingPolyLines.stream()
	                .map(polyLine -> new MeasurementDetail(polyLine, true))
	                .collect(Collectors.toList());
	        rampLanding.setLandings(landingPolyLinesMeasurement);

	        // Set length of landing
	        List<BigDecimal> landingLengths = Util.getListOfDimensionByColourCode(
	                pl, landingLayer, DxfFileConstants.STAIR_FLIGHT_LENGTH_COLOR);
	        rampLanding.setLengths(landingLengths);

	        // Set width of landing
	        List<BigDecimal> landingWidths = Util.getListOfDimensionByColourCode(
	                pl, landingLayer, DxfFileConstants.STAIR_FLIGHT_WIDTH_COLOR);
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
