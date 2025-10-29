package org.egov.edcr.feature;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.egov.common.entity.edcr.Block;
import org.egov.common.entity.edcr.Building;
import org.egov.common.entity.edcr.Floor;
import org.egov.common.entity.edcr.Measurement;
import org.egov.common.entity.edcr.PercolationPit;
import org.egov.common.entity.edcr.RainWaterHarvesting;
import org.egov.common.entity.edcr.RoofArea;
import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.entity.blackbox.MeasurementDetail;
import org.egov.edcr.entity.blackbox.PlanDetail;
import org.egov.edcr.service.LayerNames;
import org.egov.edcr.utility.Util;
import org.kabeja.dxf.DXFCircle;
import org.kabeja.dxf.DXFConstants;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFLWPolyline;
import org.kabeja.dxf.DXFLayer;
import org.kabeja.dxf.DXFText;
import org.kabeja.dxf.helpers.StyledTextParagraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RainWaterHarvestingExtract extends FeatureExtract {
    private static final Logger LOG = LogManager.getLogger(RainWaterHarvestingExtract.class);
    @Autowired
    private LayerNames layerNames;
    private String digitsRegex = "[^\\d.]";
  

    @Override
    public PlanDetail extract(PlanDetail pl) {
        if (LOG.isInfoEnabled())
            LOG.info("Starting of Rain Water Harvesting Extract......");
        // Rain water harvesting Utility
        List<DXFLWPolyline> rainWaterHarvesting = Util.getPolyLinesByLayer(pl.getDoc(),
                layerNames.getLayerName("LAYER_NAME_RAINWATER_HARWESTING"));
        if (rainWaterHarvesting != null && !rainWaterHarvesting.isEmpty())
            for (DXFLWPolyline pline : rainWaterHarvesting) {
                Measurement measurement = new MeasurementDetail(pline, true);
                RainWaterHarvesting rwh = new RainWaterHarvesting();
                rwh.setArea(measurement.getArea());
                rwh.setColorCode(measurement.getColorCode());
                rwh.setHeight(measurement.getHeight());
                rwh.setWidth(measurement.getWidth());
                rwh.setLength(measurement.getLength());
                rwh.setInvalidReason(measurement.getInvalidReason());
                rwh.setPresentInDxf(true);
                pl.getUtility().addRainWaterHarvest(rwh);
            }

        List<DXFLWPolyline> percolationPits = Util.getPolyLinesByLayer(
                pl.getDoc(), layerNames.getLayerName("LAYER_NAME_RAINWATER_HARWESTING_PERCOLATION_PIT"));

        if (percolationPits != null && !percolationPits.isEmpty()) {
            for (DXFLWPolyline pline : percolationPits) {
                Measurement measurement = new MeasurementDetail(pline, true);
                PercolationPit pit = new PercolationPit();
//                pit.setLength(measurement.getLength());
//                pit.setWidth(measurement.getWidth());
                pit.setHeight(measurement.getHeight());
                pit.setArea(measurement.getArea());
                List<BigDecimal> width = Util.getListOfDimensionByColourCode(pl, layerNames.getLayerName("LAYER_NAME_RAINWATER_HARWESTING_PERCOLATION_PIT"),
    					DxfFileConstants.INDEX_COLOR_TWO);
                
                List<BigDecimal> length = Util.getListOfDimensionByColourCode(pl, layerNames.getLayerName("LAYER_NAME_RAINWATER_HARWESTING_PERCOLATION_PIT"),
    					DxfFileConstants.INDEX_COLOR_ONE);
                
                String pitHeight = Util.getMtextByLayerName(pl.getDoc(), layerNames.getLayerName("LAYER_NAME_RAINWATER_HARWESTING_PERCOLATION_PIT"), "RWH_PERCOLATION_PIT");

                if (!isBlank(pitHeight)) {
                    if (pitHeight.contains("="))
                    	pitHeight = pitHeight.split("=")[1] != null
                                ? pitHeight.split("=")[1].replaceAll("[^\\d.]", "")
                                : "";
                    else
                    	pitHeight = pitHeight.replaceAll("[^\\d.]", "");

                    if (!isBlank(pitHeight)) {
                        BigDecimal height = BigDecimal.valueOf(Double.parseDouble(pitHeight));
                        pit.setPitHeight(height);
                    }}
                
                pit.setPitLength(length);
                pit.setPitWidth(width);

                pit.setPresentInDxf(true);

                pl.getUtility().addPercolationPit(pit); 
        

                if (LOG.isInfoEnabled()) {
                    LOG.info("Percolation Pit extracted: Area={}, Height={}, Width={}, Length={}",
                            pit.getArea(), pit.getHeight(), pit.getWidth(), pit.getLength());
                }
            }
        }
        
        List<DXFCircle> rainWaterHarvestingCircle = Util.getPolyCircleByLayer(pl.getDoc(),
                layerNames.getLayerName("LAYER_NAME_RAINWATER_HARWESTING"));
        if (rainWaterHarvestingCircle != null && !rainWaterHarvestingCircle.isEmpty())
            for (DXFCircle pline : rainWaterHarvestingCircle) {
                RainWaterHarvesting rwh = new RainWaterHarvesting();
                rwh.setColorCode(pline.getColor());
                rwh.setRadius(BigDecimal.valueOf(pline.getRadius()));
                rwh.setPresentInDxf(true);
                pl.getUtility().addRainWaterHarvest(rwh);
            }

        if (pl.getDoc().containsDXFLayer(layerNames.getLayerName("LAYER_NAME_RAINWATER_HARWESTING"))) {
            String tankCapacity = Util.getMtextByLayerName(pl.getDoc(),
                    layerNames.getLayerName("LAYER_NAME_RAINWATER_HARWESTING"),
                    layerNames.getLayerName("LAYER_NAME_RWH_CAPACITY_L"));
            if (tankCapacity != null && !tankCapacity.isEmpty())
                try {
                    if (tankCapacity.contains(";")) {
                        String[] textSplit = tankCapacity.split(";");
                        int length = textSplit.length;

                        if (length >= 1) {
                            int index = length - 1;
                            tankCapacity = textSplit[index];
                            tankCapacity = tankCapacity.replaceAll("[^\\d.]", "");
                        } else
                            tankCapacity = tankCapacity.replaceAll("[^\\d.]", "");
                    } else
                        tankCapacity = tankCapacity.replaceAll("[^\\d.]", "");

                    if (!tankCapacity.isEmpty())
                        pl.getUtility().setRainWaterHarvestingTankCapacity(BigDecimal.valueOf(Double.parseDouble(tankCapacity)));

                } catch (NumberFormatException e) {
                    pl.addError(layerNames.getLayerName("LAYER_NAME_RAINWATER_HARWESTING"),
                            "Rain water Harwesting tank capity value contains non numeric character.");
                }
        }

        String rwhLayerPattern = layerNames.getLayerName("LAYER_NAME_RAINWATER_HARWESTING") + "_+\\d";

        List<String> rwhLayers = Util.getLayerNamesLike(pl.getDoc(), rwhLayerPattern);

        if (rwhLayers != null && !rwhLayers.isEmpty())
            for (String rwhLayer : rwhLayers) {
                String[] rwhLayerNameSplit = rwhLayer.split("_");
                List<DXFLWPolyline> rwhPolyLine = Util.getPolyLinesByLayer(pl.getDoc(), rwhLayer);
                List<BigDecimal> dimensionValues = Util.getListOfDimensionValueByLayer(pl, rwhLayer);

                String tankCapacity = Util.getMtextByLayerName(pl.getDoc(), rwhLayer,
                        layerNames.getLayerName("LAYER_NAME_RWH_CAPACITY_L"));
                if (tankCapacity != null && !tankCapacity.isEmpty())
                    try {
                        if (tankCapacity.contains(";")) {
                            String[] textSplit = tankCapacity.split(";");
                            int length = textSplit.length;

                            if (length >= 1) {
                                int index = length - 1;
                                tankCapacity = textSplit[index];
                                tankCapacity = tankCapacity.replaceAll("[^\\d.]", "");
                            } else
                                tankCapacity = tankCapacity.replaceAll("[^\\d.]", "");
                        } else
                            tankCapacity = tankCapacity.replaceAll("[^\\d.]", "");

                    } catch (NumberFormatException e) {
                        pl.addError(layerNames.getLayerName("LAYER_NAME_RAINWATER_HARWESTING"),
                                "Rain water Harwesting tank capity value contains non numeric character.");
                    }

                if (rwhPolyLine != null && !rwhPolyLine.isEmpty())
                    for (DXFLWPolyline pline : rwhPolyLine) {
                        Measurement measurement = new MeasurementDetail(pline, true);
                        RainWaterHarvesting rwh = new RainWaterHarvesting();
                        rwh.setNumber(Integer.valueOf(rwhLayerNameSplit[1]));
                        rwh.setArea(measurement.getArea());
                        rwh.setColorCode(measurement.getColorCode());
                        rwh.setHeight(measurement.getHeight());
                        rwh.setWidth(measurement.getWidth());
                        rwh.setLength(measurement.getLength());
                        rwh.setInvalidReason(measurement.getInvalidReason());
                        rwh.setPresentInDxf(true);
                        rwh.setTankHeight(dimensionValues);

                        if (tankCapacity != null && !tankCapacity.isEmpty())
                            rwh.setTankCapacity(BigDecimal.valueOf(Double.parseDouble(tankCapacity)));

                        pl.getUtility().addRainWaterHarvest(rwh);
                    }

                List<DXFCircle> rwhCircle = Util.getPolyCircleByLayer(pl.getDoc(), rwhLayer);
                if (rwhCircle != null && !rwhCircle.isEmpty())
                    for (DXFCircle pline : rwhCircle) {
                        RainWaterHarvesting rwh = new RainWaterHarvesting();
                        rwh.setNumber(Integer.valueOf(rwhLayerNameSplit[1]));
                        rwh.setColorCode(pline.getColor());
                        rwh.setRadius(BigDecimal.valueOf(pline.getRadius()));
                        rwh.setPresentInDxf(true);
                        rwh.setTankHeight(dimensionValues);
                        
                        if (!tankCapacity.isEmpty())
                            rwh.setTankCapacity(BigDecimal.valueOf(Double.parseDouble(tankCapacity)));

                        pl.getUtility().addRainWaterHarvest(rwh);
                    }

            }

        for (Block block : pl.getBlocks()) {
            Building building = block.getBuilding();
            if (building != null) {
                List<Floor> floors = building.getFloors();
                if (floors != null && !floors.isEmpty())
                    for (Floor floor : floors) {
                        String roofAreaLayerName = String.format(layerNames.getLayerName("LAYER_NAME_ROOF_AREA"),
                                block.getNumber(), floor.getNumber());
                        List<DXFLWPolyline> roofAreas = Util.getPolyLinesByLayer(pl.getDoc(), roofAreaLayerName);

                        if (roofAreas != null && !roofAreas.isEmpty()) {
                            List<RoofArea> roofAreaList = new ArrayList<>();
                            for (DXFLWPolyline pline : roofAreas) {
                                Measurement measurement = new MeasurementDetail(pline, true);
                                RoofArea roofArea = new RoofArea();
                                roofArea.setArea(measurement.getArea());
                                roofArea.setColorCode(measurement.getColorCode());
                                roofArea.setHeight(measurement.getHeight());
                                roofArea.setWidth(measurement.getWidth());
                                roofArea.setLength(measurement.getLength());
                                roofArea.setInvalidReason(measurement.getInvalidReason());
                                roofArea.setPresentInDxf(true);
                                roofAreaList.add(roofArea);
                            }
                            floor.setRoofAreas(roofAreaList);
                        }
                    }
            }
        }
        extractRWHInfo(pl);
        if (LOG.isInfoEnabled())
            LOG.info("End of Rain Water Harvesting Extract......");
        return pl;
    }
    
    public Map<String, String> getFormatedRWHProperties(DXFDocument doc) {

        DXFLayer rwhlayer = doc.getDXFLayer(layerNames.getLayerName("LAYER_NAME_RAINWATER_HARWESTING"));
        List texts = rwhlayer.getDXFEntities(DXFConstants.ENTITY_TYPE_MTEXT);
        DXFText text = null;
        Map<String, String> rwhProperties = new HashMap<>();

        if (texts != null && texts.size() > 0) {
            Iterator iterator = texts.iterator();
            while (iterator.hasNext()) {
                text = (DXFText) iterator.next();
                Iterator styledParagraphIterator = text.getTextDocument().getStyledParagraphIterator();
                while (styledParagraphIterator.hasNext()) {
                    StyledTextParagraph styledTextParagraph = (StyledTextParagraph) styledParagraphIterator.next();
                    String[] data = styledTextParagraph.getText().split("=");
                    LOG.info(styledTextParagraph.getText());
                    if (data.length == 2)
                    	rwhProperties.put(data[0].trim(), data[1].trim());
                }

            }
        }
        return rwhProperties;
    }
    
    /**
     * Extracts Rain Water Harvesting (RWH) related information from the DXF document
     * and updates the {@link PlanDetail} with extracted values.
     * <p>
     * Specifically, this method looks for the pipe diameter property in the RWH layer.
     * If found, it removes any non-numeric characters, converts the value into a
     * {@link BigDecimal}, and stores it in the plan's utility object.
     * </p>
     *
     * @param pl the {@link PlanDetail} object containing the DXF document and utility details
     */
    private void extractRWHInfo(PlanDetail pl) {
        if (LOG.isInfoEnabled()) {
            LOG.info("Starting extraction of Rain Water Harvesting (RWH) information...");
        }

        Map<String, String> rwhProperties = getFormatedRWHProperties(pl.getDoc());
        if (LOG.isDebugEnabled()) {
            LOG.debug("Extracted RWH properties: {}", rwhProperties);
        }

        String pipeDiameter = rwhProperties.get(DxfFileConstants.PIPE_DIA);
        if (StringUtils.isNotBlank(pipeDiameter)) {
            LOG.info("Raw pipe diameter value found: {}", pipeDiameter);

            pipeDiameter = pipeDiameter.replaceAll(digitsRegex, "");
            BigDecimal pipeDiameterValue = getNumericValue(pipeDiameter, pl, DxfFileConstants.PIPE_DIA);

            if (pipeDiameterValue != null) {
                pl.getUtility().setRwhPipeDia(pipeDiameterValue);
                LOG.info("RWH Pipe Diameter extracted and set: {}", pipeDiameterValue);
            } else {
                LOG.warn("Failed to convert RWH pipe diameter '{}' into numeric value.", pipeDiameter);
            }
        } else {
            LOG.warn("No valid RWH pipe diameter information found in RWH properties.");
        }

        if (LOG.isInfoEnabled()) {
            LOG.info("Completed extraction of Rain Water Harvesting (RWH) information.");
        }
    }
    
 
    @Override
    public PlanDetail validate(PlanDetail pl) {
        return pl;
    }

}
