package org.egov.edcr.feature;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.egov.common.entity.edcr.*;
import org.egov.edcr.entity.blackbox.MeasurementDetail;
import org.egov.edcr.entity.blackbox.PlanDetail;
import org.egov.edcr.utility.Util;
import org.kabeja.dxf.DXFLWPolyline;
import org.springframework.stereotype.Service;

@Service
public class BalconyExtract extends FeatureExtract {
    private static final Logger LOG = LogManager.getLogger(BalconyExtract.class);

    @Override
    public PlanDetail validate(PlanDetail planDetail) {
        return planDetail;
    }

    @Override
    public PlanDetail extract(PlanDetail planDetail) {
        LOG.info("Starting of BalconyExtract extract method");

        if (planDetail != null && !planDetail.getBlocks().isEmpty()) {
            for (Block block : planDetail.getBlocks()) {
                if (block.getBuilding() != null && !block.getBuilding().getFloors().isEmpty()) {

                    outside: for (Floor floor : block.getBuilding().getFloors()) {

                        // Handle Typical Floor
                        if (!block.getTypicalFloor().isEmpty()) {
                            for (TypicalFloor tp : block.getTypicalFloor()) {
                                if (tp.getRepetitiveFloorNos().contains(floor.getNumber())) {
                                    for (Floor allFloors : block.getBuilding().getFloors()) {
                                        if (allFloors.getNumber().equals(tp.getModelFloorNo())) {

                                            // Copy balconies from model floor
                                            for (FloorUnit modelUnit : allFloors.getUnits()) {
                                                for (FloorUnit targetUnit : floor.getUnits()) {
                                                    if (modelUnit.getUnitNumber().equals(targetUnit.getUnitNumber())) {
                                                        targetUnit.setBalconies(modelUnit.getBalconies());
                                                    }
                                                }
                                            }

                                            LOG.info("Copied balconies from model floor {} to repetitive floor {} in block {}",
                                                    tp.getModelFloorNo(), floor.getNumber(), block.getNumber());
                                            continue outside;
                                        }
                                    }
                                }
                            }
                        }

                        if (floor.getUnits() != null && !floor.getUnits().isEmpty()) {
                            for (FloorUnit floorUnit : floor.getUnits()) {
                                List<Balcony> balconies = new ArrayList<>();

                                LOG.info("Processing Balcony for Block: {} Floor: {} Unit: {}",
                                        block.getNumber(), floor.getNumber(), floorUnit.getUnitNumber());

                                String balconyLayerPattern = "BLK_" + block.getNumber() + "_FLR_" + floor.getNumber() + "_UNIT_" + floorUnit.getUnitNumber() + "_BALCONY_" + "\\d{1,2}";
                                List<String> balconyLayers = Util.getLayerNamesLike(planDetail.getDoc(), balconyLayerPattern);

                                for (String balconyLayer : balconyLayers) {
                                    List<DXFLWPolyline> balconyPolyLines = Util.getPolyLinesByLayer(planDetail.getDoc(), balconyLayer);
                                    List<BigDecimal> dimensions = Util.getListOfDimensionValueByLayer(planDetail, balconyLayer);
                                    String[] split = balconyLayer.split("_");
                                    String balconyNo = split[5];

                                    if (!dimensions.isEmpty() || !balconyPolyLines.isEmpty()) {
                                        Balcony balcony = new Balcony();
                                        List<Measurement> balconyMeasurements = balconyPolyLines.stream()
                                                .map(balconyPolyLine -> new MeasurementDetail(balconyPolyLine, true))
                                                .collect(Collectors.toList());

                                        balcony.setMeasurements(balconyMeasurements);
                                        balcony.setWidths(dimensions);
                                        balcony.setNumber(balconyNo);
                                        balconies.add(balcony);
                                    }
                                }

                                floorUnit.setBalconies(balconies);
                                LOG.info("Total balconies found for block {} floor {} unit {} : {}",
                                        block.getNumber(), floor.getNumber(), floorUnit.getUnitNumber(), balconies.size());
                            }
                        }
                    }
                }
            }
        }

        LOG.info("End of BalconyExtract extract method");
        return planDetail;
    }
}	
