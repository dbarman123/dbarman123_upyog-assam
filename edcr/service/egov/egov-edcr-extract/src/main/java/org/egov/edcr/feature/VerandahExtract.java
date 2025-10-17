package org.egov.edcr.feature;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.egov.common.entity.edcr.Block;
import org.egov.common.entity.edcr.Floor;
import org.egov.common.entity.edcr.FloorUnit;
import org.egov.common.entity.edcr.Measurement;
import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.entity.blackbox.MeasurementDetail;
import org.egov.edcr.entity.blackbox.PlanDetail;
import org.egov.edcr.service.LayerNames;
import org.egov.edcr.utility.Util;
import org.kabeja.dxf.DXFLWPolyline;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VerandahExtract extends FeatureExtract {

	private static final Logger LOG = LogManager.getLogger(VerandahExtract.class);
	@Autowired
	private LayerNames layerNames;

	/**
	 * Extracts verandah details for all units in all floors of all blocks in the given PlanDetail.
	 *
	 * @param pl the PlanDetail containing blocks, floors, and units
	 * @return the same PlanDetail with verandah details extracted for each unit
	 */
	@Override
	public PlanDetail extract(PlanDetail pl) {
	    LOG.debug("Starting verandah extraction for PlanDetail with [{}] blocks", pl.getBlocks().size());

	    for (Block b : pl.getBlocks()) {
	        if (b.getBuilding() != null && b.getBuilding().getFloors() != null
	                && !b.getBuilding().getFloors().isEmpty()) {
	            LOG.debug("Processing Block [{}] with [{}] floors", b.getNumber(), b.getBuilding().getFloors().size());

	            for (Floor f : b.getBuilding().getFloors()) {
	                LOG.debug("Processing Floor [{}] in Block [{}]", f.getNumber(), b.getNumber());

	                for (FloorUnit unit : f.getUnits()) {
	                    LOG.debug("Extracting verandah for Unit [{}] in Floor [{}], Block [{}]",
	                              unit.getUnitNumber(), f.getNumber(), b.getNumber());
	                    extractVerandah(pl, b, f, unit);
	                }
	            }
	        } else {
	            LOG.debug("Skipping Block [{}] as it has no floors or building information", b.getNumber());
	        }
	    }

	    LOG.debug("Completed verandah extraction for PlanDetail");
	    return pl;
	}

	
	
	/**
	 * Extracts verandah measurements, height, and width for a specific floor.
	 *
	 * @param pl the plan detail object
	 * @param b  the block being processed
	 * @param f  the floor being processed
	 */
	private void extractVerandah(PlanDetail pl, Block b, Floor f, FloorUnit unit) {
	    String verandahLayer = String.format(
	            layerNames.getLayerName("LAYER_NAME_UNIT_VERANDAH"),
	            b.getNumber(), f.getNumber(), unit.getUnitNumber());

	    LOG.debug("Extracting verandah for Unit [{}] in Floor [{}], Block [{}] from layer [{}]",
	            unit.getUnitNumber(), f.getNumber(), b.getNumber(), verandahLayer);

	    List<DXFLWPolyline> verandahs = Util.getPolyLinesByLayer(pl.getDoc(), verandahLayer);
	    LOG.debug("Found [{}] verandah polylines for Unit [{}]", verandahs.size(), unit.getUnitNumber());

	    if (!verandahs.isEmpty()) {
	        // Extract measurements
	        List<Measurement> verandahMeasurements = verandahs.stream()
	                .map(polyline -> new MeasurementDetail(polyline, true))
	                .collect(Collectors.toList());
	        unit.getVerandah().setMeasurements(verandahMeasurements);
	        LOG.debug("Added [{}] verandah measurements for Unit [{}]", verandahMeasurements.size(), unit.getUnitNumber());

	        // Verandah Height from dimension
	        List<BigDecimal> verandahHeight = Util.getListOfDimensionByColourCode(
	                pl, verandahLayer, DxfFileConstants.INDEX_COLOR_ONE);
	        LOG.debug("Extracted [{}] verandah height values for Unit [{}]", verandahHeight.size(), unit.getUnitNumber());

	        // Verandah Width from dimension
	        List<BigDecimal> verandahWidth = Util.getListOfDimensionByColourCode(
	                pl, verandahLayer, DxfFileConstants.INDEX_COLOR_TWO);
	        LOG.debug("Extracted [{}] verandah width values for Unit [{}]", verandahWidth.size(), unit.getUnitNumber());

	        unit.getVerandah().setHeightOrDepth(verandahHeight);
	        unit.getVerandah().setVerandahWidth(verandahWidth);
	    } else {
	        LOG.debug("No verandah polylines found for Unit [{}] in Floor [{}], Block [{}]",
	                unit.getUnitNumber(), f.getNumber(), b.getNumber());
	    }
	}
	
	@Override
	public PlanDetail validate(PlanDetail pl) {
		return pl;
	}

}
