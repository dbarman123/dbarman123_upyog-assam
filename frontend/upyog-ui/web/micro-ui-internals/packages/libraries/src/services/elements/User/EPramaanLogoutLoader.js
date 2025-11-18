import React from "react";
import { Loader } from "@upyog/digit-ui-react-components";

/**
 * EPramaanLogoutLoader Component
 * 
 * This component displays a full-screen loader with a "Logging out..." message.
 * It is intended to be shown during the ePramaan SSO logout process to inform
 * users that the logout is in progress.
 * Added Styles inline for simplicity and centering the loader.
 */
const EPramaanLogoutLoader = () => {
  return (
    <div
      style={{
        position: "fixed",
        top: 0,
        left: 0,
        width: "100vw",
        height: "100vh",
        background: "white",
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        justifyContent: "center",
        zIndex: 999999,
      }}
    >
      <div style={{ textAlign: "center" }}>
        <div
          style={{
            fontSize: "18px",
            color: "#333",
            marginBottom: "20px",
            fontWeight: "500",
          }}
        >
          Logging out...
        </div>
        <Loader />
      </div>
    </div>
  );
};

export default EPramaanLogoutLoader;