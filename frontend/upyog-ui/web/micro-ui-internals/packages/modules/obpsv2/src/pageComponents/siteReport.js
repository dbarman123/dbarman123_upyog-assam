import React, { useState, useEffect } from "react";
import {
  TextInput,
  CardLabel,
  CardSectionHeader,
  DatePicker,
  TextArea
} from "@upyog/digit-ui-react-components";
import { useTranslation } from "react-i18next";
import DocumentsPreview from "../../../templates/ApplicationDetails/components/DocumentsPreview";

const siteReport = ({submitReport, onChange, data}) => {
  // Extract BPA data from FormComposer
  const bpaData = data?.bpaData;
  const { t } = useTranslation();
  const [form, setForm] = useState({
    proposalNo: "",
    submittedOn: "",
    applicantName: "",
    applicantAddress: "",
    architectName: "",
    inspectorName: "",
    revenueVillage: "",
    pattaNo: "",
    dagNo: "",
    plotArea: "",
    proposedUse: "",
    masterPlanZone: "",
    inspectionDate: "",
    mouza: "",
 
    topographyOfLand: "",
    topographyOfLandRemarks: "",
    earthFillingRequired: "",
    earthFillingRequiredRemarks: "",
    provisionOfExistingRoadSideDrain: "",
    provisionOfExistingRoadSideDrainRemarks: "",
    provisionOfParkingForHighRiseBuilding: "",
    provisionOfParkingForHighRiseBuildingRemarks: "",
    roadWidthInFrontPlot: "",
    roadWidthInFrontPlotRemarks: "",
    roadWidthNearestPlot: "",
    roadWidthNearestPlotRemarks: "",
    roadWidthNarrowestPlot: "",
    roadWidthNarrowestPlotRemarks: "",
    totalAverageRoadWidth: "",
    totalAverageRoadWidthRemarks: "",
    proposedRoadWidth: "",
    proposedRoadWidthRemarks: "",
    descriptionOfAnyOtherRoad: "",
    descriptionOfAnyOtherRoadRemarks: "",
    existingNatureOfApproachRoad: "",
    existingNatureOfApproachRoadRemarks: "",
    approximateLengthDeadEndRoad: "",
    approximateLengthDeadEndRoadRemarks: "",
    roadCondition: "",
    roadConditionRemarks: "",
    proposedUseConformityWithMasterPlan: "",
    proposedUseConformityWithMasterPlanRemarks: "",
    anyWaterBodyExistsInPlot: "",
    anyWaterBodyExistsInPlotRemarks: "",
    distanceOfPlotFromNearestWaterBody: "",
    distanceOfPlotFromNearestWaterBodyRemarks: "",
    areaOfPlotMeasured: "",
    areaOfPlotMeasuredRemarks: "",
    north: "",
    northRemarks: "",
    south: "",
    southRemarks: "",
    east: "",
    eastRemarks: "",
    west: "",
    westRemarks: "",
    commentsOnProposal: "",
    commentsOnProposalRemarks: ""
  });

  // Autofill form with BPA data
  useEffect(() => {
    if (bpaData?.applicationData) {
      const appData = bpaData.applicationData;
      const landInfo = appData?.landInfo || {};
      const owners = landInfo?.owners || [];
      const primaryOwner = owners[0] || {};
      const address = landInfo?.address || {};
      const areaMapping = appData?.areaMapping || {};
      const adjoiningOwners = appData?.additionalDetails?.adjoiningOwners || {};
      const rtpDetails = appData?.rtpDetails || {};
      const architectName = rtpDetails?.rtpName ? rtpDetails.rtpName.split(',')[0] : '';
      
      setForm(prev => ({
        ...prev,
        proposalNo: t(appData?.applicationNo) || t(prev.proposalNo),
        applicantName: t(primaryOwner?.name) || t(prev.applicantName),
        applicantAddress: `${address?.houseNo || ''} ${address?.addressLine1 || ''} ${address?.addressLine2 || ''}`.trim() || prev.applicantAddress,
        architectName: t(architectName) || t(prev.architectName),
        masterPlanZone: t(areaMapping?.planningArea) || t(prev.masterPlanZone),
        revenueVillage: t(areaMapping?.revenueVillage) || t(prev.revenueVillage),
        pattaNo: t(landInfo?.newPattaNumber) || t(landInfo?.oldPattaNumber) || t(prev.pattaNo),
        dagNo: t(landInfo?.newDagNumber) || t(landInfo?.oldDagNumber) || t(prev.dagNo),
        plotArea: t(landInfo?.totalPlotArea) || t(prev.plotArea),
        proposedUse: t(landInfo?.units?.[0]?.occupancyType) || t(prev.proposedUse),
        mouza: t(areaMapping?.mouza) || t(prev.mouza),
        north: t(adjoiningOwners?.north) || t(prev.north),
        south: t(adjoiningOwners?.south) || t(prev.south),
        east: t(adjoiningOwners?.east) || t(prev.east),
        west: t(adjoiningOwners?.west) || t(prev.west)
      }));
    }
  }, [bpaData]);

  const handleChange = (key, value) => {
    const updated = { ...form, [key]: value };
    setForm(updated);
    
        sessionStorage.setItem("SUBMIT_REPORT_DATA", JSON.stringify([updated]));
  };
  

  const fieldRowStyle = {
    display: "flex",
    alignItems: "flex-start",
    marginBottom: "12px",
    gap: "150px",
  };

//   const labelStyle = {
//     flex: "0 0 250px", // fixed width for all labels
//     fontWeight: 500,
//     textAlign: "left",
//     marginTop: "8px",
//   };

  const inputStyle = {
    flex: "1",
  };

  const labelStyle = {
    flex: "0 0 250px", 
    fontWeight: 500,
    textAlign: "left",
    whiteSpace: "normal", 
    wordBreak: "break-word",
    lineHeight: "1.4", 
  };
  
  const renderFieldWithRemarks = (labelKey, fieldKey) => {
    const isAutoFilled = ['north', 'south', 'east', 'west'].includes(fieldKey) && !!form[fieldKey];
    
    return (
      <div style={{ display: "grid", gridTemplateColumns: "250px 1fr", rowGap: "8px", columnGap: "150px", marginBottom: "16px" }}>
        <CardLabel style={{
          fontWeight: 500,
          textAlign: "left",
          whiteSpace: "nowrap",
          lineHeight: "1.4",
          alignSelf: "start"
        }}>
          {t(labelKey)}
        </CardLabel>
    
        <div style={{ display: "flex", flexDirection: "column", gap: "8px" }}>
          <TextInput
            style={{ width: "100%" }}
            value={form[fieldKey]}
            onChange={(e) => handleChange(fieldKey, e.target.value)}
            disable={isAutoFilled}
          />
          <TextArea
            style={{ width: "100%" }}
            value={form[`${fieldKey}Remarks`]}
            onChange={(e) => handleChange(`${fieldKey}Remarks`, e.target.value)}
            maxLength={500}
            placeholder={t("REMARKS")}
            rows={3}
          />
        </div>
      </div>
    );
  };
  
  return (
    <React.Fragment>
      <div style={{ marginBottom: "16px", maxWidth: "950px" }}>
        <div className="fieldInspectionWrapper">
          <CardSectionHeader>{t("BPA_SITE_INSPECTION_REPORT")}</CardSectionHeader>

          <div style={fieldRowStyle}>
            <CardLabel style={labelStyle}>{t("BPA_PROPOSAL_APPLICATION_NO")}</CardLabel>
            <TextInput
              style={inputStyle}
              value={form.proposalNo}
              onChange={(e) => handleChange("proposalNo", e.target.value)}
              disable={!!form.proposalNo}
            />
          </div>

          <div style={fieldRowStyle}>
            <CardLabel style={labelStyle}>{t("BPA_SUBMITTED_ON")}</CardLabel>
            <DatePicker
              style={inputStyle}
              date={form.submittedOn}
              onChange={(d) => handleChange("submittedOn", d)}
            />
          </div>

          <div style={fieldRowStyle}>
            <CardLabel style={labelStyle}>{t("BPA_APPLICANT_NAME")}</CardLabel>
            <TextInput
              style={inputStyle}
              value={form.applicantName}
              onChange={(e) => handleChange("applicantName", e.target.value)}
              disable={!!form.applicantName}
            />
          </div>

          <div style={fieldRowStyle}>
            <CardLabel style={labelStyle}>{t("BPA_APPLICANT_ADDRESS")}</CardLabel>
            <TextInput
              style={inputStyle}
              value={form.applicantAddress}
              onChange={(e) => handleChange("applicantAddress", e.target.value)}
              disable={!!form.applicantAddress}
            />
          </div>

          <div style={fieldRowStyle}>
            <CardLabel style={labelStyle}>{t("BPA_ARCHITECT_NAME")}</CardLabel>
            <TextInput
              style={inputStyle}
              value={form.architectName}
              onChange={(e) => handleChange("architectName", e.target.value)}
              disable={!!form.architectName}
            />
          </div>

          <div style={fieldRowStyle}>
            <CardLabel style={labelStyle}>{t("BPA_INSPECTOR_NAME")}</CardLabel>
            <TextInput
              style={inputStyle}
              value={form.inspectorName}
              onChange={(e) => handleChange("inspectorName", e.target.value)}
            />
          </div>

          <CardSectionHeader style={{ marginTop: "20px" }}>
            {t("BPA_LOCATION_OF_LAND")}
          </CardSectionHeader>

          <div style={fieldRowStyle}>
            <CardLabel style={labelStyle}>{t("BPA_REVENUE_VILLAGE")}</CardLabel>
            <TextInput
              style={inputStyle}
              value={form.revenueVillage}
              onChange={(e) => handleChange("revenueVillage", e.target.value)}
              disable={!!form.revenueVillage}
            />
          </div>

          <div style={fieldRowStyle}>
            <CardLabel style={labelStyle}>{t("BPA_PATTA_NO")}</CardLabel>
            <TextInput
              style={inputStyle}
              value={form.pattaNo}
              onChange={(e) => handleChange("pattaNo", e.target.value)}
              disable={!!form.pattaNo}
            />
          </div>

          <div style={fieldRowStyle}>
            <CardLabel style={labelStyle}>{t("BPA_DAG_NO")}</CardLabel>
            <TextInput
              style={inputStyle}
              value={form.dagNo}
              onChange={(e) => handleChange("dagNo", e.target.value)}
              disable={!!form.dagNo}
            />
          </div>

          <div style={fieldRowStyle}>
            <CardLabel style={labelStyle}>{t("BPA_PLOT_AREA")}</CardLabel>
            <TextInput
              style={inputStyle}
              value={form.plotArea}
              onChange={(e) => handleChange("plotArea", e.target.value)}
              disable={!!form.plotArea}
            />
          </div>

          <div style={fieldRowStyle}>
            <CardLabel style={labelStyle}>{t("BPA_PROPOSED_USE_OF_BUILDING")}</CardLabel>
            <TextInput
              style={inputStyle}
              value={form.proposedUse}
              onChange={(e) => handleChange("proposedUse", e.target.value)}
              disable={!!form.proposedUse}
            />
          </div>

          <div style={fieldRowStyle}>
            <CardLabel style={labelStyle}>{t("BPA_MASTER_PLAN_ZONE")}</CardLabel>
            <TextInput
              style={inputStyle}
              value={form.masterPlanZone}
              onChange={(e) => handleChange("masterPlanZone", e.target.value)}
              disable={!!form.masterPlanZone}
            />
          </div>

          <div style={fieldRowStyle}>
            <CardLabel style={labelStyle}>{t("BPA_SITE_INSPECTION_DATE")}</CardLabel>
            <DatePicker
              style={inputStyle}
              date={form.inspectionDate}
              onChange={(d) => handleChange("inspectionDate", d)}
            />
          </div>

          <div style={fieldRowStyle}>
            <CardLabel style={labelStyle}>{t("BPA_MOUZA")}</CardLabel>
            <TextInput
              style={inputStyle}
              value={form.mouza}
              onChange={(e) => handleChange("mouza", e.target.value)}
              disable={!!form.mouza}
            />
          </div>

         
          <CardSectionHeader style={{ marginTop: "20px" }}>
            {t("BPA_SITE_CHECKLIST")}
          </CardSectionHeader>

          {renderFieldWithRemarks("TOPOGRAPHY_OF_LAND", "topographyOfLand")}
          {renderFieldWithRemarks("EARTH_FILLING_REQUIRED", "earthFillingRequired")}
          {renderFieldWithRemarks("BPA_PROVISION_OF_EXISTING_ROAD_SIDE_DRAIN", "provisionOfExistingRoadSideDrain")}
          {renderFieldWithRemarks("PROVISION_OF_PARKING_FOR_HIGH_RISE_BUILDING", "provisionOfParkingForHighRiseBuilding")}
          {renderFieldWithRemarks("ROAD_WIDTH_INFRONT_PLOT", "roadWidthInFrontPlot")}
          {renderFieldWithRemarks("ROAD_WIDTH_NEAREST_PLOT", "roadWidthNearestPlot")}
          {renderFieldWithRemarks("ROAD_WIDTH_NARROWEST_PLOT", "roadWidthNarrowestPlot")}
          {renderFieldWithRemarks("TOTAL_AVERAGE_ROAD_WIDTH", "totalAverageRoadWidth")}
          {renderFieldWithRemarks("PROPOSED_ROAD_WIDTH", "proposedRoadWidth")}
          {renderFieldWithRemarks("DESCRIPTION_OF_ANY_OTHER_ROAD", "descriptionOfAnyOtherRoad")}
          {renderFieldWithRemarks("EXISTING_NATURE_OF_APPROACH_ROAD", "existingNatureOfApproachRoad")}
          {renderFieldWithRemarks("APPROXIMATE_LENGTH_DEAD_END_ROAD", "approximateLengthDeadEndRoad")}
          {renderFieldWithRemarks("ROAD_CONDITION", "roadCondition")}
          {renderFieldWithRemarks("WHETHER_THE_PROPOSED_USE_IS_IN_CONFORMITY_WITH_MASTER_PLAN", "proposedUseConformityWithMasterPlan")}
          {renderFieldWithRemarks("WHETHER_ANY_WATER_BODY_EXISTS_IN_PLOT", "anyWaterBodyExistsInPlot")}
          {renderFieldWithRemarks("DISTANCE_OF_PLOT_FROM_NEAREST_WATER_BODY", "distanceOfPlotFromNearestWaterBody")}
          {renderFieldWithRemarks("AREA_OF_PLOT_MEASURED", "areaOfPlotMeasured")}
          {renderFieldWithRemarks("NORTH", "north")}
          {renderFieldWithRemarks("SOUTH", "south")}
          {renderFieldWithRemarks("EAST", "east")}
          {renderFieldWithRemarks("WEST", "west")}
          {renderFieldWithRemarks("COMMENTS_ON_PROPOSAL", "commentsOnProposal")}

        </div>
      </div>
    </React.Fragment>
  );
}

export default siteReport;
