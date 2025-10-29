package org.egov.edcr.feature;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.egov.common.entity.edcr.BiodegradableWasteTreatment;
import org.egov.common.entity.edcr.Measurement;
import org.egov.edcr.entity.blackbox.MeasurementDetail;
import org.egov.edcr.entity.blackbox.PlanDetail;
import org.egov.edcr.service.LayerNames;
import org.egov.edcr.utility.Util;
import org.kabeja.dxf.DXFLWPolyline;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BiodegradableWasteExtract extends FeatureExtract {

    private static final Logger LOG = LogManager.getLogger(BiodegradableWasteExtract.class);
    @Autowired
    private LayerNames layerNames;

    @Override
    public PlanDetail extract(PlanDetail pl) {
        if (LOG.isInfoEnabled())
            LOG.info("Starting Biodegradable Waste Extract......");
        // biodegrdable waste treatment
        if (pl != null && pl.getDoc().containsDXFLayer(layerNames.getLayerName("LAYER_NAME_BIODEGRADABLE_WASTE"))) {
            List<DXFLWPolyline> biodegradableWastePolyLines = Util.getPolyLinesByLayer(pl.getDoc(),
                    layerNames.getLayerName("LAYER_NAME_BIODEGRADABLE_WASTE"));
            if (biodegradableWastePolyLines != null && !biodegradableWastePolyLines.isEmpty())
                for (DXFLWPolyline polyLine : biodegradableWastePolyLines) {
                    Measurement measurement = new MeasurementDetail(polyLine, true);
                    BiodegradableWasteTreatment biodegradableWaste = new BiodegradableWasteTreatment();
                    biodegradableWaste.setArea(measurement.getArea());
                    biodegradableWaste.setColorCode(measurement.getColorCode());
                    biodegradableWaste.setHeight(measurement.getHeight());
                    biodegradableWaste.setWidth(measurement.getWidth());
                    biodegradableWaste.setLength(measurement.getLength());
                    biodegradableWaste.setInvalidReason(measurement.getInvalidReason());
                    biodegradableWaste.setPresentInDxf(true);
                    pl.getUtility().addBiodegradableWasteTreatment(biodegradableWaste);
                }
        }
        if (LOG.isInfoEnabled())
            LOG.info("End of biodegradable Waste Management Extract......");
        return pl;
    }

    @Override
    public PlanDetail validate(PlanDetail pl) {
        return pl;
    }

}
