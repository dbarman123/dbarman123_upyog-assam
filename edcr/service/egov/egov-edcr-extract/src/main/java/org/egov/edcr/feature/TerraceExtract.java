package org.egov.edcr.feature;

import java.math.BigDecimal;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.egov.common.entity.edcr.Block;
import org.egov.common.entity.edcr.Measurement;
import org.egov.common.entity.edcr.Terrace;
import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.entity.blackbox.MeasurementDetail;
import org.egov.edcr.entity.blackbox.PlanDetail;
import org.egov.edcr.service.LayerNames;
import org.egov.edcr.utility.Util;
import org.kabeja.dxf.DXFLWPolyline;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TerraceExtract extends FeatureExtract {
    private static final Logger LOG = LogManager.getLogger(TerraceExtract.class);

    @Autowired
    private LayerNames layerNames;

    @Override
    public PlanDetail validate(PlanDetail pl) {
        return pl;
    }

    @Override
    public PlanDetail extract(PlanDetail pl) {

        if (LOG.isInfoEnabled())
            LOG.info("Starting of terrace Extract......");

        extractTerrace(pl);
        extractStaircaseHeadroom(pl); 

        if (LOG.isInfoEnabled())
            LOG.info("End of terrace Extract......");

        return pl;
    }

    /**
 * Extracts terrace geometry such as area, height, width and length
 * for each block based on the terrace layer defined in the DXF file.
 * <p>
 * This method reads the layer pattern configured as "LAYER_NAME_TERRACE",
 * fetches the terrace polyline for each block, converts the polyline
 * into measurement details, and updates the corresponding {@link Terrace}
 * object inside the {@link Block}. If the Terrace object does not exist,
 * it is created and attached to the block.
 * </p>
 *
 * @param pl The plan detail object containing building blocks and DXF data.
 */
    private void extractTerrace(PlanDetail pl) {

        for (Block block : pl.getBlocks()) {

            String terraceLayerName = String.format(
                    layerNames.getLayerName("LAYER_NAME_TERRACE"),
                    block.getNumber()
            );

            if (!pl.getDoc().containsDXFLayer(terraceLayerName))
                continue;

            List<DXFLWPolyline> terracePolyline =
                    Util.getPolyLinesByLayer(pl.getDoc(), terraceLayerName);

            if (terracePolyline == null || terracePolyline.isEmpty())
                continue;

            for (DXFLWPolyline pline : terracePolyline) {

                Measurement measurement = new MeasurementDetail(pline, true);

                Terrace terrace = block.getTerrace();
                if (terrace == null) {
                    terrace = new Terrace();
                    block.setTerrace(terrace);
                }

                terrace.setArea(measurement.getArea());
                terrace.setHeight(measurement.getHeight());
                terrace.setWidth(measurement.getWidth());
                terrace.setLength(measurement.getLength());
                terrace.setInvalidReason(measurement.getInvalidReason());
                terrace.setPresentInDxf(true);
            }
        }
    }

    /**
     * Extracts the staircase headroom height for each block by reading
     * the configured headroom layer ("LAYER_NAME_TERRACE_STAIRCASE_HEADROOM").
     * <p>
     * This method reads all height dimensions represented on the headroom layer
     * using color-code based extraction. Only height values are extracted; no
     * geometric area or width calculations are performed here.
     * <p>
     * If a {@link Terrace} object does not already exist for a block, one is
     * created and attached. The extracted headroom height list is stored in
     * {@code Terrace.setStaircaseHeadroomHt()}.
     * </p>
     *
     * @param pl The plan detail containing block and DXF document information.
     */
    private void extractStaircaseHeadroom(PlanDetail pl) {

        for (Block block : pl.getBlocks()) {

            String headroomLayer = String.format(
                    layerNames.getLayerName("LAYER_NAME_TERRACE_STAIRCASE_HEADROOM"),
                    block.getNumber()
            );

            if (!pl.getDoc().containsDXFLayer(headroomLayer))
                continue;

            List<BigDecimal> headroomHeights =
                    Util.getListOfDimensionByColourCode(
                            pl, headroomLayer, DxfFileConstants.INDEX_COLOR_ONE
                    );

            if (headroomHeights == null || headroomHeights.isEmpty())
                continue;

            Terrace terrace = block.getTerrace();
            if (terrace == null) {
                terrace = new Terrace();
                block.setTerrace(terrace);
            }

            terrace.setStaircaseHeadroomHt(headroomHeights);
            terrace.setPresentInDxf(true);
        }
    }
}
