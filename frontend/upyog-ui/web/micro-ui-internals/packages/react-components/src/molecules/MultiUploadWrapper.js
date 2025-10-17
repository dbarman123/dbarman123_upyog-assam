import React, { useState, useEffect, useReducer } from "react";
import UploadFile from "../atoms/UploadFile"

const displayError = ({ t, error, name }) => (
    <span style={{ display: 'flex', flexDirection: 'column' }}>
        <div className="validation-error">{t(error)}</div>
        <div className="validation-error">{`${t('ES_COMMON_DOC_FILENAME')} : ${name} ...`}</div>
    </span>
)

const fileValidationStatus = (file, regex, maxSize, t) => {
    const status = { valid: true, name: file?.name?.substring(0, 15), error: '' };
    if (!file) return;

    if (!regex.test(file.type) && (file.size / 1024 / 1024) > maxSize) {
        status.valid = false; status.error = t(`NOT_SUPPORTED_FILE_TYPE_AND_FILE_SIZE_EXCEEDED`);
    } else if (!regex.test(file.type)) {
        status.valid = false; status.error = t(`NOT_SUPPORTED_FILE_TYPE`);
    } else if ((file.size / 1024 / 1024) > maxSize) {
        status.valid = false; status.error = t(`FILE_SIZE_EXCEEDED`);
    }

    return status;
}
const checkIfAllValidFiles = (files, regex, maxSize, t) => {
    if (!files.length || !regex || !maxSize) return [{}, false];
    const messages = [];
    let isInValidGroup = false;
    for (let file of files) {
        const fileStatus = fileValidationStatus(file, regex, maxSize, t);
        if (!fileStatus.valid) {
            isInValidGroup = true;
        }
        messages.push(fileStatus);
    }
    return [messages, isInValidGroup];
}

// can use react hook form to set validations @neeraj-egov
const MultiUploadWrapper = ({ t, module = "PGR", tenantId = Digit.ULBService.getStateId(), getFormState, requestSpecifcFileRemoval, extraStyleName="",setuploadedstate = [], showHintBelow, hintText, allowedFileTypesRegex=/(.*?)(jpg|jpeg|webp|aif|png|image|pdf|msword|openxmlformats-officedocument)$/i, allowedMaxSizeInMB=10, acceptFiles = "image/*, .jpg, .jpeg, .webp, .aif, .png, .image, .pdf, .msword, .openxmlformats-officedocument, .dxf" }) => {
    const FILE_UPLOAD_INIT = "FILE_UPLOAD_INIT"
    const FILES_UPLOADED = "FILES_UPLOADED"
    const TARGET_FILE_REMOVAL = "TARGET_FILE_REMOVAL"

    const [fileErrors, setFileErrors] = useState([]);
    const [uploadStatus, setUploadStatus] = useState({}); // Track loading state of each file

    const uploadMultipleFilesInit = (state, payload) => {
        const { files } = payload;
        const filesData = Array.from(files);
        const existingFileNames = state.map(item => item[0]);
        const newUploads = filesData
            .filter(file => !existingFileNames.includes(file.name))
            .map((file) => [file.name, { file, fileStoreId: null, status: 'loading' }]);
        return [...state, ...newUploads];
    };
    const updateFileStoreId = (state, payload) => {
        const { fileName, fileStoreId } = payload;
        return state.map(([name, details]) => {
            if (name === fileName) {
                return [name, { ...details, fileStoreId }];
            }
            return [name, details];
        });
    }

    const removeFile = (state, payload) => {
        const __indexOfItemToDelete = state.findIndex(e => e[0] === payload.file.name)
        const mutatedState = state.filter((e, index) => index !== __indexOfItemToDelete)
        return [...mutatedState]
    }

    const uploadReducer = (state, action) => {
        switch (action.type) {
            case FILE_UPLOAD_INIT:
                return uploadMultipleFilesInit(state, action.payload);
            case FILES_UPLOADED:
                const { files, fileStoreIds } = action.payload;
                let currentState = [...state];
                files.forEach((file, index) => {
                    currentState = updateFileStoreId(currentState, {
                        fileName: file.name,
                        fileStoreId: fileStoreIds[index]
                    });
                });
                return currentState;
            case TARGET_FILE_REMOVAL:
                return removeFile(state, action.payload)
            default:
                return state;
        }
    };
    const LoadingSpinner = () => <div className="loading-spinner" />;
    const [state, dispatch] = useReducer(uploadReducer, [...setuploadedstate]);

    const onUploadMultipleFiles = async (e) => {
        setFileErrors([])
        const files = Array.from(e.target.files);
        if (!files.length) return;
        const [validationMsg, error] = checkIfAllValidFiles(files, allowedFileTypesRegex, allowedMaxSizeInMB, t);
        if (!error) {
            files.forEach((file) => {
                dispatch({
                    type: FILE_UPLOAD_INIT,
                    payload: { files: [file] }
                });
                setUploadStatus((prev) => ({ ...prev, [file.name]: 'loading' }));
            });

            try {
                const { data: { files: fileStoreIds } = {} } = await Digit.UploadServices.MultipleFilesStorage(module, e.target.files, tenantId);
                files.forEach((file, index) => {
                    dispatch({
                        type: FILES_UPLOADED,
                        payload: { files: [file], fileStoreIds: [fileStoreIds[index]] }
                    });
                    setUploadStatus((prev) => ({ ...prev, [file.name]: 'uploaded' }));
                });
            } catch (err) {
                files.forEach((file) => {
                    setUploadStatus((prev) => ({ ...prev, [file.name]: 'failed' }));
                });
                setFileErrors([{ valid: false, error: t('UPLOAD_FAILED'), name: '' }]);
            }
        } else {
            setFileErrors(validationMsg);
        }
    }

    useEffect(() => getFormState(state), [state])

    useEffect(() => {
        requestSpecifcFileRemoval ? dispatch({ type: TARGET_FILE_REMOVAL, payload: requestSpecifcFileRemoval }) : null
    }, [requestSpecifcFileRemoval])

    return (
        <div>
            <UploadFile
                onUpload={(e) => onUploadMultipleFiles(e)}
                removeTargetedFile={(fileDetailsData) => {
                    setUploadStatus((prev) => {
                        const newState = { ...prev };
                        delete newState[fileDetailsData.file.name];
                        return newState;
                    });
                    dispatch({ type: TARGET_FILE_REMOVAL, payload: fileDetailsData })
                }}
                uploadedFiles={state}
                multiple={true}
                showHintBelow={showHintBelow}
                hintText={hintText}
                extraStyleName={extraStyleName}
                onDelete={() => {
                    setFileErrors([])
                }}
                accept={acceptFiles}
            />
            <span style={{ display: 'flex' }}>
                {fileErrors.length ? fileErrors.map(({ valid, name, error }) => (
                    valid ? null : displayError({ t, error, name })
                )) : null}
            </span>
            <div>
                {state.map(([fileName]) => {
                    const status = uploadStatus[fileName];
                    return (
                        <div key={fileName}>
                            
                            {status === 'loading' && (
                                <div style={{ display: 'flex', gap: '8px' }}>
                                    <LoadingSpinner />
                                    <span>{fileName} uploading...</span>
                                </div>
                            )}
                            
                            {status === 'uploaded' && (
                                <div>{fileName} uploaded successfully!</div>
                            )}
                            {status === 'failed' && (
                                <div style={{ color: 'red' }}>{fileName} failed to upload</div>
                            )}
                        </div>
                    );
                })}
            </div>
        </div>
    );
};

export default MultiUploadWrapper