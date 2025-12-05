import React, { useEffect, useState, Fragment } from "react";
import { FormStep, CardLabel, Dropdown, TextInput } from "@upyog/digit-ui-react-components";

const AreaMapping = ({ t, config, onSelect, formData, searchResult }) => {

  // State for dropdown options
  const [districts, setDistricts] = useState([]);
  const [planningAreas, setPlanningAreas] = useState([]);
  const [ppAuthorities, setPpAuthorities] = useState([]);
  const [concernedAuthorities, setConcernedAuthorities] = useState([]);
  const [bpAuthorities, setBpAuthorities] = useState([]);
  const [revenueVillages, setRevenueVillages] = useState([]);
  const [wards, setWards] = useState([]);
  const [villages, setVillages] = useState([]);
  const stateId = Digit.ULBService.getStateId();
  
 // State for all dropdown values
const [district, setDistrict] = useState(formData?.areaMapping?.district || (searchResult?.areaMapping?.district ? { code: searchResult.areaMapping.district, name: searchResult.areaMapping.district, i18nKey: searchResult.areaMapping.district } : ""));

const [planningArea, setPlanningArea] = useState(formData?.areaMapping?.planningArea || (searchResult?.areaMapping?.planningArea ? { code: searchResult.areaMapping.planningArea, name: searchResult.areaMapping.planningArea, i18nKey: searchResult.areaMapping.planningArea } : ""));

const [ppAuthority, setPpAuthority] = useState(formData?.areaMapping?.ppAuthority || (searchResult?.areaMapping?.planningPermitAuthority ? { code: searchResult.areaMapping.planningPermitAuthority, name: searchResult.areaMapping.planningPermitAuthority, i18nKey: searchResult.areaMapping.planningPermitAuthority } : ""));

const [concernedAuthority, setConcernedAuthority] = useState(formData?.areaMapping?.concernedAuthority || (searchResult?.areaMapping?.concernedAuthority ? { code: searchResult.areaMapping.concernedAuthority, name: searchResult.areaMapping.concernedAuthority, i18nKey: searchResult.areaMapping.concernedAuthority } : ""));

const [bpAuthority, setBpAuthority] = useState(formData?.areaMapping?.bpAuthority || (searchResult?.areaMapping?.buildingPermitAuthority ? { code: searchResult.areaMapping.buildingPermitAuthority, name: searchResult.areaMapping.buildingPermitAuthority, i18nKey: searchResult.areaMapping.buildingPermitAuthority } : ""));

const [revenueVillage, setRevenueVillage] = useState(formData?.areaMapping?.revenueVillage || (searchResult?.areaMapping?.revenueVillage ? { code: searchResult.areaMapping.revenueVillage, name: searchResult.areaMapping.revenueVillage, i18nKey: searchResult.areaMapping.revenueVillage } : ""));

const [mouza, setMouza] = useState(formData?.areaMapping?.mouza || searchResult?.areaMapping?.mouza || "");

const [ward, setWard] = useState(formData?.areaMapping?.ward || (searchResult?.areaMapping?.ward ? { code: searchResult.areaMapping.ward, name: searchResult.areaMapping.ward, i18nKey: searchResult.areaMapping.ward } : ""));

const [villageName, setVillageName] = useState(formData?.areaMapping?.villageName || (searchResult?.areaMapping?.villageName ? { code: searchResult.areaMapping.villageName, name: searchResult.areaMapping.villageName, i18nKey: searchResult.areaMapping.villageName } : ""));



  const { data : areaMappingData , isLoading} = Digit.Hooks.useEnabledMDMS(stateId, "egov-location", [{ name: "egov-location" }], {
    select: (data) => {
      const formattedData = data?.["egov-location"]?.["egov-location"]?.[0];
      return formattedData;
    },
  });


  const tenantMasterDetails =
  bpAuthority?.code && planningArea?.code
    ? [{ name: "tenants", ulbGrade: bpAuthority.code, planningArea: planningArea.code }]
    : [{ name: "tenants" }];

  const { data: tenantData } = Digit.Hooks.useEnabledMDMS(
    stateId, 
    "tenant", 
    tenantMasterDetails,
    {
      select: (data) => {
        const formattedData = data?.["tenant"]?.["tenants"];
        return formattedData?.filter((tenant) =>  (bpAuthority?.code === tenant?.city?.ulbGrade && planningArea.code===tenant?.city?.planningAreaCode));
      },
      enabled: !!bpAuthority?.code, // Only fetch when bpAuthority.code exists
    }
  );

  console.log("tenantData", bpAuthority,concernedAuthority);


  const { data: fetchedLocalities } = Digit.Hooks.useBoundaryLocalities(
    concernedAuthority?.code,
    bpAuthority?.code==="GRAM_PANCHAYAT" ? "village" : "revenuevillage",
    {
      enabled: !!concernedAuthority && !!bpAuthority,
    },
    t
  );




   // Initialize Districts
  useEffect(() => {
    if (areaMappingData?.districts) {
      const formattedDistricts = areaMappingData.districts.map((dist) => ({
        code: dist.districtCode,
        name: dist.districtName,
        i18nKey: dist.districtCode,
      })).sort((a,b)=> a.code.localeCompare(b.code));
      setDistricts(formattedDistricts);
    }
  }, [areaMappingData]);

    // ✅ Planning Areas (based on selected District)
useEffect(() => {
  if (district && areaMappingData?.districts) {
    const selectedDistrict = areaMappingData.districts.find(
      (d) => d.districtCode === district.code
    );
    const formattedPlanningAreas =
      selectedDistrict?.planningAreas?.map((area) => ({
        code: area.planningAreaCode,
        name: area.planningAreaName,
        i18nKey: area.planningAreaCode,
      })) || [];
    setPlanningAreas(formattedPlanningAreas);
  } else {
    setPlanningAreas([]);
  }
}, [district, areaMappingData]);

// ✅ PP Authority (comes from selected planning area)
useEffect(() => {
  if (district && planningArea && areaMappingData?.districts) {
    const selectedDistrict = areaMappingData.districts.find(
      (d) => d.districtCode === district.code
    );
    const selectedArea = selectedDistrict?.planningAreas?.find(
      (a) => a.planningAreaCode === planningArea.code
    );

    if (selectedArea?.ppAuthority) {
      setPpAuthorities([
        {
          code: selectedArea.ppAuthority.ppAuthorityCode,
          name: selectedArea.ppAuthority.ppAuthorityName,
          i18nKey: selectedArea.ppAuthority.ppAuthorityCode,
        },
      ]);
    } else {
      setPpAuthorities([]);
    }
  } else {
    setPpAuthorities([]);
  }
}, [district, planningArea, areaMappingData]);

// ✅ BP Authorities (comes from same selected planning area)
useEffect(() => {
  if (district && planningArea && areaMappingData?.districts) {
    const selectedDistrict = areaMappingData.districts.find(
      (d) => d.districtCode === district.code
    );
    const selectedArea = selectedDistrict?.planningAreas?.find(
      (a) => a.planningAreaCode === planningArea.code
    );

    const formattedBpAuthorities =
      selectedArea?.bpAuthorities?.map((auth) => ({
        code: auth.code,
        name: auth.name,
        i18nKey: auth.code,
      })) || [];
    setBpAuthorities(formattedBpAuthorities);
  } else {
    setBpAuthorities([]);
  }
}, [district, planningArea, areaMappingData]);


  // setting the concerned Authority based on tenants
    useEffect(() => {
    if (tenantData && tenantData.length > 0) {
      const formattedConcernedAuthorities = tenantData.map((tenant) => ({
        code: tenant.code,
        name: tenant.name, 
        i18nKey: tenant.name,
        ulbGrade: tenant.city?.ulbGrade,
        planningAreaCode: tenant.city?.planningAreaCode,
      }));
      setConcernedAuthorities(formattedConcernedAuthorities);
    } else {
      setConcernedAuthorities([]);
    }
  }, [tenantData]);

  // Update Wards / Revenue Villages / Villages dynamically based on fetched localities
useEffect(() => {
  if (Array.isArray(fetchedLocalities) && fetchedLocalities.length > 0) {
    
    // --- MUNICIPAL BOARD logic ---
    if (bpAuthority?.code === "MUNICIPAL_BOARD") {
      // Extract and format Wards
      const formattedWards = fetchedLocalities.map((ward) => ({
        code: ward.code,
        name: ward.name,
        i18nKey: ward.name,
        children: ward.children || [],
      }));

      // Flatten all Revenue Villages
      const formattedRevenueVillages = fetchedLocalities.flatMap((ward) =>
        (ward.children || []).map((child) => ({
          code: child.code,
          name: child.name,
          i18nKey: child.name,
          parentWardCode: ward.code,
        }))
      );

      setWards(formattedWards);
      setRevenueVillages(formattedRevenueVillages);
      setVillages([]); // clear villages for GP case

    } 
        // ---GRAM PANCHAYAT logic ---
        else if (bpAuthority?.code === "GRAM_PANCHAYAT") {
          const formattedVillages = fetchedLocalities.map((loc) => ({
            code: loc.code,
            name: loc.name,
            i18nKey: loc.name,
          }));
          setVillages(formattedVillages);
          setWards([]);
          setRevenueVillages([]);
        }
        
      } else {
        // Reset all if no data
        setVillages([]);
        setWards([]);
        setRevenueVillages([]);
      }
    }, [fetchedLocalities, bpAuthority]);



  // Custom handlers for dropdown changes
  const handleDistrictChange = (selectedDistrict) => {
    setDistrict(selectedDistrict);
    setPlanningArea("");
    setPpAuthority("");
    setConcernedAuthority("");
    setBpAuthority("");
    setRevenueVillage("");
    setMouza("");
    setWard("");
    setVillageName("");
  };

  const handlePlanningAreaChange = (selectedPlanningArea) => {
    setPlanningArea(selectedPlanningArea);
    setPpAuthority("");
    setConcernedAuthority("");
    setBpAuthority("");
    setRevenueVillage("");
    setMouza("");
    setWard("");
    setVillageName("");
  };

  const handlePpAuthorityChange = (selectedPpAuthority) => {
    setPpAuthority(selectedPpAuthority);
    setConcernedAuthority("");
    setRevenueVillage("");
    setWard("");
    setVillageName("");
  };

  const handleBpAuthorityChange = (selectedBpAuthority) => {
    setBpAuthority(selectedBpAuthority);
    setConcernedAuthority("");
    setRevenueVillage("");
    setWard("");
    setVillageName("");
  };

  const handleConcernedAuthorityChange = (selectedConcernedAuthority) => {
    setConcernedAuthority(selectedConcernedAuthority);
    setRevenueVillage("");
    setWard("");
    setVillageName("");
  };

  const handleWardChange = (selectedWard) => {
    setWard(selectedWard);
    setRevenueVillage("");
  };

  // Validation logic based on BP authority
  const getValidationLogic = () => {
    const baseValidation = !district || !planningArea || !ppAuthority || !bpAuthority || !concernedAuthority;
    
    if (bpAuthority?.code === "MUNICIPAL_BOARD") {
      return baseValidation || !ward || !revenueVillage;
    } else if (bpAuthority?.code === "GRAM_PANCHAYAT") {
      return baseValidation || !villageName;
    }
    
    return baseValidation;
  };

  // Go next
  const goNext = () => {
    let areaMappingStep = {
      district,
      planningArea,
      ppAuthority,
      concernedAuthority,
      bpAuthority,
      ...(bpAuthority?.code === "MUNICIPAL_BOARD" && { ward, revenueVillage }),
      ...(bpAuthority?.code === "GRAM_PANCHAYAT" && { villageName }),
      mouza
    };

    onSelect(config.key, { ...formData[config.key], ...areaMappingStep });
  };

  const onSkip = () => onSelect();

  return (
    <React.Fragment>
      <FormStep
        config={config}
        onSelect={goNext}
        onSkip={onSkip}
        t={t}
        isDisabled={getValidationLogic()}
      >
        <div>
          {/* District */}
          <CardLabel>{`${t("DISTRICT")}`} <span className="check-page-link-button">*</span></CardLabel>
          <Dropdown
            t={t}
            option={districts}
            optionKey="i18nKey"
            id="district"
            selected={district}
            select={handleDistrictChange}
            optionCardStyles={{ maxHeight: "300px", overflowY: "auto" }}
            placeholder={isLoading ? t("LOADING_DISTRICTS") : t("SELECT_DISTRICT")}
          />

          {/* Planning Area */}
          <CardLabel>{`${t("PLANNING_AREA")}`} <span className="check-page-link-button">*</span></CardLabel>
          <Dropdown
            t={t}
            option={planningAreas}
            optionKey="i18nKey" 
            selected={planningArea}
            select={handlePlanningAreaChange} 
            optionCardStyles={{ maxHeight: "300px", overflowY: "auto" }}
            placeholder={!district ? t("SELECT_DISTRICT_FIRST") : t("SELECT_PLANNING_AREA")} 
          />

          {/* PP Authority */}
          <CardLabel>{`${t("PP_AUTHORITY")}`} <span className="check-page-link-button">*</span></CardLabel>
          <Dropdown
            t={t}
            option={ppAuthorities}
            optionKey="i18nKey"
            selected={ppAuthority}
            select={handlePpAuthorityChange} 
            optionCardStyles={{ maxHeight: "300px", overflowY: "auto" }}
            placeholder={!planningArea ? t("SELECT_PLANNING_AREA_FIRST") : t("SELECT_PP_AUTHORITY")}
          />

          {/* BP Authority */}
          <CardLabel>{`${t("BP_AUTHORITY")}`} <span className="check-page-link-button">*</span></CardLabel>
          <Dropdown
            t={t}
            option={bpAuthorities}
            optionKey="i18nKey"
            selected={bpAuthority}
            select={handleBpAuthorityChange}
            optionCardStyles={{ maxHeight: "300px", overflowY: "auto" }}
            placeholder={t("SELECT_BP_AUTHORITY")}
          />

          {/* Concerned Authority - Dynamic Label */}
          {bpAuthority && (
            <>
              <CardLabel>{`${t(bpAuthority.code + "_NAME")}`} <span className="check-page-link-button">*</span></CardLabel>
              <Dropdown
                t={t}
                option={concernedAuthorities}
                optionKey="i18nKey"
                selected={concernedAuthority}
                select={handleConcernedAuthorityChange} 
                optionCardStyles={{ maxHeight: "300px", overflowY: "auto" }}
                placeholder={t("SELECT_CONCERNED_AUTHORITY")}
              />
            </>
          )}

          {/* Conditional fields based on BP authority */}
          {bpAuthority?.code === "MUNICIPAL_BOARD" && (
            <>
              {/* Ward */}
              <CardLabel>{`${t("WARD")}`} <span className="check-page-link-button">*</span></CardLabel>
              <Dropdown
                t={t}
                option={wards}
                optionKey="i18nKey"
                selected={ward}
                select={handleWardChange}
                optionCardStyles={{ maxHeight: "250px", overflowY: "auto" }}
                placeholder={!concernedAuthority ? t("SELECT_CONCERNED_AUTHORITY_FIRST") : t("SELECT_WARD")}
              />

              {/* Revenue Village */}
              <CardLabel>{`${t("REVENUE_VILLAGE")}`} <span className="check-page-link-button">*</span></CardLabel>
              <Dropdown
                t={t}
                // option={revenueVillages}
                option={revenueVillages.filter(rv => rv.parentWardCode === ward?.code)}
                optionKey="i18nKey"
                id="revenueVillage"
                selected={revenueVillage}
                select={setRevenueVillage}
                optionCardStyles={{ maxHeight: "300px", overflowY: "auto" }}
                placeholder={!ward ? t("SELECT_WARD_FIRST") : t("SELECT_REVENUE_VILLAGE")}
              />
            </>
          )}

          {bpAuthority?.code === "GRAM_PANCHAYAT" && (
            <>
              {/* Village Name */}
              <CardLabel>{`${t("VILLAGE_NAME")}`} <span className="check-page-link-button">*</span></CardLabel>
              <Dropdown
                t={t}
                option={villages}
                optionKey="i18nKey"
                selected={villageName}
                select={setVillageName}
                optionCardStyles={{ maxHeight: "300px", overflowY: "auto" }}
                placeholder={!concernedAuthority ? t("SELECT_CONCERNED_AUTHORITY_FIRST") : t("SELECT_VILLAGE")}
              />
            </>
          )}

          {/* Mouza - Always text input */}
          <CardLabel>{`${t("MOUZA")}`}</CardLabel>
          <TextInput
            t={t}
            name="mouza"
            value={mouza}
            placeholder={`${t("ENTER_MOUZA_NAME")}`}
            onChange={(e) =>
              setMouza(
                e.target.value.replace(/[^a-zA-Z0-9\s]/g, "")
              )
            }
            ValidationRequired={true}
            pattern="^[A-Za-z0-9 ]+$"
            title={t("BPA_NAME_ERROR_MESSAGE")}
          />
        </div>
      </FormStep>
    </React.Fragment>
  );
};

export default AreaMapping;