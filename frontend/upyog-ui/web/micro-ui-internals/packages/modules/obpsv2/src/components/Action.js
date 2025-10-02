import React, { useState, useEffect } from "react";
import {
  Card,
  Modal,
  TextArea,
  UploadFile,
  Heading,
  CloseBtn,
  CardLabel,
  CardLabelDesc,
  SubmitBar,
  Toast,
} from "@upyog/digit-ui-react-components";
import { OBPSV2Services } from "../../../../libraries/src/services/elements/OBPSV2";
import { useTranslation } from "react-i18next";

const Action = ({ selectedAction, applicationNo, closeModal }) => {
  const { t } = useTranslation();
  const [comments, setComments] = useState("");
  const [uploadedFile, setUploadedFile] = useState(null);
  const [actionError, setActionError] = useState(null);
  const [toastMessage, setToastMessage] = useState("");
  const [showToast, setShowToast] = useState(false);
  const [toast, setToast] = useState(false);
  const [oldRTPName, setOldRTPName] = useState();
  const [newRTPName, setNewRTPName] = useState();
  const [popup, setPopup] = useState(false);
  const [error, setError] = useState(null);

  const [assignResponse, setAssignResponse] = useState(null);
  const tenantId =  Digit.ULBService.getCitizenCurrentTenant(true) || Digit.ULBService.getCurrentTenantId();
  useEffect(() => {
    if (showToast || error) {
      const timer = setTimeout(() => {
        setToast(false);
        setError(null)
      }, 2000);
      return () => clearTimeout(timer);
    }
  }, [showToast, error]);
  useEffect(() => {
    if (selectedAction) {
      switch (selectedAction) {
        case "NEWRTP":
            setPopup(true);
          break;
        case "REJECT":
            setPopup(true);
          break;
        case "APPROVE":
          setPopup(true);
          break;
        case "SEND":
          setPopup(true);
          break;
          case "SEND_BACK_TO_RTP":
            setPopup(true);
            
            break;
        case "VALIDATE_GIS":
            setPopup(true);
            break;
        case "EDIT":

          const url = window.location.href;
          const redirectingUrl =`${window.location.origin}/upyog-ui/citizen/obpsv2/editApplication/${applicationNo}`;;
          redirectToPage(redirectingUrl);
          break;
          case "APPLY_FOR_SCRUTINY":
            let scrutinyurl=window.location.href;
            let scrutinyRedirectingUrl= scrutinyurl.split("/inbox")[0] + "/apply/home";
            redirectToPage(scrutinyRedirectingUrl);
            break;
        default:
          setPopup(false);
      }
    }
  }, [selectedAction]);

  function addComment(e) {
    setActionError(null);
    setComments(e.target.value);
  }
  function selectFile(e) {
    setUploadedFile(e.target.files[0]);
  }
  const Close = () => (
    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="#FFFFFF">
      <path d="M0 0h24v24H0V0z" fill="none" />
      <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12 19 6.41z" />
    </svg>
  );

  const CloseBtn = (props) => {
    return (
      <div className="icon-bg-secondary" onClick={props.onClick}>
        <Close />
      </div>
    );
  };

  const Heading = (props) => {
    return <h1 className="heading-m">{props.label}</h1>;
  };

  function closeToast() {
    setToast(false);
  }
  function close(state) {
    
    switch (state) {
      case popup:
        setPopup(!popup);
        break;
      default:
        break;
    }
  }
  function redirectToPage(redirectingUrl) {
    window.location.href = redirectingUrl;
  }

  async function onAssign(selectedAction, comments) {
    if (selectedAction === "REJECT" && !comments) {
      setActionError(t("CS_MANDATORY_COMMENTS"));
      return;
    }

    if (selectedAction === "APPROVE" && !comments) {
      setActionError(t("CS_MANDATORY_COMMENTS"));
      return;
    }

        const bpaDetails = await OBPSV2Services.search({
            tenantId,
            filters: { applicationNo },
            config: { staleTime: Infinity, cacheTime: Infinity }
        });
        if(bpaDetails?.bpa?.[0]){
            bpaDetails.bpa[0].workflow = {
            ...(bpaDetails.bpa[0].workflow || {}),
            action: selectedAction ,
            assignes: null,
            comments: null,
            };
            try {
                const response = await OBPSV2Services.update({BPA : bpaDetails?.bpa[0]}, tenantId);
                setToast(true);
                setPopup(false)
        
                
                return response;
            }
            catch(error){
                setError(error?.response?.data?.Errors[0].message)
                throw new Error(error?.response?.data?.Errors[0].message);
            }

            
        }
        setAssignResponse(response);
        setToast(true)
        await refetch();
    const updatedWorkflowDetails = await Digit.WorkflowService.getByBusinessId(tenantId, acknowledgementIds);
    setWorkflowDetails(updatedWorkflowDetails);
      
      setTimeout(() => setShowToast(false), 5000);
      closeModal(setPopup(false));
  }

  return (
    <React.Fragment>
      {selectedAction && popup && (
        <Modal
          headerBarMain={<Heading label={t(selectedAction)} />}
          headerBarEnd={<CloseBtn onClick={() => setPopup(false)} />}
          actionCancelLabel={t("CS_COMMON_CANCEL")}
          actionCancelOnSubmit={() => setPopup(false)}
          actionSaveLabel={t("CS_COMMON_CONFIRM")}
          actionSaveOnSubmit={() => {
        if(selectedAction==="APPROVE"||selectedAction==="SEND"||selectedAction==="REJECT"||selectedAction==="SEND_BACK_TO_RTP"||selectedAction==="VALIDATE_GIS")
        //setActionError(t("CS_MANDATORY_REASON"));
           onAssign(selectedAction, "Edit");
      if(selectedAction==="NEWRTP"&&!oldRTPName)
        setActionError(t("CS_OLD_RTP_NAME_MANDATORY"))
      if(selectedAction==="NEWRTP" &&!newRTPName)
        setActionError(t("CS_NEW_RTP_NAME_MANDATORY"))
        if(selectedAction === "REJECT" && !comments)
        setActionError(t("CS_MANDATORY_COMMENTS"));
        
       
      }}
          error={error}
          
        >
          <Card>
            <React.Fragment>
              {(selectedAction === "APPROVE" || selectedAction === "SEND" || selectedAction === "REJECT" || selectedAction==="SEND_BACK_TO_RTP") && (
                <div>
                  <CardLabel>{t("COMMENTS")}</CardLabel>
                  <TextArea
                    name="reason"
                    onChange={addComment}
                    value={comments}
                    maxLength={500}
                  />
                  <div style={{ textAlign: "right", fontSize: "12px", color: "#666" }}>
                    {comments.length}/500
                  </div>
                  <CardLabel>{t("CS_ACTION_SUPPORTING_DOCUMENTS")}</CardLabel>
                  <CardLabelDesc>{t("CS_UPLOAD_RESTRICTIONS")}</CardLabelDesc>
                  <UploadFile
                    id="approve-doc"
                    accept=".jpg"
                    onUpload={selectFile}
                    onDelete={() => setUploadedFile(null)}
                    message={uploadedFile ? `1 ${t("CS_ACTION_FILEUPLOADED")}` : t("CS_ACTION_NO_FILEUPLOADED")}
                  />
                </div>
              )}
              {selectedAction === "VALIDATE_GIS" && (
        <div>
         <CardLabel>{t("CS_ACTION_UPLOAD_LOCATION_FILE")}</CardLabel>
          <UploadFile
            id="pgr-doc"
            accept=".jpg"
            onUpload={selectFile}
            onDelete={() => setUploadedFile(null)}
            message={
              uploadedFile
                ? `1 ${t("CS_ACTION_FILEUPLOADED")}`
                : t("CS_ACTION_NO_FILEUPLOADED")
            }
          />
        </div>
      )}

              {selectedAction === "REJECT" && (
                <div>
                  <CardLabel>{t("COMMENTS")}</CardLabel>
                  <TextArea
                    name="reason"
                    onChange={addComment}
                    value={comments}
                    maxLength={500}
                  />
                  <div style={{ textAlign: "right", fontSize: "12px", color: "#666" }}>
                    {comments.length}/500
                  </div>
                </div>
              )}
            </React.Fragment>
          </Card>
        </Modal>
      )}

      {(toast||error) && (
                        <Toast
                          error={error ? error : null}
                          
                          label={error ? error : t(`ACTION_UPDATE_DONE_SUCCESSFULLY`)}
                          onClose={() => {
                            setToast(false);
                          }}
                        />
                      )}
    </React.Fragment>
  );
};

export default Action;
