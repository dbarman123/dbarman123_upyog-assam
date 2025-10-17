package org.egov.common.entity.edcr;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WindowsRequirement extends MdmsFeatureRule {
	
	    @JsonProperty("minWindowWidth")
	    private  BigDecimal minWindowWidth;
	    
	    @JsonProperty("minWindowHeight")
	    private  BigDecimal minWindowHeight;

		public BigDecimal getMinWindowWidth() {
			return minWindowWidth;
		}

		public void setMinWindowWidth(BigDecimal minWindowWidth) {
			this.minWindowWidth = minWindowWidth;
		}

		public BigDecimal getMinWindowHeight() {
			return minWindowHeight;
		}

		public void setMinWindowHeight(BigDecimal minWindowHeight) {
			this.minWindowHeight = minWindowHeight;
		}

		@Override
		public String toString() {
			return "WindowsRequirement [minWindowWidth=" + minWindowWidth + ", minWindowHeight=" + minWindowHeight
					+ "]";
		}

}
