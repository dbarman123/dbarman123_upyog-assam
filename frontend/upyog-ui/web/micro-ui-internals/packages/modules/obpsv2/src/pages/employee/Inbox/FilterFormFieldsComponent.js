import React, { Fragment, useMemo } from "react";
import { FilterFormField, Dropdown, RemoveableTag, CheckBox, Loader, MultiSelectDropdown
 } from "@upyog/digit-ui-react-components";
import { useTranslation } from "react-i18next";
import { Controller, useWatch } from "react-hook-form";
const FilterFormFieldsComponent = ({
  statuses,
  isInboxLoading,
  registerRef,
  controlFilterForm,
  setFilterFormValue,
  filterFormState,
  getFilterFormValue,
  localitiesForEmployeesCurrentTenant,
  loadingLocalitiesForEmployeesCurrentTenant,
}) => {
  const selectrole = (listOfSelections, props) => {
    const res = listOfSelections.map( (propsData) => {
      const data = propsData[1]
        return data
     })
    return props.onChange(res);
  };

  const { t } = useTranslation();

  const tenantId = Digit.ULBService.getCurrentTenantId();


 
  const { data: areaMappingData, isLoading } = Digit.Hooks.useEnabledMDMS(
    "as", 
    "BPA", 
    [
      { name: "districts" }, 
    ],
    {
      select: (data) => {
        const formattedData = data?.BPA || {};
        return formattedData;
      },
    }
  );
  const districtOptions = areaMappingData?.districts?.map((district) => ({
    code: district.districtCode,
    name: district.districtCode,
    i18nKey: district.districtCode,
  })).sort((a, b) => a.code.localeCompare(b.code)) || [];
  if(localitiesForEmployeesCurrentTenant?.length===0){
    localitiesForEmployeesCurrentTenant=districtOptions
  }
  const statusOptions = [
    { i18nKey: "Pending RTP Approval", code: "PENDING_RTP_APPROVAL", value: t("BPA_PENDING_RTP_APPROVAL") },
    { i18nKey: "Edit Application", code: "EDIT_APPLICATION", value: t("BPA_EDIT_APPLICATION") },
    { i18nKey: "GIS Validation", code: "GIS_VALIDATION", value: t("BPA_GIS_VALIDATION") },
    { i18nKey: "Pending For Scrutiny", code: "PENDING_FOR_SCRUTINY", value: t("BPA_PENDING_FOR_SCRUTINY") },
    { i18nKey: "Send To Citizen", code: "SEND_TO_CITIZEN", value: t("BPA_SEND_TO_CITIZEN") },
    { i18nKey: "Citizen Approval", code: "CITIZEN_APPROVAL", value: t("BPA_CITIZEN_APPROVAL") },
    { i18nKey: "Pending DA Engineer", code: "PENDING_DA_ENGINEER", value: t("BPA_PENDING_DA_ENGINEER") },
    { i18nKey: "Pending DD AD Development Authority", code: "PENDING_DD_AD_DEVELOPMENT_AUTHORITY", value: t("BPA_PENDING_DD_AD_DEVELOPMENT_AUTHORITY") },
    { i18nKey: "Pending Chairman DA", code: "PENDING_CHAIRMAN_DA", value: t("BPA_PENDING_CHAIRMAN_DA") },
    { i18nKey: "Payment Pending", code: "PAYMENT_PENDING", value: t("BPA_PAYMENT_PENDING") },
    { i18nKey: "Forwarded To Technical Engineer", code: "FORWARDED_TO_TECHNICAL_ENGINEER", value: t("BPA_FORWARDED_TO_TECHNICAL_ENGINEER") },
    { i18nKey: "Forwarded To DD AD TCP", code: "FORWARDED_TO_DD_AD_TCP", value: t("BPA_FORWARDED_TO_DD_AD_TCP") },
    { i18nKey: "Pending Chairman President", code: "PENDING_CHAIRMAN_PRESIDENT", value: t("BPA_PENDING_CHAIRMAN_PRESIDENT") },
    { i18nKey: "Citizen Final Payment", code: "CITIZEN_FINAL_PAYMENT", value: t("BPA_CITIZEN_FINAL_PAYMENT") },
    { i18nKey: "Application Completed", code: "APPLICATION_COMPLETED", value: t("BPA_APPLICATION_COMPLETED") },
    { i18nKey: "Rejected", code: "REJECTED", value: t("BPA_REJECTED") }
  ].sort((a, b) => a.code.localeCompare(b.code));

  const assigneeOptions = [
    { code: "ASSIGNED_TO_ALL", i18nKey: "ES_INBOX_ASSIGNED_TO_ALL" },
    { code: "ASSIGNED_TO_ME", i18nKey: "ES_INBOX_ASSIGNED_TO_ME" },
  ];

  return (
    <Fragment>
      <FilterFormField>
        {/* <Controller
          name="applicationStatus"
          control={controlFilterForm}
          render={(props) => {
            const renderRemovableTokens = useMemo(()=>props?.value?.map((status, index) => {
              return (
                <RemoveableTag
                key={index}
                text={status.i18nKey}
                onClick={() => {
                  props.onChange(props?.value?.filter((loc) => loc.code !== status.code))
                }}
                />
                );
              }),[props?.value])
            return  <>
              <div className="filter-label sub-filter-label" style={{fontSize: "18px", fontWeight: "600"}}>{t("ES_APPLICATION_STATUS")}</div>
              <MultiSelectDropdown
              options={statusOptions ? statusOptions : []}
              optionsKey="i18nKey"
              props={props}
              isPropsNeeded={true}
              onSelect={selectrole}
              selected={props?.value}
              defaultLabel={t("ES_BPA_ALL_SELECTED")}
              defaultUnit={t("BPA_SELECTED_TEXT")}
              />
              <div className="tag-container">
                {renderRemovableTokens}
              </div>
            </>
          }
        }
        /> */}
        
      </FilterFormField>

      <FilterFormField>
        {/* <Dropdown
          option={localitiesForEmployeesCurrentTenant}
          selected={getFilterFormValue("locality")}
          optionKey="i18nKey"
          onAssignmentChange={(e) => setFilterFormValue("locality", e)}
          disable={loadingLocalitiesForEmployeesCurrentTenant}
          optionCardStyles={{ maxHeight: "200px" }}
          placeholder={t("ES_INBOX_LOCALITY")}
          t={t}
        /> */}
         <Controller
          name="district"
          control={controlFilterForm}
          render={(props) => {
            const renderRemovableTokens = useMemo(()=>props?.value?.map((locality, index) => {
              return (
                <RemoveableTag
                key={index}
                text={locality.i18nKey}
                onClick={() => {
                  props.onChange(props?.value?.filter((loc) => loc.code !== locality.code))
                }}
                />
                );
              }),[props?.value])
            return loadingLocalitiesForEmployeesCurrentTenant ? <Loader/> : <>
              <div className="filter-label sub-filter-label" style={{fontSize: "18px", fontWeight: "600"}}>{t("ES_INBOX_LOCALITY")}</div>
              <MultiSelectDropdown
              options={localitiesForEmployeesCurrentTenant ? localitiesForEmployeesCurrentTenant : []}
              optionsKey="i18nKey"
              props={props}
              isPropsNeeded={true}
              onSelect={selectrole}
              selected={props?.value}
              defaultLabel={t("ES_BPA_ALL_SELECTED")}
              defaultUnit={t("BPA_SELECTED_TEXT")}
              />
              <div className="tag-container">
                {renderRemovableTokens}
              </div>
            </>
          }
        }
        />
      </FilterFormField>

      
    </Fragment>
  );
};

export default FilterFormFieldsComponent;