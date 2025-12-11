import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { Switch, useLocation, Route } from "react-router-dom";
import { PrivateRoute, BackButton } from "@upyog/digit-ui-react-components";
import Inbox from "../employee/Inbox"
import Search from "./Search";
const App = ({ path }) => {
  const location = useLocation();
  const { t } = useTranslation();


  const BPACreate= Digit?.ComponentRegistryService?.getComponent("BPACreate");
  const RTPInbox= Digit?.ComponentRegistryService?.getComponent("RTPInbox");
  const RTPCreate= Digit?.ComponentRegistryService?.getComponent("RTPCreate");
  const BPAMyApplications = Digit?.ComponentRegistryService?.getComponent("BPAMyApplications");
  const BPAApplicationDetails = Digit?.ComponentRegistryService?.getComponent("BPAApplicationDetails");
  const BPAEdit= Digit?.ComponentRegistryService?.getComponent("BPAEdit");
  const OBPASCitizenHomeScreen = Digit?.ComponentRegistryService?.getComponent("OBPASCitizenHomeScreen")
  const isDocScreenAfterEdcr = sessionStorage.getItem("clickOnBPAApplyAfterEDCR") === "true" ? true : false
  return (
    <React.Fragment>
      <div className="ws-citizen-wrapper">
       {!location.pathname.includes("response") && !location.pathname.includes("openlink/stakeholder") && !location.pathname.includes("/acknowledgement") && !isDocScreenAfterEdcr && <BackButton style={{ border: "none" }}>{t("CS_COMMON_BACK")}</BackButton>}
      <Switch>
        <PrivateRoute path={`${path}/rtp/home`} component={OBPASCitizenHomeScreen}/>
        <PrivateRoute path={`${path}/my-applications`} component={BPAMyApplications}></PrivateRoute>
        <PrivateRoute path={`${path}/application/:acknowledgementIds/:tenantId`} component={BPAApplicationDetails}></PrivateRoute>
        <PrivateRoute path={`${path}/building-permit`} component={BPACreate}/>
        <PrivateRoute path={`${path}/rtp/apply`} component={RTPCreate}/>
        <PrivateRoute path={`${path}/rtp/inbox`} component={(props) => <RTPInbox {...props} parentRoute={path} />} />
        <PrivateRoute path={`${path}/rtp/search/application`} component={(props) => <Search {...props} parentRoute={path} />} />
        <PrivateRoute path={`${path}/search/application`} component={(props) => <Search {...props} parentRoute={path} />} />
        <PrivateRoute path={`${path}/editApplication/:applicationNo/:tenantId`} component={BPAEdit}/>
      </Switch>
      </div>
    </React.Fragment>
  );
};

export default App;