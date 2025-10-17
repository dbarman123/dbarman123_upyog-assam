import React from "react";
import { Card, CardHeader } from "@upyog/digit-ui-react-components";

const getMohallaLocale = (value = "", tenantId = "") => {
  let convertedValue = convertDotValues(tenantId);
  if (convertedValue == "NA" || !checkForNotNull(value)) {
    return "PGR_NA";
  }
  convertedValue = convertedValue.toUpperCase();
  return convertToLocale(value, `${convertedValue}_REVENUE`);
};

const convertDotValues = (value = "") => {
  return (
    (checkForNotNull(value) &&
      ((value.replaceAll && value.replaceAll(".", "_")) ||
        (value.replace && stringReplaceAll(value, ".", "_")))) ||
    "NA"
  );
};

const stringReplaceAll = (str = "", searcher = "", replaceWith = "") => {
  if (searcher == "") return str;
  while (str.includes(searcher)) {
    str = str.replace(searcher, replaceWith);
  }
  return str;
};

const checkForNotNull = (value = "") => {
  return value && value != null && value != undefined && value != ""
    ? true
    : false;
};

const getCityLocale = (value = "") => {
  let convertedValue = convertDotValues(value);
  if (convertedValue == "NA" || !checkForNotNull(value)) {
    return "PGR_NA";
  }
  convertedValue = convertedValue.toUpperCase();
  return convertToLocale(convertedValue, `TENANT_TENANTS`);
};

const convertToLocale = (value = "", key = "") => {
  let convertedValue = convertDotValues(value);
  if (convertedValue == "NA") {
    return "PGR_NA";
  }
  return `${key}_${convertedValue}`;
};

const capitalize = (text) => text.substr(0, 1).toUpperCase() + text.substr(1);
const ulbCamel = (ulb) =>
  ulb.toLowerCase().split(" ").map(capitalize).join(" ");

const formatformData = (formData) => {
  if (!formData || typeof formData !== "object") return [];

  return Object.entries(formData)
    .filter(([key]) => key !== "scrutinyDetails") 
    .map(([key, value]) => {
      let finalValue;

      if (value === null || value === undefined || value === "") {
        finalValue = "NA";
      } else if (typeof value === "object") {
        finalValue = JSON.stringify(value);
      } else {
        finalValue = value;
      }

      return {
        title: `FORM_${key.replace(/\s+/g, "_").toUpperCase()}`,
        value: finalValue,
      };
    });
};

const FormAcknowledgement = async (application, tenantInfo, t) => {

  const { formType, formData } = application || {};

  let detailsSection = [];

  if (formType && formData) {
    detailsSection = [
      {
        title: t(`${formType}_DETAILS`),
        values: formatformData(formData),
      },
    ];
  } else {
    detailsSection = [
      {
        title: t("BPA_BASIC_DETAILS_TITLE"),
        values: "NA",
      },
    ];
  }

  return {
    t: t,
    tenantId: tenantInfo?.code,
    name: `${t(tenantInfo?.i18nKey)} ${ulbCamel(
      t(
        `ULBGRADE_${tenantInfo?.city?.ulbGrade
          .toUpperCase()
          .replace(" ", "_")
          .replace(".", "_")}`
      )
    )}`,
    email: tenantInfo?.emailId,
    phoneNumber: tenantInfo?.contactNumber,
    heading: t("FORM_DETAILS"),
    applicationNumber: application?.applicationNo || "NA",
    details: detailsSection,
  };
};

export default FormAcknowledgement;
