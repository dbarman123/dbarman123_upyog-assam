import React, { useEffect, useState } from "react";
import {
  FormStep,
  TextInput,
  CardLabel,
  Card,
  CardSubHeader,
} from "@upyog/digit-ui-react-components";
import { useLocation } from "react-router-dom";
import { useTranslation } from "react-i18next";
import Timeline from "../components/Timeline";

const Form22A = ({ config, onSelect, userType, formData, value = formData }) => {
  const { t } = useTranslation();
  const { pathname: url } = useLocation();
  const flow = window.location.href.includes("editApplication")
    ? "editApplication"
    : "buildingPermit";

  const [scrutinyDetails, setScrutinyDetails] = useState(null);

  // main state variables
  const [plotArea, setPlotArea] = useState(formData?.plotArea || formData?.form?.plotArea || "");
  const [existingPlinthArea, setExistingPlinthArea] = useState(formData?.existingPlinthArea || formData?.form?.existingPlinthArea || "");
  const [proposedPlinthArea, setProposedPlinthArea] = useState(formData?.proposedPlinthArea || formData?.form?.proposedPlinthArea || "");
  const [floorAreaCalculation, setFloorAreaCalculation] = useState(formData?.floorAreaCalculation || formData?.form?.floorAreaCalculation || []);
  const [mezzanineFloorArea, setMezzanineFloorArea] = useState(formData?.mezzanineFloorArea || formData?.form?.mezzanineFloorArea || "");
  const [deductionCalculation, setDeductionCalculation] = useState(formData?.deductionCalculation || formData?.form?.deductionCalculation || 0);
  const [totalFloorAreaAfterDeduction, setTotalFloorAreaAfterDeduction] = useState(formData?.totalFloorAreaAfterDeduction || formData?.form?.totalFloorAreaAfterDeduction || []);
  const [totalFloorAreaBeforeDeduction, setTotalFloorAreaBeforeDeduction] = useState(formData?.totalFloorAreaBeforeDeduction || formData?.form?.totalFloorAreaBeforeDeduction || []);
  const [coverage, setCoverage] = useState(formData?.coverage || formData?.form?.coverage || "");
  const [floorAreaRatio, setFloorAreaRatio] = useState(formData?.floorAreaRatio || formData?.form?.floorAreaRatio || "");

  // Helper for updating table data
  const handleTableChange = (setter, data, index, field, value) => {
    const updated = [...data];
    updated[index][field] = value;
    setter(updated);
  };

  const goNext = () => {
    const formStepData = {
      plotArea,
      existingPlinthArea,
      proposedPlinthArea,
      floorAreaCalculation,
      mezzanineFloorArea,
      deductionCalculation,
      totalFloorAreaAfterDeduction,
      totalFloorAreaBeforeDeduction,
      coverage,
      floorAreaRatio,
      scrutinyDetails,
    };

    if (userType === "citizen") {
      onSelect(config.key, { ...formData[config.key], ...formStepData });
    } else {
      onSelect(config.key, formStepData);
    }
  };

  const onSkip = () => onSelect();

  useEffect(() => {
    const tenantId = "assam";

    const fetchScrutinyDetails = async () => {
      try {
        const response = await Digit.OBPSService.scrutinyDetails(tenantId, {
          edcrNumber: "DCR102025WDMEL",
        });
        const scrutinyData = response?.edcrDetail?.[0];
        if (!scrutinyData) return;

        setScrutinyDetails(scrutinyData);

        const plan = scrutinyData?.planDetail || {};
        const block = plan?.blocks?.[0] || {};
        const building = block?.building || {};
        const floors = building?.floors || [];

        // create structured data arrays from floors
        const floorCalcData = floors.map((f, i) => ({
          floor: `Floor ${i + 1}`,
          builtUpArea: f?.occupancies?.[0]?.builtUpArea || "",
          existingFloorArea: f?.existingFloorArea || "",
        }));

        const beforeDeductionData = floors.map((f, i) => ({
          floor: `Floor ${i + 1}`,
          area: f?.occupancies?.[0]?.builtUpArea || "",
        }));

        const afterDeductionData = floors.map((f, i) => ({
          floor: `Floor ${i + 1}`,
          area: f?.occupancies?.[0]?.floorArea || "",
        }));

        // set state
        setPlotArea(plan?.planInformation?.plotArea || "");
        setExistingPlinthArea(plan?.existingPlinthArea || "");
        setProposedPlinthArea(block?.setBacks?.[0]?.buildingFootPrint?.area || "");
        setMezzanineFloorArea(plan?.mezzanineFloorArea || "");
        setDeductionCalculation((building?.totalArea?.[0]?.deduction+building?.totalArea?.[0]?.existingDeduction) || 0);
        setCoverage(plan?.coverage || "");
        setFloorAreaRatio(plan?.farDetails?.providedFar || "");
        setFloorAreaCalculation(floorCalcData);
        setTotalFloorAreaBeforeDeduction(beforeDeductionData);
        setTotalFloorAreaAfterDeduction(afterDeductionData);
      } catch (err) {
        console.error("Error fetching scrutiny details:", err);
      }
    };

    fetchScrutinyDetails();
  }, []);

  useEffect(() => {
    if (userType === "citizen") goNext();
  }, []); 

  // Render helper for per-floor tables
  const renderFloorCalcTable = () => (
    <div style={{ marginLeft: "20px" }}>
      <table style={{ width: "80%", borderCollapse: "collapse" }}>
        <thead>
          <tr>
            <th style={{ border: "1px solid #ddd", padding: "8px" }}>{t("FLOOR")}</th>
            <th style={{ border: "1px solid #ddd", padding: "8px" }}>{t("BUILTUP_AREA")}</th>
            <th style={{ border: "1px solid #ddd", padding: "8px" }}>{t("EXISTING_FLOOR_AREA")}</th>
          </tr>
        </thead>
        <tbody>
          {floorAreaCalculation.map((row, index) => (
            <tr key={index}>
              <td style={{ border: "1px solid #ddd", padding: "8px" }}>{row.floor}</td>
              <td style={{ border: "1px solid #ddd", padding: "8px" }}>
                <input
                  value={row.builtUpArea}
                  onChange={(e) =>
                    handleTableChange(setFloorAreaCalculation, floorAreaCalculation, index, "builtUpArea", e.target.value)
                  }
                  style={{ width: "100%" }}
                />
              </td>
              <td style={{ border: "1px solid #ddd", padding: "8px" }}>
                <input
                  value={row.existingFloorArea}
                  onChange={(e) =>
                    handleTableChange(setFloorAreaCalculation, floorAreaCalculation, index, "existingFloorArea", e.target.value)
                  }
                  style={{ width: "100%" }}
                />
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );

  const renderSimpleAreaTable = (data, setter) => (
    <div style={{ marginLeft: "20px" }}>
      <table style={{ width: "80%", borderCollapse: "collapse" }}>
        <thead>
          <tr>
            <th style={{ border: "1px solid #ddd", padding: "8px" }}>{t("FLOOR")}</th>
            <th style={{ border: "1px solid #ddd", padding: "8px" }}>{t("AREA")}</th>
          </tr>
        </thead>
        <tbody>
          {data.map((row, index) => (
            <tr key={index}>
              <td style={{ border: "1px solid #ddd", padding: "8px" }}>{row.floor}</td>
              <td style={{ border: "1px solid #ddd", padding: "8px" }}>
                <input
                  value={row.area}
                  onChange={(e) => handleTableChange(setter, data, index, "area", e.target.value)}
                  style={{ width: "100%" }}
                />
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );

  return (
    <React.Fragment>
      <Timeline currentStep={flow === "editApplication" ? 5 : null} flow={flow} />

      <Card>
        <CardSubHeader style={{ textAlign: "center" }}>
          <h2>{t("Form 22")}</h2>
          <div style={{ fontSize: "14px", color: "#555" }}>{t("(For all categories of buildings)")}</div>
        </CardSubHeader>
      </Card>

      <FormStep config={config} onSelect={goNext} onSkip={onSkip} t={t} isDisabled={false}>
        <CardLabel>A. {t("PLOT_AREA")}</CardLabel>
        <TextInput value={plotArea} onChange={(e) => setPlotArea(e.target.value)} />

        <CardLabel>B. {t("PLINTH_AREA")}</CardLabel>
        <CardLabel style={{ fontSize: "14px", marginLeft: "20px" }}>
          I. {t("EXISTING_PLINTH_AREA")}
        </CardLabel>
        <TextInput value={existingPlinthArea} onChange={(e) => setExistingPlinthArea(e.target.value)} />

        <CardLabel style={{ fontSize: "14px", marginLeft: "20px" }}>
          II. {t("PROPOSED_PLINTH_AREA")}
        </CardLabel>
        <TextInput value={proposedPlinthArea} onChange={(e) => setProposedPlinthArea(e.target.value)} />

        <CardLabel>C. {t("FLOOR_AREA_DETAIL_CALCULATION")}</CardLabel>
        {renderFloorCalcTable()}

        <CardLabel>D. {t("DETAIL_OF_MEZZANINE_FLOOR_AREA")}</CardLabel>
        <TextInput value={mezzanineFloorArea} onChange={(e) => setMezzanineFloorArea(e.target.value)} />

        <CardLabel>E. {t("DEDUCTION_SHOWING_DETAIL_CALCULATION")}</CardLabel>
        <TextInput value={deductionCalculation} onChange={(e) => setDeductionCalculation(e.target.value)} />

        <CardLabel>F. {t("TOTAL_FLOOR_AREA_AFTER_DEDUCTION")}</CardLabel>
        {renderSimpleAreaTable(totalFloorAreaAfterDeduction, setTotalFloorAreaAfterDeduction)}

        <CardLabel>G. {t("TOTAL_FLOOR_AREA_BEFORE_DEDUCTION")}</CardLabel>
        {renderSimpleAreaTable(totalFloorAreaBeforeDeduction, setTotalFloorAreaBeforeDeduction)}

        <CardLabel>H. {t("COVERAGE")}</CardLabel>
        <TextInput value={coverage} onChange={(e) => setCoverage(e.target.value)} />

        <CardLabel>I. {t("FLOOR_AREA_RATIO")}</CardLabel>
        <TextInput value={floorAreaRatio} onChange={(e) => setFloorAreaRatio(e.target.value)} />
      </FormStep>
    </React.Fragment>
  );
};

export default Form22A;
