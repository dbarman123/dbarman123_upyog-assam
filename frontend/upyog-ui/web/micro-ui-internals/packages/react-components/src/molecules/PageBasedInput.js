import React, { useMemo } from "react";
import Card from "../atoms/Card";
import SubmitBar from "../atoms/SubmitBar";

const PageBasedInput = ({ children, texts, onSubmit }) => {
  const barHeight = 64;
  const wrapperStyle = useMemo(() => ({
    paddingBottom: `${barHeight}px`,
  }), [barHeight]);
  // This styling keeps the submit button fixed at the bottom of the viewport, ensuring it stays visible and accessible as the user scrolls.
  const buttonStyle = useMemo(() => ({
    position: "fixed",
    bottom: 0,
    left: 0,
    width: "100%",
    height: `${barHeight}px`,
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    background: "#fff",
    padding: "8px 16px",
    boxSizing: "border-box",
    boxShadow: "0 -2px 8px rgba(0,0,0,0.08)",
    zIndex: 2000,
  }), [barHeight]);
  return (
    <div className="PageBasedInputWrapper PageBased" style={wrapperStyle}>
      <Card>
        {children}
        <div style={{ display: "none" }}>
          <SubmitBar label={texts.submitBarLabel} onSubmit={onSubmit} />
        </div>
      </Card>
      <div style={buttonStyle} role="region" aria-label="Primary actions">
      <div style={{display: "flex", justifyContent: "flex-end", width: "100%", paddingRight: "16px"}}>
        <SubmitBar label={texts.submitBarLabel} onSubmit={onSubmit} style={{ width: "100%" }} />
      </div>
      </div>
    </div>
  );
};

export default PageBasedInput;