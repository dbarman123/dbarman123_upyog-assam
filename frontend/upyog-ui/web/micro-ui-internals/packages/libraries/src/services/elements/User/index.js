import React from "react";
import ReactDOM from "react-dom";
import Urls from "../../atoms/urls";
import { Request, ServiceRequest } from "../../atoms/Utils/Request";
import { Storage } from "../../atoms/Utils/Storage";
import EPramaanLogoutLoader from "./EPramaanLogoutLoader";

export const UserService = {
  authenticate: (details) => {
    const data = new URLSearchParams();
    Object.entries(details).forEach(([key, value]) => data.append(key, value));
    data.append("scope", "read");
    data.append("grant_type", "password");
    return ServiceRequest({
      serviceName: "authenticate",
      url: Urls.Authenticate,
      data,
      headers: {
        authorization: `Basic ${window?.globalConfigs?.getConfig("JWT_TOKEN")||"ZWdvdi11c2VyLWNsaWVudDo="}`,
        "Content-Type": "application/x-www-form-urlencoded",
      },
    });
  },
  logoutUser: async () => {
    let user = UserService.getUser();
    if (!user || !user.info || !user.access_token) return { ePramaanInitiated: false };
    const { type } = user.info;
    const tenantId = type === "CITIZEN" ? Digit.ULBService.getStateId() : Digit.ULBService.getCurrentTenantId();
    
    // Check if ePramaan session exists
    const sessionId = Digit.SessionStorage.get("epramaan_sessionId");
    const sub = Digit.SessionStorage.get("epramaan_sub");
    let ePramaanInitiated = false;

    // ePramaan logout flow - if session exists, get form data and submit
    if (sessionId && sub) {
      try {
        // Step 1: Get ePramaan logout form data from backend
        const formDataResponse = await ServiceRequest({
          serviceName: "getEPramaanLogoutData",
          url: Urls.EPramaanLogoutData,
          data: { sessionId, sub, tenantId },
          auth: true,
        });

        if (formDataResponse) {
      // Step 2: COMPLETE UPYOG logout first (wait for it)
      try {
        await Promise.race([
          ServiceRequest({
            serviceName: "logoutUser",
            url: Urls.UserLogout,
            data: { access_token: user?.access_token },
            auth: true,
            params: { tenantId },
          }),
          new Promise((_, reject) => 
            setTimeout(() => reject(new Error('Logout timeout')), 3000)
          )
        ]);
        } catch (logoutError) {
          console.error("Logout error (continuing with ePramaan):", logoutError);
        }
          // Step 3: Clear UPYOG session storage NOW (before ePramaan redirect)
          window.localStorage.clear();
          window.sessionStorage.clear();
          
          // Step 4: Set flag for when we return from ePramaan
          localStorage.setItem("SSO_REDIRECTING", "true");
          await new Promise(resolve => setTimeout(resolve, 100));
          // Step 6: NOW submit form to ePramaan
          const form = document.createElement("form");
          form.method = "POST";
          form.action = Urls.ePramaan.logoutUrl;
          form.style.display = "none";

          const dataInput = document.createElement("input");
          dataInput.type = "hidden";
          dataInput.name = "data";
          dataInput.value = JSON.stringify(formDataResponse);
          form.appendChild(dataInput);

          document.body.appendChild(form);
          ePramaanInitiated = true;
          
          form.submit();
          return { ePramaanInitiated };
        }
      } catch (error) {
        console.error("ePramaan logout error:", error);
      }
    }

    // Normal UPYOG logout (no ePramaan session)
    await ServiceRequest({
      serviceName: "logoutUser",
      url: Urls.UserLogout,
      data: { access_token: user?.access_token },
      auth: true,
      params: { tenantId: type === "CITIZEN" ? Digit.ULBService.getStateId() : Digit.ULBService.getCurrentTenantId() },
    });

    return { ePramaanInitiated };
  },

  getType: () => {
    return Storage.get("userType") || "citizen";
  },
  setType: (userType) => {
    Storage.set("userType", userType);
    Storage.set("user_type", userType);
  },
  getUser: () => {
    return Digit.SessionStorage.get("User");
  },


  logout: async () => {
    // **HIDE MAIN APP**
    const appRoot = document.getElementById("root");
    if (appRoot) {
      appRoot.style.display = "none";
    }
    
    // **SHOW LOGOUT LOADER COMPONENT**
    const loaderContainer = document.createElement("div");
    loaderContainer.id = "logout-loader-container";
    document.body.appendChild(loaderContainer);
    
    ReactDOM.render(<EPramaanLogoutLoader />, loaderContainer);
  const userType = UserService.getType();

  let result;
  try {
    result = await UserService.logoutUser();
  } catch (err) {
    console.error("[Logout] logoutUser() threw an error:", err);
  }

  // If ePramaan SSO logout happened → storage already cleared, form submitted
  if (result?.ePramaanInitiated) {
    console.log("ePramaan logout initiated, waiting for redirect...");
    return;
  }

  // Normal logout cleanup (ONLY if SSO not triggered)
  window.localStorage.clear();
  window.sessionStorage.clear();

  if (userType === "citizen") {
    window.location.replace("/upyog-ui/citizen");
  } else {
    window.location.replace("/upyog-ui/employee/user/language-selection");
  }
},

  sendOtp: (details, stateCode) =>
    ServiceRequest({
      serviceName: "sendOtp",
      url: Urls.OTP_Send,
      data: details,
      auth: false,
      params: { tenantId: stateCode },
    }),
  setUser: (data) => {
    return Digit.SessionStorage.set("User", data);
  },
  setExtraRoleDetails: (data) => {
    const userDetails = Digit.SessionStorage.get("User");
    return Digit.SessionStorage.set("User", { ...userDetails, extraRoleInfo: data });
  },
  getExtraRoleDetails: () => {
    return Digit.SessionStorage.get("User")?.extraRoleInfo;
  },
  registerUser: (details, stateCode) =>
    ServiceRequest({
      serviceName: "registerUser",
      url: Urls.RegisterUser,
      data: {
        User: details,
      },
      params: { tenantId: stateCode },
    }),
  updateUser: async (details, stateCode) =>
    ServiceRequest({
      serviceName: "updateUser",
      url: Urls.UserProfileUpdate,
      auth: true,
      data: {
        user: details,
      },
      params: { tenantId: stateCode },
    }),
   
    //create address for user
      createAddressV2: async (details, stateCode, userUuid) =>
        ServiceRequest({
          serviceName: "createAddress",
          url: Urls.UserCreateAddressV2,
          auth: true,
          data: {
            address: details,
            userUuid: userUuid,
          },
          params: { tenantId: stateCode },
        }),
  hasAccess: (accessTo) => {
    const user = Digit.UserService.getUser();
    if (!user || !user.info) return false;
    const { roles } = user.info;
    return roles && Array.isArray(roles) && roles.filter((role) => accessTo.includes(role.code)).length;
  },

  changePassword: (details, stateCode) =>
    ServiceRequest({
      serviceName: "changePassword",
      url: Digit.SessionStorage.get("User")?.info ? Urls.ChangePassword1 : Urls.ChangePassword,
      data: {
        ...details,
      },
      auth: true,
      params: { tenantId: stateCode },
    }),

  employeeSearch: (tenantId, filters) => {
    return Request({
      url: Urls.EmployeeSearch,
      params: { tenantId, ...filters },
      auth: true,
    });
  },
  userSearch: async (tenantId, data, filters) => {
    return Request({
      url: Urls.UserSearch,
      params: { ...filters },
      method: "POST",
      auth: true,
      userService: true,
      data: data.pageSize ? { tenantId, ...data } : { tenantId, ...data, pageSize: "100" },
    });
  },
  userCreate: async (tenantId, user, filters) => {
    return Request({
      url: Urls.UserCreate,
      params: { ...filters },
      method: "POST",
      auth: false,
      userService: false,
      data: { user: {tenantId, ...user} } ,
    });
  },
  // user search for user profile
  userSearchNewV2: async (tenantId, data, filters) => {
    return Request({
      url: Urls.UserSearchNewV2,
      params: { ...filters },
      method: "POST",
      auth: true,
      userService: true,
      data: data.pageSize ? { tenantId, ...data } : { tenantId, ...data, pageSize: "100" },
    });
  },
  //update address for user
  updateAddressV2: async (details, stateCode) =>
    ServiceRequest({
      serviceName: "updateAddress",
      url: Urls.UserUpdateAddressV2,
      auth: true,
      data: {
        address: details
      },
      params: { tenantId: stateCode },
    })
};
