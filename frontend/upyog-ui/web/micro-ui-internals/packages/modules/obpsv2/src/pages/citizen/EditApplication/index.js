import React, { useMemo } from "react";
import { useTranslation } from "react-i18next";
import { useQueryClient } from "react-query";
import {
  Redirect,
  Route,
  Switch,
  useHistory,
  useLocation,
  useRouteMatch,
  useParams
} from "react-router-dom";
import { editApplicationConfig } from "../../../config/editApplicationConfig";
import { Loader } from "@upyog/digit-ui-react-components";
import useBPAV2Search from "../../../../../../libraries/src/hooks/obpsv2/useBPAV2Search";

const Edit = () => {
  const queryClient = useQueryClient();
  const { applicationNo } = useParams();
  const match = useRouteMatch();
  const { t } = useTranslation();
  const { pathname } = useLocation();
  const history = useHistory();
  const stateId = Digit.ULBService.getStateId();

  const [params, setParams, clearParams] = Digit.Hooks.useSessionStorage("OBPSV2_CREATE", {});
  const tenantId = "pg.citya";
  const { isLoading, isError, error, data, refetch } =Digit.Hooks.obpsv2.useBPASearchApi({
    tenantId,
    filters: { applicationNo },
    config: { staleTime: Infinity, cacheTime: Infinity }
  });
  const config = useMemo(() => {
    let merged = [];
    editApplicationConfig.forEach((obj) => {
      merged = merged.concat(obj.body.filter((a) => !a.hideInCitizen));
    });
    merged.indexRoute = "area-mapping";
    return merged;
  }, []);

  if (isLoading) {
    return <Loader />;
  }

  const goNext = (skipStep, index, isAddMultiple, key) => {
    let currentPath = pathname.split("/").pop(),
      lastchar = currentPath.charAt(currentPath.length - 1),
      isMultiple = false,
      nextPage;
    if (Number(parseInt(currentPath)) || currentPath == "0" || currentPath == "-1") {
      if (currentPath == "-1" || currentPath == "-2") {
        currentPath = pathname.slice(0, -3);
        currentPath = currentPath.split("/").pop();
        isMultiple = true;
      } else {
        currentPath = pathname.slice(0, -2);
        currentPath = currentPath.split("/").pop();
        isMultiple = true;
      }
    } else {
      isMultiple = false;
    }
    if (!isNaN(lastchar)) {
      isMultiple = true;
    }
    let { nextStep = {} } = config.find((routeObj) => routeObj.route === (currentPath || "0")) || {};
    let redirectWithHistory = history.push;
    if (skipStep) {
      redirectWithHistory = history.replace;
    }
    if (isAddMultiple) {
      nextStep = key;
    }
    if (nextStep === null) {
      return redirectWithHistory(`${match.url}/check`);
    }
    if (!isNaN(nextStep.split("/").pop())) {
      nextPage = `${match.url}/${nextStep}`;
    } else {
      nextPage =
        isMultiple && nextStep !== "map"
          ? `${match.url}/${nextStep}/${index}`
          : `${match.url}/${nextStep}`;
    }
    redirectWithHistory(nextPage);
  };

  const acknowledgement = async () => {
    history.push(`${match.url}/acknowledgement`);
  };

  function handleSelect(key, formData, skipStep, index, isAddMultiple = false) {
    setParams({ ...params, ...{ [key]: { ...params[key], ...formData } } });
    goNext(skipStep, index, isAddMultiple, key);
  }

  const handleSkip = () => {};
  const handleMultiple = () => {};

  const onSuccess = () => {
    clearParams();
    queryClient.invalidateQueries("OBPSV2_CREATE");
  };

  const CheckPage = Digit?.ComponentRegistryService?.getComponent("CheckPage");
  const BPAAcknowledgement = Digit?.ComponentRegistryService?.getComponent("BPAAcknowledgement");

  return (
    <Switch>
      {config.map((routeObj, index) => {
        const { component, texts, inputs, key } = routeObj;
        const Component =
          typeof component === "string"
            ? Digit.ComponentRegistryService.getComponent(component)
            : component;

        return (
          <Route path={`${match.url}/${routeObj.route}`} key={index}>
            <Component
              config={{ texts, inputs, key }}
              onSelect={handleSelect}
              onSkip={handleSkip}
              onAdd={handleMultiple}
              t={t}
              formData={params}
              searchResult={data?.bpa?.[0]}
            />
          </Route>
        );
      })}

      <Route path={`${match.url}/check`}>
        <CheckPage onSubmit={acknowledgement} value={params} searchResult={data} />
      </Route>

      <Route path={`${match.url}/acknowledgement`}>
        <BPAAcknowledgement data={params} searchResult={data} onSuccess={onSuccess} />
      </Route>

      <Route>
        <Redirect to={`${match.url}/${config.indexRoute}`} />
      </Route>
    </Switch>
  );
};

export default Edit;