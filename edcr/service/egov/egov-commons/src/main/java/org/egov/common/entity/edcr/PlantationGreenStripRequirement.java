package org.egov.common.entity.edcr;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PlantationGreenStripRequirement extends MdmsFeatureRule {
	
	    @JsonProperty("plantationGreenStripPlanValue")
	    private BigDecimal plantationGreenStripPlanValue;
	    @JsonProperty("minPercentage")
	    private BigDecimal minPercentage;
	    @JsonProperty("maxPercentage")
	    private BigDecimal maxPercentage;
	    @JsonProperty("plantationGreenStripMinWidth")
	    private BigDecimal plantationGreenStripMinWidth;
		public BigDecimal getPlantationGreenStripPlanValue() {
			return plantationGreenStripPlanValue;
		}
		public void setPlantationGreenStripPlanValue(BigDecimal plantationGreenStripPlanValue) {
			this.plantationGreenStripPlanValue = plantationGreenStripPlanValue;
		}
		public BigDecimal getPlantationGreenStripMinWidth() {
			return plantationGreenStripMinWidth;
		}
		public void setPlantationGreenStripMinWidth(BigDecimal plantationGreenStripMinWidth) {
			this.plantationGreenStripMinWidth = plantationGreenStripMinWidth;
		}
		public BigDecimal getMinPercentage() {
			return minPercentage;
		}
		public void setMinPercentage(BigDecimal minPercentage) {
			this.minPercentage = minPercentage;
		}
		public BigDecimal getMaxPercentage() {
			return maxPercentage;
		}
		public void setMaxPercentage(BigDecimal maxPercentage) {
			this.maxPercentage = maxPercentage;
		}
		@Override
		public String toString() {
			return "PlantationGreenStripRequirement [plantationGreenStripPlanValue=" + plantationGreenStripPlanValue
					+ ", minPercentage=" + minPercentage + ", maxPercentage=" + maxPercentage
					+ ", plantationGreenStripMinWidth=" + plantationGreenStripMinWidth + "]";
		}

}
