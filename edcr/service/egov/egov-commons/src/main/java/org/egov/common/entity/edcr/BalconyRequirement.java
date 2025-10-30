package org.egov.common.entity.edcr;

import java.math.BigDecimal;

public class BalconyRequirement extends MdmsFeatureRule {
	
	private BigDecimal maxBalconyLength;
	private BigDecimal maxBalconyWidth;
	private BigDecimal minSetbackFromPlotBoundary;
	
	public BigDecimal getMaxBalconyLength() {
		return maxBalconyLength;
	}
	public void setMaxBalconyLength(BigDecimal maxBalconyLength) {
		this.maxBalconyLength = maxBalconyLength;
	}
	public BigDecimal getMaxBalconyWidth() {
		return maxBalconyWidth;
	}
	public void setMaxBalconyWidth(BigDecimal maxBalconyWidth) {
		this.maxBalconyWidth = maxBalconyWidth;
	}
	public BigDecimal getMinSetbackFromPlotBoundary() {
		return minSetbackFromPlotBoundary;
	}
	public void setMinSetbackFromPlotBoundary(BigDecimal minSetbackFromPlotBoundary) {
		this.minSetbackFromPlotBoundary = minSetbackFromPlotBoundary;
	}
	@Override
	public String toString() {
		return "BalconyRequirement [maxBalconyLength=" + maxBalconyLength + ", maxBalconyWidth=" + maxBalconyWidth
				+ ", minSetbackFromPlotBoundary=" + minSetbackFromPlotBoundary + "]";
	}

	
}

