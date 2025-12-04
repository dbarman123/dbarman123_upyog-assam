package org.egov.common.entity.edcr;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FarRequirement extends MdmsFeatureRule {

    @JsonProperty("permissibleLight")
    private BigDecimal permissibleLight;

    @JsonProperty("permissibleMedium")
    private BigDecimal permissibleMedium;

    @JsonProperty("permissibleFlattered")
    private BigDecimal permissibleFlattered;

    @JsonProperty("maxTDRLoading")
    private BigDecimal maxTDRLoading;

    @JsonProperty("farGuardRoomArea")
    private BigDecimal farGuardRoomArea;

    @JsonProperty("farCareTakerRoomArea")
    private BigDecimal farCareTakerRoomArea;

    @JsonProperty("farCanopyLength")
    private BigDecimal farCanopyLength;

    @JsonProperty("farCanopyWidth")
    private BigDecimal farCanopyWidth;

    @JsonProperty("farCanopyHeight")
    private BigDecimal farCanopyHeight;

    @JsonProperty("farBalconyWidth")
    private BigDecimal farBalconyWidth;

    @JsonProperty("farBalconySetback")
    private BigDecimal farBalconySetback;

    @JsonProperty("farBalconyLength")
    private BigDecimal farBalconyLength;

    @JsonProperty("farEntranceLobbyArea")
    private BigDecimal farEntranceLobbyArea;

    @JsonProperty("farMaxBalconyExemption")
    private BigDecimal farMaxBalconyExemption;

    @JsonProperty("farCorridorArea")
    private BigDecimal farCorridorArea;

    @JsonProperty("farProjectionWidth")
    private BigDecimal farProjectionWidth;

    @JsonProperty("farProjectionLength")
    private BigDecimal farProjectionLength;

    @JsonProperty("farPermittedRoomAreaPercentage")
    private BigDecimal farPermittedRoomAreaPercentage;

    public BigDecimal getBaseFar() {
        return baseFar;
    }

    public void setBaseFar(BigDecimal baseFar) {
        this.baseFar = baseFar;
    }

    @JsonProperty("baseFar")
    private BigDecimal baseFar;

    public BigDecimal getMaxTDRLoading() {
        return maxTDRLoading;
    }

    public void setMaxTDRLoading(BigDecimal maxTDRLoading) {
        this.maxTDRLoading = maxTDRLoading;
    }

    public BigDecimal getPermissibleLight() {
        return permissibleLight;
    }

    public void setPermissibleLight(BigDecimal permissibleLight) {
        this.permissibleLight = permissibleLight;
    }

    public BigDecimal getPermissibleMedium() {
        return permissibleMedium;
    }

    public void setPermissibleMedium(BigDecimal permissibleMedium) {
        this.permissibleMedium = permissibleMedium;
    }

    public BigDecimal getPermissibleFlattered() {
        return permissibleFlattered;
    }

    public void setPermissibleFlattered(BigDecimal permissibleFlattered) {
        this.permissibleFlattered = permissibleFlattered;
    }

    public BigDecimal getFarGuardRoomArea() {
        return farGuardRoomArea;
    }

    public void setFarGuardRoomArea(BigDecimal farGuardRoomArea) {
        this.farGuardRoomArea = farGuardRoomArea;
    }

    public BigDecimal getFarCareTakerRoomArea() {
        return farCareTakerRoomArea;
    }

    public void setFarCareTakerRoomArea(BigDecimal farCareTakerRoomArea) {
        this.farCareTakerRoomArea = farCareTakerRoomArea;
    }

    public BigDecimal getFarCanopyLength() {
        return farCanopyLength;
    }

    public void setFarCanopyLength(BigDecimal farCanopyLength) {
        this.farCanopyLength = farCanopyLength;
    }

    public BigDecimal getFarCanopyWidth() {
        return farCanopyWidth;
    }

    public void setFarCanopyWidth(BigDecimal farCanopyWidth) {
        this.farCanopyWidth = farCanopyWidth;
    }

    public BigDecimal getFarCanopyHeight() {
        return farCanopyHeight;
    }

    public void setFarCanopyHeight(BigDecimal farCanopyHeight) {
        this.farCanopyHeight = farCanopyHeight;
    }

    public BigDecimal getFarBalconyWidth() {
        return farBalconyWidth;
    }

    public void setFarBalconyWidth(BigDecimal farBalconyWidth) {
        this.farBalconyWidth = farBalconyWidth;
    }

    public BigDecimal getFarBalconySetback() {
        return farBalconySetback;
    }

    public void setFarBalconySetback(BigDecimal farBalconySetback) {
        this.farBalconySetback = farBalconySetback;
    }

    public BigDecimal getFarBalconyLength() {
        return farBalconyLength;
    }

    public void setFarBalconyLength(BigDecimal farBalconyLength) {
        this.farBalconyLength = farBalconyLength;
    }

    public BigDecimal getFarEntranceLobbyArea() {
        return farEntranceLobbyArea;
    }

    public void setFarEntranceLobbyArea(BigDecimal farEntranceLobbyArea) {
        this.farEntranceLobbyArea = farEntranceLobbyArea;
    }

    public BigDecimal getFarMaxBalconyExemption() {
        return farMaxBalconyExemption;
    }

    public void setFarMaxBalconyExemption(BigDecimal farMaxBalconyExemption) {
        this.farMaxBalconyExemption = farMaxBalconyExemption;
    }

    public BigDecimal getFarCorridorArea() {
        return farCorridorArea;
    }

    public void setFarCorridorArea(BigDecimal farCorridorArea) {
        this.farCorridorArea = farCorridorArea;
    }

    public BigDecimal getFarProjectionWidth() {
        return farProjectionWidth;
    }

    public void setFarProjectionWidth(BigDecimal farProjectionWidth) {
        this.farProjectionWidth = farProjectionWidth;
    }

    public BigDecimal getFarProjectionLength() {
        return farProjectionLength;
    }

    public void setFarProjectionLength(BigDecimal farProjectionLength) {
        this.farProjectionLength = farProjectionLength;
    }

    public BigDecimal getFarPermittedRoomAreaPercentage() {
        return farPermittedRoomAreaPercentage;
    }

    public void setFarPermittedRoomAreaPercentage(BigDecimal farPermittedRoomAreaPercentage) {
        this.farPermittedRoomAreaPercentage = farPermittedRoomAreaPercentage;
    }

    @Override
	public String toString() {
		return "FarRequirement [permissibleLight=" + permissibleLight + ", permissibleMedium=" + permissibleMedium
				+ ", permissibleFlattered=" + permissibleFlattered + ", maxTDRLoading=" + maxTDRLoading
				+ ", farGuardRoomArea=" + farGuardRoomArea + ", farCareTakerRoomArea=" + farCareTakerRoomArea
				+ ", farCanopyLength=" + farCanopyLength + ", farCanopyWidth=" + farCanopyWidth + ", farCanopyHeight="
				+ farCanopyHeight + ", farBalconyWidth=" + farBalconyWidth + ", farBalconySetback=" + farBalconySetback
				+ ", farBalconyLength=" + farBalconyLength + ", farEntranceLobbyArea=" + farEntranceLobbyArea
				+ ", farMaxBalconyExemption=" + farMaxBalconyExemption + ", farCorridorArea=" + farCorridorArea
				+ ", farProjectionWidth=" + farProjectionWidth + ", farProjectionLength=" + farProjectionLength
				+ ", farPermittedRoomAreaPercentage=" + farPermittedRoomAreaPercentage + ", baseFar=" + baseFar
				+ ", getFromBuildingHeight()=" + getFromBuildingHeight() + ", getToBuildingHeight()="
				+ getToBuildingHeight() + ", getFromPlotDepth()=" + getFromPlotDepth() + ", getToPlotDepth()="
				+ getToPlotDepth() + ", getSubOccupancy()=" + getSubOccupancy() + ", getId()=" + getId()
				+ ", getFromPlotArea()=" + getFromPlotArea() + ", getFromRoadWidth()=" + getFromRoadWidth()
				+ ", getToRoadWidth()=" + getToRoadWidth() + ", getToPlotArea()=" + getToPlotArea() + ", getState()="
				+ getState() + ", getCity()=" + getCity() + ", getZone()=" + getZone() + ", getSubZone()="
				+ getSubZone() + ", getOccupancy()=" + getOccupancy() + ", getRiskType()=" + getRiskType()
				+ ", getPermissible()=" + getPermissible() + ", getFeatureName()=" + getFeatureName()
				+ ", getValuePermissible()=" + getValuePermissible() + ", getActive()=" + getActive() + ", toString()="
				+ super.toString() + ", hashCode()=" + hashCode() + ", getClass()=" + getClass() + "]";
	}
}
