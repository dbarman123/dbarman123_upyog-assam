package org.egov.common.entity.edcr;

import java.math.BigDecimal;
import java.util.List;

public class PercolationPit extends Measurement {
    private static final long serialVersionUID = 49L;

    private List<BigDecimal> pitLength;
    private List<BigDecimal> pitWidth;
    private BigDecimal pitHeight;

	public List<BigDecimal> getPitLength() {
		return pitLength;
	}
	public void setPitLength(List<BigDecimal> pitLength) {
		this.pitLength = pitLength;
	}
	public List<BigDecimal> getPitWidth() {
		return pitWidth;
	}
	public void setPitWidth(List<BigDecimal> pitWidth) {
		this.pitWidth = pitWidth;
	}
	public BigDecimal getPitHeight() {
		return pitHeight;
	}
	public void setPitHeight(BigDecimal pitHeight) {
		this.pitHeight = pitHeight;
	}
	public BigDecimal getHeight() { return height; }
    public void setHeight(BigDecimal height) { this.height = height; }
}
